FROM gradle:8.3.0-jdk17-alpine AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
EXPOSE 8080:8080
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar ./metabank.jar
ENTRYPOINT java -jar ./metabank.jar