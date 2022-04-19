package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.models.Context;
import io.inblocks.civicpower.cryptopolitics.models.Transformation;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@Introspected
public class DeprecateSeriesTransformation implements Transformation {
  @Valid @NotNull
  public final Map<String, List<String>> seriesToDeprecate;

  public DeprecateSeriesTransformation(final Map<String, List<String>> seriesToDeprecate) {
    this.seriesToDeprecate = seriesToDeprecate;
  }

  @Override
  public Talon apply(final Context context, final Talon in) {
    return in.deprecateSeries(seriesToDeprecate);
  }
}
