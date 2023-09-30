FROM eclipse-temurin:17-jdk

COPY backend/target/backend-0.0.1-SNAPSHOT.jar /backend.jar

ENV TZ="Europe/Berlin"

EXPOSE 8081

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/backend.jar"]