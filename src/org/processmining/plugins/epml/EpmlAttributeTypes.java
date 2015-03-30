package org.processmining.plugins.epml;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeAttrTypes"> <xs:sequence> <xs:sequence
 *         minOccurs="0" maxOccurs="unbounded"> <xs:element name="attributeType"
 *         type="epml:typeAttrType"/> </xs:sequence> </xs:sequence>
 *         </xs:complexType>
 */

public class EpmlAttributeTypes extends EpmlElement {

	public final static String TAG = "attributeTypes";

	/*
	 * Elements
	 */
	private final List<EpmlAttributeType> attributeTypeList;

	public EpmlAttributeTypes() {
		super(TAG);

		attributeTypeList = new ArrayList<EpmlAttributeType>();
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlAttributeType.TAG)) {
			EpmlAttributeType attributeType = new EpmlAttributeType();
			attributeType.importElement(xpp, epml);
			attributeTypeList.add(attributeType);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		for (EpmlAttributeType attributeType : attributeTypeList) {
			s += attributeType.exportElement();
		}
		return s;
	}
}
