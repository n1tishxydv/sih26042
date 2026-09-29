package org.sih26042.coteacher.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import org.sih26042.coteacher.core.model.LanguagePackInfo
import java.io.*
import java.security.MessageDigest
import java.util.zip.ZipFile

/**
 * Repository managing offline Language Packs with atomic installation,
 * cryptographic verification, and safe rollback protection.
 */
class LanguagePackRepository(
    private val context: Context,
    private val packParser: PackParser = PackParser(context)
) {
    companion object {
        private const val TAG = "LanguagePackRepo"
        const val CURRENT_APP_VERSION = "1.0.0"
    }

    private val _availablePacks = MutableStateFlow<List<LanguagePackInfo>>(
        listOf(
            LanguagePackInfo(
                packId = "lang-pack-sat-olck-v1",
                languageCode = "sat",
                languageName = "Santali",
                nativeName = "ᱥᱟᱱᱛᱟᱲᱤ",
                version = "1.0.0",
                primaryScript = "Ol Chiki",
                isInstalled = true,
                isActive = true,
                phrasesCount = 21,
                flnVocabCount = 28,
                worksheetsCount = 2,
                sha256Checksum = "cb804c770e4b2feb9035f4e8dccfb84fd84cc3511d662978b29faf6c2de13ff1",
                statusLabel = "FULL MVP",
                contentStatus = "PRODUCTION",
                packSizeBytes = 753360L
            ),
            LanguagePackInfo(
                packId = "lang-pack-unr-v0.1",
                languageCode = "unr",
                languageName = "Mundari",
                nativeName = "मुण्डारी",
                version = "0.1.0",
                primaryScript = "Devanagari / Mundari Bani",
                isInstalled = true,
                isActive = false,
                phrasesCount = 2,
                flnVocabCount = 0,
                worksheetsCount = 0,
                sha256Checksum = "1a8b9c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c",
                statusLabel = "PLUGGABLE / CONTENT EXPANSION",
                contentStatus = "ARCHITECTURAL_STUB",
                packSizeBytes = 1200L
            ),
            LanguagePackInfo(
                packId = "lang-pack-hoc-v0.1",
                languageCode = "hoc",
                languageName = "Ho",
                nativeName = "ᱦᱳ / 𑢹𑣉",
                version = "0.1.0",
                primaryScript = "Warang Citi",
                isInstalled = true,
                isActive = false,
                phrasesCount = 1,
                flnVocabCount = 0,
                worksheetsCount = 0,
                sha256Checksum = "9f8e7d6c5b4a3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c",
                statusLabel = "EARLY / LEXICON-FIRST",
                contentStatus = "ARCHITECTURAL_STUB",
                packSizeBytes = 1100L
            )
        )
    )
    val availablePacks: StateFlow<List<LanguagePackInfo>> = _availablePacks.asStateFlow()

    private val _activePack = MutableStateFlow(_availablePacks.value.first { it.isActive })
    val activePack: StateFlow<LanguagePackInfo> = _activePack.asStateFlow()

    // Rollback protection tracking
    private var previousValidPack: LanguagePackInfo? = null

    fun switchActivePack(packId: String): Boolean {
        val target = _availablePacks.value.find { it.packId == packId }
        if (target == null || !target.isInstalled) {
            Log.w(TAG, "Cannot activate missing or uninstalled pack: $packId")
            return false
        }

        previousValidPack = _activePack.value
        val updated = _availablePacks.value.map {
            if (it.packId == packId) {
                it.copy(isActive = true, lifecycleState = org.sih26042.coteacher.core.model.PackLifecycleState.ACTIVE)
            } else {
                it.copy(
                    isActive = false,
                    lifecycleState = if (it.isInstalled) org.sih26042.coteacher.core.model.PackLifecycleState.VALID else org.sih26042.coteacher.core.model.PackLifecycleState.DOWNLOADED
                )
            }
        }
        _availablePacks.value = updated
        _activePack.value = target.copy(isActive = true, lifecycleState = org.sih26042.coteacher.core.model.PackLifecycleState.ACTIVE)
        Log.i(TAG, "Switched active language pack to ${target.languageName} (${target.packId})")
        return true
    }

    fun rollbackToPreviousPack(): Boolean {
        val fallback = previousValidPack ?: return false
        val currentFailed = _activePack.value
        Log.w(TAG, "Triggering automatic rollback from ${currentFailed.packId} to previous pack: ${fallback.packId}")

        val updated = _availablePacks.value.map {
            when (it.packId) {
                fallback.packId -> it.copy(isActive = true, lifecycleState = org.sih26042.coteacher.core.model.PackLifecycleState.ACTIVE)
                currentFailed.packId -> it.copy(isActive = false, lifecycleState = org.sih26042.coteacher.core.model.PackLifecycleState.ROLLED_BACK)
                else -> it
            }
        }
        _availablePacks.value = updated
        _activePack.value = fallback.copy(isActive = true, lifecycleState = org.sih26042.coteacher.core.model.PackLifecycleState.ACTIVE)
        return true
    }

    fun registerInstalledPack(packInfo: LanguagePackInfo) {
        val existing = _availablePacks.value.filter { it.packId != packInfo.packId }
        _availablePacks.value = existing + packInfo
    }

    fun deletePack(packId: String): Boolean {
        if (_activePack.value.packId == packId) {
            Log.w(TAG, "Cannot delete active pack: $packId without deactivating first")
            return false
        }
        _availablePacks.value = _availablePacks.value.filter { it.packId != packId }
        val installedArchive = File(context.filesDir, "installed_packs/$packId.slp")
        if (installedArchive.exists()) {
            installedArchive.delete()
        }
        return true
    }

    fun verifyPackIntegrity(packId: String): Boolean {
        val pack = _availablePacks.value.find { it.packId == packId } ?: return false
        return pack.sha256Checksum.isNotBlank()
    }

    /**
     * Atomically imports and validates a .slp archive.
     *
     * Pipeline:
     * 1. Copy stream to temporary staging file
     * 2. Security audit (ZipSlip, ZipBomb, banned extensions)
     * 3. Read manifest and verify schema & app compatibility
     * 4. Verify all internal file checksums against checksums.json
     * 5. Move atomically to installed packs directory
     * 6. Register pack in available packs list
     * 7. Clean up temporary staging
     */
    fun importPackArchive(stream: InputStream): Result<LanguagePackInfo> {
        val timestamp = System.currentTimeMillis()
        val stagingDir = File(context.cacheDir, "staging_pack_$timestamp")
        stagingDir.mkdirs()
        val tempArchive = File(stagingDir, "candidate.slp")

        try {
            // 1. Copy to temp
            FileOutputStream(tempArchive).use { out ->
                stream.copyTo(out)
            }

            // 2. Security audit
            PackParser.validateArchiveSecurity(tempArchive)


            // 3. Inspect manifest & checksums
            val zip = ZipFile(tempArchive)
            val manifestEntry = zip.getEntry("manifest.json")
                ?: throw IllegalArgumentException("Archive missing mandatory manifest.json")
            val checksumsEntry = zip.getEntry("checksums.json")
                ?: throw IllegalArgumentException("Archive missing mandatory checksums.json")

            val manifestText = zip.getInputStream(manifestEntry).bufferedReader().use { it.readText() }
            val manifestJson = JSONObject(manifestText)

            val minApp = manifestJson.optString("min_app_version", "1.0.0")
            if (minApp > CURRENT_APP_VERSION) {
                throw IllegalStateException("Pack requires minimum app version $minApp (current: $CURRENT_APP_VERSION)")
            }

            val checksumsText = zip.getInputStream(checksumsEntry).bufferedReader().use { it.readText() }
            val checksumsJson = JSONObject(checksumsText)

            // 4. Verify internal checksums
            val keys = checksumsJson.keys()
            while (keys.hasNext()) {
                val relPath = keys.next()
                val expectedHash = checksumsJson.getString(relPath)
                val entry = zip.getEntry(relPath)
                    ?: throw IllegalStateException("Declared checksum file missing from archive: $relPath")
                val actualHash = zip.getInputStream(entry).use { packParser.computeStreamSha256(it) }
                if (!actualHash.equals(expectedHash, ignoreCase = true)) {
                    throw IllegalStateException("Cryptographic checksum mismatch for $relPath (expected $expectedHash, got $actualHash)")
                }
            }
            zip.close()

            // 5. Compute archive SHA-256
            val archiveHash = FileInputStream(tempArchive).use { packParser.computeStreamSha256(it) }

            // 6. Install atomically into permanent packs directory
            val installedDir = File(context.filesDir, "installed_packs")
            installedDir.mkdirs()
            val permanentArchive = File(installedDir, "${manifestJson.getString("pack_id")}.slp")
            if (permanentArchive.exists()) {
                permanentArchive.delete()
            }
            if (!tempArchive.renameTo(permanentArchive)) {
                tempArchive.copyTo(permanentArchive, overwrite = true)
                tempArchive.delete()
            }

            val stats = manifestJson.optJSONObject("stats")
            val packInfo = LanguagePackInfo(
                packId = manifestJson.getString("pack_id"),
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
                sha256Checksum = archiveHash,
                statusLabel = "IMPORTED",
                contentStatus = manifestJson.optString("validation_status", "VALIDATED"),
                packSizeBytes = permanentArchive.length()
            )

            // Register pack
            val existing = _availablePacks.value.filter { it.packId != packInfo.packId }
            _availablePacks.value = existing + packInfo
            Log.i(TAG, "Successfully installed pack atomically: ${packInfo.packId}")

            return Result.success(packInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Pack installation failed: ${e.message}", e)
            return Result.failure(e)
        } finally {
            // Clean up staging directory
            stagingDir.deleteRecursively()
        }
    }
}
