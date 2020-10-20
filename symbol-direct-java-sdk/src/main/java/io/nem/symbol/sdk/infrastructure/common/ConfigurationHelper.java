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

package io.nem.symbol.sdk.infrastructure.common;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;
import java.time.Duration;

public class ConfigurationHelper {

  private ConfigurationHelper() {}

  public static String removeSingleQuotation(final String value) {
    final String newValue = value.trim().replaceAll("'", "");
    if (newValue.isEmpty()) {
      throw new IllegalArgumentException("Property value cannot be empty :" + value);
    }
    return newValue;
  }

  public static BigInteger toBigInteger(final String value) {
    return new BigInteger(removeSingleQuotation(value));
  }

  public static Long toLong(final String value) {
    return Long.parseLong(removeSingleQuotation(value));
  }

  public static Integer toInteger(final String value) {
    return Integer.parseInt(removeSingleQuotation(value));
  }

  public static Pair<Long, Character> getTimeValueAndUnit(final String value) {
    if (value.length() < 2) {
      throw new IllegalArgumentException(value + " does not have value or unit.");
    }
    final char unit = value.charAt(value.length() - 1);
    final long val = toLong(value.substring(0, value.length() - 1));
    return Pair.of(val, unit);
  }

  public static Long toSeconds(final String value) {
    Pair<Long, Character> valueUnit = getTimeValueAndUnit(value);
    return toDuration(valueUnit.getKey(), valueUnit.getValue()).getSeconds();
  }

  public static Duration toDuration(final String value) {
    Pair<Long, Character> valueUnit = getTimeValueAndUnit(value);
    return toDuration(valueUnit.getKey(), valueUnit.getValue());
  }
  public static Duration toDuration(final long value, final char unit) {
    switch (unit) {
      case 's':
        return Duration.ofSeconds(value);
      case 'm':
        return Duration.ofMinutes(value);
      case 'h':
        return Duration.ofHours(value);
      case 'd':
        return Duration.ofDays(value);
      default:
        throw new IllegalArgumentException("Unit is not found:" + unit);
    }
  }

  public static Long toBlocks(final String value, final String blockGenerationTargetTimeValue) {
    final int blockGenerationTargetTime =
            toSeconds(blockGenerationTargetTimeValue).intValue();
    Pair<Long, Character> valueUnit = getTimeValueAndUnit(value);
    return toDuration(valueUnit.getKey(), valueUnit.getValue()).getSeconds()
        / blockGenerationTargetTime;
  }

  public static String toHex(final String value) {
    return removeSingleQuotation(value).replaceAll("0x", "");
  }
}
