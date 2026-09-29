package org.sih26042.coteacher.core.pack

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import org.sih26042.coteacher.core.model.LanguagePackInfo
import org.sih26042.coteacher.core.model.PackLifecycleState
import org.sih26042.coteacher.data.LanguagePackRepository
import org.sih26042.coteacher.data.PackParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

/**
 * Production-Hardened Language Pack Lifecycle Manager for SIH26042.
 *
 * Implements strict state machine:
 * DOWNLOADED -> VALIDATING -> VALID -> ACTIVE
 *                      |         |
 *                      v         v
 *                    FAILED   ROLLED_BACK
 *
 * Invariants:
 * 1. Atomic installation: Staged in temporary storage; verified before committing.
 * 2. Fail-closed validation: Missing checksum, schema violation, or ZipSlip aborts immediately.
 * 3. Never activate invalid packs: System retains prior active pack upon failure.
 * 4. Controlled rollback: Reverts to previous stable pack upon runtime degradation.
 * 5. Generic multi-language discovery: Supports Santali, Mundari, Ho with accurate status labels.
 */
class LanguagePackLifecycleManager(
    private val context: Context,
    private val repository: LanguagePackRepository
) {
    companion object {
        private const val TAG = "PackLifecycleManager"
        const val CURRENT_APP_VERSION = "1.0.0"
        const val MAX_UNCOMPRESSED_SIZE_BYTES = 50 * 1024 * 1024L // 50 MB
        val FORBIDDEN_EXTENSIONS = setOf("sh", "exe", "so", "dex", "apk", "bin", "bat", "cmd", "dll")
    }

    data class ValidationReport(
        val isValid: Boolean,
        val errors: List<String>,
        val warnings: List<String>,
        val computedSha256: String = ""
    )

    private val _lifecycleEvents = MutableStateFlow<List<String>>(emptyList())
    val lifecycleEvents: StateFlow<List<String>> = _lifecycleEvents.asStateFlow()

    private fun logEvent(event: String) {
        Log.i(TAG, event)
        _lifecycleEvents.value = (_lifecycleEvents.value + event).takeLast(50)
    }

    /**
     * Atomically validates a candidate .slp archive.
     */
    fun validateCandidateArchive(archiveFile: File): ValidationReport {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (!archiveFile.exists() || archiveFile.length() == 0L) {
            return ValidationReport(isValid = false, errors = listOf("Archive file does not exist or is empty"), warnings = warnings)
        }

        var totalUncompressedBytes = 0L
        var computedSha256 = ""

        try {
            // 1. Compute archive SHA-256
            val packParser = PackParser(context)
            computedSha256 = FileInputStream(archiveFile).use { packParser.computeStreamSha256(it) }

            // 2. Open Zip & inspect structure
            val zip = ZipFile(archiveFile)
            val entries = zip.entries()

            var hasManifest = false
            var hasChecksums = false

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name

                // ZipSlip / Path Traversal check
                if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")) {
                    errors.add("Security violation: path traversal detected in entry '$name'")
                }

                // Executable extension check
                val ext = name.substringAfterLast('.', "").lowercase()
                if (ext in FORBIDDEN_EXTENSIONS) {
                    errors.add("Security violation: forbidden executable extension '.$ext' in '$name'")
                }

                if (name == "manifest.json") hasManifest = true
                if (name == "checksums.json") hasChecksums = true

                totalUncompressedBytes += entry.size
                if (totalUncompressedBytes > MAX_UNCOMPRESSED_SIZE_BYTES) {
                    errors.add("Security violation: uncompressed archive size exceeds limit of $MAX_UNCOMPRESSED_SIZE_BYTES bytes")
                    break
                }
            }

            if (!hasManifest) errors.add("Missing mandatory manifest.json")
            if (!hasChecksums) errors.add("Missing mandatory checksums.json")

            // 3. Manifest & Schema verification
            if (hasManifest) {
                val manifestEntry = zip.getEntry("manifest.json")
                val manifestStr = zip.getInputStream(manifestEntry).bufferedReader().use { it.readText() }
                val manifestJson = JSONObject(manifestStr)

                val packId = manifestJson.optString("pack_id")
                if (packId.isBlank()) errors.add("manifest.json missing 'pack_id'")

                val langCode = manifestJson.optString("language_code")
                if (langCode.isBlank()) errors.add("manifest.json missing 'language_code'")

                val minApp = manifestJson.optString("min_app_version", "1.0.0")
                if (minApp > CURRENT_APP_VERSION) {
                    errors.add("Pack requires minimum app version $minApp (current: $CURRENT_APP_VERSION)")
                }
            }

            // 4. Internal Checksum Verification
            if (hasChecksums && errors.isEmpty()) {
                val checksumsEntry = zip.getEntry("checksums.json")
                val checksumsStr = zip.getInputStream(checksumsEntry).bufferedReader().use { it.readText() }
                val checksumsJson = JSONObject(checksumsStr)

                val keys = checksumsJson.keys()
                while (keys.hasNext()) {
                    val relPath = keys.next()
                    val expectedHash = checksumsJson.getString(relPath)
                    val entry = zip.getEntry(relPath)
                    if (entry == null) {
                        errors.add("Declared file '$relPath' missing from archive")
                        continue
                    }
                    val actualHash = zip.getInputStream(entry).use { packParser.computeStreamSha256(it) }
                    if (!actualHash.equals(expectedHash, ignoreCase = true)) {
                        errors.add("Cryptographic hash mismatch for '$relPath': expected $expectedHash, got $actualHash")
                    }
                }
            }

            zip.close()
        } catch (e: Exception) {
            errors.add("Archive processing error: ${e.message}")
        }

        return ValidationReport(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            computedSha256 = computedSha256
        )
    }

    /**
     * Atomically installs a language pack from an input stream.
     */
    fun installPack(stream: InputStream, autoActivate: Boolean = false): Result<LanguagePackInfo> {
        val timestamp = System.currentTimeMillis()
        val stagingDir = File(context.cacheDir, "staging_pack_$timestamp")
        stagingDir.mkdirs()
        val tempArchive = File(stagingDir, "candidate.slp")

        try {
            logEvent("Stage 1: Streaming archive into staging buffer...")
            FileOutputStream(tempArchive).use { out ->
                stream.copyTo(out)
            }

            logEvent("Stage 2: Executing security and cryptographic validation...")
            val report = validateCandidateArchive(tempArchive)
            if (!report.isValid) {
                val errSummary = report.errors.joinToString("; ")
                logEvent("Validation FAILED: $errSummary")
                return Result.failure(IllegalStateException("Pack validation failed: $errSummary"))
            }

            logEvent("Stage 3: Validation successful. Committing archive atomically...")
            val zip = ZipFile(tempArchive)
            val manifestEntry = zip.getEntry("manifest.json")
            val manifestText = zip.getInputStream(manifestEntry).bufferedReader().use { it.readText() }
            val manifestJson = JSONObject(manifestText)
            val packId = manifestJson.getString("pack_id")
            zip.close()

            val installedDir = File(context.filesDir, "installed_packs")
            installedDir.mkdirs()
            val permanentArchive = File(installedDir, "$packId.slp")

            if (permanentArchive.exists()) {
                permanentArchive.delete()
            }
            if (!tempArchive.renameTo(permanentArchive)) {
                tempArchive.copyTo(permanentArchive, overwrite = true)
                tempArchive.delete()
            }

            val stats = manifestJson.optJSONObject("stats")
            val packInfo = LanguagePackInfo(
                packId = packId,
                languageCode = manifestJson.getString("language_code"),
                languageName = manifestJson.getString("language_name"),
                nativeName = manifestJson.getString("native_name"),
                version = manifestJson.getString("version"),
                primaryScript = manifestJson.getJSONObject("script").getString("primary_script_name"),
                isInstalled = true,
                isActive = false,
                phrasesCount = stats?.optInt("phrases_count", 0) ?: 0,
                flnVocabCount = stats?.optInt("fln_vocab_count", 0) ?: 0,
                worksheetsCount = stats?.optInt("worksheets_count", 0) ?: 0,
                sha256Checksum = report.computedSha256,
                statusLabel = "INSTALLED",
                contentStatus = manifestJson.optString("validation_status", "VALIDATED"),
                packSizeBytes = permanentArchive.length(),
                lifecycleState = PackLifecycleState.VALID
            )

            // Register in repository
            repository.registerInstalledPack(packInfo)
            logEvent("Stage 4: Pack '$packId' committed successfully (VALID).")

            if (autoActivate) {
                activatePack(packId)
            }

            return Result.success(packInfo)
        } catch (e: Exception) {
            logEvent("Installation error: ${e.message}")
            return Result.failure(e)
        } finally {
            stagingDir.deleteRecursively()
        }
    }

    /**
     * Activates an installed language pack after ensuring it is valid.
     */
    fun activatePack(packId: String): Result<LanguagePackInfo> {
        val pack = repository.availablePacks.value.find { it.packId == packId }
            ?: return Result.failure(IllegalArgumentException("Pack '$packId' is not found"))

        if (!pack.isInstalled) {
            return Result.failure(IllegalStateException("Pack '$packId' is not installed"))
        }

        val success = repository.switchActivePack(packId)
        return if (success) {
            logEvent("Activated pack: $packId (ACTIVE)")
            Result.success(repository.activePack.value.copy(lifecycleState = PackLifecycleState.ACTIVE))
        } else {
            logEvent("Activation failed for pack: $packId")
            Result.failure(IllegalStateException("Failed to activate pack: $packId"))
        }
    }

    /**
     * Rolls back to previous known valid language pack.
     */
    fun rollbackToPreviousPack(): Result<LanguagePackInfo> {
        val currentActive = repository.activePack.value
        logEvent("Initiating rollback from active pack: ${currentActive.packId}")

        val success = repository.rollbackToPreviousPack()
        return if (success) {
            val restored = repository.activePack.value
            logEvent("Rollback successful. Restored pack: ${restored.packId}")
            Result.success(restored.copy(lifecycleState = PackLifecycleState.ACTIVE))
        } else {
            logEvent("Rollback failed: No previous valid pack available")
            Result.failure(IllegalStateException("No prior valid pack available for rollback"))
        }
    }

    /**
     * Deactivates a pack if it is currently active.
     */
    fun deactivatePack(packId: String): Result<Unit> {
        val active = repository.activePack.value
        if (active.packId == packId) {
            // Must rollback or fallback to default
            val rollbackResult = rollbackToPreviousPack()
            return if (rollbackResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Cannot deactivate only active pack without fallback"))
            }
        }
        return Result.success(Unit)
    }

    /**
     * Upgrades an existing pack atomically.
     * If the upgrade candidate fails validation, the active pack is preserved untouched.
     */
    fun upgradePack(stream: InputStream): Result<LanguagePackInfo> {
        val priorActive = repository.activePack.value
        logEvent("Starting atomic upgrade. Prior active: ${priorActive.packId}")

        val installResult = installPack(stream, autoActivate = false)
        if (installResult.isFailure) {
            logEvent("Upgrade aborted due to validation failure. Retaining active pack: ${priorActive.packId}")
            return installResult
        }

        val newPack = installResult.getOrThrow()
        val activateResult = activatePack(newPack.packId)
        return if (activateResult.isSuccess) {
            logEvent("Upgrade complete. Now active: ${newPack.packId}")
            Result.success(newPack.copy(lifecycleState = PackLifecycleState.ACTIVE))
        } else {
            logEvent("Upgrade activation failed. Rolling back to ${priorActive.packId}...")
            rollbackToPreviousPack()
            Result.failure(IllegalStateException("Failed to activate upgraded pack"))
        }
    }
}
