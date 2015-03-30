package org.processmining.plugins.epml;

import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeRANGE"> <xs:complexContent> <xs:extension
 *         base="epml:tEpcElement"> <xs:sequence> <xs:choice minOccurs="0">
 *         <xs:element name="configurableConnector" type="epml:typeCRange"/>
 *         </xs:choice> <xs:choice minOccurs="0" maxOccurs="unbounded">
 *         <xs:element name="attribute" type="epml:typeAttribute"/> </xs:choice>
 *         </xs:sequence> <xs:attribute name="lowerBound" use="required">
 *         <xs:simpleType> <xs:union memberTypes="xs:positiveInteger">
 *         <xs:simpleType> <xs:restriction base="xs:string"> <xs:enumeration
 *         value="k"/> </xs:restriction> </xs:simpleType> </xs:union>
 *         </xs:simpleType> </xs:attribute> <xs:attribute name="upperBound"
 *         use="required"> <xs:simpleType> <xs:union
 *         memberTypes="xs:positiveInteger"> <xs:simpleType> <xs:restriction
 *         base="xs:string"> <xs:enumeration value="k"/> </xs:restriction>
 *         </xs:simpleType> </xs:union> </xs:simpleType> </xs:attribute>
 *         <xs:attribute name="type" use="optional"> <xs:simpleType>
 *         <xs:restriction base="xs:string"> <xs:enumeration value="role"/>
 *         <xs:enumeration value="input"/> <xs:enumeration value="output"/>
 *         </xs:restriction> </xs:simpleType> </xs:attribute> <xs:attribute
 *         name="optional" type="xs:boolean" use="optional" default="false"/>
 *         </xs:extension> </xs:complexContent> </xs:complexType>
 */

public class EpmlRange extends EpmlAttributableEpcElement {

	public final static String TAG = "range";

	/*
	 * Attributes
	 */
	private String lowerBound;
	private String upperBound;
	private String type;
	private String optional;

	/*
	 * Elements
	 */
	private EpmlConfigurableRange configurableConnector;

	public EpmlRange() {
		super(TAG);

		lowerBound = null;
		upperBound = null;
		type = null;
		optional = null;

		configurableConnector = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "lowerBound");
		if (value != null) {
			lowerBound = value;
		}
		value = xpp.getAttributeValue(null, "upperBound");
		if (value != null) {
			upperBound = value;
		}
		value = xpp.getAttributeValue(null, "type");
		if (value != null) {
			type = value;
		}
		value = xpp.getAttributeValue(null, "optional");
		if (value != null) {
			optional = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (lowerBound != null) {
			s += exportAttribute("lowerBound", lowerBound);
		}
		if (upperBound != null) {
			s += exportAttribute("upperBound", upperBound);
		}
		if (type != null) {
			s += exportAttribute("type", type);
		}
		if (optional != null) {
			s += exportAttribute("optional", optional);
		}
		return s;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlConfigurableRange.TAG)) {
			configurableConnector = new EpmlConfigurableRange();
			configurableConnector.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (configurableConnector != null) {
			s += configurableConnector.exportElement();
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkPositiveIntegerOrK(epml, "lowerBound", lowerBound, true);
		checkPositiveIntegerOrK(epml, "upperBound", upperBound, true);
		checkRestriction(epml, "type", type, Arrays.asList("role", "input", "output"), false);
		checkBoolean(epml, "optional", optional, false);
	}
}