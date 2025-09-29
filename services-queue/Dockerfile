# Build stage
FROM gradle:jdk21-corretto AS build
WORKDIR /workspace

COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY services-common/build.gradle.kts services-common/
COPY services-queue/build.gradle.kts services-queue/
RUN ./gradlew dependencies --no-daemon

COPY services-common/src services-common/src
COPY services-queue/src services-queue/src
RUN ./gradlew :services-queue:bootJar --no-daemon

# Deployment stage
FROM amazoncorretto:21-alpine
WORKDIR /app

RUN addgroup -g 1001 -S morupark && \
    adduser -u 1001 -S queueservice -G morupark

COPY --from=build /workspace/services-queue/build/libs/*.jar app.jar
RUN chown queueservice:morupark app.jar

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8082

USER queueservice

EXPOSE $SERVER_PORT
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]