package clustering.hac.test;

import java.io.File;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import clustering.containment.ContainmentRelationImpl;
import clustering.dendogram.InternalNode;
import clustering.dendogram.Node;
import clustering.dissimilarity.DissimMatrixGenerator;
import clustering.dissimilarity.measure.GEDDissimCalc;
import clustering.dissimilarity.measure.SizeBasedDissimCalc;
import clustering.hac.CompleteLinkage;
import clustering.hac.GroupAverage;
import clustering.hac.HierarchicalAgglomerativeClustering;
import clustering.hac.SingleLinkage;

public class Test {
	
//	public static void dump(Set<InternalNode> sources) {
//		for (Node src: sources) {
//			System.out.println("------------");
//			HierarchicalAgglomerativeClustering.dumpNode(System.out, "  ", src);
//		}
//		System.out.println("------------");
//	}
//	
//	private static Set<Integer> collectChildren(Set<InternalNode> sources1) {
//		Set<Integer> children = new HashSet<Integer>();
//		for (Node n: sources1)
//			children.addAll(n.getChildren());
//		return children;
//	}
//
//	public static void main(String args[]) throws Exception {
//		// "ContainmentRelationImpl"   queries the RPSDAG to retrieve the
//		// set of models with a minimum size (in the example of 4)
//		ContainmentRelationImpl crel = new ContainmentRelationImpl();
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
//		// "DissimMatrixGeneratorImpl"   computes the dissimilarity values
//		// for pair of models. Note that:
//		//    1) If two models are in containment relation, the dissimilarity 
//		//       is not computed and nothing is stored in the matrix.
//		//    2) If the dissimilarity value is above a given threshold (0.15 in
//		//       this example), the value is not stored in the matrix.
//		//    3) There is a chain of classes that compute dissimilarity. The
//		//       first one (LJaccardDissimCalc) is cheaper to compute so the
//		//       other one is only computed when the first value is bellow
//		//       a local threshold (0.45 in this example).
//		// Note that some cells in the matrix are empty. The result might be
//		// a particularly sparse matrix. That is way, the matrix is stored
//		// as list of tuples in the form of a MultiKeyMap.
//		DissimMatrixGenerator generator = new DissimMatrixGenerator(new File("fragments"), crel, new PrintStream("dmat.txt"));		
//		generator.setDissThreshold(0.45);
//		generator.addDissimCalc(new SizeBasedDissimCalc(0.45));
//		generator.addDissimCalc(new GEDDissimCalc(0.45, 0.4));
//		
//		long start = System.currentTimeMillis();
//		generator.computeDissimilarity();
//		System.out.println(System.currentTimeMillis() - start);
//		System.exit(0);
//		SortedSet<InternalNode> sources1 = new SingleLinkage(crel, generator).cluster();
//		Set<Integer> children1 = collectChildren(sources1);
//		
//		SortedSet<InternalNode> sources2 = new CompleteLinkage(crel, generator).cluster();
//		Set<Integer> children2 = collectChildren(sources2);
//
//		SortedSet<InternalNode> sources3 = new GroupAverage(crel, generator).cluster();
//		Set<Integer> children3 = collectChildren(sources3);
//
//		System.out.println(children1.containsAll(children2));
//		System.out.println(children1.containsAll(children3));
//		
//		System.out.println(children2.containsAll(children1));
//		System.out.println(children2.containsAll(children3));
//		
//		System.out.println(children3.containsAll(children1));
//		System.out.println(children3.containsAll(children2));
//		
//		System.out.println("Single linkage");
//		dump(sources1);
//		System.out.println("Complete linkage");
//		dump(sources2);
//		System.out.println("Group average");
//		dump(sources3);
//
//		System.out.println("done");
//
//	}
}
