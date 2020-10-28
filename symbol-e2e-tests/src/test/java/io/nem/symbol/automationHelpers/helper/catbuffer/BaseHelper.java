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
import io.nem.symbol.core.crypto.PublicKey;
import io.nem.symbol.core.utils.Base32Encoder;
import io.nem.symbol.sdk.infrastructure.SerializationUtils;
import io.nem.symbol.sdk.model.transaction.TransactionType;

import java.nio.ByteBuffer;

public abstract class BaseHelper {

    protected final TestContext testContext;
    final SignatureDto signature;
    final byte version;
    final NetworkTypeDto network;
    final EntityTypeDto type;
    final AmountDto maxFee;
    ;

    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    BaseHelper(final TestContext testContext, final TransactionType transactionType) {
        this.testContext = testContext;
        signature = new SignatureDto(ByteBuffer.allocate(64));
        network = NetworkTypeDto.rawValueOf((byte) testContext.getNetworkType().getValue());
        type = EntityTypeDto.rawValueOf((short) transactionType.getValue());
        maxFee = new AmountDto(testContext.getMinFeeMultiplier() * 500);
        this.version = (byte) transactionType.getCurrentVersion();
    }

    private KeyDto toKeyDto(final PublicKey publicKey) {
        return new KeyDto(ByteBuffer.wrap(publicKey.getBytes()));
    }

    private Serializer getBaseTransaction(final PublicKey signerPublicKey) {
        final KeyDto publicKeyDto = toKeyDto(signerPublicKey);
        final TimestampDto deadline = new TimestampDto(testContext.getDefaultDeadline().getInstant(testContext.getRepositoryFactory().getEpochAdjustment().blockingFirst()).getEpochSecond());
        return TransactionBuilder.create(this.signature, publicKeyDto, this.version, this.network, this.type, this.maxFee, deadline);
    }

    protected byte[] buildTransaction(final PublicKey publicKey, final Serializer transactionBodySupplier) {
        return SerializationUtils.concat(getBaseTransaction(publicKey).serialize(), transactionBodySupplier.serialize());
    }

    public ByteBuffer fromAddressToByteBuffer(final AddressNoCheck resolvedAddress) {
        return ByteBuffer.wrap(Base32Encoder.getBytes(resolvedAddress.plain()));
    }

    public UnresolvedAddressDto toUnresolvedAddress(final AddressNoCheck unresolvedAddress) {
        return new UnresolvedAddressDto(fromAddressToByteBuffer(unresolvedAddress));
    }

}
