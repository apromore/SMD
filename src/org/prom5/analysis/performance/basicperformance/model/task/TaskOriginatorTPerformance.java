package org.prom5.analysis.performance.basicperformance.model.task;

import java.util.ArrayList;
import java.util.Arrays;

import org.prom5.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.prom5.framework.log.LogReader;

public class TaskOriginatorTPerformance extends AbstractPerformance2D {

	public TaskOriginatorTPerformance(LogReader inputLog){
		super("Task-Originator", "Task", "Originator", "Task-Originator performance", 
				new ArrayList<String>(Arrays.asList(inputLog.getLogSummary().getModelElements())),
				new ArrayList<String>(Arrays.asList(inputLog.getLogSummary().getOriginators()))
		);
	}
	
	public static String getNameCode()
	{
		return "Task-Originator";
	}
}
