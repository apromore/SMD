package org.processmining.plugins.epml.graphics;

import java.util.Arrays;

import org.processmining.plugins.epml.Epml;
import org.processmining.plugins.epml.EpmlElement;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeLine"> <xs:attribute name="shape">
 *         <xs:simpleType> <xs:restriction base="xs:string"> <xs:enumeration
 *         value="line"/> <xs:enumeration value="curve"/> </xs:restriction>
 *         </xs:simpleType> </xs:attribute> <xs:attribute name="color"
 *         type="xs:string"/> <xs:attribute name="width" type="xs:decimal"/>
 *         <xs:attribute name="style"> <xs:simpleType> <xs:restriction
 *         base="xs:string"> <xs:enumeration value="solid"/> <xs:enumeration
 *         value="dash"/> <xs:enumeration value="dot"/> </xs:restriction>
 *         </xs:simpleType> </xs:attribute> </xs:complexType>
 */

public class EpmlLine extends EpmlElement {

	public final static String TAG = "line";

	/*
	 * Attributes
	 */
	private String shape;
	private String color;
	private String width;
	private String style;

	public EpmlLine() {
		super(TAG);

		shape = null;
		color = null;
		width = null;
		style = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "shape");
		if (value != null) {
			shape = value;
		}
		value = xpp.getAttributeValue(null, "color");
		if (value != null) {
			color = value;
		}
		value = xpp.getAttributeValue(null, "width");
		if (value != null) {
			width = value;
		}
		value = xpp.getAttributeValue(null, "style");
		if (value != null) {
			style = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (shape != null) {
			s += exportAttribute("shape", shape);
		}
		if (color != null) {
			s += exportAttribute("color", color);
		}
		if (width != null) {
			s += exportAttribute("width", width);
		}
		if (style != null) {
			s += exportAttribute("style", style);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkRestriction(epml, "shape", shape, Arrays.asList("line", "curve"), false);
		checkDecimal(epml, "width", width, false);
		checkRestriction(epml, "style", style, Arrays.asList("solid", "dash", "dot"), false);
	}
}
