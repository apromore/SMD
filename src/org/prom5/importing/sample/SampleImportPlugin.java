package org.prom5.importing.sample;

import java.io.IOException;
import java.io.InputStream;

import org.prom5.framework.models.ModelGraphPanel;
import org.prom5.framework.models.dot.DotModel;
import org.prom5.importing.Importer;
import org.prom5.importing.pnml.PnmlImport;
import org.prom5.mining.petrinetmining.PetriNetResult;

public class SampleImportPlugin {
	@Importer(name = "Sample DOT importer", extension = "dot")
	public static ModelGraphPanel importDot(InputStream input) throws IOException {
		return new DotModel(read(input)).getGrappaVisualization();
	}
	
	public static String read(InputStream input) throws IOException {
		StringBuffer buffer = new StringBuffer();
		int c;
		while ((c = input.read()) != -1) {
			buffer.append((char) c);
		}
		return buffer.toString();
	}
	
	@Importer(name = "Sample Petri net importer", extension = "pnml", connectToLog = true)
	public static PetriNetResult importNet(InputStream input) throws IOException {
		PetriNetResult result = (PetriNetResult) new PnmlImport().importFile(input);
		return result;
	}
}
