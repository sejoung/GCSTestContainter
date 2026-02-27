package io.github.sejoung.gcstest.config

import com.google.cloud.NoCredentials
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.StorageOptions
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer

class TestGcsContainterConfiguration :
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        private val gcsContainer: GenericContainer<*> = GenericContainer("fsouza/fake-gcs-server:latest")
            .withExposedPorts(4443)
            .withCreateContainerCmdModifier { cmd ->
                cmd.withEntrypoint(
                    "/bin/fake-gcs-server",
                    "-scheme", "http",
                    "-port", "4443"
                )
            }

        init {
            gcsContainer.start()
        }
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val endpoint = "http://${gcsContainer.host}:${gcsContainer.getMappedPort(4443)}"

        // fake-gcs-server에 테스트 버킷 생성
        val storage = StorageOptions.newBuilder()
            .setHost(endpoint)
            .setProjectId("test-project")
            .setCredentials(NoCredentials.getInstance())
            .build()
            .service
        val bucketName = applicationContext.environment.getProperty("gcp.storage.bucket-name", "test-bucket")
        storage.create(BucketInfo.of(bucketName))

        TestPropertyValues.of(
            "spring.cloud.gcp.storage.host=$endpoint",
            "spring.cloud.gcp.project-id=test-project"
        ).applyTo(applicationContext.environment)
    }
}
