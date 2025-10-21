# Use lightweight JRE base image
FROM eclipse-temurin:17-jre-alpine

# Create app directory
WORKDIR /app

# Copy your prebuilt JAR into the container
# (replace 'sacco-management-system.jar' with your actual jar name)
COPY target/*.jar app.jar

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Health check for container monitoring
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Expose the port your app runs on
EXPOSE 8082

# Run the jar
ENTRYPOINT ["java","-jar","app.jar"]
