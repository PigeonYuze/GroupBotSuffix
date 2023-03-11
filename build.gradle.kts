plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.14.0"
}

mirai {
    jvmTarget = JavaVersion.VERSION_11
}

group = "com.pigeonyuze"
version = "1.1.1"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/net.mamoe.yamlkt/yamlkt -> yaml comment
    implementation("net.mamoe.yamlkt:yamlkt:0.12.0")

    // https://mvnrepository.com/artifact/com.github.oshi/oshi-core-java11 -> obtain system information
    implementation("com.github.oshi:oshi-core-java11:6.4.0")


}
