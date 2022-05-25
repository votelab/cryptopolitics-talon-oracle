package io.inblocks.civicpower.cryptopolitics.models.cards;

import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassEmpty;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardSerie;
import io.inblocks.civicpower.cryptopolitics.models.SerieRetirement;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

@MicronautTest
class CardClassTest {

    private final String COMMON_CLASS = "Common";

    private final Random r = new Random();

    @Test
    void cantDealFromNoSeries() {
        final CardClass cardClass = CardClass.builder().cardClass(COMMON_CLASS).isInfinite(false).series(Collections.emptyList()).build();
        long seed = r.nextLong();
        Assertions.assertThrows(CardClassEmpty.class, () -> cardClass.pickCard(seed));
    }

    @Test
    void cantDealFromRetiredSerie() {
        final CardClass cardClass = CardClass.builder()
                .cardClass(COMMON_CLASS)
                .isInfinite(false)
                .series(Collections.singletonList(new CardSerie("test", 2, true))).build();
        long seed = r.nextLong();
        Assertions.assertThrows(CardClassEmpty.class, () -> cardClass.pickCard(seed));
    }

    @Test
    void dealFromSingleSerie() {
        final CardClass cardClassOfOneSerie = CardClass.builder().cardClass(COMMON_CLASS).isInfinite(false).series(Collections.singletonList(
                new CardSerie("test", 2, false))).build();
        final CardClass.PickCardResult result1 = cardClassOfOneSerie.pickCard(r.nextLong());
        Assertions.assertEquals(new Card(COMMON_CLASS, "test", 1), result1.card);
        final CardClass.PickCardResult result2 = result1.remainingCards.pickCard(r.nextLong());
        Assertions.assertEquals(new Card(COMMON_CLASS, "test", 2), result2.card);
        Assertions.assertThrows(CardClassEmpty.class, () -> result2.remainingCards.pickCard(r.nextLong()));
    }

    @Test
    void dealFromSerieAndRetiredSerie() {
        final CardClass cardClassOfOneSerie = CardClass.builder().cardClass(COMMON_CLASS).isInfinite(false)
                .series(List.of(new CardSerie("test", 2, false), new CardSerie("black", 2, true))).build();
        final CardClass.PickCardResult result1 = cardClassOfOneSerie.pickCard(r.nextLong());
        Assertions.assertEquals(new Card(COMMON_CLASS, "test", 1), result1.card);
        final CardClass.PickCardResult result2 = result1.remainingCards.pickCard(r.nextLong());
        Assertions.assertEquals(new Card(COMMON_CLASS, "test", 2), result2.card);
        Assertions.assertThrows(CardClassEmpty.class, () -> result2.remainingCards.pickCard(r.nextLong()));
    }

    @Test
    void dealFromTwoSeries() {
    final CardClass cardClassOfTwoSeries =
        CardClass.builder()
            .cardClass(COMMON_CLASS)
            .isInfinite(false)
            .series(List.of(new CardSerie("orange", 1, false), new CardSerie("blue", 1, false)))
            .build();
        final CardClass.PickCardResult result1 = cardClassOfTwoSeries.pickCard(r.nextLong());
        final CardClass.PickCardResult result2 = result1.remainingCards.pickCard(r.nextLong());
        Assertions.assertThrows(CardClassEmpty.class, () -> result2.remainingCards.pickCard(r.nextLong()));
        // We don't know in which order the two cards have been picked
        Assertions.assertEquals(new HashSet<>(List.of(new Card(COMMON_CLASS, "orange", 1),
                new Card(COMMON_CLASS, "blue", 1)
        )), new HashSet<>(List.of(result1.card, result2.card)));
    }

    @Test
    void addCard() {
        CardClass cardClass = CardClass.builder()
                .cardClass(COMMON_CLASS)
                .isInfinite(false)
                .series(List.of(new CardSerie("blue", 2, false).removeCard(1), new CardSerie("orange", 3, false)))
                .build();
        CardClass restoredCardClass = cardClass.addCard(new Card(COMMON_CLASS, "blue", 1));
        Assertions.assertEquals(2, restoredCardClass.getCardSerieByName("blue").count());
        Assertions.assertEquals(3, restoredCardClass.getCardSerieByName("orange").count());
    }

    @Test
    void addCardToRetiredSerie() {
        CardClass cardClass = CardClass.builder()
                .cardClass(COMMON_CLASS)
                .isInfinite(false)
                .series(List.of(new CardSerie("orange", 3, false), new CardSerie("blue", 2, true).removeCard(1)))
                .build();
        CardClass restoredCardClass = cardClass.addCard(new Card(COMMON_CLASS, "blue", 1));
        Assertions.assertEquals(2, restoredCardClass.getCardSerieByName("blue").count());
        Assertions.assertEquals(3, restoredCardClass.getCardSerieByName("orange").count());
    }

    @Test
    void dealFromSingleInfiniteSerie() {
        final CardClass infiniteCardClassOfOneSerie = CardClass.builder().cardClass(COMMON_CLASS).isInfinite(true).series(Collections.singletonList(
                new CardSerie("test", null, false))).build();
        final CardClass.PickCardResult result1 = infiniteCardClassOfOneSerie.pickCard(r.nextLong());
        Assertions.assertEquals(new Card(COMMON_CLASS, "test", 1), result1.card);
        final CardClass.PickCardResult result2 = result1.remainingCards.pickCard(r.nextLong());
        Assertions.assertEquals(new Card(COMMON_CLASS, "test", 2), result2.card);
    }

    @Test
    void noInfiniteCount() {
        final CardClass infiniteCardClassOfOneSerie = CardClass.builder().cardClass(COMMON_CLASS).isInfinite(true).series(Collections.singletonList(
                new CardSerie("test", null, false))).build();
        Assertions.assertNull(infiniteCardClassOfOneSerie.count());
    }

    @Test
    void retireSeries() {
        final CardSerie serieOne = new CardSerie("one", 1, false);
        final CardSerie serieTwo = new CardSerie("two", 1, false);
        final CardSerie serieThree = new CardSerie("three", 1, false);
        final CardSerie serieFour = new CardSerie("four", 1, true);
        final CardClass cardClass = CardClass.builder()
                .cardClass(COMMON_CLASS)
                .isInfinite(false)
                .series(List.of(serieOne, serieTwo, serieThree, serieFour))
                .build();
        final CardClass newCardClass = cardClass.retireSeries(List.of(new SerieRetirement("one", true), new SerieRetirement("three", true)));
        Assertions.assertTrue(newCardClass.getCardSerieByName("one").isRetired);
        Assertions.assertFalse(newCardClass.getCardSerieByName("two").isRetired);
        Assertions.assertTrue(newCardClass.getCardSerieByName("three").isRetired);
        Assertions.assertTrue(newCardClass.getCardSerieByName("four").isRetired);
    }

    @Test
    void retireUnexistingSerie() {
        final CardSerie serieOne = new CardSerie("one", 1, false);
        final CardClass cardClass = CardClass.builder()
                .cardClass(COMMON_CLASS)
                .isInfinite(false)
                .series(List.of(serieOne))
                .build();
        Assertions.assertThrows(NoSuchCardSerie.class, () -> cardClass.retireSeries(List.of(new SerieRetirement("two", false))));
    }
}
