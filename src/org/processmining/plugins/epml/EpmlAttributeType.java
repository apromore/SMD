package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeAttrType"> <xs:complexContent>
 *         <xs:extension base="epml:tExtensibleElements"> <xs:sequence>
 *         <xs:element name="description" type="xs:string" minOccurs="0"/>
 *         </xs:sequence> <xs:attribute name="typeId" type="xs:string"/>
 *         </xs:extension> </xs:complexContent> </xs:complexType>
 */

public class EpmlAttributeType extends EpmlExtensibleElements {

	public final static String TAG = "attributeType";

	/*
	 * Attributes
	 */
	private String typeId;

	/*
	 * Elements
	 */
	private EpmlDescription description;

	public EpmlAttributeType() {
		super(TAG);

		typeId = null;

		description = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "typeId");
		if (value != null) {
			typeId = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (typeId != null) {
			s += exportAttribute("typeId", typeId);
		}
		return s;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlDescription.TAG)) {
			description = new EpmlDescription();
			description.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (description != null) {
			s += description.exportElement();
		}
		return s;
	}
}
