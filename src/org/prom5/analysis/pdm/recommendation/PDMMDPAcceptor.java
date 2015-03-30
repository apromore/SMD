package org.prom5.analysis.pdm.recommendation;


import javax.swing.JComponent;

import org.prom5.analysis.AnalysisInputItem;
import org.prom5.analysis.AnalysisPlugin;
import org.prom5.framework.models.pdm.PDMModel;
import org.prom5.framework.models.pdm.PDMStateSpace;
import org.prom5.framework.plugin.ProvidedObject;
/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

abstract public class PDMMDPAcceptor implements AnalysisPlugin{

public PDMMDPAcceptor() {
}


public AnalysisInputItem[] getInputItems() {
	AnalysisInputItem[] items = {
								new AnalysisInputItem("PDM MDP Statespace") {
		public boolean accepts(ProvidedObject object) {
			Object[] o = object.getObjects();
			boolean isPDMStateSpace = false;
			for (int i = 0; i < o.length; i++) {
				if (o[i] instanceof PDMStateSpace) {
					isPDMStateSpace = true;
				}
			}
			return isPDMStateSpace;
		}
	}
	} ;
	return items;
}

public JComponent analyse(AnalysisInputItem[] inputs) {
	Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
	PDMModel model = null;
	PDMStateSpace statespace = null;

	for (int i = 0; i < o.length; i++) {
		if (o[i] instanceof PDMModel) {
			model = (PDMModel) o[i];
		}
		if (o[i] instanceof PDMStateSpace){
			statespace = (PDMStateSpace) o[i];
		}
	}
	return analyse(statespace);

}

protected abstract JComponent analyse(PDMStateSpace statespace);


}
