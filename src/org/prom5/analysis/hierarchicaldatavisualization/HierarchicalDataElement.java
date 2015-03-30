package org.prom5.analysis.hierarchicaldatavisualization;

import java.util.Set;

import org.prom5.framework.models.ModelGraphVertex;

public interface HierarchicalDataElement {
	Set<ModelGraphVertex> getNodes();
	double getValue();
}
