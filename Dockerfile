 ============= DOCKERFILE =============
# Multi-stage build for optimized production image
FROM maven:3.8.6-openjdk-11-slim AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (this layer will be cached if pom.xml doesn't change)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application
COPY src ./src
RUN mvn clean package -DskipTests

# ============= PRODUCTION IMAGE =============
FROM openjdk:11-jre-slim

# Set labels for better image management
LABEL maintainer="logistics-team@company.com" \
      version="1.0.0" \
      description="B2B Logistics AI Agent" \
      org.opencontainers.image.source="https://github.com/company/logistics-ai-agent"

# Create app directory and non-root user for security
RUN mkdir -p /app/logs && \
    addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --group appuser && \
    chown -R appuser:appgroup /app

# Set working directory
WORKDIR /app

# Copy built jar from build stage
COPY --from=build --chown=appuser:appgroup /app/target/logistics-ai-agent.jar app.jar

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0"

# Application entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod} -jar app.jar"]
