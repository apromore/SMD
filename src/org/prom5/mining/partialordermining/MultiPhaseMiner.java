package org.prom5.mining.partialordermining;

import org.prom5.converting.AggregationGraphToEPC;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.ui.Progress;
import org.prom5.mining.MiningResult;
import org.prom5.mining.logabstraction.LogRelationBasedAlgorithm;
import org.prom5.mining.logabstraction.LogRelations;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MultiPhaseMiner extends LogRelationBasedAlgorithm{
	public MultiPhaseMiner() {
	}

	public MiningResult mine(LogReader logReader, LogRelations logRelations, Progress progress) {
		progress.setMaximum(progress.getMaximum()+ logReader.numberOfInstances()*2 +1);

		PartialOrderGeneratorPlugin poGen = new PartialOrderGeneratorPlugin();
	    PartialOrderMiningResult poGenResult = (PartialOrderMiningResult) poGen.mineWithProgressSet(logReader,logRelations,progress);

		PartialOrderAggregationPlugin poAgg = new PartialOrderAggregationPlugin();
		AggregationGraphResult aggResult = (AggregationGraphResult) poAgg.mine(poGenResult.getLogReader(),progress, false);

	    progress.setNote("Converting to EPC");
	    AggregationGraphToEPC ag2epc = new AggregationGraphToEPC();
		return ag2epc.convert(aggResult.getProvidedObjects()[0]);
	}

	public String getName() {
		return "Multi-phase Macro Plugin";
	}

	public String getHtmlDescription() {
		return "This plugin is a macro plugin that subsequently calls the Partial Order Generator, then the "+
				"Partial Order Aggregator and finally the Aggregation Graph to EPC converter.";
	}
}
