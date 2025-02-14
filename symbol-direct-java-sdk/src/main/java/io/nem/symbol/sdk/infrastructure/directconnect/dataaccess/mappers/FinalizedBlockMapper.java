/*
 * Copyright (c) 2016-present,
 * Jaguar0625, gimre, BloodyRookie, Tech Bureau, Corp. All rights reserved.
 *
 * This file is part of Catapult.
 *
 * Catapult is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Catapult is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Catapult.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.mappers;

import io.nem.symbol.sdk.model.blockchain.FinalizedBlock;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public class FinalizedBlockMapper implements Function<JsonObject, FinalizedBlock> {
  /**
   * Converts from Json to FinalizedBlock.
   *
   * @param jsonObject Json object.
   * @return FinalizedBlock object.
   */
  public FinalizedBlock apply(final JsonObject jsonObject) {
    final JsonObject finalizedBlockJsonObject = jsonObject.getJsonObject("block");
    return new FinalizedBlock(
        finalizedBlockJsonObject.getLong("finalizationEpoch"),
        finalizedBlockJsonObject.getLong("finalizationPoint"),
        MapperUtils.toBigInteger(finalizedBlockJsonObject, "height"),
        finalizedBlockJsonObject.getString("hash"));
  }
}
