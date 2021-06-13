plugins {
    id("java")
}

group = "com.github.minecraft_ta"
version = "1.2.1"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("com.formdev:flatlaf:1.2")
    implementation("com.formdev:flatlaf-intellij-themes:1.2")
    implementation("com.formdev:flatlaf-extras:1.2")
}
