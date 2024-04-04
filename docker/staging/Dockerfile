FROM openjdk:18
WORKDIR /app
COPY build/libs/mas-competition-backend-1.0-SNAPSHOT.jar app.jar
RUN echo "Checking for /home/jfa93/mas-engine/mas-engine-1.0.jar..." && \
  test -f /home/jfa93/mas-engine/mas-engine-1.0.jar && \
  echo "File found!" || echo "File not found!"
COPY /home/jfa93/mas-engine/mas-engine-1.0.jar /app/mas-engine.jar
EXPOSE 4500
CMD ["java", "-jar", "app.jar", "--server.port=4500", "--server.servlet.contextPath=/test", "--spring.application.name=mascompetition", "--spring.profiles.active=staging"]
