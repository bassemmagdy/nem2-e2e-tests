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

import com.mongodb.client.model.Filters;
import io.nem.symbol.sdk.api.NamespaceSearchCriteria;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.common.DataAccessContext;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.mappers.NamespacesMapper;
import io.nem.symbol.sdk.model.namespace.NamespaceInfo;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NamespacesCollection {
  /* Catapult context. */
  final DataAccessContext context;
  /** Catapult collection */
  private final CatapultCollection<NamespaceInfo, NamespacesMapper> catapultCollection;

  /**
   * Constructor.
   *
   * @param context Catapult context.
   */
  public NamespacesCollection(final DataAccessContext context) {
    this.context = context;
    catapultCollection =
        new CatapultCollection<>(
            context.getCatapultMongoDbClient(), "namespaces", NamespacesMapper::new);
  }

  /**
   * Gets namespace info.
   *
   * @param namespaceId Namespace id.
   * @return Namespace info.
   */
  public Optional<NamespaceInfo> findById(final long namespaceId) {
    return findById(namespaceId, context.getDatabaseTimeoutInSeconds());
  }

  /**
   * Gets namespace info.
   *
   * @param namespaceId Namespace id.
   * @param timeoutInSeconds Timeout in seconds.
   * @return Namespace info.
   */
  public Optional<NamespaceInfo> findById(final long namespaceId, final int timeoutInSeconds) {
    final String keyLevelName = "namespace.level";
    final String keyDepthName = "namespace.depth";
    final int maxDepth = 3;

    final List<Bson> filters = new ArrayList<>(maxDepth);
    for (int i = 0; i < maxDepth; ++i) {
      filters.add(
          Filters.and(Filters.eq(keyLevelName + i, namespaceId), Filters.eq(keyDepthName, i + 1)));
    }
    final List<Document> results =
        catapultCollection.find(
            addFilterActiveTrueCondition(Filters.or(filters)), timeoutInSeconds);
    final List<NamespaceInfo> namespaceInfos =
        getActiveNamespace(catapultCollection.ConvertResult(results));
    return catapultCollection.GetOneResult(namespaceInfos);
  }

  private List<NamespaceInfo> getActiveNamespace(final List<NamespaceInfo> namespaceInfos) {
    final BigInteger chainHeight = new ChainStatisticCollection(context).get().getNumBlocks();
    return namespaceInfos.stream()
        .filter(
            n ->
                (n.getEndHeight().longValue() == -1)
                    || (n.getEndHeight().longValue() > chainHeight.longValue()))
        .collect(Collectors.toList());
  }

  /**
   * Add active true condition to the current filter.
   *
   * @param filter Current filter.
   * @return Combined filter.
   */
  private Bson addFilterActiveTrueCondition(final Bson filter) {
    final String keyActiveName = "meta.latest";
    return Filters.and(Filters.eq(keyActiveName, true), filter);
  }

  private Bson toSearchCriteria(final NamespaceSearchCriteria criteria) {
    final MongoDbFilterBuilder builder =
        new MongoDbFilterBuilder()
            .withIsActive()
            .withAddress("namespace.ownerAddress", criteria.getOwnerAddress())
            .withNumericHexValue("namespace.level0", criteria.getLevel0());
    if (criteria.getRegistrationType() != null) {
      builder.withNumericValue(
          "namespace.registrationType", criteria.getRegistrationType().getValue());
    }
    if (criteria.getAliasType() != null) {
      builder.withNumericValue("namespace.alias.type", criteria.getAliasType().getValue());
    }
    return builder.build();
  }

  public List<NamespaceInfo> search(final NamespaceSearchCriteria criteria) {
    return getActiveNamespace(
        catapultCollection.findR(
            toSearchCriteria(criteria), context.getDatabaseTimeoutInSeconds()));
  }
}
