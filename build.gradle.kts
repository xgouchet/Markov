buildscript {

    repositories {
        google()
        mavenCentral()
        jcenter()
    }

}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        jcenter()
    }
}
