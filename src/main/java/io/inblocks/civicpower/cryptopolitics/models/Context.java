package io.inblocks.civicpower.cryptopolitics.models;

import java.util.Random;

public interface Context {
    Random getRandom();

    void setSeedGeneratorParams(SeedGeneratorParams seedGeneratorParams);
    SeedGeneratorParams getSeedGeneratorParams();
}
