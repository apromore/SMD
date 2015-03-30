package org.prom5.analysis.hmm.metrics;

import org.prom5.analysis.hmm.HmmAnalyzer;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.petrinet.PetriNet;

/**
 * New Hmm-based trace fitness metric for HMM experiment. 
 * 
 * @author Anne Rozinat (a.rozinat at tue.nl)
 */
public class FitnessTraceAverage extends HmmExpMetric {
	
	public FitnessTraceAverage(String aFolder) {
		super(aFolder, "Trace Average", MetricType.Fitness, ExpType.Noise);
	}

	public double calculateValue(HmmAnalyzer analyzer, PetriNet pnet,
			LogReader log) {
		return analyzer.getMetrics().getAverageTraceFitness();
	}

}
