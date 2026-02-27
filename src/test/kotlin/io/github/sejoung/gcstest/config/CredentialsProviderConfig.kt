package io.github.sejoung.gcstest.config

import com.google.api.gax.core.CredentialsProvider
import com.google.cloud.NoCredentials
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean


@TestConfiguration
class CredentialsProviderConfig {


    @Bean
    fun credentialsProvider(): CredentialsProvider {
        return CredentialsProvider { NoCredentials.getInstance() }
    }
}
