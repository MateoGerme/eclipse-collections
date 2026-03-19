/*
 * Copyright (c) 2015 Goldman Sachs.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.eclipse.collections.impl;

public final class ProbeSpreadFunctions
{
    private static final int PRIMARY_32_FIRST_SHIFT = 15;
    private static final int PRIMARY_32_FIRST_MULTIPLIER = 0xACAB2A4D;
    private static final int PRIMARY_32_SECOND_SHIFT = 15;
    private static final int PRIMARY_32_SECOND_MULTIPLIER = 0x5CC7DF53;
    private static final int PRIMARY_32_FINAL_SHIFT = 12;

    private static final int SECONDARY_32_FIRST_SHIFT = 14;
    private static final int SECONDARY_32_FIRST_MULTIPLIER = 0xBA1CCD33;
    private static final int SECONDARY_32_SECOND_SHIFT = 13;
    private static final int SECONDARY_32_SECOND_MULTIPLIER = 0x9B6296CB;
    private static final int SECONDARY_32_FINAL_SHIFT = 12;

    private static final int PRIMARY_64_FIRST_SHIFT = 28;
    private static final long PRIMARY_64_FIRST_MULTIPLIER = -4254747342703917655L;
    private static final int PRIMARY_64_SECOND_SHIFT = 43;
    private static final long PRIMARY_64_SECOND_MULTIPLIER = -908430792394475837L;
    private static final int PRIMARY_64_FINAL_SHIFT = 23;

    private static final int SECONDARY_64_FIRST_SHIFT = 23;
    private static final long SECONDARY_64_FIRST_MULTIPLIER = -6261870919139520145L;
    private static final int SECONDARY_64_SECOND_SHIFT = 39;
    private static final long SECONDARY_64_SECOND_MULTIPLIER = 2747051607443084853L;
    private static final int SECONDARY_64_FINAL_SHIFT = 37;

    public enum SpreadVariant
    {
        PRIMARY,
        SECONDARY
    }

    private ProbeSpreadFunctions()
    {
    }

    private static int thirtyTwoBitSpread(int code, SpreadVariant variant)
    {
        int spread = code;
        switch (variant)
        {
            case PRIMARY:
                spread ^= spread >>> PRIMARY_32_FIRST_SHIFT;
                spread *= PRIMARY_32_FIRST_MULTIPLIER;
                spread ^= spread >>> PRIMARY_32_SECOND_SHIFT;
                spread *= PRIMARY_32_SECOND_MULTIPLIER;
                spread ^= spread >>> PRIMARY_32_FINAL_SHIFT;
                return spread;
            case SECONDARY:
                spread ^= spread >>> SECONDARY_32_FIRST_SHIFT;
                spread *= SECONDARY_32_FIRST_MULTIPLIER;
                spread ^= spread >>> SECONDARY_32_SECOND_SHIFT;
                spread *= SECONDARY_32_SECOND_MULTIPLIER;
                spread ^= spread >>> SECONDARY_32_FINAL_SHIFT;
                return spread;
            default:
                throw new IllegalArgumentException("Unknown spread variant: " + variant);
        }
    }

    private static long sixtyFourBitSpread(long code, SpreadVariant variant)
    {
        long spread = code;
        switch (variant)
        {
            case PRIMARY:
                spread ^= spread >>> PRIMARY_64_FIRST_SHIFT;
                spread *= PRIMARY_64_FIRST_MULTIPLIER;
                spread ^= spread >>> PRIMARY_64_SECOND_SHIFT;
                spread *= PRIMARY_64_SECOND_MULTIPLIER;
                spread ^= spread >>> PRIMARY_64_FINAL_SHIFT;
                return spread;
            case SECONDARY:
                spread ^= spread >>> SECONDARY_64_FIRST_SHIFT;
                spread *= SECONDARY_64_FIRST_MULTIPLIER;
                spread ^= spread >>> SECONDARY_64_SECOND_SHIFT;
                spread *= SECONDARY_64_SECOND_MULTIPLIER;
                spread ^= spread >>> SECONDARY_64_FINAL_SHIFT;
                return spread;
            default:
                throw new IllegalArgumentException("Unknown spread variant: " + variant);
        }
    }

    public static long doubleSpread(double element, SpreadVariant variant)
    {
        long code = Double.doubleToLongBits(element);
        return ProbeSpreadFunctions.sixtyFourBitSpread(code, variant);
    }

    public static long longSpread(long element, SpreadVariant variant)
    {
        return ProbeSpreadFunctions.sixtyFourBitSpread(element, variant);
    }

    public static int intSpread(int element, SpreadVariant variant)
    {
        return ProbeSpreadFunctions.thirtyTwoBitSpread(element, variant);
    }

    public static int floatSpread(float element, SpreadVariant variant)
    {
        int code = Float.floatToIntBits(element);
        return ProbeSpreadFunctions.thirtyTwoBitSpread(code, variant);
    }

    public static int shortSpread(short element, SpreadVariant variant)
    {
        return ProbeSpreadFunctions.thirtyTwoBitSpread(element, variant);
    }

    public static int charSpread(char element, SpreadVariant variant)
    {
        return ProbeSpreadFunctions.thirtyTwoBitSpread(element, variant);
    }
}
