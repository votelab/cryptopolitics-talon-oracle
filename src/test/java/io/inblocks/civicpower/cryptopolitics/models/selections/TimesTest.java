package io.inblocks.civicpower.cryptopolitics.models.selections;

import io.inblocks.civicpower.cryptopolitics.models.Selection;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.Talon;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

@MicronautTest
class TimesTest extends SelectionTests {

    private final Random r = new Random();

    @Test
    void getTwoCardFromClass() {
        Talon talon = get52CardsTalon();
        Selection selection = new Times(2, new FromClass("Red"));
        SelectionResult result = selection.pickCards(talon, r);
        Assertions.assertEquals(2, result.cards.size());
        Assertions.assertEquals("Red", result.cards.get(0).originalClass);
        Assertions.assertEquals("Red", result.cards.get(1).originalClass);
    }
}
