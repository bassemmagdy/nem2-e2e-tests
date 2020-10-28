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
import io.nem.symbol.core.utils.ByteUtils;
import io.nem.symbol.core.utils.ConvertUtils;
import io.nem.symbol.sdk.model.mosaic.IllegalIdentifierException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Id generator
 */
public class IdGeneratorNoCheck {

    /**
     * Private constructor for this utility class.
     */
    private IdGeneratorNoCheck() {
    }

    private static final long ID_GENERATOR_FLAG = 0x8000000000000000L;

    /**
     * Generate mosaic id.
     *
     * @param nonce Nonce bytes.
     * @param publicKey Public key.
     * @return Mosaic id.
     */
    public static BigInteger generateMosaicId(final byte[] nonce, final byte[] publicKey) {
        final byte[] reverseNonce = reverseCopy(nonce);
        final byte[] hash = IdGeneratorNoCheck.getHashInLittleEndian(reverseNonce, publicKey);
        // Unset the high bit for mosaic id
        return BigInteger.valueOf(ByteBuffer.wrap(hash).getLong() & ~ID_GENERATOR_FLAG);
    }

    /**
     * Generate namespace id.
     *
     * @param namespaceName Namespace name.
     * @param parentId Parent id.
     * @return Namespace id.
     */
    public static BigInteger generateNamespaceId(final String namespaceName,
                                                 final BigInteger parentId) {
        final ByteBuffer parentIdBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
                .putLong(parentId.longValue());
        final byte[] hash = IdGeneratorNoCheck
                .getHashInLittleEndian(parentIdBuffer.array(), namespaceName.getBytes());
        // Set the high bit for namespace id
        return ConvertUtils
                .toUnsignedBigInteger(ByteBuffer.wrap(hash).getLong() | ID_GENERATOR_FLAG);
    }

    /**
     * Generate namespace id.
     *
     * @param namespaceName Namespace name.
     * @param parentNamespaceName Parent name.
     * @return Namespace id.
     */
    public static BigInteger generateNamespaceId(String namespaceName, String parentNamespaceName) {
        return IdGeneratorNoCheck.generateNamespaceId(parentNamespaceName + "." + namespaceName);
    }

    /**
     * Generate namespace id.
     *
     * @param namespacePath Namespace path.
     * @return Namespace id.
     */
    public static BigInteger generateNamespaceId(String namespacePath) {
        List<BigInteger> namespaceList = generateNamespacePath(namespacePath);
        return namespaceList.get(namespaceList.size() - 1);
    }

    /**
     * Generate namespace id.
     *
     * @param namespacePath Namespace path.
     * @return List of namespace id.
     */
    public static List<BigInteger> generateNamespacePath(String namespacePath) {
        String[] parts = namespacePath.split(Pattern.quote("."));
        List<BigInteger> path = new ArrayList<>();

        if (parts.length == 0) {
            throw new IllegalIdentifierException("invalid namespace path");
        }

        BigInteger namespaceId = BigInteger.valueOf(0);

        for (String part : parts) {
            namespaceId = generateNamespaceId(part, namespaceId);
            path.add(namespaceId);
        }
        return path;
    }

    /**
     * Gets hash in little endian.
     *
     * @param inputs Inputs to hash.
     * @return Hash value.
     */
    private static byte[] getHashInLittleEndian(final byte[]... inputs) {
        byte[] result = Hashes.sha3_256(inputs);
        result = Arrays.copyOfRange(result, 0, 8);
        ArrayUtils.reverse(result);
        return result;
    }



    /**
     * Reverse and copy to a new array.
     *
     * @param array Array to copy.
     * @return Reverse array.
     */
    public static byte[] reverseCopy(final byte[] array) {
        final byte[] reverseArray = new byte[array.length];

        for (int i = 0, j = array.length - 1; i < array.length; i++, j--) {
            reverseArray[j] = array[i];
        }
        return reverseArray;
    }
}
