FROM maven:3.8.7-eclipse-temurin-8-alpine
COPY . /app
WORKDIR /app
RUN rm -rf .idea \
    && mvn clean package

FROM openjdk:8-jdk-alpine
COPY --from=0 /app/mock-be/target/mock-be-*.jar mock-be.jar
EXPOSE 8080
CMD ["java","-jar","mock-be.jar"]