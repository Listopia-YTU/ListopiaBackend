FROM openjdk:21-jdk
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} listopia.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "listopia.jar"]