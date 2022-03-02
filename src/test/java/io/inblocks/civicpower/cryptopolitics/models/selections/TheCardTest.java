package io.inblocks.civicpower.cryptopolitics.models.selections;

import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCard;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardClass;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardSerie;
import io.inblocks.civicpower.cryptopolitics.models.Selection;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

@MicronautTest
class TheCardTest extends SelectionTests {

    private final Random r = new Random();

    @Test
    void getOneCardFromSerie() {
        Talon talon = get52CardsTalon();
        Selection selection = new TheCard("Red", "hearts", 10);
        SelectionResult result = selection.pickCards(talon, r);
        Assertions.assertEquals(1, result.cards.size());
        Assertions.assertEquals("Red", result.cards.get(0).originalClass);
        Assertions.assertEquals("hearts", result.cards.get(0).serieName);
        Assertions.assertEquals(10, result.cards.get(0).orderNumber);
    }

    @Test
    void getOneCardFromUnknownClass() {
        Talon talon = get52CardsTalon();
        Selection selection = new TheCard("Yellow", "hearts", 10);
        Assertions.assertThrows(NoSuchCardClass.class, () -> selection.pickCards(talon, r));
    }

    @Test
    void getOneCardFromUnknownSerie() {
        Talon talon = get52CardsTalon();
        Selection selection = new TheCard("Red", "baloons", 10);
        Assertions.assertThrows(NoSuchCardSerie.class, () -> selection.pickCards(talon, r));
    }

    @Test
    void getOneCardWithBadOrderNumber() {
        Talon talon = get52CardsTalon();
        Selection selection = new TheCard("Red", "hearts", 14);
        Assertions.assertThrows(IllegalArgumentException.class, () -> selection.pickCards(talon, r));
    }

    @Test
    void getOneCardTwice() {
        Talon talon = get52CardsTalon();
        Selection selection = new TheCard("Red", "hearts", 10);
        SelectionResult result1 = selection.pickCards(talon, r);
        Assertions.assertThrows(NoSuchCard.class, () -> selection.pickCards(result1.remainingCards, r));
    }
}
