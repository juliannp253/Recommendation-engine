FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

ENV MAVEN_OPTS="-Xmx512m"

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

RUN pip3 install --no-cache-dir --upgrade pip

COPY --from=build /app/target/*.jar app.jar

COPY python_agent /app/python_agent
RUN pip3 install --no-cache-dir -r /app/python_agent/requirements.txt

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]