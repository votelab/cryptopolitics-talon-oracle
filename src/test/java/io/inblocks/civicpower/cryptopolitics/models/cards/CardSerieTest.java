package io.inblocks.civicpower.cryptopolitics.models.cards;

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
        final CardSerie serie = new CardSerie("test", 1, false);
        Assertions.assertEquals(BigInteger.ONE, serie.setBitmap);
    }

    @Test
    void createSerieOfTwoCards() {
        final CardSerie serie = new CardSerie("test", 2, false);
        Assertions.assertEquals(BigInteger.valueOf(3), serie.setBitmap);
    }

    @Test
    void removeACard() {
        final CardSerie serie = new CardSerie("test", 3, false);
        final CardSerie modifiedSerie = serie.removeCard(2);
        Assertions.assertEquals(BigInteger.valueOf(5), modifiedSerie.setBitmap);
    }

    @Test
    void cantRemoveAMissingCard() {
        final CardSerie serie = new CardSerie("test", 3, false).removeCard(2);
        Assertions.assertThrows(NoSuchCard.class, () -> serie.removeCard(2));
    }

    @Test
    void addACard() {
        final CardSerie serie = new CardSerie("test", 3, false).removeCard(2);
        CardSerie modifiedSerie = serie.addCard(2);
        Assertions.assertEquals(BigInteger.valueOf(7), modifiedSerie.setBitmap);
    }

    @Test
    void cantAddAnAlreadyPresentCard() {
        final CardSerie serie = new CardSerie("test", 3, false);
        Assertions.assertThrows(CardAlreadyPresent.class, () -> serie.addCard(2));
    }

    @Test
    void cantDealOffAnEmptySerie() {
        CardSerie serie = new CardSerie("test", 1, false).pickCard().remainingCards;
        Assertions.assertThrows(IllegalArgumentException.class, serie::pickCard);
    }

    @Test
    void firstDealCardsInIncreasingOrderNumbers() {
        final CardSerie serie = new CardSerie("test", 2, false);
        final CardSerie.PickCardResult result1 = serie.pickCard();
        Assertions.assertEquals(1, result1.cardOrderNumber);
        final CardSerie result1p = result1.remainingCards.addCard(1); // Even if we put back card #1
        final CardSerie.PickCardResult result2 = result1p.pickCard();
        Assertions.assertEquals(2, result2.cardOrderNumber);
    }

    @Test
    void laterDealDiscardedCardsOfHighestOrderNumbers() {
        final CardSerie exhaustedSerie = new CardSerie("test", 2, false).pickCard().remainingCards.pickCard().remainingCards;
        final CardSerie serie = exhaustedSerie.addCard(1).addCard(2);
        final CardSerie.PickCardResult result1 = serie.pickCard();
        Assertions.assertEquals(2, result1.cardOrderNumber);
        final CardSerie.PickCardResult result2 = result1.remainingCards.pickCard();
        Assertions.assertEquals(1, result2.cardOrderNumber);
    }

    @Test
    void neverReturnIntoInitialDealState() {
        final CardSerie exhaustedSerie = new CardSerie("test", 3, false)
                .pickCard().remainingCards
                .pickCard().remainingCards
                .pickCard().remainingCards;
        final CardSerie serie = exhaustedSerie.addCard(1);
        final CardSerie dealFirst = serie.pickCard().remainingCards;
        final CardSerie addBackNextCardSerie = dealFirst.addCard(2).addCard(3);
        Assertions.assertEquals(3, addBackNextCardSerie.pickCard().cardOrderNumber);
    }

    @Test
    void createInfiniteSerie() {
        final CardSerie finiteSerie = new CardSerie("test", 3, false);
        Assertions.assertFalse(finiteSerie.isInfinite());
        final CardSerie infiniteSerie = new CardSerie("test", null, false);
        Assertions.assertTrue(infiniteSerie.isInfinite());
    }

    @Test
    void removeCardsFrominfiniteSerie() {
        final CardSerie infiniteSerie = new CardSerie("test", null, false);
        CardSerie.PickCardResult pickCardResult1 = infiniteSerie.pickCard();
        Assertions.assertEquals(1, pickCardResult1.cardOrderNumber);
        CardSerie.PickCardResult pickCardResult2 = pickCardResult1.remainingCards.pickCard();
        Assertions.assertEquals(2, pickCardResult2.cardOrderNumber);
        Assertions.assertEquals(2, pickCardResult2.remainingCards.initialDealIndex);
    }

    @Test
    void addACardToInfiniteSerie() {
        final CardSerie.PickCardResult pickCardResult1 = new CardSerie("test", null, false).pickCard();
        CardSerie revertedSerie = pickCardResult1.remainingCards.addCard(pickCardResult1.cardOrderNumber);
        Assertions.assertEquals(1, revertedSerie.unminted);
        final CardSerie.PickCardResult pickCardResult2 = revertedSerie.pickCard();
        Assertions.assertEquals(2, pickCardResult2.cardOrderNumber);
    }
}
