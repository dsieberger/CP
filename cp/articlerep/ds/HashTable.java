package cp.articlerep.ds;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

/**
 * @author Ricardo Dias
 */
public class HashTable<K extends Comparable<K>, V> implements Map<K, V>
{

     ReentrantLock[] Locks;

    private static class Node
    {
	public Object key;
	public Object value;
	public Node next;

	public Node(Object key, Object value, Node next)
	{
	    this.key = key;
	    this.value = value;
	    this.next = next;
	}
    }

    private Node[] table;

    public HashTable()
    {
	    this(1000);
        Locks = new ReentrantLock[1000];
    }

    public HashTable(int size)
    {
	    this.table = new Node[size];
        Locks = new ReentrantLock[size];
    }

    private ReentrantLock lockItem (K key, int id){
        int pos;
        if (key == null){
            pos = id ;
        } else {
            pos = this.calcTablePos(key);
        }
        ReentrantLock rwl;
        rwl = Locks[pos];
        if (rwl == null){
            rwl = new ReentrantLock();
            Locks[pos] = rwl;
        }
        return rwl;
    }

    public void getLockItem(K key, int id){
        lockItem(key,id).lock();
    }

    public void releaseLockItem(K key, int id){
        lockItem(key,id).unlock();
    }

    private int calcTablePos(K key)
    {
	return Math.abs(key.hashCode()) % this.table.length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        int pos = this.calcTablePos(key);

        Node n = this.table[pos];

        while (n != null && !n.key.equals(key)) {
            n = n.next;
        }

        if (n != null) {
            V oldValue = (V) n.value;
            n.value = value;
            return oldValue;
        }

        Node nn = new Node(key, value, this.table[pos]);
        this.table[pos] = nn;

        return null;
    }
    @SuppressWarnings("unchecked")
    @Override
    public V remove(K key)
    {
	int pos = this.calcTablePos(key);
	Node p = this.table[pos];
	if (p == null)
	{
	    return null;
	}

	if (p.key.equals(key))
	{
	    this.table[pos] = p.next;
	    return (V) p.value;
	}

	Node n = p.next;
	while (n != null && !n.key.equals(key))
	{
	    p = n;
	    n = n.next;
	}

	if (n == null)
	{
	    return null;
	}

	p.next = n.next;

	return (V) n.value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(K key)
    {
	int pos = this.calcTablePos(key);
	Node n = this.table[pos];
	while (n != null && !n.key.equals(key))
	{
	    n = n.next;
	}
	return (V) (n != null ? n.value : null);
    }

    @Override
    public boolean contains(K key)
    {
	return get(key) != null;
    }

    /**
     * No need to protect this method from concurrent interactions
     */
    @Override
    public Iterator<V> values()
    {
	return new Iterator<V>()
	{

	    private int pos = -1;
	    private Node nextBucket = advanceToNextBucket();

	    private Node advanceToNextBucket()
	    {
		pos++;
		while (pos < HashTable.this.table.length
			&& HashTable.this.table[pos] == null)
		{
		    pos++;
		}
		if (pos < HashTable.this.table.length)
		    return HashTable.this.table[pos];

		return null;
	    }

	    @Override
	    public boolean hasNext()
	    {
		return nextBucket != null;
	    }

	    @SuppressWarnings("unchecked")
	    @Override
	    public V next()
	    {
		V result = (V) nextBucket.value;

		nextBucket = nextBucket.next != null ? nextBucket.next
			: advanceToNextBucket();

		return result;
	    }

	};
    }

    @Override
    public Iterator<K> keys()
    {
	return null;
    }

}
