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

import io.nem.symbol.sdk.model.mosaic.UnresolvedMosaicId;
import org.apache.commons.lang3.Validate;

import java.math.BigInteger;

/**
 * A mosaic describes an instance of a mosaic definition. Mosaics can be transferred by means of a
 * transfer transaction.
 *
 * @since 1.0
 */
public class MosaicNoCheck {

    private final UnresolvedMosaicId id;

    private final BigInteger amount;

    public MosaicNoCheck(UnresolvedMosaicId id, BigInteger amount) {
        Validate.notNull(id, "Id must not be null");
        Validate.notNull(amount, "Amount must not be null");
        this.id = id;
        this.amount = amount;
    }

    /**
     * Returns the mosaic identifier
     *
     * @return mosaic identifier
     */
    public UnresolvedMosaicId getId() {
        return id;
    }

    /**
     * Returns mosaic id as a hexadecimal string
     *
     * @return id hex string
     */
    public String getIdAsHex() {
        return id.getIdAsHex();
    }

    /**
     * Return mosaic amount. The quantity is always given in smallest units for the mosaic i.e. if
     * it has a divisibility of 3 the quantity is given in millis.
     *
     * @return amount of mosaic
     */
    public BigInteger getAmount() {
        return amount;
    }
}