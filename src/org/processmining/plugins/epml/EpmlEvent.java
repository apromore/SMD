package org.processmining.plugins.epml;

import java.util.Map;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.epc.EPCGraph;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.processmining.models.graphbased.directed.epc.elements.Event;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeEvent"> <xs:complexContent> <xs:extension
 *         base="epml:tEpcElement"> <xs:sequence> <xs:choice minOccurs="0"
 *         maxOccurs="unbounded"> <xs:element name="attribute"
 *         type="epml:typeAttribute"/> </xs:choice> </xs:sequence>
 *         </xs:extension> </xs:complexContent> </xs:complexType>
 */
public class EpmlEvent extends EpmlAttributableEpcElement {

	public final static String TAG = "event";

	public EpmlEvent() {
		super(TAG);
	}

	public void convertToEpc(EPCGraph epc, Map<String, EPCNode> map, GraphLayoutConnection layout) {
		Event event = epc.addEvent(getName());
		if (graphics != null) {
			graphics.convertToEpc(epc, event, layout);
		}
		map.put(getId(), event);
	}
}
