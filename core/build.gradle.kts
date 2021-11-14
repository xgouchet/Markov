plugins {
    kotlin("jvm") version "1.4.32"
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("org.junit.platform:junit-platform-launcher:1.6.2")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("com.github.xgouchet.Elmyr:core:1.3.0")
    testImplementation("com.github.xgouchet.Elmyr:inject:1.3.0")
    testImplementation("com.github.xgouchet.Elmyr:junit5:1.3.0")
    testImplementation("com.github.xgouchet.Elmyr:jvm:1.3.0")
    testImplementation("net.wuerl.kotlin:assertj-core-kotlin:0.2.1")
}
