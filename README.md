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

## Additional Information Regarding Requirements and Implementation
1. Accounts can be created with any currency (ISO 4217).
2. Transfers can be made only between accounts in same currency, also transfer currency has to be the same as source account currency.
3. If account balance after transfer is less than 0, the transfer is rejected.
4. Money amount is rounded HALF_EVEN to maximum precision supported by given currency.
5. There are endpoints returning all accounts and all transactions in the system, but they are for debug purposes only.
Normally such endpoints would not be necessary.
6. Transfer submission api is asynchronous and transfer processing is multi-threaded.
It may seem too much since account logs are in-memory and transfer happens instantaneously.
But in real world condition when transfer involves contacting multiple network services and making DB transactions this approach is better IMO.

Additional details regarding API and implementation are available in API Spec, Integration Tests and comments in the code.
   
## Other
   I tested the application on Ubuntu 18.04 and OpenJDK 1.8.0_191 using Maven 3.5.2
