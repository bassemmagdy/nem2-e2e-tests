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

import io.nem.symbol.catapult.builders.BlockHeaderBuilder;
import io.nem.symbol.catapult.builders.Hash256Dto;
import io.nem.symbol.core.utils.ConvertUtils;
import io.nem.symbol.sdk.infrastructure.SerializationUtils;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.mappers.MapperUtils;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.account.PublicAccount;
import io.nem.symbol.sdk.model.blockchain.BlockInfo;
import io.nem.symbol.sdk.model.blockchain.BlockType;
import io.nem.symbol.sdk.model.network.NetworkType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.ZMQ.Socket;

import java.math.BigInteger;
import java.util.Collections;

/** Handles the block message from the symbol server. */
public class BlockMessageHandler extends MessageBaseHandler {
  /**
   * Handle a message from the broker
   *
   * @param subscriber Subscriber for the message
   * @param networkType Network type.
   */
  @Override
  public BlockInfo handleMessage(final Socket subscriber, final NetworkType networkType) {
    final BlockHeaderBuilder blockHeaderBuilder =
        BlockHeaderBuilder.loadFromBinary(toInputStream(subscriber.recv()));
    Logger logger = LogManager.getLogger("listener");
    logger.error("Actual message: ");
    final Hash256Dto entityHash = Hash256Dto.loadFromBinary(toInputStream(subscriber.recv()));
    logger.error("Actual message1: ");
    final Hash256Dto generationHash = Hash256Dto.loadFromBinary(toInputStream(subscriber.recv()));
    logger.error("Actual message2: ");
    failIfMoreMessageAvailable(subscriber, "Block message is not correct.");

    final String entityHashHex = ConvertUtils.toHex(entityHash.getHash256().array());
    final String generationHashHex = ConvertUtils.toHex(generationHash.getHash256().array());
    final NetworkType blockNetworkType =
        NetworkType.rawValueOf(
            SerializationUtils.byteToUnsignedInt(blockHeaderBuilder.getNetwork().getValue()));
    final BlockType blockType = BlockType.rawValueOf(SerializationUtils.shortToUnsignedInt(blockHeaderBuilder.getType().getValue()));
    return new BlockInfo(
        "0",
        MapperUtils.toUnsignedLong(blockHeaderBuilder.getSize()),
        entityHashHex,
        generationHashHex,
        BigInteger.ZERO,
        Collections.EMPTY_LIST,
        0,
        0,
        0,
        Collections.EMPTY_LIST,
        ConvertUtils.toHex(blockHeaderBuilder.getSignature().getSignature().array()),
        PublicAccount.createFromPublicKey(
            ConvertUtils.toHex(blockHeaderBuilder.getSignerPublicKey().getKey().array()),
            blockNetworkType),
        blockNetworkType,
        Integer.valueOf(blockHeaderBuilder.getVersion()),
        blockType,
        BigInteger.valueOf(blockHeaderBuilder.getHeight().getHeight()),
        BigInteger.valueOf(blockHeaderBuilder.getTimestamp().getTimestamp()),
        BigInteger.valueOf(blockHeaderBuilder.getDifficulty().getDifficulty()),
        MapperUtils.toUnsignedLong(blockHeaderBuilder.getFeeMultiplier().getBlockFeeMultiplier()),
        ConvertUtils.toHex(blockHeaderBuilder.getPreviousBlockHash().getHash256().array()),
        ConvertUtils.toHex(blockHeaderBuilder.getTransactionsHash().getHash256().array()),
        ConvertUtils.toHex(blockHeaderBuilder.getReceiptsHash().getHash256().array()),
        ConvertUtils.toHex(blockHeaderBuilder.getStateHash().getHash256().array()),
        ConvertUtils.toHex(
            blockHeaderBuilder.getGenerationHashProof().getGamma().getProofGamma().array()),
        ConvertUtils.toHex(
            blockHeaderBuilder.getGenerationHashProof().getScalar().getProofScalar().array()),
        ConvertUtils.toHex(
            blockHeaderBuilder
                .getGenerationHashProof()
                .getVerificationHash()
                .getProofVerificationHash()
                .array()),
        Address.createFromEncoded(
            ConvertUtils.toHex(blockHeaderBuilder.getBeneficiaryAddress().getAddress().array())));
  }
}
