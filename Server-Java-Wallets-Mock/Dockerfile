# Etapa 1: Construcción del JAR
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el resto del código y compilar
COPY src ./src
RUN mvn package -DskipTests

# Etapa 2: Imagen final más liviana
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copiar el JAR desde la etapa anterior
COPY --from=builder /app/target/goldenrace-wallet-mock-server-1.0.5.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
