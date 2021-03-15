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

package io.nem.symbol.automationHelpers.helper.catbuffer;

import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.catapult.builders.*;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.message.Message;
import io.nem.symbol.sdk.model.mosaic.MosaicId;
import io.nem.symbol.sdk.model.transaction.SignedTransaction;
import io.nem.symbol.sdk.model.transaction.TransactionType;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transfer helper.
 */
public class TransferCatbufferHelper extends BaseHelper {
    private final TransactionCatbufferHelper transactionHelper;

    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public TransferCatbufferHelper(final TestContext testContext) {
        super(testContext, TransactionType.TRANSFER);
        this.transactionHelper = new TransactionCatbufferHelper(testContext);
    }

    private ByteBuffer getMessageBuffer(Message message) {
        return message == null ? ByteBuffer.allocate(0) : message.getPayloadByteBuffer();
    }

    /**
     * Gets transfer transaction.
     *
     * @param recipientAddress Recipient address.
     * @param mosaics          Mosaics to send.
     * @param message          Message to send.
     * @return Transfer transaction.
     */
    public byte[] createTransferTransaction(
            final Account signer,
            final AddressNoCheck recipientAddress,
            final List<MosaicNoCheck> mosaics,
            final Message message) {
        final UnresolvedAddressDto unresolvedAddressDto = toUnresolvedAddress(recipientAddress);
        final List<UnresolvedMosaicBuilder> unresolvedMosaicBuilders = mosaics.stream().map(m -> UnresolvedMosaicBuilder.create(new UnresolvedMosaicIdDto(m.getId().getIdAsLong()), new AmountDto(m.getAmount().longValue()))).collect(Collectors.toList());
        final Serializer bodySerializer = TransferTransactionBodyBuilder.create(unresolvedAddressDto, unresolvedMosaicBuilders, getMessageBuffer(message));
        return buildTransaction(signer.getPublicAccount().getPublicKey(), bodySerializer);
    }

    /**
     * Gets transfer transaction.
     *
     * @param recipientAddress Recipient address.
     * @param mosaicId         Mosaics to send.
     * @param message          Message to send.
     * @return Transfer transaction.
     */
    private byte[] createTransferTransaction(
            final Account signer,
            final AddressNoCheck recipientAddress,
            final MosaicId mosaicId,
            final BigInteger amount,
            final Message message) {
        return createTransferTransaction(signer, recipientAddress, Arrays.asList(new MosaicNoCheck(mosaicId, amount)), message);
    }

    /**
     * Creates a transfer transaction and announce.
     *
     * @param sender    Sender account.
     * @param recipient Recipient unresolved address.
     * @param mosaics   Mosaics to send.
     * @param message   Message to send.
     * @return Signed transaction.
     */
    public SignedTransaction createTransferAndAnnounce(
            final Account sender,
            final AddressNoCheck recipient,
            final List<MosaicNoCheck> mosaics,
            final Message message) {
        return transactionHelper.signAndAnnounceTransaction(
                sender, TransactionType.TRANSFER, createTransferTransaction(sender, recipient, mosaics, message));
    }
}
