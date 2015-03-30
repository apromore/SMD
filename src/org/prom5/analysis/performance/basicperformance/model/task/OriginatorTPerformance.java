package org.prom5.analysis.performance.basicperformance.model.task;

import java.util.ArrayList;
import java.util.Arrays;

import org.prom5.analysis.performance.basicperformance.model.AbstractPerformance;
import org.prom5.framework.log.LogReader;

public class OriginatorTPerformance extends AbstractPerformance {
	public OriginatorTPerformance(LogReader inputLog){
		super("Originator", "Originator performance", new ArrayList(Arrays.asList(inputLog.getLogSummary().getOriginators())));
	}
	
	public static String getNameCode()
	{
		return "Originator";
	}
}
