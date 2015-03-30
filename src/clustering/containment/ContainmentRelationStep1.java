package clustering.containment;

import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.tue.tm.is.epc.EPC;
import nl.tue.tm.is.graph.SimpleGraph;

public class ContainmentRelationStep1 {
	enum Stmts {
		QueryCandidateFragmentIds("SELECT nodeid, size FROM labelhash WHERE size >= ? ORDER BY size ASC, nodeid ASC"),
		QueryFilteredParentChildRelations("SELECT parent, child FROM parentChildMap, labelhash WHERE nodeid = child AND size >= ?")
			;
		private String sql;
		private Stmts (String sql) { this.sql = sql; }
		public String getSql() { return sql; }
	}

	List<Integer> idList = new LinkedList<Integer>();
	Map<Integer, Integer> idSizeMap = new LinkedHashMap<Integer, Integer>();
	Map<Integer, Integer> idIndexMap = new HashMap<Integer, Integer>();
	boolean[][] contmatrix;
	
	Connection conn;
	
	// ----- PARAMETERS
	private int minSize = 10;

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public ContainmentRelationStep1() throws Exception {
		this("refactoring", "refactor", "refactor");
	}

	public ContainmentRelationStep1(String db, String user, String passw) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://localhost:3306/"+db;
		conn = DriverManager.getConnection(url, user, passw);
	}

	public void queryFragments(File dir) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		int nofiltered = 0;
		
		try {
			stmt = conn.prepareStatement(Stmts.QueryCandidateFragmentIds.getSql());
			stmt.setInt(1, minSize);
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				Integer id = rs.getInt(1);
				
				String fname = String.format(dir.getName() + "/Fragment_%d.epml", id);
				EPC epc = EPC.loadEPML(fname);
				SimpleGraph graph = new SimpleGraph(epc);

				if (graph.getVertices().size() >= minSize) {
					idSizeMap.put(id, graph.getVertices().size());
					idIndexMap.put(id, idIndexMap.size());
					idList.add(id);
				} else 
					nofiltered++;
			}
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {}
			}
		}
		
		System.out.println("Number of filtered fragments: " + nofiltered);
	}
	
	public void initContainmentMatrix() throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.prepareStatement(Stmts.QueryFilteredParentChildRelations.getSql());		
			stmt.setInt(1, minSize);
			
			contmatrix = new boolean[idIndexMap.size()][idIndexMap.size()];
			
			rs = stmt.executeQuery();
			
			/* ------------------------------------------------------------------
			 * Initialize the containment matrix using the parent-child relation
			 */
			while (rs.next()) {
				int frag1 = rs.getInt(1);
				int frag2 = rs.getInt(2);
				if (idIndexMap.containsKey(frag1) && idIndexMap.containsKey(frag2))	
					contmatrix[idIndexMap.get(frag1)][idIndexMap.get(frag2)] = true;
			}
			/* ------------------------------------------------------------------
			 * Compute the transitive closure (i.e., ancestor-descendant relation)
			 */
			for (int i = 0; i < contmatrix.length; i++) 
				for (int j = 0; j < contmatrix.length; j++) 
					if (contmatrix[i][j])
						for (int k = 0; k < contmatrix.length; k++)
							contmatrix[i][k] = contmatrix[j][k] | contmatrix[i][k];

			/* ------------------------------------------------------------------
			 * Compute the symmetric relation
			 */
			for (int i = 0; i < contmatrix.length; i++)
				for (int j = 0; j < contmatrix.length; j++)
					if (contmatrix[i][j]) contmatrix[j][i] = true;
			
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {}
			}
		}
	}

	
	public void serializeIdSizeList(PrintStream out) {
		for (Entry<Integer, Integer> entry: idSizeMap.entrySet())
			out.printf("%d,%d\n", entry.getKey(), entry.getValue());
	}
	
	public void serializeContainmentRelation(PrintStream out) {
		for (int i = 0; i < contmatrix.length; i++) {
			out.print(idList.get(i));
			for (int j = 0; j < contmatrix.length; j++) {
				if (contmatrix[i][j])
					out.print("," + idList.get(j));
			}
			out.println();
		}
	}
}
