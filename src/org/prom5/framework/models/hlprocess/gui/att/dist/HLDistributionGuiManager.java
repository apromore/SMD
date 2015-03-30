package org.prom5.framework.models.hlprocess.gui.att.dist;

import org.prom5.framework.models.hlprocess.distribution.HLBernoulliDistribution;
import org.prom5.framework.models.hlprocess.distribution.HLBinomialDistribution;
import org.prom5.framework.models.hlprocess.distribution.HLChiSquareDistribution;
import org.prom5.framework.models.hlprocess.distribution.HLConstantDistribution;
import org.prom5.framework.models.hlprocess.distribution.HLDiscreteDistribution;
import org.prom5.framework.models.hlprocess.distribution.HLDistribution;
import org.prom5.framework.models.hlprocess.distribution.HLDistribution.DistributionEnum;
import org.prom5.framework.models.hlprocess.distribution.HLErlangDistribution;
import org.prom5.framework.models.hlprocess.distribution.HLExponentialDistribution;
import org.prom5.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.prom5.framework.models.hlprocess.distribution.HLNormalDistribution;
import org.prom5.framework.models.hlprocess.distribution.HLPoissonDistribution;
import org.prom5.framework.models.hlprocess.distribution.HLStudentDistribution;
import org.prom5.framework.models.hlprocess.distribution.HLUniformDistribution;
import org.prom5.framework.ui.Message;
import org.prom5.framework.util.GuiNotificationTarget;

public class HLDistributionGuiManager {
	
	/**
	 * Creates a new distribution GUI based on the kind of passed distribution object.
	 * Changes on the GUI will be directly reported back to the underlying distribution.
	 * @param parent the distribution for which the GUI is requested
	 * @return the distribution GUI of matching type
	 */
	public static HLDistributionGui getDistributionGui(HLDistribution parent, GuiNotificationTarget target) {
		if (parent instanceof HLGeneralDistribution) {
			return new HLGeneralDistributionGui((HLGeneralDistribution) parent, target);
		} else if (parent instanceof HLConstantDistribution) {
			return new HLConstantDistributionGui((HLConstantDistribution) parent);
		} else if (parent instanceof HLNormalDistribution) {
			return new HLNormalDistributionGui((HLNormalDistribution) parent);
		} else if (parent instanceof HLUniformDistribution) {
			return new HLUniformDistributionGui((HLUniformDistribution) parent);
		} else if (parent instanceof HLExponentialDistribution) {
			return new HLExponentialDistributionGui((HLExponentialDistribution) parent);
		} else if (parent instanceof HLPoissonDistribution) {
			return new HLPoissonDistributionGui((HLPoissonDistribution) parent);
		} else if (parent instanceof HLStudentDistribution) {
			return new HLStudentDistributionGui((HLStudentDistribution) parent);
		} else if (parent instanceof HLBernoulliDistribution) {
			return new HLBernoulliDistributionGui((HLBernoulliDistribution) parent);
		} else if (parent instanceof HLBinomialDistribution) {
			return new HLBinomialDistributionGui((HLBinomialDistribution) parent);
		} else if (parent instanceof HLChiSquareDistribution) {
			return new HLChiSquareDistributionGui((HLChiSquareDistribution) parent);
		} else if (parent instanceof HLDiscreteDistribution) {
			return new HLDiscreteDistributionGui((HLDiscreteDistribution) parent);
		} else if (parent instanceof HLErlangDistribution) {
			return new HLErlangDistributionGui((HLErlangDistribution) parent);
		} else {
			Message.add("Requested distribution could not be found!");
			return null;
		}
	}

	/**
	 * Creates a new distribution GUI of the requested type but based on the meta distribution object
	 * held as a parent (so that changes are directly reported back to the HLGeneralDistribution itself).
	 * @param requestedType the type of GUI requested
	 * @return the distribution GUI
	 */
	public static HLDistributionGui getDistributionGui(DistributionEnum requestedType, 
			HLGeneralDistribution parent) {
		if (requestedType == DistributionEnum.CONSTANT_DISTRIBUTION) {
			return new HLConstantDistributionGui(parent);
		} else if (requestedType == DistributionEnum.NORMAL_DISTRIBUTION) {
			return new HLNormalDistributionGui(parent);
		} else if (requestedType == DistributionEnum.UNIFORM_DISTRIBUTION) {
			return new HLUniformDistributionGui(parent);
		} else if (requestedType == DistributionEnum.EXPONENTIAL_DISTRIBUTION) {
			return new HLExponentialDistributionGui(parent);
		} else if (requestedType == DistributionEnum.POISSON_DISTRIBUTION) {
			return new HLPoissonDistributionGui(parent);
		} else if (requestedType == DistributionEnum.STUDENT_DISTRIBUTION) {
			return new HLStudentDistributionGui(parent);
		} else if (requestedType == DistributionEnum.BERNOULLI_DISTRIBUTION) {
			return new HLBernoulliDistributionGui(parent);
		} else if (requestedType == DistributionEnum.BINOMIAL_DISTRIBUTION) {
			return new HLBinomialDistributionGui(parent);
		} else if (requestedType == DistributionEnum.CHISQUARE_DISTRIBUTION) {
			return new HLChiSquareDistributionGui(parent);
		} else if (requestedType == DistributionEnum.DISCRETE_DISTRIBUTION) {
			return new HLDiscreteDistributionGui(parent);
		} else if (requestedType == DistributionEnum.ERLANG_DISTRIBUTION) {
			return new HLErlangDistributionGui(parent);
		} else {
			Message.add("Requested distribution could not be found!");
			return null;
		}
	}
	
}
