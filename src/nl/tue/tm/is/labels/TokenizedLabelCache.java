package nl.tue.tm.is.labels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.didion.jwnl.JWNLException;

public class TokenizedLabelCache {

	private Map<String,Set<TokenizedLabel>> cache;
	
	public TokenizedLabelCache(){
		cache = new HashMap<String,Set<TokenizedLabel>>();
	}
	
	public void put(String model, String label) throws JWNLException{
		Set<TokenizedLabel> tlabels = cache.get(model);
		if (tlabels == null){
			tlabels = new HashSet<TokenizedLabel>();
		}
		tlabels.add(new TokenizedLabel(label));
		cache.put(model, tlabels);
	}
	
	public Set<TokenizedLabel> get(String model){
		return cache.get(model); 
	}
}
