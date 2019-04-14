package rd.transactions.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class UUIDGeneratorTest {

    @Test
    public void uuidIsGenerated() {
        UUIDGenerator uuidGenerator = new UUIDGenerator();
        assertThat(uuidGenerator).isNotNull();
    }
}