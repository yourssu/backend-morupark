# Build stage
FROM gradle:jdk21-corretto AS build
WORKDIR /workspace

COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY services-common/build.gradle.kts services-common/
COPY services-auth/build.gradle.kts services-auth/
COPY services-queue/build.gradle.kts services-queue/
RUN ./gradlew dependencies --no-daemon || true

COPY services-queue/src services-queue/src
RUN ./gradlew :services-queue:bootJar --no-daemon

# Deployment stage
FROM amazoncorretto:21-alpine
WORKDIR /app

COPY --from=build /workspace/services-queue/build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8082

EXPOSE $SERVER_PORT
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]