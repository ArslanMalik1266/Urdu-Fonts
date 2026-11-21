pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                // Google Mobile Ads Next-Gen (WebsCareAds 4.2) pulls Cronet from org.chromium
                includeGroupByRegex("org\\.chromium.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // WebsCareAds placements plugin (credentials come from gpr.key, never committed)
        maven {
            url = uri("https://maven.pkg.github.com/webscarepk/WebCareAds")
            credentials {
                username = "webscarepk"
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GPR_KEY")
            }
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/webscarepk/WebCareAds")
            credentials {
                username = "webscarepk"
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GPR_KEY")
            }
        }
    }
}

rootProject.name = "Urdu Fonts"
include(":app")
 