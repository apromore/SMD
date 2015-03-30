package org.processmining.plugins.epml;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

public class EpmlAttributableEpcElement extends EpmlEpcElement {

	private final List<EpmlAttribute> attributeList;

	public EpmlAttributableEpcElement(String tag) {
		super(tag);

		attributeList = new ArrayList<EpmlAttribute>();
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlAttribute.TAG)) {
			EpmlAttribute attribute = new EpmlAttribute();
			attribute.importElement(xpp, epml);
			attributeList.add(attribute);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		for (EpmlAttribute attribute : attributeList) {
			s += attribute.exportElement();
		}
		return s;
	}
}
