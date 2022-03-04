package io.inblocks.civicpower.cryptopolitics.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.inblocks.civicpower.cryptopolitics.models.cards.Card;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import io.inblocks.civicpower.cryptopolitics.models.contexts.SeedGeneratorContext;
import io.inblocks.civicpower.cryptopolitics.models.selections.FromClass;
import io.inblocks.civicpower.cryptopolitics.models.transformations.AddCardsTransformation;
import io.inblocks.civicpower.cryptopolitics.models.transformations.InitTransformation;
import io.inblocks.civicpower.cryptopolitics.models.transformations.PickCardsTransformation;
import io.inblocks.civicpower.cryptopolitics.services.SeedService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

@MicronautTest
public
class TransformationTest {

  @Inject ObjectMapper mapper;

  @Inject
  SeedService seedService;

  final String serializedTalon = "{\"classes\":[{\"cardClass\":\"COMMON\",\"infinite\":false,\"series\":[{\"name\":\"pique\",\"size\":4096},{\"name\":\"coeur\",\"size\":4096},{\"name\":\"carreau\",\"size\":4096},{\"name\":\"trefle\",\"size\":4096}]},{\"cardClass\":\"JOKERS\",\"infinite\":false,\"series\":[{\"name\":\"jokers\",\"size\":2}]}],\"seedGenerator\":{\"name\":\"tests\",\"index\":10000}}";

  protected Context makeSomeContext() {
    return new SeedGeneratorContext(seedService, SeedGeneratorParams.builder().name("tests").index(10000L).build());
  }

  @Test
  void testInitDeserialization() throws JsonProcessingException {
    Transformation transformation =
        mapper.readValue(
                "{\"type\": \"Init\", \"setup\": " + serializedTalon + "}",
            Transformation.class);
    Assertions.assertTrue(transformation instanceof InitTransformation);
    Talon result = transformation.apply(makeSomeContext(), null);
    Talon expected = mapper.readValue(serializedTalon, Talon.class);
    Assertions.assertEquals(expected, result);
  }

  @Test
  void testPickCardsDeserialization() throws JsonProcessingException {
    Transformation transformation = mapper.readValue("""
    {"type": "PickCards", "selection":{
      "type":"Together", "selections":[
        {"type":"FromClass", "cardClass":"COMMON"},
        {"type":"FromClass","cardClass":"JOKERS"}]}}
    """, Transformation.class);
    Assertions.assertTrue(transformation instanceof PickCardsTransformation);
    Talon talon = mapper.readValue(serializedTalon, Talon.class);
    Talon newTalon = transformation.apply(makeSomeContext(), talon);
    Object results = transformation.getResults();
    Assertions.assertTrue(results instanceof List);
    List<Card> listResult = (List<Card>) results;
    Assertions.assertEquals(2, listResult.size());
    Assertions.assertEquals("COMMON", listResult.get(0).originalClass);
    Assertions.assertEquals("JOKERS", listResult.get(1).originalClass);
    Assertions.assertEquals(16383, newTalon.getCardClassByName("COMMON").count());
    Assertions.assertEquals(1, newTalon.getCardClassByName("JOKERS").count());
  }

  @Test
  void testAddCardsDeserialization() throws JsonProcessingException {
    Talon talon = mapper.readValue(serializedTalon, Talon.class);
    final PickCardsTransformation pickTransformation = new PickCardsTransformation(new FromClass("COMMON"));
    Talon remainingCards = pickTransformation.apply(makeSomeContext(), talon);
    Card card = ((List<Card>) pickTransformation.getResults()).get(0);

    Transformation transformation = mapper.readValue("{\"type\":\"AddCards\", \"cards\":[" + mapper.writeValueAsString(card) + "]}", Transformation.class);
    Assertions.assertTrue(transformation instanceof AddCardsTransformation);
    Talon newTalon = transformation.apply(makeSomeContext(), remainingCards);
    Assertions.assertEquals(16384, newTalon.getCardClassByName("COMMON").count());
  }
}
