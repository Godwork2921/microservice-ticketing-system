# syntax=docker/dockerfile:1
# Uses the executable jar built on the host by `.\mvnw.cmd clean package -DskipTests`
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY reservation-service/target/reservation-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]