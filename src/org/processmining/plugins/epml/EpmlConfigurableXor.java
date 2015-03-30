package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCXOR"> <xs:choice minOccurs="0">
 *         <xs:element name="configuration"> <xs:complexType> <xs:attribute
 *         name="value"> <xs:simpleType> <xs:restriction base="xs:string">
 *         <xs:enumeration value="xor"/> <xs:enumeration value="seq"/>
 *         </xs:restriction> </xs:simpleType> </xs:attribute> <xs:attribute
 *         name="goto" type="xs:positiveInteger" use="optional"/>
 *         </xs:complexType> </xs:element> </xs:choice> </xs:complexType>
 */

public class EpmlConfigurableXor extends EpmlElement {

	public final static String TAG = "configurableConnector";

	/*
	 * Elements
	 */
	private EpmlXorConfiguration configuration;

	public EpmlConfigurableXor() {
		super(TAG);

		configuration = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlXorConfiguration.TAG)) {
			configuration = new EpmlXorConfiguration();
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
