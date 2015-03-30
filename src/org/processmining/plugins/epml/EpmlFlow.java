package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeFlow"> <xs:attribute name="source"
 *         type="xs:positiveInteger" use="required"/> <xs:attribute
 *         name="target" type="xs:positiveInteger" use="required"/>
 *         </xs:complexType>
 */

public class EpmlFlow extends EpmlElement {

	public final static String TAG = "flow";

	/*
	 * Attributes
	 */
	private String source;
	private String target;

	public EpmlFlow() {
		super(TAG);

		source = null;
		target = null;
	}

	public EpmlFlow(String tag) {
		super(tag);

		source = null;
		target = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "source");
		if (value != null) {
			source = value;
		}
		value = xpp.getAttributeValue(null, "target");
		if (value != null) {
			target = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (source != null) {
			s += exportAttribute("source", source);
		}
		if (target != null) {
			s += exportAttribute("target", target);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		checkPositiveInteger(epml, "source", source, true);
		checkPositiveInteger(epml, "target", target, true);
	}

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}
}
