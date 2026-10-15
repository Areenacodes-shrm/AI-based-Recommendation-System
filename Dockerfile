# Build stage using OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY src ./src
COPY pom.xml ./

# Compile Java sources and bundle static resources
RUN mkdir -p bin && \
    javac -encoding UTF-8 -d bin $(find src/main/java -name "*.java") && \
    cp -r src/main/resources/* bin/ && \
    jar cfe recommendation-system.jar com.recommendation.Main -C bin .

# Runtime stage using lightweight JRE
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/recommendation-system.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "8080"]
