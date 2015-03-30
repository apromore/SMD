package org.processmining.plugins.epml.graphics;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.epc.EPCGraph;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.processmining.plugins.epml.Epml;
import org.processmining.plugins.epml.EpmlElement;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typePosition"> <xs:attribute name="x"
 *         type="xs:decimal"/> <xs:attribute name="y" type="xs:decimal"/>
 *         <xs:attribute name="width" type="xs:decimal"/> <xs:attribute
 *         name="height" type="xs:decimal"/> </xs:complexType>
 */

public class EpmlPosition extends EpmlElement {

	public final static String TAG = "position";

	/*
	 * Attributes
	 */
	private String x;
	private String y;
	private String width;
	private String height;

	public static final double SCALE = 2.0;

	public EpmlPosition() {
		super(TAG);

		x = null;
		y = null;
		width = null;
		height = null;
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
		value = xpp.getAttributeValue(null, "width");
		if (value != null) {
			width = value;
		}
		value = xpp.getAttributeValue(null, "height");
		if (value != null) {
			height = value;
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
		if (width != null) {
			s += exportAttribute("width", width);
		}
		if (height != null) {
			s += exportAttribute("height", height);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkDecimal(epml, "x", x, false);
		checkDecimal(epml, "y", y, false);
		checkDecimal(epml, "width", width, false);
		checkDecimal(epml, "height", height, false);
	}

	public void convertToEpc(EPCGraph epc, EPCNode node, GraphLayoutConnection layout) {
		if ((x != null) && (y != null)) {
			try {
				Double dX = SCALE * Double.valueOf(x);
				Double dY = SCALE * Double.valueOf(y);
				layout.setPosition(node, new Point2D.Double(dX, dY));
			} catch (Exception ex) {
			}
		}
		if ((width != null) && (height != null)) {
			try {
				Double dWidth = SCALE * Double.valueOf(width);
				Double dHeight = SCALE * Double.valueOf(height);
				Dimension dim = new Dimension();
				dim.setSize(dWidth, dHeight);
				layout.setSize(node, dim);
			} catch (Exception ex) {
			}
		}
	}
}
