rootProject.name = "desastres-naturales"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// :core es Kotlin/JVM puro a propósito — sin nada de Android. Ahí vive toda la
// lógica que puede fallar (parseo del feed, filtros, frescura) y por eso se
// puede testear sin emulador ni SDK. El módulo :app de Compose se suma después
// y solo pinta lo que :core decide.
include(":core")
