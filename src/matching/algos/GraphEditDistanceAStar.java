package matching.algos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import matching.Matches;
import nl.tue.tm.is.epc.Connector;
import nl.tue.tm.is.epc.EPC;
import nl.tue.tm.is.epc.Event;
import nl.tue.tm.is.graph.SimpleGraph;
import nl.tue.tm.is.graph.TwoVertices;
import nl.tue.tm.is.labels.TokenizedLabel;
import nl.tue.tm.is.led.StringEditDistance;
import nl.tue.tm.is.ptnet.PTNet;

public class GraphEditDistanceAStar extends DistanceAlgoAbstr {

	public final static int EPSILON = -1;
	public final static double VERTEX_INSERTION_COST = 0.1;
	public final static double VERTEX_DELETION_COST = 0.9;
	public final static double EDGE_SUBSTITUTION_COST = 0.0;
	public final static double EDGE_INSERTION_COST = 0.0;
	public final static double EDGE_DELETION_COST = 0.0;


	class Mapping implements Comparable<Mapping> {
		private double cost;
		private Map<Integer, Integer> mappingsFrom1;
		private Map<Integer, Integer> mappingsFrom2;
		private Set<Integer> addedVertices;
		private Set<Integer> deletedVertices;
		private Set<TwoVertices> matchedEdges;
		private Set<TwoVertices> addedEdges;
		private Set<TwoVertices> deletedEdges;
		private List<Integer> remaining1;
		private List<Integer> remaining2;

		public Mapping() {
			cost = 0.0;
			mappingsFrom1 = new HashMap<Integer, Integer>();
			mappingsFrom2 = new HashMap<Integer, Integer>();
			addedVertices = new HashSet<Integer>();
			deletedVertices = new HashSet<Integer>();
			matchedEdges = new HashSet<TwoVertices>();
			addedEdges = new HashSet<TwoVertices>();
			deletedEdges = new HashSet<TwoVertices>();
			remaining1 = new LinkedList<Integer>(sg1.getVertices());
			remaining2 = new LinkedList<Integer>(sg2.getVertices());
		}

		public Mapping clone() {
			Mapping m = new Mapping();
			m.remaining1.clear(); m.remaining2.clear();
			m.cost = cost;
			m.mappingsFrom1.putAll(mappingsFrom1);
			m.mappingsFrom2.putAll(mappingsFrom2);
			m.addedVertices.addAll(addedVertices);
			m.deletedVertices.addAll(deletedVertices);
			m.matchedEdges.addAll(matchedEdges);
			m.addedEdges.addAll(addedEdges);
			m.deletedEdges.addAll(deletedEdges);
			m.remaining1.addAll(remaining1);
			m.remaining2.addAll(remaining2);
			return m;
		}

		public int compareTo(Mapping o) {
			if (cost < o.cost)
				return -1;
			else if (cost > o.cost)
				return 1;
			else
				return 0;
		}

		public double getCost() {
			return cost;
		}

		public void step(Integer v1, Integer v2) {
			step(v1, v2, 0.0);
		}

		public void step(Integer v1, Integer v2, double subsCost) {
			if (v1 == EPSILON) {
				remaining2.remove(v2);
				addedVertices.add(v2);
				cost += VERTEX_INSERTION_COST; // Cost for vertex insertion
				mappingsFrom2.put(v2, EPSILON);				
			} else if (v2 == EPSILON) {
				remaining1.remove(v1);
				deletedVertices.add(v1);
				cost += VERTEX_DELETION_COST;
				mappingsFrom1.put(v1, EPSILON);				
			} else {
				remaining1.remove(v1);
				remaining2.remove(v2);
				cost += subsCost;
				mappingsFrom1.put(v1, v2);
				mappingsFrom2.put(v2, v1);
			}

			if (v1 != EPSILON) {
				for (Integer v : sg1.preSet(v1)) {
					if (mappingsFrom1.containsKey(v)) {
						if (v2 != EPSILON && sg2.preSet(v2).contains(mappingsFrom1.get(v))) {
							cost += EDGE_SUBSTITUTION_COST; // Edge matched
							matchedEdges.add(new TwoVertices(v, v1));
						} else {
							cost += EDGE_DELETION_COST; // Edge deleted
							deletedEdges.add(new TwoVertices(v, v1));
						}
					}
				}
				for (Integer v : sg1.postSet(v1)) {
					if (mappingsFrom1.containsKey(v)) {
						if (v2 != EPSILON && sg2.postSet(v2).contains(mappingsFrom1.get(v))) {
							cost += EDGE_SUBSTITUTION_COST; // Edge matched
							matchedEdges.add(new TwoVertices(v1, v));
						} else {
							cost += EDGE_DELETION_COST; // Edge deleted
							deletedEdges.add(new TwoVertices(v1, v));							
						}	
					}
				}
			}

			if (v2 != EPSILON) {
				for (Integer v : sg2.preSet(v2)) {
					if (mappingsFrom2.containsKey(v)) {
						if (v1 != EPSILON && sg1.preSet(v1).contains(mappingsFrom2.get(v))) {
							cost += EDGE_SUBSTITUTION_COST; // Edge matched
							// Edge substitution set is handled over the SG1 edges
						} else {
							cost += EDGE_INSERTION_COST; // Edge inserted
							addedEdges.add(new TwoVertices(v, v2));
						}
					}
				}
				for (Integer v : sg2.postSet(v2)) {
					if (mappingsFrom2.containsKey(v)) {
						if (v1 != EPSILON && sg1.postSet(v1).contains(mappingsFrom2.get(v))) {
							cost += EDGE_SUBSTITUTION_COST; // Edge matched
							// Edge substitution set is handled over the SG1 edges
						} else {
							cost += EDGE_INSERTION_COST; // Edge inserted
							addedEdges.add(new TwoVertices(v, v2));
						}
					}
				}
			}
		}
	}

	private Set<Integer> partition1;
	private Set<Integer> partition2;

	private double labelSubstitutionCost(Integer v1, Integer v2) {
		if (partition1 != null)
			if ((partition1.contains(v1) && !partition2.contains(v2)) ||
					(!partition1.contains(v1) && partition2.contains(v2)))
				return Double.POSITIVE_INFINITY;
		
		String label1 =  sg1.getLabel(v1);
		String label2 = sg2.getLabel(v2);
		double led = 1.0 - StringEditDistance.similarity(label1, label2);
		if (StringEditDistance.hasCache() && 
				label1 != null && label1.length() != 0 && 
				label2 != null && label2.length() != 0) { 
			TokenizedLabel t1 = StringEditDistance.getLabelFromCache(label1);
			TokenizedLabel t2 = StringEditDistance.getLabelFromCache(label2);
			led = 1.0 - t1.similarity(t2);
		}
		return  led > 0.48 ? Double.POSITIVE_INFINITY : led;
	}

	public double compute(SimpleGraph sg1, SimpleGraph sg2) {
		double accept_threshold = Double.POSITIVE_INFINITY;
		PriorityQueue<Mapping> open = new PriorityQueue<Mapping>();
		PriorityQueue<Mapping> fullMappings = new PriorityQueue<Mapping>();
		boolean matched = false;
		Mapping m;
		
		init(sg1, sg2);
		Integer v1 = sg1.getVertices().iterator().next();
		for (Integer v2 : sg2.getVertices()) {
			double labelSubs = labelSubstitutionCost(v1, v2);
			if (labelSubs != Double.POSITIVE_INFINITY) {
				m = new Mapping();
				m.step(v1, v2, labelSubs);
				open.add(m);
				matched = true;
			}
		}

		if (!matched) {
			m = new Mapping();
			m.step(v1, EPSILON);
			open.add(m);
		}
		
		while (!open.isEmpty()) {
			Mapping p = open.remove();

			if (p.getCost() > accept_threshold)
				break;
			if (p.remaining1.size() == 0 && p.remaining2.size() == 0) {
				fullMappings.add(p);
				accept_threshold = p.getCost();
				continue;
			}
			if (p.remaining1.size() > 0 && p.remaining2.size() > 0) {
				matched = false;
				Integer vk = p.remaining1.get(0);
				for (Integer w : p.remaining2) {
					double labelSubs = labelSubstitutionCost(vk, w);
					if (labelSubs != Double.POSITIVE_INFINITY) {
						m = p.clone();
						m.step(vk, w, labelSubs);
						open.add(m);
						matched = true;
					}
				}
				
				if (!matched) {
					p.step(vk, EPSILON);
					open.add(p);	
				}
			} else if (p.remaining1.size() > 0) {
				Integer vk = p.remaining1.get(0);
				p.step(vk, EPSILON);
				open.add(p);
			} else {
				Integer vk = p.remaining2.get(0);
				p.step(EPSILON, vk);
				open.add(p);
			}
		}

		Mapping mapping = fullMappings.remove();

//		System.out.println("--- MATCHED");
//		for (Integer v : mapping.mappingsFrom1.keySet()) {
//			Integer v2 = mapping.mappingsFrom1.get(v);
//			if (v2 == EPSILON)
//				continue;
//			System.out.printf("%s\t%s\n", sg1.getLabel(v).trim(), sg2.getLabel(v2).trim());
//		}
//		System.out.println("--- INSERTED");
//		for (Integer v : mapping.addedVertices) {
//			System.out.println(sg2.getLabel(v).trim());
//		}
//		System.out.println("--- DELETED");
//		for (Integer v : mapping.deletedVertices) {
//			System.out.println(sg1.getLabel(v).trim());
//		}
		
		int skippedVertices = mapping.addedVertices.size() + mapping.deletedVertices.size();
		int substitutedVertices = sg1.getVertices().size() - mapping.deletedVertices.size();
		int skippedEdges = mapping.addedEdges.size() + mapping.deletedEdges.size();
		int substitutedEdges = mapping.matchedEdges.size();
		int groupedVertices = 0;
		
		//return weightSkippedVertex*skippedVertices + weightSkippedEdge*skippedEdges + weightSubstitutedVertex*2.0*substitutedVertices + weightGroupedVertex*groupedVertices; 
		return mapping.cost;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sm[] = { "search2.epml", "search1.epml", "search3.epml",
				"search10.epml", "search4.epml", "search5.epml",
				"search6.epml", "search7.epml", "search8.epml", "search9.epml" };
		String dm[] = { "1Be_2gm6.epml", "1Ve_58l9.epml",
				"1Be_25fz.epml", "1Be_1y63.epml",
				"1Be_204a.epml", "1Be_22v7.epml", "1Be_25fz.epml",
				"1Be_25my.epml", "1Be_2aze.epml", "1Be_2ft2.epml",
				"1Be_2gm6.epml", "1Be_2ork.epml", "1Be_2rxu.epml",
				"1Be_2tnc.epml", "1Be_2vbl.epml", "1Be_30t8.epml",
				"1Be_322n.epml", "1Be_32fe.epml", "1Be_34is.epml",
				"1Be_38qs.epml", "1Be_3a62.epml", "1Be_3e7i.epml",
				"1Be_3era.epml", "1Be_3j4l.epml", "1Be_8ri3.epml",
				"1Be_8uyu.epml", "1Ku_8w3g.epml", "1Ku_903f.epml",
				"1Ku_91bx.epml", "1Ku_93jr.epml", "1Ku_96oz.epml",
				"1Ku_97uj.epml", "1Ku_9bjf.epml", "1Ku_9do6.epml",
				"1Ku_9e6t.epml", "1Ku_9mgu.epml", "1Ku_9nk6.epml",
				"1Ku_9ojw.epml", "1Ku_9rnu.epml", "1Ku_9vyx.epml",
				"1Ku_9yyx.epml", "1Ku_9zhk.epml", "1Ku_a0t4.epml",
				"1Ku_a6af.epml", "1Ku_aa4c.epml", "1Ku_acul.epml",
				"1Ku_add8.epml", "1Ku_afas.epml", "1Ku_agg3.epml",
				"1Ve_4fin.epml", "1Ve_4gw1.epml", "1Ve_4hbk.epml",
				"1Ve_4k75.epml", "1Ve_4mai.epml", "1Ve_4mua.epml",
				"1Ve_4mxc.epml", "1Ve_4ose.epml", "1Ve_4q66.epml",
				"1Ve_4ymf.epml", "1Ve_512s.epml", "1Ve_52tx.epml",
				"1Ve_57p5.epml", "1Ve_58l9.epml", "1Ve_5a31.epml",
				"1Ve_5dvr.epml", "1Ve_5jtb.epml", "1Ve_5kzj.epml",
				"1Ve_5otm.epml", "1Ve_5tcy.epml", "1Ve_5x4o.epml",
				"1Ve_5ycw.epml", "1Ve_6294.epml", "1Ve_62rl.epml",
				"1Ve_6a59.epml", "1Ve_6bms.epml", "1Ve_6dlt.epml",
				"1Ve_6lp9.epml", "1Ve_6mnb.epml", "1Ve_6u91.epml",
				"1Ve_6wdf.epml", "1Ve_70sp.epml", "1Ve_710u.epml",
				"1Ve_77z0.epml", "1Ve_7btr.epml", "1Ve_7c1w.epml",
				"1Ve_7coq.epml", "1Ve_7kcl.epml", "1Ve_7n23.epml",
				"1Ve_7rr4.epml", "1Ve_7s3r.epml", "1Ve_7sma.epml",
				"1Ve_7suf.epml", "1Ve_7uuo.epml", "1Ve_7vev.epml",
				"1Ve_82t3.epml", "1Ve_84am.epml", "1Ve_85il.epml",
				"1Ve_8a7d.epml", "1Ve_8b2j.epml", "1Ve_8bao.epml",
				"1Ve_m47b.epml", "1Ve_m4y2.epml", "1Ve_mzcb.epml" };

		String prefix = "./models/modelpairs/";

		boolean considerevents = false;
		
		Locale l = new Locale("EN");
		
		Locale.setDefault(l);
		
		for (String s : sm) {
			EPC searchepc = EPC.loadEPML(prefix + s);
			for (Connector c: searchepc.getConnectors()){
				c.setName("");
			}
			for (Event e: searchepc.getEvents()){
				if (!considerevents) {
					e.setName("");
				}
			}
			SimpleGraph searchgraph = new SimpleGraph(searchepc);
			Set<Integer> silentVertices = new HashSet<Integer>();
			for (Integer v: searchgraph.getVertices()){
				if (searchgraph.getLabel(v).length() == 0){
					silentVertices.add(v);
				}
			}
			searchgraph = searchgraph.removeVertices(silentVertices);
			
			for (String d : dm) {
				EPC docepc = EPC.loadEPML(Matches.prefix + d);
				for (Connector c: docepc.getConnectors()){
					c.setName("");
				}
				for (Event e: docepc.getEvents()){
					if (!considerevents){
						e.setName("");
					}
				}
				SimpleGraph docgraph = new SimpleGraph(docepc);
				silentVertices = new HashSet<Integer>();
				for (Integer v: docgraph.getVertices()){
					if (docgraph.getLabel(v).length() == 0){
						silentVertices.add(v);
					}
				}
				docgraph = docgraph.removeVertices(silentVertices);
				GraphEditDistanceAStar gedepc = new GraphEditDistanceAStar();
				double vweight = 0.1;
				double sweight = 0.9;
				double eweight = 0.0;					

				Object weights[] = {"vweight",vweight,"sweight",sweight,"eweight",eweight};
				gedepc.setWeight(weights);
				double ged = gedepc.compute(searchgraph, docgraph);
				System.out.printf("%s\t%s\t%f\n", s, d, ged);
				break;
			}
			break;
		}
	}

	public void setPartitions(Set<Integer> functions1, Set<Integer> functions2) {
		this.partition1 = functions1;
		this.partition2 = functions2;
	}
}
