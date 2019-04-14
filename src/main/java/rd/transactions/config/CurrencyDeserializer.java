package rd.transactions.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Currency;

public class CurrencyDeserializer extends StdDeserializer<Currency> {
    protected CurrencyDeserializer() {
        super(Currency.class);
    }

    @Override
    public Currency deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return Currency.getInstance(p.readValueAs(String.class));
    }
}
