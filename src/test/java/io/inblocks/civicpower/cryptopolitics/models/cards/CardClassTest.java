package io.inblocks.civicpower.cryptopolitics.models.cards;

import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassEmpty;
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
    void cantDealFromDeprecatedSerie() {
        final CardClass cardClass = CardClass.builder().cardClass(COMMON_CLASS).isInfinite(false).series(Collections.emptyList())
                .deprecatedSeries(Collections.singletonList(new CardSerie("test", 2))).build();
        long seed = r.nextLong();
        Assertions.assertThrows(CardClassEmpty.class, () -> cardClass.pickCard(seed));
    }

    @Test
    void dealFromSingleSerie() {
        final CardClass cardClassOfOneSerie = CardClass.builder().cardClass(COMMON_CLASS).isInfinite(false).series(Collections.singletonList(
                new CardSerie("test", 2))).build();
        final CardClass.PickCardResult result1 = cardClassOfOneSerie.pickCard(r.nextLong());
        Assertions.assertEquals(new Card(COMMON_CLASS, "test", 1), result1.card);
        final CardClass.PickCardResult result2 = result1.remainingCards.pickCard(r.nextLong());
        Assertions.assertEquals(new Card(COMMON_CLASS, "test", 2), result2.card);
        Assertions.assertThrows(CardClassEmpty.class, () -> result2.remainingCards.pickCard(r.nextLong()));
    }

    @Test
    void dealFromSerieAndDeprecatedSerie() {
        final CardClass cardClassOfOneSerie = CardClass.builder().cardClass(COMMON_CLASS).isInfinite(false)
                .series(List.of(new CardSerie("test", 2)))
                .deprecatedSeries(List.of(new CardSerie("black", 2))).build();
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
            .series(List.of(new CardSerie("orange", 1), new CardSerie("blue", 1)))
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
                .series(List.of(new CardSerie("blue", 2).removeCard(1), new CardSerie("orange", 3)))
                .build();
        CardClass restoredCardClass = cardClass.addCard(new Card(COMMON_CLASS, "blue", 1));
        Assertions.assertEquals(2, restoredCardClass.getCardSerieByName("blue").count());
        Assertions.assertEquals(3, restoredCardClass.getCardSerieByName("orange").count());
    }

    @Test
    void addCardToDeprecatedSerie() {
        CardClass cardClass = CardClass.builder()
                .cardClass(COMMON_CLASS)
                .isInfinite(false)
                .series(List.of(new CardSerie("orange", 3)))
                .deprecatedSeries(List.of(new CardSerie("blue", 2).removeCard(1)))
                .build();
        CardClass restoredCardClass = cardClass.addCard(new Card(COMMON_CLASS, "blue", 1));
        Assertions.assertEquals(2, restoredCardClass.getDeprecatedCardSerieByName("blue").count());
        Assertions.assertEquals(3, restoredCardClass.getCardSerieByName("orange").count());
    }

    @Test
    void dealFromSingleInfiniteSerie() {
        final CardClass infiniteCardClassOfOneSerie = CardClass.builder().cardClass(COMMON_CLASS).isInfinite(true).series(Collections.singletonList(
                new CardSerie("test", null))).build();
        final CardClass.PickCardResult result1 = infiniteCardClassOfOneSerie.pickCard(r.nextLong());
        Assertions.assertEquals(new Card(COMMON_CLASS, "test", 1), result1.card);
        final CardClass.PickCardResult result2 = result1.remainingCards.pickCard(r.nextLong());
        Assertions.assertEquals(new Card(COMMON_CLASS, "test", 2), result2.card);
    }

    @Test
    void noInfiniteCount() {
        final CardClass infiniteCardClassOfOneSerie = CardClass.builder().cardClass(COMMON_CLASS).isInfinite(true).series(Collections.singletonList(
                new CardSerie("test", null))).build();
        Assertions.assertNull(infiniteCardClassOfOneSerie.count());
    }

    @Test
    void deprecateSeries() {
        final CardSerie serieOne = new CardSerie("one", 1);
        final CardSerie serieTwo = new CardSerie("two", 1);
        final CardSerie serieThree = new CardSerie("three", 1);
        final CardSerie serieFour = new CardSerie("four", 1);
        final CardClass cardClass = CardClass.builder()
                .cardClass(COMMON_CLASS)
                .isInfinite(false)
                .series(List.of(serieOne, serieTwo, serieThree))
                .deprecatedSeries(List.of(serieFour))
                .build();
        final CardClass newCardClass = cardClass.deprecateSeriesByName(List.of("one", "three"));
        Assertions.assertEquals(List.of(serieTwo), newCardClass.series);
        Assertions.assertEquals(List.of(serieFour, serieOne, serieThree), newCardClass.deprecatedSeries);
    }
}
