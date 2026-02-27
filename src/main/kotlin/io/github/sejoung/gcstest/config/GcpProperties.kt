package io.github.sejoung.gcstest.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gcp")
data class GcpProperties(
    val storage: StorageProperties,
) {
    data class StorageProperties(
        val bucketName: String,
    )
}
