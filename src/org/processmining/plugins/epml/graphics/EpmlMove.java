package org.processmining.plugins.epml.graphics;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.epc.EPCGraph;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.processmining.plugins.epml.Epml;
import org.processmining.plugins.epml.EpmlElement;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeMove"> <xs:sequence> <xs:element
 *         name="position" type="epml:typeMove2" minOccurs="0"
 *         maxOccurs="unbounded"/> <xs:element name="line" type="epml:typeLine"
 *         minOccurs="0"/> <xs:element name="font" type="epml:typeFont"
 *         minOccurs="0"/> </xs:sequence> </xs:complexType>
 */

public class EpmlMove extends EpmlElement {

	public final static String TAG = "graphics";

	/*
	 * Elements
	 */
	private final List<EpmlMove2> positionList;
	private EpmlLine line;
	private EpmlFont font;

	public EpmlMove() {
		super(TAG);

		positionList = new ArrayList<EpmlMove2>();
		line = null;
		font = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlMove2.TAG)) {
			EpmlMove2 position = new EpmlMove2();
			position.importElement(xpp, epml);
			positionList.add(position);
			return true;
		}
		if (xpp.getName().equals(EpmlLine.TAG)) {
			line = new EpmlLine();
			line.importElement(xpp, epml);
			return true;
		}
		if (xpp.getName().equals(EpmlFont.TAG)) {
			font = new EpmlFont();
			font.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		for (EpmlMove2 position : positionList) {
			s += position.exportElement();
		}
		if (line != null) {
			s += line.exportElement();
		}
		if (font != null) {
			s += font.exportElement();
		}
		return s;
	}

	public void convertToEpc(EPCGraph epc, List<Point2D> points, GraphLayoutConnection layout) {
		for (EpmlMove2 position : positionList) {
			position.convertToEpc(epc, points, layout);
		}
	}

	public void convertToEpc(EPCGraph epc, List<Point2D> points, EPCNode source, EPCNode target,
			GraphLayoutConnection layout) {
		int last = positionList.size() - 1;
		int i = 0;
		for (EpmlMove2 position : positionList) {
			if (i == 0) {
				/*
				 * Only add first position if it is sufficiently far away from
				 * source.
				 */
				position.convertToEpc(epc, points, source, layout);
			} else if (i == last) {
				/*
				 * Only add last position if it is sufficiently far away from
				 * target.
				 */
				position.convertToEpc(epc, points, target, layout);
			} else {
				/*
				 * Add any intermediate point.
				 */
				position.convertToEpc(epc, points, layout);
			}
			i++;
		}
	}

}
