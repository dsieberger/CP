package cp.articlerep.ds;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Ricardo Dias
 */
public class LinkedList<V> implements List<V>
{
    private final ReadWriteLock rwl;
    public final Lock readLock;
    public final Lock writeLock;

    public class Node
    {
	final private V m_value;
	private Node m_next;

	public Node(V value, Node next)
	{
	    m_value = value;
	    m_next = next;
	}

	public Node(V value)
	{
	    this(value, null);
	}

	public V getValue()
	{
	    return m_value;
	}

	public void setNext(Node next)
	{
	    m_next = next;
	}

	public Node getNext()
	{
	    return m_next;
	}
    }

    private Node m_head;

    public LinkedList()
    {
	m_head = null;
	rwl = new ReentrantReadWriteLock();
	readLock = rwl.readLock();
	writeLock = rwl.writeLock();
    }

    public void add(V value)
    {
	writeLock.lock();
	try
	{
	    m_head = new Node(value, m_head);
	}
	finally { writeLock.unlock(); }
    }

    public void add(int pos, V value)
    {

	if (pos == 0)
	{
	    add(value);
	    return;
	}

	Node n = null;
	Node f = null;

	writeLock.lock();
	try
	{
	    for (n = m_head; n != null && pos > 0; n = n.m_next)
	    {
		f = n;
		pos--;
	    }

	    Node newNode = new Node(value, f.m_next);
	    f.m_next = newNode;
    	}
    	finally { writeLock.unlock(); }
    }

    public V remove(int pos)
    {
	V res = null;

	Node f = null;
	Node n = null;

	writeLock.lock();
	try
	{
	    for (n = m_head; n != null && pos > 0; n = n.m_next)
	    {
		f = n;
		pos--;
	    }

	    if (n != null)
	    {
		res = n.m_value;
		if (f != null)
		{
		    f.m_next = n.m_next;
		} else
		{
		    m_head = n.m_next;
		}
	    }
	}
	finally { writeLock.unlock(); }

	return res;
    }

    public V get(int pos)
    {
	V res = null;
	Node n = null;
	
	readLock.lock();
	try
	{
	    for (n = m_head; n != null && pos > 0; n = n.m_next)
	    {
		pos--;
	    }
	    if (n != null)
	    {
		res = n.m_value;
	    }
	}
	finally { readLock.unlock(); }
	return res;
    }

    public int size()
    {
	int res = 0;
	
	readLock.lock();
	try
	{
	    for (Node n = m_head; n != null; n = n.m_next)
	    {
		res++;
	    }
	}
	finally { readLock.unlock(); }
	
	return res;
    }

    public Iterator<V> iterator()
    {
	return new Iterator<V>()
	{

	    private Node curr = m_head;

	    public boolean hasNext()
	    {
		return curr != null;
	    }

	    public V next()
	    {
		V ret = curr.m_value;
		curr = curr.m_next;
		return ret;
	    }
	};
    }

    public String toString()
    {
	StringBuffer sb = new StringBuffer("[");

	Iterator<V> it = this.iterator();

	if (it.hasNext())
	{
	    sb.append(it.next());
	}

	while (it.hasNext())
	{
	    sb.append(", " + it.next());
	}

	sb.append("]");

	return sb.toString();
    }
}
