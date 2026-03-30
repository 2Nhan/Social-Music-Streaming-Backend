# Dockerfile
# Stage 1: Build the application (using Maven)
# Wait, typically we can just copy the pre-built jar from GitHub Actions, or let Dockerfile build it.
# To keep it simple and clean, GitHub Action will build the jar, and Dockerfile will just package it with FFmpeg.
FROM eclipse-temurin:21-jdk-jammy

# Install FFmpeg for HLS processing
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the pre-built jar file (built in Github Actions workflow)
COPY target/*.jar app.jar

# Expose port (matched with Spring Boot)
EXPOSE 8081

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
