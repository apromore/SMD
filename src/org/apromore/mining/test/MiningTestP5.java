package org.apromore.mining.test;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apromore.dao.ProcessDao;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.ProcessMiner;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.mining.utils.CPFtoMultiDirectedGraphConverter;
import org.apromore.mining.utils.SingleTerminalCycleFormer;
import org.apromore.service.FragmentService;
import org.apromore.service.ProcessService;
import org.apromore.service.RepositoryService;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.graph.algo.StronglyConnectedComponents;
import org.jbpt.hypergraph.abs.Vertex;
import org.prom5.framework.log.LogFile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.rfb.BufferedLogReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class MiningTestP5 {
	
	private String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp/miningtests/log";
	private String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp/miningtests/model_p5.epml";
	
	private static DirectedGraphAlgorithms<DirectedEdge, Vertex> ga = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	private static StronglyConnectedComponents<DirectedEdge, Vertex> sa = new StronglyConnectedComponents<DirectedEdge, Vertex>();
	
	@Autowired
	private ProcessMiner processMiner;
	
	@Autowired
	RepositoryService rsrv;
	
	@Autowired
	private ProcessService psrv;
	
	@Autowired @Qualifier("FragmentService")
	private FragmentService fsrv;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdoa;
	
	public void mine() throws Exception {

		String outpath2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp/miningtests/model_p5_exported.epml";
		
		rsrv.clearRepositoryContent();
		
		CPF model = new EPCDeserializer().deserializeFile(outPath);
		
		String modelString = new EPCSerializer().serializeToString(model);
		psrv.addProcessModel("p1", modelString);
		
		String rootId = pdoa.getRootFragmentId("p1");
		CPF model2 = fsrv.getFragment(rootId, false);
		
		FormattableEPCSerializer epcSerializer = new FormattableEPCSerializer();
		epcSerializer.serialize(model2, outpath2);
		
	}
 	
	public void mine2() throws Exception {
		
		rsrv.clearRepositoryContent();
		
		File logFolder = new File(logPath);
		String logFilePath = logFolder.listFiles()[0].getAbsolutePath();
		
		LogFile lf = LogFile.getInstance(logFilePath);
		LogReader log = BufferedLogReader.createInstance(null, lf);
		
		String epml = processMiner.mineEPC(log);
		
		CPF model = new EPCDeserializer().deserializeInputStream(IOUtils.toInputStream(epml));
		CPFTransformer.correct(model);
		SingleTerminalCycleFormer.formSingleTerminalCycles(model);
		
		FormattableEPCSerializer epcSerializer = new FormattableEPCSerializer();
		epcSerializer.serialize(model, outPath);
		
//		String modelString = new EPCSerializer().serializeToString(model);
//		psrv.addProcessModel("p1", modelString);
//		
//		String rootId = pdoa.getRootFragmentId("p1");
//		CPF model2 = fsrv.getFragment(rootId, false);
//		
//		FormattableEPCSerializer epcSerializer = new FormattableEPCSerializer();
//		epcSerializer.serialize(model2, outPath);
		
		MultiDirectedGraph mdg = CPFtoMultiDirectedGraphConverter.covert(model);
		if (ga.isMultiTerminal(mdg)) {
			System.out.println("Model is multiterminal");
		} else {
			System.out.println("Model is NOT multiterminal");
		}
	}
}
