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
import io.nem.symbol.automationHelpers.helper.sdk.CommonHelper;
import io.nem.symbol.core.crypto.CryptoEngines;
import io.nem.symbol.core.crypto.DsaSigner;
import io.nem.symbol.core.crypto.Hashes;
import io.nem.symbol.core.crypto.Signature;
import io.nem.symbol.core.utils.ConvertUtils;
import io.nem.symbol.core.utils.ExceptionUtils;
import io.nem.symbol.sdk.api.TransactionRepository;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.transaction.CosignatureSignedTransaction;
import io.nem.symbol.sdk.model.transaction.SignedTransaction;
import io.nem.symbol.sdk.model.transaction.TransactionType;

/**
 * Transaction helper.
 */
public class TransactionCatbufferHelper {
    final TestContext testContext;

    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public TransactionCatbufferHelper(final TestContext testContext) {
        this.testContext = testContext;
    }

    /**
     * Generates hash for a serialized transaction payload.
     *
     * @param transactionPayload  Transaction payload
     * @param generationHashBytes the generation hash.
     * @return generated transaction hash.
     */
    public String createTransactionHash(String transactionPayload, final byte[] generationHashBytes) {
        byte[] bytes = ConvertUtils.fromHexToBytes(transactionPayload);
        final byte[] dataBytes = getSignBytes(bytes, generationHashBytes);
        final int sizeOfSignatureAndSignerPublicKey = 96;
        byte[] signingBytes = new byte[dataBytes.length + sizeOfSignatureAndSignerPublicKey];
        System.arraycopy(bytes, 8, signingBytes, 0, sizeOfSignatureAndSignerPublicKey);
        System.arraycopy(dataBytes, 0, signingBytes, sizeOfSignatureAndSignerPublicKey, dataBytes.length);
        byte[] result = Hashes.sha3_256(signingBytes);
        return ConvertUtils.toHex(result);
    }

    /**
     * Get the bytes required for signing.
     *
     * @param payloadBytes        Payload bytes.
     * @param generationHashBytes Generation hash bytes.
     * @return Bytes to sign.
     */
    public byte[] getSignBytes(final byte[] payloadBytes, final byte[] generationHashBytes) {
        final short headerSize = 4 + 32 + 64 + 8;
        final byte[] signingBytes = new byte[payloadBytes.length + generationHashBytes.length - headerSize];
        System.arraycopy(generationHashBytes, 0, signingBytes, 0, generationHashBytes.length);
        System.arraycopy(payloadBytes, headerSize, signingBytes, generationHashBytes.length,
                payloadBytes.length - headerSize);
        return signingBytes;
    }

    /**
     * Serialize and sign transaction creating a new SignedTransaction.
     *
     * @param account        The account to sign the transaction.
     * @param generationHash The generation hash for the network.
     * @return {@link SignedTransaction}
     */
    public SignedTransaction signWith(final Account account, final byte[] transactionBytes, final String generationHash, final TransactionType transactionType) {
        final DsaSigner theSigner = CryptoEngines.defaultEngine().createDsaSigner(account.getKeyPair());
        final byte[] generationHashBytes = ConvertUtils.getBytes(generationHash);
        final byte[] signingBytes = getSignBytes(transactionBytes, generationHashBytes);
        final Signature theSignature = theSigner.sign(signingBytes);

        final byte[] payload = new byte[transactionBytes.length];
        System.arraycopy(transactionBytes, 0, payload, 0, 8); // Size
        System.arraycopy(theSignature.getBytes(), 0, payload, 8, theSignature.getBytes().length); // Signature
        System.arraycopy(account.getKeyPair().getPublicKey().getBytes(), 0, payload, 64 + 8,
                account.getKeyPair().getPublicKey().getBytes().length); // Signer
        System.arraycopy(transactionBytes, 104, payload, 104, transactionBytes.length - 104);

        final String hash = createTransactionHash(ConvertUtils.toHex(payload), generationHashBytes);
        return new SignedTransaction(account.getPublicAccount(), ConvertUtils.toHex(payload), hash, TransactionType.rawValueOf(transactionType.getValue()));
    }


    /**
     * Signs the transaction.
     *
     * @param transactionBytes Transaction to sign.
     * @param account          Account to sign the transaction.
     * @param generationHash   Generation hash
     * @return Signed Transaction.
     */
    public SignedTransaction signTransaction(
            final byte[] transactionBytes, final Account account, final String generationHash, final TransactionType transactionType) {
        final SignedTransaction signedTransaction = signWith(account, transactionBytes, generationHash, transactionType);
        testContext.setSignedTransaction(signedTransaction);
        return signedTransaction;
    }

    /**
     * Signs the transaction.
     *
     * @param transactionBytes Transaction to sign.
     * @param account          Account to sign the transaction.
     * @return Signed Transaction.
     */
    public SignedTransaction signTransaction(final byte[] transactionBytes, final Account account, final TransactionType transactionType) {
        return signTransaction(transactionBytes, account, testContext.getSymbolConfig().getGenerationHashSeed(), transactionType);
    }

    /**
     * Announce a signed transaction.
     *
     * @param signedTransaction Signed transaction.
     */
    public void announceTransaction(final SignedTransaction signedTransaction) {
        final TransactionRepository transactionRepository =
                testContext.getRepositoryFactory().createTransactionRepository();
        testContext.getLogger().LogInfo("Announce tx : " + CommonHelper.toString(signedTransaction));
        ExceptionUtils.propagate(
                () -> transactionRepository.announce(signedTransaction).toFuture().get());
    }

    /**
     * Announce an aggregate bonded transaction.
     *
     * @param signedTransaction Signed transaction.
     */
    public void announceAggregateBonded(final SignedTransaction signedTransaction) {
        final TransactionRepository transactionRepository =
                testContext.getRepositoryFactory().createTransactionRepository();
        testContext
                .getLogger()
                .LogInfo("Announce bonded tx : " + CommonHelper.toString(signedTransaction));
        ExceptionUtils.propagate(
                () -> transactionRepository.announceAggregateBonded(signedTransaction).toFuture().get());
    }

    /**
     * Announce a cosignature signed transaction.
     *
     * @param signedTransaction Signed transaction.
     */
    public void announceAggregateBondedCosignature(
            final CosignatureSignedTransaction signedTransaction) {
        final TransactionRepository transactionRepository =
                testContext.getRepositoryFactory().createTransactionRepository();
        testContext
                .getLogger()
                .LogInfo(
                        "Announce aggregate bonded cosignature tx : "
                                + CommonHelper.toString(signedTransaction));
        ExceptionUtils.propagate(
                () ->
                        transactionRepository
                                .announceAggregateBondedCosignature(signedTransaction)
                                .toFuture()
                                .get());
    }

    /**
     * Sign and announce transaction.
     *
     * @param transactionBytes Transaction to sign.
     * @param signer           Signer of the transaction.
     * @return Signed transaction.
     */
    public SignedTransaction signAndAnnounceTransaction(final Account signer, final TransactionType transactionType, final byte[] transactionBytes) {
        final SignedTransaction signedTransaction = signTransaction(transactionBytes, signer, transactionType);
        announceTransaction(signedTransaction);
        return signedTransaction;
    }
}
