/**
 * 
 */
package org.apromore.service;

import org.apromore.exception.ExceptionDao;
import org.apromore.graph.JBPT.CPF;

/**
 * @author Chathura Ekanayake
 *
 */
public interface IComposer {
	
	public CPF compose(String fragmentVersionId) throws ExceptionDao;
}
