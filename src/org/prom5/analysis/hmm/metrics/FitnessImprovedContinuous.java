package org.prom5.analysis.hmm.metrics;

import org.prom5.analysis.benchmark.metric.ImprovedContinuousSemanticsFitnessMetric;
import org.prom5.analysis.hmm.HmmAnalyzer;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.petrinet.PetriNet;
import org.prom5.framework.ui.Progress;

/**
 * Improved continuous metric from Genetic miner for HMM experiment. 
 * 
 * @author Anne Rozinat (a.rozinat at tue.nl)
 */
public class FitnessImprovedContinuous extends HmmExpMetric {
	
	public FitnessImprovedContinuous(String aFolder) {
		super(aFolder, "Improved Continous", MetricType.Fitness, ExpType.Noise);
	}

	public double calculateValue(HmmAnalyzer analyzer, PetriNet pnet, 
			LogReader log) {
		ImprovedContinuousSemanticsFitnessMetric improvedContinuousFitnessMetric = new ImprovedContinuousSemanticsFitnessMetric();
		return improvedContinuousFitnessMetric.measure(pnet, log, null, new Progress(""));
	}

}
