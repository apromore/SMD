package clustering.containment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AncestorRelationImpl {
	enum Stmts {
		QueryCandidateFragmentIds("SELECT nodeid, size FROM labelhash WHERE size >= ? ORDER BY size DESC, nodeid ASC"),
		QueryRootFragmentIds("SELECT nodeid FROM roots"),
		QueryFilteredParentChildRelations("SELECT parent, child FROM parentChildMap, labelhash WHERE nodeid = child AND size >= ?")
			;
		private String sql;
		private Stmts (String sql) { this.sql = sql; }
		public String getSql() { return sql; }
	}

	Map<Integer, Integer> idIndexMap = new HashMap<Integer, Integer>();
	Map<Integer, Integer> indexIdMap = new HashMap<Integer, Integer>();
	List<Integer> idList = new ArrayList<Integer>();
	Map<Integer, Integer> fragSize = new HashMap<Integer, Integer>();

	boolean[][] ancmatrix;
	
	Connection conn;
	
	// ----- PARAMETERS
	private int minSize = 10;

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public AncestorRelationImpl() throws Exception {
		this("refactoring", "refactor", "refactor");
	}

	public AncestorRelationImpl(String db, String user, String passw) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://localhost:3306/"+db;
		conn = DriverManager.getConnection(url, user, passw);
	}

	public void queryFragments() throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.prepareStatement(Stmts.QueryCandidateFragmentIds.getSql());		
			stmt.setInt(1, minSize);
			
			idIndexMap.clear();
			fragSize.clear();
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				Integer index = idIndexMap.size();
				Integer id = rs.getInt(1);
				idIndexMap.put(id, index);
				indexIdMap.put(index, id);
				idList.add(id);
				fragSize.put(id, rs.getInt(2));
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
	}
	
	public void initAncestorRelation() throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.prepareStatement(Stmts.QueryFilteredParentChildRelations.getSql());		
			stmt.setInt(1, minSize);
			
			ancmatrix = new boolean[idIndexMap.size()][idIndexMap.size()];
			
			rs = stmt.executeQuery();
			
			/* ------------------------------------------------------------------
			 * Initialize the containment matrix using the child-parent relation
			 */
			while (rs.next())
				ancmatrix[idIndexMap.get(rs.getInt(2))][idIndexMap.get(rs.getInt(1))] = true;
			
			/* ------------------------------------------------------------------
			 * Compute the transitive closure (i.e., descendant-ancestor relation)
			 */
			for (int i = 0; i < ancmatrix.length; i++) 
				for (int j = 0; j < ancmatrix.length; j++) 
					if (ancmatrix[j][i])
						for (int k = 0; k < ancmatrix.length; k++)
							ancmatrix[j][k] = ancmatrix[j][k] | ancmatrix[i][k];

			rs.close();
			stmt.close();
			
			BitSet roots = new BitSet(idIndexMap.size());
			
			stmt = conn.prepareStatement(Stmts.QueryRootFragmentIds.getSql());
			rs = stmt.executeQuery();
			while (rs.next()) {
				int index = idIndexMap.get(rs.getInt(1));
				roots.set(index);
			}
			
			for (int i = roots.nextClearBit(0); i >= 0 && i < ancmatrix.length; i = roots.nextClearBit(i+1))
				for (int j = 0; j < ancmatrix.length; j++)
					ancmatrix[j][i] = false;
			
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
	
	public Set<Integer> getRoots(int fragId) {
		Set<Integer> result = new HashSet<Integer>();
		int fragIndex = idIndexMap.get(fragId);
		for (int i = 0; i < ancmatrix.length; i++)
			if (ancmatrix[fragIndex][i])
				result.add(indexIdMap.get(i));
		return result;
	}
	
	public int getFragSize(int fragId) {
		return fragSize.get(fragId);
	}
	
	public void initialize() throws Exception {
		queryFragments();
		initAncestorRelation();
	}
}
