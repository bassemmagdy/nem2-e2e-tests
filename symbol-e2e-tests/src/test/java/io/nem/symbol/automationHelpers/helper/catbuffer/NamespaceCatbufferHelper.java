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
import io.nem.symbol.catapult.builders.BlockDurationDto;
import io.nem.symbol.catapult.builders.NamespaceIdDto;
import io.nem.symbol.catapult.builders.NamespaceRegistrationTransactionBodyBuilder;
import io.nem.symbol.catapult.builders.Serializer;
import io.nem.symbol.core.utils.StringEncoder;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.namespace.NamespaceId;
import io.nem.symbol.sdk.model.transaction.SignedTransaction;
import io.nem.symbol.sdk.model.transaction.TransactionType;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * Namespace helper.
 */
public class NamespaceCatbufferHelper extends BaseHelper {
    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public NamespaceCatbufferHelper(final TestContext testContext) {
        super(testContext, TransactionType.NAMESPACE_REGISTRATION);
    }

    private NamespaceId createNamespaceId(final String namespaceName) {
        return NamespaceId.createFromIdAndFullName(IdGeneratorNoCheck.generateNamespaceId(namespaceName), namespaceName);
    }

    private NamespaceId createFromNameAndParentId(String namespaceName, BigInteger parentId) {
        return NamespaceId.createFromIdAndFullName(IdGeneratorNoCheck.generateNamespaceId(namespaceName, parentId),
                namespaceName);
    }

    private byte[] createSubNamespaceTransaction(
            final Account signer, final String namespaceName, final String parentNamespaceName) {
        final ByteBuffer namespaceNameByteBuffer = ByteBuffer
                .wrap(StringEncoder.getBytes(namespaceName));
        final NamespaceId parentId = createNamespaceId(parentNamespaceName);
        final NamespaceIdDto parentNamespaceIdDto = new NamespaceIdDto(parentId.getIdAsLong());
        final NamespaceIdDto namespaceIdDto = new NamespaceIdDto(createFromNameAndParentId(namespaceName, parentId.getId()).getIdAsLong());
        final Serializer bodySerializer = NamespaceRegistrationTransactionBodyBuilder.createChild(parentNamespaceIdDto, namespaceIdDto, namespaceNameByteBuffer);
        return buildTransaction(signer.getPublicAccount().getPublicKey(), bodySerializer);
    }

    /**
     * Creates a root namespace transaction.
     *
     * @param signer        Signer
     * @param namespaceName Root namespace name.
     * @param duration      Duration of the namespace.
     * @return Register namespace transaction.
     */
    private byte[] createRootNamespaceTransaction(
            final Account signer, final String namespaceName, final BigInteger duration) {
        final ByteBuffer namespaceNameByteBuffer = ByteBuffer
                .wrap(StringEncoder.getBytes(namespaceName));
        final BlockDurationDto durationDto = new BlockDurationDto(duration.longValue());
        final NamespaceIdDto namespaceIdDto = new NamespaceIdDto(createNamespaceId(namespaceName).getIdAsLong());
        final Serializer bodySerializer = NamespaceRegistrationTransactionBodyBuilder.createRoot(durationDto, namespaceIdDto, namespaceNameByteBuffer);
        return buildTransaction(signer.getPublicAccount().getPublicKey(), bodySerializer);
    }

    /**
     * Creates and announce a root namespace transaction.
     *
     * @param account       Signer account.
     * @param namespaceName Namesapce name.
     * @param duration      Duration.
     * @return Signed transaction.
     */
    public SignedTransaction createRootNamespaceAndAnnounce(
            final Account account, final String namespaceName, final BigInteger duration) {
        return new TransactionCatbufferHelper(testContext)
                .signAndAnnounceTransaction(
                        account, TransactionType.NAMESPACE_REGISTRATION, createRootNamespaceTransaction(account, namespaceName, duration));
    }

    /**
     * Creates and announce a sub namespace transaction.
     *
     * @param account             Signer account.
     * @param namespaceName       Namesapce name.
     * @param parentNamespaceName Parent namespace name.
     * @return Signed transaction.
     */
    public SignedTransaction createSubNamespaceAndAnnounce(
            final Account account, final String namespaceName, final String parentNamespaceName) {
        return new TransactionCatbufferHelper(testContext)
                .signAndAnnounceTransaction(
                        account, TransactionType.NAMESPACE_REGISTRATION, createSubNamespaceTransaction(account, namespaceName, parentNamespaceName));
    }
}
