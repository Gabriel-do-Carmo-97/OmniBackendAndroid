package br.wgc.omnibackend.testing.architecture

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Architecture Fitness Tests.
 *
 * Enforces architectural rules:
 * 1. Zero Jetpack Compose in headless backend and bundle modules.
 * 2. Prohibit raw android.util.Log in production code.
 * 3. Enforce corporate package standard br.wgc.omnibackend.
 */
@Suppress("NestedBlockDepth")
class ArchitectureFitnessTest {

    private val projectRoot: File by lazy {
        var current: File = File(".").canonicalFile
        while (current.parentFile != null && !File(current, "settings.gradle.kts").exists()) {
            val parent = current.parentFile ?: break
            current = parent
        }
        current
    }

    @Test
    fun testEnsureNoComposeInHeadlessModules() {
        val headlessDirs = listOf(
            File(projectRoot, "core/src/main/java"),
            File(projectRoot, "backend"),
            File(projectRoot, "bundle"),
            File(projectRoot, "testing/src/main/java"),
        )

        val violations = mutableListOf<String>()

        headlessDirs.forEach { dir ->
            if (dir.exists()) {
                dir.walkTopDown()
                    .filter { it.extension == "kt" && it.path.contains("src${File.separator}main") }
                    .forEach { file ->
                        val lines = file.readLines()
                        lines.forEachIndexed { index, line ->
                            if (line.trim().startsWith("import androidx.compose.")) {
                                violations.add("${file.relativeTo(projectRoot)}:${index + 1} -> $line")
                            }
                        }
                    }
            }
        }

        assertTrue(
            "Found Jetpack Compose imports in headless backend modules:\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }

    @Test
    fun testEnsureNoRawLog() {
        val violations = mutableListOf<String>()

        val prodDirs = listOf(
            File(projectRoot, "core"),
            File(projectRoot, "backend"),
            File(projectRoot, "bundle"),
        )

        prodDirs.forEach { dir ->
            if (dir.exists()) {
                dir.walkTopDown()
                    .filter { it.extension == "kt" && it.path.contains("src${File.separator}main") }
                    .forEach { file ->
                        val lines = file.readLines()
                        lines.forEachIndexed { index, line ->
                            if (line.contains("android.util.Log.")) {
                                violations.add("${file.relativeTo(projectRoot)}:${index + 1} -> $line")
                            }
                        }
                    }
            }
        }

        assertTrue(
            "Direct use of android.util.Log is prohibited in production code. Use telemetry:\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }

    @Test
    fun testEnsurePackageStandard() {
        val violations = mutableListOf<String>()

        val prodDirs = listOf(
            File(projectRoot, "core"),
            File(projectRoot, "backend"),
            File(projectRoot, "bundle"),
            File(projectRoot, "testing"),
        )

        prodDirs.forEach { dir ->
            if (dir.exists()) {
                dir.walkTopDown()
                    .filter { it.extension == "kt" && it.path.contains("src${File.separator}main") }
                    .forEach { file ->
                        val firstPackageLine = file.useLines { lines ->
                            lines.map { it.removePrefix("\uFEFF").trim() }
                                .firstOrNull { it.startsWith("package ") }
                        }
                        if (firstPackageLine == null || !firstPackageLine.startsWith("package br.wgc.omnibackend")) {
                            violations.add("${file.relativeTo(projectRoot)} -> ${firstPackageLine ?: "No package declared"}")
                        }
                    }
            }
        }

        assertTrue(
            "Kotlin files outside of corporate namespace 'br.wgc.omnibackend':\n" +
                violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }
}
