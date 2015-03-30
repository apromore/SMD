/**
 * 
 */
package org.apromore.service;

import java.sql.Connection;
import java.util.List;

import org.apromore.dao.model.FragmentVersion;
import org.apromore.exception.RepositoryException;
import org.apromore.graph.JBPT.CPF;

/**
 * @author Chathura Ekanayake
 *
 */
public interface IDecomposer {
	
	public FragmentVersion decompose(CPF graph, List<String> fragmentIds)
			throws RepositoryException;
	
	public String decomposeFragment(CPF graph, List<String> fragmentIds)
			throws RepositoryException;
}
