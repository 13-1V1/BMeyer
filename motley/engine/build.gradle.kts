plugins {
    kotlin("jvm") version "2.0.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

application {
    // `./gradlew :engine:run` prints the demo.
    mainClass.set("com.motley.engine.DemoKt")
}

tasks.test {
    useJUnitPlatform()
}
