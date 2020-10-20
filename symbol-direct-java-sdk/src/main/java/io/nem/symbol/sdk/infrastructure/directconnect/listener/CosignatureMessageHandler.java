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

import io.nem.symbol.catapult.builders.DetachedCosignatureBuilder;
import io.nem.symbol.core.utils.ConvertUtils;
import io.nem.symbol.sdk.model.account.PublicAccount;
import io.nem.symbol.sdk.model.network.NetworkType;
import io.nem.symbol.sdk.model.transaction.CosignatureSignedTransaction;
import org.zeromq.ZMQ.Socket;

import java.math.BigInteger;

/** Handle the transaction cosignature message from the server. */
public class CosignatureMessageHandler extends MessageBaseHandler {
  /**
   * Handle a message from the broker
   *
   * @param subscriber Subscriber for the message.
   * @param networkType Network type.
   */
  @Override
  public CosignatureSignedTransaction handleMessage(final Socket subscriber, final NetworkType networkType) {
    final DetachedCosignatureBuilder detachedCosignatureBuilder =
        DetachedCosignatureBuilder.loadFromBinary(toInputStream(subscriber.recv()));
    failIfMoreMessageAvailable(subscriber, "Detached cosignature message is not correct.");

    return new CosignatureSignedTransaction(
        BigInteger.valueOf(detachedCosignatureBuilder.getVersion()),
        ConvertUtils.toHex(detachedCosignatureBuilder.getParentHash().getHash256().array()),
        ConvertUtils.toHex(detachedCosignatureBuilder.getSignature().getSignature().array()),
        PublicAccount.createFromPublicKey(ConvertUtils.toHex(detachedCosignatureBuilder.getSignerPublicKey().getKey().array()),
                networkType));
  }
}
