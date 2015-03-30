package org.apromore.service;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface ClusterAnalyzerService {

	void serializeClusterAssignments(OutputStream out);

	Map<String, String> getIdenticalClusters(File f1, File f2);

	Map<String, String> getCoveredClusters(File f1, File f2);

	Map<String, String> getNCoveredClusters(File f1, File f2, int n);

	List<String> getDisjointClusters(File f1, File f2);
}
