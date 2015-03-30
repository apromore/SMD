package org.processmining.plugins.epc;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.reduction.Murata;

@Plugin(name = "Convert EPC to Petri Net", parameterLabels = { "EPC" }, returnLabels = { "Petri net" }, returnTypes = { Petrinet.class }, userAccessible = true)
public class EPCtoPNPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack = "EPCConversion")
	@PluginVariant(variantLabel = "Select options to use", requiredParameterLabels = { 0 })
	public static Petrinet main(final UIPluginContext context, final ConfigurableEPC epc) throws ConnectionCannotBeObtained {
		EPCConversion conversion = new EPCConversion();
		Petrinet net = conversion.convertToPN(epc);
		Marking marking = new Marking();
		Murata murata = new Murata();
		Object object[] = murata.run(null, net, marking);
		return (Petrinet) object[0];
	}
}
