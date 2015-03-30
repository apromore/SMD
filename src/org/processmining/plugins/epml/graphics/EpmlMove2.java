package org.processmining.plugins.epml.graphics;

import java.awt.Dimension;
import java.awt.geom.Point2D;
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
 *         <xs:complexType name="typeMove2"> <xs:attribute name="x"
 *         type="xs:decimal" use="required"/> <xs:attribute name="y"
 *         type="xs:decimal" use="required"/> </xs:complexType>
 */

public class EpmlMove2 extends EpmlElement {

	public final static String TAG = "position";

	/*
	 * Attributes
	 */
	private String x;
	private String y;

	public EpmlMove2() {
		super(TAG);

		x = null;
		y = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "x");
		if (value != null) {
			x = value;
		}
		value = xpp.getAttributeValue(null, "y");
		if (value != null) {
			y = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (x != null) {
			s += exportAttribute("x", x);
		}
		if (y != null) {
			s += exportAttribute("y", y);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkDecimal(epml, "x", x, true);
		checkDecimal(epml, "y", y, true);
	}

	public void convertToEpc(EPCGraph epc, List<Point2D> points, GraphLayoutConnection layout) {
		if ((x != null) && (y != null)) {
			try {
				Double dX = EpmlPosition.SCALE * Double.valueOf(x);
				Double dY = EpmlPosition.SCALE * Double.valueOf(y);
				points.add(new Point2D.Double(dX, dY));
			} catch (Exception ex) {
			}
		}
	}

	public void convertToEpc(EPCGraph epc, List<Point2D> points, EPCNode node, GraphLayoutConnection layout) {
		if ((x != null) && (y != null)) {
			try {
				Double epsilon = 0.01;
				Double dX = EpmlPosition.SCALE * Double.valueOf(x);
				Double dY = EpmlPosition.SCALE * Double.valueOf(y);
				Point2D posNode = layout.getPosition(node);
				if (posNode == null) {
					posNode = new Point2D.Double(10, 10);
				}
				Dimension dimNode = layout.getSize(node);
				if ((dX + epsilon < posNode.getX()) || (dY + epsilon < posNode.getY())
						|| (dX - epsilon > posNode.getX() + dimNode.width)
						|| (dY - epsilon > posNode.getY() + dimNode.height)) {
					/*
					 * Point is sufficiently far away from object. Add it.
					 */
					points.add(new Point2D.Double(dX, dY));
				}
			} catch (Exception ex) {
			}
		}
	}
}
