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
                spread ^= spread >>> 15;
                spread *= 0xACAB2A4D;
                spread ^= spread >>> 15;
                spread *= 0x5CC7DF53;
                spread ^= spread >>> 12;
                return spread;
            case SECONDARY:
                spread ^= spread >>> 14;
                spread *= 0xBA1CCD33;
                spread ^= spread >>> 13;
                spread *= 0x9B6296CB;
                spread ^= spread >>> 12;
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
                spread ^= spread >>> 28;
                spread *= -4254747342703917655L;
                spread ^= spread >>> 43;
                spread *= -908430792394475837L;
                spread ^= spread >>> 23;
                return spread;
            case SECONDARY:
                spread ^= spread >>> 23;
                spread *= -6261870919139520145L;
                spread ^= spread >>> 39;
                spread *= 2747051607443084853L;
                spread ^= spread >>> 37;
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
