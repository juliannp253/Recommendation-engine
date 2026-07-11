# ----- STAGE 1: Build (La "Fábrica") -----
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Optimizamos RAM para Maven en la t2.micro
ENV MAVEN_OPTS="-Xmx512m"

# Copiamos solo lo necesario para descargar dependencias (Cache Layer)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copiamos el código y construimos el JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ----- STAGE 2: Runtime (El "Servidor" ligero) -----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Instalamos Python de forma mínima
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

RUN pip3 install --no-cache-dir --upgrade pip

# Copiamos solo el JAR resultante de la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Copiamos el agente de Python
COPY python_agent /app/python_agent
RUN pip3 install --no-cache-dir -r /app/python_agent/requirements.txt

# Exponemos el puerto de Spring Boot
EXPOSE 8080

# Comando de ejecución con límites de memoria para la JVM
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]