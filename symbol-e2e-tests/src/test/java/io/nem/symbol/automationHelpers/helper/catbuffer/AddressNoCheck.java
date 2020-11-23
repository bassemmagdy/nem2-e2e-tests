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

package io.nem.symbol.automationHelpers.helper.catbuffer;

import io.nem.symbol.core.crypto.Hashes;
import io.nem.symbol.core.crypto.PublicKey;
import io.nem.symbol.core.crypto.RawAddress;
import io.nem.symbol.core.utils.Base32Encoder;
import io.nem.symbol.core.utils.ConvertUtils;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.account.UnresolvedAddress;
import io.nem.symbol.sdk.model.network.NetworkType;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * The address structure describes an address with its network.
 *
 * @since 1.0
 */
public class AddressNoCheck implements UnresolvedAddress {

    /**
     * The plain address size.
     */
    private static final int PLAIN_ADDRESS_SIZE = 39;

    /**
     * The raw address size.
     */
    private static final int RAW_ADDRESS_SIZE = 24;

    /**
     * The checksum size.
     */
    private static final int CHECKSUM_SIZE = RawAddress.NUM_CHECKSUM_BYTES;

    private final String plainAddress;

    private final NetworkType networkType;

    /**
     * Constructor
     *
     * @param prettyOrRaw The address in pretty or raw.
     * @param networkType Network type
     */
    public AddressNoCheck(String prettyOrRaw, NetworkType networkType) {
        this.plainAddress = toPlainAddress(Validate.notNull(prettyOrRaw, "address must not be null"));
        this.networkType = Objects.requireNonNull(networkType, "networkType must not be null");
    }

    /**
     * It normalizes a plain or pretty address into an upercase plain address.
     *
     * @param address the plain or pretty address.
     * @return the plain address.
     */
    private static String toPlainAddress(String address) {
        return address.trim().toUpperCase().replace("-", "");
    }

    /**
     * Create an Address from a given raw address.
     *
     * @param rawAddress String
     * @return {@link AddressNoCheck}
     */
    public static AddressNoCheck createFromRawAddress(String rawAddress, NetworkType networkType) {
        return new AddressNoCheck(rawAddress, networkType);
    }


    /**
     * Get address in plain format ex: SB3KUBHATFCPV7UZQLWAQ2EUR6SIHBSBEOEDDDF3.
     *
     * @return String
     */
    public String plain() {
        return this.plainAddress;
    }

    /**
     * Returns network type.
     *
     * @return {@link NetworkType}
     */
    public NetworkType getNetworkType() {
        return networkType;
    }


    /**
     * Returns the encoded address.
     *
     * @param networkType the network type.
     * @return the encoded plain address.
     */
    @Override
    public String encoded(NetworkType networkType) {
        return encoded();
    }

    /**
     * Returns the encoded address.
     *
     * @return the encoded plain address.
     */
    public String encoded() {
        return fromPlainToEncoded(plain());
    }


    /**
     * Get address in pretty format ex: SB3KUB-HATFCP-V7UZQL-WAQ2EU-R6SIHB-SBEOED-DDF3.
     *
     * @return String
     */
    public String pretty() {
        return this.plainAddress.replaceAll("(.{6})", "$1-");
    }

    /**
     * Concert a encoded address to a plain one
     *
     * @param plain the plain address.
     * @return the encoded address.
     */
    private static String fromPlainToEncoded(String plain) {
        byte[] bytes = Base32Encoder.getBytes(plain);
        return ConvertUtils.toHex(bytes);
    }

    /**
     * Concerts an encoded to a plain one
     *
     * @param encoded the encoded address.
     * @return the encoded address.
     */
    private static String fromEncodedToPlain(String encoded) {
        byte[] bytes = ConvertUtils.fromHexToBytes(encoded);
        String rawAddress = Base32Encoder.getString(bytes);
        return rawAddress.substring(0, rawAddress.length() - 1);
    }

    /**
     * Compares addresses for equality.
     *
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddressNoCheck)) {
            return false;
        }
        AddressNoCheck address1 = (AddressNoCheck) o;
        return Objects.equals(plainAddress, address1.plainAddress) && networkType == address1.networkType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(plainAddress, networkType);
    }

}
