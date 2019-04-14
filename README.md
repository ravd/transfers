# Transfers API

This is simple transfer API implementation allowing for money transfers between accounts.

## Build and run

### To build and test
```
mvn clean verify
```
### To run only integration-tests once the package is built.
```
mvn failsafe:integration-test
```
### There are 2 integration tests.
   `HttpAppIntegrationTest.performBasicCalls` demonstrates the API (I added explanations in logs). 

   `HttpAppIntegrationTest.performCheckThatThereAreNoAnomaliesRelatedToMutiThreading` checks transfer processing engine, it is long running ~1 minute.

   Integration tests start http server on port `8889` (it can be changed via `HttpAppIntegrationTest::HTTP_PORT`).
  
### To start the application
```
mvn clean package
java -jar target/transactions-1.0.jar
```
   The application starts on port `8888`, this can be changed via `HttpApp::DEFAULT_SERVER_PORT`
   
## The API
   API specification is available in [doc/swagger.yaml](doc/swagger.yaml) file.
   
   You can browse the API spec in more human friendly way by copying swagger.yaml URL into sample Swagger UI.
   
   swagger.yaml URL `https://raw.githubusercontent.com/ravd/transfers/master/doc/swagger.yaml`
   
   Swagger UI: https://petstore.swagger.io/
   
   Screenshot below shows how to do this:
   
   ![Alt text](doc/helpful_screen.png?raw=true)
   
## Other
   I tested the application on Ubuntu 18.04 and OpenJDK 1.8.0_191 using Maven 3.5.2
