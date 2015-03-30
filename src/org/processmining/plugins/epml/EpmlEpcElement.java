package org.processmining.plugins.epml;

import org.processmining.plugins.epml.graphics.EpmlGraphics;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="tEpcElement"> <xs:complexContent> <xs:extension
 *         base="epml:tExtensibleElements"> <xs:sequence> <xs:element
 *         name="name" type="xs:string" minOccurs="0"/> <xs:element
 *         name="description" type="xs:string" minOccurs="0"/> <xs:choice
 *         minOccurs="0"> <xs:element name="graphics" type="epml:typeGraphics"/>
 *         </xs:choice> <xs:any namespace="##other" processContents="lax"
 *         minOccurs="0" maxOccurs="unbounded"/> </xs:sequence> <xs:attribute
 *         name="id" type="xs:positiveInteger" use="required"/> <xs:attribute
 *         name="defRef" type="xs:positiveInteger" use="optional"/>
 *         </xs:extension> </xs:complexContent> </xs:complexType>
 */

public class EpmlEpcElement extends EpmlExtensibleElements {

	/*
	 * Attributes
	 */
	private String id;
	private String defRef;

	/*
	 * Elements
	 */
	private EpmlName name;
	private EpmlDescription description;
	protected EpmlGraphics graphics;

	public EpmlEpcElement(String tag) {
		super(tag);

		id = null;
		defRef = null;

		name = null;
		description = null;
		graphics = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "id");
		if (value != null) {
			id = value;
		}
		value = xpp.getAttributeValue(null, "defRef");
		if (value != null) {
			defRef = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (id != null) {
			s += exportAttribute("id", id);
		}
		if (defRef != null) {
			s += exportAttribute("defRef", defRef);
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
		if (xpp.getName().equals(EpmlGraphics.TAG)) {
			graphics = new EpmlGraphics();
			graphics.importElement(xpp, epml);
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
		if (graphics != null) {
			s += graphics.exportElement();
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkPositiveInteger(epml, "id", id, true);
		checkPositiveInteger(epml, "defRef", defRef, false);
	}

	public String getName() {
		return (name != null ? name.get() : "");
	}

	public String getId() {
		return id;
	}
}
