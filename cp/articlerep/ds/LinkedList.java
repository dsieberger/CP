package cp.articlerep.ds;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ricardo Dias
 */
public class LinkedList<V> implements List<V> {

	public class Node {

		final private Lock lock = new ReentrantLock();		//modified

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
		
		//modified
		public void lock() {
			lock.lock();
		}
		
		//modified
		public void unlock() {
			lock.unlock();
		}
	}

	private Node m_head;

	public LinkedList() {
		m_head = null;
	}

	//consistent
	//no lock on both nodes because one of them is a local variable (only accessed by one thread)
	public void add(V value) {
		
		boolean isNull = (m_head == null);
		
		if(!isNull)
			m_head.lock();
		try {
			m_head = new Node(value, m_head);
		} finally {
			if(!isNull)
				m_head.unlock();
		}
	}
	
	//consistent
	//"hand-over-hand" lock
	public void add(int pos, V value) {
		
		if (pos == 0) {
			add(value);
			return;
		}
		
		Node old = null;
		Node prev = null;
		Node next = null;

		for (next = m_head; next != null && pos > 0; next = next.m_next) {
			
			if(old != null)
				old.unlock();
			
			if(prev != null)
				old = prev;
			
			next.lock();
			prev = next;
			pos--;
		}
		
		Node newNode = new Node(value, prev.m_next);
		prev.m_next = newNode;
		
		prev.unlock();
		newNode.m_next.unlock();
	}

	//consistent
	public V remove(int pos) {
		
		V res = null;
		
		Node old = null;
		Node prev = null;
		Node next = null;
		
		for (next = m_head; next != null && pos > 0; next = next.m_next) {
			
			if(old != null)
				old.unlock();
			
			if(prev != null)
				old = prev;
			
			next.lock();
			prev = next;
			pos--;
		}
		
		if (next != null) {
			res = next.m_value;
			if (prev != null) {
			    prev.m_next = next.m_next;
				old.unlock();
			}
			else {
			    m_head = next.m_next;
			}
	
			prev.unlock();
		}

		return res;
	}

	//consistent
	public V get(int pos) {
		
		V res = null;
		
		Node old = null;
		Node prev = null;
		Node next = null;
		
		for (next = m_head; next != null && pos > 0; next = next.m_next) {
			
			if(old != null)
				old.unlock();
			
			if(prev != null)
				old = prev;
			
			next.lock();
			prev = next;
			pos--;
		}
		
		if (next != null) {
			res = next.m_value;
		}
		
		old.unlock();
		prev.unlock();
		
		return res;
	}

	//consistent
	public int size() {
		
		int res = 0;
		
		Node old = null;
		Node prev = null;
		Node next = null;
		
		for (next = m_head; next != null; next = next.m_next) {
			
			if(old != null)
				old.unlock();
			
			if(prev != null)
				old = prev;
			
			next.lock();
			prev = next;
			res++;
		}
		
		old.unlock();
		prev.unlock();
		
		return res;
	}

	//consistent
	public Iterator<V> iterator() {
		
		if(m_head != null)
			m_head.lock();
		
		return new Iterator<V>() {
			
			private Node curr = m_head;
			
			public boolean hasNext() {
				return curr != null;
			}
			
			public V next() {
				
				Node temp = curr;
				curr.m_next.lock();
				V ret = curr.m_value;
				
				if(hasNext())
					temp.unlock();
				
				curr = curr.m_next;
				return ret;
			}
		};
	}
	
	//consistent
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
