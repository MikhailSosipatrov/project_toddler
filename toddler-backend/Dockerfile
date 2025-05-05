FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

ARG ENV_TAG=dev
ENV SPRING_PROFILE=${ENV_TAG}

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/api-server/target/api-server.jar app.jar
ENTRYPOINT ["java", "-Xmx400m", "-Dspring.profiles.active=${SPRING_PROFILE}", "org.springframework.boot.loader.JarLauncher"]