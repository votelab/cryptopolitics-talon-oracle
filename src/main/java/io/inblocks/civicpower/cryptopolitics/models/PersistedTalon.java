package io.inblocks.civicpower.cryptopolitics.models;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Introspected
@Builder
public class PersistedTalon {
    public final long version;
    public final Talon talonBefore;
    public final List<Transformation> transformations;
    public final SeedGeneratorParams seedGenerator;
    public final List<Object> results;
}
