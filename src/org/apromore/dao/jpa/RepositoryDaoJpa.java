package org.apromore.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apromore.dao.RepositoryDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class RepositoryDaoJpa implements RepositoryDao {
	
	@PersistenceContext
    private EntityManager em;

	@Override
	public void clearRepositoryContent() {
		
		execute("DELETE FROM Annotation");
		execute("DELETE FROM ClusterAssignment");
		execute("DELETE FROM ClusterInfo");
		execute("DELETE FROM ProcessFragmentMap");
		execute("DELETE FROM ProcessModelVersion");
		execute("DELETE FROM ProcessBranch");
		execute("DELETE FROM Process");
		execute("DELETE FROM FragmentVersionDag");
		execute("DELETE FROM FragmentVersion");
		execute("DELETE FROM Content");
		execute("DELETE FROM Edge");
		execute("DELETE FROM Node");
		execute("DELETE FROM EditSessionMapping");
		execute("DELETE FROM GED");
		execute("DELETE FROM Native");
		execute("DELETE FROM NonPocketNode");
	}
	
	private void execute(String st) {
		Query q = em.createQuery(st);
		q.executeUpdate();
	}
}
