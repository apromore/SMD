package matching.algos;

import java.util.HashSet;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Set;

import matching.Matches;
import nl.tue.tm.is.epc.Connector;
import nl.tue.tm.is.epc.EPC;
import nl.tue.tm.is.epc.Event;
import nl.tue.tm.is.graph.SimpleGraph;
import nl.tue.tm.is.labels.TokenizedLabel;
import nl.tue.tm.is.led.StringEditDistance;

public class GraphEditDistanceAStarSim extends DistanceAlgoAbstr {

	private Set<Integer> partition1;
	private Set<Integer> partition2;
	private Mapping mapping = null;

	private double labelSubstitutionCost(Integer v1, Integer v2) {
		if (partition1 != null)
			if ((partition1.contains(v1) && !partition2.contains(v2)) ||
					(!partition1.contains(v1) && partition2.contains(v2)))
				return Double.POSITIVE_INFINITY;
		
		String label1 =  sg1.getLabel(v1);
		String label2 = sg2.getLabel(v2);
		double sim = StringEditDistance.similarity(label1, label2);
//		System.out.println(label1+ " <> "+ label2+ " similarity "+ sim);
		double led = 1.0 - sim;

		return  led > this.ledcutoff ? Double.POSITIVE_INFINITY : led;
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
				m.step(v1, v2, labelSubs); m.updateCost(this);
				open.add(m);
				matched = true;
			}
		}

		if (!matched) {
			m = new Mapping();
			m.step(v1, EPSILON); m.updateCost(this);
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
						m.step(vk, w, labelSubs); m.updateCost(this);
						open.add(m);
						matched = true;
					}
				}
				
				if (!matched) {
					p.step(vk, EPSILON); p.updateCost(this);
					open.add(p);	
				}
			} else if (p.remaining1.size() > 0) {
				Integer vk = p.remaining1.get(0);
				p.step(vk, EPSILON); p.updateCost(this);
				open.add(p);
			} else {
				Integer vk = p.remaining2.get(0);
				p.step(EPSILON, vk); p.updateCost(this);
				open.add(p);
			}
		}

		Mapping mapping = fullMappings.remove();
		
		this.mapping = mapping;

		return mapping.cost;
	}
	
	public Mapping getMapping(){
		return mapping;
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
				GraphEditDistanceAStarSim gedepc = new GraphEditDistanceAStarSim();
				double vweight = 1.0;
				double sweight = 0.5;
				double eweight = 0.0;					

				Object weights[] = {"vweight",vweight,"sweight",sweight,"eweight",eweight,"ledcutoff",0.48,"usepuredistance",1.0,"prunewhen",100.0,"pruneto",10.0,"useepsilon",1.0};
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
