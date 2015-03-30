package org.prom5.exporting.ontologies;

import java.io.OutputStream;

import org.prom5.exporting.Exporter;
import org.prom5.framework.models.ontology.OntologyModel;

public class ExportOntologyModel {

	@Exporter(extension = "wsml", name = "Export ontology to WSML")
	public void export(OntologyModel ontology, OutputStream out) throws Exception {
		out.write(ontology.serialize().getBytes());
	}
}
