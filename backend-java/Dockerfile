# 1. Base image that have Java
FROM eclipse-temurin:17-jdk-focal

# 2. Install Python and PIP
RUN apt-get update && apt-get install -y python3 python3-pip

# 3. Establish work directory
WORKDIR /app

RUN pip3 install --upgrade pip
# ---------------------------------------------------------------

# 4. Copy Python's Agent files
COPY python_agent /app/python_agent

# 5. Install Python's dependencies
RUN pip3 install -r /app/python_agent/requirements.txt

# 6. Copy Java code and Maven Wrapper
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src ./src

# 7. Build Spring Boot's JAR without tests
RUN ./mvnw clean package -DskipTests

# 8. Command to start
CMD ["java", "-jar", "target/recommendatio-engine-0.0.1-SNAPSHOT.jar"]