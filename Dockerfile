# 1. Usamos un sistema operativo muy ligero que ya tiene Java 21 instalado
FROM eclipse-temurin:21-jre-alpine

# 2. Creamos una carpeta llamada /app dentro del contenedor
WORKDIR /app

# 3. Copiamos nuestro Fat JAR desde nuestro ordenador al contenedor
COPY target/vehicle-sensors-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

# 4. Le decimos al contenedor qué hacer cuando se encienda
ENTRYPOINT ["java", "-jar", "app.jar"]