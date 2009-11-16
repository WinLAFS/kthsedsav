package dks.utils;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * This class represents a simple interval on the ID space line [0, N-1].
 * 
 * <p>
 * This class allows to perform simple interval operations like ID inclusion
 * tests and intersection operations.
 * </p>
 * 
 * <p>
 * The simple interval support closed and opened bounds.
 * </p>
 * 
 * <p>
 * This class is mainly for internal use! For DKS users it is recommended to use
 * <code>IntervalsList</code> directly for more flexibility.
 * </p>
 * 
 * @see IntervalsList
 * @author Luc Maisonobe
 * @author Ahmad Al-Shishtawy
 * @version $Id: SimpleInterval.java 1539 2003-12-13 19:31:14Z luc $
 */
public class SimpleInterval implements Serializable{

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -1227171015141710423L;

	/*
	 * The four possible combinations of bounds
	 */
	public static enum Bounds {
		OPEN_OPEN, OPEN_CLOSED, CLOSED_OPEN, CLOSED_CLOSED
	};

	/**
	 * Build the [0, 0] interval.
	 * 
	 * @param N
	 *            The size of the ID space
	 */
	public SimpleInterval(BigInteger N) {
		inf = BigInteger.ZERO;
		sup = BigInteger.ZERO;
		bounds = Bounds.CLOSED_CLOSED;
		this.N = N;
	}

	/**
	 * Build a simple interval with the given bounds.
	 * <p>
	 * The given bounds should be between 0 and N-1. <code>mod</code> is used
	 * to force it.
	 * </p>
	 * <p>
	 * "a" should be less than or equal "b".
	 * </p>
	 * <p>
	 * Intervals where a > b should be represented by two simple intervals [a,
	 * N-1] and [0, b] using IntervalsList.
	 * </p>
	 * 
	 * @param a
	 *            first bound
	 * @param b
	 *            second bound
	 * @param bounds
	 *            The bounds type for this interval
	 * @param N
	 *            The size of the ID space
	 * @throws SimpleIntervalException
	 */
	public SimpleInterval(BigInteger a, BigInteger b, Bounds bounds,
			BigInteger N) throws SimpleIntervalException {
		this.N = N;
		this.bounds = bounds;
		if (bounds == Bounds.CLOSED_CLOSED) {
			inf = a.mod(N);
			sup = b.mod(N);
		} else if (bounds == Bounds.CLOSED_OPEN) {
			inf = a.mod(N);
			sup = b.subtract(BigInteger.ONE).mod(N);

		} else if (bounds == Bounds.OPEN_CLOSED) {
			inf = a.add(BigInteger.ONE).mod(N);
			sup = b.mod(N);
		} else {
			inf = a.add(BigInteger.ONE).mod(N);
			sup = b.subtract(BigInteger.ONE).mod(N);
		}

		if (inf.compareTo(sup) > 0) {
			inf = BigInteger.ZERO;
			sup = BigInteger.ZERO;
			bounds = Bounds.CLOSED_CLOSED;
			throw new SimpleIntervalException("Invalid SimpleInterval!");
		}

	}

	/**
	 * Copy-constructor.
	 * 
	 * @param i
	 *            interval to copy
	 */
	public SimpleInterval(SimpleInterval i) {
		inf = i.inf;
		sup = i.sup;
		bounds = i.bounds;
		N = i.N;
	}

	/**
	 * Get the actual lower bound of the <strong>closed</strong> interval.
	 * 
	 * @return lower bound of the <strong>closed</strong> interval
	 */
	public BigInteger getInf() {
		return inf;
	}

	/**
	 * Get the lower bound of the interval respecting its bound type.
	 * 
	 * @return lower bound of the interval respecting its bound type
	 */
	public BigInteger getInfBounded() {
		if (bounds == Bounds.CLOSED_CLOSED || bounds == Bounds.CLOSED_OPEN)
			return inf;
		else
			return inf.subtract(BigInteger.ONE).mod(N);
	}

	/**
	 * Get the upper bound of the interval.
	 * 
	 * @return upper bound of the interval
	 */
	public BigInteger getSup() {
		return sup;
	}

	/**
	 * Get the upper bound of the interval.
	 * 
	 * @return upper bound of the interval
	 */
	public BigInteger getSupBounded() {
		if (bounds == Bounds.OPEN_CLOSED || bounds == Bounds.CLOSED_CLOSED)
			return sup;
		else
			return sup.add(BigInteger.ONE).mod(N);
	}

	/**
	 * @return Returns the bounds.
	 */
	public Bounds getBounds() {
		return bounds;
	}

	/**
	 * Get the length of the interval.
	 * 
	 * @return length of the interval
	 */
	public BigInteger getLength() {
		return sup.subtract(inf).add(BigInteger.ONE);
	}

	/**
	 * Check if the interval contains a point.
	 * 
	 * @param x
	 *            point to check
	 * @return true if the interval contains x
	 */
	public boolean contains(BigInteger x) {
		return (x.compareTo(inf) >= 0) && (x.compareTo(sup) <= 0);
	}

	/**
	 * Check if the interval contains another interval.
	 * 
	 * @param i
	 *            interval to check
	 * @return true if i is completely included in the instance
	 */
	public boolean contains(SimpleInterval i) {
		return (inf.compareTo(i.inf) <= 0) && (i.sup.compareTo(sup) <= 0);
	}

	/**
	 * Check if an interval intersects the instance.
	 * 
	 * @param i
	 *            interval to check
	 * @return true if i intersects the instance
	 */
	public boolean intersects(SimpleInterval i) {
		return (inf.compareTo(i.sup) <= 0) && (i.inf.compareTo(sup) <= 0);
	}

	/**
	 * Add an interval to the instance.
	 * <p>
	 * This method expands the instance.
	 * </p>
	 * <p>
	 * This operation is <strong>not</strong> a union operation. If the
	 * instance and the interval are disjoints (i.e. if {@link #intersects
	 * intersects(i)} would return <code>false</code>), then the hole between
	 * the intervals is filled in.
	 * </p>
	 * 
	 * @param i
	 *            interval to add to the instance
	 */
	public void addToSelf(SimpleInterval i) {
		inf = inf.min(i.inf);
		sup = sup.max(i.sup);

	}

	/**
	 * Add two intervals.
	 * <p>
	 * This operation is <strong>not</strong> a union operation. If the
	 * intervals are disjoints (i.e. if {@link #intersects i1.intersects(i2)}
	 * would return <code>false</code>), then the hole between the intervals
	 * is filled in.
	 * </p>
	 * 
	 * @param i1
	 *            first interval
	 * @param i2
	 *            second interval
	 * @return a new interval
	 */
	public static SimpleInterval add(SimpleInterval i1, SimpleInterval i2) {
		SimpleInterval copy = new SimpleInterval(i1);
		copy.addToSelf(i2);
		return copy;
	}

	/**
	 * Intersects the instance with an interval.
	 * <p>
	 * This method reduces the instance, it could even become empty if the
	 * interval does not intersects the instance.
	 * </p>
	 * 
	 * @param i
	 *            interval with which the instance should be intersected
	 */
	public void intersectSelf(SimpleInterval i) {
		inf = inf.max(i.inf);
		sup = (sup.min(i.sup)).max(inf);
	}

	/**
	 * Intersect two intervals.
	 * 
	 * @param i1
	 *            first interval
	 * @param i2
	 *            second interval
	 * @return a new interval which is the intersection of i1 with i2
	 */
	public static SimpleInterval intersection(SimpleInterval i1,
			SimpleInterval i2) {
		SimpleInterval copy = new SimpleInterval(i1);
		copy.intersectSelf(i2);
		return copy;
	}

	@Override
	public String toString() {
		String str = new String();
		if (bounds == Bounds.CLOSED_CLOSED || bounds == Bounds.CLOSED_OPEN)
			str += "[";
		else
			str += "]";

		str += getInfBounded();
		str += ",";
		str += getSupBounded();

		if (bounds == Bounds.CLOSED_CLOSED || bounds == Bounds.OPEN_CLOSED)
			str += "]";
		else
			str += "[";

		return str;
	}

	
	/** Lower bound of the interval. */
	private BigInteger inf;

	/** Upper bound of the interval. */
	private BigInteger sup;

	private BigInteger N;

	private Bounds bounds;

	public BigInteger getN() {
		return N;
	}

}
