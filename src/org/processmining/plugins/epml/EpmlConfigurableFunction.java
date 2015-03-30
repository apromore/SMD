package org.processmining.plugins.epml;

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

public class EpmlConfigurableFunction extends EpmlElement {

	public final static String TAG = "configurableFunction";

	/*
	 * Elements
	 */
	private EpmlFunctionConfiguration configuration;

	public EpmlConfigurableFunction() {
		super(TAG);

		configuration = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlFunctionConfiguration.TAG)) {
			configuration = new EpmlFunctionConfiguration();
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
