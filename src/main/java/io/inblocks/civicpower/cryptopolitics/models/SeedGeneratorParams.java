package io.inblocks.civicpower.cryptopolitics.models;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Introspected
@Builder
public class SeedGeneratorParams {
    @NotNull public final String name;
    public Long index;
    @Nullable public String seedUsed;
}
