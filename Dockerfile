# TODO graalvm, stage with gradle dependencies only
FROM gradle:7-jdk17 as build
#ARG CODEARTIFACT_AUTH_TOKEN
WORKDIR /build/
COPY . ./
RUN gradle build

# adoptopenjdk is deprecated in favor of eclipse-temurin
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/build/libs/*-all.jar ./app-all.jar
#COPY entrypoint.sh /
ENV MICRONAUT_SERVER_PORT 80
EXPOSE 80
#CMD /entrypoint.sh
CMD ["java", "-jar", "/app/app-all.jar"]
