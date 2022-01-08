plugins {
    java
    id("io.izzel.taboolib") version "1.33"
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
}

taboolib {
    install("common")
    install("common-5")
    install("module-configuration")
    install("module-database-shaded")
    install("module-chat")
    install("module-lang")
    install("module-nms")
    install("module-nms-util")
    install("platform-bukkit")
    install("expansion-command-helper")
    classifier = null
    version = "6.0.7-6"
}

repositories {
    maven {
        credentials {
            username = "a5phyxia"
            password = "zxzbc13456"
        }
        url = uri("https://maven.ycraft.cn/repository/maven-snapshots/")
    }
    maven {
        url = uri("https://repo.tabooproject.org/repository/maven-releases/")
    }
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms:Zaphkiel:1.7.0@jar")
    compileOnly("net.sakuragame.eternal:JustInventory:1.0.0-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:DragonCore:2.5.1-SNAPSHOT@jar")
    compileOnly("net.sakuragame:DataManager-Bukkit-API:1.3.2-SNAPSHOT@jar")
    compileOnly("com.taylorswiftcn:UIFactory:1.0.1-SNAPSHOT@jar")
    compileOnly("ink.ptms.core:v11200:11200")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}