package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeAttribute"> <xs:attribute name="typeRef"
 *         type="xs:string" use="required"/> <xs:attribute name="value"
 *         type="xs:string"/> </xs:complexType>
 */

public class EpmlAttribute extends EpmlElement {

	public final static String TAG = "attribute";

	/*
	 * Attributes
	 */
	private String typeRef;
	private String value;

	public EpmlAttribute() {
		super(TAG);

		typeRef = null;
		value = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "typeRef");
		if (value != null) {
			typeRef = value;
		}
		value = xpp.getAttributeValue(null, "value");
		if (value != null) {
			this.value = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (typeRef != null) {
			s += exportAttribute("typeRef", typeRef);
		}
		if (value != null) {
			s += exportAttribute("value", value);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkRequired(epml, "typeRef", typeRef);
	}
}
