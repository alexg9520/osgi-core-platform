FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY ./core-app/target/core-app.jar /app/core-app.jar
ENTRYPOINT java -jar core-app.jar

# Open required ports
EXPOSE 2222
EXPOSE 8443