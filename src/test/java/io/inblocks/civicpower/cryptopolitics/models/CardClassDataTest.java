package io.inblocks.civicpower.cryptopolitics.models;

import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassEmpty;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

@MicronautTest
class CardClassDataTest {

    private final String COMMON_CLASS = "Common";

    private final Random r = new Random();

    @Test
    void cantDealFromNoSeries() {
        final CardClass cardClass = CardClass.builder().cardClass(COMMON_CLASS).series(Collections.emptyList()).build();
        long seed = r.nextLong();
        Assertions.assertThrows(CardClassEmpty.class, () -> cardClass.pickNextCard(seed));
    }

    @Test
    void dealFromSingleSerie() {
        final CardClass cardClassOfOneSerie = CardClass.builder().cardClass(COMMON_CLASS).series(Collections.singletonList(
                new CardSerie("test", 2))).build();
        final CardClass.PickNextCardResult result1 = cardClassOfOneSerie.pickNextCard(r.nextLong());
        Assertions.assertEquals(new Card(COMMON_CLASS, "test", 1), result1.card);
        final CardClass.PickNextCardResult result2 = result1.remainingCards.pickNextCard(r.nextLong());
        Assertions.assertEquals(new Card(COMMON_CLASS, "test", 2), result2.card);
    }

    @Test
    void dealFromTwoSeries() {
    final CardClass cardClassOfTwoSeries =
        CardClass.builder()
            .cardClass(COMMON_CLASS)
            .series(Arrays.asList(new CardSerie("orange", 1), new CardSerie("blue", 1)))
            .build();
        final CardClass.PickNextCardResult result1 = cardClassOfTwoSeries.pickNextCard(r.nextLong());
        final CardClass.PickNextCardResult result2 = result1.remainingCards.pickNextCard(r.nextLong());
        // We don't know in which order the two cards have been picked
        Assertions.assertEquals(new HashSet<>(Arrays.asList(new Card(COMMON_CLASS, "orange", 1),
                new Card(COMMON_CLASS, "blue", 1)
        )), new HashSet<>(Arrays.asList(result1.card, result2.card)));
    }

    @Test
    void addCard() {
        CardClass cardClass = CardClass.builder()
                .cardClass(COMMON_CLASS)
                .series(Arrays.asList(new CardSerie("blue", 2).removeCard(1), new CardSerie("orange", 3)))
                .build();
        CardClass restoredCardClass = cardClass.addCard(new Card(COMMON_CLASS, "blue", 1));
        Assertions.assertEquals(2, restoredCardClass.getSerieByName("blue").count());
        Assertions.assertEquals(3, restoredCardClass.getSerieByName("orange").count());
    }
}
