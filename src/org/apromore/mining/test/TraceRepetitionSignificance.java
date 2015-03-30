package org.apromore.mining.test;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XTrace;


public class TraceRepetitionSignificance<T extends Object> {
	
	public boolean isTraceRepetitionSignificant(XTrace trace) {
		for (int i=0; i<trace.size(); i++) {
			for (int j=i+1; j<trace.size(); j++) {
				//if (!trace.get(i).equals(trace.get(j))) continue;
				if (!XLogInfoImpl.STANDARD_CLASSIFIER.sameEventClass(trace.get(i), trace.get(j))) continue;
				
				boolean flag = false; // there is no significant occurrence between i and j
				for (int k=i+1; k<j; k++) {
					boolean flagK = true; // is position k significant 
					for (int m=0; m<trace.size(); m++) {
						if (m>=i && m<=j) continue;
						
						//if (trace.get(k).equals(trace.get(m))) {
						if (XLogInfoImpl.STANDARD_CLASSIFIER.sameEventClass(trace.get(k), trace.get(m))) {
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
	
	public XTrace minCompressTraceToRepetitionSignigficant(XTrace trace) {
		XTrace result = (XTrace) trace.clone();
		
		boolean done = false;
		while (!done) {
			done = true;
			
			int minI = 0;
			int minJ = 0;
			int min = Integer.MAX_VALUE;
			
			// look for insignificant patterns
			for (int i=0; i<result.size(); i++) {
				for (int j=i+1; j<result.size(); j++) {
					if (!XLogInfoImpl.STANDARD_CLASSIFIER.sameEventClass(result.get(i), result.get(j))){
						continue;
					}
					
					boolean flag = false; // there is no significant occurrence between i and j
					for (int k=i+1; k<j; k++) {
						boolean flagK = true; // is position k significant 
						for (int m=0; m<result.size(); m++) {
							if (m>=i && m<=j) continue;
							
							if (XLogInfoImpl.STANDARD_CLASSIFIER.sameEventClass(result.get(k), result.get(m))) {
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
						}
					}
				}
			}
			
			if (!done){
				for (int v=0; v<minJ-minI; v++) {
					result.remove(minI+1);
				}
				
			} 
			
		}
		return result;
	}

}
