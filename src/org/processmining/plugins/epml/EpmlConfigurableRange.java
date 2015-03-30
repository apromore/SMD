package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCRange"> <xs:choice minOccurs="0">
 *         <xs:element name="configuration"> <xs:complexType> <xs:attribute
 *         name="optionality"> <xs:simpleType> <xs:restriction base="xs:string">
 *         <xs:enumeration value="mnd"/> <xs:enumeration value="off"/>
 *         <xs:enumeration value="opt"/> </xs:restriction> </xs:simpleType>
 *         </xs:attribute> <xs:attribute name="range"> <xs:simpleType>
 *         <xs:restriction base="xs:string"> <xs:enumeration value="node"/>
 *         <xs:enumeration value="reducedRange"/> </xs:restriction>
 *         </xs:simpleType> </xs:attribute> <xs:attribute name="goto"
 *         type="xs:positiveInteger" use="optional"/> <xs:attribute
 *         name="increment" use="optional"> <xs:simpleType> <xs:union
 *         memberTypes="xs:nonNegativeInteger"> <xs:simpleType> <xs:restriction
 *         base="xs:string"> <xs:enumeration value="k"/> </xs:restriction>
 *         </xs:simpleType> </xs:union> </xs:simpleType> </xs:attribute>
 *         <xs:attribute name="decrement" use="optional"> <xs:simpleType>
 *         <xs:union memberTypes="xs:nonNegativeInteger"> <xs:simpleType>
 *         <xs:restriction base="xs:string"> <xs:enumeration value="k"/>
 *         </xs:restriction> </xs:simpleType> </xs:union> </xs:simpleType>
 *         </xs:attribute> </xs:complexType> </xs:element> </xs:choice>
 *         </xs:complexType>
 */

public class EpmlConfigurableRange extends EpmlElement {

	public final static String TAG = "configurableConnector";

	/*
	 * Elements
	 */
	private EpmlRangeConfiguration configuration;

	public EpmlConfigurableRange() {
		super(TAG);

		configuration = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlRangeConfiguration.TAG)) {
			configuration = new EpmlRangeConfiguration();
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