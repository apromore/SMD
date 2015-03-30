package org.prom5.analysis.hierarchicaldatavisualization.logstatistics;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.prom5.analysis.hierarchicaldatavisualization.HierarchicalDataElement;
import org.prom5.analysis.logstatistics.LogStatistic;
import org.prom5.analysis.logstatistics.ValueWithModelReferences;
import org.prom5.framework.models.ModelGraphVertex;

public class ProcessingTimeFactory implements LogStatisticDataElementFactory {
	public void create(String key, LogStatistic value, Map<String, ModelGraphVertex> mapping, Collection<HierarchicalDataElement> data) {
		for (Map.Entry<String, List<ValueWithModelReferences>> item : value.getActivityDurationsWithModelReferences().entrySet()) {
			for (ValueWithModelReferences valueWithMR : item.getValue()) {
				data.add(new ProcessingTimeDataElement(valueWithMR.getValue(), valueWithMR.getModelReferences(), mapping));
			}
		}
	}
}

class ProcessingTimeDataElement implements HierarchicalDataElement {

	private double value;
	private Set<ModelGraphVertex> nodes;

	public ProcessingTimeDataElement(long value, Collection<String> modelReferences, Map<String, ModelGraphVertex> mapping) {
		this.value = value;
		this.nodes = new HashSet<ModelGraphVertex>();
		for (String uri : modelReferences) {
			this.nodes.add(mapping.get(uri));
		}
	}

	public double getValue() {
		return value;
	}

	public Set<ModelGraphVertex> getNodes() {
		return nodes;
	}
}
