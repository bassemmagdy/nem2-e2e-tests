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

package io.nem.symbol.sdk.infrastructure.directconnect;

import io.nem.symbol.sdk.api.*;
import io.nem.symbol.sdk.infrastructure.common.CatapultContext;
import io.nem.symbol.sdk.infrastructure.common.ConfigurationHelper;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.dao.*;
import io.nem.symbol.sdk.infrastructure.directconnect.listener.ListenerImpl;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.blockchain.BlockInfo;
import io.nem.symbol.sdk.model.mosaic.*;
import io.nem.symbol.sdk.model.mosaic.Currency;
import io.nem.symbol.sdk.model.mosaic.CurrencyBuilder;
import io.nem.symbol.sdk.model.namespace.AliasType;
import io.nem.symbol.sdk.model.namespace.NamespaceInfo;
import io.nem.symbol.sdk.model.network.NetworkConfiguration;
import io.nem.symbol.sdk.model.network.NetworkType;
import io.reactivex.Observable;

import java.math.BigInteger;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Implementation for the direct connect. */
public class DirectConnectRepositoryFactoryImpl implements RepositoryFactory {

  private final CatapultContext context;
  private final BlockInfo firstBlock;
  private final NetworkConfiguration networkConfiguration;
  private Map<String, Currency> currencyMap;

  /**
   * Constructor.
   *
   * @param context Catapult context.
   */
  public DirectConnectRepositoryFactoryImpl(CatapultContext context) {
    this.context = context;
    this.firstBlock = createBlockRepository().getBlockByHeight(BigInteger.ONE).blockingFirst();
    this.networkConfiguration = createNetworkRepository().getNetworkProperties().blockingFirst();
    this.currencyMap = new HashMap<>();
  }

  private Currency getNetworkCurrency(final String mosaicIdValue) {
    final MosaicId mosaicId = new MosaicId(toHex(mosaicIdValue));
    final MosaicInfo mosaicInfo = createMosaicRepository().getMosaic(mosaicId).blockingFirst();
    final CurrencyBuilder builder =
        new CurrencyBuilder(mosaicId, mosaicInfo.getDivisibility())
            .withSupplyMutable(mosaicInfo.isSupplyMutable())
            .withTransferable(mosaicInfo.isTransferable());
    final Optional<NamespaceInfo> namespaceInfoOptional = createNamespaceRepository()
        .search(
            new NamespaceSearchCriteria()
                .aliasType(AliasType.MOSAIC)
                .ownerAddress((Address) mosaicInfo.getOwnerAddress()))
        .blockingFirst()
        .getData()
        .stream()
        .filter(
            namespaceInfo ->
                namespaceInfo.isActive()
                    && namespaceInfo.getStartHeight() == BigInteger.ONE
                    && ((MosaicId) namespaceInfo.getAlias().getAliasValue()).getIdAsLong()
                        == mosaicInfo.getMosaicId().getIdAsLong())
        .findFirst();

    namespaceInfoOptional.ifPresent(n -> builder.withNamespaceId(n.getId()));
    return builder.build();
  }

  private Currency getCurrency(final String mosaicId) {
    return currencyMap.computeIfAbsent(mosaicId, this::getNetworkCurrency);
  }

  /**
   * Gets the network type.
   *
   * @return Network type.
   */
  @Override
  public Observable<NetworkType> getNetworkType() {
    return Observable.just(firstBlock.getNetworkType());
  }

  /**
   * Gets the generation hash.
   *
   * @return Generation Hash.
   */
  @Override
  public Observable<String> getGenerationHash() {
    return Observable.just(firstBlock.getGenerationHash());
  }

  @Override
  public Observable<Currency> getNetworkCurrency() {
    return Observable.fromCallable(
        () -> getCurrency(networkConfiguration.getChain().getCurrencyMosaicId()));
  }

  @Override
  public Observable<Currency> getHarvestCurrency() {
    return Observable.fromCallable(
        () -> getCurrency(networkConfiguration.getChain().getHarvestingMosaicId()));
  }

  /**
   * @return the configured harvest currencies configuration like "cat.currency" or "cat.harvest".
   * This method uses the user configured properties if provided. If it's not provided, it
   * resolves the configuration * from the /network/properties endpoint. This method is cached,
   * the server is only called the * first time. The network currency configuration
   * @see CurrencyService
   * @see RepositoryFactoryConfiguration
   */
  @Override
  public Observable<NetworkCurrencies> getNetworkCurrencies() {
    return null;
  }

  /**
   * @return the configured server epochAdjustment. This method uses the user configured properties
   *     if provided. If it's not provided, it resolves the configuration from the
   *     /network/properties endpoint. This method is cached, the server is only called the first
   *     time. The network currency configuration
   * @see RepositoryFactoryConfiguration
   */
  @Override
  public Observable<Duration> getEpochAdjustment() {
    return Observable.fromCallable(
        () -> toDuration(networkConfiguration.getNetwork().getEpochAdjustment()));
  }

  /**
   * Creates the account repository.
   *
   * @return Account repository.
   */
  @Override
  public AccountRepository createAccountRepository() {
    return new AccountsDao(context);
  }

  /**
   * Creates the multisig repository.
   *
   * @return Multisig repository.
   */
  @Override
  public MultisigRepository createMultisigRepository() {
    return new MultisigDao(context);
  }

  /**
   * Creates the block repository.
   *
   * @return Block repository.
   */
  @Override
  public BlockRepository createBlockRepository() {
    return new BlockchainDao(context);
  }

  /**
   * Creates the receipt repository.
   *
   * @return Receipt repository.
   */
  @Override
  public ReceiptRepository createReceiptRepository() {
    return new BlockchainDao(context);
  }

  /**
   * Creates the chain repository.
   *
   * @return Chain repository.
   */
  @Override
  public ChainRepository createChainRepository() {
    return new BlockchainDao(context);
  }

  /**
   * Creates the mosaic repository.
   *
   * @return Mosaic repository.
   */
  @Override
  public MosaicRepository createMosaicRepository() {
    return new MosaicsDao(context);
  }

  @Override
  public NamespaceRepository createNamespaceRepository() {
    return new NamespaceDao(context);
  }

  @Override
  public NetworkRepository createNetworkRepository() {
    return new NetworkDao(context);
  }

  @Override
  public NodeRepository createNodeRepository() {
    throw new UnsupportedOperationException("Method not implemented");
  }

  @Override
  public TransactionRepository createTransactionRepository() {
    return new TransactionDao(context);
  }

  /** @return a newly created {@link TransactionStatusRepository} */
  @Override
  public TransactionStatusRepository createTransactionStatusRepository() {
    return new TransactionDao(context);
  }

  @Override
  public MetadataRepository createMetadataRepository() {
    return new MetadataDao(context);
  }

  @Override
  public RestrictionAccountRepository createRestrictionAccountRepository() {
    throw new UnsupportedOperationException("Method not implemented");
  }

  @Override
  public RestrictionMosaicRepository createRestrictionMosaicRepository() {
    throw new UnsupportedOperationException("Method not implemented");
  }

  /** @return a newly created {@link HashLockRepository} */
  @Override
  public HashLockRepository createHashLockRepository() {
    throw new UnsupportedOperationException("Method not implemented");
  }

  /** @return a newly created {@link SecretLockRepository} */
  @Override
  public SecretLockRepository createSecretLockRepository() {
    throw new UnsupportedOperationException("Method not implemented");
  }

  /**
   * @return a newly created {@link FinalizationRepository}
   */
  @Override
  public FinalizationRepository createFinalizationRepository() {
    return null;
  }

  @Override
  public Listener createListener() {
    return new ListenerImpl(context.getBrokerNodeContext(), getNetworkType().blockingFirst());
  }

  @Override
  public JsonSerialization createJsonSerialization() {
    throw new UnsupportedOperationException("Method not implemented");
  }

  @Override
  public void close() {}

  public CatapultContext getContext() {
    return context;
  }

  private String toHex(final String value) {
    return ConfigurationHelper.toHex(value);
  }

  private Duration toDuration(final String value) {
    return ConfigurationHelper.toDuration(value);
  }
}
