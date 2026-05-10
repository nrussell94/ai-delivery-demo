FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace/backend
COPY backend/ .
COPY contracts/ /workspace/contracts/
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
