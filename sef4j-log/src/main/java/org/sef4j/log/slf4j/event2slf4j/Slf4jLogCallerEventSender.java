package org.sef4j.log.slf4j.event2slf4j;

import java.util.Collection;

import org.sef4j.core.api.EventSender;
import org.sef4j.core.helpers.adapters.TypeHierarchyToObjectMap;
import org.slf4j.Logger;


/**
 * adapter EventSender -> Slf4j
 * 
 * @see EventToSlf4jLogCaller
 */
public class Slf4jLogCallerEventSender<T> implements EventSender<T> {
	
	private Logger slf4jLogger;
	
	/**
	 * class -> converter for event to method call of <code>slf4jLogger.loXX(YY, ZZ)</code>
	 */
	private TypeHierarchyToObjectMap<EventToSlf4jLogCaller<T>> eventToSlf4jLogCallerPerClass;

	/**
	 * converter for event object to method call of <code>slf4jLogger.loXX(YY, ZZ)</code>
	 */
	private EventToSlf4jLogCaller<T> defautEventToSlf4jLogCaller;

	
    // ------------------------------------------------------------------------
	
	public Slf4jLogCallerEventSender(Logger slf4jLogger, 
			TypeHierarchyToObjectMap<EventToSlf4jLogCaller<T>> eventToSlf4jLogCallerPerClass,
			EventToSlf4jLogCaller<T> defautEventToSlf4jLogCaller
			) {
		this.slf4jLogger = slf4jLogger;
		this.eventToSlf4jLogCallerPerClass = eventToSlf4jLogCallerPerClass;
		this.defautEventToSlf4jLogCaller = defautEventToSlf4jLogCaller;
	}
	
    // ------------------------------------------------------------------------

	@Override
	public void sendEvent(T event) {
		EventToSlf4jLogCaller<T> logCaller;
		if (eventToSlf4jLogCallerPerClass != null && event != null) {
			logCaller = eventToSlf4jLogCallerPerClass.get(event.getClass());
			//	if logCaller == null and needing default => see register logCaller to Object.class
		} else {
			logCaller = defautEventToSlf4jLogCaller;
		}
		if (logCaller != null) {
			logCaller.logTo(event, slf4jLogger);
		}
	}

	@Override
    public void sendEvents(Collection<T> events) {
        for(T event : events) {
        	sendEvent(event);
        }
    }

	public Logger getSlf4jLogger() {
		return slf4jLogger;
	}

	public void setSlf4jLogger(Logger slf4jLogger) {
		this.slf4jLogger = slf4jLogger;
	}

	public TypeHierarchyToObjectMap<EventToSlf4jLogCaller<T>> getEventToSlf4jLogCallerPerClass() {
		return eventToSlf4jLogCallerPerClass;
	}

	public void setEventToSlf4jLogCallerPerClass(TypeHierarchyToObjectMap<EventToSlf4jLogCaller<T>> eventToSlf4jLogCallerPerClass) {
		this.eventToSlf4jLogCallerPerClass = eventToSlf4jLogCallerPerClass;
	}

	public EventToSlf4jLogCaller<T> getDefautEventToSlf4jLogCaller() {
		return defautEventToSlf4jLogCaller;
	}

	public void setDefautEventToSlf4jLogCaller(EventToSlf4jLogCaller<T> defautEventToSlf4jLogCaller) {
		this.defautEventToSlf4jLogCaller = defautEventToSlf4jLogCaller;
	}
	
	
	
}
