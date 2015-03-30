package org.processmining.plugins.epml;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeDefinitions"> <xs:sequence> <xs:choice
 *         minOccurs="0" maxOccurs="unbounded"> <xs:element name="definition"
 *         type="epml:typeDefinition"/> <xs:element name="specialization"
 *         type="epml:typeSpecialization"/> </xs:choice> </xs:sequence>
 *         </xs:complexType>
 */

public class EpmlDefinitions extends EpmlElement {

	public final static String TAG = "definitions";

	/*
	 * Elements
	 * 
	 * Note that this implementation assumes that the order between definitions
	 * and specializations is not relevant.
	 */
	private final List<EpmlDefinition> definitionList;
	private final List<EpmlSpecialization> specializationList;

	public EpmlDefinitions() {
		super(TAG);

		definitionList = new ArrayList<EpmlDefinition>();
		specializationList = new ArrayList<EpmlSpecialization>();
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlDefinition.TAG)) {
			EpmlDefinition definition = new EpmlDefinition();
			definition.importElement(xpp, epml);
			definitionList.add(definition);
			return true;
		}
		if (xpp.getName().equals(EpmlSpecialization.TAG)) {
			EpmlSpecialization specialization = new EpmlSpecialization();
			specialization.importElement(xpp, epml);
			specializationList.add(specialization);
			return true;
		}
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		for (EpmlDefinition definition : definitionList) {
			s += definition.exportElement();
		}
		for (EpmlSpecialization specialization : specializationList) {
			s += specialization.exportElement();
		}
		return s;
	}

}
