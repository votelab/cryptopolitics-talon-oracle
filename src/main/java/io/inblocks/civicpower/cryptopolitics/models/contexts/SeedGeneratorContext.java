package io.inblocks.civicpower.cryptopolitics.models.contexts;

import io.inblocks.civicpower.cryptopolitics.models.Context;
import io.inblocks.civicpower.cryptopolitics.models.SeedGeneratorParams;
import io.inblocks.civicpower.cryptopolitics.services.SeedService;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.Random;

public class SeedGeneratorContext implements Context {
    public final SeedService seedService;
    private SeedGeneratorParams seedGeneratorParams;

    private Random rand = null;

    public SeedGeneratorContext(final SeedService seedService, final SeedGeneratorParams seedGeneratorParams) {
        this.seedService = seedService;
        setSeedGeneratorParams(seedGeneratorParams);
    }

    @Override
    public Random getRandom() {
        if (rand == null) {
            final byte[] rawSeed;
            // Lazily take a new seed when randomness is called for
            seedGeneratorParams.index--;
            rawSeed = seedService.get(seedGeneratorParams.name, seedGeneratorParams.index);
            seedGeneratorParams.seedUsed = Hex.encodeHexString(rawSeed);
            rand = new Random(ByteBuffer.wrap(rawSeed).getLong());
        }
        return rand;
    }

    @Override
    public void setSeedGeneratorParams(SeedGeneratorParams seedGeneratorParams) {
        this.seedGeneratorParams = seedGeneratorParams;
        if (seedGeneratorParams != null) {
            this.seedGeneratorParams.seedUsed = null;
            final long upperBound = seedService.getSize(seedGeneratorParams.name);
            if (seedGeneratorParams.index <= 0 || seedGeneratorParams.index > upperBound)
                throw new IllegalArgumentException("Seed index " + seedGeneratorParams.index + " out of bounds [0," + upperBound + "[");
        }
    }

    @Override
    public SeedGeneratorParams getSeedGeneratorParams() {
        return seedGeneratorParams;
    }
}
