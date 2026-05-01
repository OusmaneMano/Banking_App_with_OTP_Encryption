plugins {
    java
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("com.example.bankingbackendserver.Main")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.twilio.com") // Twilio's custom repository
    }
}

dependencies {
    implementation("com.twilio.sdk:twilio:9.13.0") // or 9.0.0
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.slf4j:slf4j-nop:2.0.9")

    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
}