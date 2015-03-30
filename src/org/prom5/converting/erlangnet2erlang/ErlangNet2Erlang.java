/**
 * 
 */
package org.prom5.converting.erlangnet2erlang;

import org.prom5.converting.ConvertingPlugin;
import org.prom5.framework.models.erlangnet.ErlangNet;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.mining.MiningResult;
import org.prom5.mining.petrinetmining.PetriNetResult;

/**
 * @author Kristian Bisgaard Lassen
 *
 */
public class ErlangNet2Erlang implements ConvertingPlugin {

	/**
	 * @see org.prom5.converting.ConvertingPlugin#accepts(org.prom5.framework.plugin.ProvidedObject)
	 */
	public boolean accepts(ProvidedObject original) {
		for (Object o : original.getObjects())
			if (o instanceof ErlangNet)
				return true;
		return false;
	}

	/**
	 * @see org.prom5.converting.ConvertingPlugin#convert(org.prom5.framework.plugin.ProvidedObject)
	 */
	public MiningResult convert(ProvidedObject object) {
		ErlangNet providedPN = null;
		//LogReader log = null;

		for (int i = 0; providedPN == null && i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof ErlangNet) {
				providedPN = (ErlangNet) object.getObjects()[i];
			}
//			if (object.getObjects()[i] instanceof LogReader) {
//				log = (LogReader) object.getObjects()[i];
//			}
		}

		if (providedPN == null) {
			return null;
		}

		ErlangWorkflowNet wfnet = new ErlangNet2ErlangWorkflowNetConverter().convert(providedPN);
		
		return new PetriNetResult(wfnet);
	}

	/**
	 * @see org.prom5.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return null;
	}

	/**
	 * @see org.prom5.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Petri net to Erlang";
	}

}
