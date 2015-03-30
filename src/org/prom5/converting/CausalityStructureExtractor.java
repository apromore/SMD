package org.prom5.converting;

import org.prom5.framework.models.causality.CausalFootprint;
import org.prom5.framework.models.causality.CausalityFootprintFactory;
import org.prom5.framework.models.epcpack.ConfigurableEPC;
import org.prom5.framework.models.petrinet.PetriNet;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.mining.MiningResult;
import org.prom5.mining.instancemining.ModelGraphResult;

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
public class CausalityStructureExtractor implements ConvertingPlugin {
	public CausalityStructureExtractor() {
	}

	public String getName() {
		return "Extract Causal Footprint";
	}

	public String getHtmlDescription() {
		return "This plugin extracts the causalit footprint from any EPC or PetriNet";
	}

	public boolean accepts(ProvidedObject original) {
		int i = 0;
		boolean b = false;
		while (!b && (i < original.getObjects().length)) {
			b |= (original.getObjects()[i] instanceof ConfigurableEPC);
			b |= (original.getObjects()[i] instanceof PetriNet);
			i++;
		}
		return b;
	}

	public MiningResult convert(ProvidedObject original) {
		int i = 0;
		boolean b = false;
		while (!b && (i < original.getObjects().length)) {
			b |= (original.getObjects()[i] instanceof ConfigurableEPC);
			b |= (original.getObjects()[i] instanceof PetriNet);
			i++;
		}
		Object o = original.getObjects()[i - 1];
		CausalFootprint cs = null;
		if (o instanceof ConfigurableEPC) {
			ConfigurableEPC epc = (ConfigurableEPC) o;
			cs = CausalityFootprintFactory.make(epc);
			cs.Test("EPCconvertedTo");
		} else if (o instanceof PetriNet) {
			PetriNet pn = (PetriNet) o;
			cs = CausalityFootprintFactory.make(pn);
			cs.Test("PNetConvertedTo");
		}
		return new ModelGraphResult(cs);
	}
}
