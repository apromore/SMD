package pcf;

import java.io.BufferedReader;
import java.io.FileReader;

import nl.tue.tm.is.led.LabelEditDistance;

import org.tartarus.snowball.SnowballStemmer;

public class LoaderOpt {
	String filename = "pcf.conf";
	
	public static ClassificationTree loadClassificationTree(String delimeter) {
		
		SnowballStemmer stemmer = null;
		try {
			Class stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
			stemmer = (SnowballStemmer) stemClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ClassificationTree tree = new ClassificationTree();

		BufferedReader fr;
		try {
			fr = new BufferedReader(new FileReader("lib/pcf.conf"));
			String l = fr.readLine(); 
			ClassificationTreeNode toAdd = tree.rootnode;
			
			while (l != null){
				int level = 0;
				if (l.startsWith("\t\t\t")) {
					level = 3;
					l = l.trim();
					l = l.substring(l.indexOf(" ")+1, l.lastIndexOf(" "));
				}
				else if (l.startsWith("\t\t")) {
					level = 2;
					l = l.trim();
					l = l.substring(l.indexOf(" ")+1, l.lastIndexOf(" "));
				}
				else if (l.startsWith("\t")) {
					level = 1;
					l = l.trim();
					l = l.substring(l.indexOf(" ")+1, l.lastIndexOf(" "));
				}
				else {
					level = 0;
					l = l.trim();
					l = l.substring(l.indexOf(" ")+1, l.lastIndexOf(" "));
				}
				ClassificationTreeNode add = null;
				switch (level) {
					case 0:
						toAdd = tree.rootnode;
						add = new ClassificationTreeNode(l, 0);
						break;
					case 1:
						add = new ClassificationTreeNode(l, 1);
						break;
					case 2:
						add = new ClassificationTreeNode(l, 2);
						break;
					case 3:
						add = new ClassificationTreeNode(l, 3);
						break;
				}
				while (toAdd.level > add.level - 1) {
					toAdd = toAdd.parent;
				}
				tree.stringNodeMap.put(LabelEditDistance.tokenizeAndStem(l, delimeter, stemmer), add);
				toAdd.addChild(add);
				toAdd = add;

				l = fr.readLine();
			}
//			System.out.println(tree);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tree;
	}
		
}
