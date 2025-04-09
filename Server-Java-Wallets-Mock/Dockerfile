# Usar una imagen base de Java 17
FROM eclipse-temurin:17-jdk

# Crear un directorio para la aplicación
WORKDIR /app

# Copiar el archivo JAR al contenedor
COPY target/goldenrace-wallet-mock-server-1.0.5.jar app.jar

# Exponer el puerto predeterminado de la aplicación (internamente)
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
