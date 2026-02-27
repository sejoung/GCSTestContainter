package io.github.sejoung.gcstest.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.sejoung.gcstest.config.GcpProperties
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class GcpStorageUploader(
    private val storage: Storage,
    private val gcpProperties: GcpProperties
) {
    private val bucketName get() = gcpProperties.storage.bucketName

    /** 단일 파일 동기 업로드 **/
    fun upload(fileName: String, data: ByteArray, contentType: String): BlobInfo {
        val blobId = BlobId.of(bucketName, fileName)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(contentType)
            .build()

        storage.create(blobInfo, data)
        logger.info { "gcs에 파일업로드 완료, gs://$bucketName/$fileName (크기: ${data.size} bytes)" }
        return blobInfo
    }

}
