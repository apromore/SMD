package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCObject"> <xs:choice minOccurs="0">
 *         <xs:element name="configuration"> <xs:complexType> <xs:attribute
 *         name="optionality"> <xs:simpleType> <xs:restriction base="xs:string">
 *         <xs:enumeration value="mnd"/> <xs:enumeration value="off"/>
 *         <xs:enumeration value="opt"/> </xs:restriction> </xs:simpleType>
 *         </xs:attribute> <xs:attribute name="specialization"
 *         type="xs:positiveInteger"/> <xs:attribute name="usage">
 *         <xs:simpleType> <xs:restriction base="xs:string"> <xs:enumeration
 *         value="use"/> <xs:enumeration value="cns"/> </xs:restriction>
 *         </xs:simpleType> </xs:attribute> </xs:complexType> </xs:element>
 *         </xs:choice> </xs:complexType>
 */

public class EpmlConfigurableObject extends EpmlElement {

	public final static String TAG = "configurableObject";

	/*
	 * Elements
	 */
	private EpmlObjectConfiguration configuration;

	public EpmlConfigurableObject() {
		super(TAG);

		configuration = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlObjectConfiguration.TAG)) {
			configuration = new EpmlObjectConfiguration();
			configuration.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (configuration != null) {
			s += configuration.exportElement();
		}
		return s;
	}
}
