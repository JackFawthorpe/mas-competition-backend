FROM openjdk:18
WORKDIR /app
COPY build/libs/mas-competition-backend-1.0-SNAPSHOT.jar app.jar
COPY /mas-engine-1.0.jar /app/mas-engine.jar
EXPOSE 4500
CMD ["java", "-jar", "app.jar", "--server.port=4500", "--server.servlet.contextPath=/test", "--spring.application.name=mascompetition", "--spring.profiles.active=staging"]
