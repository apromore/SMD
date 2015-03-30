package org.processmining.plugins.epml;

import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCXOR"> <xs:choice minOccurs="0">
 *         <xs:element name="configuration"> <xs:complexType> <xs:attribute
 *         name="value"> <xs:simpleType> <xs:restriction base="xs:string">
 *         <xs:enumeration value="xor"/> <xs:enumeration value="seq"/>
 *         </xs:restriction> </xs:simpleType> </xs:attribute> <xs:attribute
 *         name="goto" type="xs:positiveInteger" use="optional"/>
 *         </xs:complexType> </xs:element> </xs:choice> </xs:complexType>
 */

public class EpmlXorConfiguration extends EpmlElement {

	public final static String TAG = "configuration";

	/*
	 * Attributes
	 */
	private String value;
	private String go2;

	public EpmlXorConfiguration() {
		super(TAG);

		value = null;
		go2 = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "value");
		if (value != null) {
			this.value = value;
		}
		value = xpp.getAttributeValue(null, "goto");
		if (value != null) {
			go2 = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (value != null) {
			s += exportAttribute("value", value);
		}
		if (go2 != null) {
			s += exportAttribute("goto", go2);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkRestriction(epml, "value", value, Arrays.asList("xor", "seq"), false);
		checkPositiveInteger(epml, "goto", go2, false);
	}
}