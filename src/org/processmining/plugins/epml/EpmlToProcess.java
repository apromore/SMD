package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeToProcess"> <xs:attribute
 *         name="linkToEpcId" type="xs:positiveInteger" use="required"/>
 *         </xs:complexType>
 */

public class EpmlToProcess extends EpmlElement {

	public final static String TAG = "toProcess";

	/*
	 * Attributes
	 */
	private String linkToEpcId;

	public EpmlToProcess() {
		super(TAG);

		linkToEpcId = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "linkToEpcId");
		if (value != null) {
			linkToEpcId = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (linkToEpcId != null) {
			s += exportAttribute("linkToEpcId", linkToEpcId);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkPositiveInteger(epml, "linkToEpcId", linkToEpcId, true);
	}
}
