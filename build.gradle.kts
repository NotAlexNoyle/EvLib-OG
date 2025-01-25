plugins {
    id("com.gradleup.shadow") version "8.3.5" // Import shadow API.
    java // Tell gradle this is a java project.
    eclipse // Import eclipse plugin for IDE integration.
    kotlin("jvm") version "2.0.21" // Import kotlin jvm plugin for kotlin/java integration.
}

java {
    // Declare java version.
    sourceCompatibility = JavaVersion.VERSION_17
}

group = "net.evmodder.EvLib" // Declare bundle identifier.
version = "1.0" // Declare plugin version (will be in .jar).
val apiVersion = "1.19" // Declare minecraft server target version.

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to version,
        "apiVersion" to apiVersion
    )

    inputs.properties(props) // Indicates to rerun if version changes.

    filesMatching("plugin.yml") {
        expand(props)
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://repo.purpurmc.org/snapshots") // Import the PurpurMC Maven Repository.
    }
    
    maven {
    
    	url = uri("https://repo.codemc.io/repository/maven-public/") // Import the CodeMC Maven Repository.
    
    }

	maven {
    
		url = uri("https://repo.essentialsx.net/releases/") // Import EssentialsX Repository.
    
	}
	
	maven {

		url = uri("https://maven.enginehub.org/repo/") // Import the EngineHub Maven Repository.

	}
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT") // Declare purpur API version to be packaged.
	compileOnly("de.tr7zw:item-nbt-api-plugin:2.11.2") // Import NBT API.
	compileOnly("net.essentialsx:EssentialsX:2.20.1") // Import EssentialsX API.
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3") // Import MiniPlaceholders API.
	compileOnly("com.github.MilkBowl:VaultAPI:1.7.1") // Import Vault API.i
	compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.15") // Import WorldEdit API.

	implementation(project(":libs:Utilities-OG"))
}

tasks.withType<AbstractArchiveTask>().configureEach { // Ensure reproducible builds.
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.shadowJar {
    archiveClassifier.set("") // Use empty string instead of null
    from("LICENSE") {
        into("/")
    }
    exclude("io.github.miniplaceholders.*") // Exclude the MiniPlaceholders package from being shadowed.
    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    archiveClassifier.set("part")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.compilerArgs.add("-Xlint:deprecation") // Triggers deprecation warning messages.
    options.encoding = "UTF-8"
    options.isFork = true
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}
