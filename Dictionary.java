import java.util.NoSuchElementException;

/**
 * An interface that specifies a data dictionary type (i.e., a data structure
 * type that has support for storing key-value pairs). The type of key chosen
 * should have its own well-performing hashCode() method.
 * 
 * @author Alexander Breen <alexander.breen@gmail.com>
 *
 * @param <Key> The type of the keys for this hash table
 * @param <Value> The type of the values for this hash table
 */
public interface Dictionary<Key, Value> {
    public boolean add(Key k, Value v);
    public void remove(Key k) throws NoSuchElementException;
    public boolean hasKey(Key k);
    public Value get(Key k) throws NoSuchElementException;
}
