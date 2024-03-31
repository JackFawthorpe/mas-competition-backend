FROM openjdk:20
WORKDIR /app
COPY build/libs/mas-competition-backend-1.0-SNAPSHOT.jar app.jar
EXPOSE 4000
CMD ["java", "-jar", "app.jar", "--server.port=4000", "--server.servlet.contextPath=/prod", "--spring.application.name=mascompetition", "--spring.profiles.active=production"]
