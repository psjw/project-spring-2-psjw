FROM openjdk:15

ARG JAR_FILE=build/libs/app.jar

WORKDIR /app/thisbox-api-server

COPY ${JAR_FILE}  /app/thisbox-api-server

CMD ["java","-jar","/app/thisbox-api-server/app.jar"]
