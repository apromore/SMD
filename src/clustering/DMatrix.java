package clustering;

import org.apromore.mining.MiningConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import clustering.containment.ContainmentRelation;
import clustering.containment.ContainmentRelationImpl;
import clustering.dissimilarity.measure.GEDDissimCalc;
import clustering.dissimilarity.measure.SizeBasedDissimCalc;
import clustering.hierarchy.HierarchyAwareDissimMatrixGenerator;
import clustering.tasksim.TaskDissimCalc;
import clustering.tasksim.TaskDissimSizeBasedCalc;

public class DMatrix {
	
	@Autowired @Qualifier("DissimilarityMatrix")
	private HierarchyAwareDissimMatrixGenerator generator;
	
	@Autowired @Qualifier("ContainmentRelation")
	private ContainmentRelationImpl crel;
	
	public void compute() throws Exception {
		// "ContainmentRelationImpl"   queries the RPSDAG to retrieve the
		// set of models with a minimum size (in the example of 4)
//		ContainmentRelationImpl crel = new ContainmentRelationImpl();
		// ----> IMPORTANT: As the identifier of models retrieved by this
		// class are usually discontinues, the class also assigns an index
		// and provides a method to translate such index to the identifier
		// as stored in the RPSDAG
		// TODO: original minsize = 2
		crel.setMinSize(2);
		// Additionally, it computes the containment relation
		// (transitive and symmetric version of the parent-child relation
		// on the RPSDAG
		crel.initialize();
		
		// "DissimMatrixGeneratorImpl"   computes the dissimilarity values
		// for pair of models. Note that:
		//    1) If two models are in containment relation, the dissimilarity 
		//       is not computed and nothing is stored in the matrix.
		//    2) If the dissimilarity value is above a given threshold (0.15 in
		//       this example), the value is not stored in the matrix.
		//    3) There is a chain of classes that compute dissimilarity. The
		//       first one (LJaccardDissimCalc) is cheaper to compute so the
		//       other one is only computed when the first value is bellow
		//       a local threshold (0.45 in this example).
		// Note that some cells in the matrix are empty. The result might be
		// a particularly sparse matrix. That is way, the matrix is stored
		// as list of tuples in the form of a MultiKeyMap.
//		DissimMatrixGenerator generator = new DissimMatrixGenerator(new File(folder), crel, new PrintStream("/home/cn/temp/data/dmat_"+ dataset +".txt"));		
//		DissimMatrixGenerator generator = new DissimMatrixGenerator(new File(folder), crel, new PrintStream(outFilePath));		
		generator.setDissThreshold(MiningConfig.GED_THRESHOLD);
		generator.addDissimCalc(new SizeBasedDissimCalc(MiningConfig.GED_THRESHOLD));
		generator.addDissimCalc(new GEDDissimCalc(MiningConfig.GED_THRESHOLD, 0.6));
		
//		generator.addDissimCalc(new TaskDissimSizeBasedCalc(0.4));
//		generator.addDissimCalc(new TaskDissimCalc(0.4, 0.4, false));
		
		long start = System.currentTimeMillis();
		generator.computeDissimilarity();
		System.out.println(System.currentTimeMillis() - start);
	}
}
