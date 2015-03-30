package org.prom5.analysis.hierarchicaldatavisualization.logstatistics;

import java.util.Collection;
import java.util.Map;

import org.prom5.analysis.hierarchicaldatavisualization.HierarchicalDataElement;
import org.prom5.analysis.logstatistics.LogStatistic;
import org.prom5.framework.models.ModelGraphVertex;

public interface LogStatisticDataElementFactory {
	void create(String key, LogStatistic value, Map<String, ModelGraphVertex> vertexMapping, Collection<HierarchicalDataElement> data);
}
