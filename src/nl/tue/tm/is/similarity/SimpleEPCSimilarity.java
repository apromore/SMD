package nl.tue.tm.is.similarity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.didion.jwnl.JWNLException;
import nl.tue.tm.is.labels.TokenizedLabel;
import nl.tue.tm.is.labels.TokenizedLabelCache;

import com.mallardsoft.tuple.Triple;

public class SimpleEPCSimilarity {

	Map<String,Set<Triple<Double,String,String>>> f1totriple;
	Map<String,Set<Triple<Double,String,String>>> f2totriple;
	SortedSet<Triple<Double,String,String>> result;
	double maxscore;
	double divmin;
	double divmax;
	public static double treshold = 0;
	
	/**
	 * @return	a list containing a similarity score for each pair f1, f2
	 * 			where f1 and f2 are functions from e1 and e2 respectively.
	 * 			The list is ordered by similarity score (ascending). 
	 * @throws JWNLException 
	 */
	private SortedSet<Triple<Double,String,String>> functionSimilarities(String model1, String model2, TokenizedLabelCache tlcache){
		f1totriple = new HashMap<String,Set<Triple<Double,String,String>>>();
		f2totriple = new HashMap<String,Set<Triple<Double,String,String>>>();
		SortedSet<Triple<Double,String,String>> openpairs = new TreeSet<Triple<Double,String,String>>();
		
		for (TokenizedLabel t1: tlcache.get(model1)){
			Set<Triple<Double,String,String>> f1set = new HashSet<Triple<Double,String,String>>();
			f1totriple.put(t1.getLabel(), f1set);
			for (TokenizedLabel t2: tlcache.get(model2)){
				double score = 0;
				score = t1.similarity(t2);
				Set<Triple<Double,String,String>> f2set = f2totriple.get(t2.getLabel());
				if (f2set == null){
					f2set = new HashSet<Triple<Double,String,String>>();
					f2totriple.put(t2.getLabel(),f2set);
				}
				if (score > 0){
					Triple<Double,String,String> toAdd = new Triple<Double,String,String>(score,t1.getLabel(),t2.getLabel());
					f1set.add(toAdd);
					f2set.add(toAdd);
					openpairs.add(toAdd);
				}
			}
		}
		return openpairs;
	}
	
	public void compute(String model1, String model2, TokenizedLabelCache tlcache){
		result = new TreeSet<Triple<Double,String,String>>();
		SortedSet<Triple<Double,String,String>> openpairs = functionSimilarities(model1, model2, tlcache);
		maxscore = 0;
		divmin = 0;
		divmax = 0;
		
		while (!openpairs.isEmpty()){
			Triple<Double,String,String> candidate = openpairs.last();
			result.add(candidate);
			maxscore += Triple.get1(candidate);
			for (Triple<Double,String,String> toRemove: f1totriple.get(Triple.get2(candidate))){
				openpairs.remove(toRemove);
			}
			for (Triple<Double,String,String> toRemove: f2totriple.get(Triple.get3(candidate))){
				openpairs.remove(toRemove);
			}
		}
		
		divmax = maxscore/Math.max((double)tlcache.get(model1).size(), (double)tlcache.get(model2).size());
		divmin = maxscore/Math.min((double)tlcache.get(model1).size(), (double)tlcache.get(model2).size());
	}

	public SortedSet<Triple<Double, String, String>> getResult() {
		return result;
	}

	public double getMaxscore() {
		return maxscore;
	}

	public double getDivmin() {
		return divmin;
	}

	public double getDivmax() {
		return divmax;
	}
	
}
