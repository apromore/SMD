package clustering.hac;

import clustering.containment.ContainmentRelation;
import clustering.containment.ContainmentRelationImpl;
import clustering.dissimilarity.DissimilarityMatrix;
import clustering.hac.HierarchicalAgglomerativeClustering;

public class CompleteLinkage extends HierarchicalAgglomerativeClustering {
	private double proximity = -1;
	
	public CompleteLinkage(ContainmentRelationImpl crel, DissimilarityMatrix dmatrix) throws Exception {
		super(crel, dmatrix);
	}
	
	protected void resetProximityValue() {
		proximity = -1;
	}

	protected void updateProximityValue(double newValue) {
		proximity = Math.max(proximity, newValue);
	}

	protected boolean isItAValidProximityValue() {
		return proximity >= 0;
	}

	protected double getProximityValue() {
		return proximity;
	}

//	public static void main(String[] args) throws Exception {
//		ContainmentRelationImpl crel = new ContainmentRelationImpl();
//		crel.setMinSize(6);
//		crel.initialize();
//		
//		DissimMatrixGeneratorImpl generator = new DissimMatrixGeneratorImpl(new File("fragments"), crel);		
//		generator.setDissThreshold(0.15);
//		generator.addDissimCalc(new LJaccardDissimCalc(0.45));
//		generator.addDissimCalc(new GEDDissimCalc(0.15));				
//
//		CompleteLinkage clusterer = new CompleteLinkage(crel, generator);
//		
//		clusterer.cluster();
//		
//		System.out.println("done");
//	}
}
