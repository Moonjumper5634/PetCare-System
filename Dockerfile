FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S petcare && adduser -S petcare -G petcare
WORKDIR /app
COPY --from=build /build/target/petcare-*.jar /app/petcare.jar
USER petcare
EXPOSE 8081
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-Djava.security.egd=file:/dev/urandom","-jar","/app/petcare.jar"]
