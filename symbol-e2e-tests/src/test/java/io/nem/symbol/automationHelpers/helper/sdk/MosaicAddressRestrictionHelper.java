package io.nem.symbol.automationHelpers.helper.sdk;

import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.sdk.api.MosaicRestrictionSearchCriteria;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.account.UnresolvedAddress;
import io.nem.symbol.sdk.model.mosaic.MosaicId;
import io.nem.symbol.sdk.model.mosaic.UnresolvedMosaicId;
import io.nem.symbol.sdk.model.restriction.MosaicRestriction;
import io.nem.symbol.sdk.model.restriction.MosaicRestrictionEntryType;
import io.nem.symbol.sdk.model.transaction.MosaicAddressRestrictionTransaction;
import io.nem.symbol.sdk.model.transaction.MosaicAddressRestrictionTransactionFactory;
import io.nem.symbol.sdk.model.transaction.SignedTransaction;

import java.math.BigInteger;

public class MosaicAddressRestrictionHelper extends BaseHelper<MosaicAddressRestrictionHelper> {

    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public MosaicAddressRestrictionHelper(final TestContext testContext) {
        super(testContext);
    }

    public MosaicAddressRestrictionTransaction createMosaicAddressRestrictionTransaction(
            final UnresolvedMosaicId mosaicId,
            final BigInteger restrictionKey,
            final UnresolvedAddress unresolvedAddress,
            final BigInteger restrictionValue) {
        final MosaicAddressRestrictionTransactionFactory mosaicAddressRestrictionTransactionFactory =
                MosaicAddressRestrictionTransactionFactory.create(
                        testContext.getNetworkType(),
                        transactionHelper.getDefaultDeadline(),
                        mosaicId,
                        restrictionKey,
                        unresolvedAddress,
                        restrictionValue);
        return buildTransaction(mosaicAddressRestrictionTransactionFactory);
    }

    /**
     * Creates Mosaic Address Restriction Transaction and announce it to the network.
     *
     * @param account           User account.
     * @param mosaicId          Mosaic id to restrict.
     * @param restrictionKey    Restriction key.
     * @param unresolvedAddress Unresolved address.
     * @param restrictionValue  Restriction value.
     * @return Signed transaction.
     */
    public SignedTransaction createMosaicAddressRestrictionAndAnnounce(
            final Account account,
            final UnresolvedMosaicId mosaicId,
            final BigInteger restrictionKey,
            final UnresolvedAddress unresolvedAddress,
            final BigInteger restrictionValue) {
        final TransactionHelper transactionHelper = new TransactionHelper(testContext);
        return transactionHelper.signAndAnnounceTransaction(
                account,
                () ->
                        createMosaicAddressRestrictionTransaction(
                                mosaicId, restrictionKey, unresolvedAddress, restrictionValue));
    }

    /**
     * Creates Mosaic Address Restriction Transaction and announce it to the network and wait for
     * confirmed status.
     *
     * @param account           User account.
     * @param mosaicId          Mosaic id to restrict.
     * @param restrictionKey    Restriction key.
     * @param unresolvedAddress Unresolved address.
     * @param restrictionValue  Restriction value.
     * @return Mosaic supply change transaction.
     */
    public MosaicAddressRestrictionTransaction submitMosaicAddressRestrictionTransactionAndWait(
            final Account account,
            final UnresolvedMosaicId mosaicId,
            final BigInteger restrictionKey,
            final UnresolvedAddress unresolvedAddress,
            final BigInteger restrictionValue) {
        final TransactionHelper transactionHelper = new TransactionHelper(testContext);
        return transactionHelper.signAndAnnounceTransactionAndWait(
                account,
                () ->
                        createMosaicAddressRestrictionTransaction(
                                mosaicId, restrictionKey, unresolvedAddress, restrictionValue));
    }

    public MosaicRestriction<?> getMosaicRestriction(final MosaicId mosaicId, final Address address) {
        return testContext.getRepositoryFactory().createRestrictionMosaicRepository().search(
                new MosaicRestrictionSearchCriteria().mosaicId(mosaicId).targetAddress(address)
                        .entryType(MosaicRestrictionEntryType.ADDRESS)).blockingFirst().getData().get(0);
    }
}
