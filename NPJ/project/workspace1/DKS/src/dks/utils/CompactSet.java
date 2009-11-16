package dks.utils;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * The <code>CompactSet</code> class. This class represents a <b>set</b> of elements.
 * 
 * <p>This class stores large ranges of elements efficiently by compacting
 * consecutive elements and represent them internally as a single interval.</p>
 * 
 * <p>The purpose of this class is to efficiently handel sets like
 * <i>delivered set</i> and <i>ack set</i>.</p>
 * 
 * <p>TODO: implement addAll(CompactSet S) and removeAll(CompactSet S)</p>
 * 
 * @author Ahmad Al-Shishtawy
 * @version $Id: IntervalBroadcastInfo.java 294 2006-05-05 17:14:14Z alshishtawy $
 */

public class CompactSet {
	
	/**
	 * The <code>Interval</code> class.
	 */	
	
	protected class Interval {
		// interval infima
		BigInteger inf;
		// interval suprema
		BigInteger sup;
		
		/**
		 * @param inf
		 * @param sup
		 */
		public Interval(BigInteger inf, BigInteger sup) {
			super();
			if(inf.compareTo(sup) <= 0) {
				this.inf = inf;
				this.sup = sup;
			} else {
				this.inf = sup;
				this.sup = inf;
			}
		}

		/**
		 * @param x
		 */
		public Interval(BigInteger x) {
			super();
			this.inf = x;
			this.sup = x;
		}

		
		boolean contains(BigInteger x) {
			if(inf.compareTo(x)<=0 && x.compareTo(sup)<=0)
				return true;
			return false;
		}
		
		// Sub interval
		boolean containsAll(Interval I) {
			if(inf.compareTo(I.inf)<=0 && I.sup.compareTo(sup)<=0)
				return true;
			return false;
		}
		
		/**
		 * The size of this interval.
		 * @return The number of elements in this interval (sup - inf + 1)
		 */
		BigInteger size() {
			return sup.subtract(inf).add(BigInteger.ONE);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Interval) {
				Interval I = (Interval) obj;
				if(inf.equals(I.inf) && sup.equals(I.sup))
					return true;
				
			} 
				
			return false;
		}

		@Override
		public String toString() {
			if(size().equals(BigInteger.ONE))
				return "["+inf+"]";
			return "["+inf+", "+sup+"]";
		}
	}
	
	protected ArrayList<Interval> list;
	
	/**
	 * Creates an empty <code>CompactSet</code>.
	 *	
	 */
	public CompactSet() {
		list = new ArrayList<Interval>();
	}
	
	
	/**
	 * Creates a <code>CompactSet</code> that contains all the elements
	 * between and including inf & sup.
	 * @param inf	The lower bound.
	 * @param sup	The upper bound.
	 */
	public CompactSet(BigInteger inf, BigInteger sup) {
		list = new ArrayList<Interval>();
		list.add(new Interval(inf, sup));
	}
	
		
	/**
	 * The copy constructor.
	 * @param S	The compact set to copy from.
	 */
	public CompactSet(CompactSet S) {
		list = new ArrayList<Interval>(S.list.size());
		for (Interval I : S.list) {
			list.add(new Interval(I.inf, I.sup));
		}
	}
	
	
	/**
	 * Adds an element to the set.
	 * @param x	The element to add.
	 */
	public void add(BigInteger x){
		boolean done = false;
		for (Interval i : list) {
			
			// x is already in the set
			if(i.contains(x))
				return;
			
			// x is less that the current interval
			// (implies that it is larger than previous ones)
			// then add it at this index and shift i 
			if(x.compareTo(i.inf)<0) {
				list.add(list.indexOf(i), new Interval(x));
				done = true;
				break;
			}
		}
		
		// if not done then x is larget than all intervals so add it at the end
		if(!done)
			list.add(new Interval(x));
		
		// merge sets if needed
		for (int i = 1; i < list.size(); i++) {
			// if sup(i-1) equals inf(i)-1 then merge these intervals
			if(list.get(i-1).sup.  equals(  list.get(i).inf.  subtract(BigInteger.ONE)  )) {
				list.get(i-1).sup = list.get(i).sup;
				list.remove(i);
				i--;
			}
		}
	}
	
	/**
	 * Removes an element from the set.
	 * @param x	The element to remove.
	 * @return	true if the element was found & removes. false if the element is not in the set.
	 */
	public boolean remove(BigInteger x) {
		for (int i = 0; i < list.size(); i++) {
			Interval I = list.get(i);
			if(x.equals(I.inf)) {
				if(I.size().equals(BigInteger.ONE)) {
					list.remove(i);
					return true;
				}
				else {
					I.inf = I.inf.add(BigInteger.ONE);
					return true;
				}
			} else if(x.equals(I.sup)) {
				I.sup = I.sup.subtract(BigInteger.ONE);
				return true;
			} else if (I.inf.compareTo(x)<0 && x.compareTo(I.sup)<0) {
				Interval tmp = new Interval(I.inf, x.subtract(BigInteger.ONE));
				I.inf = x.add(BigInteger.ONE);
				list.add(i, tmp);
				return true;
			}			
		}
		return false;
		
	}
	
	/**
	 * Checks if an element is in the set.
	 * @param x
	 * @return
	 */
	public boolean contains(BigInteger x) {
		for (Interval i : list) {
			if (i.contains(x))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if all elements in a set S is already in this set (sub set).
	 * @param S
	 * @return
	 */
	public boolean containsAll(CompactSet S) {
		boolean found;
		for(Interval s: S.list) {
			found = false;
			for (Interval i : list) {
				if (i.containsAll(s)) {
					found = true;
					break; //for i
				}
			}
			if(!found)
				return false;
			
		}
		return true;
	}
	
	/**
	 * Returns the number of elements in this set.
	 * <p>Note that this is independent of the number of intervals used to represent
	 * the elements internally.</p>
	 * @return	The number of elements in this set.
	 */
	public BigInteger size() {
		BigInteger c = BigInteger.ZERO;
		for (Interval I : list) {
			c = c.add(I.size());
		}
		return c;
	}
	
	/**
	 * @return	True if the set is empty. False otherwise.
	 */
	public boolean isEmpty() {
		return list.size() == 0;
	}
	
	/**
	 * Removes all elements from the set and makes it empty.
	 */
	public void clear() {
		list.clear();
	}
	
	@Override
	public String toString() {
		String s = new String();
		for (Interval i : list) {
			s += i + " ";
		}
		return s;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof CompactSet) {
			CompactSet S = (CompactSet) obj;
			if(list.size() == S.list.size()) {
				for (int i = 0; i < list.size(); i++) {
					if(!list.get(i).equals(S.list.get(i)))
						return false;
				}
				return true;
			}
		}
		return false;
	}
	
}
