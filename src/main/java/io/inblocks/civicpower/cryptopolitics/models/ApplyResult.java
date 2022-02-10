package io.inblocks.civicpower.cryptopolitics.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ApplyResult {
    public List<Object> results;
    public Talon talon;
    public SeedGeneratorParams seedGenerator;
}
