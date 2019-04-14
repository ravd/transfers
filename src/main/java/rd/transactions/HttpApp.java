package rd.transactions;

import rd.transactions.config.UUIDGenerator;
import rd.transactions.controller.account.AccountController;
import rd.transactions.controller.account.AccountConverter;
import rd.transactions.controller.transfer.TransferController;
import rd.transactions.controller.transfer.TransferConverter;
import rd.transactions.exceptions.*;
import rd.transactions.managers.AccountManager;
import rd.transactions.managers.TransferManager;
import rd.transactions.validators.BalanceValidator;
import rd.transactions.validators.NonNegativeBalanaceValidator;
import rd.transactions.validators.TransferAmountValidator;
import spark.Service;

import java.util.concurrent.*;

import static org.eclipse.jetty.http.HttpStatus.*;
import static spark.Service.ignite;

class HttpApp {
    private final static String JSON_CONTENT_TYPE = "application/json";
    private final static String API_VER = "v1";
    private final static String NOT_FOUND_MESSAGE = "Not found.";
    private final static String INTERNAL_ERROR_MESSAGE = "Internal server error.";


    private final static int DEFAULT_SERVER_PORT = 8888;

    private final static int MIN_THREADS_FOR_TRANSFER_PROCESSING = 4;
    private final static int MAX_THREADS_FOR_TRANSFER_PROCESSING = 20;
    private final static long THREAD_KA_SECONDS_FOR_TRANSFER_PROCESSING = 60;

    private final int port;
    private final Service spark;

    HttpApp() {
        this(DEFAULT_SERVER_PORT);
    }

    HttpApp(int port) {
        this.port = port;
        this.spark = ignite();
    }

    void run() {
        BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<>();
        ExecutorService executorService = new ThreadPoolExecutor(
                MIN_THREADS_FOR_TRANSFER_PROCESSING,
                MAX_THREADS_FOR_TRANSFER_PROCESSING,
                THREAD_KA_SECONDS_FOR_TRANSFER_PROCESSING,
                TimeUnit.SECONDS, workQueue
        );

        BalanceValidator balanceValidator = new NonNegativeBalanaceValidator();
        AccountManager accountManager = new AccountManager(balanceValidator);
        TransferManager transferManager = new TransferManager(accountManager, executorService);
        AccountController accountController = new AccountController(
                accountManager, balanceValidator, new AccountConverter());
        UUIDGenerator uuidGenerator = new UUIDGenerator();
        TransferController transferController = new TransferController(
                transferManager, new TransferAmountValidator(), new TransferConverter(), uuidGenerator);

        configureServer();

        configurePaths(accountController, transferController);

        configureErrorHandling();
    }

    void stopServer() {
        spark.stop();
    }

    private void configureServer() {
        spark.port(this.port);
    }

    private void configurePaths(AccountController accountController, TransferController transferController) {
        spark.path("/" + API_VER, () -> {
            spark.get("/accounts", JSON_CONTENT_TYPE,
                    (request, response) -> accountController.getAccounts());
            spark.get("/accounts/:id", JSON_CONTENT_TYPE,
                    (request, response) -> accountController.getAccount(request.params(":id")));
            spark.post("/accounts", JSON_CONTENT_TYPE,
                    (request, response) -> accountController.addAccount(request.body()));
            spark.get("/transfers", JSON_CONTENT_TYPE,
                    (request, response) -> transferController.getTransfers());
            spark.get("/transfers/:id", JSON_CONTENT_TYPE,
                    (request, response) -> transferController.getTransfer(request.params(":id")));
            spark.post("/transfers", JSON_CONTENT_TYPE,
                    (request, response) -> transferController.submitTransfer(request.body()));
        });
    }

    private void configureErrorHandling() {
        handleExceptionWithStatusCode(AccountBadRequestException.class, BAD_REQUEST_400);
        handleExceptionWithStatusCode(AccountIdConflictException.class, CONFLICT_409);
        handleExceptionWithStatusCode(AccountDoesNotExistException.class, NOT_FOUND_404);
        handleExceptionWithStatusCode(BalanceInvalidException.class, UNPROCESSABLE_ENTITY_422);
        handleExceptionWithStatusCode(TransferAmountInvalidException.class, UNPROCESSABLE_ENTITY_422);
        handleExceptionWithStatusCode(TransferNotFoundException.class, NOT_FOUND_404);

        spark.notFound((req, res) -> {
            res.type(JSON_CONTENT_TYPE);
            return buildErrorMessage(NOT_FOUND_404, NOT_FOUND_MESSAGE);
        });
        spark.internalServerError((req, res) -> {
            res.type(JSON_CONTENT_TYPE);
            return buildErrorMessage(INTERNAL_SERVER_ERROR_500, INTERNAL_ERROR_MESSAGE);
        });
    }

    private void handleExceptionWithStatusCode(Class<? extends Exception> exceptionClass, int httpStatus) {
        spark.exception(exceptionClass, (exception, request, response) -> {
            response.type(JSON_CONTENT_TYPE);
            response.status(httpStatus);
            response.body(buildErrorMessage(httpStatus, exception.getMessage()));
        });
    }

    private String buildErrorMessage(int code, String message) {
        return String.format("{\"errorCode\": %d, \"message\": \"%s\"}", code, message);
    }
}
