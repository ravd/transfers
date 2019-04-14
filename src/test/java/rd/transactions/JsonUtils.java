package rd.transactions;

public class JsonUtils {
    public static String buildTransferJson(String source, String target, String currency, String amount) {
        return String.format("{" +
                "\"sourceAccountId\": \"%s\"," +
                "\"targetAccountId\": \"%s\"," +
                "\"currency\": \"%s\"," +
                "\"amount\": \"%s\"" +
                "}", source, target, currency, amount);
    }

    public static String buildTransferJsonWithReadOnlyData(
            String source, String target, String currency,
            String amount, String uuid, String transferStatus) {
        return String.format("{" +
                "\"sourceAccountId\": \"%s\"," +
                "\"targetAccountId\": \"%s\"," +
                "\"currency\": \"%s\"," +
                "\"amount\": \"%s\"," +
                "\"transferId\": \"%s\"," +
                "\"transferStatus\": \"%s\"" +
                "}", source, target, currency, amount, uuid, transferStatus);
    }

    public static String buildAccountJson(String id, String currency, String balance) {
        return String.format("{" +
                "\"accountId\": \"%s\"," +
                "\"currency\": \"%s\"," +
                "\"amount\": \"%s\"" +
                "}", id, currency, balance);
    }
}
