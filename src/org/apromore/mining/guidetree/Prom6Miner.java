package org.apromore.mining.guidetree;

import org.apromore.graph.JBPT.CPF;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.petrinet.epcconversion.EpcConversion;
import org.prom6.models.heuristics.HeuristicsNet;
import org.prom6.plugins.heuristicsnet.miner.heuristics.converter.HeuristicsNetToPetriNetConverter;
import org.prom6.plugins.heuristicsnet.miner.heuristics.miner.HeuristicsMiner;

public class Prom6Miner {
	
	private FormattableEPCSerializer epcSerializer = new FormattableEPCSerializer();
	
	private static int processNumber = 0;

	public CPF mineMode(XLog log, PluginContext context) {
		
		HeuristicsMiner hminer = new HeuristicsMiner(context, log);
		HeuristicsNet hnet = hminer.mine();
		System.out.println(hnet.getFitness());
		
		Object[] pnresults = HeuristicsNetToPetriNetConverter.converter(context, hnet);
		Petrinet pnet = (Petrinet) pnresults[0];
		System.out.println(pnet.getNodes().size());
		
		ConfigurableEPC epc = EpcConversion.convert(pnet);
		
		CPF cpf = EPCToCPFConverter.convert(epc);
		
		processNumber++;
		String fileName = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp/gtmine/p_" + processNumber + ".epml";
//		epcSerializer.serialize(cpf, fileName);
		
		return cpf;
	}
}
