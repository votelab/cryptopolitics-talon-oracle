package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.models.Context;
import io.inblocks.civicpower.cryptopolitics.models.Transformation;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Introspected
public class DeprecateSeriesTransformation implements Transformation {

  @Valid @NotNull
  public final List<Talon.ClassDeprecations> deprecatedCards;

  public DeprecateSeriesTransformation(final List<Talon.ClassDeprecations> deprecatedCards) {
    this.deprecatedCards = deprecatedCards;
  }

  @Override
  public Talon apply(final Context context, final Talon in) {
    return in.deprecateSeries(deprecatedCards);
  }
}
