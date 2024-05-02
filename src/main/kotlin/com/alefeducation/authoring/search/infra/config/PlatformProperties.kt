package com.alefeducation.authoring.search.infra.config

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import com.alefeducation.authoring.search.domain.KindType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "platform")
class PlatformProperties {
    lateinit var name: String
    lateinit var spaces: List<SpaceProperties>
}

data class SpaceProperties(
    val name: String,
    val kinds: List<KindProperties>
)

data class KindProperties(
    val name: String,
    val index: String
)

sealed interface PlatformNotFoundError
object NoSpaceFound : PlatformNotFoundError
object NoKindFound : PlatformNotFoundError

object Platform {
    fun PlatformProperties.kindProperties(kindType: KindType): Either<NonEmptyList<PlatformNotFoundError>, KindProperties> {
        val platform = this
        return either {
            zipOrAccumulate(
                { ensure(platform.spaces.any { it.name == "aat" }) { NoSpaceFound } },
                { ensure(platform.spaces.flatMap { it.kinds }.any { it.name == kindType.value }) { NoKindFound } }
            ) { _, _ -> spaces.first { it.name == "aat" }.kinds.first { it.name == kindType.value } }
        }
    }

    fun PlatformProperties.indexNames(kindTypes: List<KindType>): List<String> = kindTypes.map { kindType ->
        when (val either = this.kindProperties(kindType)) {
            is Either.Left -> throw IllegalArgumentException(either.value.joinToString())
            is Either.Right -> either.value.index
        }
    }.distinct()
}
