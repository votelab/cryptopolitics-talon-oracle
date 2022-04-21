package io.inblocks.civicpower.cryptopolitics.models.selections;

import io.inblocks.civicpower.cryptopolitics.models.cards.CardClass;
import io.inblocks.civicpower.cryptopolitics.models.cards.CardSerie;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;

import java.util.List;

public abstract class SelectionTests {
    protected Talon get52CardsTalon() {
        return Talon.builder().classes(List.of(CardClass.builder()
                .cardClass("Black")
                .isInfinite(false)
                .series(List.of(new CardSerie("piques", 13, false), new CardSerie("spades", 13, false)))
                .build(), CardClass.builder()
                .cardClass("Red")
                .isInfinite(false)
                .series(List.of(new CardSerie("hearts", 13, false), new CardSerie("tiles", 13, false))).build())).build();
    }
}
