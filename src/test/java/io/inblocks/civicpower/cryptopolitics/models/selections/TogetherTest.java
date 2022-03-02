package io.inblocks.civicpower.cryptopolitics.models.selections;

import io.inblocks.civicpower.cryptopolitics.models.Selection;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

@MicronautTest
class TogetherTest extends SelectionTests {

    private final Random r = new Random();

    @Test
    void getTwoCardsFromTwoSeries() {
        Talon talon = get52CardsTalon();
        Selection selection = new Together(List.of(new FromSerie("Red", "hearts"), new FromSerie("Black", "spades")));
        SelectionResult result = selection.pickCards(talon, r);
        Assertions.assertEquals(2, result.cards.size());
        Assertions.assertEquals("Red", result.cards.get(0).originalClass);
        Assertions.assertEquals("hearts", result.cards.get(0).serieName);
        Assertions.assertEquals("Black", result.cards.get(1).originalClass);
        Assertions.assertEquals("spades", result.cards.get(1).serieName);
    }

}
