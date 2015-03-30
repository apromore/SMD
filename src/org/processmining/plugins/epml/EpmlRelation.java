package org.processmining.plugins.epml;

import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeRelation"> <xs:attribute name="source"
 *         type="xs:positiveInteger" use="required"/> <xs:attribute
 *         name="target" type="xs:positiveInteger" use="required"/>
 *         <xs:attribute name="type" use="optional"> <xs:simpleType>
 *         <xs:restriction base="xs:string"> <xs:enumeration value="role"/>
 *         <xs:enumeration value="input"/> <xs:enumeration value="output"/>
 *         <xs:enumeration value="any"/> </xs:restriction> </xs:simpleType>
 *         </xs:attribute> </xs:complexType>
 */

public class EpmlRelation extends EpmlFlow {

	public final static String TAG = "relation";

	/*
	 * Attributes
	 * 
	 * source and target are inherited from EpmlFlow.
	 */
	private String type;

	public EpmlRelation() {
		super(TAG);

		type = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "type");
		if (value != null) {
			type = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (type != null) {
			s += exportAttribute("type", type);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkRestriction(epml, "type", type, Arrays.asList("role", "input", "output", "any"), false);
	}
}
