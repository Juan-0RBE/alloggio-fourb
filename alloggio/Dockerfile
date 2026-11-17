# Imagen del back de Alloggio para desplegar en Render, Railway u otro servicio con Docker.

# 1. Compilación con Maven y JDK 21 (la versión que pide el pom)
FROM maven:3.9-eclipse-temurin-21 AS compilacion
WORKDIR /app
# Primero solo el pom, para reutilizar las dependencias descargadas entre despliegues
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# 2. Imagen final, solo con el runtime de Java
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=compilacion /app/target/*.war app.war

# Las fechas (vigencia de cotizaciones, reservas completadas) usan la hora de Colombia
ENV TZ=America/Bogota
# Usa como máximo el 75 % de la memoria del contenedor (el plan gratuito tiene poca)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.war"]
