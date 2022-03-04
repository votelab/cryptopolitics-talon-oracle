package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassFinitudeMismatch;
import io.inblocks.civicpower.cryptopolitics.exceptions.TalonAlreadyInitialized;
import io.inblocks.civicpower.cryptopolitics.models.Context;
import io.inblocks.civicpower.cryptopolitics.models.SeedGeneratorParams;
import io.inblocks.civicpower.cryptopolitics.models.TransformationTest;
import io.inblocks.civicpower.cryptopolitics.models.cards.CardClass;
import io.inblocks.civicpower.cryptopolitics.models.cards.CardSerie;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

@MicronautTest
class InitTransformationTest extends TransformationTest {

  @Test
  void cantReinitializeTalon() {
    Talon talon =
        Talon.builder()
            .classes(
                    List.of(
                            CardClass.builder()
                                    .cardClass("COMMON")
                                    .isInfinite(false)
                                    .series(List.of(new CardSerie("cards", 52)))
                                    .build()))
            .build();
    Talon setup =
        Talon.builder()
            .classes(
                Arrays.asList(
                    CardClass.builder()
                        .cardClass("COMMON")
                        .isInfinite(false)
                        .series(
                            Arrays.asList(
                                new CardSerie("pique", 13),
                                new CardSerie("coeur", 13),
                                new CardSerie("carreau", 13),
                                new CardSerie("trèfle", 13)))
                        .build()))
                .build();
    SeedGeneratorParams seedGeneratorParams = SeedGeneratorParams.builder()
                    .name("tests")
                    .index(10000L)
                    .build();
    InitTransformation init = new InitTransformation(setup, seedGeneratorParams);
    Context context = makeSomeContext();
    Assertions.assertThrows(TalonAlreadyInitialized.class, () -> init.apply(context, talon));
  }

  @Test
  void returnsSetup() {
    Talon setup =
        Talon.builder()
            .classes(
                List.of(
                    CardClass.builder()
                        .cardClass("COMMON")
                        .isInfinite(false)
                        .series(
                            Arrays.asList(
                                new CardSerie("pique", 13),
                                new CardSerie("coeur", 13),
                                new CardSerie("carreau", 13),
                                new CardSerie("trèfle", 13)))
                        .build()))
                .build();
    SeedGeneratorParams seedGeneratorParams = SeedGeneratorParams.builder()
                    .name("tests")
                    .index(9000L)
                    .build();
    InitTransformation init = new InitTransformation(setup, seedGeneratorParams);
    final Context context = makeSomeContext();
    Talon talon = init.apply(context, null);
    Assertions.assertEquals(setup, talon);
    Assertions.assertNull(init.getResults());
    Assertions.assertEquals(seedGeneratorParams, context.getSeedGeneratorParams());
  }

  @Test
  void cantUseInfiniteSerieInFiniteClass() {
    Talon setup = Talon.builder()
            .classes(List.of(CardClass.builder()
                            .cardClass("COMMON")
                            .isInfinite(false)
                            .series(List.of(new CardSerie("pique", null)))
                    .build()))
            .build();
    SeedGeneratorParams seedGeneratorParams = SeedGeneratorParams.builder()
            .name("tests")
            .index(9000L)
            .build();
    Assertions.assertThrows(CardClassFinitudeMismatch.class, () ->  new InitTransformation(setup, seedGeneratorParams));
  }

  @Test
  void cantUseFiniteSerieInInfiniteClass() {
    Talon setup = Talon.builder()
            .classes(List.of(CardClass.builder()
                    .cardClass("COMMON")
                            .isInfinite(true)
                    .series(List.of(new CardSerie("pique", 3)))
                    .build()))
            .build();
    SeedGeneratorParams seedGeneratorParams = SeedGeneratorParams.builder()
            .name("tests")
            .index(9000L)
            .build();
    Assertions.assertThrows(CardClassFinitudeMismatch.class, () ->  new InitTransformation(setup, seedGeneratorParams));
  }
}
