FROM eclipse-temurin:11-jre

WORKDIR /app

COPY target/vuelokbt-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]