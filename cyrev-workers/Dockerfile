FROM eclipse-temurin:21-jre

WORKDIR /app

COPY cyrev-apis/target/cyrev-workers-1.0.0.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
