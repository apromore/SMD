package org.processmining.plugins.epml.graphics;

import org.processmining.plugins.epml.Epml;
import org.processmining.plugins.epml.EpmlElement;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeGraphicsDefault"> <xs:sequence> <xs:element
 *         name="fill" type="epml:typeFill" minOccurs="0"/> <xs:element
 *         name="line" type="epml:typeLine" minOccurs="0"/> <xs:element
 *         name="font" type="epml:typeFont" minOccurs="0"/> </xs:sequence>
 *         </xs:complexType>
 */

public class EpmlGraphicsDefault extends EpmlElement {

	public final static String TAG = "graphicsDefault";

	/*
	 * Elements
	 */
	private EpmlFill fill;
	private EpmlLine line;
	private EpmlFont font;

	public EpmlGraphicsDefault() {
		super(TAG);

		fill = null;
		line = null;
		font = null;
	}

	public EpmlGraphicsDefault(String tag) {
		super(tag);

		fill = null;
		line = null;
		font = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlFill.TAG)) {
			fill = new EpmlFill();
			fill.importElement(xpp, epml);
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
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (fill != null) {
			s += fill.exportElement();
		}
		if (line != null) {
			s += line.exportElement();
		}
		if (font != null) {
			s += font.exportElement();
		}
		return s;
	}
}
