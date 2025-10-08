# Multi-stage build for SACCO Management System
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (layer caching)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Production stage
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

# Add user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the application
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

EXPOSE 8082
ENTRYPOINT ["java","-cp","app:app/lib/*","com.example.demo.DemoApplication"]