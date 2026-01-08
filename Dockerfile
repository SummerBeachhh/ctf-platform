# =========================
# Build stage
# =========================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Build the application, skip tests for speed
RUN mvn clean package -DskipTests


# =========================
# Run stage
# =========================
FROM eclipse-temurin:17-jre-jammy

# Install font libraries required for Thumbnailator / Java AWT (headless)
RUN apt-get update && apt-get install -y \
    fontconfig \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Create directories needed at runtime
RUN mkdir -p uploads cache/thumbnails

# Create non-root user for security
RUN groupadd -r ctf && useradd -r -g ctf ctf

# Copy application JAR
COPY --from=build /app/target/*.jar app.jar

# Set permissions
RUN chown -R ctf:ctf /app

# Switch to non-root user
USER ctf

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
