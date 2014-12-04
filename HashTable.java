import java.util.NoSuchElementException;

/**
 * A plain implementation of a hash table. This implementation uses
 * quadratic probing to resolve collisions. It makes use of the Pair class
 * to store key-value pairs in the underlying array.
 * 
 * @author Alexander Breen <alexander.breen@gmail.com>
 * 
 * @see Pair
 */
public class HashTable<Key, Value> implements Dictionary<Key, Value> {
    
    /**
     * An array of prime numbers such that each prime is roughly twice the
     * size of the previous prime. These are used as underlying array sizes;
     * using prime numbers reduces the probability that keys hash to the
     * same index in the array.
     */
    private static final int[] PRIMES = {
        53, 97, 193, 389, 769, 1543, 3079, 6151, 12289, 24593, 49157,
        98317, 196613, 393241, 786433, 1572869
    };
    
    private static final int DEFAULT_SIZE = PRIMES[0];
    
    /**
     * Underlying array containing key-value pairs.
     */
    private Pair<Key, Value>[] buckets;
    
    /**
     * A local count of how many items are in the hash table,
     * for quick determinations that the table is full.
     */
    private int numPairs;
    
    /**
     * A Boolean array keeping track of whether a particular bucket
     * previously had an item in it. This is needed for keeping duplicates
     * out of the table, since we're using quadratic probing to resolve
     * collisions.
     */
    private boolean[] removed;
    
    public HashTable() {
        this(DEFAULT_SIZE);
    }
    
    @SuppressWarnings("unchecked")
    public HashTable(int initialSize) {
        this.buckets = new Pair[initialSize];
        this.numPairs = 0;
        
        this.removed = new boolean[initialSize];
    }
    
    /**
     * Called when the underlying array is full and needs to be doubled in
     * size. This method will re-hash every item in the old array to obtain
     * indices in the new array. If the underlying array cannot be made any
     * larger due to the PRIMES array being exhausted, this method returns
     * false. Otherwise, it returns true.
     */
    @SuppressWarnings("unchecked")
    private boolean resize() {
        int nextIndex;
        for (nextIndex = 0;
             nextIndex < PRIMES.length && PRIMES[nextIndex] <= buckets.length;
             nextIndex++);
        
        if (nextIndex == PRIMES.length)
            return false;
        
        int newSize = PRIMES[nextIndex];
        
        Pair<Key, Value>[] oldBuckets = buckets;
        buckets = new Pair[newSize];
        removed = new boolean[newSize];
        numPairs = 0;
        
        for (int i = 0; i < oldBuckets.length; i++)
            if (oldBuckets[i] != null)
                add(oldBuckets[i].first, oldBuckets[i].second);
        
        return true;
    }
    
    public boolean add(Key k, Value v) {
        if (numPairs == buckets.length)
            if (!resize())
                return false;
        
        int hash = hash(k);
        
        // implementation note: using quadratic probing to resolve duplicates
        int removedIndex = -1;
        int n = 0;
        int index = hash % buckets.length;
        
        while (buckets[index] != null || removed[index]) {
            if (removed[index] && removedIndex == -1)
                removedIndex = index;
            
            if (buckets[index] != null &&
                hash(buckets[index].first) == hash)
            {
                // found a duplicate
                buckets[index].second = v;
                return true;
            }
            
            n++;
            index = (hash + n) % buckets.length;
        }
        
        /*
         * If we get here, we didn't find a duplicate value on the way
         * through the hash table. If we have an index for a removed bucket
         * and we reached an empty bucket, we can safely put this value
         * into the removed bucket and reverse the removed flag.
         * 
         * Otherwise, we didn't find any duplicates and all other buckets
         * were occupied, and we have the next free bucket.
         */
        if (removedIndex != -1) {
            buckets[removedIndex] = new Pair<Key, Value>(k, v);
            removed[removedIndex] = false;
            
        } else {
            buckets[index] = new Pair<Key, Value>(k, v);
        }
        
        numPairs++;
        
        return true;
    }
    
    public void remove(Key k) throws NoSuchElementException {
        int index = indexOf(k);
        
        if (index == -1) {
            throw new NoSuchElementException();
        } else {
            buckets[index] = null;
            removed[index] = true;
        }
    }
    
    public boolean hasKey(Key k) {
        return indexOf(k) != -1;
    }

    public Value get(Key k) throws NoSuchElementException {
        int index = indexOf(k);
        
        if (index == -1)
            throw new NoSuchElementException();
        else
            return buckets[index].second;
    }
    
    @SuppressWarnings("unchecked")
    public Value[] values() {
        Value[] arr = (Value[]) new Object[numPairs];
        int j = 0;
        
        for (int i = 0; i < buckets.length; i++)
            if (buckets[i] != null)
                arr[j++] = (Value)buckets[i].second;
        
        return arr;
    }
    
    /**
     * Returns a string representation of the hash table. Uses Python-like
     * style to produce a list of key-value pairs in whatever ordering they
     * happen to take in the underlying array.
     * 
     * @return A string representing the hash table
     */
    public String toString() {
        String s = "{";
        
        int i = 0;
        while (i < buckets.length && buckets[i] == null) i++;
        
        if (i == buckets.length)
            return s + "}";
        
        s += buckets[i];
        i++;
        
        while (i < buckets.length) {
            if (buckets[i] != null)
                s += ", " + buckets[i];
            i++;
        }
        
        return s + "}";
    }
    
    private int hash(Object o) {
        return Math.abs(o.hashCode());
    }
    
    private int indexOf(Key k) {
        int hash = hash(k);
        
        int n = 0;
        int index = hash % buckets.length;
        
        while (buckets[index] != null || removed[index]) {
            if (buckets[index] != null &&
                hash(buckets[index].first) == hash)
            {
                // found item
                return index;
            }
            
            n++;
            index = (hash + n) % buckets.length;
        }
        
        return -1;
    }
    
    public static void main(String[] args) {
        HashTable<String, Integer> table = new HashTable<String, Integer>();
        
        for (int i = 0; i < 10; i++)
            table.add("key" + i, i);
        
        Object[] values = table.values();
        
        // test that all values made it into the table
        for (int i = 0; i < 10; i++)
            assert(indexOf(values, i) != -1);
        
        // causing a resize (should resize to 97)
        for (int i = 10; i < 100; i++)
            table.add("key" + i, i);
        
        values = table.values();
        
        // testing that resize works
        for (int i = 0; i < 100; i++)
            assert(indexOf(values, i) != -1);
        
        // causing two resizes
        for (int i = 100; i < 600; i++)
            table.add("key" + i, i);
        
        values = table.values();
        
        // test of resizing twice
        for (int i = 0; i < 600; i++)
            assert(indexOf(values, i) != -1);
    }
    
    private static int indexOf(Object[] arr, Object o) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i].equals(o))
                return i;
        
        return -1;
    }
}
