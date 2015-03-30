package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCOR"> <xs:choice minOccurs="0"> <xs:element
 *         name="configuration"> <xs:complexType> <xs:attribute name="value">
 *         <xs:simpleType> <xs:restriction base="xs:string"> <xs:enumeration
 *         value="or"/> <xs:enumeration value="and"/> <xs:enumeration
 *         value="xor"/> <xs:enumeration value="seq"/> </xs:restriction>
 *         </xs:simpleType> </xs:attribute> <xs:attribute name="goto"
 *         type="xs:positiveInteger" use="optional"/> </xs:complexType>
 *         </xs:element> </xs:choice> </xs:complexType>
 */

public class EpmlConfigurableOr extends EpmlElement {

	public final static String TAG = "configurableConnector";

	/*
	 * Elements
	 */
	private EpmlOrConfiguration configuration;

	public EpmlConfigurableOr() {
		super(TAG);

		configuration = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlOrConfiguration.TAG)) {
			configuration = new EpmlOrConfiguration();
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
