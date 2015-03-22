package org.sef4j.callstack.export.influxdb.jsonprinters;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.sef4j.core.helpers.exporters.ValuePrinter;

/**
 * abstract helper class for InfluxDB serie json formater
 * <PRE>
 * { 
 *   "name": "metric1", 
 *   "columns": [ "count0", "sum0", "count1", "sum1" .... "count9", "sum9" ],
 *   "points":  [ 
 *      [ 12, 3456, 123, 4563,  ...  123, 456546 ]
 *   ]
 * }</PRE>
 * 
 */
public abstract class AbstractInfluxDBValuePrinter<T> implements ValuePrinter<T> {

    private boolean printIndented;
    
    // ------------------------------------------------------------------------

    public AbstractInfluxDBValuePrinter(boolean printIndented) {
        this.printIndented = printIndented;
    }
    
    // ------------------------------------------------------------------------

    @Override
    public final void printValues(PrintWriter output, String metricName, List<T> values) {
        printMetricHeader(output, metricName);
        
        for(Iterator<T> iter = values.iterator(); iter.hasNext(); ) {
            T value = iter.next();
            output.print("[ "); 
            printPointValues(output, value);
            output.print(" ]"); 
            if (iter.hasNext()) {
                output.print(", ");
            }
            optPrintln(output);
        }
        
        printMetricFooter(output);
    }

    @Override
    public final void printValue(PrintWriter output, String metricName, T value) {
        printMetricHeader(output, metricName);
        
        output.print("[ "); 
        printPointValues(output, value);
        output.print(" ]");
        
        printMetricFooter(output);
    }


    private void printMetricHeader(PrintWriter output, String metricName) {
        output.print("{ \"name\":\"");
        output.print(metricName);
        output.print("\",");
        optPrintln(output);
        
        output.print("\"columns\":[");
        printColumnNames(output);
        output.print("],");
        optPrintln(output);
        
        output.print("\"points\":["); 
        optPrintln(output);
    }

    private void printMetricFooter(PrintWriter output) {
        optPrintln(output);
        output.print("]");
        // optPrintln(output);
        output.print("}");
    }

    protected void optPrintln(PrintWriter output) {
        if (printIndented) {
            output.print("\n");
        } else {
            output.print(" ");
        }
    }
    
    public abstract void printColumnNames(PrintWriter output);

    public abstract void printPointValues(PrintWriter output, T point);
    
}
