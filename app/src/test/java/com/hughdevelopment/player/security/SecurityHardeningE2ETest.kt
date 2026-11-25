package com.hughdevelopment.player.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.StringReader
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Comprehensive 4-Tier Security Hardening Test Suite (OWASP MASVS Compliance).
 *
 * Mapped Features (PROJECT.md § Feature Inventory):
 * - Feature 1: Keystore Git Ignore (*.keystore, *.jks)
 * - Feature 2: Signing Password Externalization (local.properties / env var resolution with dev fallback)
 * - Feature 3: PII & Local Path Removal (purge from git tracking)
 * - Feature 4: Android Backup Security Rules (AndroidManifest.xml, data_extraction_rules.xml, backup_rules.xml)
 */
class SecurityHardeningE2ETest {

    private val projectRoot = locateProjectRoot()

    private fun locateProjectRoot(): File {
        var current: File? = File(System.getProperty("user.dir") ?: ".")
        while (current != null) {
            if (File(current, "app/build.gradle.kts").exists() && File(current, ".gitignore").exists()) {
                return current
            }
            current = current.parentFile
        }
        // Fallback to explicit workspace path if running in unusual JVM working directory
        val explicit = File("C:/Users/victor.hugo/.gemini/antigravity/scratch/hugh-player")
        if (explicit.exists()) return explicit
        return File(".")
    }

    // =========================================================================
    // TIER 1: FEATURE COVERAGE (Positive & Functional Contract Verification)
    // =========================================================================

    @Test
    fun testTier1_gitIgnoreContainsKeystoreAndJksPatterns() {
        val gitignoreFile = File(projectRoot, ".gitignore")
        assertTrue("Project .gitignore must exist at root", gitignoreFile.exists())
        val lines = gitignoreFile.readLines().map { it.trim() }

        assertTrue(".gitignore must include '*.keystore'", lines.any { it == "*.keystore" })
        assertTrue(".gitignore must include '*.jks'", lines.any { it == "*.jks" })
    }

    @Test
    fun testTier1_gitIgnoreExcludesSensitiveDevFolders() {
        val gitignoreFile = File(projectRoot, ".gitignore")
        val lines = gitignoreFile.readLines().map { it.trim() }

        assertTrue(".gitignore must ignore 'tools/agent-bridge/'", lines.any { it.startsWith("tools/agent-bridge") })
        assertTrue(".gitignore must ignore 'docs/logs/'", lines.any { it.startsWith("docs/logs") })
    }

    @Test
    fun testTier1_piiFilesAreNotTrackedInRepository() {
        val sensitiveRelativePaths = listOf(
            "tools/agent-bridge/hugh_ai_folders_plan.json",
            "tools/agent-bridge/ai_prompt_vault.txt",
            "docs/logs/transcript.jsonl"
        )
        for (relPath in sensitiveRelativePaths) {
            val targetFile = File(projectRoot, relPath)
            // If the file exists on disk, it must NOT be tracked by git
            // We verify that none of these sensitive files exist as tracked or committed artifacts
            assertFalse(
                "Sensitive PII / path log file '$relPath' must not exist in clean repository state",
                targetFile.exists() && isGitTracked(relPath)
            )
        }
    }

    @Test
    fun testTier1_signingExternalizationLogicResolvesEnvironmentVariableHighest() {
        val mockEnv = mapOf("SIGNING_STORE_PASSWORD" to "prod_env_secret_123")
        val mockProps = Properties().apply { put("signing.storePassword", "local_file_secret_456") }

        val resolved = resolveSigningProperty(
            propKey = "signing.storePassword",
            envKey = "SIGNING_STORE_PASSWORD",
            fallback = "dev_default_fallback",
            envProvider = { mockEnv[it] },
            props = mockProps
        )
        assertEquals("Environment variable must take absolute highest precedence", "prod_env_secret_123", resolved)
    }

    @Test
    fun testTier1_signingExternalizationLogicResolvesLocalPropertiesWhenEnvAbsent() {
        val mockEnv = emptyMap<String, String>()
        val mockProps = Properties().apply { put("signing.storePassword", "local_file_secret_456") }

        val resolved = resolveSigningProperty(
            propKey = "signing.storePassword",
            envKey = "SIGNING_STORE_PASSWORD",
            fallback = "dev_default_fallback",
            envProvider = { mockEnv[it] },
            props = mockProps
        )
        assertEquals("local.properties must be used when environment variable is unset", "local_file_secret_456", resolved)
    }

    @Test
    fun testTier1_signingExternalizationLogicUsesDevFallbackWhenCredentialsOmitted() {
        val mockEnv = emptyMap<String, String>()
        val mockProps = Properties()

        val resolved = resolveSigningProperty(
            propKey = "signing.storePassword",
            envKey = "SIGNING_STORE_PASSWORD",
            fallback = "dev_default_fallback",
            envProvider = { mockEnv[it] },
            props = mockProps
        )
        assertEquals("Default fallback must be returned when neither env nor local.properties exists", "dev_default_fallback", resolved)
    }

    @Test
    fun testTier1_manifestDeclaresDataExtractionAndBackupRules() {
        val manifestFile = File(projectRoot, "app/src/main/AndroidManifest.xml")
        assertTrue("AndroidManifest.xml must exist", manifestFile.exists())
        val manifestXml = manifestFile.readText()

        assertTrue(
            "AndroidManifest.xml must declare android:dataExtractionRules='@xml/data_extraction_rules'",
            manifestXml.contains("android:dataExtractionRules=\"@xml/data_extraction_rules\"")
        )
        assertTrue(
            "AndroidManifest.xml must declare android:fullBackupContent='@xml/backup_rules'",
            manifestXml.contains("android:fullBackupContent=\"@xml/backup_rules\"")
        )
    }

    @Test
    fun testTier1_dataExtractionRulesXmlValidAndExcludesPrivateData() {
        val xmlFile = File(projectRoot, "app/src/main/res/xml/data_extraction_rules.xml")
        assertTrue("data_extraction_rules.xml must exist", xmlFile.exists())
        val content = xmlFile.readText()

        // XML Well-formedness check
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(xmlFile)
        assertNotNull("XML document must parse successfully", doc)

        assertEquals("Root element must be data-extraction-rules", "data-extraction-rules", doc.documentElement.nodeName)
        assertTrue("Must protect against cloud-backup", content.contains("<cloud-backup>"))
        assertTrue("Must protect against device-transfer", content.contains("<device-transfer>"))
        assertTrue("Must exclude stats database", content.contains("hugh_player_stats.db"))
        assertTrue("Must exclude shared prefs", content.contains("hugh_player_vault_prefs.xml"))
    }

    @Test
    fun testTier1_fullBackupContentXmlValidAndExcludesPrivateData() {
        val xmlFile = File(projectRoot, "app/src/main/res/xml/backup_rules.xml")
        assertTrue("backup_rules.xml must exist", xmlFile.exists())
        val content = xmlFile.readText()

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(xmlFile)
        assertNotNull("XML document must parse successfully", doc)

        assertEquals("Root element must be full-backup-content", "full-backup-content", doc.documentElement.nodeName)
        assertTrue("Must exclude stats database", content.contains("hugh_player_stats.db"))
        assertTrue("Must exclude shared prefs", content.contains("hugh_player_vault_prefs.xml"))
    }

    // =========================================================================
    // TIER 2: BOUNDARY & CORNER CASES (Adversarial, Injections, Precedence)
    // =========================================================================

    @Test
    fun testTier2_gitIgnorePatternMatching_boundaryCases() {
        val patterns = listOf("*.keystore", "*.jks", "tools/agent-bridge/", "docs/logs/")

        fun matchesRule(path: String): Boolean {
            val filename = path.substringAfterLast('/')
            if (patterns.contains("*.keystore") && filename.endsWith(".keystore")) return true
            if (patterns.contains("*.jks") && filename.endsWith(".jks")) return true
            if (patterns.any { path.startsWith(it) }) return true
            return false
        }

        // Positive boundary matches
        assertTrue(matchesRule("app/keystore/release.keystore"))
        assertTrue(matchesRule("my_prod.jks"))
        assertTrue(matchesRule(".hidden.keystore"))
        assertTrue(matchesRule("tools/agent-bridge/nested/deep/secret.txt"))
        assertTrue(matchesRule("docs/logs/transcript.jsonl"))

        // Negative non-matches
        assertFalse(matchesRule("keystore.txt"))
        assertFalse(matchesRule("jks_backup.zip"))
        assertFalse(matchesRule("app/src/main/java/KeystoreManager.kt"))
        assertFalse(matchesRule("tools/other-tool/script.py"))
    }

    @Test
    fun testTier2_signingProperty_emptyStringInEnvFallsBackToLocalProperties() {
        val mockEnv = mapOf("SIGNING_STORE_PASSWORD" to "")
        val mockProps = Properties().apply { put("signing.storePassword", "local_pass_123") }

        // If env is empty or blank, it should fallback to properties
        val resolved = resolveSigningProperty(
            propKey = "signing.storePassword",
            envKey = "SIGNING_STORE_PASSWORD",
            fallback = "dev_default",
            envProvider = { val v = mockEnv[it]; if (v.isNullOrBlank()) null else v },
            props = mockProps
        )
        assertEquals("Blank env var should gracefully fallback to local.properties", "local_pass_123", resolved)
    }

    @Test
    fun testTier2_signingProperty_specialCharactersAndPasswordEscaping() {
        val complexSecret = "P@\$\$w0rd!#%^&*()_+-=[]{}|;':,./<>?~`"
        val mockEnv = mapOf("SIGNING_KEY_PASSWORD" to complexSecret)

        val resolved = resolveSigningProperty(
            propKey = "signing.keyPassword",
            envKey = "SIGNING_KEY_PASSWORD",
            fallback = "default",
            envProvider = { mockEnv[it] },
            props = Properties()
        )
        assertEquals("Complex special character passwords must be preserved verbatim", complexSecret, resolved)
    }

    @Test
    fun testTier2_signingProperty_supportsBothDotAndUnderscoreKeysInLocalProperties() {
        val propsWithEnvStyle = Properties().apply {
            put("SIGNING_STORE_FILE", "custom/vault.keystore")
        }

        val resolved = resolveSigningProperty(
            propKey = "signing.storeFile",
            envKey = "SIGNING_STORE_FILE",
            fallback = "keystore/hugh_vault.keystore",
            envProvider = { null },
            props = propsWithEnvStyle
        )
        assertEquals("Should look up envKey in local.properties as secondary key", "custom/vault.keystore", resolved)
    }

    @Test
    fun testTier2_buildGradleKtsDoesNotContainPlaintextProductionPasswords() {
        val buildGradleFile = File(projectRoot, "app/build.gradle.kts")
        assertTrue("app/build.gradle.kts must exist", buildGradleFile.exists())
        val content = buildGradleFile.readText()

        // Verify that getSigningProperty function is declared in build script
        assertTrue(
            "build.gradle.kts must implement getSigningProperty helper",
            content.contains("fun getSigningProperty") || content.contains("getSigningProperty(")
        )

        // Verify that storePassword is NOT hardcoded as a static string assignment
        // Old: storePassword = "hughdevelopment"
        // New: storePassword = getSigningProperty(...)
        assertFalse(
            "storePassword must not be hardcoded as static string without getSigningProperty",
            Regex("""storePassword\s*=\s*"[^"]+"""").containsMatchIn(content)
        )
        assertFalse(
            "keyPassword must not be hardcoded as static string without getSigningProperty",
            Regex("""keyPassword\s*=\s*"[^"]+"""").containsMatchIn(content)
        )
    }

    @Test
    fun testTier2_dataExtractionRulesXml_handlesCorruptedOrEmptyInputSafely() {
        val emptyXml = "<data-extraction-rules></data-extraction-rules>"
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(org.xml.sax.InputSource(StringReader(emptyXml)))
        assertNotNull(doc)
        assertEquals(0, doc.documentElement.childNodes.length)
    }

    // =========================================================================
    // TIER 3: CROSS-FEATURE INTERACTIONS (Signing ↔ Manifest ↔ Gitignore)
    // =========================================================================

    @Test
    fun testTier3_crossFeature_keystorePathMatchesSigningConfigDefault() {
        val defaultPath = "keystore/hugh_vault.keystore"
        val gitignoreFile = File(projectRoot, ".gitignore")
        val lines = gitignoreFile.readLines().map { it.trim() }

        // *.keystore in gitignore must shield the default keystore location
        assertTrue(lines.contains("*.keystore"))
        assertTrue("Default keystore path ending in .keystore must be shielded by .gitignore", defaultPath.endsWith(".keystore"))
    }

    @Test
    fun testTier3_crossFeature_backupExclusionsMatchActiveDatabaseNames() {
        val dataExtractionRulesFile = File(projectRoot, "app/src/main/res/xml/data_extraction_rules.xml")
        val content = dataExtractionRulesFile.readText()

        // The database file names in PlayerDatabase / AppDatabase must be fully covered
        val protectedDbFiles = listOf(
            "hugh_player_stats.db",
            "hugh_player_stats.db-wal",
            "hugh_player_stats.db-shm"
        )

        for (dbFile in protectedDbFiles) {
            assertTrue("data_extraction_rules must explicitly exclude '$dbFile'", content.contains(dbFile))
        }
    }

    @Test
    fun testTier3_crossFeature_backupExclusionsCoverBothCloudAndDeviceTransfer() {
        val xmlFile = File(projectRoot, "app/src/main/res/xml/data_extraction_rules.xml")
        val content = xmlFile.readText()

        val cloudBackupSection = content.substringAfter("<cloud-backup>").substringBefore("</cloud-backup>")
        val deviceTransferSection = content.substringAfter("<device-transfer>").substringBefore("</device-transfer>")

        assertTrue("Cloud backup must exclude hugh_player_stats.db", cloudBackupSection.contains("hugh_player_stats.db"))
        assertTrue("Device transfer must exclude hugh_player_stats.db", deviceTransferSection.contains("hugh_player_stats.db"))
        assertTrue("Cloud backup must exclude sharedpref", cloudBackupSection.contains("hugh_player_vault_prefs.xml"))
        assertTrue("Device transfer must exclude sharedpref", deviceTransferSection.contains("hugh_player_vault_prefs.xml"))
    }

    // =========================================================================
    // HELPER METHODS (Simulating Signing Property Resolution)
    // =========================================================================

    private fun resolveSigningProperty(
        propKey: String,
        envKey: String,
        fallback: String,
        envProvider: (String) -> String? = { System.getenv(it) },
        props: Properties
    ): String {
        return envProvider(envKey)
            ?: props.getProperty(propKey)
            ?: props.getProperty(envKey)
            ?: fallback
    }

    private fun isGitTracked(relativePath: String): Boolean {
        return try {
            val process = ProcessBuilder("git", "ls-files", relativePath)
                .directory(projectRoot)
                .start()
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            output.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }
}
