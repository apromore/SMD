package clustering.hac.test;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;

import clustering.containment.ContainmentRelation;
import clustering.dendogram.InternalNode;
import clustering.dendogram.Node;
import clustering.dissimilarity.DissimilarityMatrix;
import clustering.hac.CompleteLinkage;
import clustering.hac.HierarchicalAgglomerativeClustering;
import clustering.hac.SingleLinkage;

public class BookExample {
//	static class DummyCR implements ContainmentRelation {
//		public DummyCR() {}
//		public boolean areInContainmentRelation(int frag1, int frag2) { return false; }
//		public int getNumberOfFragments() { return 5; }
//		public Integer getFragmentId(int frag) { return frag + 1; }
//		public Integer getFragmentIndex(int frag) { return frag - 1; }
//		@Override
//		public List<Integer> getRoots() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		@Override
//		public List<Integer> getHierarchy(Integer integer) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//	}
//	
//	static class DummyDMatrix implements DissimilarityMatrix {
//		MultiKeyMap map;
//		public DummyDMatrix() {
//			map = new MultiKeyMap();
//			map.put(0, 1, 2.0);
//			map.put(0, 2, 6.0);
//			map.put(0, 3, 10.0);
//			map.put(0, 4, 9.0);
//			map.put(1, 2, 5.0);
//			map.put(1, 3, 9.0);
//			map.put(1, 4, 8.0);
//			map.put(2, 3, 4.0);
//			map.put(2, 4, 5.0);
//			map.put(3, 4, 3.0);
//		}
//		public Double getDissimilarity(Integer frag1, Integer frag2) {
//			Double result = (Double)map.get(frag1, frag2);
//			if (result == null)
//				result = (Double)map.get(frag2, frag1);
//			return result;
//		}
//	}
//	
//	public static void main(String args[]) throws Exception {
//		System.out.println("Single linkage");
//		Set<InternalNode> sources = new SingleLinkage(new DummyCR(), new DummyDMatrix()).cluster();
//		
//		for (Node src: sources) {
//			System.out.println("------------");
//			HierarchicalAgglomerativeClustering.dumpNode(System.out, "  ", src);
//		}
//		System.out.println("------------");
//
//		
//		System.out.println("Complete linkage");
//		sources = new CompleteLinkage(new DummyCR(), new DummyDMatrix()).cluster();
//		for (Node src: sources) {
//			System.out.println("------------");
//			HierarchicalAgglomerativeClustering.dumpNode(System.out, "  ", src);
//		}
//		System.out.println("------------");
//
//		System.out.println("done");
//	}
}
