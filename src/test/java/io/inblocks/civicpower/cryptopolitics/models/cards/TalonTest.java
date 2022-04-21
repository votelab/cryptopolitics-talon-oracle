package io.inblocks.civicpower.cryptopolitics.models.cards;

import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassEmpty;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardClass;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.SerieRetirement;
import io.inblocks.civicpower.cryptopolitics.models.SeriesRetirementsByClass;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
                List.of(
                    CardClass.builder()
                        .cardClass(COMMON_CLASS)
                        .isInfinite(false)
                        .series(
                            List.of(
                                new CardSerie("test", 1, false).pickCard().remainingCards))
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
                List.of(
                    CardClass.builder()
                        .cardClass(COMMON_CLASS)
                        .isInfinite(false)
                        .series(List.of(new CardSerie("test", 1, false)))
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
                List.of(
                    CardClass.builder()
                        .cardClass(COMMON_CLASS)
                        .isInfinite(false)
                        .series(
                            List.of(
                                new CardSerie("test", 1, false).pickCard().remainingCards))
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
                List.of(
                    CardClass.builder()
                        .cardClass(COMMON_CLASS)
                        .isInfinite(false)
                        .series(List.of(new CardSerie("test", 1, false)))
                        .build()))
            .build();
    long seed = r.nextLong();
    Assertions.assertThrows(
        NoSuchCardClass.class, () -> talon.pickCardByClass(EPIC_CLASS, seed));
  }

  @Test
  void retireSeries() {
    final CardSerie serieOne = new CardSerie("one", 1, false);
    final CardSerie serieTwo = new CardSerie("two", 1, false);
    final CardSerie serieThree = new CardSerie("three", 1, false);
    final CardSerie serieFour = new CardSerie("four", 1, false);
    final Talon talon = Talon.builder()
            .classes(List.of(
                    CardClass.builder()
                            .cardClass(COMMON_CLASS)
                            .isInfinite(false)
                            .series(List.of(serieOne, serieTwo))
                            .build(),
                    CardClass.builder()
                            .cardClass(EPIC_CLASS)
                            .isInfinite(false)
                            .series(List.of(serieThree, serieFour))
                            .build()
            ))
            .build();
    final Talon newTalon = talon.retireSeries(List.of(
            new SeriesRetirementsByClass(COMMON_CLASS, List.of(new SerieRetirement("two", true))),
            new SeriesRetirementsByClass(EPIC_CLASS, List.of(new SerieRetirement("three", true)))));
    final CardClass newCommonClass = newTalon.getCardClassByName(COMMON_CLASS);
    Assertions.assertFalse(newCommonClass.getCardSerieByName("one").retired);
    Assertions.assertTrue(newCommonClass.getCardSerieByName("two").retired);
    final CardClass newEpicClass = newTalon.getCardClassByName(EPIC_CLASS);
    Assertions.assertFalse(newEpicClass.getCardSerieByName("four").retired);
    Assertions.assertTrue(newEpicClass.getCardSerieByName("three").retired);
  }

  @Test
  void retireSerieFromUnexistingClass() {
    final CardSerie serieOne = new CardSerie("one", 1, false);
    final Talon talon = Talon.builder()
            .classes(List.of(
                    CardClass.builder()
                            .cardClass(COMMON_CLASS)
                            .isInfinite(false)
                            .series(List.of(serieOne))
                            .build()
            ))
            .build();
    Assertions.assertThrows(NoSuchCardClass.class, () -> talon.retireSeries(List.of(new SeriesRetirementsByClass("FREE", List.of(new SerieRetirement("one", true))))));
  }
}
