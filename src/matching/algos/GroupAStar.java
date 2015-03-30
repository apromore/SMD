package matching.algos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import matching.Matches;
import matching.algos.DistanceAlgoAbstr.Mapping;
import nl.tue.tm.is.epc.Connector;
import nl.tue.tm.is.epc.EPC;
import nl.tue.tm.is.epc.Event;
import nl.tue.tm.is.graph.SimpleGraph;
import nl.tue.tm.is.graph.TwoVertices;
import nl.tue.tm.is.led.StringEditDistance;

public class GroupAStar extends GraphEditDistanceAStarSim {
	private GroupMapping groupMapping = null;
	private Mapping mapping = null;
	
	public GroupMapping getGroupMapping() {
		return groupMapping;
	}
	
	public class GroupMapping implements Comparable<GroupMapping> {
		protected double cost;
		Mapping mapping;
		List<Integer> candidates1;
		List<Integer> candidates2;
		Set<TwoVertices> matchedEdges;
		Set<TwoVertices> addedEdges;
		Set<TwoVertices> deletedEdges;
		Set<TwoVertices> groupMatchings;
		
		private GroupMapping() {}
		
		public Set<TwoVertices> getGroupMatchings() {
			return groupMatchings;
		}
		
		public GroupMapping(Mapping mapping, List<Integer> candidates1, List<Integer> candidates2) {
			this.mapping = mapping;
			this.cost = mapping.cost;
			this.candidates1 = candidates1;
			this.candidates2 = candidates2;
			this.matchedEdges = new HashSet<TwoVertices>();
			this.addedEdges = new HashSet<TwoVertices>();
			this.deletedEdges = new HashSet<TwoVertices>();
			this.groupMatchings = new HashSet<TwoVertices>();
		}

		public double getCost() {
			return cost;
		}
		public void updateCost(Object o) {
			
		}
		
		public int compareTo(GroupMapping o) {
			if (cost < o.cost)
				return -1;
			else if (cost > o.cost)
				return 1;
			else
				return 0;
		}
		
		public GroupMapping clone() {
			GroupMapping result = new GroupMapping();
			
			result.candidates1 = new LinkedList<Integer>(this.candidates1);
			result.candidates2 = new LinkedList<Integer>(this.candidates2);
			result.cost = this.mapping.cost;
			result.mapping = this.mapping.clone();
			result.matchedEdges = new HashSet<TwoVertices>(this.matchedEdges);
			result.addedEdges = new HashSet<TwoVertices>(this.addedEdges);
			result.deletedEdges = new HashSet<TwoVertices>(this.deletedEdges);
			result.groupMatchings = new HashSet<TwoVertices>(this.groupMatchings);
			
			return result;
		}
		
		public void matchInsertedNDeleted(Integer v1, Integer v2) {
			candidates1.remove((Object)v1);
			candidates2.remove((Object)v2);

			double led = 1.0 - StringEditDistance.similarity(sg1.getLabel(v1), sg2.getLabel(v2));
			if (led > 0.7)
				return;

//			System.out.printf("Trying to match: %s, %s\n", sg1.getLabel(v1), sg2.getLabel(v2));
			mapping.mappingsFrom1.put(v1, v2);
			mapping.mappingsFrom2.put(v2, v1);
			mapping.deletedVertices.remove(v1);
			mapping.addedVertices.remove(v2);
			for (Integer v : sg1.preSet(v1)) {
				if (mapping.mappingsFrom1.containsKey(v)) {
					if (v2 != EPSILON && sg2.preSet(v2).contains(mapping.mappingsFrom1.get(v))) {
						matchedEdges.add(new TwoVertices(v, v1));
					} else {
						deletedEdges.add(new TwoVertices(v, v1));
					}
				}
			}
			for (Integer v : sg1.postSet(v1)) {
				if (mapping.mappingsFrom1.containsKey(v)) {
					if (v2 != EPSILON && sg2.postSet(v2).contains(mapping.mappingsFrom1.get(v))) {
						matchedEdges.add(new TwoVertices(v1, v));
					} else {
						deletedEdges.add(new TwoVertices(v1, v));							
					}	
				}
			}

			for (Integer v : sg2.preSet(v2)) {
				if (mapping.mappingsFrom2.containsKey(v)) {
					if (v1 != EPSILON && sg1.preSet(v1).contains(mapping.mappingsFrom2.get(v))) {
						// Edge substitution set is handled over the SG1 edges
					} else {
						addedEdges.add(new TwoVertices(v, v2));
					}
				}
			}
			for (Integer v : sg2.postSet(v2)) {
				if (mapping.mappingsFrom2.containsKey(v)) {
					if (v1 != EPSILON && sg1.postSet(v1).contains(mapping.mappingsFrom2.get(v))) {
						// Edge substitution set is handled over the SG1 edges
					} else {
						addedEdges.add(new TwoVertices(v, v2));
					}
				}
			}
			if (matchedEdges.size() > 1)
				cost -= matchedEdges.size() * 0.3;
			else if (led <= 0.7)
				cost -= 0.5;
		}
		
		public void groupDeleted(Integer v1, Integer v2) {
			candidates1.remove((Object)v1);
			candidates2.remove((Object)v2);
						
			Integer v1p = mapping.mappingsFrom1.get(v1);
			if (v1p == EPSILON)
				return;
			
			boolean commonIncomingEdge = false;
			boolean commonOutgoingEdge = false;
			boolean selfLoop = false;
			
			Set<Integer> incomings = new HashSet<Integer>(sg1.preSet(v2));
			incomings.retainAll(sg1.preSet(v1));
			boolean flag = false;
			for (Integer sv1 : sg2.preSet(v1p)) {
				if (mapping.mappingsFrom2.get(sv1) != EPSILON && incomings.contains(mapping.mappingsFrom2.get(sv1)))
					flag = true;
			}
			commonIncomingEdge = !incomings.isEmpty() && flag;
			
			Set<Integer> outgoings = new HashSet<Integer>(sg1.postSet(v2));
			outgoings.retainAll(sg1.postSet(v1));
			flag = false;
			for (Integer sv1 : sg2.preSet(v1p)) {
				if (mapping.mappingsFrom2.get(sv1) != EPSILON && incomings.contains(mapping.mappingsFrom2.get(sv1)))
					flag = true;
			}
			commonOutgoingEdge = !outgoings.isEmpty() && flag;
			
			if (mapping.deletedEdges.contains(new TwoVertices(v1, v2)) ||
					mapping.deletedEdges.contains(new TwoVertices(v2, v1)))
				selfLoop = true;

			if (commonIncomingEdge && selfLoop ||
				commonOutgoingEdge && selfLoop ||
				commonIncomingEdge && commonOutgoingEdge) {
				groupMatchings.add(new TwoVertices(v1, v2));
				//cost = cost - (0.5 - StringEditDistance.similarity(sg1.getLabel(v1), sg2.getLabel(v2)));
//				System.out.printf("Group matching: %s, %s [%f]\n", sg1.getLabel(v1), sg1.getLabel(v2), cost);
			}
		}
		
		public void groupInserted(Integer v1, Integer v2) {						
			candidates1.remove((Object)v1);
			candidates2.remove((Object)v2);
			
			Integer v1p = mapping.mappingsFrom2.get(v1);
			if (v1p == EPSILON)
				return;
			
			boolean commonIncomingEdge = false;
			boolean commonOutgoingEdge = false;
			boolean selfLoop = false;
			
			Set<Integer> incomings = new HashSet<Integer>(sg2.preSet(v2));
			incomings.retainAll(sg2.preSet(v1));
			boolean flag = false;
			for (Integer sv1 : sg1.preSet(v1p)) {
				if (mapping.mappingsFrom1.get(sv1) != EPSILON && incomings.contains(mapping.mappingsFrom1.get(sv1)))
					flag = true;
			}
			commonIncomingEdge = !incomings.isEmpty() && flag;
			
			Set<Integer> outgoings = new HashSet<Integer>(sg2.postSet(v2));
			outgoings.retainAll(sg2.postSet(v1));
			flag = false;
			for (Integer sv1 : sg1.postSet(v1p)) {
				if (mapping.mappingsFrom1.get(sv1) != EPSILON && outgoings.contains(mapping.mappingsFrom1.get(sv1)))
					flag = true;
			}
			commonOutgoingEdge = !outgoings.isEmpty() && flag;
			
			if (mapping.addedEdges.contains(new TwoVertices(v1, v2)) ||
					mapping.addedEdges.contains(new TwoVertices(v2, v1)))
				selfLoop = true;

			if (commonIncomingEdge && selfLoop ||
				commonOutgoingEdge && selfLoop ||
				commonIncomingEdge && commonOutgoingEdge) {
				groupMatchings.add(new TwoVertices(v1p, v2));
				cost = cost - (0.5 - StringEditDistance.similarity(sg1.getLabel(v1), sg2.getLabel(v2)));
//				System.out.printf("Group matching: %s, %s [%f]\n", sg2.getLabel(v1), sg2.getLabel(v2), cost);
			}
		}
	}

	public void matchInsertedNDeleted() {
		PriorityQueue<GroupMapping> open = new PriorityQueue<GroupMapping>();
				
		open.add(new GroupMapping(mapping, new LinkedList<Integer>(mapping.deletedVertices), new LinkedList<Integer>(mapping.addedVertices)));
		
		while (!open.isEmpty()) {
			GroupMapping gm = open.remove();
			double current = gm.cost;
			boolean matched = false;
			
			if (gm.candidates1.isEmpty()) {
				groupMapping = gm;
				break;
			}
				
			int vertex1 = gm.candidates1.get(0);
			
			for (int vertex2 : gm.candidates2) {
				GroupMapping gmp = gm.clone();
				gmp.matchInsertedNDeleted(vertex1, vertex2);
				if (gmp.cost < current) {
					//System.out.println("GED improved!");
					current = gmp.cost;
					open.add(gmp);
					matched = true;
				}
			}
			
			if (!matched) {
				gm.candidates1.remove(0);
				open.add(gm);
			}
		}
		
		mapping = groupMapping.mapping;		
	}

	public void groupDeleted() {
		Queue<GroupMapping> open = new LinkedList<GroupMapping>();
		Set<GroupMapping> gmps = new HashSet<GroupMapping>();
				
		Mapping m = getMapping();
		
		Set<Integer> candidateVertices1 = new HashSet<Integer>();
		for (Integer v: m.mappingsFrom1.keySet()) {
			if (m.mappingsFrom1.get(v) != EPSILON)
				candidateVertices1.add(v);
		}

		open.add(new GroupMapping(m, new LinkedList<Integer>(candidateVertices1), new LinkedList<Integer>(m.deletedVertices)));
		while (!open.isEmpty()) {
			GroupMapping gm = open.remove();	
			if (gm.candidates1.size() == 0)
				continue;
			boolean matched = false;
			int vertex1 = gm.candidates1.get(0);
			for (int vertex2 : gm.candidates2) {
				GroupMapping gmp = gm.clone();
				int matchingCount = gmp.groupMatchings.size();
				gmp.groupDeleted(vertex1, vertex2);
				if (gmp.groupMatchings.size() > matchingCount) {	
					gmps.add(gmp);
					open.add(gmp);
					matched = true;
				}
			}
			
//			if (!matched) {
				gm.candidates1.remove(0);
				open.add(gm);
//			}
		}
		
		double cost = Double.MAX_VALUE;
		for (GroupMapping gm : gmps) {
			for (TwoVertices pair : gm.groupMatchings) {
				gm.cost += StringEditDistance.similarity(sg1.getLabel(pair.v1), sg1.getLabel(pair.v2));
			}
			if (gm.cost < cost) {
				groupMapping = gm;
				cost = gm.cost;
			}
		}
		
		mapping = groupMapping.mapping;		

	}
	public void groupInserted() {
		Queue<GroupMapping> open = new LinkedList<GroupMapping>();
		Set<GroupMapping> gmps = new HashSet<GroupMapping>();
				
		Mapping m = getMapping();
		
		Set<Integer> candidateVertices1 = new HashSet<Integer>();
		for (Integer v: m.mappingsFrom2.keySet()) {
			if (m.mappingsFrom2.get(v) != EPSILON)
				candidateVertices1.add(v);
		}

		open.add(new GroupMapping(m, new LinkedList<Integer>(candidateVertices1), new LinkedList<Integer>(m.addedVertices)));
		while (!open.isEmpty()) {
			GroupMapping gm = open.remove();	
			if (gm.candidates1.size() == 0)
				continue;
			boolean matched = false;
			int vertex1 = gm.candidates1.get(0);
			for (int vertex2 : gm.candidates2) {
				GroupMapping gmp = gm.clone();
				int matchingCount = gmp.groupMatchings.size();
				gmp.groupInserted(vertex1, vertex2);
				if (gmp.groupMatchings.size() > matchingCount) {	
					gmps.add(gmp);
					open.add(gmp);
					matched = true;
				}
			}
			
//			if (!matched) {
				gm.candidates1.remove(0);
				open.add(gm);
//			}
		}
		
		double cost = Double.MAX_VALUE;
		for (GroupMapping gm : gmps) {
			for (TwoVertices pair : gm.groupMatchings) {
				gm.cost += StringEditDistance.similarity(sg2.getLabel(pair.v1), sg2.getLabel(pair.v2));
			}
			if (gm.cost < cost) {
				groupMapping = gm;
				cost = gm.cost;
			}
		}
		
		mapping = groupMapping.mapping;		

	}
}
