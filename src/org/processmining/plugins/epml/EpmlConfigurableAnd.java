package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCAnd"> <xs:choice minOccurs="0">
 *         <xs:element name="configuration"> <xs:complexType> <xs:attribute
 *         name="value" type="xs:string" fixed="and"/> </xs:complexType>
 *         </xs:element> </xs:choice> </xs:complexType>
 */

public class EpmlConfigurableAnd extends EpmlElement {

	public final static String TAG = "configurableConnector";

	/*
	 * Elements
	 */
	private EpmlAndConfiguration configuration;

	public EpmlConfigurableAnd() {
		super(TAG);

		configuration = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlAndConfiguration.TAG)) {
			configuration = new EpmlAndConfiguration();
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
