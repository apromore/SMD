package org.processmining.plugins.epml.graphics;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.epc.EPCGraph;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.processmining.plugins.epml.Epml;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeGraphics"> <xs:sequence> <xs:element
 *         name="position" type="epml:typePosition" minOccurs="0"/> <xs:element
 *         name="fill" type="epml:typeFill" minOccurs="0"/> <xs:element
 *         name="line" type="epml:typeLine" minOccurs="0"/> <xs:element
 *         name="font" type="epml:typeFont" minOccurs="0"/> </xs:sequence>
 *         </xs:complexType>
 */
public class EpmlGraphics extends EpmlGraphicsDefault {

	public final static String TAG = "graphics";

	/*
	 * Elements
	 * 
	 * fill, line, and font are inherited from EpmlGraphicsDefault.
	 */
	private EpmlPosition position;

	public EpmlGraphics() {
		super(TAG);

		position = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlPosition.TAG)) {
			position = new EpmlPosition();
			position.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (position != null) {
			s += position.exportElement();
		}
		return s;
	}

	public void convertToEpc(EPCGraph epc, EPCNode node, GraphLayoutConnection layout) {
		if (position != null) {
			position.convertToEpc(epc, node, layout);
		}
	}
}
