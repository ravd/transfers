package rd.transactions;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static rd.transactions.JsonUtils.buildAccountJson;
import static rd.transactions.JsonUtils.buildTransferJson;

@Category(IntegrationTest.class)
public class HttpAppIntegrationTest {
    private final static int HTTP_PORT = 8889;
    private final static String HOSTNAME = "localhost";
    private final static Logger logger = LoggerFactory.getLogger(HttpAppIntegrationTest.class);

    private final int INDENT = 2;
    private final String ACCOUNTS = url("accounts");
    private final String TRANSFERS = url("transfers");

    private HttpApp app;
    private ExecutorService executor;

    @Before
    public void startServer() {
        executor = Executors.newSingleThreadExecutor();

        app = new HttpApp(HTTP_PORT);
        executor.submit(() -> {
            try {
                app.run();
            } catch (Throwable ex) {
                logger.error("Error in server's thread", ex);
            }
        });
        logger.info("Waiting for the server to start.");
        await().atMost(10, SECONDS)
                .until(() ->
                        Unirest.get(ACCOUNTS)
                                .asJson()
                                .getStatus() == OK_200);
        logger.info("Server started.");
    }

    @After
    public void stopServer() throws InterruptedException {
        app.stopServer();
        executor.shutdownNow();
        executor.awaitTermination(60, SECONDS);
    }

    @Test
    public void performBasicApiCalls() throws UnirestException, JSONException {
        logger.info(">>> ===========================================================================================");
        logger.info(">>> Let's start by adding some accounts.");

        String[] accounts = new String[]{
            buildAccountJson("A", "EUR", "350.50"),
            buildAccountJson("B", "USD", "100.00"),
            buildAccountJson("C", "EUR", "0.20")
        };
        for (String account : accounts) {
            logger.info(">>> POST /accounts {}", account);
            HttpResponse<JsonNode> response = Unirest.post(ACCOUNTS)
                    .body(account)
                    .asJson();
            assertThat(response.getStatus()).isEqualTo(OK_200);
        }

        logger.info(">>> GET /accounts");
        HttpResponse<JsonNode> accountsResponse = Unirest.get(ACCOUNTS)
                .asJson();
        assertThat(accountsResponse.getBody().getArray().length()).isEqualTo(3);
        logger.info(">>> 3 accounts added {}", bodyToString(accountsResponse));

        logger.info(">>> Now let's transfer 50 EUR from account A to C");
        String transfer50EurFromAToC = buildTransferJson("A", "C", "EUR", "50.00");
        logger.info(">>> POST /transfers " + transfer50EurFromAToC);
        HttpResponse<JsonNode> transferResponse = Unirest.post(TRANSFERS)
                .body(transfer50EurFromAToC)
                .asJson();
        assertThat(transferResponse.getStatus()).isEqualTo(OK_200);
        logger.info(">>> /transfer api is asynchronous, transfer is registered in queue with some transferId {} " +
                        "and processed by the server in background",
                bodyToString(transferResponse));
        logger.info(">>> Initially transfer is in SUBMITTED status, then after completion it changes to COMPLETED status.");
        String transferUuid = transferResponse.getBody().getObject().getString("transferId");
        logger.info(">>> Let's wait until transfer is completed, by GETing /transfers/{}.", transferUuid);

        await().atMost(10, SECONDS).until(() -> {
            logger.info(">>> GET /transfers/{}.", transferUuid);
            HttpResponse<JsonNode> transferDetails = Unirest.get(TRANSFERS + "/" + transferUuid).asJson();
            logger.info("Got response {}", bodyToString(transferDetails));
            return transferDetails.getBody().getObject().get("transferStatus").equals("COMPLETED");
        });
        logger.info(">>> Transfer is processed almost instantaneously, ");
        logger.info(">>> But we can imagine that when normal DB and other micro-services are involved it takes couple seconds.");
        logger.info(">>> This is why I decided for asynchronous api for transfers.");
        logger.info(">>> Let's check if transfer is correctly reflected on account A's and C's balances");

        {
            logger.info(">>> GET /accounts/A");
            HttpResponse<JsonNode> accountADetails = Unirest.get(ACCOUNTS + "/A").asJson();
            assertThat(accountADetails.getStatus()).isEqualTo(OK_200);
            logger.info(">>> GET /accounts/C");
            HttpResponse<JsonNode> accountCDetails = Unirest.get(ACCOUNTS + "/C").asJson();
            assertThat(accountCDetails.getStatus()).isEqualTo(OK_200);

            assertThat(accountADetails.getBody().getObject().getString("amount")).isEqualTo("300.50");
            assertThat(accountCDetails.getBody().getObject().getString("amount")).isEqualTo("50.20");

            logger.info(">>> The transfer was successful {} {}",
                    bodyToString(accountADetails),
                    bodyToString(accountCDetails));
        }

        logger.info(">>> This is it regarding normal operation.");

        logger.info(">>> There are couple of transfer rejection scenarios.");
        logger.info(">>> Accounts can be created with any currency, but transfers work only in single currency.");
        logger.info(">>> This is why transferring 30 USD from B to A won't work.");

        {
            String transfer20UsdFromBToA = buildTransferJson("B", "A", "USD", "20.00");
            logger.info(">>> POST /transfers {}", transfer20UsdFromBToA);
            HttpResponse<JsonNode> transferBtoAResponse = Unirest.post(TRANSFERS)
                    .body(transfer20UsdFromBToA)
                    .asJson();
            String transferBtoAUuid = transferBtoAResponse.getBody().getObject().getString("transferId");
            await().atMost(10, SECONDS).until(() -> {
                logger.info(">>> GET /transfers/{}", transferBtoAUuid);
                HttpResponse<JsonNode> transferDetails = Unirest.get(TRANSFERS + "/" + transferBtoAUuid).asJson();
                logger.info("Got response {}", bodyToString(transferDetails));
                return transferDetails.getBody().getObject().get("transferStatus").equals("REJECTED_DIFFERENT_CURRENCIES");
            });
        }

        logger.info(">>> Yup REJECTED_DIFFERENT_CURRENCIES, you could transfer USD to another account in USD though.");

        logger.info(">>> Another thing you can't do is transferring more than you got.");
        logger.info(">>> This is why C won't be able to transfer 100 EUR to A, she has only 50.20 EUR.");

        {
            String transfer100EURFromCToA = buildTransferJson("C", "A", "EUR", "100.00");
            logger.info(">>> POST /transfers {}", transfer100EURFromCToA);
            HttpResponse<JsonNode> transferCtoAResponse = Unirest.post(TRANSFERS)
                    .body(transfer100EURFromCToA)
                    .asJson();
            String transferCtoAUuid = transferCtoAResponse.getBody().getObject().getString("transferId");
            await().atMost(10, SECONDS).until(() -> {
                logger.info(">>> GET /transfers/{}", transfer100EURFromCToA);
                HttpResponse<JsonNode> transferDetails = Unirest.get(TRANSFERS + "/" + transferCtoAUuid).asJson();
                logger.info("Got response {}", bodyToString(transferDetails));
                return transferDetails.getBody().getObject()
                        .get("transferStatus").equals("REJECTED_NOT_ENOUGH_CREDIT_ON_SOURCE_ACCOUNT");
            });
        }

        {
            logger.info(">>> GET /accounts/A");
            HttpResponse<JsonNode> accountADetails = Unirest.get(ACCOUNTS + "/A").asJson();
            assertThat(accountADetails.getStatus()).isEqualTo(OK_200);
            logger.info(">>> GET /accounts/C");
            HttpResponse<JsonNode> accountCDetails = Unirest.get(ACCOUNTS + "/C").asJson();
            assertThat(accountCDetails.getStatus()).isEqualTo(OK_200);

            assertThat(accountADetails.getBody().getObject().getString("amount")).isEqualTo("300.50");
            assertThat(accountCDetails.getBody().getObject().getString("amount")).isEqualTo("50.20");
        }

        logger.info(">>> When transfer is rejected it has no effect on neither source nor target accounts.");
        logger.info(">>> ===========================================================================================");
    }

    @Test
    /**
     * This test checks if account balances calculated on server-side (multi-threaded)
     *  are the same with the ones calculated in test (single-thread)
     *
     * It creates accounts, and then performs multiple thousands transactions.
     *  The test keeps track of source and destination accounts to calculate expected balances.
     *
     * Initial Value is much larger than number of transactions (amount = 1.0)
     *  because if there are rejections due to low amount of credit, there is unpredictability.
     *  In such case for two "simultaneous" transactions, we don't know which transaction wins the race.
     */
    public void performCheckThatThereAreNoAnomaliesRelatedToMutiThreading() throws UnirestException, JSONException {

        final int TIMEOUT_FOR_TRANSACTION_COMPLETION_SEC = 240;
        final int NUM_ACCOUNTS = 50;
        List<Integer> accounts = IntStream.range(0, NUM_ACCOUNTS).boxed().collect(toList());
        List<Integer> balances = new ArrayList<>(NUM_ACCOUNTS);
        final int INITIAL_VALUE = 10000000;
        final int NUM_TRANSACTIONS = 100000;

        logger.info(">>> Creating {} accounts with {} EUR each", NUM_ACCOUNTS, INITIAL_VALUE);
        for (int i = 0; i < NUM_ACCOUNTS; i++) {
            String accountBody = buildAccountJson(String.valueOf(i), "EUR", String.valueOf(INITIAL_VALUE));
            HttpResponse<JsonNode> response = Unirest.post(ACCOUNTS).body(accountBody).asJson();
            assertThat(response.getStatus()).isEqualTo(OK_200);
            balances.add(INITIAL_VALUE);
        }

        List<String> transactions = new ArrayList<>(NUM_TRANSACTIONS);
        for (int i = 0; i < NUM_TRANSACTIONS; i++) {
            Collections.shuffle(accounts);
            int a = accounts.get(0);
            int b = accounts.get(1);
            transactions.add(
                    buildTransferJson(String.valueOf(a), String.valueOf(b), "EUR", "1.00"));
            balances.set(a, balances.get(a) - 1);
            balances.set(b, balances.get(b) + 1);
        }

        logger.info(">>> Expecting to get balances like these: {}", balances);

        logger.info(">>> Submitting {} transactions.", NUM_TRANSACTIONS);
        logger.info(">>> This step tends to be slow, there is no batch api for transfer submission.");

        List<String> transferUuids = new ArrayList<>(NUM_TRANSACTIONS);
        for (String transaction : transactions) {
            HttpResponse<JsonNode> response = Unirest.post(TRANSFERS).body(transaction).asJson();
            assertThat(response.getStatus()).isEqualTo(OK_200);
            transferUuids.add(response.getBody().getObject().getString("transferId"));
        }

        logger.info(">>> Submitted transactions.");

        // wait for all transactions to finish
        await().atMost(TIMEOUT_FOR_TRANSACTION_COMPLETION_SEC, SECONDS)
                .pollInterval(5, SECONDS)
                .until(() -> {
            HttpResponse<JsonNode> allTransfers = Unirest.get(TRANSFERS).asJson();
            JSONArray transfers = allTransfers.getBody().getArray();
            logger.info(">>> Got {} transfers, checking if all are completed.", transfers.length());
            for (int i = 0; i < transfers.length(); i++) {
                if (!transfers.getJSONObject(i).get("transferStatus").equals("COMPLETED")) {
                    logger.info(">>> There are uncompleted transfers, let's try again.");
                    return false;
                }
            }
            return true;
        });

        logger.info(">>> All transactions completed.");

        for (int i = 0; i < NUM_ACCOUNTS; i++) {
            HttpResponse<JsonNode> accountDetails = Unirest.get(ACCOUNTS + "/" + i).asJson();
            BigDecimal expectedBalance = new BigDecimal(balances.get(i) + ".00");
            BigDecimal actualBalance = new BigDecimal(accountDetails.getBody().getObject().getString("amount"));
            logger.info(">>> Account {} expected = {} got {}", i, expectedBalance, actualBalance);
            assertThat(actualBalance).isEqualByComparingTo(expectedBalance);
        }

        logger.info(">>> All balances check.");
    }

    private String bodyToString(HttpResponse<JsonNode> response) throws JSONException {
        if (response.getBody().getObject() != null) {
            return response.getBody().getObject().toString(INDENT);
        } else {
            return response.getBody().getArray().toString(INDENT);
        }
    }

    private String url(String resource) {
        return "http://" + HOSTNAME + ":" + HTTP_PORT + "/v1/" + resource;
    }
}