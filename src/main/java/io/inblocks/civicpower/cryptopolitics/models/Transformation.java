package io.inblocks.civicpower.cryptopolitics.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import io.inblocks.civicpower.cryptopolitics.models.transformations.*;
import io.micronaut.core.annotation.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InitTransformation.class, name = "Init"),
        @JsonSubTypes.Type(value = PickCardsTransformation.class, name = "PickCards"),
        @JsonSubTypes.Type(value = AddCardsTransformation.class, name = "AddCards"),
        @JsonSubTypes.Type(value = OpenChestTransformation.class, name = "OpenChest"),
        @JsonSubTypes.Type(value = ExtendTalonTransformation.class, name = "ExtendTalon"),
})
@Schema(anyOf = {InitTransformation.class, PickCardsTransformation.class, AddCardsTransformation.class, OpenChestTransformation.class, ExtendTalonTransformation.class})
public interface Transformation {

    Talon apply(@NotNull Context context, @Nullable final Talon in);
    @JsonIgnore
    default Object getResults() { return null; }
}
