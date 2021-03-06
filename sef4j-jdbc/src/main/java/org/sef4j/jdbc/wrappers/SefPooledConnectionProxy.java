package org.sef4j.jdbc.wrappers;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

import org.sef4j.callstack.CallStackElt.StackPopper;
import org.sef4j.callstack.LocalCallStack;

/**
 * 
 * cf used in XAConnection as parent interface...  
 * Problem : no multiple inheritance of PRoxy class... onyl of interface!
 *
 */
public class SefPooledConnectionProxy implements PooledConnection {

	private static final String CNAME = SefPooledConnectionProxy.class.getName();
	
	private PooledConnection to;

	private Connection lastConnection;
	private SefConnectionProxy lastSefConnectionProxy;

	// ------------------------------------------------------------------------
	
	public SefPooledConnectionProxy(PooledConnection to) {
		super();
		this.to = to;
	}

	// ------------------------------------------------------------------------
	
	public final Connection getConnection() throws SQLException {
        StackPopper toPop = LocalCallStack.meth(CNAME, "getConnection").push();
        try {
        	Connection tmpres = to.getConnection();
        	
        	// wrap or reuse same wrapper?! .. TODO may use Map<Connection,SefConnectionProxy> ?
        	SefConnectionProxy res = (tmpres == lastConnection)? lastSefConnectionProxy 
        			: new SefConnectionProxy(null, tmpres);
        	lastConnection = tmpres;
        	lastSefConnectionProxy = res;
        	
            return toPop.returnValue(res);
        } catch(SQLException ex) {
            throw toPop.returnException(ex);
        } finally {
            toPop.close();
        }
	}

	public final void close() throws SQLException {
        StackPopper toPop = LocalCallStack.meth(CNAME, "close").push();
        try {
        	to.close();
        } catch(SQLException ex) {
            throw toPop.returnException(ex);
        } finally {
            toPop.close();
        }
        // TODO  if (owner != null) owner.onChildPooledConnectionClose();
	}

	
	public void addConnectionEventListener(ConnectionEventListener listener) {
	    StackPopper toPop = LocalCallStack.meth(CNAME, "addConnectionEventListener").push();
	    try {
			to.addConnectionEventListener(listener);
		} catch(RuntimeException ex) {
		    throw toPop.returnException(ex);
		} finally {
		    toPop.close();
		}
	}

	public void removeConnectionEventListener(ConnectionEventListener listener) {
	    StackPopper toPop = LocalCallStack.meth(CNAME, "removeConnectionEventListener").push();
	    try {
	        to.removeConnectionEventListener(listener);
	    } catch(RuntimeException ex) {
	        throw toPop.returnException(ex);
	    } finally {
	        toPop.close();
	    }
	}

	public void addStatementEventListener(StatementEventListener listener) {
	    StackPopper toPop = LocalCallStack.meth(CNAME, "addStatementEventListener").push();
        try {
            to.addStatementEventListener(listener);
        } catch(RuntimeException ex) {
            throw toPop.returnException(ex);
        } finally {
            toPop.close();
        }
	}

	public void removeStatementEventListener(StatementEventListener listener) {
        StackPopper toPop = LocalCallStack.meth(CNAME, "removeStatementEventListener").push();
        try {
            to.removeStatementEventListener(listener);
        } catch(RuntimeException ex) {
            throw toPop.returnException(ex);
        } finally {
            toPop.close();
        }
	}

	// ------------------------------------------------------------------------

	@Override
	public String toString() {
		return "SefPooledConnectionProxy [to=" + to + "]";
	}
	
}
