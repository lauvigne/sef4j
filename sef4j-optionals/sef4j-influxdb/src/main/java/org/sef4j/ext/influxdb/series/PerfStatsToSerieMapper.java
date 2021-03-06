package org.sef4j.ext.influxdb.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.influxdb.dto.Serie;
import org.sef4j.callstack.stats.BasicTimeStatsSlotInfo;
import org.sef4j.callstack.stats.PendingPerfCount;
import org.sef4j.callstack.stats.PerfStats;

/**
 * Mapper for PerfStats -> InfluxDB Serie
 */
public class PerfStatsToSerieMapper {

    private final boolean printPendings;
    private final boolean printElapsed;
    private final boolean printCpu;
    private final boolean printUser;

    private PendingPerfCountToSerieMapper pendingPerfCountMapper;
    private BasicTimeStatsLogHistogramToSerieMapper elapsedTimeStatsToSerieMapper;
    private BasicTimeStatsLogHistogramToSerieMapper userTimeStatsToSerieMapper;
    private BasicTimeStatsLogHistogramToSerieMapper cpuTimeStatsToSerieMapper;

    private final String[] columnNames;

    // ------------------------------------------------------------------------

    public PerfStatsToSerieMapper(String prefix, String suffix,
    		boolean printPendings, boolean printElapsed, boolean printCpu, boolean printUser) {
        this.printPendings = printPendings;
        this.printElapsed = printElapsed;
        this.printCpu = printCpu;
        this.printUser = printUser;
        
        List<String> tmpColNames = new ArrayList<String>();
        if (printPendings) {
        	pendingPerfCountMapper = new PendingPerfCountToSerieMapper(prefix, suffix);
        	tmpColNames.addAll(Arrays.asList(pendingPerfCountMapper.getColumnNames()));
        }
        if (printElapsed) {
        	elapsedTimeStatsToSerieMapper = new BasicTimeStatsLogHistogramToSerieMapper(
        			SerieColNameUtil.prefixed(prefix, "elapsed"), suffix);
        	tmpColNames.addAll(Arrays.asList(elapsedTimeStatsToSerieMapper.getColumnNames()));
        }
        if (printCpu) {
        	cpuTimeStatsToSerieMapper = new BasicTimeStatsLogHistogramToSerieMapper(
        			SerieColNameUtil.prefixed(prefix, "cpu"), suffix);
        	tmpColNames.addAll(Arrays.asList(cpuTimeStatsToSerieMapper.getColumnNames()));
        }
        if (printUser) {
        	userTimeStatsToSerieMapper = new BasicTimeStatsLogHistogramToSerieMapper(
        			SerieColNameUtil.prefixed(prefix, "user"), suffix);
        	tmpColNames.addAll(Arrays.asList(userTimeStatsToSerieMapper.getColumnNames()));
        }
        this.columnNames = tmpColNames.toArray(new String[tmpColNames.size()]);
    }
    
    // ------------------------------------------------------------------------

    public void getColumns(List<String> dest) {
        dest.addAll(Arrays.asList(columnNames));
    }

    public void getValues(List<Object> dest, PerfStats src) {
        if (printPendings) {
            PendingPerfCount pendingCounts = src.getPendingCounts();
            pendingPerfCountMapper.getValues(dest, pendingCounts);
        }
        final BasicTimeStatsSlotInfo[] timeStatsInfo = src.getElapsedTimeStats().getSlotInfoCopy();
        final BasicTimeStatsSlotInfo[] cpuStatsInfo = src.getThreadCpuTimeStats().getSlotInfoCopy();
        final BasicTimeStatsSlotInfo[] userStatsInfo = src.getThreadUserTimeStats().getSlotInfoCopy();
        if (printElapsed) {
            elapsedTimeStatsToSerieMapper.getValues(dest, timeStatsInfo);
        }
        if (printCpu) {
            cpuTimeStatsToSerieMapper.getValues(dest, cpuStatsInfo);
        }
        if (printUser) {
            userTimeStatsToSerieMapper.getValues(dest, userStatsInfo);
        }
    }
    
    
	public Serie map(PerfStats src, String serieName) {
		 Serie.Builder dest = new Serie.Builder(serieName);
		 dest.columns(columnNames);
		 mapValues(dest, src);
		 return dest.build();
	}
	
	public void mapValues(Serie.Builder dest, PerfStats src) {
	    List<Object> tmp = new ArrayList<Object>(columnNames.length);
	    getValues(tmp, src);
	    dest.values(tmp.toArray());
	}
	
}
