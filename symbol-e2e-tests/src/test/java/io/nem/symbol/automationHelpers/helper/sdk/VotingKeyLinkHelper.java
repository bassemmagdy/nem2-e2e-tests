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

package io.nem.symbol.automationHelpers.helper.sdk;

import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.core.crypto.PublicKey;
import io.nem.symbol.core.crypto.VotingKey;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.transaction.LinkAction;
import io.nem.symbol.sdk.model.transaction.SignedTransaction;
import io.nem.symbol.sdk.model.transaction.VotingKeyLinkTransaction;
import io.nem.symbol.sdk.model.transaction.VotingKeyLinkTransactionFactory;

public class VotingKeyLinkHelper extends BaseHelper<VotingKeyLinkHelper> {

  /**
   * Constructor.
   *
   * @param testContext Test context.
   */
  public VotingKeyLinkHelper(final TestContext testContext) {
    super(testContext);
  }

  public VotingKeyLinkTransaction createVotingKeyLinkTransaction(
      final PublicKey linkedPublicKey,
      final long startEpoch,
      final long endEpoch,
      final LinkAction linkAction) {
    final VotingKeyLinkTransactionFactory votingKeyLinkTransactionFactory =
        VotingKeyLinkTransactionFactory.create(
            testContext.getNetworkType(),
            transactionHelper.getDefaultDeadline(),
            linkedPublicKey,
            startEpoch,
            endEpoch,
            linkAction);
    return buildTransaction(votingKeyLinkTransactionFactory);
  }

  /**
   * Creates a voting key link transaction and announce it to the network.
   *
   * @param account User account.
   * @param linkedPublicKey Linked public key.
   * @param startEpoch Start point.
   * @param endEpoch End point.
   * @param linkAction Link action.
   * @return Signed transaction.
   */
  public SignedTransaction createVotingKeyLinkTransactionAndAnnounce(
      final Account account,
      final PublicKey linkedPublicKey,
      final long startEpoch,
      final long endEpoch,
      final LinkAction linkAction) {
    final TransactionHelper transactionHelper = new TransactionHelper(testContext);
    return transactionHelper.signAndAnnounceTransaction(
        account,
        () -> createVotingKeyLinkTransaction(linkedPublicKey, startEpoch, endEpoch, linkAction));
  }

  /**
   * Creates a voting key link transaction transaction and announce it to the network and wait for
   * confirmed status.
   *
   * @param account User account.
   * @param linkedPublicKey Linked public key.
   * @param startEpoch Start point.
   * @param endEpoch End point.
   * @param linkAction Link action.
   * @return Mosaic supply change transaction.
   */
  public VotingKeyLinkTransaction submitVotingKeyLinkTransactionAndWait(
      final Account account,
      final PublicKey linkedPublicKey,
      final long startEpoch,
      final long endEpoch,
      final LinkAction linkAction) {
    final TransactionHelper transactionHelper = new TransactionHelper(testContext);
    return transactionHelper.signAndAnnounceTransactionAndWait(
        account,
        () -> createVotingKeyLinkTransaction(linkedPublicKey, startEpoch, endEpoch, linkAction));
  }
}
