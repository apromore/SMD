package org.processmining.plugins.epml;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.processmining.plugins.epml.graphics.EpmlGraphicsDefault;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeEPML"> <xs:complexContent> <xs:extension
 *         base="epml:tExtensibleElements"> <xs:sequence> <xs:element
 *         name="graphicsDefault" type="epml:typeGraphicsDefault"
 *         minOccurs="0"/> <xs:element name="coordinates"
 *         type="epml:typeCoordinates"/> <xs:element name="definitions"
 *         type="epml:typeDefinitions" minOccurs="0"/> <xs:element
 *         name="attributeTypes" type="epml:typeAttrTypes" minOccurs="0"
 *         maxOccurs="unbounded"/> <xs:element name="directory"
 *         type="epml:typeDirectory" maxOccurs="unbounded"> <xs:unique
 *         name="epcId"> <xs:selector xpath=".//*"/> <xs:field xpath="@epcId"/>
 *         </xs:unique> <xs:keyref name="linkToEpcId" refer="epcId">
 *         <xs:selector xpath=".//toProcess"/> <xs:field xpath="@linkToEpcId"/>
 *         </xs:keyref> </xs:element> </xs:sequence> </xs:extension>
 *         </xs:complexContent> </xs:complexType>
 */
public class Epml extends EpmlExtensibleElements {

	public final static String TAG = "epml";

	private XFactory factory;
	private XLog log;
	private XTrace trace;
	private XExtension conceptExtension;
	private XExtension organizationalExtension;

	private boolean hasErrors;

	/*
	 * Elements
	 */
	private EpmlGraphicsDefault graphicsDefault;
	private EpmlCoordinates coordinates;
	private EpmlDefinitions definitions;
	private final List<EpmlAttributeTypes> attributeTypesList;
	private final List<EpmlDirectory> directoryList;

	public Epml() {
		super(TAG);

		factory = new XFactoryNaiveImpl();
		log = factory.createLog();
		log.getExtensions().add(XConceptExtension.instance());
		log.getExtensions().add(XLifecycleExtension.instance());
		trace = factory.createTrace();
		log.add(trace);

		graphicsDefault = null;
		coordinates = null;
		definitions = null;
		attributeTypesList = new ArrayList<EpmlAttributeTypes>();
		directoryList = new ArrayList<EpmlDirectory>();

		initializeLog();
	}

	/**
	 * Creates and initializes a log to throw to the framework when importing
	 * the PNML file fails. In this log, every net will have its own trace. The
	 * first net is preceded by a preamble.
	 */
	private void initializeLog() {
		factory = XFactoryRegistry.instance().currentDefault();
		conceptExtension = XConceptExtension.instance();
		organizationalExtension = XOrganizationalExtension.instance();
		log = factory.createLog();
		log.getExtensions().add(conceptExtension);
		log.getExtensions().add(organizationalExtension);

		log("<preamble>");

		hasErrors = false;
	}

	public XLog getLog() {
		return log;
	}

	/**
	 * Adds a log event to the current trace in the log.
	 * 
	 * @param context
	 *            Context of the message, typically the current PNML tag.
	 * @param lineNumber
	 *            Current line number.
	 * @param message
	 *            Error message.
	 */
	public void log(String context, int lineNumber, String message) {
		XAttributeMap attributeMap = new XAttributeMapImpl();
		attributeMap.put(XConceptExtension.KEY_NAME,
				factory.createAttributeLiteral(XConceptExtension.KEY_NAME, message, conceptExtension));
		attributeMap.put(XConceptExtension.KEY_INSTANCE,
				factory.createAttributeLiteral(XConceptExtension.KEY_INSTANCE, context, conceptExtension));
		attributeMap.put(XOrganizationalExtension.KEY_RESOURCE, factory.createAttributeLiteral(
				XOrganizationalExtension.KEY_RESOURCE, "Line " + lineNumber, organizationalExtension));
		XEvent event = factory.createEvent(attributeMap);
		trace.add(event);
		hasErrors = true;
	}

	/**
	 * Adds a new trace with the given name to the log. This trace is now
	 * current.
	 * 
	 * @param name
	 *            The give name.
	 */
	public void log(String name) {
		trace = factory.createTrace();
		log.add(trace);
		trace.getAttributes().put(XConceptExtension.KEY_NAME,
				factory.createAttributeLiteral(XConceptExtension.KEY_NAME, name, conceptExtension));
	}

	public boolean hasErrors() {
		return hasErrors;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlGraphicsDefault.TAG)) {
			graphicsDefault = new EpmlGraphicsDefault();
			graphicsDefault.importElement(xpp, epml);
			return true;
		}
		if (xpp.getName().equals(EpmlCoordinates.TAG)) {
			coordinates = new EpmlCoordinates();
			coordinates.importElement(xpp, epml);
			return true;
		}
		if (xpp.getName().equals(EpmlDefinitions.TAG)) {
			definitions = new EpmlDefinitions();
			definitions.importElement(xpp, epml);
			return true;
		}
		if (xpp.getName().equals(EpmlAttributeTypes.TAG)) {
			EpmlAttributeTypes attributeTypes = new EpmlAttributeTypes();
			attributeTypes.importElement(xpp, epml);
			attributeTypesList.add(attributeTypes);
			return true;
		}
		if (xpp.getName().equals(EpmlDirectory.TAG)) {
			EpmlDirectory directory = new EpmlDirectory();
			directory.importElement(xpp, epml);
			directoryList.add(directory);
			return true;
		}
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (graphicsDefault != null) {
			s += graphicsDefault.exportElement();
		}
		if (coordinates != null) {
			s += coordinates.exportElement();
		}
		if (definitions != null) {
			s += definitions.exportElement();
		}
		for (EpmlAttributeTypes attributeTypes : attributeTypesList) {
			s += attributeTypes.exportElement();
		}
		for (EpmlDirectory directory : directoryList) {
			s += directory.exportElement();
		}
		return s;
	}

	public void convertToCEpc(ConfigurableEPC cEpc, GraphLayoutConnection layout) {
		if (!directoryList.isEmpty()) {
			directoryList.get(0).convertToCEpc(cEpc, layout);
			boolean doLayout = false;
			/*
			 * If any node has no position, then we need to layout the graph.
			 */
			for (EPCNode node : cEpc.getNodes()) {
				if (layout.getPosition(node) == null) {
					doLayout = true;
				}
			}
			if (!doLayout) {
				/*
				 * All nodes have position (10.0,10.0) (which is the default
				 * position) we need to layout as well.
				 */
				doLayout = true;
				for (EPCNode node : cEpc.getNodes()) {
					Point2D position = layout.getPosition(node);
					if ((position.getX() != 10.0) || (position.getY() != 10.0)) {
						doLayout = false;
					}
				}
			}
			layout.setLayedOut(!doLayout);
		}
	}
}
