package org.apromore.mining;

import java.util.HashMap;
import java.util.Map;

import org.apromore.graph.JBPT.CPF;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.prom5.framework.log.LogReader;

public class MiningData {
	
	private LogReader masterLog;
	
	private MyCluster logCluster;
	
	private LogReader log;
	
	private CPF processModel;
	
	private Map<MyCluster, CPF> minedModels = new HashMap<MyCluster, CPF>();
	
	public MiningData() {}
	
	public MiningData(MiningData parent) {
		this.masterLog = parent.getMasterLog();
		this.minedModels = parent.getMinedModels();
	}
	
	public String getClusterIdAsString() {
		String clusterId = this.getLogCluster() != null? Integer.toString(this.getLogCluster().getID()) : "Null";
		return clusterId;
	}

	public LogReader getMasterLog() {
		return masterLog;
	}

	public void setMasterLog(LogReader masterLog) {
		this.masterLog = masterLog;
	}
	
	public MyCluster getLogCluster() {
		return logCluster;
	}

	public void setLogCluster(MyCluster logCluster) {
		this.logCluster = logCluster;
	}

	public LogReader getLog() {
		return log;
	}

	public void setLog(LogReader log) {
		this.log = log;
	}

	public CPF getProcessModel() {
		return processModel;
	}

	public void setProcessModel(CPF processModel) {
		this.processModel = processModel;
	}

	public Map<MyCluster, CPF> getMinedModels() {
		return minedModels;
	}
}
