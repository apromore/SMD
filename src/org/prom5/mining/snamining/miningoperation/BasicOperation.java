package org.prom5.mining.snamining.miningoperation;

import org.prom5.framework.log.LogEvents;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.LogSummary;

import cern.colt.matrix.DoubleMatrix2D;

public abstract class BasicOperation {
    protected LogSummary summary;
	protected String[] users;
	protected LogEvents modelElements;
	protected String[] elements;
	protected LogReader log;

	public BasicOperation(LogSummary summary, LogReader log)
	{
       this.users = summary.getOriginators();
	   this.modelElements = summary.getLogEvents();
	   this.elements = summary.getModelElements();
	   this.summary = summary;
	   this.log = log;
	}

	public DoubleMatrix2D calculation(double beta, int depth)
	{return calculation();};

	public DoubleMatrix2D calculation()
	{return null;};
}

