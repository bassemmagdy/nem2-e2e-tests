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

import io.nem.symbol.sdk.api.MetadataSearchCriteria;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.common.DataAccessContext;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.mappers.MetadataEntryMapper;
import io.nem.symbol.sdk.model.metadata.Metadata;
import org.bson.conversions.Bson;

import java.util.List;

/* Metadata Collection */
public class MetadataCollection {
  /** Catapult collection */
  private final CatapultCollection<Metadata, MetadataEntryMapper> catapultCollection;
  /* Catapult context. */
  private final DataAccessContext context;

  /**
   * Constructor.
   *
   * @param context Catapult context.
   */
  public MetadataCollection(final DataAccessContext context) {
    this.context = context;
    catapultCollection =
        new CatapultCollection<>(
            context.getCatapultMongoDbClient(), "metadata", MetadataEntryMapper::new);
  }

  private Bson toBsonFilter(final MetadataSearchCriteria criteria) {
    final MongoDbFilterBuilder builder =
        new MongoDbFilterBuilder()
            .withAddress("metadataEntry.targetAddress", criteria.getTargetAddress())
            .withNumericValue(
                "metadataEntry.scopedMetadataKey", criteria.getScopedMetadataKey().longValue())
            .withAddress("metadataEntry.sourceAddress", criteria.getSourceAddress())
            .withNumericValue("metadataEntry.metadataType", criteria.getMetadataType().getValue())
            .withNumericHexValue("metadataEntry.targetId", criteria.getTargetId());
    return builder.build();
  }

  public List<Metadata> search(final MetadataSearchCriteria criteria) {
    return catapultCollection.findR(toBsonFilter(criteria), context.getDatabaseTimeoutInSeconds());
  }
}
