package io.inblocks.civicpower.cryptopolitics.models.contexts;

import io.inblocks.civicpower.cryptopolitics.models.Context;
import io.inblocks.civicpower.cryptopolitics.models.SeedGeneratorParams;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.Random;

public class ForcedSeedContext implements Context {
    private final String forcedSeed;
    private Random rand = null;

    public ForcedSeedContext( final String forcedSeed) {
        this.forcedSeed = forcedSeed;
    }

    @Override
    public Random getRandom() {
        if (rand == null) {
            final byte[] rawSeed;
            try {
                rawSeed = Hex.decodeHex(forcedSeed.toCharArray());
            } catch (DecoderException e) {
                throw new IllegalArgumentException("forcedSeed");
            }
            rand = new Random(ByteBuffer.wrap(rawSeed).getLong());
        }
        return rand;
    }

    @Override
    public void setSeedGeneratorParams(SeedGeneratorParams seedGeneratorParams) {
        // do nothing
    }

    @Override
    public SeedGeneratorParams getSeedGeneratorParams() {
    return SeedGeneratorParams.builder().seedUsed(forcedSeed).build();
    }
}
