package clustering.hac.test;

import java.io.FileReader;
import java.io.PrintStream;
import java.util.Set;
import java.util.SortedSet;

import clustering.containment.AncestorRelationImpl;
import clustering.containment.ContainmentRelation;
import clustering.containment.ContainmentRelationImpl;
import clustering.dendogram.InternalNode;
import clustering.dendogram.Node;
import clustering.dissimilarity.DissimilarityMatrix;
import clustering.dissimilarity.DissimilarityMatrixReader;
import clustering.hac.CompleteLinkage;
import clustering.hac.HierarchicalAgglomerativeClustering;

public class HACSAPAllModels {
	
//	public static void dump(PrintStream out, ContainmentRelation crel, Set<InternalNode> sources) {
//		for (Node src: sources) {
//			boolean first = true;
//			for (Integer child: src.getChildren()) {
//				if (first)
//					first = false;
//				else
//					out.print(",");
//				out.print(crel.getFragmentId(child));
//			}
//			out.println();
//		}
//	}
//
//	public static void dumpWithSizes(PrintStream out, ContainmentRelation crel, AncestorRelationImpl arel, Set<InternalNode> sources) {
//		for (Node src: sources) {
//			boolean first = true;
//			for (Integer child: src.getChildren()) {
//				if (first)
//					first = false;
//				else
//					out.print(",");
//				int fragId = crel.getFragmentId(child);
//				out.print(fragId);
//				out.printf("(%d)", arel.getFragSize(fragId));
//			}
//			out.println();
//		}
//	}
//
//	public static void main(String args[]) throws Exception {
//		// "ContainmentRelationImpl"   queries the RPSDAG to retrieve the
//		// set of models with a minimum size (in the example of 4)
//		ContainmentRelationImpl crel = new ContainmentRelationImpl("sap", "refactor", "refactor");
//		// ----> IMPORTANT: As the identifier of models retrieved by this
//		// class are usually discontinues, the class also assigns an index
//		// and provides a method to translate such index to the identifier
//		// as stored in the RPSDAG
//		crel.setMinSize(4);
//		// Additionally, it computes the containment relation
//		// (transitive and symmetric version of the parent-child relation
//		// on the RPSDAG
//		crel.initialize();
//		
//		DissimilarityMatrix generator = new DissimilarityMatrixReader(new FileReader("dmatrix_sap.txt"), crel, 0.40);
//		
//		HierarchicalAgglomerativeClustering clusterer = new CompleteLinkage(crel, generator);
//		clusterer.setDiameterThreshold(0.40);
//		SortedSet<InternalNode> sources2 = clusterer.cluster();
//
//		System.out.println("Number of clusters: " + sources2.size());
//		
//		AncestorRelationImpl arel = new AncestorRelationImpl("sap", "refactor", "refactor");
//		arel.setMinSize(4);
//		arel.initialize();
//		dumpWithSizes(new PrintStream("clusters_sap.txt"), crel, arel, sources2);
//
//		System.out.println("done");
//	}
}
