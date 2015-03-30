package org.prom5.analysis.performance.executiontimes;

import org.prom5.analysis.Analyzer;
import org.prom5.framework.log.LogReader;


public class ExecutionTimesPlugin {
    
    public final static String NAME = "Execution Times Using Availability Based on Hours Per Shift";
    
    @Analyzer(name = NAME, names = { "Log file" }, help = "http://prom.win.tue.nl/research/wiki/onlinedoc/wiki/exectimesbasedworkshifts?s=availability")
    public static ExecutionTimesResultUI analyse(LogReader log) {
        return new ExecutionTimesResultUI(log);
    }
}
