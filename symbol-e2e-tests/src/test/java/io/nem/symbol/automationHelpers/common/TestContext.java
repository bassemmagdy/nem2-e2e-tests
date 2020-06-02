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

package io.nem.symbol.automationHelpers.common;

import io.nem.symbol.automationHelpers.config.ConfigFileReader;
import io.nem.symbol.automationHelpers.helper.TransactionHelper;
import io.nem.symbol.core.utils.ExceptionUtils;
import io.nem.symbol.sdk.api.RepositoryFactory;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.account.PublicAccount;
import io.nem.symbol.sdk.model.blockchain.BlockInfo;
import io.nem.symbol.sdk.model.blockchain.NetworkType;
import io.nem.symbol.sdk.model.mosaic.MosaicId;
import io.nem.symbol.sdk.model.mosaic.NetworkCurrency;
import io.nem.symbol.sdk.model.mosaic.UnresolvedMosaicId;
import io.nem.symbol.sdk.model.transaction.*;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** Test context */
public class TestContext {
  private final ConfigFileReader configFileReader;
  private final Account defaultSignerAccount;
  private final ScenarioContext scenarioContext;
  private final List<Transaction> transactions;
  private final PublicAccount harvesterPublicAccount;
  private final Map<String, List<Transaction>> userFeeMap = new ConcurrentHashMap<>();
  private final RepositoryFactory repositoryFactory;
  private SignedTransaction signedTransaction;
  private Log logger;
  private MosaicId networkCurrencyMosaicId;

  /** Constructor. */
  public TestContext() {
    configFileReader = new ConfigFileReader();
    scenarioContext = new ScenarioContext();
    repositoryFactory = new RepositoryFactoryImpl(configFileReader).create();
    this.getLogger().LogError("Connecting to Network: " + getNetworkType());
    transactions = Collections.synchronizedList(new ArrayList<>());
    final String privateString = configFileReader.getUserPrivateKey();
    defaultSignerAccount = Account.createFromPrivateKey(privateString, getNetworkType());
    harvesterPublicAccount =
        PublicAccount.createFromPublicKey(
            configFileReader.getHarvesterPublicKey(), getNetworkType());
  }

  /**
   * Gets the configuration reader.
   *
   * @return Configuration reader.
   */
  public ConfigFileReader getConfigFileReader() {
    return configFileReader;
  }

  /**
   * Gets default signer account.
   *
   * @return Default signer account.
   */
  public Account getDefaultSignerAccount() {
    return defaultSignerAccount;
  }

  /**
   * Gets scenario context.
   *
   * @return Scenario context.
   */
  public ScenarioContext getScenarioContext() {
    return scenarioContext;
  }

  /**
   * Gets transactations.
   *
   * @return List of transactions.
   */
  public List<Transaction> getTransactions() {
    return transactions;
  }

  /**
   * Gets a transactation of a given type.
   *
   * @param transactionType Transaction type.
   * @return Transaction object if found.
   */
  public <T extends Transaction> Optional<T> findTransaction(
      final TransactionType transactionType) {
    final List<Transaction> filterTransaction =
        transactions.stream()
            .filter(t -> t.getType() == transactionType)
            .collect(Collectors.toList());
    return filterTransaction.isEmpty()
        ? Optional.empty()
        : Optional.of((T) filterTransaction.get(filterTransaction.size() - 1));
  }

  /**
   * Adds a transaction.
   *
   * @param transaction Transaction to add.
   */
  public void addTransaction(Transaction transaction) {
    this.transactions.add(transaction);
  }

  /** Clear the transaction list. */
  public void clearTransaction() {
    this.transactions.clear();
  }

  /**
   * Gets signed transaction.
   *
   * @return Signed transaction.
   */
  public SignedTransaction getSignedTransaction() {
    return signedTransaction;
  }

  /**
   * Sets the signed transaction.
   *
   * @param signedTransaction Signed transaction.
   */
  public void setSignedTransaction(SignedTransaction signedTransaction) {
    this.signedTransaction = signedTransaction;
  }

  /**
   * Sets the scenario name for the logger.
   *
   * @param scenarioName Scenario Name.
   */
  public void setLoggerScenario(final String scenarioName) {
    this.logger = Log.getLogger(scenarioName);
  }

  /**
   * Gets the current logger.
   *
   * @return Current logger or the default.
   */
  public Log getLogger() {
    if (null == logger) {
      logger = Log.getLogger("TestAutomation");
    }
    return logger;
  }

  /**
   * Get the generation hash from the server.
   *
   * @return Generation hash.
   */
  public String getGenerationHash() {
    return "BF42024E58FE65C487A099CEF6370D023B4746C3E89027D9740817CEA54A2D63";
    //return "E74320AB77DE107DF6FB14258F936F0B0EFAC1A1BACE5BE4A2AEE8AF01CB865A"; //ExceptionUtils.propagate(() -> repositoryFactory.getGenerationHash().toFuture().get());
  }

  /**
   * Get the network type from the server.
   *
   * @return Network type.
   */
  public NetworkType getNetworkType() {
    return ExceptionUtils.propagate(() -> repositoryFactory.getNetworkType().toFuture().get());
  }

  /**
   * Get the network currency.
   *
   * @return Namespace id.
   */
  public NetworkCurrency getNetworkCurrency() {
    return NetworkCurrency.CAT_CURRENCY;
  }

  public MosaicId getNetworkCurrencyMosaicId() {
    if (networkCurrencyMosaicId == null) {
      networkCurrencyMosaicId =
          repositoryFactory
              .createNamespaceRepository()
              .getLinkedMosaicId(getNetworkCurrency().getNamespaceId().get())
              .blockingFirst();
    }
    return networkCurrencyMosaicId;
  }

  /**
   * Gets harvester public account.
   *
   * @return Public account.
   */
  public PublicAccount getHarvesterPublicAccount() {
    return harvesterPublicAccount;
  }

  /**
   * Gets repository factory.
   *
   * @return Repository factory.
   */
  public RepositoryFactory getRepositoryFactory() {
    return repositoryFactory;
  }

  public void updateUserFee(final PublicAccount publicAccount, final Transaction transaction) {
    final String key = publicAccount.getPublicKey().toHex();
    userFeeMap.putIfAbsent(key, new LinkedList<>());
    userFeeMap.get(key).add(transaction);
  }

  public BigInteger getFeesForUser(final PublicAccount publicAccount, final UnresolvedMosaicId id) {
    boolean isNetworkCurrency =
        Arrays.asList(getNetworkCurrency(), getNetworkCurrencyMosaicId().getId())
            .contains(id.getId());
    if (!isNetworkCurrency) {
      return BigInteger.ZERO;
    }
    final TransactionHelper transactionHelper = new TransactionHelper(this);
    final List<Transaction> userTransactions =
        userFeeMap.getOrDefault(publicAccount.getPublicKey().toHex(), new LinkedList<>());
    final Integer fee =  userTransactions.parallelStream().map( transaction -> {
      final TransactionStatus status = transactionHelper.getTransactionStatus(transaction.getTransactionInfo().get().getHash().get());
      if (status.getGroup() != TransactionState.CONFIRMED) {
        return 0;
      }
      final BlockInfo blockInfo =
          repositoryFactory
              .createBlockRepository()
              .getBlockByHeight(transaction.getTransactionInfo().get().getHeight())
              .blockingFirst();
      return transaction.getSize() * blockInfo.getFeeMultiplier();
      //      if (transaction.getType() == TransactionType.AGGREGATE_BONDED) {
      //        final String hash = transaction.getTransactionInfo().get().getHash().get();
      //        final int coSigner = getScenarioContext().isContains(hash) ?
      // getScenarioContext().getContext(hash) : 0;
      //        userFee = userFee.add(BigInteger.valueOf((coSigner * 96)*
      // blockInfo.getFeeMultiplier()));
      //      }
    }).reduce(0, Integer::sum);
    return BigInteger.valueOf(fee.intValue());
  }

  public void clearUserFee(final PublicAccount publicAccount) {
    userFeeMap.remove(publicAccount.getPublicKey().toHex());
  }
}
