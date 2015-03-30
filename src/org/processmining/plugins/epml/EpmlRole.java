package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeRole"> <xs:complexContent> <xs:extension
 *         base="epml:tEpcElement"> <xs:sequence> <xs:choice minOccurs="0">
 *         <xs:element name="configurableRole" type="epml:typeCRole"/>
 *         </xs:choice> <xs:choice minOccurs="0" maxOccurs="unbounded">
 *         <xs:element name="attribute" type="epml:typeAttribute"/> </xs:choice>
 *         </xs:sequence> <xs:attribute name="optional" type="xs:boolean"
 *         use="optional" default="false"/> </xs:extension> </xs:complexContent>
 *         </xs:complexType>
 */
public class EpmlRole extends EpmlAttributableEpcElement {

	public final static String TAG = "role";

	/*
	 * Attributes
	 */
	private String optional;

	/*
	 * Elements
	 */
	private EpmlConfigurableRole configurableRole;

	public EpmlRole() {
		super(TAG);

		optional = null;

		configurableRole = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "optional");
		if (value != null) {
			optional = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (optional != null) {
			s += exportAttribute("optional", optional);
		}
		return s;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlConfigurableRole.TAG)) {
			configurableRole = new EpmlConfigurableRole();
			configurableRole.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (configurableRole != null) {
			s += configurableRole.exportElement();
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkBoolean(epml, "optional", optional, false);
	}
}
