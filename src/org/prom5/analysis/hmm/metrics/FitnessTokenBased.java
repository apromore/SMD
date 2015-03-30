package org.prom5.analysis.hmm.metrics;

import org.prom5.analysis.benchmark.metric.TokenFitnessMetric;
import org.prom5.analysis.hmm.HmmAnalyzer;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.petrinet.PetriNet;
import org.prom5.framework.ui.Progress;

/**
 * Conformance Checker Token fitness metric for HMM experiment. 
 * 
 * @author Anne Rozinat (a.rozinat at tue.nl)
 */
public class FitnessTokenBased extends HmmExpMetric {
	
	public FitnessTokenBased(String aFolder) {
		super(aFolder, "Token Based", MetricType.Fitness, ExpType.Noise);
	}

	public double calculateValue(HmmAnalyzer analyzer, PetriNet pnet, 
			LogReader log) {
		TokenFitnessMetric tokenFitnessMetric = new TokenFitnessMetric();
		return tokenFitnessMetric.measure(pnet, log, null, new Progress(""));
	}

}
