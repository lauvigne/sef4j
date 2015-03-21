package org.sef4j.core.helpers;

import java.beans.PropertyChangeListener;

/**
 * missing interface for jdk PropertyChangeSupport !
 *
 */
public interface IPropertyChangeListenerSupport {

	public void addPropertyChangeListener(PropertyChangeListener listener);
	public void removePropertyChangeListener(PropertyChangeListener listener);
	
}
