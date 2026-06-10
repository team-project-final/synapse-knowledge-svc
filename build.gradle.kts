import java.time.Duration
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.testing.Test

plugins {
	java
	idea
	jacoco
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

group = "com.synapse.knowledge"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

sourceSets {
	main {
		java.srcDir(layout.buildDirectory.dir("generated-main-avro-java"))
	}
}

idea {
	module {
		generatedSourceDirs.add(layout.buildDirectory.dir("generated-main-avro-java").get().asFile)
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://packages.confluent.io/maven/") }
}

jacoco {
	toolVersion = "0.8.12"
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-restclient")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20240325.1")
	implementation("org.mapstruct:mapstruct:1.6.3")
	implementation("org.apache.avro:avro:1.11.3")
	implementation("io.confluent:kafka-avro-serializer:7.5.0") {
		exclude(group = "io.swagger.core.v3", module = "swagger-annotations")
	}
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	testRuntimeOnly("com.h2database:h2")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
	implementation("org.springframework.modulith:spring-modulith-starter-core")
	implementation("org.springframework.modulith:spring-modulith-events-api")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.modulith:spring-modulith-starter-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
	testImplementation("org.testcontainers:junit-jupiter:1.21.3")
	testImplementation("org.testcontainers:testcontainers:1.21.3")
	testImplementation("org.testcontainers:postgresql:1.21.3")
	testImplementation("org.testcontainers:elasticsearch:1.21.3")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.testcontainers:kafka:1.21.3")
}


tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("spring.profiles.active", "test")
	System.getenv("DOCKER_HOST")?.let { environment("DOCKER_HOST", it) }
	systemProperty("junit.jupiter.execution.timeout.default", "120s")
	timeout.set(Duration.ofMinutes(15))
	testLogging {
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
		events("failed")
		showStackTraces = true
		showCauses = true
	}
}

tasks.named<Test>("test") {
	exclude("**/SearchElasticsearchIntegrationTest.class")
	exclude("**/ChunkingPostgresFlywayIntegrationTest.class")
}

tasks.register<Test>("searchE2eTest") {
	group = JavaBasePlugin.VERIFICATION_GROUP
	description = "Runs the SearchElasticsearchIntegrationTest suite."
	testClassesDirs = sourceSets["test"].output.classesDirs
	classpath = sourceSets["test"].runtimeClasspath
	include("**/SearchElasticsearchIntegrationTest.class")
	shouldRunAfter(tasks.named("test"))
}

tasks.register<Test>("chunkingPgTest") {
	group = JavaBasePlugin.VERIFICATION_GROUP
	description = "Runs the ChunkingPostgresFlywayIntegrationTest suite."
	testClassesDirs = sourceSets["test"].output.classesDirs
	classpath = sourceSets["test"].runtimeClasspath
	include("**/ChunkingPostgresFlywayIntegrationTest.class")
	shouldRunAfter(tasks.named("test"))
}

val searchCoverageIncludes = listOf("com/synapse/knowledge/search/**")
val searchCoverageExcludes = listOf(
	"com/synapse/knowledge/search/dto/**",
	"com/synapse/knowledge/search/entity/**",
	"com/synapse/knowledge/search/event/**",
	"com/synapse/knowledge/search/internal/**",
	"com/synapse/knowledge/search/package-info.class",
	"com/synapse/knowledge/search/client/LearningAiSearchClient.class",
	"com/synapse/knowledge/search/service/SemanticSearchService.class",
	"com/synapse/knowledge/search/repository/ElasticsearchNoteSearchRepository.class",
	"com/synapse/knowledge/search/service/SearchAccuracyBenchmarkSeeder*.class",
	"com/synapse/knowledge/search/config/SlackNotifier.class",
	"com/synapse/knowledge/search/service/consumer/KafkaIdempotencyStore.class"
)

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
	classDirectories.setFrom(
		files(classDirectories.files.map {
			fileTree(it) {
				searchCoverageIncludes.forEach(::include)
				searchCoverageExcludes.forEach(::exclude)
			}
		})
	)
}

tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.test)
	classDirectories.setFrom(
		files(classDirectories.files.map {
			fileTree(it) {
				searchCoverageIncludes.forEach(::include)
				searchCoverageExcludes.forEach(::exclude)
			}
		})
	)
	violationRules {
		rule {
			limit {
				counter = "LINE"
				minimum = "0.80".toBigDecimal()
			}
		}
	}
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.modulith:spring-modulith-bom:2.0.6")
	}
}
