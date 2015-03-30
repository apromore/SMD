package nl.tue.tm.is.labels;

import java.util.HashMap;
import java.util.Map;

import net.didion.jwnl.JWNLException;

public class TokenizedLabelCacheStrings {

	private Map<String,TokenizedLabel> cache;
	
	public TokenizedLabelCacheStrings(){
		cache = new HashMap<String, TokenizedLabel>();
	}
	
	public void put(String label) throws JWNLException{
		cache.put(label, new TokenizedLabel(label));
	}
	
	public TokenizedLabel get(String label){
		return cache.get(label); 
	}
}
