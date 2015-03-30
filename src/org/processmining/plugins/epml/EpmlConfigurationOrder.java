package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCOrder"> <xs:sequence> <xs:element
 *         name="flow" type="epml:typeFlow"/> </xs:sequence> </xs:complexType>
 */

public class EpmlConfigurationOrder extends EpmlElement {

	public final static String TAG = "configurationGuideline";

	/*
	 * Elements
	 */
	private EpmlFlow flow;

	public EpmlConfigurationOrder() {
		super(TAG);

		flow = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlFlow.TAG)) {
			flow = new EpmlFlow();
			flow.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (flow != null) {
			s += flow.exportElement();
		}
		return s;
	}
}
