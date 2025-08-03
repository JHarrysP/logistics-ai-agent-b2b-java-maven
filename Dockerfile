# ============= DOCKERFILE =============
# Multi-stage build for optimized production image
FROM maven:3.9.4-openjdk-11-slim AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (for better Docker layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application
COPY src ./src
RUN mvn clean package -DskipTests

# ============= PRODUCTION IMAGE =============
FROM openjdk:11-jre-slim

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create application user for security
RUN groupadd -r logistics && useradd -r -g logistics logistics

# Set working directory
WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/target/logistics-ai-agent.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R logistics:logistics /app

# Switch to non-root user
USER logistics

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]