package io.inblocks.civicpower.cryptopolitics.models;

import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassEmpty;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardClass;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@MicronautTest
class TalonTest {

  private final String COMMON_CLASS = "Common";
  private final String EPIC_CLASS = "Epic";

  private final Random r = new Random();

  @Test
  void cantDealFromEmptyTalon() {
    Talon talon =
        Talon.builder()
            .classes(
                Collections.singletonList(
                    CardClass.builder()
                        .cardClass(COMMON_CLASS)
                        .series(
                            Collections.singletonList(
                                new CardSerie("test", 1).pickCard().remainingCards))
                        .build()))
            .build();
    long seed = r.nextLong();
    Assertions.assertThrows(
        CardClassEmpty.class, () -> talon.pickCardByClass(COMMON_CLASS, seed));
  }

  @Test
  void dealACard() {
    Talon talon =
        Talon.builder()
            .classes(
                Collections.singletonList(
                    CardClass.builder()
                        .cardClass(COMMON_CLASS)
                        .series(Collections.singletonList(new CardSerie("test", 1)))
                        .build()))
            .build();
    SelectionResult pick = talon.pickCardByClass(COMMON_CLASS, r.nextLong());
    Assertions.assertEquals(List.of(new Card(COMMON_CLASS, "test", 1)), pick.cards);
  }

  @Test
  void putBackACard() {
    Talon talon =
        Talon.builder()
            .classes(
                Collections.singletonList(
                    CardClass.builder()
                        .cardClass(COMMON_CLASS)
                        .series(
                            Collections.singletonList(
                                new CardSerie("test", 1).pickCard().remainingCards))
                        .build()))
            .build();
    Talon restoredTalon = talon.addCard(new Card(COMMON_CLASS, "test", 1));
    Assertions.assertEquals(
        0, talon.getCardClassByName(COMMON_CLASS).getCardSerieByName("test").count());
    Assertions.assertEquals(
        1, restoredTalon.getCardClassByName(COMMON_CLASS).getCardSerieByName("test").count());
  }

  @Test
  void cantDealAnotherClass() {
    Talon talon =
        Talon.builder()
            .classes(
                Collections.singletonList(
                    CardClass.builder()
                        .cardClass(COMMON_CLASS)
                        .series(Collections.singletonList(new CardSerie("test", 1)))
                        .build()))
            .build();
    long seed = r.nextLong();
    Assertions.assertThrows(
        NoSuchCardClass.class, () -> talon.pickCardByClass(EPIC_CLASS, seed));
  }
}
