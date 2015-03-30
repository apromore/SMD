package nl.tue.tm.is.led;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import nl.tue.tm.is.graph.SimpleGraph;

import com.mallardsoft.tuple.Pair;

import lpsolve.*;

public class OptimalLabelMapping {

	public static Set<Pair<Integer,Integer>> optimalScore1(SimpleGraph g1, SimpleGraph g2){
		try {
			//Must convert sets to vectors to ensure order of labels stays the same
			Vector<String> labels1 = new Vector<String>();
			Vector<Integer> vertices1 = new Vector<Integer>();
			for (Integer v1: g1.getVertices()){
				labels1.add(g1.getLabel(v1));
				vertices1.add(v1);
			}
			Vector<String> labels2 = new Vector<String>();			
			Vector<Integer> vertices2 = new Vector<Integer>();
			for (Integer v2: g2.getVertices()){
				labels2.add(g2.getLabel(v2));
				vertices2.add(v2);
			}
			// The problem constitutes an n*m matix
			// Create a problem with n*m variables and 0 constraints
			int n = labels1.size();
			int m = labels2.size();
			LpSolve solver = LpSolve.makeLp(0, n*m);		      		      

			int var = 0;
			double r[] = new double[n*m]; //The formula to maximize
			for (String l1: labels1){
				for (String l2: labels2){
					//The similarity scores are the weights of the variables
					r[var] = StringEditDistance.similarity(l1, l2);
					//All variables are binary
					solver.setBinary(var+1, true);  
					solver.setBounds(var+1, 0.0, 1.0);
					var++;
				}
			}
			String rstr = Arrays.toString(r);
			rstr = rstr.substring(1, rstr.length()-1).replace(",", "");
			solver.strSetObjFn(rstr);			
			solver.setMaxim();
			solver.setVerbose(LpSolve.MSG_NONE);

			//All rows must add up to max. 1 to ensure that each label is mapped to at most one label 
			double[] constraint = new double[n*m];
			for (int i = 0; i < n; i++) {
				Arrays.fill(constraint, 0.0);
				for (int j = 0; j < m; j++) {
					constraint[i * m + j] = 1.0;
				}
				String constraintstr = Arrays.toString(constraint);
				constraintstr = constraintstr.substring(1, constraintstr.length()-1).replace(",", "");
				solver.strAddConstraint(constraintstr, LpSolve.LE, 1.0);
			}

			//All columns must add up to max. 1 to ensure that each label is mapped to at most one label 
			for (int j = 0; j < m; j++) {
				Arrays.fill(constraint, 0);
				for (int i = 0; i < n; i++) {
					constraint[i * m + j] = 1;
				}
				String constraintstr = Arrays.toString(constraint);
				constraintstr = constraintstr.substring(1, constraintstr.length()-1).replace(",", "");
				solver.strAddConstraint(constraintstr, LpSolve.LE, 1.0);
			}

			// solve the problem
			solver.solve();
			
			// process the results
			Set<Pair<Integer,Integer>> result = new HashSet<Pair<Integer,Integer>>();
			double[] res = solver.getPtrVariables();
			for (int i = 0; i < res.length; i++) {
				if (res[i] > 0.1){
					result.add(new Pair<Integer,Integer>(vertices1.get(i/m),vertices2.get(i%m)));
				}
			}

			// delete the problem to free memory
			solver.deleteLp();

			return result;
		}
		catch (LpSolveException e) {
			e.printStackTrace();
		}
		return null; //On error
	}	
}
