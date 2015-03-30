package clustering.hac;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apromore.mining.MiningConfig;

import clustering.containment.ContainmentRelation;
import clustering.containment.ContainmentRelationImpl;
import clustering.dendogram.InternalNode;
import clustering.dendogram.LeafNode;
import clustering.dendogram.Node;
import clustering.dissimilarity.DissimilarityMatrix;

public abstract class HierarchicalAgglomerativeClustering {
	
	class Pair {
		int first, second;
		double dissimilarity;
		Pair(int first, int second, double dissimilarity) {
			this.first = first;
			this.second = second;
			this.dissimilarity = dissimilarity;
		}
		
		public String toString() {
			return String.format("(%d,%d): %f", first, second, dissimilarity);
		}
	}


	private ContainmentRelationImpl crel;
	private DissimilarityMatrix dmatrix;
	
	private SortedSet<InternalNode> sources;
	private double diameterThreshold = 0.4; // default value;
	
	public HierarchicalAgglomerativeClustering(ContainmentRelationImpl crel, DissimilarityMatrix dmatrix) {
		this.crel = crel;
		this.dmatrix = dmatrix;
		this.sources = null;
	}
	
	public void setDiameterThreshold(double diameterThreshold) {
		this.diameterThreshold = diameterThreshold;
	}

	public SortedSet<InternalNode> cluster() {
		Set<Integer> open = new HashSet<Integer>();
		Set<InternalNode> closed = new HashSet<InternalNode>();
		
		Map<Integer, Node> map = new HashMap<Integer, Node>();
		
		TreeSet<Pair> queue = new TreeSet<Pair>(new Comparator<Pair>() {
			public int compare(Pair p1, Pair p2) {
				int result = Double.compare(p1.dissimilarity, p2.dissimilarity);
				if (result == 0) {
					result = Double.compare(p1.first, p2.first);
					if (result == 0)
						result = Double.compare(p1.second, p2.second);
				}
				return result;
			}
		});
				
		
		for (int index = 0; index < crel.getNumberOfFragments(); index++) {
			
			// chathura code: prevent fragments that are not in ged matrix from inclusion in clusters.
//			int fragSize = crel.getFragmentSize(crel.getFragmentId(index));
//			if (fragSize < MiningConfig.MIN_GED_FRAGMENT_SIZE || fragSize > MiningConfig.MAX_GED_FRAGMENT_SIZE) {
//				continue;
//			}
			// end of chathura code
			
			open.add(index);
			map.put(index, new LeafNode(index, crel.getFragmentId(index)));
		}
		
		// Step 1. Add all initial distances
		for (int i = 0; i < crel.getNumberOfFragments() - 1; i++) {
			for (int j = i + 1; j < crel.getNumberOfFragments(); j++) {
				Double dissim = dmatrix.getDissimilarity(i, j);
				if (dissim != null) {
					Pair pair = new Pair(i, j, dissim);	
					queue.add(pair);
				}
			}			
		}
				
		while (!queue.isEmpty()) {
			Pair curr = queue.pollFirst();
			
			if (open.contains(curr.first) && open.contains(curr.second)) {
				open.remove(curr.first);
				open.remove(curr.second);
				
				int index = map.size();
				InternalNode node1 = new InternalNode(index, map.get(curr.first), map.get(curr.second), curr.dissimilarity);
				map.put(index, node1);
				
				closed.add(node1);
				
				// Update distance matrix
				for (Integer indexp: open) {
					Node node2 = map.get(indexp);
					
					boolean containment = false;
					resetProximityValue();
					
					for (String child1: node1.getChildren()) {
						for (String child2: node2.getChildren()) {
							if (crel.areInContainmentRelation(crel.getFragmentIndex(child1), crel.getFragmentIndex(child2))) {
								containment = true;
								break;
							} else {								
								Double tmp = dmatrix.getDissimilarity(crel.getFragmentIndex(child1), crel.getFragmentIndex(child2));
								if (tmp != null)
									updateProximityValue(tmp);
							}
						}
						if (containment) break;
					}

					if (!containment && isItAValidProximityValue()) {
						double dissimilarity = getProximityValue();
						if (dissimilarity <= diameterThreshold)
							queue.add(new Pair(node1.getIndex(), node2.getIndex(), dissimilarity));
					}
				}
				open.add(index);
			}
		}
		
		sources = new TreeSet<InternalNode>(new Comparator<InternalNode>(){
			public int compare(InternalNode p1, InternalNode p2) {
				
				int result = Double.compare(p1.getProximity(), p2.getProximity());
				if (result == 0)
					result = Double.compare(p1.getIndex(), p2.getIndex());
				return result;
			}
		});
		
		sources.addAll(closed);
		for (Node node: closed) {
			if (node.getFirst() instanceof InternalNode)
				sources.remove(node.getFirst());
			if (node.getSecond() instanceof InternalNode)			
				sources.remove(node.getSecond());
		}
		
		return sources;
	}
	
	public SortedSet<InternalNode> getSources() {
		return sources;
	}
	
    protected abstract void resetProximityValue();
    protected abstract void updateProximityValue(double newValue);
    protected abstract boolean isItAValidProximityValue();
    protected abstract double getProximityValue();


	public static void dumpNode(PrintStream out, final String indent, final Node node) {
        if (node==null) {
        	return;
        } else if (node instanceof InternalNode) {
            out.println(indent + node);
            dumpNode(out, indent+"  ", node.getFirst());
            dumpNode(out, indent+"  ", node.getSecond());
        } else {
            out.println(indent + node);
        }
    }	
}
