import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.4.5"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.4.32"
	kotlin("plugin.spring") version "1.4.32"
	kotlin("plugin.jpa") version "1.4.32"
	kotlin("plugin.allopen") version "1.4.32"
	id("jacoco")
}

jacoco {
	toolVersion = "0.8.6"
}

tasks.jacocoTestReport {
	reports {
		html.isEnabled = true
		xml.isEnabled = false
		csv.isEnabled = true
	}
	finalizedBy("jacocoTestCoverageVerification")
}

tasks.jacocoTestCoverageVerification {
	violationRules {
		rule {
			enabled = true
			element = "CLASS"

			limit {
				counter = "BRANCH"
				value = "COVEREDRATIO"
				minimum = "0.80".toBigDecimal()
			}

			limit {
				counter = "LINE"
				value = "COVEREDRATIO"
				minimum = "0.80".toBigDecimal()
			}

			limit {
				counter = "LINE"
				value = "TOTALCOUNT"
				maximum = "200".toBigDecimal()
			}
			excludes = listOf(
				"com.mptsix.todaydiary.TodayDiaryServerApplicationKt",
				"com.mptsix.todaydiary.error.**",
				"com.mptsix.todaydiary.data.request.*",
				"com.mptsix.todaydiary.data.response.*",
				"com.mptsix.todaydiary.data.user.**",
				"com.mptsix.todaydiary.security.*",
				"com.mptsix.todaydiary.service.UserService.registerJournal.*"
			)
		}
	}
}

group = "com.mptsix.todaydiary"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

dependencies {
	// Kotlin Related
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

	// Spring Related
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

	// Jackson ObjectMapper
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	// H2 MemDB
	implementation("com.h2database:h2")

	// Embedded DB
	implementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")

	// JWT Token
	implementation("io.jsonwebtoken:jjwt-impl:0.11.2")
	implementation("io.jsonwebtoken:jjwt-jackson:0.11.2")

	// Spring Annotation Processor
	compileOnly("org.springframework.boot:spring-boot-configuration-processor")

	// Test-Related
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

noArg {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
}

allOpen {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	finalizedBy("jacocoTestReport")
	useJUnitPlatform()
}
