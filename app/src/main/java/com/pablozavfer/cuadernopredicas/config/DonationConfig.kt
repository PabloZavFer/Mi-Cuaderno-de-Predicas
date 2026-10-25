package com.pablozavfer.cuadernopredicas.config

import com.pablozavfer.cuadernopredicas.BuildConfig

/**
 * Enlace de aportación voluntaria al desarrollador.
 *
 * NO lo cambies aquí: colócalo en `gradle.properties` (propiedad `donationUrl`) en la raíz
 * del proyecto. Ver docs/DONACIONES.md. Solo se aceptan enlaces https.
 */
object DonationConfig {
    val url: String? = BuildConfig.DONATION_URL.trim().takeIf { it.startsWith("https://") && it.length > "https://".length }

    val isConfigured: Boolean get() = url != null
}
