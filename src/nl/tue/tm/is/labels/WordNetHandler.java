package nl.tue.tm.is.labels;

import java.io.FileInputStream;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.dictionary.Dictionary;

public class WordNetHandler {
	private static final String WORDNET_CONFIG_FILE = "lib/wordnet/config/my_properties.xml";

	private static Dictionary dict;

	public static Dictionary getDict() {
		if (dict == null) {
			try {
				System.out.println("Loading WordNet....");
				JWNL.initialize(new FileInputStream(WORDNET_CONFIG_FILE));
				dict = Dictionary.getInstance();
				System.out.print("done.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dict;
	}

}
