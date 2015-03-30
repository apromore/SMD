package org.processmining.plugins.epml;

import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCRange"> <xs:choice minOccurs="0">
 *         <xs:element name="configuration"> <xs:complexType> <xs:attribute
 *         name="optionality"> <xs:simpleType> <xs:restriction base="xs:string">
 *         <xs:enumeration value="mnd"/> <xs:enumeration value="off"/>
 *         <xs:enumeration value="opt"/> </xs:restriction> </xs:simpleType>
 *         </xs:attribute> <xs:attribute name="range"> <xs:simpleType>
 *         <xs:restriction base="xs:string"> <xs:enumeration value="node"/>
 *         <xs:enumeration value="reducedRange"/> </xs:restriction>
 *         </xs:simpleType> </xs:attribute> <xs:attribute name="goto"
 *         type="xs:positiveInteger" use="optional"/> <xs:attribute
 *         name="increment" use="optional"> <xs:simpleType> <xs:union
 *         memberTypes="xs:nonNegativeInteger"> <xs:simpleType> <xs:restriction
 *         base="xs:string"> <xs:enumeration value="k"/> </xs:restriction>
 *         </xs:simpleType> </xs:union> </xs:simpleType> </xs:attribute>
 *         <xs:attribute name="decrement" use="optional"> <xs:simpleType>
 *         <xs:union memberTypes="xs:nonNegativeInteger"> <xs:simpleType>
 *         <xs:restriction base="xs:string"> <xs:enumeration value="k"/>
 *         </xs:restriction> </xs:simpleType> </xs:union> </xs:simpleType>
 *         </xs:attribute> </xs:complexType> </xs:element> </xs:choice>
 *         </xs:complexType>
 */

public class EpmlRangeConfiguration extends EpmlElement {

	public final static String TAG = "configuration";

	/*
	 * Attributes
	 */
	private String optionality;
	private String range;
	private String go2;
	private String increment;
	private String decrement;

	public EpmlRangeConfiguration() {
		super(TAG);

		optionality = null;
		range = null;
		go2 = null;
		increment = null;
		decrement = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "optionality");
		if (value != null) {
			optionality = value;
		}
		value = xpp.getAttributeValue(null, "range");
		if (value != null) {
			range = value;
		}
		value = xpp.getAttributeValue(null, "goto");
		if (value != null) {
			go2 = value;
		}
		value = xpp.getAttributeValue(null, "increment");
		if (value != null) {
			increment = value;
		}
		value = xpp.getAttributeValue(null, "decrement");
		if (value != null) {
			decrement = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (optionality != null) {
			s += exportAttribute("optionality", optionality);
		}
		if (range != null) {
			s += exportAttribute("range", range);
		}
		if (go2 != null) {
			s += exportAttribute("goto", go2);
		}
		if (increment != null) {
			s += exportAttribute("increment", decrement);
		}
		if (decrement != null) {
			s += exportAttribute("decrement", decrement);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkRestriction(epml, "optionality", optionality, Arrays.asList("mnd", "off", "opt"), false);
		checkRestriction(epml, "range", range, Arrays.asList("node", "reducedrange"), false);
		checkPositiveInteger(epml, "goto", go2, false);
		checkNonNegativeIntegerOrK(epml, "increment", increment, false);
		checkNonNegativeIntegerOrK(epml, "decrement", decrement, false);
	}
}