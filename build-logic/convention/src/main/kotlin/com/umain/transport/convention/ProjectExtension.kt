import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.getByType
import org.gradle.api.artifacts.VersionCatalogsExtension

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val Project.androidLibs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("androidLibs")