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

package io.nem.symbol.sdk.infrastructure.directconnect.listener;

import io.nem.symbol.catapult.builders.FinalizedBlockHeaderBuilder;
import io.nem.symbol.core.utils.ConvertUtils;
import io.nem.symbol.sdk.infrastructure.SerializationUtils;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.mappers.MapperUtils;
import io.nem.symbol.sdk.model.blockchain.FinalizedBlock;
import io.nem.symbol.sdk.model.network.NetworkType;
import org.zeromq.ZMQ.Socket;

/** Handles the block message from the symbol server. */
public class FinalizedBlockMessageHandler extends MessageBaseHandler {
  /**
   * Handle a message from the broker
   *
   * @param subscriber Subscriber for the message
   * @param networkType Network type.
   */
  @Override
  public FinalizedBlock handleMessage(final Socket subscriber, final NetworkType networkType) {
    final FinalizedBlockHeaderBuilder finalizedBlockHeaderBuilder =
        FinalizedBlockHeaderBuilder.loadFromBinary(toInputStream(subscriber.recv()));
    failIfMoreMessageAvailable(subscriber, "Finalized proof message is not correct.");
    return new FinalizedBlock(
        MapperUtils.toUnsignedLong(
            finalizedBlockHeaderBuilder.getRound().getEpoch().getFinalizationEpoch()),
        MapperUtils.toUnsignedLong(
            finalizedBlockHeaderBuilder.getRound().getPoint().getFinalizationPoint()),
        SerializationUtils.toUnsignedBigInteger(
            finalizedBlockHeaderBuilder.getHeight().getHeight()),
        ConvertUtils.toHex(finalizedBlockHeaderBuilder.getHash().getHash256().array()));
  }
}
