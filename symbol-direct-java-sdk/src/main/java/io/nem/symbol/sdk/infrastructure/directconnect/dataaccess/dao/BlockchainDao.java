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

package io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.dao;

import io.nem.symbol.sdk.api.*;
import io.nem.symbol.sdk.infrastructure.common.CatapultContext;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.database.mongoDb.*;
import io.nem.symbol.sdk.model.blockchain.*;
import io.nem.symbol.sdk.model.receipt.AddressResolutionStatement;
import io.nem.symbol.sdk.model.receipt.MosaicResolutionStatement;
import io.nem.symbol.sdk.model.receipt.TransactionStatement;
import io.reactivex.Observable;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Blockchain dao repository. */
public class BlockchainDao implements BlockRepository, ChainRepository, ReceiptRepository {
  /* Catapult context. */
  private final CatapultContext catapultContext;
  private final BlocksCollection blocksCollection;

  /**
   * Constructor.
   *
   * @param context Catapult context.
   */
  public BlockchainDao(final CatapultContext context) {
    this.catapultContext = context;
    blocksCollection = new BlocksCollection(catapultContext.getDataAccessContext());
  }

  private MerkleProofInfo getMerkleProofInfo(
      final BigInteger height,
      final String hash,
      final Function<FullBlockInfo, List<String>> getMerkleTree,
      final Function<FullBlockInfo, Integer> getNumberOfLeafs) {
    final FullBlockInfo fullBlockInfo = blocksCollection.find(height.longValue()).get();
    final Integer numOfLeafs = getNumberOfLeafs.apply(fullBlockInfo);
    Validate.isTrue(numOfLeafs > 0, "No elements was found in the block.");
    final List<String> merkleTreeList = getMerkleTree.apply(fullBlockInfo);
    final MerkleTree merkleTree = new MerkleTree();
    return new MerkleProofInfo(merkleTree.buildAuditPath(hash, merkleTreeList));
  }

  /**
   * Gets the block info at specific height.
   *
   * @param height Height of the block.
   * @return Block info.
   */
  @Override
  public Observable<BlockInfo> getBlockByHeight(final BigInteger height) {
    return Observable.fromCallable(
        () -> getBlockInfo(blocksCollection.find(height.longValue()).get()));
  }

  /**
   * @param height the height
   * @param hash the hash.
   * @return {@link Observable} of MerkleProofInfo
   */
  @Override
  public Observable<MerkleProofInfo> getMerkleReceipts(BigInteger height, String hash) {
    return Observable.fromCallable(
        () ->
            getMerkleProofInfo(
                height,
                hash,
                fullBlockInfo -> fullBlockInfo.getStatementMerkleTree(),
                fullBlockInfo -> fullBlockInfo.getStatementsCount()));
  }

  /**
   * Get the merkle path for a given a transaction and block Returns the merkle path for a
   * [transaction](https://nemtech.github.io/concepts/transaction.html) included in a block. The
   * path is the complementary data needed to calculate the merkle root. A client can compare if the
   * calculated root equals the one recorded in the block header, verifying that the transaction was
   * included in the block.
   *
   * @param height
   * @param hash
   * @return {@link Observable} of MerkleProofInfo
   */
  @Override
  public Observable<MerkleProofInfo> getMerkleTransaction(BigInteger height, String hash) {
    return Observable.fromCallable(
        () ->
            getMerkleProofInfo(
                height,
                hash,
                fullBlockInfo -> fullBlockInfo.getTransactionMerkleTree(),
                fullBlockInfo -> fullBlockInfo.getTransactionsCount()));
  }

  /**
   * Gets the height of the blockchain.
   *
   * @return Height of the blockchain.
   */
  public Observable<BigInteger> getBlockchainHeight() {
    return Observable.fromCallable(
        () ->
            new ChainStatisticCollection(catapultContext.getDataAccessContext())
                .get()
                .getNumBlocks());
  }

  /**
   * Gets the score of the blockchain.
   *
   * @return Score of the blockchain.
   */
  public Observable<ChainStatisticInfo> getBlockchainScore() {
    return Observable.fromCallable(
        () -> {
          final ChainStatisticInfo chainStatisticInfo =
              new ChainStatisticCollection(catapultContext.getDataAccessContext()).get();
          return chainStatisticInfo;
        });
  }

  private BlockInfo getBlockInfo(final FullBlockInfo fullBlockInfo) {
    return new BlockInfo(
        fullBlockInfo.getRecordId(),
        fullBlockInfo.getSize(),
        fullBlockInfo.getHash(),
        fullBlockInfo.getGenerationHash(),
        fullBlockInfo.getTotalFee(),
        fullBlockInfo.getSubCacheMerkleRoots(),
        fullBlockInfo.getTransactionsCount(),
        fullBlockInfo.getTotalTransactionsCount(),
        fullBlockInfo.getStatementsCount(),
        fullBlockInfo.getSubCacheMerkleRoots(),
        fullBlockInfo.getSignature(),
        fullBlockInfo.getSignerPublicAccount(),
        fullBlockInfo.getNetworkType(),
        fullBlockInfo.getVersion(),
        fullBlockInfo.getType(),
        fullBlockInfo.getHeight(),
        fullBlockInfo.getTimestamp(),
        fullBlockInfo.getDifficulty(),
        fullBlockInfo.getFeeMultiplier(),
        fullBlockInfo.getPreviousBlockHash(),
        fullBlockInfo.getBlockTransactionsHash(),
        fullBlockInfo.getBlockReceiptsHash(),
        fullBlockInfo.getStateHash(),
        fullBlockInfo.getProofGamma(),
        fullBlockInfo.getProofScalar(),
        fullBlockInfo.getProofVerificationHash(),
        fullBlockInfo.getBeneficiaryAddress());
  }

  /**
   * It searches entities of a type based on a criteria.
   *
   * @param criteria the criteria
   * @return a page of entities.
   */
  @Override
  public Observable<Page<BlockInfo>> search(BlockSearchCriteria criteria) {
    // TODO: paging logic
    return Observable.fromCallable(
        () ->
            new Page<>(
                blocksCollection.search(criteria).stream()
                    .map(b -> getBlockInfo(b))
                    .collect(Collectors.toList())));
  }

  /**
   * Returns a transaction statements page based on the criteria.
   *
   * @param criteria the criteria
   * @return a page of {@link TransactionStatement}
   */
  @Override
  public Observable<Page<TransactionStatement>> searchReceipts(
      TransactionStatementSearchCriteria criteria) {
    // TODO: paging logic
    return Observable.fromCallable(
        () ->
            new Page<>(
                new TransactionStatementsCollection(catapultContext.getDataAccessContext())
                    .search(criteria)));
  }

  /**
   * Returns an addresses resolution statements page based on the criteria.
   *
   * @param criteria the criteria
   * @return a page of {@link AddressResolutionStatement}
   */
  @Override
  public Observable<Page<AddressResolutionStatement>> searchAddressResolutionStatements(
      ResolutionStatementSearchCriteria criteria) {
    // TODO: paging logic
    return Observable.fromCallable(
        () ->
            new Page<>(
                new AddressResolutionStatementsCollection(catapultContext.getDataAccessContext())
                    .search(criteria)));
  }

  /**
   * Returns an mosaic resoslution statements page based on the criteria.
   *
   * @param criteria the criteria
   * @return a page of {@link MosaicResolutionStatement}
   */
  @Override
  public Observable<Page<MosaicResolutionStatement>> searchMosaicResolutionStatements(
      ResolutionStatementSearchCriteria criteria) {
    // TODO: paging logic
    return Observable.fromCallable(
        () ->
            new Page<>(
                new MosaicResolutionStatementsCollection(catapultContext.getDataAccessContext())
                    .search(criteria)));
  }

  /**
   * Gets current blockchain score.
   *
   * @return Observable of BigInteger
   */
  @Override
  public Observable<ChainInfo> getChainInfo() {
    return getBlockchainScore()
        .map(
            s -> {
              return getBlockchainHeight()
                  .map(
                      h -> {
                        final FinalizedBlock finalizedBlock =
                            new FinalizedBlocksCollection(catapultContext.getDataAccessContext())
                                .getLastFinalizedBlock();
                        return new ChainInfo(h, s.getScoreLow(), s.getScoreHigh(), finalizedBlock);
                      });
            })
        .blockingFirst();
  }
}

class MerkleTree {
  /**
   * Returns the index of a hash in a Merkle tree.
   *
   * @param {Uint8Array} hash Hash to look up in the tree.
   * @param {object} tree Merkle tree object containing the number of hashed elements and the tree
   *     of hashes.
   * @returns {array} Index of the first element in the tree matching the given hash, otherwise -1
   *     is returned.
   */
  int indexOfLeafWithHash(final String hash, List<String> tree) {
    return tree.indexOf(hash);
  }

  Pair<Integer, Position> siblingOf(final int nodeIndex) {
    return nodeIndex % 2 == 1
        ? Pair.of(nodeIndex - 1, Position.LEFT)
        : Pair.of(nodeIndex + 1, Position.RIGHT);
  }

  /**
   * Given a Merkle tree and a hashed element in it, returns the audit path required for a
   * consistency check.
   *
   * @param {Uint8Array} hash Element's hash for which to build the audit path.
   * @param {object} tree Merkle tree object containing the number of elements and the tree of
   *     hashes.
   * @returns {array} Array of objects containing the Merkle tree hash, and its relative position
   *     (left or right).
   */
  List<MerklePathItem> buildAuditPath(final String hash, final List<String> tree) {
    if (tree.isEmpty()) {
      throw new IllegalArgumentException("Merkle tree is empty.");
    }

    int start = 0;
    int currentLayerCount = tree.size();
    int layerSubindexOfHash = indexOfLeafWithHash(hash, tree);
    if (layerSubindexOfHash == -1) {
      throw new IllegalArgumentException("Hash not found.");
    }

    final List<MerklePathItem> auditPath = new ArrayList<>();
    while (currentLayerCount > 2) {
      currentLayerCount = currentLayerCount % 2 == 1 ? currentLayerCount + 1 : currentLayerCount;
      Pair<Integer, Position> sibling = siblingOf(start + layerSubindexOfHash);
      auditPath.add(new MerklePathItem(sibling.getRight(), tree.get(sibling.getLeft())));
      currentLayerCount /= 2;
      start += currentLayerCount;
      layerSubindexOfHash = (int) Math.floor(layerSubindexOfHash / 2);
    }
    return auditPath;
  }
}
