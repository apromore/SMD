package org.prom5.analysis.epc.epcmetrics;


/**
 * @author   Daniel Teixeira
 */
public interface ICalculator {
	public String Calculate();
	public String VerifyBasicRequirements();
	public String getType();
	public String getName();
}
