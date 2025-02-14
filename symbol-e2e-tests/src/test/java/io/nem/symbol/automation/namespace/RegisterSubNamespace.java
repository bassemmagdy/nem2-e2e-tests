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

package io.nem.symbol.automation.namespace;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.nem.symbol.automation.common.BaseTest;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.catbuffer.NamespaceCatbufferHelper;
import io.nem.symbol.automationHelpers.helper.sdk.NamespaceHelper;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.account.AccountInfo;
import io.nem.symbol.sdk.model.namespace.NamespaceId;
import io.nem.symbol.sdk.model.namespace.NamespaceInfo;
import io.nem.symbol.sdk.model.namespace.NamespaceRegistrationType;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

/** Register subnamcespace tests. */
public class RegisterSubNamespace extends BaseTest {
  private final NamespaceHelper namespaceHelper;

  /**
   * Constructor.
   *
   * @param testContext Test context.
   */
  public RegisterSubNamespace(final TestContext testContext) {
    super(testContext);
    namespaceHelper = new NamespaceHelper(testContext);
  }

  private String getParentName(final String subNamespace) {
    final int parentEnd = subNamespace.lastIndexOf(".");
    return subNamespace.substring(0, parentEnd);
  }

  private String getSubNamespaceName(final String subNamespace) {
    final int parentEnd = subNamespace.lastIndexOf(".");
    return subNamespace.substring(parentEnd + 1);
  }

  private void createSubNamespaceForUser(
      final String userName, final String name, final String parentName) {
    storeUserInfoInContext(userName);
    final Account account = getUser(userName);
    new NamespaceCatbufferHelper(getTestContext()).createSubNamespaceAndAnnounce(account, name, parentName);
    //namespaceHelper.createSubNamespaceAndAnnonce(account, name, parentName);
  }

  private void registerSubNamespaceForUserAndWait(
      final String userName, final String name, final String parentName) {
    storeUserInfoInContext(userName);
    final Account account = getUser(userName);
    namespaceHelper.createSubNamespaceAndWait(account, name, parentName);
  }

  private void verifySubNamespaceInfo(final String userName, final String namespace) {
    final NamespaceId namespaceId = getNamespaceIdFromName(namespace);
    final NamespaceInfo namespaceInfo =
        new NamespaceHelper(getTestContext()).getNamespaceInfoWithRetry(namespaceId);
    final String errorMessage =
        "SubNamespace info check failed for id: " + namespaceId.getIdAsLong();
    assertEquals(errorMessage, namespaceId.getIdAsLong(), namespaceInfo.getId().getIdAsLong());
    final AccountInfo accountInfo = getAccountInfoFromContext(userName);
//    assertEquals(
//        errorMessage,
//        accountInfo.getAddress().plain(),
//        namespaceInfo.getOwnerAddress().plain());
    final String[] namespaceParts = namespace.split("\\.");
    assertEquals(
        errorMessage, NamespaceRegistrationType.SUB_NAMESPACE, namespaceInfo.getRegistrationType());
    assertEquals(errorMessage, namespaceParts.length, namespaceInfo.getDepth().intValue());
    final NamespaceId parentNamespaceId = getNamespaceIdFromName(getParentName(namespace));
    assertEquals(
        errorMessage,
        parentNamespaceId.getIdAsLong(),
        namespaceInfo.parentNamespaceId().getIdAsLong());
    assertEquals(errorMessage, true, namespaceInfo.isSubnamespace());
  }

  @When("^(\\w+) creates a subnamespace named \"(.*)\"$")
  public void createSubNamespace(final String userName, final String subNamespace) {
    registerSubNamespaceForUserAndWait(
        userName, getSubNamespaceName(subNamespace), getParentName(subNamespace));
  }

  @And("^(\\w+) should become the owner of the new subnamespace \"(.*)\"$")
  public void verifySubNamespaceOwnerShip(final String userName, final String namespaceName) {
    verifySubNamespaceInfo(userName, resolveNamespaceName(namespaceName));
  }

  @Given("^(\\w+) registered the subnamespace \"(.*)\"$")
  public void registerSubNamespace(final String userName, final String subNamespace) {
    final BigInteger duration = addMinDuration(BigInteger.TEN);
    final String subNamespaceName = getSubNamespaceName(subNamespace);
    final String parentName = getParentName(subNamespace);
    final String parentRandomName = resolveNamespaceName(parentName);
    final String randomSubName = createRandomNamespace(subNamespaceName, getTestContext());
    new RegisterNamespace(getTestContext())
        .registerNamespaceForUserAndWait(userName, parentName, duration);
    registerSubNamespaceForUserAndWait(userName, randomSubName, parentRandomName);
    getTestContext()
        .getScenarioContext()
        .setContext(subNamespace, parentRandomName + "." + randomSubName);
  }

  @When("^(\\w+) tries to creates a subnamespace named \"(.*)\"$")
  public void createSubNamespaceInvalid(final String userName, final String subNamespace) {
    final String realName = resolveNamespaceName(subNamespace);
    createSubNamespaceForUser(
        userName, resolveNamespaceName(getSubNamespaceName(realName)), resolveNamespaceName(getParentName(realName)));
  }

  @When("^(\\w+) tries to creates a subnamespace \"(.*)\" which is too deep$")
  public void createSubNamespaceTooDeep(final String userName, final String subNamespace) {
    final String[] parts = subNamespace.split("\\.");
    String parentName = resolveNamespaceName(parts[0]);
    for (int i = 1; i < parts.length - 1; i++) {
      final String randomSubName = createRandomNamespace(parts[i], getTestContext());
      registerSubNamespaceForUserAndWait(userName, randomSubName, parentName);
      parentName += "." + randomSubName;
    }
    createSubNamespaceForUser(userName, parts[parts.length - 1], parentName);
  }
}
