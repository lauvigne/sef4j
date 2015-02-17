package org.sef4j.callstack.stats;

import java.io.Serializable;

public class PerfTimeStatsSlotInfo implements Serializable {

	/** for java.io.Serializable */
	private static final long serialVersionUID = 1L;
	
	private long from;
	private long to;
	private int count;
	private long sum;
	
	// ------------------------------------------------------------------------
	
	public PerfTimeStatsSlotInfo(long from, long to, int count, long sum) {
		super();
		this.from = from;
		this.to = to;
		this.count = count;
		this.sum = sum;
	}
	
	// ------------------------------------------------------------------------


	public long getFrom() {
		return from;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public long getTo() {
		return to;
	}

	public void setTo(long to) {
		this.to = to;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getSum() {
		return sum;
	}

	public void setSum(long sum) {
		this.sum = sum;
	}

	public double getAverage() {
		return ((double)sum) / ((count != 0)? count : 1);
	}

	// ------------------------------------------------------------------------

	@Override
	public String toString() {
		return "PerfTimeStatsSlotInfo [range=" + from + "-" + to
				+ ", count=" + count + ", sum=" + sum + "]";
	}
	
}