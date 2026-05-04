# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B --no-transfer-progress

COPY src src
RUN ./mvnw package -DskipTests --no-transfer-progress

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S sheria && adduser -S sheria -G sheria
USER sheria

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8079
ENTRYPOINT ["java", \
  "-Xmx512m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
