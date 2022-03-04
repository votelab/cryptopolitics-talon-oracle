package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassFinitudeMismatch;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.TransformationTest;
import io.inblocks.civicpower.cryptopolitics.models.cards.Card;
import io.inblocks.civicpower.cryptopolitics.models.cards.CardClass;
import io.inblocks.civicpower.cryptopolitics.models.cards.CardSerie;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

@MicronautTest
class ExtendTalonTransformationTest extends TransformationTest {

  @Test
  void addNewClass() {
    Talon talon = getInitialTalon();
    final CardClass newClass =
        CardClass.builder()
            .cardClass("NEW")
            .isInfinite(false)
            .series(Collections.singletonList(new CardSerie("clean", 4)))
            .build();
    ExtendTalonTransformation transformation =
        new ExtendTalonTransformation(
            Talon.builder().classes(Collections.singletonList(newClass)).build());
    Talon newTalon = transformation.apply(makeSomeContext(), talon);
    Assertions.assertEquals(2, newTalon.classes.size());
    Assertions.assertEquals(
        talon.getCardClassByName("COMMON"), newTalon.getCardClassByName("COMMON"));
    Assertions.assertEquals(newClass, newTalon.getCardClassByName("NEW"));
    Assertions.assertNull(transformation.getResults());
  }

  @Test
  void addNewSerieInExistingClass() {
    Talon talon = getInitialTalon();
    final CardSerie newSerie = new CardSerie("clean", 4);
    final CardClass newClass =
        CardClass.builder()
            .cardClass("COMMON")
            .isInfinite(false)
            .series(Collections.singletonList(newSerie))
            .build();
    ExtendTalonTransformation transformation =
        new ExtendTalonTransformation(
            Talon.builder().classes(Collections.singletonList(newClass)).build());
    Talon newTalon = transformation.apply(makeSomeContext(), talon);
    Assertions.assertEquals(1, newTalon.classes.size());
    CardClass cardClass = newTalon.getCardClassByName("COMMON");
    Assertions.assertEquals(3, cardClass.series.size());
    Assertions.assertEquals(
        talon.getCardClassByName("COMMON").getCardSerieByName("first"),
        cardClass.getCardSerieByName("first"));
    Assertions.assertEquals(
        talon.getCardClassByName("COMMON").getCardSerieByName("second"),
        cardClass.getCardSerieByName("second"));
    Assertions.assertEquals(newSerie, cardClass.getCardSerieByName("clean"));
    Assertions.assertNull(transformation.getResults());
  }

  @Test
  void extendExistingSerie() {
    Talon talon = getInitialTalon();
    final CardClass classExtension =
        CardClass.builder()
            .cardClass("COMMON")
            .isInfinite(false)
            .series(Collections.singletonList(new CardSerie("second", 4)))
            .build();
    ExtendTalonTransformation transformation =
        new ExtendTalonTransformation(
            Talon.builder().classes(Collections.singletonList(classExtension)).build());
    Talon newTalon = transformation.apply(makeSomeContext(), talon);
    Assertions.assertEquals(1, newTalon.classes.size());
    CardClass cardClass = newTalon.getCardClassByName("COMMON");
    Assertions.assertEquals(2, cardClass.series.size());
    Assertions.assertEquals(
        talon.getCardClassByName("COMMON").getCardSerieByName("first"),
        cardClass.getCardSerieByName("first"));
    final CardSerie secondSerie = cardClass.getCardSerieByName("second");
    Assertions.assertEquals(7, secondSerie.size);
    Assertions.assertEquals(7, secondSerie.count());
    Assertions.assertEquals(0, secondSerie.initialDealIndex);
    Assertions.assertNull(transformation.getResults());
  }

  @Test
  void extendSerieInInitialDeal() {
    Talon talon =
        Talon.builder()
            .classes(
                Collections.singletonList(
                    CardClass.builder()
                        .cardClass("COMMON")
                        .isInfinite(false)
                        .series(
                            Collections.singletonList(
                                new CardSerie("first", 2).pickCard().remainingCards))
                        .build()))
            .build();
    final CardClass classExtension =
        CardClass.builder()
            .cardClass("COMMON")
            .isInfinite(false)
            .series(Collections.singletonList(new CardSerie("first", 3)))
            .build();
    ExtendTalonTransformation transformation =
        new ExtendTalonTransformation(
            Talon.builder().classes(Collections.singletonList(classExtension)).build());
    Talon newTalon = transformation.apply(makeSomeContext(), talon);
    CardSerie newSerie = newTalon.getCardClassByName("COMMON").getCardSerieByName("first");
    Assertions.assertEquals(5, newSerie.size);
    Assertions.assertEquals(4, newSerie.count());
    // Next one out is the card remaining from unextended serie
    SelectionResult pick1 = newTalon.pickCardByClass("COMMON", 42);
    Assertions.assertEquals(List.of(new Card("COMMON", "first", 2)), pick1.cards);
    // Then the first extension card
    SelectionResult pick2 = pick1.remainingCards.pickCardByClass("COMMON", 42);
    Assertions.assertEquals(List.of(new Card("COMMON", "first", 3)), pick2.cards);
  }

  @Test
  void extendSerieAfterInitialDeal() {
    final CardSerie oldSerie = new CardSerie("first", 2)
            .pickCard().remainingCards
            .pickCard().remainingCards // now depleted
            .addCard(1);
    Talon talon =
            Talon.builder()
                    .classes(
                            Collections.singletonList(
                                    CardClass.builder()
                                            .cardClass("COMMON")
                                            .isInfinite(false)
                                            .series(
                                                    Collections.singletonList(oldSerie))
                                            .build()))
                    .build();
    final CardClass classExtension =
            CardClass.builder()
                    .cardClass("COMMON")
                    .isInfinite(false)
                    .series(Collections.singletonList(new CardSerie("first", 2)))
                    .build();
    ExtendTalonTransformation transformation =
            new ExtendTalonTransformation(
                    Talon.builder().classes(Collections.singletonList(classExtension)).build());
    Talon newTalon = transformation.apply(makeSomeContext(), talon);
    CardSerie newSerie = newTalon.getCardClassByName("COMMON").getCardSerieByName("first");
    Assertions.assertEquals(4, newSerie.size);
    Assertions.assertEquals(3, newSerie.count());
    // Next one out is first extension card
    SelectionResult pick1 = newTalon.pickCardByClass("COMMON", 42);
    Assertions.assertEquals(List.of(new Card("COMMON", "first", 3)), pick1.cards);
    // Then second extension card
    SelectionResult pick2 = pick1.remainingCards.pickCardByClass("COMMON", 42);
    Assertions.assertEquals(List.of(new Card("COMMON", "first", 4)), pick2.cards);
    // Then the remaining card
    SelectionResult pick3 = pick2.remainingCards.pickCardByClass("COMMON", 42);
    Assertions.assertEquals(List.of(new Card("COMMON", "first", 1)), pick3.cards);
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
    Assertions.assertThrows(CardClassFinitudeMismatch.class, () ->  new ExtendTalonTransformation(setup));
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
    Assertions.assertThrows(CardClassFinitudeMismatch.class, () ->  new ExtendTalonTransformation(setup));
  }

  @Test
  void extentTalonPreservesFinitude() {
    Talon firstTalon = Talon.builder().classes(List.of(CardClass.builder()
                    .cardClass("COMMON")
                        .isInfinite(false)
                    .series(List.of(new CardSerie("INITIAL_FINITE", 3))).build(),
            CardClass.builder()
                    .cardClass("FREE")
                    .isInfinite(true)
                    .series(List.of(new CardSerie("INITIAL_INFINITE", null)))
            .build())).build();
    Talon extraTalon = Talon.builder()
            .classes(List.of(CardClass.builder()
                            .cardClass("COMMON")
                            .isInfinite(false)
                            .series(List.of(new CardSerie("NEW_FINITE", 4)))
                    .build(), CardClass.builder()
                            .cardClass("FREE")
                            .isInfinite(true)
                            .series(List.of(new CardSerie("NEW_INFINITE", null)))
                    .build()))
            .build();
    ExtendTalonTransformation transformation =
            new ExtendTalonTransformation(extraTalon);
    Talon newTalon = transformation.apply(makeSomeContext(), firstTalon);
    CardClass commonClass = newTalon.getCardClassByName("COMMON");
    Assertions.assertEquals(2, commonClass.getSeries().size());
    Assertions.assertFalse(commonClass.getCardSerieByName("INITIAL_FINITE").isInfinite());
    Assertions.assertFalse(commonClass.getCardSerieByName("NEW_FINITE").isInfinite());
    CardClass freeClass = newTalon.getCardClassByName("FREE");
    Assertions.assertEquals(2, freeClass.getSeries().size());
    Assertions.assertTrue(freeClass.getCardSerieByName("INITIAL_INFINITE").isInfinite());
    Assertions.assertTrue(freeClass.getCardSerieByName("NEW_INFINITE").isInfinite());
  }

  @Test
  void canExtendFiniteClassWithInfiniteClass() {
    Talon firstTalon = Talon.builder().classes(List.of(CardClass.builder()
                    .cardClass("COMMON")
                    .isInfinite(false)
                    .series(List.of(new CardSerie("INITIAL_FINITE", 3))).build())).build();
    Talon extraTalon = Talon.builder()
            .classes(List.of(CardClass.builder()
                    .cardClass("COMMON")
                    .isInfinite(true)
                    .series(List.of(new CardSerie("NEW_INFINITE", null)))
                    .build()))
            .build();
    ExtendTalonTransformation transformation =
            new ExtendTalonTransformation(extraTalon);
    Assertions.assertThrows(CardClassFinitudeMismatch.class, () -> transformation.apply(makeSomeContext(), firstTalon));
  }

  @Test
  void canExtendInfiniteClassWithfiniteClass() {
    Talon firstTalon = Talon.builder().classes(List.of(CardClass.builder()
            .cardClass("COMMON")
            .isInfinite(true)
            .series(List.of(new CardSerie("INITIAL_INFINITE", null))).build())).build();
    Talon extraTalon = Talon.builder()
            .classes(List.of(CardClass.builder()
                    .cardClass("COMMON")
                    .isInfinite(false)
                    .series(List.of(new CardSerie("NEW_FINITE", 3)))
                    .build()))
            .build();
    ExtendTalonTransformation transformation =
            new ExtendTalonTransformation(extraTalon);
    Assertions.assertThrows(CardClassFinitudeMismatch.class, () -> transformation.apply(makeSomeContext(), firstTalon));
  }

  private Talon getInitialTalon() {
    return Talon.builder()
        .classes(
            Collections.singletonList(
                CardClass.builder()
                    .cardClass("COMMON")
                    .isInfinite(false)
                    .series(
                        List.of(
                            new CardSerie("first", 2), new CardSerie("second", 3)))
                    .build()))
        .build();
  }
}
