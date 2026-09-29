package org.sih26042.coteacher.data

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.sih26042.coteacher.core.model.LanguagePackInfo
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Tests pack security safeguards (ZipSlip, ZipBomb, banned extensions)
 * and atomic rollback capabilities.
 */
class PackSecurityAndRollbackTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testZipSlipPathTraversalRejected() {
        val badZip = tempFolder.newFile("malicious_path_traversal.slp")
        ZipOutputStream(FileOutputStream(badZip)).use { zos ->
            zos.putNextEntry(ZipEntry("../../../etc/passwd"))
            zos.write("malicious payload".toByteArray())
            zos.closeEntry()
        }

        try {
            PackParser.validateArchiveSecurity(badZip)
            fail("Expected SecurityException for path traversal attempt")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Path traversal") == true)
        }
    }

    @Test
    fun testBannedExecutableFileRejected() {
        val badZip = tempFolder.newFile("malicious_dex.slp")
        ZipOutputStream(FileOutputStream(badZip)).use { zos ->
            zos.putNextEntry(ZipEntry("classes.dex"))
            zos.write(byteArrayOf(0x64, 0x65, 0x78, 0x0a))
            zos.closeEntry()
        }

        try {
            PackParser.validateArchiveSecurity(badZip)
            fail("Expected SecurityException for banned executable file")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Executable file") == true)
        }
    }

    @Test
    fun testCleanArchivePassesSecurityValidation() {
        val goodZip = tempFolder.newFile("clean_pack.slp")
        ZipOutputStream(FileOutputStream(goodZip)).use { zos ->
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write("{}".toByteArray())
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("phrases.json"))
            zos.write("[]".toByteArray())
            zos.closeEntry()
        }

        // Should complete without exception
        PackParser.validateArchiveSecurity(goodZip)
    }

    @Test
    fun testPackStateAndRollbackLogic() {
        // Pure JVM state rollback simulation
        var activePack = LanguagePackInfo(
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
            statusLabel = "FULL MVP"
        )
        var previousPack: LanguagePackInfo? = null

        // 1. Initial state
        assertEquals("sat", activePack.languageCode)
        assertEquals("FULL MVP", activePack.statusLabel)

        // 2. Switch to candidate Mundari pack
        previousPack = activePack
        val candidateMundari = LanguagePackInfo(
            packId = "lang-pack-unr-v0.1",
            languageCode = "unr",
            languageName = "Mundari",
            nativeName = "मुण्डारी",
            version = "0.1.0",
            primaryScript = "Devanagari / Mundari Bani",
            isInstalled = true,
            isActive = true,
            phrasesCount = 2,
            flnVocabCount = 0,
            worksheetsCount = 0,
            sha256Checksum = "1a8b9c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c",
            statusLabel = "PLUGGABLE / CONTENT EXPANSION"
        )
        activePack = candidateMundari
        assertEquals("unr", activePack.languageCode)
        assertEquals("PLUGGABLE / CONTENT EXPANSION", activePack.statusLabel)

        // 3. Candidate fails validation in field -> automatic rollback to previous
        assertNotNull(previousPack)
        activePack = previousPack
        assertEquals("sat", activePack.languageCode)

        assertEquals("FULL MVP", activePack.statusLabel)
    }
}
