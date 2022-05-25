package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.models.Context;
import io.inblocks.civicpower.cryptopolitics.models.SeriesRetirementsByClass;
import io.inblocks.civicpower.cryptopolitics.models.Transformation;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Introspected
public class SeriesRetirementTransformation implements Transformation {

  @Valid @NotNull
  public final List<SeriesRetirementsByClass> retiredCards;

  public SeriesRetirementTransformation(final List<SeriesRetirementsByClass> retiredCards) {
    this.retiredCards = retiredCards;
  }

  @Override
  public Talon apply(final Context context, final Talon in) {
    return in.retireSeries(retiredCards);
  }
}
