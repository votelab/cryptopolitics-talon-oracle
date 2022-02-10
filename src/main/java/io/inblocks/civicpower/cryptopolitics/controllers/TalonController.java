package io.inblocks.civicpower.cryptopolitics.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.inblocks.civicpower.cryptopolitics.models.*;
import io.inblocks.civicpower.cryptopolitics.models.contexts.ForcedSeedContext;
import io.inblocks.civicpower.cryptopolitics.models.contexts.SeedGeneratorContext;
import io.inblocks.civicpower.cryptopolitics.services.SeedService;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Controller("/talon")
@Validated
@Slf4j
public class TalonController {

  @Inject private ObjectMapper mapper;

  @Inject private SeedService seedService;

  // Workaround so that talon parameter can be both @Valid and @Nullable
  @Data
  @Introspected
  public static class ApplyTransformationQuery {
    @Valid @Nullable Talon talon;
    @Valid @Nullable SeedGeneratorParams seedGenerator;
    @Valid @NotNull List<Transformation> transformations;
  }

  @Post("/applyTransformations")
  @Operation(
      summary = "Compute the result of applying transformation(s) to a talon",
      description =
          "Given a talon and a list of transformations, return the transformed talon and the list of results of each transformation",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Valid transformations, return the list of their results and the new talon state"),
        @ApiResponse(
            responseCode = "400",
            description = "No such class, serie or card; or a requested class was empty."),
        @ApiResponse(responseCode = "409", description = "Talon or card already exist.")
      })
  public ApplyResult applyTransformations(@Body @Valid ApplyTransformationQuery body) {
    Context context = buildContext(body.seedGenerator);
    return transformTalon(body.talon, body.transformations, context);
  }

  private Context buildContext(SeedGeneratorParams seedGenerator) {
    if (seedGenerator == null) {
      // Build an incomplete context (used before Init)
      return new SeedGeneratorContext(seedService, null);
    }
    if (seedGenerator.name == null || seedGenerator.index == null)
      return new ForcedSeedContext(seedGenerator.seedUsed);
    return new SeedGeneratorContext(seedService, seedGenerator);
  }

  @Post("/checkTransformations")
  @Operation(
      summary = "Check that persisted talon results are consistent",
      description =
          "Apply the list of transformations to the persisted talonBefore state, using the persisted random seed, and check that the results match",
      responses = {
        @ApiResponse(responseCode = "200", description = "Results match persisted results"),
        @ApiResponse(
            responseCode = "409",
            description = "Computed results and persisted results don't match")
      })
  public HttpResponse<List<Object>> checkTransformations(
      @Body @Valid PersistedTalon persistedTalon) {
    Context context = new ForcedSeedContext(persistedTalon.seedGenerator.seedUsed);
    ApplyResult applyResults =
        transformTalon(
            persistedTalon.talonBefore, persistedTalon.transformations, context);
    List<Object> jsonResults = mapper.convertValue(applyResults.results, new TypeReference<>() {});
    return (jsonResults.equals(persistedTalon.results)
            ? HttpResponse.ok()
            : HttpResponse.status(
                HttpStatus.CONFLICT, "Computed result and persisted result don't match"))
        .body(jsonResults);
  }

  private ApplyResult transformTalon(
      Talon talon, List<Transformation> transformations, Context context) {
    final ApplyResult applyResults = new ApplyResult();
    applyResults.results = new ArrayList<>();
    Talon accumulator = talon;
    for (Transformation transformation : transformations) {
      accumulator = transformation.apply(context, accumulator);
      applyResults.results.add(transformation.getResults());
    }
    applyResults.talon = accumulator;
    applyResults.seedGenerator = context.getSeedGeneratorParams();
    return applyResults;
  }

  @Requires(env = "dev")
  @Get("/seedGenerator/{generatorName}/{seedIndex}")
  public String getSeed(String generatorName, long seedIndex) {
    return Hex.encodeHexString(seedService.get(generatorName, seedIndex));
  }
}
