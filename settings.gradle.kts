pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io" ) }
      //  maven { url = uri("https://raw.githubusercontent.com/alexgreench/google-webrtc/master") }
    }
}

rootProject.name = "vaultmessenger"
include(":app")