FROM gradle:jdk21 AS build
WORKDIR /workspace

COPY build.gradle.kts settings.gradle.kts ./
RUN gradle --no-daemon dependencies

COPY src ./src
RUN gradle --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
