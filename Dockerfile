FROM eclipse-temurin:17-jre-alpine

# Use Java 21 base image
FROM eclipse-temurin:21-jdk-jammy

# Copy jar built by Maven/Gradle
COPY target/iam-provisioning-1.0.0.jar app.jar

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
