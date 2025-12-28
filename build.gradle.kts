import org.gradle.jvm.tasks.Jar

plugins {
    java
    application
    id("org.beryx.runtime") version "1.13.1" // Badass Runtime's plugin to make exe
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Indicates which Java vesrion to use

    }
}
repositories {
    mavenCentral()

}




sourceSets {   // add source code and resources to the project
    main {
        java {
            srcDirs("src")
        }
        resources {
            srcDirs("resources")
        }
        }
        test {
            java {
                srcDirs("Test")
            }
        }
    }

dependencies {
    implementation ("com.fasterxml.jackson.core:jackson-databind:2.16.1") // Jackson library for JSON processing
    implementation ("com.google.guava:guava:32.1.2-jre")// Guava library for additional utilities
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0") // JUnit dependency for testing
    testImplementation("org.mockito:mockito-core:5.11.0") // Mockito dependency for mocking in tests
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0") // Mockito extension for JUnit 5
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0") // JUnit engine for running tests
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // JUnit platform launcher for running tests
}
tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true // Show standard output and error streams during tests
    }
}

application {
    mainClass.set("Frontend.GameWindow") // Indicate the main class of the application, the clas you need to execute to run the program
    }

// --- JAR CONFIGURATION ---
tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "Frontend.GameWindow" // Specify the main class in the JAR manifest
    }
    from(sourceSets.main.get().output) // Include the compiled classes and resources in the JAR
}

// --- EXE CONFIGURATION (JPackage) ---
runtime {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.desktop", "java.logging"))

    launcher {
        noConsole = true
    }

    jpackage {
        imageName = "JeuWorms"
        //icon = file("resources/Images/Worm_Icon.ico").absolutePath // Path to the icon file for the executable
        
        skipInstaller = true
        
        mainJar = tasks.jar.get().archiveFileName.get() // Specify the main JAR file for the executable
    }
}
