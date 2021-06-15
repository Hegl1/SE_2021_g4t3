FROM maven:3.8.1-jdk-8 AS build  

RUN mkdir -p /build/
COPY pom.xml /build/
COPY src /build/src/

RUN mvn -f /build/pom.xml clean package

FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY --from=build /build/target/TimeGuess-1.0.5.jar /app
COPY --from=build /build/target/site/jacoco /app/jacoco_report
COPY --from=build /build/target/apidocs /app/javadocs

RUN apk add --no-cache openssl

ENV DOCKERIZE_VERSION v0.6.1
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz

ENTRYPOINT ["dockerize", "-wait", "tcp://mysql:3306", "-timeout", "300s", "java","-jar","TimeGuess-1.0.5.jar"]
