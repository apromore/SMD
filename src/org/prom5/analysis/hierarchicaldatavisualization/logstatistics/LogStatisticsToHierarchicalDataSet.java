package org.prom5.analysis.hierarchicaldatavisualization.logstatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.prom5.analysis.hierarchicaldatavisualization.AbstractHierarchicalData;
import org.prom5.analysis.hierarchicaldatavisualization.HierarchicalDataElement;
import org.prom5.analysis.logstatistics.LogStatistic;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.ModelGraph;
import org.prom5.framework.models.ModelGraphVertex;
import org.prom5.framework.models.ontology.ConceptModel;
import org.prom5.framework.models.ontology.OntologyModel;

public class LogStatisticsToHierarchicalDataSet extends AbstractHierarchicalData {

	private Map<String, LogStatistic> statistics;
	private LogReader log;
	private LogStatisticDataElementFactory factory;
	private ArrayList<HierarchicalDataElement> dataElements;
	
	public LogStatisticsToHierarchicalDataSet(Map<String, LogStatistic> statistics, LogReader log, LogStatisticDataElementFactory factory) {
		this.statistics = statistics;
		this.log = log;
		this.factory = factory;
		this.dataElements = null;		
	}

	public Iterator<HierarchicalDataElement> iterator() {
		initDataElements();
		return dataElements.iterator();
	}

	private void initDataElements() {
		if (dataElements == null) {
			Map<String, ModelGraphVertex> vertexMapping = new HashMap<String, ModelGraphVertex>();
			
			for (OntologyModel ontology : log.getLogSummary().getOntologies().getOntologies()) {
				ModelGraph model = ontology.toModelGraph();
				
				for (ModelGraphVertex vertex : model.getVerticeList()) {
					String uri = ontology.getConceptURIInLog(ontology.findConcept(
							OntologyModel.getConceptPart(vertex.getIdentifier())
							));
					vertexMapping.put(uri, vertex);
				}
			}
			
			dataElements = new ArrayList<HierarchicalDataElement>();
			for (Map.Entry<String, LogStatistic> element : statistics.entrySet()) {
				factory.create(element.getKey(), element.getValue(), vertexMapping, dataElements);
			}
			this.statistics = null; // we don't need it anymore
		}
	}

	public String getPluralHierarchyName() {
		return "Ontologies";
	}

	public String getSingularHierarchyName() {
		return "Ontology";
	}

	@Override
	public String formatVertexLabel(String label) {
		ConceptModel concept = log.getLogSummary().getOntologies().findConceptByUriInOntology(label);
		
		if (concept != null) {
			label = label.replace(concept.getName(), concept.getShortName());
		}
		return label;
	}

	@Override
	public String getGraphName(String id) {
		return OntologyModel.getConceptPart(id);
	}
}
