package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassFinitudeMismatch;
import io.inblocks.civicpower.cryptopolitics.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class ExtendTalonTransformationTest extends TransformationTest {

  @Test
  void addNewClass() {
    Talon talon = getInitialTalon();
    final CardClass newClass =
        CardClass.builder()
            .cardClass("NEW")
            .series(Collections.singletonList(new CardSerie("clean", 4)))
            .build();
    ExtendTalonTransformation transformation =
        new ExtendTalonTransformation(
            Talon.builder().classes(Collections.singletonList(newClass)).build());
    Talon newTalon = transformation.apply(makeSomeContext(), talon);
    Assertions.assertEquals(2, newTalon.classes.size());
    Assertions.assertEquals(
        talon.getCardClassDataByClass("COMMON"), newTalon.getCardClassDataByClass("COMMON"));
    Assertions.assertEquals(newClass, newTalon.getCardClassDataByClass("NEW"));
    Assertions.assertNull(transformation.getResults());
  }

  @Test
  void addNewSerieInExistingClass() {
    Talon talon = getInitialTalon();
    final CardSerie newSerie = new CardSerie("clean", 4);
    final CardClass newClass =
        CardClass.builder()
            .cardClass("COMMON")
            .series(Collections.singletonList(newSerie))
            .build();
    ExtendTalonTransformation transformation =
        new ExtendTalonTransformation(
            Talon.builder().classes(Collections.singletonList(newClass)).build());
    Talon newTalon = transformation.apply(makeSomeContext(), talon);
    Assertions.assertEquals(1, newTalon.classes.size());
    CardClass cardClass = newTalon.getCardClassDataByClass("COMMON");
    Assertions.assertEquals(3, cardClass.series.size());
    Assertions.assertEquals(
        talon.getCardClassDataByClass("COMMON").getSerieByName("first"),
        cardClass.getSerieByName("first"));
    Assertions.assertEquals(
        talon.getCardClassDataByClass("COMMON").getSerieByName("second"),
        cardClass.getSerieByName("second"));
    Assertions.assertEquals(newSerie, cardClass.getSerieByName("clean"));
    Assertions.assertNull(transformation.getResults());
  }

  @Test
  void extendExistingSerie() {
    Talon talon = getInitialTalon();
    final CardClass classExtension =
        CardClass.builder()
            .cardClass("COMMON")
            .series(Collections.singletonList(new CardSerie("second", 4)))
            .build();
    ExtendTalonTransformation transformation =
        new ExtendTalonTransformation(
            Talon.builder().classes(Collections.singletonList(classExtension)).build());
    Talon newTalon = transformation.apply(makeSomeContext(), talon);
    Assertions.assertEquals(1, newTalon.classes.size());
    CardClass cardClass = newTalon.getCardClassDataByClass("COMMON");
    Assertions.assertEquals(2, cardClass.series.size());
    Assertions.assertEquals(
        talon.getCardClassDataByClass("COMMON").getSerieByName("first"),
        cardClass.getSerieByName("first"));
    final CardSerie secondSerie = cardClass.getSerieByName("second");
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
                        .series(
                            Collections.singletonList(
                                new CardSerie("first", 2).pickNextCard().remainingCards))
                        .build()))
            .build();
    final CardClass classExtension =
        CardClass.builder()
            .cardClass("COMMON")
            .series(Collections.singletonList(new CardSerie("first", 3)))
            .build();
    ExtendTalonTransformation transformation =
        new ExtendTalonTransformation(
            Talon.builder().classes(Collections.singletonList(classExtension)).build());
    Talon newTalon = transformation.apply(makeSomeContext(), talon);
    CardSerie newSerie = newTalon.getCardClassDataByClass("COMMON").getSerieByName("first");
    Assertions.assertEquals(5, newSerie.size);
    Assertions.assertEquals(4, newSerie.count());
    // Next one out is the card remaining from unextended serie
    PickNextCardResult pick1 = newTalon.pickNextCard("COMMON", 42);
    Assertions.assertEquals(new Card("COMMON", "first", 2), pick1.card);
    // Then the first extension card
    PickNextCardResult pick2 = pick1.remainingCards.pickNextCard("COMMON", 42);
    Assertions.assertEquals(new Card("COMMON", "first", 3), pick2.card);
  }

  @Test
  void extendSerieAfterInitialDeal() {
    final CardSerie oldSerie = new CardSerie("first", 2)
            .pickNextCard().remainingCards
            .pickNextCard().remainingCards // now depleted
            .addCard(1);
    Talon talon =
            Talon.builder()
                    .classes(
                            Collections.singletonList(
                                    CardClass.builder()
                                            .cardClass("COMMON")
                                            .series(
                                                    Collections.singletonList(oldSerie))
                                            .build()))
                    .build();
    final CardClass classExtension =
            CardClass.builder()
                    .cardClass("COMMON")
                    .series(Collections.singletonList(new CardSerie("first", 2)))
                    .build();
    ExtendTalonTransformation transformation =
            new ExtendTalonTransformation(
                    Talon.builder().classes(Collections.singletonList(classExtension)).build());
    Talon newTalon = transformation.apply(makeSomeContext(), talon);
    CardSerie newSerie = newTalon.getCardClassDataByClass("COMMON").getSerieByName("first");
    Assertions.assertEquals(4, newSerie.size);
    Assertions.assertEquals(3, newSerie.count());
    // Next one out is first extension card
    PickNextCardResult pick1 = newTalon.pickNextCard("COMMON", 42);
    Assertions.assertEquals(new Card("COMMON", "first", 3), pick1.card);
    // Then second extension card
    PickNextCardResult pick2 = pick1.remainingCards.pickNextCard("COMMON", 42);
    Assertions.assertEquals(new Card("COMMON", "first", 4), pick2.card);
    // Then the remaining card
    PickNextCardResult pick3 = pick2.remainingCards.pickNextCard("COMMON", 42);
    Assertions.assertEquals(new Card("COMMON", "first", 1), pick3.card);
  }

  @Test
  void cantUseInfiniteSerieInFiniteClass() {
    Talon setup = Talon.builder()
            .classes(List.of(CardClass.builder()
                    .cardClass("COMMON")
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
                    .series(List.of(new CardSerie("INITIAL_FINITE", 3))).build(),
            CardClass.builder()
                    .cardClass("FREE")
                    .isInfinite(true)
                    .series(List.of(new CardSerie("INITIAL_INFINITE", null)))
            .build())).build();
    Talon extraTalon = Talon.builder()
            .classes(List.of(CardClass.builder()
                            .cardClass("COMMON")
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
    CardClass commonClass = newTalon.getCardClassDataByClass("COMMON");
    Assertions.assertEquals(2, commonClass.getSeries().size());
    CardClass freeClass = newTalon.getCardClassDataByClass("FREE");
    Assertions.assertEquals(2, freeClass.getSeries().size());
  }

  @Test
  void canExtendFiniteClassWithInfiniteClass() {
    Talon firstTalon = Talon.builder().classes(List.of(CardClass.builder()
                    .cardClass("COMMON")
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
                    .series(
                        Arrays.asList(
                            new CardSerie("first", 2), new CardSerie("second", 3)))
                    .build()))
        .build();
  }
}
