package org.processmining.plugins.epml.importing;

import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.epc.EPCFactory;
import org.processmining.plugins.epml.Epml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

@Plugin(name = "Import C-EPC from EPML file", parameterLabels = { "Filename" }, returnLabels = { "Configurable EPC" }, returnTypes = { ConfigurableEPC.class })
@UIImportPlugin(description = "EPML EPC files", extensions = { "epml" })
public class EpmlImportPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("PNML files", "pnml");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		/*
		 * Get an XML pull parser.
		 */
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		/*
		 * Initialize the parser on the provided input.
		 */
		xpp.setInput(input, null);
		/*
		 * Get the first event type.
		 */
		int eventType = xpp.getEventType();
		Epml epml = new Epml();

		/*
		 * Skip whatever we find until we've found a start tag.
		 */
		while (eventType != XmlPullParser.START_TAG) {
			eventType = xpp.next();
		}
		/*
		 * Check whether start tag corresponds to PNML start tag.
		 */
		if (xpp.getName().equals(Epml.TAG)) {
			/*
			 * Yes it does. Import the PNML element.
			 */
			epml.importElement(xpp, epml);
		} else {
			/*
			 * No it does not. Return null to signal failure.
			 */
			epml.log(Epml.TAG, xpp.getLineNumber(), "Expected epml");
		}
		if (epml.hasErrors()) {
			context.getProvidedObjectManager().createProvidedObject("Log of EPML import", epml.getLog(), XLog.class,
					context);
			return null;
		}

		ConfigurableEPC cEpc = EPCFactory.newConfigurableEPC("C-EPC imported from " + filename);
		/*
		 * Set the label of the Petri net.
		 */
		context.getFutureResult(0).setLabel("C-EPC imported from " + filename);

		GraphLayoutConnection layout = new GraphLayoutConnection(cEpc);
		epml.convertToCEpc(cEpc, layout);
		return cEpc;
	}
}