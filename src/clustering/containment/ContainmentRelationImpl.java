package clustering.containment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apromore.dao.FragmentVersionDagDao;
import org.apromore.dao.FragmentVersionDao;
import org.apromore.dao.ProcessModelVersionDao;
import org.apromore.dao.model.FragmentVersion;
import org.apromore.dao.model.FragmentVersionDag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ContainmentRelationImpl implements ContainmentRelation {
	enum Stmts {
		QueryCandidateFragmentIds(
				"SELECT fragment_version_id, fragment_size FROM fragment_version WHERE fragment_size >= ? ORDER BY fragment_version_id ASC"), QueryRootFragmentIds(
				"SELECT root_fragment_version_id FROM process_model_version ORDER BY root_fragment_version_id ASC"), QueryParentIds(
				"SELECT fragment_version_id FROM fragment_version_dag WHERE child_fragment_version_id = ?"), QueryFilteredParentChildRelations(
				"SELECT fragment_version_id, child_fragment_version_id FROM fragment_version_dag fd, fragment_version f WHERE f.fragment_version_id = fd.child_fragment_version_id AND f.fragment_size >= ?");
		private String sql;

		private Stmts(String sql) {
			this.sql = sql;
		}

		public String getSql() {
			return sql;
		}
	}

	Map<String, Integer> idIndexMap = new HashMap<String, Integer>();
	Map<Integer, String> indexIdMap = new HashMap<Integer, String>();
	List<String> idList = new ArrayList<String>();
	Map<String, Integer> fragSize = new HashMap<String, Integer>();

	List<String> rootIds = new ArrayList<String>();

	@Autowired @Qualifier("FragmentVersionDao")
	private FragmentVersionDao fDao;

	@Autowired @Qualifier("FragmentVersionDagDao")
	private FragmentVersionDagDao fdagDao;
	
	@Autowired @Qualifier("ProcessModelVersionDao")
	private ProcessModelVersionDao pmvDao;

	/**
	 * Mapping from root fragment Id -> Ids of all ascendant fragments of that
	 * root fragment
	 */
	Map<String, List<String>> hierarchies = new HashMap<String, List<String>>();

	boolean[][] contmatrix;

	// Connection conn;

	// ----- PARAMETERS
	private int minSize = 3;

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public ContainmentRelationImpl() {
	}
	
	public void initialize() throws Exception {
	
		idList.clear();
		indexIdMap.clear();
		idIndexMap.clear();
		fragSize.clear();
		rootIds.clear();
		hierarchies.clear();
		contmatrix = null;
		
		queryFragments();
		initContainmentMatrix();
	}

	private void queryFragments() throws Exception {
		// TODO: original maxsize = 5000
		List<FragmentVersion> fs = fDao
				.getSimilarFragmentsBySize(minSize, 5000);
		for (FragmentVersion f : fs) {
			Integer index = idIndexMap.size();
			String id = f.getFragmentVersionId();
			idIndexMap.put(id, index);
			indexIdMap.put(index, id);
			idList.add(id);
			fragSize.put(id, f.getFragmentSize());
		}
	}

	public void initHierarchies() throws Exception {

		List<String> rootIds = queryRoots();
		System.out.println("Total roots: " + rootIds.size());

		for (String rootId : rootIds) {
			List<String> hierarchy = new ArrayList<String>();
			hierarchies.put(rootId, hierarchy);
			hierarchy.add(rootId);

			int rootIndex = getFragmentIndex(rootId);
			Collection<Integer> fragmentIndecies = indexIdMap.keySet();
			for (Integer fIndex : fragmentIndecies) {
				if (!fIndex.equals(rootIndex)
						&& areInContainmentRelation(rootIndex, fIndex)) {
					hierarchy.add(getFragmentId(fIndex));
				}
			}
		}
	}
	
	public int getFragmentSize(String fragmentId) {
		return fragSize.get(fragmentId);
	}

	public List<String> queryRoots() throws Exception {
		
		rootIds = pmvDao.getRootFragments(minSize);
		return rootIds;

//		Set<String> visitedFIds = new HashSet<String>();
//		for (String fid : idList) {
//			fillRoots(fid, rootIds, visitedFIds);
//		}
//		return rootIds;
	}

	/**
	 * @param fid
	 * @param rootIds2
	 * @param visitedFIds
	 */
	private void fillRoots(String fid, List<String> rootIds,
			Set<String> visitedFIds) throws Exception {

		if (!visitedFIds.contains(fid)) {
			visitedFIds.add(fid);

			List<FragmentVersion> parents = fDao.getParentFragments(fid);
			if (parents.isEmpty()) {
				rootIds.add(fid);
			} else {
				for (FragmentVersion parent : parents) {
					fillRoots(parent.getFragmentVersionId(), rootIds,
							visitedFIds);
				}
			}
		}
	}

	public List<String> queryRootsOld1() {

		return null;

		// PreparedStatement stmt = null;
		// ResultSet rs = null;
		//
		// try {
		// stmt = conn.prepareStatement(Stmts.QueryRootFragmentIds.getSql());
		// rs = stmt.executeQuery();
		// while (rs.next()) {
		// String rootId = rs.getString("fragment_version_id");
		// rootIds.add(rootId);
		// }
		// } finally {
		// if (stmt != null) {
		// try {
		// stmt.close();
		// } catch (SQLException e) {}
		// }
		// if (rs != null) {
		// try {
		// rs.close();
		// } catch (SQLException e) {}
		// }
		// }
		// return rootIds;
	}

	private void initContainmentMatrix() throws Exception {

		List<FragmentVersionDag> dags = fdagDao.getAllDAGEntries(minSize);

		contmatrix = new boolean[idIndexMap.size()][idIndexMap.size()];

		// Initialize the containment matrix using the parent-child relation
		for (FragmentVersionDag fdag : dags) {
			Integer parentIndex = idIndexMap.get(fdag.getId().getFragmentVersionId());
			Integer childIndex = idIndexMap.get(fdag.getId().getChildFragmentVersionId());
			if (parentIndex != null && childIndex != null) {
				contmatrix[parentIndex][childIndex] = true;
//				contmatrix[idIndexMap.get(fdag.getId().getFragmentVersionId())][idIndexMap.get(fdag.getId().getChildFragmentVersionId())] = true;
			}
		}

		// Compute the transitive closure (i.e., ancestor-descendant relation)
		for (int i = 0; i < contmatrix.length; i++)
			for (int j = 0; j < contmatrix.length; j++)
				if (contmatrix[j][i])
					for (int k = 0; k < contmatrix.length; k++)
						contmatrix[j][k] = contmatrix[j][k] | contmatrix[i][k];

		// Compute the symmetric relation
		for (int i = 0; i < contmatrix.length; i++)
			for (int j = 0; j < contmatrix.length; j++)
				if (contmatrix[i][j])
					contmatrix[j][i] = true;

		initHierarchies();
	}

	@Override
	public List<String> getRoots() {
		return rootIds;
	}

	@Override
	public List<String> getHierarchy(String rootFragmentId) {
		return hierarchies.get(rootFragmentId);
	}

	public int getNumberOfFragments() {
		return idIndexMap.size();
	}

	public String getFragmentId(int frag) {
		return indexIdMap.get(frag);
	}

	public Integer getFragmentIndex(String frag) {
		return idIndexMap.get(frag);
	}

	public boolean areInContainmentRelation(int frag1, int frag2) {
		return contmatrix[frag1][frag2];
	}
}
