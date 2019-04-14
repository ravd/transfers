package rd.transactions.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Currency;

public class CurrencySerializer extends StdSerializer<Currency> {
    protected CurrencySerializer() {
        super(Currency.class);
    }

    @Override
    public void serialize(Currency value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.getCurrencyCode());
    }
}
