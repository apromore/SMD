package org.processmining.plugins.epml;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jgraph.graph.GraphConstants;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.epc.EPCGraph;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.processmining.models.graphbased.directed.epc.elements.Arc;
import org.processmining.plugins.epml.graphics.EpmlGraphics;
import org.processmining.plugins.epml.graphics.EpmlMove;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeArc"> <xs:complexContent> <xs:extension
 *         base="epml:tExtensibleElements"> <xs:sequence> <xs:element
 *         name="name" type="xs:string" minOccurs="0"/> <xs:element
 *         name="description" type="xs:string" minOccurs="0"/> <xs:choice>
 *         <xs:element name="flow" type="typeFlow" minOccurs="0"/> <xs:element
 *         name="relation" type="typeRelation" minOccurs="0"/> </xs:choice>
 *         <xs:choice minOccurs="0" maxOccurs="unbounded"> <xs:element
 *         name="graphics" type="epml:typeMove"/> </xs:choice> <xs:choice
 *         minOccurs="0" maxOccurs="unbounded"> <xs:element name="attribute"
 *         type="epml:typeAttribute"/> </xs:choice> </xs:sequence> <xs:attribute
 *         name="id" type="xs:positiveInteger" use="required"/> </xs:extension>
 *         </xs:complexContent> </xs:complexType>
 */

public class EpmlArc extends EpmlExtensibleElements {

	public final static String TAG = "arc";

	/*
	 * Attributes
	 */
	private String id;

	/*
	 * Elements
	 */
	private EpmlName name;
	private EpmlDescription description;
	private EpmlFlow flow;
	private EpmlRelation relation;
	private final List<EpmlMove> graphicsList;
	private final List<EpmlAttribute> attributeList;

	public EpmlArc() {
		super(TAG);

		id = null;

		name = null;
		description = null;
		flow = null;
		relation = null;
		graphicsList = new ArrayList<EpmlMove>();
		attributeList = new ArrayList<EpmlAttribute>();
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "id");
		if (value != null) {
			id = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (id != null) {
			s += exportAttribute("id", id);
		}
		return s;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlName.TAG)) {
			name = new EpmlName();
			name.importElement(xpp, epml);
			return true;
		}
		if (xpp.getName().equals(EpmlDescription.TAG)) {
			description = new EpmlDescription();
			description.importElement(xpp, epml);
			return true;
		}
		if (xpp.getName().equals(EpmlFlow.TAG)) {
			flow = new EpmlFlow();
			flow.importElement(xpp, epml);
			return true;
		}
		if (xpp.getName().equals(EpmlRelation.TAG)) {
			relation = new EpmlRelation();
			relation.importElement(xpp, epml);
			return true;
		}
		if (xpp.getName().equals(EpmlGraphics.TAG)) {
			EpmlMove graphics = new EpmlMove();
			graphics.importElement(xpp, epml);
			graphicsList.add(graphics);
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
		if (name != null) {
			s += name.exportElement();
		}
		if (description != null) {
			s += description.exportElement();
		}
		if (flow != null) {
			s += flow.exportElement();
		}
		if (relation != null) {
			s += relation.exportElement();
		}
		for (EpmlMove graphics : graphicsList) {
			s += graphics.exportElement();
		}
		for (EpmlAttribute attribute : attributeList) {
			s += attribute.exportElement();
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkPositiveInteger(epml, "id", id, true);
	}

	public void convertToEpc(EPCGraph epc, Map<String, EPCNode> map, GraphLayoutConnection layout) {
		EPCNode source = null, target = null;
		if (flow != null) {
			source = map.get(flow.getSource());
			target = map.get(flow.getTarget());
		}
		if (relation != null) {
			source = map.get(relation.getSource());
			target = map.get(relation.getTarget());
		}
		if ((source != null) && (target != null)) {
			Arc arc;
			if (name != null) {
				arc = epc.addArc(source, target, name.get());
			} else {
				arc = epc.addArc(source, target);
			}
			arc.getAttributeMap().put(AttributeMap.STYLE, GraphConstants.STYLE_ORTHOGONAL);
			ArrayList<Point2D> points = new ArrayList<Point2D>();
			layout.setEdgePoints(arc, points);
			for (EpmlMove graphics : graphicsList) {
				graphics.convertToEpc(epc, points, source, target, layout);
			}
		}
	}
}
