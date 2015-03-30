package org.processmining.plugins.epml;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeSpecialization"> <xs:complexContent>
 *         <xs:extension base="epml:tExtensibleElements"> <xs:sequence>
 *         <xs:element name="name" type="xs:string" minOccurs="0"/> <xs:element
 *         name="description" type="xs:string" minOccurs="0"/> <xs:element
 *         name="flow" type="epml:typeFlow" minOccurs="0"/> <xs:choice
 *         minOccurs="0" maxOccurs="unbounded"> <xs:element name="attribute"
 *         type="epml:typeAttribute"/> </xs:choice> </xs:sequence> <xs:attribute
 *         name="specId" type="xs:positiveInteger" use="required"/>
 *         </xs:extension> </xs:complexContent> </xs:complexType>
 */

public class EpmlSpecialization extends EpmlExtensibleElements {

	public final static String TAG = "specialization";

	/*
	 * Attributes
	 */
	private String specId;

	/*
	 * Elements
	 */
	private EpmlName name;
	private EpmlDescription description;
	private EpmlFlow flow;
	private final List<EpmlAttribute> attributeList;

	public EpmlSpecialization() {
		super(TAG);

		specId = null;

		name = null;
		description = null;
		flow = null;
		attributeList = new ArrayList<EpmlAttribute>();
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "specId");
		if (value != null) {
			specId = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (specId != null) {
			s += exportAttribute("specId", specId);
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
		if (xpp.getName().equals(EpmlFlow.TAG)) {
			flow = new EpmlFlow();
			flow.importElement(xpp, epml);
			return true;
		}
		if (xpp.getName().equals(EpmlAttribute.TAG)) {
			EpmlAttribute attribute = new EpmlAttribute();
			attribute.importElement(xpp, epml);
			attributeList.add(attribute);
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
		if (flow != null) {
			s += flow.exportElement();
		}
		for (EpmlAttribute attribute : attributeList) {
			s += attribute.exportElement();
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkPositiveInteger(epml, "specId", specId, true);
	}
}
