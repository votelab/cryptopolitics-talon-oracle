package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.exceptions.TalonAlreadyInitialized;
import io.inblocks.civicpower.cryptopolitics.models.Context;
import io.inblocks.civicpower.cryptopolitics.models.SeedGeneratorParams;
import io.inblocks.civicpower.cryptopolitics.models.Talon;
import io.inblocks.civicpower.cryptopolitics.models.Transformation;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Introspected
public class InitTransformation implements Transformation {
    @Valid @NotNull public final Talon setup;
    @Valid @NotNull public final SeedGeneratorParams seedGenerator;

    public InitTransformation(final Talon setup, final SeedGeneratorParams seedGenerator) {
        this.setup = setup;
        this.seedGenerator = seedGenerator;
        setup.checkFinitudeConsistency();
    }

    @Override
    public Talon apply(final Context context, final Talon in) {
        if (in != null) {
            throw new TalonAlreadyInitialized();
        }
        context.setSeedGeneratorParams(seedGenerator);
        return setup;
    }
}
