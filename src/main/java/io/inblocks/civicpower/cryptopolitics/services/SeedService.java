package io.inblocks.civicpower.cryptopolitics.services;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.event.ServiceReadyEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.toIntExact;

/*
 * Generates provable random values using cascading hashes served in reverse order
 */

@Singleton
@Slf4j
public class SeedService implements ApplicationEventListener<ServiceReadyEvent> {

  // Governs how many hashes to store in memory. Memory / CPU tradeoff
  private static final int DEFAULT_SAMPLING = 1000;

  private static class SeedGenerator {
    // SHA-256 => 32
    private static final int SEED_SIZE_IN_BYTES = 32;

    private final long size;
    private final int sampling;

    private final ByteBuffer attic;

    private final ByteBuffer buffer;
    private int bufferAtticIndex;

    public SeedGenerator(byte[] seed, long size, int sampling) {
      this.size = size;
      this.sampling = sampling;
      int atticSize = toIntExact(size / sampling);
      attic = ByteBuffer.allocate(SEED_SIZE_IN_BYTES * atticSize);
      seed = DigestUtils.sha256(seed);
      attic.put(0, seed);
      for (int j = 1; j < atticSize; j++) {
        for (int i = 0; i < sampling; i++) {
          seed = DigestUtils.sha256(seed);
        }
        attic.put(j * SEED_SIZE_IN_BYTES, seed);
      }

      buffer = ByteBuffer.allocate(SEED_SIZE_IN_BYTES * (sampling - 1));
      bufferAtticIndex = -1;
    }

    private void populateBuffer(int atticIndex) {
      bufferAtticIndex = atticIndex;
      int bufferSize = sampling - 1;
      byte[] seed = new byte[SEED_SIZE_IN_BYTES];
      attic.get(atticIndex * SEED_SIZE_IN_BYTES, seed);
      for (int i = 0; i < bufferSize; i++) {
        seed = DigestUtils.sha256(seed);
        buffer.put(SEED_SIZE_IN_BYTES * i, seed);
      }
    }

    public synchronized byte[] get(long index) {
      if (index < 0 || index > size) throw new IllegalArgumentException("index");
      byte[] result = new byte[SEED_SIZE_IN_BYTES];
      int atticIndex = toIntExact(index / sampling);
      int offset = toIntExact(index % sampling);
      if (offset == 0) attic.get(atticIndex * SEED_SIZE_IN_BYTES, result);
      else {
        if (atticIndex != bufferAtticIndex) populateBuffer(atticIndex);
        buffer.get((offset - 1) * SEED_SIZE_IN_BYTES, result);
      }
      return result;
    }
  }

  record NamedSeeds(String name, String value, long size, boolean preloaded) {}

  @Property(name = "talon.generators")
  public List<NamedSeeds> namedSeeds;

  private final Map<String, SeedGenerator> generators = new HashMap<>();

  private SeedGenerator buildGenerator(String name) {
    NamedSeeds namedSeed =
        namedSeeds.stream()
            .filter(ns -> ns.name.equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(name));
    final byte[] seed = DigestUtils.sha256(namedSeed.value);
    long before = System.nanoTime();
    final SeedGenerator seedGenerator = new SeedGenerator(seed, namedSeed.size, DEFAULT_SAMPLING);
    long after = System.nanoTime();
    log.info("SeedGeneratorParams " + name + " init time: " + (int) ((after - before) / 1e6) + "ms");
    return seedGenerator;
  }

  private synchronized SeedGenerator getGenerator(String generatorName) {
      return generators.computeIfAbsent(generatorName, this::buildGenerator);
  }

  public byte[] get(String generatorName, long index) {
    SeedGenerator generator = getGenerator(generatorName);
    return generator.get(index);
  }

  public long getSize(String generatorName) {
    SeedGenerator generator = getGenerator(generatorName);
    return generator.size;
  }

  @Override
  public void onApplicationEvent(ServiceReadyEvent event) {
    // Warmup
    namedSeeds.stream().filter(ns -> ns.preloaded).forEach(ns -> getGenerator(ns.name));
  }
}
