package org.sef4j.callstack.stats;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("restriction")
public final class PerfStats {

	
	private static final Logger LOG = LoggerFactory.getLogger(PerfStats.class);
	
	private static final sun.misc.Unsafe UNSAFE;
	private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
	static {
		try {
			checkThreadCpuEnabled();
		} catch(Exception ex) {
			LOG.error("FAILED to check/activate threadCpuTime! ... ignore, no rethrow!", ex);
			// ignore, no rethrow!
		}
	}
	
	public static void checkThreadCpuEnabled() {
		if (!threadMXBean.isThreadCpuTimeEnabled()) {
			boolean isSupported = threadMXBean.isThreadCpuTimeSupported();
			if (isSupported) {
				LOG.info("ThreadMXBean.isThreadCpuTimeEnabled(): false => enable");
				threadMXBean.setThreadCpuTimeEnabled(true);
			} else {
				LOG.warn("ThreadMXBean.isThreadCpuTimeSupported(): false !!");
			}
		}
	}
	
	
	/*pp*/ volatile int pendingCount;
	
	/*pp*/ volatile int pendingSumStartTime;
	
	private PerfTimeStats elapsedTimeStats = new PerfTimeStats();
	private PerfTimeStats threadUserTimeStats = new PerfTimeStats();
	private PerfTimeStats threadCpuTimeStats = new PerfTimeStats();
	
	// ------------------------------------------------------------------------

	public PerfStats() {
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
	
	public int getPendingCount() {
		return pendingCount; // UNSAFE.getIntVolatile(this, pendingCountFieldOffset);
	}

	public long getPendingSumStartTime() {
		return pendingSumStartTime; // UNSAFE.getLongVolatile(this, pendingSumStartTimeFieldOffset);
	}

	public void incrAndRemovePending(long startedTime, long startedThreadUserTime, long startedThreadCpuTime) {
		removePending(startedTime);
		incr(startedTime, startedThreadUserTime, startedThreadCpuTime);
	}

	public void incr(long startedTime, long startedThreadUserTime, long startedThreadCpuTime) {
		long currThreadCpuTime = threadMXBean.getCurrentThreadCpuTime();
		long currThreadUserTime = threadMXBean.getCurrentThreadUserTime();
		long currTime = System.nanoTime();
		
		long elapsedTime = currTime - startedTime;
		long elapsedThreadUserTime = currThreadUserTime - startedThreadUserTime;
		long elapsedThreadCpuTime = currThreadCpuTime - startedThreadCpuTime;
		
		elapsedTimeStats.incr(elapsedTime);
		threadUserTimeStats.incr(elapsedThreadUserTime);
		threadCpuTimeStats.incr(elapsedThreadCpuTime);
	}
	
	// internal for UNSAFE
	// ------------------------------------------------------------------------
	
	private static final long pendingCountFieldOffset;
	private static final long pendingSumStartTimeFieldOffset;
	
	static {
		UNSAFE = UnsafeUtils.getUnsafe();
        
        Class<PerfStats> thisClass = PerfStats.class;
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