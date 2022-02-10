package io.inblocks.civicpower.cryptopolitics.models;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Introspected
public class Card {
    @NotNull public final String originalClass;
    @NotNull public final String serieName;
    public final int orderNumber;
}
