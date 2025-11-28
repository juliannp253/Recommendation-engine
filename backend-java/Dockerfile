# 1. Usamos una imagen base que tenga Java (JDK 17 o la versión que uses)
FROM eclipse-temurin:17-jdk-focal

# 2. Instalamos Python y Pip en el mismo sistema
RUN apt-get update && apt-get install -y python3 python3-pip

# 3. Establecemos el directorio de trabajo
WORKDIR /app

# 4. Copiamos los archivos del Agente Python
COPY python_agent /app/python_agent

# 5. Instalamos las dependencias de Python
RUN pip3 install -r /app/python_agent/requirements.txt

# 6. Copiamos el código de Java y el Maven Wrapper
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

# 7. Construimos el JAR de Spring Boot (Saltando tests para ahorrar tiempo en deploy)
RUN ./mvnw clean package -DskipTests

# 8. Comando para iniciar la aplicación
# Ajusta el nombre del .jar al que genere tu proyecto (mira en target/)
CMD ["java", "-jar", "target/recommendatio-engine-0.0.1-SNAPSHOT.jar"]