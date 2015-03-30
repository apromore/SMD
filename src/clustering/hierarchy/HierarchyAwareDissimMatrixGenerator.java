package clustering.hierarchy;

import java.sql.Connection;
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

import org.apache.commons.collections.map.MultiKeyMap;
import org.apromore.dao.ClusteringDao;
import org.apromore.exception.ExceptionDao;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.MiningConfig;
import org.apromore.service.IComposer;
import org.apromore.service.helper.SimpleGraphWrapper;
import org.apromore.service.impl.Composer;
import org.apromore.service.impl.RepositoryServiceImpl;
import org.apromore.service.impl.SimpleComposer;
import org.apromore.service.impl.SimpleGraphAwareComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import clustering.containment.ContainmentRelation;
import clustering.containment.ContainmentRelationImpl;
import clustering.dissimilarity.DissimilarityCalc;
import clustering.dissimilarity.DissimilarityMatrix;
import clustering.dissimilarity.measure.GEDDissimCalc;

import com.google.common.collect.TreeMultiset;

public class HierarchyAwareDissimMatrixGenerator implements DissimilarityMatrix {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HierarchyAwareDissimMatrixGenerator.class);
	
	private	MultiKeyMap dissimmap = null;
	
	@Autowired @Qualifier("ContainmentRelation")
	private ContainmentRelationImpl crel;
	
	private double dissThreshold;
//	private File dir;
	
	@Autowired @Qualifier("ClusteringDao")
	private ClusteringDao clusteringDao;
	
	@Autowired @Qualifier("SGAwareComposer")
    private SimpleGraphAwareComposer composer;
	
	/**
	 * This is used in the prototype version. Has to be replaced with jpa methods later.
	 */
	private Connection conn;
	
	/**
	 * Fragment Id -> SimpleGraph object containing all nodes and edges of the fragment
	 */
	private Map<String, SimpleGraph> models = new HashMap<String, SimpleGraph>();
	
	private List<DissimilarityCalc> chain = new LinkedList<DissimilarityCalc>();
//	private PrintStream out;
	
	long startedTime = 0;
	int nfrag = 0;
	int totalPairs = 0;
	int reportingInterval = 0;
	int processedPairs = 0;

//	public HierarchyAwareDissimMatrixGenerator(ContainmentRelation crel) {
//		this(dir, crel, null);
//	}
	
//	public HierarchyAwareDissimMatrixGenerator(ContainmentRelation crel) {
//		this.crel = crel;
//	}
	
	public void setContainmentRelation(ContainmentRelationImpl crel) {
		this.crel = crel;
	}
	
	public void setDissThreshold(double dissThreshold) {
		this.dissThreshold = dissThreshold;
	}
		
	public void computeDissimilarity() {
		
		startedTime = System.currentTimeMillis();
		List<String> processedFragmentIds = new ArrayList<String>();
		
		dissimmap = new MultiKeyMap();

		nfrag = crel.getNumberOfFragments();
		totalPairs = nfrag * (nfrag + 1) / 2;
		reportingInterval = 0;
		processedPairs = 0;
		
		List<String> roots = crel.getRoots();
		for (int p = 0; p < roots.size(); p++) {
			List<String> h1 = crel.getHierarchy(roots.get(p));
			h1.removeAll(processedFragmentIds);
			
			// fill composer cache with details of h1's fragments (i.e. fragments rooted at p)
			try {
				getSimpleGraph(roots.get(p));
			} catch (Exception e) {
				LOGGER.error("Failed to get a root fragment. Rest of the GED computation will progress normally.");
			}
//			System.out.println("Intra: " + roots.get(p));
			computeIntraHierarchyGEDs(h1);
			
			if (p < roots.size() - 1) {
				for (int q = p + 1; q < roots.size(); q++) {
					List<String> h2 = crel.getHierarchy(roots.get(q));
					
					// preload all fragments of h2 (rooted at q) into memory
					try {
						getSimpleGraph(roots.get(q));
					} catch (Exception e) {
						LOGGER.error("Failed to get a root fragment. Rest of the GED computation will progress normally.");
					}
//					System.out.println("Inter: " + roots.get(p) + " and " + roots.get(q));
					computeInterHierarchyGEDs(h1, h2);
					
					if (models.size() > 1000000) {
						// we don't have enough capacity to cache fragments in h2. let's remove them
//						models.keySet().removeAll(h2);
//						composer.clearCache(h2);
					}
				}
			}
			
			// at this point we have processed all fragments of h1, with fragments in the entire repository.
			// so we can remove all h1's fragments from the cache
			models.keySet().removeAll(h1);
			composer.clearCache(h1);
			
			processedFragmentIds.addAll(h1);
		}
		
		// ged values are written to the database periodically after reporting period. if there are left over geds we have to write them here.
		if (!dissimmap.isEmpty()) {
			clusteringDao.insertDistancesJDBC(dissimmap);
			dissimmap.clear();
		}
	}
	
	/**
	 * @param h1
	 * @param h2
	 */
	private void computeInterHierarchyGEDs(List<String> h1, List<String> h2) {

		StringEditDistance.clearWordCache();
		
		for (int s = 0; s < h1.size(); s++) {
			String fid1 = h1.get(s);
			
			for (int t = 0; t < h2.size(); t++) {
				String fid2 = h2.get(t);
				computeDissim(fid1, fid2);
			}
		}
	}

	private void computeDissim(String fid1, String fid2) {
		
		try {
			int fid1Index = crel.getFragmentIndex(fid1);
			int fid2Index = crel.getFragmentIndex(fid2);
			
			if (!crel.areInContainmentRelation(fid1Index, fid2Index)) {
				double dissim = compute(fid1, fid2);
				
				if (dissim <= dissThreshold) {
					dissimmap.put(fid1, fid2, dissim);
				}
			}
			
			reportingInterval++;
			processedPairs++;
			
			if (reportingInterval == 1000) {
				long duration = (System.currentTimeMillis() - startedTime) / 1000;
				reportingInterval = 0;
				double percentage = (double) processedPairs * 100 / totalPairs;
				percentage = (double) Math.round((percentage * 1000)) / 1000d;
//				System.out.println(processedPairs + " processed out of " + totalPairs + " | " + percentage + " % completed. | Elapsed time: " + duration + " s | Distances to write: " + dissimmap.size());
				LOGGER.info(processedPairs + " processed out of " + totalPairs + " | " + percentage + " % completed. | Elapsed time: " + duration + " s | Distances to write: " + dissimmap.size());
				clusteringDao.insertDistancesJDBC(dissimmap);
				dissimmap.clear();
			}
		
		} catch (Exception e) {
			LOGGER.error("Failed to compute GED between {} and {} due to {}. " +
					"GED computation between other fragments will proceed normally.", 
					new Object[] {fid1, fid2, e.getMessage()});
		}
	}

	/**
	 * @param h1
	 */
	private void computeIntraHierarchyGEDs(List<String> h1) {
		
		StringEditDistance.clearWordCache();
		
		for (int i = 0; i < h1.size() - 1; i++) {
			for (int j = i+1; j < h1.size(); j++) {
				String fid1 = h1.get(i);
				String fid2 = h1.get(j);
				
				computeDissim(fid1, fid2);
			}
		}
		
	}

	public Double getDissimilarity(Integer frag1, Integer frag2) {
		Double result = (Double)dissimmap.get(frag1, frag2);
		if (result == null)
			result = (Double)dissimmap.get(frag2, frag1);
		return result;
	}
	
	public void addDissimCalc(DissimilarityCalc calc) {
		chain.add(calc);
	}

	public double compute(String frag1, String frag2) {
		
		double disim = 1.0;
		
		// a filter for very large fragment
		if (crel.getFragmentSize(frag1) > MiningConfig.MAX_GED_FRAGMENT_SIZE || crel.getFragmentSize(frag2) > MiningConfig.MAX_GED_FRAGMENT_SIZE) {
			return disim;
		}
		
		// filter for small fragments
		if (crel.getFragmentSize(frag1) < MiningConfig.MIN_GED_FRAGMENT_SIZE || crel.getFragmentSize(frag2) < MiningConfig.MIN_GED_FRAGMENT_SIZE) {
			return disim;
		}
		
		SimpleGraph g1 = getSimpleGraph(frag1);
		SimpleGraph g2 = getSimpleGraph(frag2);
		
//		int i = 0;
		for (DissimilarityCalc calc: chain) {
			disim = calc.compute(g1, g2);
			if (calc instanceof GEDDissimCalc) {
				if (((GEDDissimCalc)calc).isDeterministicGED() == false && !calc.isAboveThreshold(disim))
					LOGGER.trace("Incurs in at least one non-deterministic mapping (cf. Greedy algorithm) between " + frag1 + " and " + frag2);
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
	
//	public double compute1(int frag1, int frag2) {
//		SimpleGraph g1 = getSimpleGraph(frag1);
//		SimpleGraph g2 = getSimpleGraph(frag2);
//		return chain.get(0).compute(g1, g2);
//	}
//
//	public double compute2(int frag1, int frag2) {
//		SimpleGraph g1 = getSimpleGraph(frag1);
//		SimpleGraph g2 = getSimpleGraph(frag2);
//		return chain.get(1).compute(g1, g2);
//	}
	
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
				LOGGER.error("Failed to get graph of fragment {}", frag);
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
//			System.out.println(c.getName());
		}
	}	

}
