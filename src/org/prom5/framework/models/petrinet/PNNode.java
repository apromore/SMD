/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.prom5.framework.models.petrinet;

import org.prom5.framework.models.ModelGraphVertex;

/**
 * <p>Title: PN Node</p>
 *
 * <p>Description: Superclass of Transition and Place</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author Eric Verbeek
 * @version 1.0
 */
abstract public class PNNode extends ModelGraphVertex{
	public PNNode(PetriNet net) {
		super(net);
	}
}
