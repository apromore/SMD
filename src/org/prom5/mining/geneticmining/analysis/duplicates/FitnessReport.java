/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.prom5.mining.geneticmining.analysis.duplicates;

import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.mining.geneticmining.fitness.FitnessFactory;
import org.prom5.mining.geneticmining.fitness.duplicates.DTFitnessFactory;

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
public class FitnessReport {
	HeuristicsNet[] pop = null;
	public FitnessReport(LogReader log, HeuristicsNet net, int indexFitnessType) {
		pop = new HeuristicsNet[] {net.copyNet()};
		pop = FitnessFactory.getFitness(indexFitnessType,
			  log,DTFitnessFactory.ALL_FITNESS_PARAMETERS).calculate(pop);
	}

	public double getFitness() {
		return pop[0].getFitness();
	}

	public HeuristicsNet getNet() {
		return pop[0];
	}
}
