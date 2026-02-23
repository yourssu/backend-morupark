# Build stage
FROM gradle:jdk21-corretto AS build
WORKDIR /workspace

COPY settings.gradle.kts .
COPY build.gradle.kts .

RUN gradle dependencies --no-daemon --parallel

COPY src src

RUN gradle bootJar --no-daemon --parallel

# Deployment stage
FROM amazoncorretto:21-alpine
WORKDIR /app

RUN addgroup -g 1001 -S morupark && \
    adduser -u 1001 -S queueservice -G morupark --no-create-home --disabled-password

COPY --from=build /workspace/build/libs/*.jar app.jar
RUN chown queueservice:morupark app.jar

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8082

USER queueservice

EXPOSE $SERVER_PORT
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
