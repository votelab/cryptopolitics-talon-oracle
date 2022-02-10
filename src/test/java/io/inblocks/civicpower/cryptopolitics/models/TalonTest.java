package io.inblocks.civicpower.cryptopolitics.models;

import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassEmpty;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardClass;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
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
                                new CardSerie("test", 1).pickNextCard().remainingCards))
                        .build()))
            .build();
    long seed = r.nextLong();
    Assertions.assertThrows(
        CardClassEmpty.class, () -> talon.pickNextCard(COMMON_CLASS, seed));
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
    PickNextCardResult pick = talon.pickNextCard(COMMON_CLASS, r.nextLong());
    Assertions.assertEquals(new Card(COMMON_CLASS, "test", 1), pick.card);
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
                                new CardSerie("test", 1).pickNextCard().remainingCards))
                        .build()))
            .build();
    Talon restoredTalon = talon.addCard(new Card(COMMON_CLASS, "test", 1));
    Assertions.assertEquals(
        0, talon.getCardClassDataByClass(COMMON_CLASS).getSerieByName("test").count());
    Assertions.assertEquals(
        1, restoredTalon.getCardClassDataByClass(COMMON_CLASS).getSerieByName("test").count());
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
        NoSuchCardClass.class, () -> talon.pickNextCard(EPIC_CLASS, seed));
  }
}
