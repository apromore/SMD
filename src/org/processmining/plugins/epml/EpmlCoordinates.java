package org.processmining.plugins.epml;

import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCoordinates"> <xs:complexContent>
 *         <xs:extension base="epml:tExtensibleElements"> <xs:attribute
 *         name="xOrigin" use="required"> <xs:simpleType> <xs:restriction
 *         base="xs:string"> <xs:enumeration value="leftToRight"/>
 *         <xs:enumeration value="rightToLeft"/> </xs:restriction>
 *         </xs:simpleType> </xs:attribute> <xs:attribute name="yOrigin"
 *         use="required"> <xs:simpleType> <xs:restriction base="xs:string">
 *         <xs:enumeration value="topToBottom"/> <xs:enumeration
 *         value="bottomToTop"/> </xs:restriction> </xs:simpleType>
 *         </xs:attribute> </xs:extension> </xs:complexContent>
 *         </xs:complexType>
 */

public class EpmlCoordinates extends EpmlExtensibleElements {

	public final static String TAG = "coordinates";

	/*
	 * Attributes
	 */
	private String xOrigin;
	private String yOrigin;

	public EpmlCoordinates() {
		super(TAG);

		xOrigin = null;
		yOrigin = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "xOrigin");
		if (value != null) {
			xOrigin = value;
		}
		value = xpp.getAttributeValue(null, "yOrigin");
		if (value != null) {
			yOrigin = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (xOrigin != null) {
			s += exportAttribute("xOrigin", xOrigin);
		}
		if (yOrigin != null) {
			s += exportAttribute("yOrigin", yOrigin);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkRestriction(epml, "xOrigin", xOrigin, Arrays.asList("leftToRight", "rightToLeft"), true);
		checkRestriction(epml, "yOrigin", yOrigin, Arrays.asList("topToBottom", "bottomToTop"), true);
	}

}
