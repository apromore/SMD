package org.prom5.analysis.performance.basicperformance.model.task;

import java.util.ArrayList;
import java.util.Arrays;

import org.prom5.analysis.performance.basicperformance.model.AbstractPerformance;
import org.prom5.framework.log.LogReader;

public class TaskTPerformance extends AbstractPerformance {
	
	public TaskTPerformance(LogReader inputLog){
		super("Task", "Task performance", new ArrayList(Arrays.asList(inputLog.getLogSummary().getModelElements())));
	}
	
	public static String getNameCode()
	{
		return "Task";
	}
}
