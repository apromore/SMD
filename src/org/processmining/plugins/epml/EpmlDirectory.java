package org.processmining.plugins.epml;

import java.util.ArrayList;
import java.util.List;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeDirectory"> <xs:complexContent>
 *         <xs:extension base="epml:tExtensibleElements"> <xs:sequence>
 *         <xs:choice minOccurs="0" maxOccurs="unbounded"> <xs:element
 *         name="epc" type="epml:typeEPC"> <xs:unique name="id"> <xs:selector
 *         xpath="./*"/> <xs:field xpath="@id"/> </xs:unique> <xs:keyref
 *         name="source" refer="id"> <xs:selector xpath="./arc/*"/> <xs:field
 *         xpath="@source"/> </xs:keyref> <xs:keyref name="target" refer="id">
 *         <xs:selector xpath="./arc/*"/> <xs:field xpath="@target"/>
 *         </xs:keyref> <xs:keyref name="goto" refer="id"> <xs:selector
 *         xpath=".//configurableConnector/configuration"/> <xs:field
 *         xpath="@goto"/> </xs:keyref> </xs:element> <xs:element
 *         name="directory" type="epml:typeDirectory"/> </xs:choice>
 *         </xs:sequence> <xs:attribute name="name" type="xs:string"/>
 *         </xs:extension> </xs:complexContent> </xs:complexType>
 */

public class EpmlDirectory extends EpmlExtensibleElements {

	public final static String TAG = "directory";

	/*
	 * Attributes
	 */
	private String name;

	/*
	 * Elements
	 */
	private final List<EpmlEpc> epcList;
	private final List<EpmlDirectory> directoryList;

	public EpmlDirectory() {
		super(TAG);

		name = null;

		epcList = new ArrayList<EpmlEpc>();
		directoryList = new ArrayList<EpmlDirectory>();
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "name");
		if (value != null) {
			name = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (name != null) {
			s += exportAttribute("name", name);
		}
		return s;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlEpc.TAG)) {
			EpmlEpc epc = new EpmlEpc();
			epc.importElement(xpp, epml);
			epcList.add(epc);
			return true;
		}
		if (xpp.getName().equals(EpmlDirectory.TAG)) {
			EpmlDirectory directory = new EpmlDirectory();
			directory.importElement(xpp, epml);
			directoryList.add(directory);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		for (EpmlEpc epc : epcList) {
			s += epc.exportElement();
		}
		for (EpmlDirectory directory : directoryList) {
			s += directory.exportElement();
		}
		return s;
	}

	public void convertToCEpc(ConfigurableEPC cEpc, GraphLayoutConnection layout) {
		if (!epcList.isEmpty()) {
			epcList.get(0).convertToCEpc(cEpc, layout);
		}
	}
}
