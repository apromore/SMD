package org.processmining.plugins.epml.graphics;

import java.util.Arrays;

import org.processmining.plugins.epml.Epml;
import org.processmining.plugins.epml.EpmlElement;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeFill"> <xs:attribute name="color"
 *         type="xs:string"/> <xs:attribute name="image" type="xs:anyURI"/>
 *         <xs:attribute name="gradient-color" type="xs:string"/> <xs:attribute
 *         name="gradient-rotation"> <xs:simpleType> <xs:restriction
 *         base="xs:string"> <xs:enumeration value="vertical"/> <xs:enumeration
 *         value="horizontal"/> <xs:enumeration value="diagonal"/>
 *         </xs:restriction> </xs:simpleType> </xs:attribute> </xs:complexType>
 */

public class EpmlFill extends EpmlElement {

	public final static String TAG = "fill";

	/*
	 * Attributes
	 */
	private String color;
	private String image;
	private String gradientColor;
	private String gradientRotation;

	public EpmlFill() {
		super(TAG);

		color = null;
		image = null;
		gradientColor = null;
		gradientRotation = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "color");
		if (value != null) {
			color = value;
		}
		value = xpp.getAttributeValue(null, "image");
		if (value != null) {
			image = value;
		}
		value = xpp.getAttributeValue(null, "gradient-color");
		if (value != null) {
			gradientColor = value;
		}
		value = xpp.getAttributeValue(null, "gradient-rotation");
		if (value != null) {
			gradientRotation = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (color != null) {
			s += exportAttribute("color", color);
		}
		if (image != null) {
			s += exportAttribute("image", image);
		}
		if (gradientColor != null) {
			s += exportAttribute("gradient-color", gradientColor);
		}
		if (gradientRotation != null) {
			s += exportAttribute("gradient-rotation", gradientRotation);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		checkURI(epml, "image", image, false);
		checkRestriction(epml, "gradient-rotation", gradientRotation, Arrays.asList("vertical", "horizontal",
				"diagonal"), false);
	}
}
