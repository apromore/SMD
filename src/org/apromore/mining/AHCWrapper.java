/**
 * 
 */
package org.apromore.mining;

import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm;

/**
 * @author Chathura C. Ekanayake
 *
 */
public class AHCWrapper extends AgglomerativeHierarchicalAlgorithm {

	public void doCluster() {
		this.noTraces = input.getProfile().numberOfInstances();
		if (currentMethod == null) {
			currentMethod = "Complete linkage";
		}
		clusterCriteria = currentMethod;
		this.distances = input.getDistanceMatrix();
		agglomerativeClustering();
//		setupGui();
//		cluster();
	}
	
	public void setClusteringMethod(String method) {
		this.currentMethod = method;
	}
}
