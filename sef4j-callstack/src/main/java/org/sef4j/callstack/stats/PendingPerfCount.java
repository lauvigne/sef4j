package org.sef4j.callstack.stats;

import java.lang.reflect.Field;

import org.sef4j.callstack.CallStackElt;

/**
 * perf counter for pending thread in a section
 * 
 * this class is multi-thread safe, and lock-FREE!
 */
@SuppressWarnings("restriction")
public class PendingPerfCount {

	private int pendingCount;
	
	private long pendingSumStartTime;

	// ------------------------------------------------------------------------

	public PendingPerfCount() {
	}

	// ------------------------------------------------------------------------

	public int getPendingCount() {
		return UNSAFE.getIntVolatile(this, pendingCountFieldOffset);
	}

	public long getPendingSumStartTime() {
		return UNSAFE.getLongVolatile(this, pendingSumStartTimeFieldOffset);
	}

	public void getCopyTo(PendingPerfCount dest) {
		dest.pendingCount = getPendingCount();
		dest.pendingSumStartTime = getPendingSumStartTime();
	}

	public PendingPerfCount getCopy() {
		PendingPerfCount res = new PendingPerfCount();
		getCopyTo(res);
		return res;
	}

	public void setCopy(PendingPerfCount src) {
		src.getCopyTo(this);
	}

	public void clear() {
		UNSAFE.getAndSetInt(this, pendingCountFieldOffset, 0);
		UNSAFE.getAndSetLong(this, pendingSumStartTimeFieldOffset, 0L);
	}

	// ------------------------------------------------------------------------
	
	public void addPending(long currTime) {
		UNSAFE.getAndAddInt(this, pendingCountFieldOffset, 1); 
		UNSAFE.getAndAddLong(this, pendingSumStartTimeFieldOffset, currTime); 
	}

	public void removePending(long startedTime) {
		UNSAFE.getAndAddInt(this, pendingCountFieldOffset, -1); 
		UNSAFE.getAndAddLong(this, pendingSumStartTimeFieldOffset, - startedTime); 
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
				+ ((count != 0)? "count:" + count: "")
				+ ((count != 0)? ", avg:" + avgApproxMillis + " ms": "")
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
