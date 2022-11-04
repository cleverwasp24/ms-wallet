FROM openjdk:17-oracle
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ms-wallet.jar
ENTRYPOINT ["java","-jar","/ms-wallet.jar"]