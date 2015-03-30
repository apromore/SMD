package org.processmining.plugins.epml;

import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeDefinition"> <xs:complexContent>
 *         <xs:extension base="epml:tExtensibleElements"> <xs:sequence>
 *         <xs:element name="name" type="xs:string" minOccurs="0"/> <xs:element
 *         name="description" type="xs:string" minOccurs="0"/> </xs:sequence>
 *         <xs:attribute name="defId" type="xs:positiveInteger" use="required"/>
 *         <xs:attribute name="type"> <xs:simpleType> <xs:restriction
 *         base="xs:string"> <xs:enumeration value="event"/> <xs:enumeration
 *         value="function"/> <xs:enumeration value="role"/> <xs:enumeration
 *         value="object"/> <xs:enumeration value="connector"/> <xs:enumeration
 *         value="any"/> </xs:restriction> </xs:simpleType> </xs:attribute>
 *         </xs:extension> </xs:complexContent> </xs:complexType>
 */

public class EpmlDefinition extends EpmlExtensibleElements {

	public final static String TAG = "definition";

	/*
	 * Attributes
	 */
	private String defId;
	private String type;

	/*
	 * Elements
	 */
	private EpmlName name;
	private EpmlDescription description;

	public EpmlDefinition() {
		super(TAG);

		defId = null;
		type = null;

		name = null;
		description = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "defId");
		if (value != null) {
			defId = value;
		}
		value = xpp.getAttributeValue(null, "type");
		if (value != null) {
			type = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (defId != null) {
			s += exportAttribute("defId", defId);
		}
		if (type != null) {
			s += exportAttribute("type", type);
		}
		return s;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlName.TAG)) {
			name = new EpmlName();
			name.importElement(xpp, epml);
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
		if (name != null) {
			s += name.exportElement();
		}
		if (description != null) {
			s += description.exportElement();
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkPositiveInteger(epml, "defId", defId, true);
		checkRestriction(epml, "type", type, Arrays.asList("event", "function", "role", "object", "connector", "any"),
				false);
	}
}
