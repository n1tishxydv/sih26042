package org.sih26042.coteacher.core.pack

import android.content.Context
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.*
import org.sih26042.coteacher.core.model.LanguagePackInfo
import org.sih26042.coteacher.core.model.PackLifecycleState
import org.sih26042.coteacher.data.LanguagePackRepository
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class LanguagePackLifecycleManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var mockContext: Context
    private lateinit var repository: LanguagePackRepository
    private lateinit var lifecycleManager: LanguagePackLifecycleManager
    private lateinit var cacheDir: File
    private lateinit var filesDir: File

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        cacheDir = tempFolder.newFolder("cache")
        filesDir = tempFolder.newFolder("files")
        `when`(mockContext.cacheDir).thenReturn(cacheDir)
        `when`(mockContext.filesDir).thenReturn(filesDir)

        repository = LanguagePackRepository(mockContext)
        lifecycleManager = LanguagePackLifecycleManager(mockContext, repository)
    }

    private fun sha256(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes).joinToString("") { "%02x".format(it) }
    }

    private fun createValidArchiveFile(packId: String = "test-pack-01", version: String = "1.0.0"): File {
        val zipFile = File(tempFolder.newFolder(), "$packId.slp")
        val manifest = JSONObject().apply {
            put("pack_id", packId)
            put("language_code", "sat")
            put("language_name", "Santali")
            put("native_name", "ᱥᱟᱱᱛᱟᱲᱤ")
            put("version", version)
            put("min_app_version", "1.0.0")
            put("script", JSONObject().put("primary_script_name", "Ol Chiki"))
            put("stats", JSONObject().put("phrases_count", 10))
        }

        val phrasesJson = "[]".toByteArray(Charsets.UTF_8)
        val manifestBytes = manifest.toString().toByteArray(Charsets.UTF_8)

        val checksums = JSONObject().apply {
            put("phrases.json", sha256(phrasesJson))
        }
        val checksumsBytes = checksums.toString().toByteArray(Charsets.UTF_8)

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(manifestBytes)
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("checksums.json"))
            zos.write(checksumsBytes)
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("phrases.json"))
            zos.write(phrasesJson)
            zos.closeEntry()
        }
        return zipFile
    }

    @Test
    fun testValidateCandidateArchive_Valid() {
        val archive = createValidArchiveFile()
        val report = lifecycleManager.validateCandidateArchive(archive)
        assertTrue("Report must be valid", report.isValid)
        assertTrue("Errors must be empty", report.errors.isEmpty())
        assertTrue("Computed SHA256 must not be empty", report.computedSha256.isNotBlank())
    }

    @Test
    fun testValidateCandidateArchive_RejectsForbiddenExecutable() {
        val badZip = File(tempFolder.newFolder(), "malicious.slp")
        ZipOutputStream(FileOutputStream(badZip)).use { zos ->
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write("{}".toByteArray())
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("checksums.json"))
            zos.write("{}".toByteArray())
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("exploit.sh"))
            zos.write("#!/bin/sh\nrm -rf /".toByteArray())
            zos.closeEntry()
        }

        val report = lifecycleManager.validateCandidateArchive(badZip)
        assertFalse("Archive containing .sh must be rejected", report.isValid)
        assertTrue("Error must mention forbidden executable", report.errors.any { it.contains("forbidden executable") })
    }

    @Test
    fun testValidateCandidateArchive_RejectsZipSlip() {
        val badZip = File(tempFolder.newFolder(), "zipslip.slp")
        ZipOutputStream(FileOutputStream(badZip)).use { zos ->
            zos.putNextEntry(ZipEntry("../../../evil.txt"))
            zos.write("hack".toByteArray())
            zos.closeEntry()
        }

        val report = lifecycleManager.validateCandidateArchive(badZip)
        assertFalse("Archive with path traversal must be rejected", report.isValid)
        assertTrue("Error must mention path traversal", report.errors.any { it.contains("path traversal") })
    }

    @Test
    fun testInstallPackAtomically() {
        val archive = createValidArchiveFile("lang-sat-v1", "1.0.0")
        val stream = archive.inputStream()

        val result = lifecycleManager.installPack(stream, autoActivate = false)
        assertTrue("Installation must succeed", result.isSuccess)

        val installedPack = result.getOrThrow()
        assertEquals("lang-sat-v1", installedPack.packId)
        assertEquals(PackLifecycleState.VALID, installedPack.lifecycleState)
        assertFalse(installedPack.isActive)

        // Verify permanent archive was created in filesDir
        val permanentArchive = File(filesDir, "installed_packs/lang-sat-v1.slp")
        assertTrue("Permanent archive must exist", permanentArchive.exists())
    }

    @Test
    fun testActivationAndRollbackLifecycle() {
        val initialActive = repository.activePack.value
        assertEquals("lang-pack-sat-olck-v1", initialActive.packId)

        // Switch to Mundari
        val activateResult = lifecycleManager.activatePack("lang-pack-unr-v0.1")
        assertTrue("Activation must succeed", activateResult.isSuccess)
        assertEquals("lang-pack-unr-v0.1", repository.activePack.value.packId)
        assertEquals(PackLifecycleState.ACTIVE, repository.activePack.value.lifecycleState)

        // Trigger rollback
        val rollbackResult = lifecycleManager.rollbackToPreviousPack()
        assertTrue("Rollback must succeed", rollbackResult.isSuccess)
        assertEquals("lang-pack-sat-olck-v1", repository.activePack.value.packId)
        assertEquals(PackLifecycleState.ACTIVE, repository.activePack.value.lifecycleState)

        // The rolled-back pack must have ROLLED_BACK state in repository
        val unrPack = repository.availablePacks.value.first { it.packId == "lang-pack-unr-v0.1" }
        assertEquals(PackLifecycleState.ROLLED_BACK, unrPack.lifecycleState)
    }

    @Test
    fun testUpgradePack_AtomicPreservationOnFailure() {
        val initialActive = repository.activePack.value
        assertEquals("lang-pack-sat-olck-v1", initialActive.packId)

        // Create corrupt upgrade stream (invalid zip)
        val corruptStream = "corrupted byte stream not a zip".byteInputStream()

        val upgradeResult = lifecycleManager.upgradePack(corruptStream)
        assertTrue("Corrupt upgrade must fail", upgradeResult.isFailure)

        // Prior active pack must remain active untouched
        assertEquals("lang-pack-sat-olck-v1", repository.activePack.value.packId)
        assertTrue("Initial active pack must remain active", repository.activePack.value.isActive)
    }
}
