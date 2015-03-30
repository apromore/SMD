package org.processmining.plugins.epml;

import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeObject"> <xs:complexContent> <xs:extension
 *         base="epml:tEpcElement"> <xs:sequence> <xs:choice minOccurs="0">
 *         <xs:element name="configurableObject" type="epml:typeCObject"/>
 *         </xs:choice> <xs:choice minOccurs="0" maxOccurs="unbounded">
 *         <xs:element name="attribute" type="epml:typeAttribute"/> </xs:choice>
 *         </xs:sequence> <xs:attribute name="type" use="required">
 *         <xs:simpleType> <xs:restriction base="xs:string"> <xs:enumeration
 *         value="input"/> <xs:enumeration value="output"/> </xs:restriction>
 *         </xs:simpleType> </xs:attribute> <xs:attribute name="optional"
 *         type="xs:boolean" use="optional" default="false"/> <xs:attribute
 *         name="consumed" type="xs:boolean" use="optional" default="false"/>
 *         <xs:attribute name="initial" type="xs:boolean" use="optional"
 *         default="false"/> <xs:attribute name="final" type="xs:boolean"
 *         use="optional" default="false"/> </xs:extension> </xs:complexContent>
 *         </xs:complexType>
 */

public class EpmlObject extends EpmlAttributableEpcElement {

	public final static String TAG = "object";

	/*
	 * Attributes
	 */
	private String type;
	private String optional;
	private String consumed;
	private String initial;
	private String isFinal;

	/*
	 * Elements
	 */
	private EpmlConfigurableObject configurableObject;

	public EpmlObject() {
		super(TAG);

		type = null;
		optional = null;
		consumed = null;
		initial = null;
		isFinal = null;

		configurableObject = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "type");
		if (value != null) {
			type = value;
		}
		value = xpp.getAttributeValue(null, "optional");
		if (value != null) {
			optional = value;
		}
		value = xpp.getAttributeValue(null, "consumed");
		if (value != null) {
			consumed = value;
		}
		value = xpp.getAttributeValue(null, "initial");
		if (value != null) {
			initial = value;
		}
		value = xpp.getAttributeValue(null, "isFinal");
		if (value != null) {
			isFinal = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (type != null) {
			s += exportAttribute("type", type);
		}
		if (optional != null) {
			s += exportAttribute("optional", optional);
		}
		if (consumed != null) {
			s += exportAttribute("consumed", consumed);
		}
		if (initial != null) {
			s += exportAttribute("initial", initial);
		}
		if (isFinal != null) {
			s += exportAttribute("final", isFinal);
		}
		return s;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlConfigurableObject.TAG)) {
			configurableObject = new EpmlConfigurableObject();
			configurableObject.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (configurableObject != null) {
			s += configurableObject.exportElement();
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkRestriction(epml, "type", type, Arrays.asList("input", "output"), true);
		checkBoolean(epml, "optional", optional, false);
		checkBoolean(epml, "consumed", consumed, false);
		checkBoolean(epml, "initial", initial, false);
		checkBoolean(epml, "final", isFinal, false);
	}
}
