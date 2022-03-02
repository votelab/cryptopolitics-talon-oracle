package io.inblocks.civicpower.cryptopolitics.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import io.inblocks.civicpower.cryptopolitics.models.selections.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Random;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FromClass.class, name = "FromClass"),
        @JsonSubTypes.Type(value = FromSerie.class, name = "FromSerie"),
        @JsonSubTypes.Type(value = OneOf.class, name = "OneOf"),
        @JsonSubTypes.Type(value = TheCard.class, name = "TheCard"),
        @JsonSubTypes.Type(value = Times.class, name = "Times"),
        @JsonSubTypes.Type(value = Together.class, name = "Together"),
})
@Schema(anyOf = {FromClass.class, FromSerie.class, OneOf.class, TheCard.class, Times.class, Together.class})
public interface Selection {
    SelectionResult pickCards(final Talon talon, final Random random);
}
