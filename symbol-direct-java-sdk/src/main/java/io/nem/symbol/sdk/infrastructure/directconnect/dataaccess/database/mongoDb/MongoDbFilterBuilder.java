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
import io.nem.symbol.core.crypto.PublicKey;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.mappers.MapperUtils;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.mosaic.MosaicId;
import org.apache.commons.lang3.Validate;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MongoDbFilterBuilder {

  List<Bson> filters = new ArrayList<>();

  private byte[] getAddressBytes(final Address address) {
    return MapperUtils.fromAddressToByteBuffer(address).array();
  }

  private long fromHexId(final String id) {
    return new BigInteger(id, 16).longValue();
  }

  private void validateKeyName(final String keyName) {
    Validate.notNull(keyName, "keyName must not be null");
  }

  public Bson build() {
    return Filters.and(filters);
  }

  public MongoDbFilterBuilder withAddress(final String keyName, final Address address) {
    validateKeyName(keyName);
    if (address != null) {
      withBinaryValue(keyName, getAddressBytes(address));
    }
    return this;
  }

  public MongoDbFilterBuilder withMetaAddress(final Address address) {
    return withAddress("meta.addresses", address);
  }

  public MongoDbFilterBuilder withIfExists(final String keyName) {
    validateKeyName(keyName);
    filters.add(Filters.exists(keyName));
    return this;
  }

  public MongoDbFilterBuilder withNoEmbeddedTransaction() {
    return withIfExists("meta.hash");
  }

  public <T> MongoDbFilterBuilder withIn(final String keyName, final List<T> values) {
    validateKeyName(keyName);
    if (values != null) {
      filters.add(Filters.in(keyName, values));
    }
    return this;
  }

  public MongoDbFilterBuilder withBinaryValue(final String keyName, final byte[] objectBytes) {
    validateKeyName(keyName);
    if (objectBytes != null) {
      filters.add(Filters.eq(keyName, new Binary((byte) 0, objectBytes)));
    }
    return this;
  }

  public MongoDbFilterBuilder withPublicKey(final String keyName, final PublicKey publicKey) {
    validateKeyName(keyName);
    if (publicKey != null) {
      withBinaryValue(keyName, publicKey.getBytes());
    }
    return this;
  }

  public MongoDbFilterBuilder withNumericValue(final String keyName, final long keyValue) {
    validateKeyName(keyName);
    filters.add(Filters.eq(keyName, keyValue));
    return this;
  }

  public MongoDbFilterBuilder withNumericHexValue(final String keyName, final String keyValue) {
    validateKeyName(keyName);
    if (keyValue != null) {
      withNumericValue(keyName, fromHexId(keyValue));
    }
    return this;
  }

  public MongoDbFilterBuilder withBooleanValue(final String keyName, final boolean value) {
    validateKeyName(keyName);
    filters.add(Filters.eq(keyName, value));
    return this;
  }

  public MongoDbFilterBuilder withIsActive() {
    withBooleanValue("meta.latest", true);
    return this;
  }

  public MongoDbFilterBuilder withMosaic(final String keyName, final String keyValue) {
    validateKeyName(keyName);
    if (keyValue != null) {
      withNumericValue(keyName, new MosaicId(keyValue).getIdAsLong());
    }
    return this;
  }
}
