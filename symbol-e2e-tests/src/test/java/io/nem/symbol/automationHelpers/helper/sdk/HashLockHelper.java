package io.nem.symbol.automationHelpers.helper.sdk;

import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.mosaic.Mosaic;
import io.nem.symbol.sdk.model.transaction.HashLockInfo;
import io.nem.symbol.sdk.model.transaction.HashLockTransaction;
import io.nem.symbol.sdk.model.transaction.HashLockTransactionFactory;
import io.nem.symbol.sdk.model.transaction.SignedTransaction;

import java.math.BigInteger;

public class HashLockHelper extends BaseHelper<HashLockHelper> {
    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public HashLockHelper(final TestContext testContext) {
        super(testContext);
    }


    private HashLockTransaction createHashLockTransaction(
            final Mosaic mosaic, final BigInteger duration, final SignedTransaction signedTransaction) {
        final HashLockTransactionFactory hashLockTransactionFactory =
                HashLockTransactionFactory.create(
                        testContext.getNetworkType(), transactionHelper.getDefaultDeadline(), mosaic, duration, signedTransaction);
        return buildTransaction(hashLockTransactionFactory);
    }

    /**
     * Creates a lock fund transaction and announce it to the network and wait for confirmed status.
     *
     * @param account User account.
     * @param mosaic Mosaic to lock.
     * @param duration Duration to lock.
     * @param signedTransaction Signed transaction.
     * @return Signed transaction.
     */
    public SignedTransaction createLockFundsAndAnnounce(
            final Account account,
            final Mosaic mosaic,
            final BigInteger duration,
            final SignedTransaction signedTransaction) {
        return new TransactionHelper(testContext)
                .signAndAnnounceTransaction(
                        account, () -> createHashLockTransaction(mosaic, duration, signedTransaction));
    }

    /**
     * Creates a lock fund transaction and announce it to the network and wait for confirmed status.
     *
     * @param account User account.
     * @param mosaic Mosaic to lock.
     * @param duration Duration to lock.
     * @param signedTransaction Signed transaction.
     * @return Lock funds transaction.
     */
    public HashLockTransaction submitHashLockTransactionAndWait(
            final Account account,
            final Mosaic mosaic,
            final BigInteger duration,
            final SignedTransaction signedTransaction) {
        return new TransactionHelper(testContext)
                .signAndAnnounceTransactionAndWait(
                        account, () -> createHashLockTransaction(mosaic, duration, signedTransaction));
    }

    public HashLockInfo getHashLock(final String hash)
    {
        return testContext.getRepositoryFactory().createHashLockRepository().getHashLock(hash).blockingFirst();
    }
}
