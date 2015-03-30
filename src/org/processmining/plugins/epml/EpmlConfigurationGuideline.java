package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

public class EpmlConfigurationGuideline extends EpmlElement {

	public final static String TAG = "configurationGuideline";

	private String idRefs;

	private EpmlExpression expression;

	public EpmlConfigurationGuideline() {
		super(TAG);

		idRefs = null;

		expression = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		String value = xpp.getAttributeValue(null, "idRefs");
		if (value != null) {
			idRefs = value;
		}
	}

	protected String exportAttributes() {
		String s = "";
		if (idRefs != null) {
			s += exportAttribute("idRefs", idRefs);
		}
		return s;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (xpp.getName().equals(EpmlExpression.TAG)) {
			expression = new EpmlExpression();
			expression.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = "";
		if (expression != null) {
			s += expression.exportElement();
		}
		return s;
	}
}
