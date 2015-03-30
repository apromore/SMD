package org.processmining.plugins.epml.graphics;

import java.util.Arrays;

import org.processmining.plugins.epml.Epml;
import org.processmining.plugins.epml.EpmlElement;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeFont"> <xs:attribute name="family"
 *         type="xs:string"/> <xs:attribute name="style" type="xs:string"/>
 *         <xs:attribute name="weight" type="xs:string"/> <xs:attribute
 *         name="size" type="xs:positiveInteger"/> <xs:attribute
 *         name="decoration"> <xs:simpleType> <xs:restriction base="xs:string">
 *         <xs:enumeration value="underline"/> <xs:enumeration
 *         value="overline"/> <xs:enumeration value="line-through"/>
 *         </xs:restriction> </xs:simpleType> </xs:attribute> <xs:attribute
 *         name="color" type="xs:string"/> <xs:attribute name="verticalAlign">
 *         <xs:simpleType> <xs:restriction base="xs:string"> <xs:enumeration
 *         value="top"/> <xs:enumeration value="middle"/> <xs:enumeration
 *         value="bottom"/> </xs:restriction> </xs:simpleType> </xs:attribute>
 *         <xs:attribute name="horizontalAlign"> <xs:simpleType> <xs:restriction
 *         base="xs:string"> <xs:enumeration value="left"/> <xs:enumeration
 *         value="middle"/> <xs:enumeration value="right"/> </xs:restriction>
 *         </xs:simpleType> </xs:attribute> <xs:attribute name="rotation"
 *         type="xs:decimal"/> </xs:complexType>
 */

public class EpmlFont extends EpmlElement {

	public final static String TAG = "font";

	/*
	 * Attributes
	 */
	private String family;
	private String style;
	private String weight;
	private String size;
	private String decoration;
	private String color;
	private String verticalAlign;
	private String horizontalAlign;
	private String rotation;

	public EpmlFont() {
		super(TAG);

		family = null;
		style = null;
		weight = null;
		size = null;
		decoration = null;
		color = null;
		verticalAlign = null;
		horizontalAlign = null;
		rotation = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "family");
		if (value != null) {
			family = value;
		}
		value = xpp.getAttributeValue(null, "style");
		if (value != null) {
			style = value;
		}
		value = xpp.getAttributeValue(null, "weight");
		if (value != null) {
			weight = value;
		}
		value = xpp.getAttributeValue(null, "size");
		if (value != null) {
			size = value;
		}
		value = xpp.getAttributeValue(null, "decoration");
		if (value != null) {
			decoration = value;
		}
		value = xpp.getAttributeValue(null, "color");
		if (value != null) {
			color = value;
		}
		value = xpp.getAttributeValue(null, "verticalAlign");
		if (value != null) {
			verticalAlign = value;
		}
		value = xpp.getAttributeValue(null, "horizontalAlign");
		if (value != null) {
			horizontalAlign = value;
		}
		value = xpp.getAttributeValue(null, "rotation");
		if (value != null) {
			rotation = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (family != null) {
			s += exportAttribute("family", family);
		}
		if (style != null) {
			s += exportAttribute("style", style);
		}
		if (weight != null) {
			s += exportAttribute("weight", weight);
		}
		if (size != null) {
			s += exportAttribute("size", size);
		}
		if (decoration != null) {
			s += exportAttribute("decoration", decoration);
		}
		if (color != null) {
			s += exportAttribute("color", color);
		}
		if (verticalAlign != null) {
			s += exportAttribute("verticalAlign", verticalAlign);
		}
		if (horizontalAlign != null) {
			s += exportAttribute("horizontalAlign", horizontalAlign);
		}
		if (rotation != null) {
			s += exportAttribute("rotation", rotation);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkPositiveInteger(epml, "size", size, false);
		checkRestriction(epml, "decoration", decoration, Arrays.asList("underline", "overline", "line-through"), false);
		checkRestriction(epml, "verticalAlign", verticalAlign, Arrays.asList("top", "middle", "bottom"), false);
		checkRestriction(epml, "horizontalAlign", horizontalAlign, Arrays.asList("left", "middle", "right"), false);
		checkDecimal(epml, "rotation", rotation, false);
	}
}
