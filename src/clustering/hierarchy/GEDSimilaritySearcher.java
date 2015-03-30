package clustering.hierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.tue.tm.is.epc.Connector;
import nl.tue.tm.is.epc.EPC;
import nl.tue.tm.is.epc.Node;
import nl.tue.tm.is.graph.SimpleGraph;
import nl.tue.tm.is.led.StringEditDistance;

import org.apromore.graph.JBPT.CPF;
import org.apromore.service.helper.SimpleGraphWrapper;
import org.apromore.service.impl.SimpleGraphAwareComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import clustering.containment.ContainmentRelationImpl;
import clustering.dissimilarity.DissimilarityCalc;
import clustering.dissimilarity.measure.GEDDissimCalc;
import clustering.dissimilarity.measure.SizeBasedDissimCalc;

import com.google.common.collect.TreeMultiset;

public class GEDSimilaritySearcher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GEDSimilaritySearcher.class);
	
	private	List<ResultFragment> resultsFragments = null;
	
	@Autowired @Qualifier("ContainmentRelation")
	private ContainmentRelationImpl crel;
	
	private double dissThreshold = 0.5;
	
	@Autowired @Qualifier("SGAwareComposer")
    private SimpleGraphAwareComposer composer;
	
	/**
	 * Fragment Id -> SimpleGraph object containing all nodes and edges of the fragment
	 */
	private Map<String, SimpleGraph> models = new HashMap<String, SimpleGraph>();
	
	private List<DissimilarityCalc> chain = new LinkedList<DissimilarityCalc>();
	
	long startedTime = 0;
	int nfrag = 0;
	int reportingInterval = 0;
	int comparedFragments = 0;

	public void setContainmentRelation(ContainmentRelationImpl crel) {
		this.crel = crel;
	}
	
	public void setDissThreshold(double dissThreshold) {
		this.dissThreshold = dissThreshold;
		initialize();
	}
	
	public void initialize() {
		try {
			crel.setMinSize(2);
			crel.initialize();
			
			this.chain.clear();
			this.addDissimCalc(new SizeBasedDissimCalc(dissThreshold));
			this.addDissimCalc(new GEDDissimCalc(dissThreshold, dissThreshold));
			
		} catch (Exception e) {
			String msg = "Failed to initialize the GEDSimilaritySearcher for distance threshold: " + dissThreshold;
			LOGGER.error(msg, e);
		}
	}
	
	public List<ResultFragment> search(String fragmentId) {
		SimpleGraph query = getSimpleGraph(fragmentId);
		return search(query);
	}
		
	public List<ResultFragment> search(SimpleGraph query) {
		
		startedTime = System.currentTimeMillis();
		List<String> processedFragmentIds = new ArrayList<String>();
		
		resultsFragments = new ArrayList<ResultFragment>();

		nfrag = crel.getNumberOfFragments();
		System.out.println("Fragments to compare: " + nfrag);
		reportingInterval = 0;
		comparedFragments = 0;
		
		List<String> roots = crel.getRoots();
		for (int p = 0; p < roots.size(); p++) {
			List<String> h1 = crel.getHierarchy(roots.get(p));
			h1.removeAll(processedFragmentIds);
			
			// fill composer cache with details of h1's fragments (i.e. fragments rooted at p)
			getSimpleGraph(roots.get(p));

			computeGEDsWithHierarchy(query, h1);
			
			// at this point we have compared query fragments with all fragments of h1
			// so we can remove all h1's fragments from the cache
			models.keySet().removeAll(h1);
			composer.clearCache(h1);
			
			processedFragmentIds.addAll(h1);
		}
		
		return resultsFragments;
	}

	private void computeDissim(String fid1, SimpleGraph q) {
		
		int fid1Index = crel.getFragmentIndex(fid1);
		
		double dissim = compute(fid1, q);
		
		if (dissim <= dissThreshold) {
			int fragSize = crel.getFragmentSize(fid1);
			ResultFragment rf = new ResultFragment();
			rf.setFragmentId(fid1);
			rf.setDistance(dissim);
			rf.setFragmentSize(fragSize);
			resultsFragments.add(rf);
		}
		
		reportingInterval++;
		comparedFragments++;
		
		if (reportingInterval == 1000) {
			long duration = (System.currentTimeMillis() - startedTime) / 1000;
			reportingInterval = 0;
			double percentage = (double) comparedFragments * 100 / nfrag;
			percentage = (double) Math.round((percentage * 1000)) / 1000d;
			System.out.println(comparedFragments + " compared out of " + nfrag + " | " + percentage + " % completed. | Elapsed time: " + duration + " s | Distances to write: " + resultsFragments.size());
			LOGGER.info(comparedFragments + " compared out of " + nfrag + " | " + percentage + " % completed. | Elapsed time: " + duration + " s");
		}
	}

	/**
	 * @param h1
	 */
	private void computeGEDsWithHierarchy(SimpleGraph q, List<String> h1) {
		
		StringEditDistance.clearWordCache();
		
		for (int i = 0; i < h1.size(); i++) {
				String fid1 = h1.get(i);
				computeDissim(fid1, q);
		}
	}
	
	public void addDissimCalc(DissimilarityCalc calc) {
		chain.add(calc);
	}

	public double compute(String frag1, SimpleGraph g2) {
		SimpleGraph g1 = getSimpleGraph(frag1);
		double disim = 1.0;
		
		// a filter for very large fragment
		if (g1.getVertices().size() > 200 || g2.getVertices().size() > 200) {
			return disim;
		}
		
//		int i = 0;
		for (DissimilarityCalc calc: chain) {
			disim = calc.compute(g1, g2);
			if (calc instanceof GEDDissimCalc) {
				if (((GEDDissimCalc)calc).isDeterministicGED() == false && !calc.isAboveThreshold(disim))
					LOGGER.info("Incurs in at least one non-deterministic mapping (cf. Greedy algorithm) with " + frag1);
			}
			if (calc.isAboveThreshold(disim)) {
				disim = 1.0;
				break;
			}
//			i++;
		}			
//		System.out.println();
		return disim;
	}
	
	private SimpleGraph getSimpleGraph(String frag) {
		SimpleGraph graph = models.get(frag);
		
		if (graph == null) {
			try {
				CPF cpfGraph = composer.compose(frag);
				graph = new SimpleGraphWrapper(cpfGraph);
				
				// NOTE: this was commented out in the svn version
				if (graph.getEdges().size() < 100) {
					models.put(frag, graph);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		SimpleGraph graphCopy = new SimpleGraph(graph);
		return graphCopy;
	}
	
	private void formatConnectorLabel(EPC epc) {
		Map<Connector, String> labels = new HashMap<Connector, String>();
		
		for (Connector c: epc.getConnectors()) {
			String label = c.getName();
			TreeMultiset<String> mset = TreeMultiset.create();
			
			for (Node n: epc.getPre(c))
				if (n != null && n.getName() != null)
					mset.add(n.getName());
			label += mset.toString();
			mset.clear();
			
			for (Node n: epc.getPost(c))
				if (n != null && n.getName() != null)
					mset.add(n.getName());
			label += mset.toString();
			
			labels.put(c, label);
		}
		
		for (Connector c: labels.keySet()) {
			c.setName(labels.get(c));
		}
	}
}
