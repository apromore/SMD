package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCReq"> <xs:sequence> <xs:element
 *         name="expression" type="xs:string"/> </xs:sequence> <xs:attribute
 *         name="idRefs"> <xs:simpleType> <xs:list
 *         itemType="xs:positiveInteger"/> </xs:simpleType> </xs:attribute>
 *         </xs:complexType>
 */

public class EpmlConfigurationRequirement extends EpmlElement {

	public final static String TAG = "configurationRequirement";

	/*
	 * Attributes
	 */
	private String idRefs;

	/*
	 * Elements
	 */
	private EpmlExpression expression;

	public EpmlConfigurationRequirement() {
		super(TAG);

		idRefs = null;

		expression = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "idRefs");
		if (value != null) {
			idRefs = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (idRefs != null) {
			s += exportAttribute("idRefs", idRefs);
		}
		return s;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlExpression.TAG)) {
			expression = new EpmlExpression();
			expression.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (expression != null) {
			s += expression.exportElement();
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkPositiveIntegers(epml, "idRefs", idRefs);
	}
}
