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

package io.nem.symbol.automation.transfer;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.nem.symbol.automation.common.BaseTest;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.sdk.TransactionHelper;
import io.nem.symbol.automationHelpers.helper.sdk.TransferHelper;
import io.nem.symbol.sdk.model.message.PlainMessage;
import io.nem.symbol.sdk.model.transaction.SignedTransaction;
import io.nem.symbol.sdk.model.transaction.TransferTransaction;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/** Send message tests. */
public class SendAMessage extends BaseTest {
  final TransferHelper transferHelper;

  /**
   * Constructor.
   *
   * @param testContext Test context.
   */
  public SendAMessage(final TestContext testContext) {
    super(testContext);
    transferHelper = new TransferHelper(testContext);
  }

  @When("^(\\w+) tries to send a message which is over the max length to (.*)$")
  public void sendReallyLongMessage(
      final String sender, final String recipient) {
    int messageSize = getTestContext().getSymbolConfig().getTransferMaxMessageSize() + 1;
    final PlainMessage longMessage = PlainMessage.create(new String(new char[messageSize]));
    triesToTransferAssets(sender, recipient, new ArrayList<>(), longMessage);
  }

  @When("^(\\w+) sends \"(\\w+)\" to \"(.*)\"$")
  public void sendMessageToUser(final String sender, final String message, final String recipient) {
    final PlainMessage plainMessage = PlainMessage.create(message);
    transferAssets(sender, recipient, new ArrayList<>(), plainMessage);
  }

  @When("^(\\w+) tries to send \"(\\w+)\" to \"(.*)\"$")
  public void sendMessageInvalid(
      final String sender, final String message, final String recipient) {
    final PlainMessage plainMessage = PlainMessage.create(message);
    triesToTransferAssets(sender, recipient, new ArrayList<>(), plainMessage);
  }

  @And("the \"(.*)\" should receive the message \"(\\w+)\"$")
  public void VerifyTransfer(final String recipient, final String message) {
    final SignedTransaction signedTransaction = getTestContext().getSignedTransaction();
    final TransferTransaction transferTransaction =
        new TransactionHelper(getTestContext()).waitForTransactionToComplete(signedTransaction);
    assertEquals("Message test is not the same", message, transferTransaction.getMessage().get().getText());
  }
}
