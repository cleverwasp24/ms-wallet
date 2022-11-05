FROM openjdk:17-oracle
ADD target/ms-wallet-0.0.1-SNAPSHOT.jar ms-wallet.jar
ENTRYPOINT ["java","-jar","ms-wallet.jar"]