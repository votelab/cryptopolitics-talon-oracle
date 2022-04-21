package io.inblocks.civicpower.cryptopolitics.models.cards;

import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassEmpty;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardClass;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
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
                List.of(
                    CardClass.builder()
                        .cardClass(COMMON_CLASS)
                        .isInfinite(false)
                        .series(List.of(new CardSerie("test", 1)))
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
                List.of(
                    CardClass.builder()
                        .cardClass(COMMON_CLASS)
                        .isInfinite(false)
                        .series(List.of(new CardSerie("test", 1)))
                        .build()))
            .build();
    long seed = r.nextLong();
    Assertions.assertThrows(
        NoSuchCardClass.class, () -> talon.pickCardByClass(EPIC_CLASS, seed));
  }

  @Test
  void retireSeries() {
    final CardSerie serieOne = new CardSerie("one", 1);
    final CardSerie serieTwo = new CardSerie("two", 1);
    final CardSerie serieThree = new CardSerie("three", 1);
    final CardSerie serieFour = new CardSerie("four", 1);
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
    final Talon newTalon = talon.modifyActiveSeries(List.of(
            new Talon.SeriesSelection(COMMON_CLASS, List.of("two")),
            new Talon.SeriesSelection(EPIC_CLASS, List.of("three"))), null);
    final CardClass newCommonClass = newTalon.getCardClassByName(COMMON_CLASS);
    Assertions.assertEquals(List.of(serieOne), newCommonClass.series);
    Assertions.assertEquals(List.of(serieTwo), newCommonClass.retiredSeries);
    final CardClass newEpicClass = newTalon.getCardClassByName(EPIC_CLASS);
    Assertions.assertEquals(List.of(serieFour), newEpicClass.series);
    Assertions.assertEquals(List.of(serieThree), newEpicClass.retiredSeries);
  }
  @Test
  void reinstateSeries() {
    final CardSerie serieOne = new CardSerie("one", 1);
    final CardSerie serieTwo = new CardSerie("two", 1);
    final CardSerie serieThree = new CardSerie("three", 1);
    final CardSerie serieFour = new CardSerie("four", 1);
    final Talon talon = Talon.builder()
            .classes(List.of(
                    CardClass.builder()
                            .cardClass(COMMON_CLASS)
                            .isInfinite(false)
                            .series(List.of(serieOne))
                            .retiredSeries(List.of(serieTwo))
                            .build(),
                    CardClass.builder()
                            .cardClass(EPIC_CLASS)
                            .isInfinite(false)
                            .series(List.of(serieFour))
                            .retiredSeries(List.of(serieThree))
                            .build()
            ))
            .build();
    final Talon newTalon = talon.modifyActiveSeries(null, List.of(
            new Talon.SeriesSelection(COMMON_CLASS, List.of("two")),
            new Talon.SeriesSelection(EPIC_CLASS, List.of("three"))));
    final CardClass newCommonClass = newTalon.getCardClassByName(COMMON_CLASS);
    Assertions.assertEquals(List.of(serieOne, serieTwo), newCommonClass.series);
    Assertions.assertEquals(List.of(), newCommonClass.retiredSeries);
    final CardClass newEpicClass = newTalon.getCardClassByName(EPIC_CLASS);
    Assertions.assertEquals(List.of(serieFour, serieThree), newEpicClass.series);
    Assertions.assertEquals(List.of(), newEpicClass.retiredSeries);
  }

  @Test
  void retireSerieFromUnexistingClass() {
    final CardSerie serieOne = new CardSerie("one", 1);
    final Talon talon = Talon.builder()
            .classes(List.of(
                    CardClass.builder()
                            .cardClass(COMMON_CLASS)
                            .isInfinite(false)
                            .series(List.of(serieOne))
                            .build()
            ))
            .build();
    Assertions.assertThrows(NoSuchCardClass.class, () -> talon.modifyActiveSeries(List.of(new Talon.SeriesSelection("FREE", List.of("one"))), null));
  }

  @Test
  void reinstateSerieFromUnexistingClass() {
    final CardSerie serieOne = new CardSerie("one", 1);
    final Talon talon = Talon.builder()
            .classes(List.of(
                    CardClass.builder()
                            .cardClass(COMMON_CLASS)
                            .isInfinite(false)
                            .series(List.of(serieOne))
                            .build()
            ))
            .build();
    Assertions.assertThrows(NoSuchCardClass.class, () -> talon.modifyActiveSeries(null, List.of(new Talon.SeriesSelection("FREE", List.of("one")))));
  }
}
