package org.processmining.plugins.epml;

import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCFunction"> <xs:choice minOccurs="0">
 *         <xs:element name="configuration"> <xs:complexType> <xs:attribute
 *         name="value"> <xs:simpleType> <xs:restriction base="xs:string">
 *         <xs:enumeration value="on"/> <xs:enumeration value="off"/>
 *         <xs:enumeration value="opt"/> </xs:restriction> </xs:simpleType>
 *         </xs:attribute> </xs:complexType> </xs:element> </xs:choice>
 *         </xs:complexType>
 */
public class EpmlFunctionConfiguration extends EpmlElement {

	public final static String TAG = "configuration";

	/*
	 * Attributes
	 */
	String value;

	public EpmlFunctionConfiguration() {
		super(TAG);

		value = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "value");
		if (value != null) {
			this.value = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (value != null) {
			s += exportAttribute("value", value);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkRestriction(epml, "value", value, Arrays.asList("on", "off", "opt"), false);
	}
}
