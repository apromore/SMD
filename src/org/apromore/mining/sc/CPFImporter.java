package org.apromore.mining.sc;

import org.apromore.dao.ProcessDao;
import org.apromore.exception.ImportException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.AggregatedLogClusterer;
import org.apromore.mining.ProcessMiner;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.mining.utils.CPFtoMultiDirectedGraphConverter;
import org.apromore.mining.utils.CycleFixer;
import org.apromore.mining.utils.CycleRemover;
import org.apromore.mining.utils.SingleTerminalCycleFormer;
import org.apromore.service.ProcessService;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.hypergraph.abs.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class CPFImporter {
	
	private static final Logger logger = LoggerFactory.getLogger(CPFImporter.class);
	
	@Autowired
	private ProcessService psrv;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdao;
	
	private DirectedGraphAlgorithms<DirectedEdge, Vertex> algo = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	
	private EPCSerializer epcSerializer = new EPCSerializer();
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	
	public boolean importModel(String processName, CPF model) {
		
		boolean success = true;
		
		CPFTransformer.correct(model);
		SingleTerminalCycleFormer.formSingleTerminalCycles(model);
		String epmlString = epcSerializer.serializeToString(model);
		
		try {
			if (pdao.getProcess(processName) == null) {
				MultiDirectedGraph mdg = CPFtoMultiDirectedGraphConverter.covert(model);
	            if (algo.isMultiTerminal(mdg)) {
	            	psrv.addProcessModel(processName, epmlString);
	            } else {
	            	logger.info("Process model {} is not multiterminal. Removing cycles...", processName);
//	            	boolean multiterminal = CycleRemover.removeCycles(model, mdg, true);
	            	boolean multiterminal = CycleFixer.fixCycles(model);
	            	if (multiterminal) {
	            		logger.debug("Process model {} is multiterminal after removing cycles. Attempting to add the refined model...", processName);
	            		epmlString = epcSerializer.serializeToString(model);
	            		psrv.addProcessModel(processName, epmlString);
	            		logger.debug("Process model {} of size {} is successfully added after cycle removal.", processName, model.getVertices().size());
	            	} else {
	            		throw new ImportException("Process model is not multiterminal after cycle removal.");
	            	}
	            }
				
			} else {
				String msg = processName + " already exists. Skipped the import.";
				logger.info(msg);
			}
			
		} catch (Exception e) {
			logger.error("Process model {} is not valid. Queuing it for reclustering...", processName);
			success = false;
		}
		
		return success;
	}
}
