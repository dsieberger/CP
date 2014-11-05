package cp.articlerep.ds;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Ricardo Dias
 */
public interface Map<K extends Comparable<K>, V>
{
    public V put(K key, V value);

    public boolean contains(K key);

    public V remove(K key);

    public V get(K key);

    public void getWriteLockItem(K key);
    
    public void getReadLockItem(K key);
    
    public void releaseWriteLockItem(K key);
    
    public void releaseReadLockItem(K key);

    public Iterator<V> values();

    public Iterator<K> keys();
}
