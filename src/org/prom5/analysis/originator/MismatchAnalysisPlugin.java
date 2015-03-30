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

package org.prom5.analysis.originator;


import javax.swing.JComponent;

import org.prom5.analysis.AnalysisInputItem;
import org.prom5.analysis.AnalysisPlugin;
import org.prom5.analysis.orgsimilarity.SimilarityModel;
import org.prom5.framework.plugin.ProvidedObject;
/**
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: TU/e</p>
 *
 * @author Minseok Song
 * @version 1.0
 */

public class MismatchAnalysisPlugin implements AnalysisPlugin {

	public MismatchAnalysisPlugin() {
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		SimilarityModel simModel = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof SimilarityModel) {
				simModel = (SimilarityModel) o[i];
			}
		}
		
		return new MinmatchAnalysisUI(simModel, new Mismatch2DTableModel(simModel));
	}

	public String getHtmlDescription() {
		return "<p> This plug-in performs mismatch analysis.<p> ";
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = {
									new AnalysisInputItem("Similarity Model") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasLog = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof SimilarityModel) {
						hasLog = true;
					}
				}
				return hasLog;
			}
		}
		} ;
		return items;
	}

	public String getName() {
		return "Mismatch Analysis for Org Models";
	}
}
