# syntax=docker/dockerfile:1
# Uses the executable jar built on the host by `.\mvnw.cmd clean package -DskipTests`
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY analytics-service/target/analytics-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]