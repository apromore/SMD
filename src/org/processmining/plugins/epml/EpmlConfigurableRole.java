package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCRole"> <xs:choice minOccurs="0">
 *         <xs:element name="configuration"> <xs:complexType> <xs:attribute
 *         name="optionality"> <xs:simpleType> <xs:restriction base="xs:string">
 *         <xs:enumeration value="mnd"/> <xs:enumeration value="off"/>
 *         <xs:enumeration value="opt"/> </xs:restriction> </xs:simpleType>
 *         </xs:attribute> <xs:attribute name="specialization"
 *         type="xs:positiveInteger"/> </xs:complexType> </xs:element>
 *         </xs:choice> </xs:complexType>
 */

public class EpmlConfigurableRole extends EpmlElement {

	public final static String TAG = "configurableRole";

	/*
	 * Elements
	 */
	private EpmlRoleConfiguration configuration;

	public EpmlConfigurableRole() {
		super(TAG);

		configuration = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlRoleConfiguration.TAG)) {
			configuration = new EpmlRoleConfiguration();
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
