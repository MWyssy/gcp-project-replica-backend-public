FROM maven:3.8.4-openjdk-17-slim as builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests

FROM openjdk:17-jdk

WORKDIR /src

COPY --from=builder /app/target/learners-api-1.0-SNAPSHOT.jar .

EXPOSE 8080

CMD ["java", "-jar", "learners-api-1.0-SNAPSHOT.jar"]
