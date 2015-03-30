package org.processmining.plugins.epml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeEPC"> <xs:complexContent> <xs:extension
 *         base="epml:tExtensibleElements"> <xs:sequence> <xs:choice
 *         minOccurs="0" maxOccurs="unbounded"> <xs:element
 *         name="configurationRequirement" type="epml:typeCReq"/> <xs:element
 *         name="configurationGuideline" type="epml:typeCReq"/> <xs:element
 *         name="configurationOrder" type="epml:typeCOrder"/> </xs:choice>
 *         <xs:choice minOccurs="0" maxOccurs="unbounded"> <xs:element
 *         name="event" type="epml:typeEvent" minOccurs="0"/> <xs:element
 *         name="function" type="epml:typeFunction" minOccurs="0"/> <xs:element
 *         name="role" type="epml:typeRole" minOccurs="0"/> <xs:element
 *         name="object" type="epml:typeObject" minOccurs="0"/> <xs:element
 *         name="processInterface" type="epml:typeProcessInterface"
 *         minOccurs="0"/> <xs:element name="and" type="epml:typeAND"
 *         minOccurs="0"/> <xs:element name="or" type="epml:typeOR"
 *         minOccurs="0"/> <xs:element name="xor" type="epml:typeXOR"
 *         minOccurs="0"/> <xs:element name="range" type="epml:typeRANGE"
 *         minOccurs="0"/> <xs:element name="arc" type="epml:typeArc"
 *         minOccurs="0"/> <xs:any namespace="##other" processContents="lax"
 *         minOccurs="0" maxOccurs="unbounded"/> </xs:choice> </xs:sequence>
 *         <xs:attribute name="epcId" type="xs:positiveInteger" use="required"/>
 *         <xs:attribute name="name" type="xs:string" use="required"/>
 *         </xs:extension> </xs:complexContent> </xs:complexType>
 */

public class EpmlEpc extends EpmlExtensibleElements {

	public final static String TAG = "epc";

	/*
	 * Attributes
	 */
	private String epcId;
	private String name;

	/*
	 * Elements
	 * 
	 * Note that the order between different Configuration elements is lost
	 * here. Note that the order between different non-Configuration elements is
	 * lost here as well.
	 */
	private final List<EpmlConfigurationRequirement> configurationRequirementList;
	private final List<EpmlConfigurationGuideline> configurationGuidelineList;
	private final List<EpmlConfigurationOrder> configurationOrderList;
	private final List<EpmlEvent> eventList;
	private final List<EpmlFunction> functionList;
	private final List<EpmlRole> roleList;
	private final List<EpmlObject> objectList;
	private final List<EpmlProcessInterface> processInterfaceList;
	private final List<EpmlAnd> andList;
	private final List<EpmlOr> orList;
	private final List<EpmlXor> xorList;
	private final List<EpmlRange> rangeList;
	private final List<EpmlArc> arcList;

	public EpmlEpc() {
		super(TAG);

		epcId = null;
		name = null;

		configurationRequirementList = new ArrayList<EpmlConfigurationRequirement>();
		configurationGuidelineList = new ArrayList<EpmlConfigurationGuideline>();
		configurationOrderList = new ArrayList<EpmlConfigurationOrder>();
		eventList = new ArrayList<EpmlEvent>();
		functionList = new ArrayList<EpmlFunction>();
		roleList = new ArrayList<EpmlRole>();
		objectList = new ArrayList<EpmlObject>();
		processInterfaceList = new ArrayList<EpmlProcessInterface>();
		andList = new ArrayList<EpmlAnd>();
		orList = new ArrayList<EpmlOr>();
		xorList = new ArrayList<EpmlXor>();
		rangeList = new ArrayList<EpmlRange>();
		arcList = new ArrayList<EpmlArc>();
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "epcId");
		if (value != null) {
			epcId = value;
		}
		value = xpp.getAttributeValue(null, "name");
		if (value != null) {
			name = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (epcId != null) {
			s += exportAttribute("epcId", epcId);
		}
		if (name != null) {
			s += exportAttribute("name", name);
		}
		return s;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlConfigurationRequirement.TAG)) {
			EpmlConfigurationRequirement configurationRequirement = new EpmlConfigurationRequirement();
			configurationRequirement.importElement(xpp, epml);
			configurationRequirementList.add(configurationRequirement);
			return true;
		}
		if (xpp.getName().equals(EpmlConfigurationGuideline.TAG)) {
			EpmlConfigurationGuideline configurationGuideline = new EpmlConfigurationGuideline();
			configurationGuideline.importElement(xpp, epml);
			configurationGuidelineList.add(configurationGuideline);
			return true;
		}
		if (xpp.getName().equals(EpmlConfigurationOrder.TAG)) {
			EpmlConfigurationOrder configurationOrder = new EpmlConfigurationOrder();
			configurationOrder.importElement(xpp, epml);
			configurationOrderList.add(configurationOrder);
			return true;
		}
		if (xpp.getName().equals(EpmlEvent.TAG)) {
			EpmlEvent event = new EpmlEvent();
			event.importElement(xpp, epml);
			eventList.add(event);
			return true;
		}
		if (xpp.getName().equals(EpmlFunction.TAG)) {
			EpmlFunction function = new EpmlFunction();
			function.importElement(xpp, epml);
			functionList.add(function);
			return true;
		}
		if (xpp.getName().equals(EpmlRole.TAG)) {
			EpmlRole role = new EpmlRole();
			role.importElement(xpp, epml);
			roleList.add(role);
			return true;
		}
		if (xpp.getName().equals(EpmlObject.TAG)) {
			EpmlObject object = new EpmlObject();
			object.importElement(xpp, epml);
			objectList.add(object);
			return true;
		}
		if (xpp.getName().equals(EpmlProcessInterface.TAG)) {
			EpmlProcessInterface processInterface = new EpmlProcessInterface();
			processInterface.importElement(xpp, epml);
			processInterfaceList.add(processInterface);
			return true;
		}
		if (xpp.getName().equals(EpmlAnd.TAG)) {
			EpmlAnd and = new EpmlAnd();
			and.importElement(xpp, epml);
			andList.add(and);
			return true;
		}
		if (xpp.getName().equals(EpmlOr.TAG)) {
			EpmlOr or = new EpmlOr();
			or.importElement(xpp, epml);
			orList.add(or);
			return true;
		}
		if (xpp.getName().equals(EpmlXor.TAG)) {
			EpmlXor xor = new EpmlXor();
			xor.importElement(xpp, epml);
			xorList.add(xor);
			return true;
		}
		if (xpp.getName().equals(EpmlRange.TAG)) {
			EpmlRange range = new EpmlRange();
			range.importElement(xpp, epml);
			rangeList.add(range);
			return true;
		}
		if (xpp.getName().equals(EpmlArc.TAG)) {
			EpmlArc arc = new EpmlArc();
			arc.importElement(xpp, epml);
			arcList.add(arc);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		for (EpmlConfigurationRequirement configurationRequirement : configurationRequirementList) {
			s += configurationRequirement.exportElement();
		}
		for (EpmlConfigurationGuideline configurationGuideline : configurationGuidelineList) {
			s += configurationGuideline.exportElement();
		}
		for (EpmlConfigurationOrder configurationOrder : configurationOrderList) {
			s += configurationOrder.exportElement();
		}
		for (EpmlEvent event : eventList) {
			s += event.exportElement();
		}
		for (EpmlFunction function : functionList) {
			s += function.exportElement();
		}
		for (EpmlRole role : roleList) {
			s += role.exportElement();
		}
		for (EpmlObject object : objectList) {
			s += object.exportElement();
		}
		for (EpmlProcessInterface processInterface : processInterfaceList) {
			s += processInterface.exportElement();
		}
		for (EpmlAnd and : andList) {
			s += and.exportElement();
		}
		for (EpmlOr or : orList) {
			s += or.exportElement();
		}
		for (EpmlXor xor : xorList) {
			s += xor.exportElement();
		}
		for (EpmlRange range : rangeList) {
			s += range.exportElement();
		}
		for (EpmlArc arc : arcList) {
			s += arc.exportElement();
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkPositiveInteger(epml, "epcId", epcId, true);
		checkRequired(epml, "name", name);
	}

	public void convertToCEpc(ConfigurableEPC cEpc, GraphLayoutConnection layout) {
		Map<String, EPCNode> map = new HashMap<String, EPCNode>();
		for (EpmlEvent event : eventList) {
			event.convertToEpc(cEpc, map, layout);
		}
		for (EpmlFunction function : functionList) {
			function.convertToCEpc(cEpc, map, layout);
		}
		for (EpmlAnd and : andList) {
			and.convertToCEpc(cEpc, map, layout);
		}
		for (EpmlOr or : orList) {
			or.convertToCEpc(cEpc, map, layout);
		}
		for (EpmlXor xor : xorList) {
			xor.convertToCEpc(cEpc, map, layout);
		}
		for (EpmlArc arc : arcList) {
			arc.convertToEpc(cEpc, map, layout);
		}
	}
}
