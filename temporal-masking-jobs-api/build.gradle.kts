plugins {
    id("java")
}

dependencies {
    implementation(project(":common"))

    implementation("dev.blaauwendraad:json-masker:1.0.2")

    testImplementation("io.temporal:temporal-testing:1.25.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
