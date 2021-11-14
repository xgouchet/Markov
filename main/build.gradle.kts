plugins {
    kotlin("jvm") version "1.4.32"
    id("java")
    id("application")
}
group = "fr.xgouchet"
version = "1.0-SNAPSHOT"


dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
}

application {
    mainClass.set("fr.xgouchet.markov.Main")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "fr.xgouchet.markov.Main"
    }
}
