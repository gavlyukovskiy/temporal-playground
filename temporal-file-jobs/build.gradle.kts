plugins {
    id("java")
    id("application")
}

dependencies {
    implementation(project(":common"))

    implementation(project(":temporal-file-jobs-api"))
    implementation(project(":temporal-masking-jobs-api"))

    implementation("io.temporal:temporal-sdk:1.25.2")

    testImplementation("io.temporal:temporal-testing:1.25.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "com.github.gavlyukovskiy.FileJobsWorker"
}
