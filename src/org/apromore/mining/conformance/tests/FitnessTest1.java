package org.apromore.mining.conformance.tests;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apromore.anf.AnnotationsType;
import org.apromore.canoniser.adapters.Canonical2PNML;
import org.apromore.canoniser.adapters.PNML2Canonical;
import org.apromore.canoniser.adapters.pnml2canonical.NamespaceFilter;
import org.apromore.cpf.CanonicalProcessType;
import org.apromore.exception.CanoniserException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.mining.utils.CycleFixer;
import org.apromore.pnml.ObjectFactory;
import org.apromore.pnml.PnmlType;
import org.apromore.service.impl.CanoniserServiceImpl;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.epc.EPCConversion;
import org.processmining.plugins.pnml.Pnml;
import org.prom5.analysis.conformance.ConformanceAnalysisConfiguration;
import org.prom5.analysis.conformance.ConformanceLogReplayResult;
import org.prom5.analysis.conformance.ConformanceMeasurer;
import org.prom5.converting.EPCToPetriNetConverterPlugin;
import org.prom5.converting.HNNetToEPCConverter;
import org.prom5.converting.HNetToPetriNetConverter;
import org.prom5.converting.PetriNetToWFNet;
import org.prom5.converting.WFNetToEPC;
import org.prom5.exporting.epcs.EpmlExport;
import org.prom5.exporting.petrinet.PnmlExport;
import org.prom5.framework.log.LogFile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.classic.LogReaderClassic;
import org.prom5.framework.models.epcpack.ConfigurableEPC;
import org.prom5.framework.models.epcpack.algorithms.EPCToPetriNetConverter;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.framework.models.petrinet.PetriNet;
import org.prom5.framework.models.petrinet.WFNet;
import org.prom5.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;
import org.prom5.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisResult;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.framework.ui.Progress;
import org.prom5.importing.epml.epmlImport;
import org.prom5.mining.MiningResult;
import org.prom5.mining.epcmining.EPCResult;
import org.prom5.mining.heuristicsmining.HeuristicsMiner;
import org.prom5.mining.heuristicsmining.HeuristicsMinerParameters;
import org.prom5.mining.heuristicsmining.HeuristicsNetResult;
import org.prom5.mining.petrinetmining.PetriNetResult;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class FitnessTest1 {

	public static void main(String[] args) {
		FitnessTest1 ft1 = new FitnessTest1();
		try {
//			ft1.test2();
//			ft1.runTest();
//			ft1.testCanonization();
//			ft1.test4();
//			ft1.format();
			ft1.analyzeFolder();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void analyzeFolder() {
		
//		String folderPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/pcm/logs";
		String folderPath = "/home/cn/eps/apromore-mining/tests/t2/prob_logs";
//		String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/pcm/fa.csv";
		String outPath = "/home/cn/eps/apromore-mining/tests/t2/fa.csv";
//		String folderPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs1";
		
		File outFile = new File(outPath);
		String header = "Log file name,Fitness,Appropriateness\n";
		try {
			FileUtils.write(outFile, header, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		double totalFitness = 0;
		double totalA = 0;
		File folder = new File(folderPath);
		File[] logFiles = folder.listFiles();
		int count = 0;
		for (File logFile : logFiles) {
			count++;
			double[] conf = computeFitness(logFile);
			System.out.println(count + " of " + logFiles.length + ". Fitness: " + conf[0] + ", Appropriateness: " + conf[1]);
			String dataLine = logFile.getName() + "," + conf[0] + "," + conf[1] + "\n";
			try {
				FileUtils.write(outFile, dataLine, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			totalFitness += conf[0];
			totalA += conf[1];
		}
		
		double avgFitness = totalFitness / logFiles.length;
		double avgA = totalA / logFiles.length;
		System.out.println("Average fitness: " + avgFitness);
		System.out.println("Average appropriateness: " + avgA);
	}
	
	class EmptyProgress extends Progress {
		@Override
		public void setNote(String note) {}

		@Override
		public void setProgress(int nv) {}
	}
	
	private double[] computeFitness(File logFile) {
		
		System.out.println("Analyzing " + logFile.getName());
		
		try {
			LogReader log = readLog(logFile.getAbsolutePath());
			PetriNet pn = minePNML(log);
			
//			LogReplayAnalysisMethod logReplayAnalysis = new LogReplayAnalysisMethod(
//					pn, log, new ConformanceMeasurer(),
//					new Progress("Log replay analysis.."));
			LogReplayAnalysisMethod logReplayAnalysis = new LogReplayAnalysisMethod(
					pn, log, new ConformanceMeasurer(),
					new EmptyProgress());
			logReplayAnalysis.setMaxDepth(1);
			logReplayAnalysis.findBestShortestSequence = true;
			System.out.println("Analysing the fitness...");
//			ConformanceAnalysisConfiguration analysisOptions = new ConformanceAnalysisConfiguration();
			ConformanceAnalysisConfiguration analysisOptions = new MyAnalysisOptions();
			ConformanceLogReplayResult analysisResult = (ConformanceLogReplayResult) logReplayAnalysis.analyse(analysisOptions);
			double fitness = analysisResult.getFitnessMeasure();
			double appropriateness = analysisResult.getBehavioralAppropriatenessMeasure();
			return new double[] {fitness, appropriateness};
//			System.out.println("Fitness: " + analysisResult.getFitnessMeasure());
			
		} catch (Exception e) {
			System.out.println("Failed to compute fitness of log: " + logFile.getAbsolutePath());
			return new double[] {-1, -1};
		}
	}

	public void format() {
		String epmlPath1 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/e_test1.epml";
		String epmlPath2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/e_test2.epml";
		
		CPF cpf = new EPCDeserializer().deserializeFile(epmlPath1);
		new FormattableEPCSerializer().serialize(cpf, epmlPath2);
	}
	
	public void test4() throws Exception {
		String logsPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/logs";
		String epmlPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/e_test1.epml";
		String epmlPath2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/e_test2.epml";
		String pnmlPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/pn_test1.pnml";
		String pnmlPath2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/pn_test2.pnml";
		
		String logPath = new File(logsPath).listFiles()[0].getAbsolutePath();
		LogReader log = readLog(logPath);

		ConfigurableEPC epc = mineEPML(log);
		String epml = epcToEPML(epc);
		FileUtils.write(new File(epmlPath), epml);
		System.out.println("Wrote the original epml.");
		
		PetriNet pn = minePNML(log);
		String pnml = pnToPNML(pn);		
		FileUtils.write(new File(pnmlPath), pnml);
		System.out.println("Wrote the original pnml.");
		
		CPF cpf = new EPCDeserializer().deserializeInputStream(IOUtils.toInputStream(epml));
		CycleFixer.fixCycles(cpf);
		String epml2 = new EPCSerializer().serializeToString(cpf);
		FileUtils.write(new File(epmlPath2), epml2);
		System.out.println("Wrote the fixed epml.");
		
		ConfigurableEPC epc2 = epmlTpEPC(epml2);
		HashMap connectorMapping = new HashMap();
		PetriNet pn2 = EPCToPetriNetConverter.convert(epc, connectorMapping);
		System.out.println("Coverted to a petri net.");
		String pnml2 = pnToPNML(pn2);
		FileUtils.write(new File(pnmlPath2), pnml2);
		System.out.println("Wrote the fixed and converted pnml.");
		
		LogReplayAnalysisMethod logReplayAnalysis = new LogReplayAnalysisMethod(
				pn, log, new ConformanceMeasurer(),
				new Progress("Log replay analysis.."));
		logReplayAnalysis.setMaxDepth(1);
		logReplayAnalysis.findBestShortestSequence = true;
		System.out.println("Analysing the fitness...");
		ConformanceAnalysisConfiguration analysisOptions = new ConformanceAnalysisConfiguration();
		ConformanceLogReplayResult analysisResult = (ConformanceLogReplayResult) logReplayAnalysis.analyse(analysisOptions);
		System.out.println("Fitness: " + analysisResult.getFitnessMeasure());
	}
	
	private String epcToEPML(ConfigurableEPC epc) throws Exception {
		ProvidedObject po2 = new ProvidedObject("epc_result", new Object[] {epc});
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		EpmlExport exporter = new EpmlExport();
		exporter.export(po2, output);
		String epml = output.toString();
		epml = epml.replaceAll("&", "-");
		epml = removeEvents(epml);
		return epml;
	}
	
	private ConfigurableEPC epmlTpEPC(String epml) throws Exception {
		InputStream in = IOUtils.toInputStream(epml);
		epmlImport epmlI = new epmlImport();
		EPCResult epcResult = (EPCResult) epmlI.importFile(in);
		ConfigurableEPC epc = epcResult.getEPC();
		return epc;
	}
	
	private String pnToPNML(PetriNet pn) throws Exception {
		ProvidedObject po = new ProvidedObject("pnml_result", new Object[] {pn});
		ByteArrayOutputStream output2 = new ByteArrayOutputStream();
		PnmlExport pnmlExport = new PnmlExport();
		pnmlExport.export(po, output2);
		String pnml = output2.toString();
		return pnml;
	}

	public void test2() throws Exception {
		
//		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t2/splitted1_commercial_system_subject/log_L_1_9664_40_27.mxml";
		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/splitted5_windscreen_system_subject/log_L_1_8528_78_22.mxml";
		LogReader log = readLog(logPath);
		ConfigurableEPC epc = mineEPML(log);
		System.out.println("Mined a model.");
		
//		InputStream in = IOUtils.toInputStream(epml);
//		epmlImport epmlI = new epmlImport();
//		EPCResult epcResult = (EPCResult) epmlI.importFile(in);
//		ConfigurableEPC epc = epcResult.getEPC();
//		System.out.println("Imported the epc.");
		
		PetriNet pn = minePNML(log);
		System.out.println("Mined a petri net from the log.");
		
		WFNet wfnet = new PetriNetToWFNet().convert(pn);
		
		WFNetToEPC wfNetToEPC = new WFNetToEPC();
//		ConfigurableEPC epc = wfNetToEPC.convert(wfnet);
		System.out.println("Converted petrinet to epc.");
		
		HashMap connectorMapping = new HashMap();
		PetriNet pn2 = EPCToPetriNetConverter.convert(epc, connectorMapping);
		System.out.println("Converted epc to petri net.");
		
		String pnmlPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/pn1.pnml";
		ProvidedObject po = new ProvidedObject("pnml_result", new Object[] {pn2});
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PnmlExport pnmlExport = new PnmlExport();
		pnmlExport.export(po, output);
		String pnml = output.toString();
		FileUtils.write(new File(pnmlPath), pnml);
		System.out.println("Completed writing to the file.");
		
//		LogReplayAnalysisMethod logReplayAnalysis = new LogReplayAnalysisMethod(
//				pn2, log, new ConformanceMeasurer(),
//				new Progress("Log replay analysis.."));
//		logReplayAnalysis.setMaxDepth(1);
//		logReplayAnalysis.findBestShortestSequence = true;
//		System.out.println("Analysing the fitness...");
//		ConformanceAnalysisConfiguration analysisOptions = new ConformanceAnalysisConfiguration();
//		ConformanceLogReplayResult analysisResult = (ConformanceLogReplayResult) logReplayAnalysis.analyse(analysisOptions);
//		System.out.println("Fitness: " + analysisResult.getFitnessMeasure());
	}
	
	private LogReader readLog(String logPath) {
		LogFile lf = LogFile.getInstance(logPath);
		LogReader lr = LogReaderClassic.createInstance(null, lf);
		return lr;
	}
	
	private PetriNet minePNML(LogReader log) throws Exception {
		
		HeuristicsMiner miner = new HeuristicsMiner();
		HeuristicsMinerParameters params = miner.getParameters();
		params.setAndThreshold(0.1);
		
		HeuristicsNetResult result = (HeuristicsNetResult) miner.mine(log);
		HeuristicsNet net = result.getHeuriticsNet();
		
		ProvidedObject po = new ProvidedObject("hm results", new Object[] {net, log});
		HNetToPetriNetConverter hpc = new HNetToPetriNetConverter();
		PetriNetResult pnResult = (PetriNetResult) hpc.convert(po);
		PetriNet pn = pnResult.getPetriNet();
		return pn;
	}
	
	private ConfigurableEPC mineEPML(LogReader log) throws Exception {
		
		HeuristicsMiner miner = new HeuristicsMiner();
		HeuristicsMinerParameters params = miner.getParameters();
		params.setAndThreshold(0.1);
		
		HeuristicsNetResult result = (HeuristicsNetResult) miner.mine(log);
		HeuristicsNet net = result.getHeuriticsNet();
		
		ProvidedObject po = new ProvidedObject("hm results", new Object[] {net, log});
		HNNetToEPCConverter converter = new HNNetToEPCConverter();
		EPCResult epcResult = (EPCResult) converter.convert(po);
		
		ConfigurableEPC epc = epcResult.getEPC();
		return epc;
//		ProvidedObject po2 = new ProvidedObject("epc result", new Object[] {epc});
//		ByteArrayOutputStream output = new ByteArrayOutputStream();
//		EpmlExport exporter = new EpmlExport();
//		exporter.export(po2, output);
//		String epml = output.toString();
//		return epml;
	}
	
	public void testCanonization() {
		
		String pnmlInPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/test_models/05_Insurance.pnml";
//		String pnmlInPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/pn1.pnml";
		
		
//		String pnmlInPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/pn_3_example.pnml";
		String pnmlOutPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/pn_4_example.pnml";
		String epmlOutPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/epml_4_example.epml";
		String epmlInPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/p1.epml";
		
		try {
//			CPF cpf = pnmlToCPF(pnmlInPath);
			CPF cpf = new EPCDeserializer().deserializeFile(epmlInPath);
//			new EPCSerializer().serializeToFile(cpf, epmlOutPath);
			cpfToPnml(cpf, pnmlOutPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void cpfToPnml(CPF cpfModel, String outpath) throws Exception {
		
		File output = new File(outpath);
		
		JAXBContext jc = JAXBContext
                .newInstance("org.apromore.cpf");
//        Unmarshaller u = jc.createUnmarshaller();
//        JAXBElement<CanonicalProcessType> rootElement = (JAXBElement<CanonicalProcessType>) u
//                .unmarshal(cpf_file);
//        CanonicalProcessType cpf = rootElement.getValue();
        
        CanoniserServiceImpl cs = new CanoniserServiceImpl();
        CanonicalProcessType cpf = cs.serializeCPF(cpfModel);

        // Canonical2EPML canonical2epml_1 = new
        // Canonical2EPML(cpf,true);

        jc = JAXBContext.newInstance("org.apromore.pnml");

        Canonical2PNML canonical2pnml = new Canonical2PNML(cpf);

        Marshaller m1 = jc.createMarshaller();

        NamespaceFilter outFilter = new NamespaceFilter(null, false);

        OutputFormat format = new OutputFormat();
        format.setIndent(true);
        format.setNewlines(true);
        format.setXHTML(true);
        format.setExpandEmptyElements(true);
        format.setNewLineAfterDeclaration(false);

        XMLWriter writer = null;
        try {
            writer = new XMLWriter(new FileOutputStream(output),
                    format);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Attach the writer to the filter
        outFilter.setContentHandler(writer);

        // Tell JAXB to marshall to the filter which in turn will
        // call the writer
        m1.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        JAXBElement<PnmlType> cprocRootElem1 = new ObjectFactory().createPnml(canonical2pnml.getPNML());
        m1.marshal(cprocRootElem1, outFilter);
	}
	
	public CPF pnmlToCPF(String filepath) throws Exception {
		
		JAXBContext jc = JAXBContext.newInstance("org.apromore.pnml");
		Unmarshaller u = jc.createUnmarshaller();
		XMLReader reader = XMLReaderFactory.createXMLReader();

		// Create the filter (to add namespace) and set the xmlReader as its parent.
		NamespaceFilter inFilter = new NamespaceFilter("pnml.apromore.org", true);
		inFilter.setParent(reader);

		// Prepare the input, in this case a java.io.File (output)
		File file = new File(filepath);
		InputSource is = new InputSource(new FileInputStream(file));

		// Create a SAXSource specifying the filter
		SAXSource source = new SAXSource(inFilter, is);
		JAXBElement<PnmlType> rootElement = (JAXBElement<PnmlType>) u.unmarshal(source);
		PnmlType pnml = rootElement.getValue();

		PNML2Canonical pn = new PNML2Canonical(pnml, file.getName());
		
		CanonicalProcessType cpt = pn.getCPF();
		CanoniserServiceImpl cs = new CanoniserServiceImpl();
		CPF cpf = cs.deserializeCPF(cpt);
		System.out.println(cpf.getFlowNodes().size());
		
		return cpf;
	}
	
	public void runTest() throws Exception {
		
		String epmlPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/1An_ka9y.epml";
		String pnmlPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/pn1.pnml";
		String epml = FileUtils.readFileToString(new File(epmlPath));
		InputStream in = IOUtils.toInputStream(epml);
		
		epmlImport epmlI = new epmlImport();
		EPCResult epcResult = (EPCResult) epmlI.importFile(in);
		
		ConfigurableEPC epc = epcResult.getEPC();
		System.out.println("Imported the epc.");
		
//		EPCToPetriNetConverterPlugin epcToPn = new EPCToPetriNetConverterPlugin();
		
		HashMap connectorMapping = new HashMap();
		PetriNet pn = EPCToPetriNetConverter.convert(epc, connectorMapping);
		System.out.println("Coverted to a petri net.");
		
		ProvidedObject po = new ProvidedObject("pnml_result", new Object[] {pn});
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PnmlExport pnmlExport = new PnmlExport();
		pnmlExport.export(po, output);
		String pnml = output.toString();
		FileUtils.write(new File(pnmlPath), pnml);
		System.out.println("Completed writing to the file.");
//		System.out.println(pnml);
	}
	
	private String removeEvents(String originalEPML) {
		
		CPF model = new EPCDeserializer().deserializeInputStream(IOUtils.toInputStream(originalEPML));
		CPFTransformer.correct(model);
		
		for (FlowNode v : model.getVertices()) {
			if (v instanceof CpfEvent) {
				String name = v.getName();
				if ("fictive start".equals(name) || "fictive end".equals(name)) {
					continue;
				}
				
				Collection<FlowNode> preset = model.getDirectPredecessors(v);
				Collection<FlowNode> postset = model.getDirectSuccessors(v);
				if (preset.size() > 1 || postset.size() > 1) {
					continue;
				}
				
				Collection<ControlFlow<FlowNode>> edgeToRemove = new ArrayList<ControlFlow<FlowNode>>();
				edgeToRemove.addAll(model.getIncomingEdges(v));
				edgeToRemove.addAll(model.getOutgoingEdges(v));
				model.removeEdges(edgeToRemove);
				model.removeVertex(v);
				
				if (preset.size() == 1 && postset.size() == 1) {
					FlowNode n1 = preset.iterator().next();
					FlowNode n2 = postset.iterator().next();
					model.addEdge(n1, n2);
				}
			}
		}
		
		String newEPML = new EPCSerializer().serializeToString(model);
		return newEPML;
	}
	
}
