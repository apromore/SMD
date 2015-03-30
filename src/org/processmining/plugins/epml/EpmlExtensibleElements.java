package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="tExtensibleElements"> <xs:sequence> <xs:element
 *         name="documentation" type="xs:anyType" minOccurs="0"/> <xs:element
 *         name="toolInfo" type="xs:anyType" minOccurs="0"/> </xs:sequence>
 *         <xs:anyAttribute namespace="##other" processContents="lax"/>
 *         </xs:complexType>
 */

public class EpmlExtensibleElements extends EpmlElement {

	/*
	 * Elements
	 */
	private EpmlDocumentation documentation;
	private EpmlToolInfo toolInfo;

	public EpmlExtensibleElements(String tag) {
		super(tag);

		documentation = null;
		toolInfo = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlDocumentation.TAG)) {
			documentation = new EpmlDocumentation();
			documentation.importElement(xpp, epml);
			return true;
		}
		if (xpp.getName().equals(EpmlToolInfo.TAG)) {
			toolInfo = new EpmlToolInfo();
			toolInfo.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (documentation != null) {
			s += documentation.exportElement();
		}
		if (toolInfo != null) {
			s += toolInfo.exportElement();
		}
		return s;
	}

}
