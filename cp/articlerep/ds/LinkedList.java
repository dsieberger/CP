package cp.articlerep.ds;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ricardo Dias
 */
public class LinkedList<V> implements List<V> {

	public class Node {
		
		final private ReentrantLock lock = new ReentrantLock();
		final private V m_value;
		private Node m_next;

		public Node(V value, Node next) {
			m_value = value;
			m_next = next;
		}

		public Node(V value) {
			this(value, null);
		}

		public V getValue() {
			return m_value;
		}

		public void setNext(Node next) {
			m_next = next;
		}

		public Node getNext() {
			return m_next;
		}
		
		public void lock() {
			lock.lock();
		}
		
		public void unlock() {
			lock.unlock();
		}
	}

	private Node m_head;

	public LinkedList() {
		m_head = new Node(null);
	}

	//immutable head - always stays in the list so that you can lock only the head, instead of 'synchronized(list)'
	public void startHOH() {
		
		// if list not empty:
		// head -> elem1 -> ...
		// [head] -> elem1 -> ...
		// [head] -> [elem1] -> ...
		
		// else:
		// head -> null
		// [head] -> null 
		m_head.lock();
		assert m_head.lock.isHeldByCurrentThread();

		if(m_head.m_next != null)
		{
			m_head.m_next.lock();
			assert m_head.m_next.lock.isHeldByCurrentThread();
		}
	}

	public void add(V value) {
		
		// if list not empty:
		// 1) head -> elem1 -> ...
		// 2) [head] -> [elem1] -> ...
		// 3) [head] -> newNode -> [elem1] -> ...
		// 4) head -> newNode -> [elem1] -> ...
		// 5) head -> newNode -> elem1 -> ...
		
		// else:
		// head -> null
		// [head] -> null
		// [head] -> elem1 -> null
		// head -> elem1 -> null
		
		//steps 1 and 2
		//lock list head and it's successor (if it exists)
		startHOH();
		
		//assert that startHOH() method has locked correctly
		//the list head and it's successor if it exists
		if(m_head.m_next != null)
			assert m_head.m_next.lock.isHeldByCurrentThread();
		assert m_head.lock.isHeldByCurrentThread();
		
		//insert new node between the list head and the original
		//list head successor (steps 2 and 3)
		Node new_elem = new Node(value, m_head.m_next);
		m_head.m_next = new_elem;
		
		//unlock the list head (step 4)
		m_head.unlock();
		if(new_elem.m_next != null)
		{
			//step 5 (if the list wasn't originally empty)
			new_elem.m_next.unlock();
			//verify that no locks are forgotten
			assert !m_head.m_next.m_next.lock.isHeldByCurrentThread();
		}
		
		//verify that no locks are forgotten
		assert !m_head.lock.isHeldByCurrentThread() &&
			   !m_head.m_next.lock.isHeldByCurrentThread();
		
	}
	
	//consistent
	//"hand-over-hand" lock
	public void add(int pos, V value) {
		
		if (pos == 0)
		{
			add(value);
			assert m_head.m_next != null;
			return;
		}
		
		startHOH();
		
		Node a = m_head;
		Node b = null;

		for (b = m_head.m_next; b != null && a != null && pos > 0; pos--)
		{	
			assert a.lock.isHeldByCurrentThread() &&
				   b.lock.isHeldByCurrentThread();
			
			if(a.m_next == b)
			{
				a.unlock();
				a = b.m_next;
				if(a != null)
					a.lock();
			}
			else if(b.m_next == a)
			{
				b.unlock();
				b = a.m_next;
				if(b != null)
					b.lock();
			}
		}
		
		if(a != null && a.m_next == b)
		{
			Node newNode = new Node(value, b);
			a.m_next = newNode;
			a.unlock();
			if(b != null)
				b.unlock();
		}
		else if(b != null && b.m_next == a)
		{
			Node newNode = new Node(value, a.m_next);
			b.m_next = newNode;
			b.unlock();
			if(a != null)
				a.unlock();
		}
	}
	
	public V remove(int pos)
	{
		startHOH();
		
		Node a = m_head;
		Node b = null;

		for (b = m_head.m_next; b != null && a != null && pos > 0; pos--)
		{	
			assert a.lock.isHeldByCurrentThread() &&
				   b.lock.isHeldByCurrentThread();
			
			if(a.m_next == b)
			{
				a.unlock();
				a = b.m_next;
				if(a != null)
					a.lock();
			}
			else if(b.m_next == a)
			{
				b.unlock();
				b = a.m_next;
				if(b != null)
					b.lock();
			}
		}
		
		if(a != null && a.m_next == b)
		{
			Node removed = b;
			if(a.m_next != null)
				a.m_next = a.m_next.m_next;
			a.unlock();
			if(removed != null)
			{
				removed.unlock();
				return removed.getValue();
			}
		}
		else if(b != null && b.m_next == a)
		{
			Node removed = a;
			if(b.m_next != null)
				b.m_next = b.m_next.m_next;
			b.unlock();
			if(removed != null)
			{
				removed.unlock();
				return removed.getValue();
			}
		}
		return null;
	}
	
	public V get(int pos)
	{
		startHOH();
		
		Node a = m_head;
		Node b = null;

		for (b = m_head.m_next; b != null && a != null && pos > 0; pos--)
		{	
			assert a.lock.isHeldByCurrentThread() &&
				   b.lock.isHeldByCurrentThread();
			
			if(a.m_next == b)
			{
				a.unlock();
				a = b.m_next;
				if(a != null)
					a.lock();
			}
			else if(b.m_next == a && a != null)
			{
				b.unlock();
				b = a.m_next;
				if(b != null)
					b.lock();
			}
		}
		
		if(a != null && a.m_next == b)
		{
			a.unlock();
			if(b != null)
			{
				b.unlock();
				return b.getValue();
			}
		}
		else if(b != null && b.m_next == a)
		{
			b.unlock();
			if(a != null)
			{
				a.unlock();
				return a.getValue();
			}
		}
		
		return null;
	}
	
	public int size()
	{
		startHOH();
		
		int size = 0;
		Node a = m_head;
		Node b = null;

		for (b = m_head.m_next; b != null && a != null; size++)
		{	
			assert a.lock.isHeldByCurrentThread() &&
				   b.lock.isHeldByCurrentThread();
			
			if(a.m_next == b)
			{
				a.unlock();
				a = b.m_next;
				if(a != null)
					a.lock();
			}
			else if(b.m_next == a && a != null)
			{
				b.unlock();
				b = a.m_next;
				if(b != null)
					b.lock();
			}
		}
		
		return size;
	}
	
	/**
	 * Corrigir este método 'iterator()' para se tornar consistente
	 * num ambiente concorrente!
	 */
	public Iterator<V> iterator()
	{
		startHOH();
		return new Iterator<V>() {
			Node a = m_head;
            Node b = m_head.m_next;
			
			public boolean hasNext() {
                //Temos de ter o node locked para garantir que que existe o next ou não?
                if (b != null){
                    if (a.m_next == b){
                        return true;
                    } else return false;
                }
			}
			public V next() {
                if (a.m_next == b){
                    a.unlock();
                    a = b;
                }
                V ret = b.m_value;
                b.m_next.lock();
                b = b.m_next;
                // onde é que fazemos o ultimo unlock?
				return ret;
			}
		};
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("[");
		
		Iterator<V> it = this.iterator();
		
		if (it.hasNext()) {
			sb.append(it.next());
		}
		
		while(it.hasNext()) {
			sb.append(", "+it.next());
		}
		
		sb.append("]");
		
		return sb.toString();
	}
}
