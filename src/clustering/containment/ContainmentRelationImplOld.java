package clustering.containment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContainmentRelationImplOld {
	enum Stmts {
		QueryCandidateFragmentIds("SELECT nodeid, size FROM labelhash WHERE size >= ? ORDER BY nodeid ASC"),
		QueryRootFragmentIds("SELECT nodeid FROM roots ORDER BY nodeid ASC"),
		QueryParentIds("SELECT parent FROM parentChildMap WHERE child = ?"),
		QueryFilteredParentChildRelations("SELECT parent, child FROM parentChildMap, labelhash WHERE nodeid = child AND size >= ?");
		private String sql;
		private Stmts (String sql) { this.sql = sql; }
		public String getSql() { return sql; }
	}
	
//	enum Stmts {
//		QueryCandidateFragmentIds("SELECT nodeid, size FROM labelhash WHERE size >= ? ORDER BY size ASC, nodeid ASC"),
//		QueryFilteredParentChildRelations("SELECT parent, child FROM parentChildMap, labelhash WHERE nodeid = child AND size >= ?")
//			;
//		private String sql;
//		private Stmts (String sql) { this.sql = sql; }
//		public String getSql() { return sql; }
//	}

	Map<Integer, Integer> idIndexMap = new HashMap<Integer, Integer>();
	Map<Integer, Integer> indexIdMap = new HashMap<Integer, Integer>();
	List<Integer> idList = new ArrayList<Integer>();
	Map<Integer, Integer> fragSize = new HashMap<Integer, Integer>();
	
	List<Integer> rootIds = new ArrayList<Integer>();
	Map<Integer, List<Integer>> hierarchies = new HashMap<Integer, List<Integer>>();

	boolean[][] contmatrix;
	
	Connection conn;
	
	// ----- PARAMETERS
	private int minSize = 10;

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public ContainmentRelationImplOld() throws Exception {
		this("refactoring", "refactor", "refactor");
	}

	public ContainmentRelationImplOld(String db, String user, String passw) throws Exception {
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
	
	public void initHierarchies() throws Exception {
		
		List<Integer> rootIds = queryRoots();
		System.out.println("Total roots: " + rootIds.size());
		
		for (Integer rootId : rootIds) {
			List<Integer> hierarchy = new ArrayList<Integer>();
			hierarchies.put(rootId, hierarchy);
			hierarchy.add(rootId);
			
			int rootIndex = getFragmentIndex(rootId);
			Collection<Integer> fragmentIndecies = indexIdMap.keySet();
			for (Integer fIndex : fragmentIndecies) {
				if (!fIndex.equals(rootIndex) && areInContainmentRelation(rootIndex, fIndex)) {
					hierarchy.add(getFragmentId(fIndex));
				}
			}
		}
	}
	
	public List<Integer> queryRoots() throws Exception {
		
		Set<Integer> visitedFIds = new HashSet<Integer>();
		for (Integer fid : idList) {
			fillRoots(fid, rootIds, visitedFIds);
		}
		return rootIds;
	}
	
	/**
	 * @param fid
	 * @param rootIds2
	 * @param visitedFIds
	 */
	private void fillRoots(Integer fid, List<Integer> rootIds, Set<Integer> visitedFIds) throws Exception {
		
		if (!visitedFIds.contains(fid)) {
			visitedFIds.add(fid);
			
			List<Integer> parentIds = getParentIds(fid);
			if (parentIds.isEmpty()) {
				rootIds.add(fid);
			} else {
				for (Integer parentId : parentIds) {
					fillRoots(parentId, rootIds, visitedFIds);
				}
			}
		}
	}

	/**
	 * @param fid
	 * @return
	 */
	private List<Integer> getParentIds(Integer fid) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Integer> parentIds = new ArrayList<Integer>();
		try {
			stmt = conn.prepareStatement(Stmts.QueryParentIds.getSql());
			stmt.setInt(1, fid);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Integer parentId = rs.getInt("parent");
				parentIds.add(parentId);
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
		return parentIds;
	}

	public List<Integer> queryRootsOld1() throws Exception {
		
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.prepareStatement(Stmts.QueryRootFragmentIds.getSql());		
			rs = stmt.executeQuery();
			while (rs.next()) {
				Integer rootId = rs.getInt("nodeid");
				rootIds.add(rootId);
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
		return rootIds;
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
			while (rs.next())
				contmatrix[idIndexMap.get(rs.getInt(1))][idIndexMap.get(rs.getInt(2))] = true;
			
			/* ------------------------------------------------------------------
			 * Compute the transitive closure (i.e., ancestor-descendant relation)
			 */
			for (int i = 0; i < contmatrix.length; i++) 
				for (int j = 0; j < contmatrix.length; j++) 
					if (contmatrix[j][i])
						for (int k = 0; k < contmatrix.length; k++)
							contmatrix[j][k] = contmatrix[j][k] | contmatrix[i][k];

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
		
		initHierarchies();
	}
	
	public List<Integer> getRoots() {
		return rootIds;
	}

	public List<Integer> getHierarchy(Integer integer) {
		return hierarchies.get(integer);
	}

	public int getNumberOfFragments() {
		return idIndexMap.size();
	}
	
	public Integer getFragmentId(int frag) {
		return indexIdMap.get(frag);
	}

	public Integer getFragmentIndex(int frag) {
		return idIndexMap.get(frag);
	}

	public boolean areInContainmentRelation(int frag1, int frag2) {
		return contmatrix[frag1][frag2];
	}
	
	public void initialize() throws Exception {
		queryFragments();
		initContainmentMatrix();
	}	
}
