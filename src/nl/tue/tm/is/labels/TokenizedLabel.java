package nl.tue.tm.is.labels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import nl.tue.tm.is.led.LabelEditDistance;
import nl.tue.tm.is.led.StringEditDistance;
import nl.tue.tm.is.similarity.SimpleEPCSimilarity;

import org.tartarus.snowball.ext.englishStemmer;

import edu.sussex.nlp.jws.JiangAndConrath;

public class TokenizedLabel {

	private static final double SYN_SCORE = 0.75; //Score for a synonym match
	
	private List<String> tkLabel; //List of words in the label
	/* Each wordsToMatch[i] is the set of lemmas from WordNet that is found 
	 * by searching for tkLabel[i]. If no words are found for tkLabel[i], then 
	 * wordsToMatch[i] == tkLabel[i].
	 */
	private List<Set<String>> wordsToMatch; 
	private Set<String> wnWords; // \Bigcup_{ws \in wordsToMatch} ws
	private Set<String> synWords; //The set of lemmas from WordNet that are synonyms of the words in wnWords
	private String label; //The original label
	
	public TokenizedLabel(String label) throws JWNLException{
		tkLabel = new ArrayList<String>();
		wordsToMatch = new ArrayList<Set<String>>();
		wnWords = new HashSet<String>();
		synWords = new HashSet<String>();
		
		this.label = label;
		
		String preprocessedLabel = new String(label);
		preprocessedLabel = preprocessedLabel.toLowerCase();
		preprocessedLabel = preprocessedLabel.replaceAll("\\W", " ");
		englishStemmer stemmer = new englishStemmer();		
		List<String> intermediate = tokenize(preprocessedLabel);
		for (String word: intermediate){
			if (!stemmer.getStopWords().contains(word)){
				tkLabel.add(word);
				Set<String> wnWordsToAdd = new HashSet<String>();
				Set<String> synWordsToAdd = new HashSet<String>();
				for (IndexWord w: WordNetHandler.getDict().lookupAllIndexWords(word).getIndexWordArray()){
					wnWordsToAdd.add(w.getLemma());
					for (Synset synset: w.getSenses()){
						for (Word syn: synset.getWords()){
							synWordsToAdd.add(syn.getLemma());
						}
					}
				}
				synWordsToAdd.removeAll(wnWordsToAdd);
				if (wnWordsToAdd.isEmpty()){
					wnWordsToAdd.add(word);
				}
				wordsToMatch.add(wnWordsToAdd);
				wnWords.addAll(wnWordsToAdd);
				synWords.addAll(synWordsToAdd);
			}
		}
	}
	
	public double similarityWordnet(TokenizedLabel t2, JiangAndConrath jcn){
		double score = 0.0;
		for (String word1: wnWords){
			double bestEd = 0;
			for (String word2: t2.wnWords){
				Double v = StringEditDistance.wordnetCache.get(word1+";"+word2+";v");
				if (v == null) {
					v = jcn.max(word1, word2, "v");
					StringEditDistance.wordnetCache.put(word1+";"+word2+";v", v);
				}
				Double n = StringEditDistance.wordnetCache.get(word1+";"+word2+";n");
				if (n == null) {
					n = jcn.max(word1, word2, "n");
					StringEditDistance.wordnetCache.put(word1+";"+word2+";n", n);
				}			
				double stringEd = LabelEditDistance.edNormalized(word1, word2);
//				System.out.println("\t"+word1+ " "+ word2+ " "+stringEd+ " "+n+" "+v+ " "+ StringEditDistance.editDistance(word1, word2));
				double ed = Math.max(stringEd, 
						Math.min(1, Math.max(v, n)));
				if (ed > bestEd) {
					bestEd = ed;
				}
			}
			score += bestEd;
		}
		
		for (String word1: t2.wnWords){
			double bestEd = 0;
			for (String word2: wnWords){
				Double v = StringEditDistance.wordnetCache.get(word1+";"+word2+";v");
				if (v == null) {
					v = jcn.max(word1, word2, "v");
					StringEditDistance.wordnetCache.put(word1+";"+word2+";v", v);
				}
				Double n = StringEditDistance.wordnetCache.get(word1+";"+word2+";n");
				if (n == null) {
					n = jcn.max(word1, word2, "n");
					StringEditDistance.wordnetCache.put(word1+";"+word2+";n", n);
				}				
				double stringEd = LabelEditDistance.edNormalized(word1, word2);
//				System.out.println("\t"+word1+ " "+ word2+ " "+stringEd+ " "+n+" "+v + " "+ StringEditDistance.editDistance(word1, word2));
				double ed = Math.max(stringEd, 
						Math.min(1, Math.max(v, n)));
				
				if (ed > bestEd) {
					bestEd = ed;
				}
			}
			score += bestEd;
		}
		return score/((double)wnWords.size()+(double)t2.wnWords.size());
	}
	
	public double similarity(TokenizedLabel t2){
		double score = 0.0;
		for (Set<String> words: wordsToMatch){
			boolean foundAsWord = false;
			boolean foundAsSynonym = false;
			double bestED = 0;
			for (Iterator<String> wordsIterator = words.iterator(); wordsIterator.hasNext() && !foundAsWord;){
				String word = wordsIterator.next();
				foundAsWord = t2.wnWords.contains(word);
				foundAsSynonym |= t2.synWords.contains(word);
				for (String w : t2.wnWords) {
					double ed = LabelEditDistance.edNormalized(word, w);
					if (ed > bestED) {
						bestED = ed;
					}
				}
			}
			if (foundAsWord){
				score += 1.0;
			}
			else if (foundAsSynonym){
				score += SYN_SCORE;
			} 
//			else if (bestED >= SimpleEPCSimilarity.treshold) {
//				score += bestED;
//			}
		}
		for (Set<String> words: t2.wordsToMatch){
			boolean foundAsWord = false;
			boolean foundAsSynonym = false;
			double bestED = 0;
			for (Iterator<String> wordsIterator = words.iterator(); wordsIterator.hasNext() && !foundAsWord;){
				String word = wordsIterator.next();
				foundAsWord = wnWords.contains(word);
				foundAsSynonym |= synWords.contains(word);
				for (String w : t2.wnWords) {
					double ed = LabelEditDistance.edNormalized(word, w);
					if (ed > bestED) {
						bestED = ed;
					}
				}
			}
			if (foundAsWord){
				score += 1.0;
			}
			else if (foundAsSynonym){
				score += SYN_SCORE;
			} 
//			else if (bestED >= SimpleEPCSimilarity.treshold) {
//				score += bestED;
//			}
		}
		return score/((double)wordsToMatch.size()+(double)t2.wordsToMatch.size());
	}
	
	@Override
	public String toString() {
		String result = "[";
		for (Iterator<String> words = tkLabel.iterator(); words.hasNext();){
			result += words.next();
			if (words.hasNext()){
				result += ",";
			}
		}
		return result + "]";
	}
		
	/**
	 * Returns the list of words in the label. A word is a list of non-whitespace characters (regexp class: \s = [ \t\n\x0B\f\r]).
	 * 
	 * @param label a random string
	 * @return the list of words in the label
	 */
	static public List<String> tokenize(String label){
		String intermediate[] = label.split("\\s+");
		List<String> result = new ArrayList<String>();
		
		for (String word: intermediate){
			String procWord = word.trim();
			if (procWord.length() != 0){
				result.add(procWord);
			}
		}
		return result;
	}
	
	public List<String> getTokenWords() {
		return tkLabel;
	}
	
	public List<Set<String>> getWordsToMatch() {
		return wordsToMatch;
	}

	public Set<String> getWordNetWords() {
		return wnWords;
	}

	public Set<String> getSynonymWords() {
		return synWords;
	}
	
	public String getLabel(){
		return label;
	}
	
	public static void main(String[] a) throws JWNLException {
		//TestBetterModelsAStar.useWordnetSimilarity();
		//TokenizedLabel l1 = new TokenizedLabel("Warehouse Management");
		//TokenizedLabel l2 = new TokenizedLabel("Printout of Physical Inventory Document");
		
		//System.out.println(l1.similarityWordnet(l2, StringEditDistance.getWordnet()));
	}
}
