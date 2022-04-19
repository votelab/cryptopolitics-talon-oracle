package io.inblocks.civicpower.cryptopolitics;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;

@OpenAPIDefinition(
    info = @Info(
            title = "Cryptopolitics Selection",
            version = "1.1",
            description = "Cryptopolitics talon transformations engine",
            license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT"),
            contact =
            @Contact(
                    url = "https://inblocks.io/",
                    name = "InBlocks developers",
                    email = "contact@inblocks.io")
    )
)
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }

    @Singleton
    static class ObjectMapperBeanEventListener implements BeanCreatedEventListener<ObjectMapper> {

        @Override
        public ObjectMapper onCreated(BeanCreatedEvent<ObjectMapper> event) {
            final ObjectMapper mapper = event.getBean();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(
                    BigInteger.class,
                    new JsonDeserializer<>() {
                        private final Base64.Decoder decoder = Base64.getDecoder();

                        @Override
                        public BigInteger deserialize(JsonParser parser, DeserializationContext context)
                                throws IOException {
                            return new BigInteger(decoder.decode(parser.readValueAs(String.class)));
                        }
                    });
            module.addSerializer(
                    BigInteger.class,
                    new JsonSerializer<>() {
                        private final Base64.Encoder encoder = Base64.getEncoder();

                        @Override
                        public void serialize(BigInteger value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                            gen.writeString(encoder.encodeToString(value.toByteArray()));
                        }
                    });
            mapper.registerModule(module);
            return mapper;
        }
    }
}
