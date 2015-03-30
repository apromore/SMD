package clustering.hac;

import java.io.File;

import clustering.containment.ContainmentRelation;
import clustering.containment.ContainmentRelationImpl;
import clustering.dissimilarity.DissimMatrixGenerator;
import clustering.dissimilarity.DissimilarityMatrix;
import clustering.dissimilarity.measure.GEDDissimCalc;
import clustering.dissimilarity.measure.LJaccardDissimCalc;

public class SingleLinkage extends HierarchicalAgglomerativeClustering {
	private double initialValue = Double.POSITIVE_INFINITY;
	private double proximity = initialValue;
	
	public SingleLinkage(ContainmentRelationImpl crel, DissimilarityMatrix dmatrix) throws Exception {
		super(crel, dmatrix);
	}
	
	protected void resetProximityValue() {
		proximity = initialValue;
	}

	protected void updateProximityValue(double newValue) {
		proximity = Math.min(proximity, newValue);
	}

	protected boolean isItAValidProximityValue() {
		return proximity < initialValue;
	}

	protected double getProximityValue() {
		return proximity;
	}

	public static void main(String[] args) throws Exception {
		ContainmentRelationImpl crel = new ContainmentRelationImpl();
		crel.setMinSize(6);
		crel.initialize();
		
		DissimMatrixGenerator generator = new DissimMatrixGenerator(new File("fragments"), crel);		
		generator.setDissThreshold(0.15);
		generator.addDissimCalc(new LJaccardDissimCalc(0.45));
		generator.addDissimCalc(new GEDDissimCalc(0.15, 0.4));				

		SingleLinkage clusterer = new SingleLinkage(crel, generator);
		clusterer.cluster();
		System.out.println("done");
	}
}
