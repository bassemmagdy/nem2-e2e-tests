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

package io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.mappers;

import io.nem.symbol.core.crypto.PublicKey;
import io.nem.symbol.sdk.model.account.*;
import io.nem.symbol.sdk.model.mosaic.ResolvedMosaic;
import io.vertx.core.json.JsonObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Account info mapper. */
public class AccountInfoMapper implements Function<JsonObject, AccountInfo> {
  /**
   * Converts a json object to account info.
   *
   * @param jsonObject Json object.
   * @return Account info.
   */
  public AccountInfo apply(final JsonObject jsonObject) {
    final String id = MapperUtils.toRecordId(jsonObject);
    final JsonObject accountJsonObject = jsonObject.getJsonObject("account");
    final Address address = Address.createFromEncoded(accountJsonObject.getString("address"));
    final BigInteger addressHeight = MapperUtils.toBigInteger(accountJsonObject, "addressHeight");
    final BigInteger publicHeight = MapperUtils.toBigInteger(accountJsonObject, "publicKeyHeight");
    final ImportancesMapper importancesMapper = new ImportancesMapper();
    final List<Importance> importances =
        accountJsonObject.getJsonArray("importances").stream()
            .map(jsonObj -> importancesMapper.apply((JsonObject) jsonObj))
            .collect(Collectors.toList());
    final Importance importance =
        importances.size() > 0
            ? importances.get(0)
            : new Importance(BigInteger.ZERO, BigInteger.ZERO);

    final List<ResolvedMosaic> resolvedMosaics =
        accountJsonObject.getJsonArray("mosaics").stream()
            .map(jsonObj -> new ResolvedMosaicMapper().apply((JsonObject) jsonObj))
            .collect(Collectors.toList());
    final List<ActivityBucket> activityBuckets = new ArrayList<>();
    final SupplementalAccountKeys accountKeys =
        getSupplementalAccountKeys(accountJsonObject.getJsonObject("supplementalPublicKeys"));
    return new AccountInfo(
        id,
        MapperUtils.getStateVersion(accountJsonObject),
        address,
        addressHeight,
        MapperUtils.toPublicKey(accountJsonObject, "publicKey"),
        publicHeight,
        importance.getValue(),
        importance.getHeight(),
        resolvedMosaics,
        AccountType.UNLINKED,
        accountKeys,
        activityBuckets);
  }

  private SupplementalAccountKeys getSupplementalAccountKeys(final JsonObject jsonObject) {
    PublicKey vrfPublickey = null;
    PublicKey linkedPublickey = null;
    PublicKey nodePublickey = null;
    List<AccountLinkVotingKey> voting = new ArrayList<>();
    if (jsonObject.containsKey("vrf")) {
      final JsonObject vrfJsonObject = jsonObject.getJsonObject("vrf");
      vrfPublickey = MapperUtils.toPublicKey(vrfJsonObject, "publicKey");
    }

    if (jsonObject.containsKey("node")) {
      final JsonObject nodeJsonObject = jsonObject.getJsonObject("node");
      nodePublickey = MapperUtils.toPublicKey(nodeJsonObject, "publicKey");
    }

    if (jsonObject.containsKey("voting")) {
      final JsonObject votingJsonObject = jsonObject.getJsonObject("voting");
      final VotingKeyMapper votingKeyMapper = new VotingKeyMapper();
      voting =
          votingJsonObject.getJsonArray("publicKeys").stream()
              .map(v -> votingKeyMapper.apply((JsonObject) v))
              .collect(Collectors.toList());
    }

    if (jsonObject.containsKey("linked")) {
      final JsonObject linkedJsonObject = jsonObject.getJsonObject("linked");
      linkedPublickey = MapperUtils.toPublicKey(linkedJsonObject, "publicKey");
    }

    return new SupplementalAccountKeys(linkedPublickey, nodePublickey, vrfPublickey, voting);
  }
}
