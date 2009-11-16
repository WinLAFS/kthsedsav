package dks.utils;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import dks.utils.SimpleInterval.Bounds;

/**
 * This class represents an intervals list.
 * <p>
 * An interval list represent a list of contiguous regions on the ID space line.
 * All intervals of the list are disjoints to each other, they are stored in
 * ascending order.
 * </p>
 * <p>
 * The class supports the main set operations like union and intersection.
 * </p>
 * 
 * @see SimpleInterval
 * @author Luc Maisonobe
 * @author Ahmad Al-Shishtawy
 * @version $Id: IntervalsList.java 1694 2006-09-03 19:53:48Z luc $
 */
public class IntervalsList implements Serializable{

	
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 4246105176295938528L;

	/** The list of intervals. */
	private ArrayList<SimpleInterval> intervals;

	private BigInteger N;

	/**
	 * Build an empty intervals list.
	 * 
	 * @param N
	 *            The size of the ID space
	 */
	public IntervalsList(BigInteger N) {
		intervals = new ArrayList<SimpleInterval>();
		this.N = N;
	}

	/**
	 * Build an intervals list containing only one simple interval [a, b] if a<=b.
	 * Or two simple intervals [a, N-1] & [0, b] if a>b.
	 * 
	 * @param a
	 *            first bound of the interval
	 * @param b
	 *            second bound of the interval
	 * @param bounds
	 *            The bounds type for this interval
	 * @param N
	 *            The size of the ID space
	 * @throws SimpleIntervalException
	 */
	public IntervalsList(BigInteger a, BigInteger b, Bounds bounds, BigInteger N)
			throws SimpleIntervalException {
		intervals = new ArrayList<SimpleInterval>();
		this.N = N;
		if (bounds == Bounds.CLOSED_CLOSED) {
			if (a.mod(N).compareTo(b.mod(N)) <= 0)
				intervals.add(new SimpleInterval(a, b, bounds, N));
			else {
				intervals.add(new SimpleInterval(BigInteger.ZERO, b,
						Bounds.CLOSED_CLOSED, N));
				intervals.add(new SimpleInterval(a, N.subtract(BigInteger.ONE),
						Bounds.CLOSED_CLOSED, N));
			}
		} else if (bounds == Bounds.CLOSED_OPEN) {
			if (a.mod(N).compareTo(b.subtract(BigInteger.ONE).mod(N)) <= 0)
				intervals.add(new SimpleInterval(a, b, bounds, N));
			else {
				intervals.add(new SimpleInterval(BigInteger.ZERO, b,
						Bounds.CLOSED_OPEN, N));
				intervals.add(new SimpleInterval(a, N.subtract(BigInteger.ONE),
						Bounds.CLOSED_CLOSED, N));
			}
		} else if (bounds == Bounds.OPEN_CLOSED) {
			if (a.subtract(BigInteger.ONE).mod(N).compareTo(b.mod(N)) <= 0)
				intervals.add(new SimpleInterval(a, b, bounds, N));
			else {
				intervals.add(new SimpleInterval(BigInteger.ZERO, b,
						Bounds.CLOSED_CLOSED, N));
				intervals.add(new SimpleInterval(a, N.subtract(BigInteger.ONE),
						Bounds.OPEN_CLOSED, N));
			}
		} else {
			if (a.subtract(BigInteger.ONE).mod(N).compareTo(
					b.subtract(BigInteger.ONE).mod(N)) <= 0)
				intervals.add(new SimpleInterval(a, b, bounds, N));
			else {
				intervals.add(new SimpleInterval(BigInteger.ZERO, b,
						Bounds.CLOSED_OPEN, N));
				intervals.add(new SimpleInterval(a, N.subtract(BigInteger.ONE),
						Bounds.OPEN_CLOSED, N));
			}
		}
	}

	/**
	 * Build an intervals list containing only one simple interval.
	 * 
	 * @param i
	 *            interval
	 * @param N
	 *            The size of the ID space
	 */
	public IntervalsList(SimpleInterval i, BigInteger N) {
		intervals = new ArrayList<SimpleInterval>();
		intervals.add(i);
		this.N = N;
	}

	/**
	 * Copy constructor.
	 * <p>
	 * The copy operation is a deep copy: the underlying intervals are
	 * independant of the instances of the copied list.
	 * </p>
	 * 
	 * @param list
	 *            intervals list to copy
	 */
	public IntervalsList(IntervalsList list) {
		intervals = new ArrayList<SimpleInterval>(list.intervals.size());

		for (SimpleInterval i : list.intervals) {
			intervals.add(new SimpleInterval(i));
		}
		this.N = list.N;
		// for (Iterator iterator = list.intervals.iterator();
		// iterator.hasNext();) {
		// intervals.add(new SimpleInterval((SimpleInterval) iterator.next()));
		// }
	}

	/**
	 * Check if the instance is empty.
	 * 
	 * @return true if the instance is empty
	 */
	public boolean isEmpty() {
		return intervals.isEmpty();
	}

	/**
	 * Check if the instance is connected.
	 * <p>
	 * An interval list is connected if it contains only one interval.
	 * </p>
	 * 
	 * @return true is the instance is connected
	 */
	public boolean isConnex() {
		return intervals.size() == 1;
	}

	/**
	 * Get the lower bound of the list.
	 * 
	 * @return lower bound of the list or <code>null</code> if the list does
	 *         not contain any interval
	 */
	public BigInteger getInf() {

		return intervals.isEmpty() ? null : intervals.get(0).getInf();
	}

	/**
	 * Get the upper bound of the list.
	 * 
	 * @return upper bound of the list or Double.NaN if the list does not
	 *         contain any interval
	 */
	public BigInteger getSup() {
		return intervals.isEmpty() ? null : intervals.get(intervals.size() - 1)
				.getSup();
	}

	/**
	 * Get the number of intervals of the list.
	 * 
	 * @return number of intervals in the list
	 */
	public int getSize() {
		return intervals.size();
	}

	/**
	 * Get an interval from the list.
	 * 
	 * @param i
	 *            index of the interval
	 * @return interval at index i
	 */
	public SimpleInterval getSimpleInterval(int i) {
		return intervals.get(i);
	}

	/**
	 * Get the ordered list of disjoints intervals.
	 * 
	 * @return list of disjoints intervals in ascending order
	 */
	public List getIntervals() {
		return intervals;
	}

	/**
	 * Check if the list contains a point.
	 * 
	 * @param x
	 *            point to check
	 * @return true if the list contains x
	 */
	public boolean contains(BigInteger x) {

		for (SimpleInterval i : intervals) {
			if (i.contains(x))
				return true;
		}
		return false;

	}

	/**
	 * Check if the list contains an interval.
	 * 
	 * @param i
	 *            interval to check
	 * @return true if i is completely included in the instance
	 */
	public boolean contains(SimpleInterval i) {

		for (SimpleInterval j : intervals) {
			if (j.contains(i))
				return true;
		}
		return false;

	}

	/**
	 * Check if an interval intersects the instance.
	 * 
	 * @param i
	 *            interval to check
	 * @return true if i intersects the instance
	 */
	public boolean intersects(SimpleInterval i) {
		for (SimpleInterval j : intervals) {
			if (j.intersects(i))
				return true;
		}
		return false;
	}

	/**
	 * Check if an interval intersects the instance.
	 * 
	 * @param i
	 *            interval to check
	 * @return true if i intersects the instance
	 */
	public boolean intersects(IntervalsList I) {
		for (SimpleInterval i : I.intervals) {
			for (SimpleInterval j : intervals) {
				if (j.intersects(i))
					return true;
			}
		}
		return false;
	}

	/**
	 * Add an interval to the instance.
	 * <p>
	 * This method expands the instance.
	 * </p>
	 * <p>
	 * This operation is a union operation. The number of intervals in the list
	 * can decrease if the interval fills some holes between existing intervals
	 * in the list.
	 * </p>
	 * 
	 * @param i
	 *            interval to add to the instance
	 */
	public void addToSelf(SimpleInterval i) {

		ArrayList<SimpleInterval> newIntervals = new ArrayList<SimpleInterval>();
		BigInteger inf = null;
		BigInteger sup = null;
		boolean pending = false;
		boolean processed = false;
		try {
			for (SimpleInterval local : intervals) {

				if (local.getSup().compareTo(i.getInf()) < 0) {
					newIntervals.add(local);
				} else if (local.getInf().compareTo(i.getSup()) < 0) {
					if (!pending) {
						inf = local.getInf().min(i.getInf());
						pending = true;
						processed = true;
					}
					sup = local.getSup().max(i.getSup());
				} else {
					if (pending) {
						newIntervals.add(new SimpleInterval(inf, sup,
								Bounds.CLOSED_CLOSED, N));
						pending = false;
					} else if (!processed) {
						newIntervals.add(i);
					}
					processed = true;
					newIntervals.add(local);
				}
			}

			if (pending) {
				newIntervals.add(new SimpleInterval(inf, sup,
						Bounds.CLOSED_CLOSED, N));
			} else if (!processed) {
				newIntervals.add(i);
			}

			for (int j = 1; j < newIntervals.size(); j++) {
				if (newIntervals.get(j - 1).getSup().add(BigInteger.ONE)
						.equals(newIntervals.get(j).getInf())) {
					SimpleInterval tmp = new SimpleInterval(newIntervals.get(
							j - 1).getInf(), newIntervals.get(j).getSup(),
							Bounds.CLOSED_CLOSED, N);
					newIntervals.remove(j - 1);
					newIntervals.remove(j - 1);
					newIntervals.add(j - 1, tmp);
					j--;
				}

			}

		} catch (SimpleIntervalException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		intervals = newIntervals;

	}

	/**
	 * Add an intervals list and an interval.
	 * 
	 * @param list
	 *            intervals list
	 * @param i
	 *            interval
	 * @return a new intervals list which is the union of list and i
	 */
	public static IntervalsList add(IntervalsList list, SimpleInterval i) {
		IntervalsList copy = new IntervalsList(list);
		copy.addToSelf(i);
		return copy;
	}

	/**
	 * Remove an interval from the list.
	 * <p>
	 * This method reduces the instance. This operation is defined in terms of
	 * points set operation. As an example, if the [2, 3] interval is subtracted
	 * from the list containing only the [0, 10] interval, the result will be
	 * the [0, 2] U [3, 10] intervals list.
	 * </p>
	 * 
	 * @param i
	 *            interval to remove
	 */
	public void subtractFromSelf(SimpleInterval i) {
		IntervalsList temp = new IntervalsList(N);
		try {
			if (i.getInf().compareTo(getInf()) > 0) {
				temp.addToSelf(new SimpleInterval(getInf(), i.getInf()
						.subtract(BigInteger.ONE), Bounds.CLOSED_CLOSED, N));
			}
			if (i.getSup().compareTo(getSup()) < 0) {
				temp
						.addToSelf(new SimpleInterval(i.getSup().add(
								BigInteger.ONE), getSup(),
								Bounds.CLOSED_CLOSED, N));
			}
			intersectSelf(temp);
		} catch (SimpleIntervalException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Remove an interval from a list.
	 * 
	 * @param list
	 *            intervals list
	 * @param i
	 *            interval to remove
	 * @return a new intervals list
	 */
	public static IntervalsList subtract(IntervalsList list, SimpleInterval i) {
		IntervalsList copy = new IntervalsList(list);
		copy.subtractFromSelf(i);
		return copy;
	}

	/**
	 * Intersects the instance and an interval.
	 * 
	 * @param i
	 *            interval
	 */
	public void intersectSelf(SimpleInterval i) {
		ArrayList<SimpleInterval> newIntervals = new ArrayList<SimpleInterval>();
		for (SimpleInterval local : intervals) {
			if (local.intersects(i)) {
				newIntervals.add(SimpleInterval.intersection(local, i));
			}
		}
		intervals = newIntervals;
	}

	/**
	 * Intersect a list and an interval.
	 * 
	 * @param list
	 *            intervals list
	 * @param i
	 *            interval
	 * @return the intersection of list and i
	 */
	public static IntervalsList intersection(IntervalsList list,
			SimpleInterval i) {
		IntervalsList copy = new IntervalsList(list);
		copy.intersectSelf(i);
		return copy;
	}

	/**
	 * Add an intervals list to the instance.
	 * <p>
	 * This method expands the instance.
	 * </p>
	 * <p>
	 * This operation is a union operation. The number of intervals in the list
	 * can decrease if the list fills some holes between existing intervals in
	 * the instance.
	 * </p>
	 * 
	 * @param list
	 *            intervals list to add to the instance
	 */
	public void addToSelf(IntervalsList list) {
		for (SimpleInterval i : list.intervals) {
			addToSelf(i);
		}
	}

	/**
	 * Add two intervals lists.
	 * 
	 * @param list1
	 *            first intervals list
	 * @param list2
	 *            second intervals list
	 * @return a new intervals list which is the union of list1 and list2
	 */
	public static IntervalsList add(IntervalsList list1, IntervalsList list2) {
		IntervalsList copy = new IntervalsList(list1);
		copy.addToSelf(list2);
		return copy;
	}

	/**
	 * Remove an intervals list from the instance.
	 * 
	 * @param list
	 *            intervals list to remove
	 */
	public IntervalsList subtractFromSelf(IntervalsList list) {
		for (SimpleInterval i : list.intervals) {
			if (isEmpty())
				return this;
			subtractFromSelf(i);
		}
		return this;
	}

	/**
	 * Remove an intervals list from another one.
	 * 
	 * @param list1
	 *            intervals list
	 * @param list2
	 *            intervals list to remove
	 * @return a new intervals list
	 */
	public static IntervalsList subtract(IntervalsList list1,
			IntervalsList list2) {
		IntervalsList copy = new IntervalsList(list1);
		copy.subtractFromSelf(list2);
		return copy;
	}

	/**
	 * Intersect the instance and another intervals list.
	 * 
	 * @param list
	 *            list to intersect with the instance
	 */
	public void intersectSelf(IntervalsList list) {
		intervals = intersection(this, list).intervals;
	}

	/**
	 * Intersect two intervals lists.
	 * 
	 * @param list1
	 *            first intervals list
	 * @param list2
	 *            second intervals list
	 * @return a new list which is the intersection of list1 and list2
	 */
	public static IntervalsList intersection(IntervalsList list1,
			IntervalsList list2) {
		IntervalsList list = new IntervalsList(list1.N);
		for (SimpleInterval i : list2.intervals) {
			list.addToSelf(intersection(list1, i));
		}

		return list;
	}

	@Override
	public String toString() {
		String str = "{ ";
		for (SimpleInterval i : intervals) {
			str += i;
			str += " ";
		}
		str += "}";
		return str;
	}

	/**
	 * Utility method to add a single id to the total list
	 * 
	 * @param id
	 *            The single id to be added
	 */
	public void addToSelf(BigInteger id) {
		try {
			addToSelf(new SimpleInterval(id, id,
					SimpleInterval.Bounds.CLOSED_CLOSED, N));
		} catch (SimpleIntervalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Utility method to add a list of single id to the total list
	 * 
	 * @param id
	 *            The single id to be added
	 */
	public void addToSelf(ArrayList<BigInteger> ids) {
		try {
			for (BigInteger integer : ids) {
				addToSelf(new SimpleInterval(integer, integer,
						SimpleInterval.Bounds.CLOSED_CLOSED, N));
			}

		} catch (SimpleIntervalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BigInteger getN() {
		return N;
	}

}
