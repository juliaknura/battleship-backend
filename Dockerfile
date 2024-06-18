FROM openjdk:21

COPY target/battleship-0.0.1-SNAPSHOT.jar battleship_backend.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "battleship_backend.jar"]