# Use a base image with Maven
FROM maven:3.8.4-openjdk-17-slim as builder

# Set the working directory
WORKDIR /app

# Copy the Maven project file(s)
COPY pom.xml .

# Download and cache dependencies
RUN mvn dependency:go-offline -B

# Copy the application source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Use the OpenJDK base image for the runtime environment
FROM openjdk:17-jdk

# Set the working directory
WORKDIR /src

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/learners-api-1.0-SNAPSHOT.jar .


# Expose the port on which the application will run
EXPOSE 8080

# Set the command to run the Spring Boot application
CMD ["java", "-jar", "learners-api-1.0-SNAPSHOT.jar"]
