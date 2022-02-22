package io.inblocks.civicpower.cryptopolitics.models.selections;

import io.inblocks.civicpower.cryptopolitics.models.Selection;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.Talon;
import io.inblocks.civicpower.cryptopolitics.models.Weighted;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

@MicronautTest
class OneOfTest extends SelectionTests {
    private final Random r = new Random();

    @Test
    void getOneCardFromOfOne() {
        Talon talon = get52CardsTalon();
        Selection selection = new OneOf(List.of(new Weighted<>(new FromSerie("Red", "hearts"), 1), new Weighted<>(new FromSerie("Red", "tiles"), 1)));
        SelectionResult result = selection.pickCards(talon, r);
        Assertions.assertEquals(1, result.cards.size());
        Assertions.assertEquals("Red", result.cards.get(0).originalClass);
        String serieName = result.cards.get(0).serieName;
        Assertions.assertTrue("hearts".equals(serieName) || "tiles".equals(serieName));
    }
}
