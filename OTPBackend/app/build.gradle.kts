plugins {
    id("java")
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // or your installed version
    }
}

dependencies {
    implementation("com.twilio.sdk:twilio:9.13.0")
    implementation("ch.qos.logback:logback-classic:1.4.14")
}

application {
    mainClass.set("com.example.otpbackend.OTPServer")
}