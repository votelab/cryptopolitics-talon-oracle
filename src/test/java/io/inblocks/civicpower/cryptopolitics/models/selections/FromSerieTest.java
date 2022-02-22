package io.inblocks.civicpower.cryptopolitics.models.selections;

import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardClass;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardSerie;
import io.inblocks.civicpower.cryptopolitics.models.Selection;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.Talon;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

@MicronautTest
class FromSerieTest extends SelectionTests {

    private final Random r = new Random();

    @Test
    void getOneCardFromSerie() {
        Talon talon = get52CardsTalon();
        Selection selection = new FromSerie("Red", "hearts");
        SelectionResult result = selection.pickCards(talon, r);
        Assertions.assertEquals(1, result.cards.size());
        Assertions.assertEquals("Red", result.cards.get(0).originalClass);
        Assertions.assertEquals("hearts", result.cards.get(0).serieName);
    }

    @Test
    void getOneCardFromUnknownClass() {
        Talon talon = get52CardsTalon();
        Selection selection = new FromSerie("Yellow", "hearts");
        Assertions.assertThrows(NoSuchCardClass.class, () -> selection.pickCards(talon, r));
    }

    @Test
    void getOneCardFromUnknownSerie() {
        Talon talon = get52CardsTalon();
        Selection selection = new FromSerie("Red", "baloons");
        Assertions.assertThrows(NoSuchCardSerie.class, () -> selection.pickCards(talon, r));
    }
}
