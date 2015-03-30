package clustering.containment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ContainmentRelationStep2 implements ContainmentRelation {
	Map<String, Integer> idIndexMap = new HashMap<String, Integer>();
	Map<Integer, String> indexIdMap = new HashMap<Integer, String>();
	Map<String, Integer> fragSize = new HashMap<String, Integer>();

	boolean[][] contmatrix;
	
	public ContainmentRelationStep2(Reader idSizeReader, Reader crelReader) throws Exception {
		deserializeIdSizeMap(idSizeReader);
		deserializeContMatrix(crelReader);
	}

	private void deserializeContMatrix(Reader crelReader) throws IOException {
		BufferedReader in = new BufferedReader(crelReader);
		contmatrix = new boolean[idIndexMap.size()][idIndexMap.size()];
		int lnumber = 1;
		String line;
		while ((line = in.readLine()) != null) {
			try {

				StringTokenizer tokenizer = new StringTokenizer(line, ",");
				String id1 = tokenizer.nextToken();	// fragment Id
				int index1 = idIndexMap.get(Integer.valueOf(id1));

				while (tokenizer.hasMoreElements()) {
					String id2 = tokenizer.nextToken();	// fragment Id
					int index2 = idIndexMap.get(Integer.valueOf(id2));
					
					contmatrix[index1][index2] = true;
				}			
			} catch (NumberFormatException e) {
				System.err.printf("Error in line: %d, '%s'\n", lnumber, line);
			}
			lnumber++;
		}		
	}

	private void deserializeIdSizeMap(Reader idSizeReader) throws IOException {
		BufferedReader in = new BufferedReader(idSizeReader);
		int lnumber = 1;
		String line;
		while ((line = in.readLine()) != null) {
			StringTokenizer tokenizer = new StringTokenizer(line, ",");
			String f = tokenizer.nextToken();	// fragment Id
			String s = tokenizer.nextToken();	// fragment Size
			
			try {
				String id = f;
				int size = Integer.valueOf(s);

				int index = idIndexMap.size();
				
				idIndexMap.put(id, index);
				indexIdMap.put(index, id);
				
				fragSize.put(id, size);
			} catch (NumberFormatException e) {
				System.err.printf("Error in line: %d, '%s'\n", lnumber, line);
			}
			lnumber++;
		}
	}

	public int getNumberOfFragments() {
		return idIndexMap.size();
	}
	
	public String getFragmentId(int index) {
		return indexIdMap.get(index);
	}

	public Integer getFragmentIndex(String frag) {
		return idIndexMap.get(frag);
	}

	public Integer getFragmentSize(int frag) {
		return fragSize.get(frag);
	}
	
	public boolean areInContainmentRelation(int frag1, int frag2) {
		return contmatrix[frag1][frag2];
	}
	
	public void initialize() throws Exception {
	}

	@Override
	public List<String> getRoots() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getHierarchy(String integer) {
		// TODO Auto-generated method stub
		return null;
	}	
}
