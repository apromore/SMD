package org.processmining.plugins.epml;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeProcessInterface"> <xs:complexContent>
 *         <xs:extension base="epml:tEpcElement"> <xs:sequence> <xs:element
 *         name="toProcess" type="epml:typeToProcess"/> <xs:choice minOccurs="0"
 *         maxOccurs="unbounded"> <xs:element name="attribute"
 *         type="epml:typeAttribute"/> </xs:choice> </xs:sequence>
 *         </xs:extension> </xs:complexContent> </xs:complexType>
 */

public class EpmlProcessInterface extends EpmlAttributableEpcElement {

	public final static String TAG = "processInterface";

	/*
	 * Elements
	 */
	private EpmlToProcess toProcess;

	public EpmlProcessInterface() {
		super(TAG);

		toProcess = null;
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
		return s;
	}
}
