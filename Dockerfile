FROM openjdk:8-jdk-alpine as builder
COPY . /app/src
WORKDIR /app/src
RUN ./gradlew --no-daemon build

FROM openjdk:8-jdk-alpine
COPY --from=builder /app/src/build/libs/slack-kicker-app-*.jar /app/slack-kicker-app.jar
ENV slackTokenFile=/app_data/tokens.json
ENV slackSignatureKey=
ENV slackClientId=
ENV slackClientSecret=
ENV spring.profiles.active=prod
VOLUME [ "/app_data" ]
WORKDIR /app
ENTRYPOINT [ "java", "-jar", "./slack-kicker-app.jar" ]