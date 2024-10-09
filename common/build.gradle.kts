plugins {
    id("java-library")
}

dependencies {
    api("org.apache.logging.log4j:log4j-api:2.3.2")
    implementation("org.apache.logging.log4j:log4j-core:2.3.2")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.3.2")

    api("io.temporal:temporal-sdk:1.25.2")

    testImplementation("io.temporal:temporal-testing:1.25.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
