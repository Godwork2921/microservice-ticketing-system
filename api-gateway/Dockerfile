# syntax=docker/dockerfile:1
# Uses the executable jar built on the host by `.\mvnw.cmd clean package -DskipTests`
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]