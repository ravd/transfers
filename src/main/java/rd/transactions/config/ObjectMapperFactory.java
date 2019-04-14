package rd.transactions.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.util.Currency;

public class ObjectMapperFactory {

    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // due to jsr310 being on classpath adds support for Instant serialization/deserialization
        objectMapper.findAndRegisterModules();
        // add support for bigDecimal output
        SimpleModule module = new SimpleModule();
        module.addSerializer(BigDecimal.class, new ToStringSerializer());
        module.addSerializer(Currency.class, new CurrencySerializer());
        module.addDeserializer(Currency.class, new CurrencyDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
