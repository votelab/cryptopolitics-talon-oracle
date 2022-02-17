package io.inblocks.civicpower.cryptopolitics.models;

import io.inblocks.civicpower.cryptopolitics.exceptions.CardAlreadyPresent;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCard;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

@MicronautTest
class CardSerieTest {

    @Test
    void createSerieOfOneCard() {
        final CardSerie serie = new CardSerie("test", 1);
        Assertions.assertEquals(BigInteger.ONE, serie.setBitmap);
    }

    @Test
    void createSerieOfTwoCards() {
        final CardSerie serie = new CardSerie("test", 2);
        Assertions.assertEquals(BigInteger.valueOf(3), serie.setBitmap);
    }

    @Test
    void removeACard() {
        final CardSerie serie = new CardSerie("test", 3);
        final CardSerie modifiedSerie = serie.removeCard(2);
        Assertions.assertEquals(BigInteger.valueOf(5), modifiedSerie.setBitmap);
    }

    @Test
    void cantRemoveAMissingCard() {
        final CardSerie serie = new CardSerie("test", 3).removeCard(2);
        Assertions.assertThrows(NoSuchCard.class, () -> serie.removeCard(2));
    }

    @Test
    void addACard() {
        final CardSerie serie = new CardSerie("test", 3).removeCard(2);
        CardSerie modifiedSerie = serie.addCard(2);
        Assertions.assertEquals(BigInteger.valueOf(7), modifiedSerie.setBitmap);
    }

    @Test
    void cantAddAnAlreadyPresentCard() {
        final CardSerie serie = new CardSerie("test", 3);
        Assertions.assertThrows(CardAlreadyPresent.class, () -> serie.addCard(2));
    }

    @Test
    void cantDealOffAnEmptySerie() {
        CardSerie serie = new CardSerie("test", 1).pickNextCard().remainingCards;
        Assertions.assertThrows(IllegalArgumentException.class, serie::pickNextCard);
    }

    @Test
    void firstDealCardsInIncreasingOrderNumbers() {
        final CardSerie serie = new CardSerie("test", 2);
        final CardSerie.PickNextCardResult result1 = serie.pickNextCard();
        Assertions.assertEquals(1, result1.cardOrderNumber);
        final CardSerie result1p = result1.remainingCards.addCard(1); // Even if we put back card #1
        final CardSerie.PickNextCardResult result2 = result1p.pickNextCard();
        Assertions.assertEquals(2, result2.cardOrderNumber);
    }

    @Test
    void laterDealDiscardedCardsOfHighestOrderNumbers() {
        final CardSerie exhaustedSerie = new CardSerie("test", 2).pickNextCard().remainingCards.pickNextCard().remainingCards;
        final CardSerie serie = exhaustedSerie.addCard(1).addCard(2);
        final CardSerie.PickNextCardResult result1 = serie.pickNextCard();
        Assertions.assertEquals(2, result1.cardOrderNumber);
        final CardSerie.PickNextCardResult result2 = result1.remainingCards.pickNextCard();
        Assertions.assertEquals(1, result2.cardOrderNumber);
    }

    @Test
    void neverReturnIntoInitialDealState() {
        final CardSerie exhaustedSerie = new CardSerie("test", 3)
                .pickNextCard().remainingCards
                .pickNextCard().remainingCards
                .pickNextCard().remainingCards;
        final CardSerie serie = exhaustedSerie.addCard(1);
        final CardSerie dealFirst = serie.pickNextCard().remainingCards;
        final CardSerie addBackNextCardSerie = dealFirst.addCard(2).addCard(3);
        Assertions.assertEquals(3, addBackNextCardSerie.pickNextCard().cardOrderNumber);
    }

    @Test
    void createInfiniteSerie() {
        final CardSerie finiteSerie = new CardSerie("test", 3);
        Assertions.assertFalse(finiteSerie.isInfinite());
        final CardSerie infiniteSerie = new CardSerie("test", null);
        Assertions.assertTrue(infiniteSerie.isInfinite());
    }

    @Test
    void removeCardsFrominfiniteSerie() {
        final CardSerie infiniteSerie = new CardSerie("test", null);
        CardSerie.PickNextCardResult pickNextCardResult1 = infiniteSerie.pickNextCard();
        Assertions.assertEquals(1, pickNextCardResult1.cardOrderNumber);
        CardSerie.PickNextCardResult pickNextCardResult2 = pickNextCardResult1.remainingCards.pickNextCard();
        Assertions.assertEquals(2, pickNextCardResult2.cardOrderNumber);
        Assertions.assertEquals(2, pickNextCardResult2.remainingCards.initialDealIndex);
    }

    @Test
    void addACardToInfiniteSerie() {
        final CardSerie.PickNextCardResult pickNextCardResult1 = new CardSerie("test", null).pickNextCard();
        CardSerie revertedSerie = pickNextCardResult1.remainingCards.addCard(pickNextCardResult1.cardOrderNumber);
        Assertions.assertEquals(1, revertedSerie.unminted);
        final CardSerie.PickNextCardResult pickNextCardResult2 = revertedSerie.pickNextCard();
        Assertions.assertEquals(2, pickNextCardResult2.cardOrderNumber);
    }
}
