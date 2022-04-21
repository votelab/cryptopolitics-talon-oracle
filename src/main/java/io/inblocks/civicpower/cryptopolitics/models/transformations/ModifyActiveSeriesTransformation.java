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
public class ModifyActiveSeriesTransformation implements Transformation {

  @Valid @NotNull
  public final List<Talon.SeriesSelection> retiredCards;
  @Valid @NotNull
  public final List<Talon.SeriesSelection> reinstatedCards;

  public ModifyActiveSeriesTransformation(final List<Talon.SeriesSelection> retiredCards, final List<Talon.SeriesSelection> reinstatedCards) {
    this.retiredCards = retiredCards;
    this.reinstatedCards = reinstatedCards;
  }

  @Override
  public Talon apply(final Context context, final Talon in) {
    return in.modifyActiveSeries(retiredCards, reinstatedCards);
  }
}
