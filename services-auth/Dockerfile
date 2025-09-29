# Build stage
FROM gradle:jdk21-corretto AS build
WORKDIR /workspace

COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY services-common/build.gradle.kts services-common/
COPY services-auth/build.gradle.kts services-auth/
COPY services-queue/build.gradle.kts services-queue/

RUN gradle dependencies --no-daemon --parallel

COPY services-common/src services-common/src
COPY services-auth/src services-auth/src

RUN gradle :services-auth:bootJar --no-daemon --parallel

# Deployment stage
FROM amazoncorretto:21-alpine
WORKDIR /app

RUN addgroup -g 1001 -S morupark && \
    adduser -u 1001 -S authservice -G morupark

COPY --from=build /workspace/services-auth/build/libs/*.jar app.jar
RUN chown authservice:morupark app.jar

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8081

USER authservice

EXPOSE $SERVER_PORT
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]