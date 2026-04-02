/*
 * Copyright (c) 2018 Goldman Sachs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.eclipse.collections.impl.map.mutable;

@SuppressWarnings("ObjectEquality")
public abstract class AbstractUnifiedMap<K, V> extends AbstractMutableMap<K, V>
{
    protected static final Object NULL_KEY = new Object()
    {
        @Override
        public boolean equals(Object obj)
        {
            throw new RuntimeException("Possible corruption through unsynchronized concurrent modification.");
        }

        @Override
        public int hashCode()
        {
            throw new RuntimeException("Possible corruption through unsynchronized concurrent modification.");
        }

        @Override
        public String toString()
        {
            return "AbstractUnifiedMap.NULL_KEY";
        }
    };

    protected static final Object CHAINED_KEY = new Object()
    {
        @Override
        public boolean equals(Object obj)
        {
            throw new RuntimeException("Possible corruption through unsynchronized concurrent modification.");
        }

        @Override
        public int hashCode()
        {
            throw new RuntimeException("Possible corruption through unsynchronized concurrent modification.");
        }

        @Override
        public String toString()
        {
            return "AbstractUnifiedMap.CHAINED_KEY";
        }
    };

    protected static final float DEFAULT_LOAD_FACTOR = 0.75f;

    protected static final int DEFAULT_INITIAL_CAPACITY = 8;

    protected transient Object[] table;

    protected transient int occupied;

    protected float loadFactor = DEFAULT_LOAD_FACTOR;

    protected int maxSize;

    protected final int fastCeil(float value)
    {
        int possibleResult = (int) value;
        if (value - possibleResult > 0.0F)
        {
            possibleResult++;
        }
        return possibleResult;
    }

    protected final int init(int initialCapacity)
    {
        int capacity = 1;
        while (capacity < initialCapacity)
        {
            capacity <<= 1;
        }

        return this.allocate(capacity);
    }

    protected final int allocate(int capacity)
    {
        this.allocateTable(capacity << 1);
        this.computeMaxSize(capacity);
        return capacity;
    }

    protected void allocateTable(int sizeToAllocate)
    {
        this.table = new Object[sizeToAllocate];
    }

    protected void computeMaxSize(int capacity)
    {
        this.maxSize = Math.min(capacity - 1, (int) (capacity * this.loadFactor));
    }

    protected final int index(Object key)
    {
        int h = this.computeHashCode(key);
        h ^= h >>> 20 ^ h >>> 12;
        h ^= h >>> 7 ^ h >>> 4;
        return (h & (this.table.length >> 1) - 1) << 1;
    }

    @Override
    public void clear()
    {
        if (this.occupied == 0)
        {
            return;
        }
        this.occupied = 0;
        Object[] currentTable = this.table;

        for (int i = currentTable.length; i-- > 0; )
        {
            currentTable[i] = null;
        }
    }

    @Override
    public V put(K key, V value)
    {
        int index = this.index(key);
        Object currentKey = this.table[index];
        if (currentKey == null)
        {
            this.table[index] = AbstractUnifiedMap.toSentinelIfNull(key);
            this.table[index + 1] = value;
            this.incrementOccupiedAndRehashIfNecessary();
            return null;
        }
        if (currentKey != CHAINED_KEY && this.nonNullTableObjectEquals(currentKey, key))
        {
            V result = (V) this.table[index + 1];
            this.table[index + 1] = value;
            return result;
        }
        return this.putInChain(key, index, value);
    }

    protected V putInChain(K key, int index, V value)
    {
        if (this.table[index] == CHAINED_KEY)
        {
            Object[] chain = (Object[]) this.table[index + 1];
            for (int i = 0; i < chain.length; i += 2)
            {
                if (chain[i] == null)
                {
                    chain[i] = AbstractUnifiedMap.toSentinelIfNull(key);
                    chain[i + 1] = value;
                    this.incrementOccupiedAndRehashIfNecessary();
                    return null;
                }
                if (this.nonNullTableObjectEquals(chain[i], key))
                {
                    V result = (V) chain[i + 1];
                    chain[i + 1] = value;
                    return result;
                }
            }
            Object[] newChain = new Object[chain.length + 4];
            System.arraycopy(chain, 0, newChain, 0, chain.length);
            this.table[index + 1] = newChain;
            newChain[chain.length] = AbstractUnifiedMap.toSentinelIfNull(key);
            newChain[chain.length + 1] = value;
            this.incrementOccupiedAndRehashIfNecessary();
            return null;
        }
        Object[] newChain = new Object[4];
        newChain[0] = this.table[index];
        newChain[1] = this.table[index + 1];
        newChain[2] = AbstractUnifiedMap.toSentinelIfNull(key);
        newChain[3] = value;
        this.table[index] = CHAINED_KEY;
        this.table[index + 1] = newChain;
        this.incrementOccupiedAndRehashIfNecessary();
        return null;
    }

    protected final void incrementOccupiedAndRehashIfNecessary()
    {
        if (++this.occupied > this.maxSize)
        {
            this.rehash(this.table.length);
        }
    }

    protected void rehash(int newCapacity)
    {
        int oldLength = this.table.length;
        Object[] old = this.table;
        this.allocate(newCapacity);
        this.occupied = 0;

        for (int i = 0; i < oldLength; i += 2)
        {
            Object currentKey = old[i];
            if (currentKey == CHAINED_KEY)
            {
                Object[] chain = (Object[]) old[i + 1];
                for (int j = 0; j < chain.length; j += 2)
                {
                    if (chain[j] != null)
                    {
                        this.put(this.nonSentinel(chain[j]), (V) chain[j + 1]);
                    }
                }
            }
            else if (currentKey != null)
            {
                this.put(this.nonSentinel(currentKey), (V) old[i + 1]);
            }
        }
    }

    @Override
    public V get(Object key)
    {
        int index = this.index(key);
        Object currentKey = this.table[index];
        if (currentKey != null)
        {
            Object value = this.table[index + 1];
            if (currentKey == CHAINED_KEY)
            {
                return this.getFromChain((Object[]) value, (K) key);
            }
            if (this.nonNullTableObjectEquals(currentKey, (K) key))
            {
                return (V) value;
            }
        }
        return null;
    }

    protected V getFromChain(Object[] chain, K key)
    {
        for (int i = 0; i < chain.length; i += 2)
        {
            Object currentKey = chain[i];
            if (currentKey == null)
            {
                return null;
            }
            if (this.nonNullTableObjectEquals(currentKey, key))
            {
                return (V) chain[i + 1];
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key)
    {
        int index = this.index(key);
        Object currentKey = this.table[index];
        if (currentKey == null)
        {
            return false;
        }
        if (currentKey != CHAINED_KEY && this.nonNullTableObjectEquals(currentKey, (K) key))
        {
            return true;
        }
        return currentKey == CHAINED_KEY && this.chainContainsKey((Object[]) this.table[index + 1], (K) key);
    }

    protected boolean chainContainsKey(Object[] chain, K key)
    {
        for (int i = 0; i < chain.length; i += 2)
        {
            Object currentKey = chain[i];
            if (currentKey == null)
            {
                return false;
            }
            if (this.nonNullTableObjectEquals(currentKey, key))
            {
                return true;
            }
        }
        return false;
    }

    protected final K nonSentinel(Object key)
    {
        return key == NULL_KEY ? null : (K) key;
    }

    protected static Object toSentinelIfNull(Object key)
    {
        return key == null ? NULL_KEY : key;
    }

    protected final boolean nonNullTableObjectEquals(Object cur, K key)
    {
        return cur == key || (cur == NULL_KEY ? key == null : this.keysEqual(this.nonSentinel(cur), key));
    }

    protected abstract int computeHashCode(Object key);

    protected abstract boolean keysEqual(K left, K right);
}
