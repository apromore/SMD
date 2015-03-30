package org.apromore.mining.test;
import java.util.ArrayList;
import java.util.List;


public class RepetitionSignificance<T extends Object> {
	
	public boolean isRepetitionSignificant(List<T> list) {
		for (int i=0; i<list.size(); i++) {
			for (int j=i+1; j<list.size(); j++) {
				if (!list.get(i).equals(list.get(j))) continue;
				
//				c++;
				boolean flag = false; // there is no significant occurrence between i and j
				for (int k=i+1; k<j; k++) {
					boolean flagK = true; // is position k significant 
					for (int m=0; m<list.size(); m++) {
						if (m>=i && m<=j) continue;
						
						if (list.get(k).equals(list.get(m))) {
							flagK = false;
							break;
						}
					}
					
					flag |= flagK;
					if (flag) break;
				}
				
				if (!flag) return false;
			}
		}
		
		
		return true;
	}
	
	public List<T> minCompressToRepetitionSignigficant(List<T> list) {
		List<T> result = new ArrayList<T>(list);
		//System.err.println("Initialization list:" + result.toString());
		
		boolean done = false;
		while (!done) {
			done = true;
			
			int minI = 0;
			int minJ = 0;
			int min = Integer.MAX_VALUE;
			
			// look for insignificant patterns
			for (int i=0; i<result.size(); i++) {
				for (int j=i+1; j<result.size(); j++) {
					if (!result.get(i).equals(result.get(j))) continue;
					
					boolean flag = false; // there is no significant occurrence between i and j
					for (int k=i+1; k<j; k++) {
						boolean flagK = true; // is position k significant 
						for (int m=0; m<result.size(); m++) {
							if (m>=i && m<=j) continue;
							
							if (result.get(k).equals(result.get(m))) {
								flagK = false;
								break;
							}
						}
						
						flag |= flagK;
						if (flag) break;
					}
					
					if (!flag) { 
						done = false;
						if (j-i < min) {
							minI = i;
							minJ = j;
							min = j-i;
						}
					}
				}
			}
			
			if (!done) {
				for (int v=0; v<minJ-minI; v++){
//					System.out.println( "Removed: " + result.get(minI+1));
					result.remove(minI+1);
//					System.out.println("Result (list) after removal:" + result.toString());
					}
				//System.err.println( "List length: " + result.size());
				//System.err.println("Result (list) after removal:" + result.toString());
//				System.out.println( "Sequence significant?" + isRepetitionSignificant(result)+ "\n");
			}
		}
		//System.err.println("Processed list:" + result.toString());
		return result;
	}

	public List<T> maxCompressToRepetitionSignigficant(List<T> list) {
		List<T> result = new ArrayList<T>(list);
		//System.err.println("Initialization list:" + result.toString());
		
		boolean done = false;
		while (!done) {
			done = true;
			
			int maxI = 0;
			int maxJ = 0;
			int max = 0;
			
			// look for insignificant patterns
			for (int i=0; i<result.size(); i++) {
				for (int j=i+1; j<result.size(); j++) {
					if (!result.get(i).equals(result.get(j))) continue;
					
					boolean flag = false; // there is no significant occurrence between i and j
					for (int k=i+1; k<j; k++) {
						boolean flagK = true; // is position k significant 
						for (int m=0; m<result.size(); m++) {
							if (m>=i && m<=j) continue;
							
							if (result.get(k).equals(result.get(m))) {
								flagK = false;
								break;
							}
						}
						
						flag |= flagK;
						if (flag) break;
					}
					
					if (!flag) { 
						done = false;
						if (j-i > max) {
							maxI = i;
							maxJ = j;
							max = j-i;
						}
					}
				}
			}
			
			if (!done) {
				for (int v=0; v<maxJ-maxI; v++){
//					System.out.println( "Removed: " + result.get(minI+1));
					result.remove(maxI+1);
//					System.out.println("Result (list) after removal:" + result.toString());
					}
				//System.err.println( "List length: " + result.size());
				//System.err.println("Result (list) after removal:" + result.toString());
//				System.out.println( "Sequence significant?" + isRepetitionSignificant(result)+ "\n");
			}
		}
		//System.err.println("Processed list:" + result.toString());
		return result;
	}
}
