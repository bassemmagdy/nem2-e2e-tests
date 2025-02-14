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

package io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.database.mongoDb;

import io.nem.symbol.sdk.api.TransactionStatementSearchCriteria;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.common.DataAccessContext;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.mappers.TransactionStatementsMapper;
import io.nem.symbol.sdk.model.receipt.TransactionStatement;
import org.bson.conversions.Bson;

import java.util.List;

public class TransactionStatementsCollection {
  /** Catapult collection */
  private final CatapultCollection<TransactionStatement, TransactionStatementsMapper>
      catapultCollection;
  /* Catapult context. */
  private final DataAccessContext context;

  /**
   * Constructor.
   *
   * @param context Catapult context.
   */
  public TransactionStatementsCollection(final DataAccessContext context) {
    this.context = context;
    catapultCollection =
        new CatapultCollection<>(
            context.getCatapultMongoDbClient(),
            "transactionStatements",
            TransactionStatementsMapper::new);
  }

  /**
   * Gets resolution statement for an unresolved address.
   *
   * @param height Block height.
   * @return Resolution statement.
   */
  public List<TransactionStatement> findByHeight(final long height) {
    final String keyName = "statement.height";
    final int timeoutInSeconds = 0;
    return catapultCollection.find(keyName, height, timeoutInSeconds);
  }

  private Bson toSearchCriteria(final TransactionStatementSearchCriteria criteria) {
    final MongoDbFilterBuilder builder =
        new MongoDbFilterBuilder()
            .withNumericValue("statement.height", criteria.getHeight().longValue())
            .withIn("statement.receipts.type", criteria.getReceiptTypes())
            .withMosaic("statement.receipts.mosaicId", criteria.getArtifactId())
            .withAddress("statement.receipts.recipientAddress", criteria.getRecipientAddress())
            .withAddress("statement.receipts.senderAddress", criteria.getSenderAddress())
            .withAddress("statement.receipts.targetAddress", criteria.getTargetAddress());
    return builder.build();
  }

  /**
   * It searches entities of a type based on a criteria.
   *
   * @param criteria the criteria
   * @return a page of entities.
   */
  public List<TransactionStatement> search(final TransactionStatementSearchCriteria criteria) {
    final Bson filters = toSearchCriteria(criteria);

    return catapultCollection.findR(filters, context.getDatabaseTimeoutInSeconds());
  }
}
