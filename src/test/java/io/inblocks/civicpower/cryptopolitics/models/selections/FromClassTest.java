package io.inblocks.civicpower.cryptopolitics.models.selections;

import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardClass;
import io.inblocks.civicpower.cryptopolitics.models.Selection;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

@MicronautTest
class FromClassTest extends SelectionTests {

    private final Random r = new Random();

    @Test
    void getOneCardFromClass() {
        Talon talon = get52CardsTalon();
        Selection selection = new FromClass("Red");
        SelectionResult result = selection.pickCards(talon, r);
        Assertions.assertEquals(1, result.cards.size());
        Assertions.assertEquals("Red", result.cards.get(0).originalClass);
    }

    @Test
    void getOneCardFromUnknownClass() {
        Talon talon = get52CardsTalon();
        Selection selection = new FromClass("Yellow");
        Assertions.assertThrows(NoSuchCardClass.class, () -> selection.pickCards(talon, r));
    }

}
