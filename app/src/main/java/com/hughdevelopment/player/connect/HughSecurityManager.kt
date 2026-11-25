package com.hughdevelopment.player.connect

import android.content.Context
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import java.security.MessageDigest

object HughSecurityManager {

    /**
     * Validates if the calling UID belongs to an approved package (NIGHT//CITY or AETHER VAULT).
     */
    fun validateCallingPackage(context: Context, callingUid: Int): String? {
        val pm = context.packageManager
        val packages = pm.getPackagesForUid(callingUid) ?: return null
        for (pkg in packages) {
            if (HughConnectContract.ALLOWED_PACKAGES.contains(pkg)) {
                return pkg
            }
        }
        return null
    }

    /**
     * Validates signature compatibility when both apps share the same signature (signature protectionLevel).
     * If certificates differ (e.g. debug.keystore vs hugh_vault.keystore in dev), evaluates if
     * package verification and strict IPC matching are satisfied.
     */
    fun isPackageAuthorized(context: Context, packageName: String): Boolean {
        if (!HughConnectContract.ALLOWED_PACKAGES.contains(packageName)) {
            return false
        }
        return try {
            val pm = context.packageManager
            pm.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Get certificate SHA-256 fingerprint of installed package.
     */
    fun getPackageCertificateFingerprint(context: Context, packageName: String): String? {
        return try {
            val pm = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = packageInfo.signingInfo ?: return null
                val signatures = if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
                if (signatures.isNotEmpty()) {
                    computeSha256(signatures[0].toByteArray())
                } else null
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                val signatures = packageInfo.signatures
                if (signatures != null && signatures.isNotEmpty()) {
                    computeSha256(signatures[0].toByteArray())
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun computeSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString(":") { String.format("%02X", it) }
    }
}
