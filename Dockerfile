FROM node:20-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src
COPY --from=frontend /app/frontend/dist frontend/dist
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=backend /app/target/hotel-security-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/data && chown -R app:app /app
USER app
EXPOSE 8080
ENV HOTEL_STORAGE_ROOMS_FILE=/app/data/rooms-db.txt
ENV HOTEL_STORAGE_AUDIT_FILE=/app/data/audit-log.txt
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
