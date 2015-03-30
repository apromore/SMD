package clustering.hac;

import java.io.File;

import clustering.containment.ContainmentRelation;
import clustering.containment.ContainmentRelationImpl;
import clustering.dissimilarity.DissimMatrixGenerator;
import clustering.dissimilarity.DissimilarityMatrix;
import clustering.dissimilarity.measure.GEDDissimCalc;
import clustering.dissimilarity.measure.LJaccardDissimCalc;

public class GroupAverage extends HierarchicalAgglomerativeClustering {
	private int count = 0;
	private double proximity = 0;
	
	public GroupAverage(ContainmentRelationImpl crel, DissimilarityMatrix dmatrix) throws Exception {
		super(crel, dmatrix);
	}
	
	protected void resetProximityValue() {
		proximity = 0;
		count = 0;
	}

	protected void updateProximityValue(double newValue) {
		proximity += newValue;
		count++;
	}

	protected boolean isItAValidProximityValue() {
		return count > 0;
	}

	protected double getProximityValue() {
		return proximity/count;
	}

	public static void main(String[] args) throws Exception {
		ContainmentRelationImpl crel = new ContainmentRelationImpl();
		crel.setMinSize(6);
		crel.initialize();
		
		DissimMatrixGenerator generator = new DissimMatrixGenerator(new File("fragments"), crel);		
		generator.setDissThreshold(0.15);
		generator.addDissimCalc(new LJaccardDissimCalc(0.45));
		generator.addDissimCalc(new GEDDissimCalc(0.15, 0.4));				

		GroupAverage clusterer = new GroupAverage(crel, generator);
		clusterer.cluster();

		System.out.println("done");
	}
}
