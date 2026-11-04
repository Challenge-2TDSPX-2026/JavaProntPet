FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# o container nao roda como root
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/*.jar app.jar
USER app

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

# a porta vem da variavel PORT (padrao 8080) via application.properties
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
