package io.inblocks.civicpower.cryptopolitics.models.selections;

import io.inblocks.civicpower.cryptopolitics.models.CardClass;
import io.inblocks.civicpower.cryptopolitics.models.CardSerie;
import io.inblocks.civicpower.cryptopolitics.models.Talon;

import java.util.List;

public abstract class SelectionTests {
    protected Talon get52CardsTalon() {
        return Talon.builder().classes(List.of(CardClass.builder()
                .cardClass("Black")
                .series(List.of(new CardSerie("piques", 13), new CardSerie("spades", 13)))
                .build(), CardClass.builder()
                .cardClass("Red")
                .series(List.of(new CardSerie("hearts", 13), new CardSerie("tiles", 13))).build())).build();
    }
}
