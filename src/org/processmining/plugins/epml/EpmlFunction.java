package org.processmining.plugins.epml;

import java.util.Map;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.processmining.models.graphbased.directed.epc.elements.Function;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeFunction"> <xs:complexContent>
 *         <xs:extension base="epml:tEpcElement"> <xs:sequence> <xs:choice
 *         minOccurs="0"> <xs:element name="toProcess" type="epml:typeToProcess"
 *         minOccurs="0"/> </xs:choice> <xs:choice minOccurs="0"> <xs:element
 *         name="configurableFunction" type="epml:typeCFunction" minOccurs="0"/>
 *         </xs:choice> <xs:choice minOccurs="0" maxOccurs="unbounded">
 *         <xs:element name="attribute" type="epml:typeAttribute"
 *         minOccurs="0"/> </xs:choice> </xs:sequence> </xs:extension>
 *         </xs:complexContent> </xs:complexType>
 */

public class EpmlFunction extends EpmlAttributableEpcElement {

	public final static String TAG = "function";

	/*
	 * Elements
	 */
	private EpmlToProcess toProcess;
	private EpmlConfigurableFunction configurableFunction;

	public EpmlFunction() {
		super(TAG);

		toProcess = null;
		configurableFunction = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlToProcess.TAG)) {
			toProcess = new EpmlToProcess();
			toProcess.importElement(xpp, epml);
			return true;
		}
		if (xpp.getName().equals(EpmlConfigurableFunction.TAG)) {
			configurableFunction = new EpmlConfigurableFunction();
			configurableFunction.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (toProcess != null) {
			s += toProcess.exportElement();
		}
		if (configurableFunction != null) {
			s += configurableFunction.exportElement();
		}
		return s;
	}

	public void convertToCEpc(ConfigurableEPC cEpc, Map<String, EPCNode> map, GraphLayoutConnection layout) {
		Function function = cEpc.addFunction(getName(), configurableFunction != null);
		if (graphics != null) {
			graphics.convertToEpc(cEpc, function, layout);
		}
		map.put(getId(), function);
	}
}
