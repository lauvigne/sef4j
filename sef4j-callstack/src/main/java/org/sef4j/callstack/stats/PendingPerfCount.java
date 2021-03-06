package org.sef4j.callstack.stats;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import org.sef4j.callstack.CallStackElt;
import org.sef4j.core.api.proptree.ICopySupport;

/**
 * perf counter for pending thread in a section
 * 
 * this class is multi-thread safe, and lock-FREE!
 */
@SuppressWarnings("restriction")
public class PendingPerfCount implements ICopySupport<PendingPerfCount> {

	private int pendingCount;
	
	private long pendingSumStartTime;

	// ------------------------------------------------------------------------

	public PendingPerfCount() {
	}

	public PendingPerfCount(PendingPerfCount src) {
		set(src);
	}

	public static final Callable<PendingPerfCount> FACTORY = new Callable<PendingPerfCount>() {
		@Override
		public PendingPerfCount call() throws Exception {
			return new PendingPerfCount();
		}
	};

	// ------------------------------------------------------------------------

	public int getPendingCount() {
		return UNSAFE.getIntVolatile(this, pendingCountFieldOffset);
	}

	public long getPendingSumStartTime() {
		return UNSAFE.getLongVolatile(this, pendingSumStartTimeFieldOffset);
	}

	public long getPendingAverageStartTimeMillis() {
		long sum = getPendingSumStartTime();
		int count = getPendingCount();
		long avg = (count != 0)? sum/count : 0;
		return ThreadTimeUtils.nanosToMillis(avg);
	}
	
	@Override /* java.lang.Object */
	public PendingPerfCount clone() {
		return copy();
	}
	
	@Override /* ICopySupport<> */
	public PendingPerfCount copy() {
		return new PendingPerfCount(this);
	}

	public void set(PendingPerfCount src) {
		this.pendingCount = src.getPendingCount();
		this.pendingSumStartTime = src.getPendingSumStartTime();
	}

	public void clear() {
		UNSAFE.getAndSetInt(this, pendingCountFieldOffset, 0);
		UNSAFE.getAndSetLong(this, pendingSumStartTimeFieldOffset, 0L);
	}

	public void incr(PendingPerfCount src) {
		UNSAFE.getAndAddInt(this, pendingCountFieldOffset, src.getPendingCount()); 
		UNSAFE.getAndAddLong(this, pendingSumStartTimeFieldOffset, src.getPendingSumStartTime()); 
	}


	// ------------------------------------------------------------------------
	
	public void addPending(long startTime) {
		UNSAFE.getAndAddInt(this, pendingCountFieldOffset, 1); 
		UNSAFE.getAndAddLong(this, pendingSumStartTimeFieldOffset, startTime); 
	}

	public void removePending(long startTime) {
		UNSAFE.getAndAddInt(this, pendingCountFieldOffset, -1); 
		UNSAFE.getAndAddLong(this, pendingSumStartTimeFieldOffset, -startTime); 
	}

	// Helper method using StackElt start/end times
	// ------------------------------------------------------------------------
	
	public void addPending(CallStackElt stackElt) {
		addPending(stackElt.getStartTime());
	}

	public void removePending(CallStackElt stackElt) {
		removePending(stackElt.getStartTime());		
	}

	// ------------------------------------------------------------------------
	
	@Override
	public String toString() {
		final int count = pendingCount;
		final long sum = this.pendingSumStartTime;
		final long avgApproxMillis = (count != 0)? ThreadTimeUtils.nanosToApproxMillis(sum / count) : 0; 
		return "PendingPerfCounts[" 
				+ ((count != 0)? "count:" + count + ", avg:" + avgApproxMillis + " ms": "")
				+ "]";
	}

	// internal for UNSAFE
	// ------------------------------------------------------------------------
	
	private static final sun.misc.Unsafe UNSAFE;

	private static final long pendingCountFieldOffset;
	private static final long pendingSumStartTimeFieldOffset;
	
	static {
		UNSAFE = UnsafeUtils.getUnsafe();
        
        Class<PendingPerfCount> thisClass = PendingPerfCount.class;
        Field pendingCountField = getField(thisClass, "pendingCount");
        pendingCountFieldOffset = UNSAFE.objectFieldOffset(pendingCountField);

        Field pendingSumStartTimeField = getField(thisClass, "pendingSumStartTime");
        pendingSumStartTimeFieldOffset = UNSAFE.objectFieldOffset(pendingSumStartTimeField);
    }
	
	private static Field getField(Class<?> clss, String name) {
		Field[] fields = clss.getDeclaredFields();
		for(Field f : fields) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}
	
}
