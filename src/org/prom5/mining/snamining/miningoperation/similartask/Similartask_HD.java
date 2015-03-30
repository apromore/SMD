package org.prom5.mining.snamining.miningoperation.similartask;

import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.LogSummary;
import org.prom5.mining.snamining.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleMatrix2D;

public class Similartask_HD extends SimilartaskBase {

	// consider multiple transfer

	public Similartask_HD(LogSummary summary, LogReader log) {
		super(summary, log);
	};

	public DoubleMatrix2D calculation() {
		DoubleMatrix2D OTMatrix = super.makeOTMatrix();

		return UtilOperation.hammingdistance(OTMatrix);
	};
}
