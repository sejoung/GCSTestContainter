package io.github.sejoung.gcstest.storage

import com.google.cloud.storage.Storage
import io.github.sejoung.gcstest.config.CredentialsProviderConfig
import io.github.sejoung.gcstest.config.GcpProperties
import io.github.sejoung.gcstest.config.TestGcsContainterConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(initializers = [TestGcsContainterConfiguration::class])
@Import(CredentialsProviderConfig::class)
class GcpStorageUploaderTest {
    @Autowired
    lateinit var gcpStorageUploader: GcpStorageUploader

    @Autowired
    lateinit var storage: Storage

    @Autowired
    lateinit var gcpProperties: GcpProperties

    @Test
    fun `Upload Functionality Testing`() {
        // given
        val fileName = "test/hello.txt"
        val content = "Hello, fake-gcs-server!"

        // when
        gcpStorageUploader.upload(fileName, content.toByteArray(), "text/plain")

        // then
        val blob = storage.get(gcpProperties.storage.bucketName, fileName)
        assertThat(String(blob.getContent())).isEqualTo(content)
    }
}
