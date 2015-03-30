package org.apromore.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apromore.service.utils.IDGenerator;
import org.prom5.analysis.traceclustering.algorithm.ClusteringInput;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.prom5.analysis.traceclustering.distance.DistanceMetric;
import org.prom5.analysis.traceclustering.distance.EuclideanDistance;
import org.prom5.analysis.traceclustering.profile.ActivityPatternsProfile;
import org.prom5.analysis.traceclustering.profile.ActivityProfile;
import org.prom5.analysis.traceclustering.profile.AggregateProfile;
import org.prom5.analysis.traceclustering.profile.Profile;
import org.prom5.analysis.traceclustering.profile.TransitionProfile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.LogReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogClusterer {
	
	private final Logger logger = LoggerFactory.getLogger(LogClusterer.class);
	
	private String id;
	private LogReader masterLog = null;
	private MyCluster root = null;
	private Map<Integer, MyCluster> idClusterMap = new HashMap<Integer, MyCluster>();
	
	private int masterLogInstances = 0;

	public String getId() {
		return id;
	}

	public MyCluster getLogCluster(int id) {
		MyCluster c = idClusterMap.get(id);
		if (c == null) {
			c = findLogCluster(id);
		}
		return c;
	}
	
	private MyCluster findLogCluster(int cid) {
		
		MyCluster result = null;
		Queue<MyCluster> q = new LinkedList<MyCluster>();
		q.add(root);
		while(!q.isEmpty()) {
			MyCluster c = q.poll();
			c.setGroupId(id);
			idClusterMap.put(c.getID(), c);
			if (c.getID() == cid) {
				result = c;
				break;
			}
			
			MyCluster l = c.getLeft();
			if (l != null) {
				q.add(l);
			}
			
			MyCluster r = c.getRight();
			if (r != null) {
				q.add(r);
			}
		}
		return result;
	}
	
	public void initialize(LogReader log) throws Exception {
		
		this.id = IDGenerator.generateLogClustererID();
		
		this.masterLog = log;
		this.masterLogInstances = masterLog.getLogSummary().getNumberOfUniqueProcessInstances();
		
		logger.debug("Starting trace clustering for a master log with {} instances...", masterLogInstances);
		
		logger.debug("Creating profiles...");
		Profile p1 = new ActivityProfile(log);
		p1.setNormalizationMaximum(1.0d);
		AggregateProfile ap = new AggregateProfile(log);
		ap.addProfile(p1);
		
		// test code
		Profile p2 = new TransitionProfile(log);
		p2.setNormalizationMaximum(1.0d);
		ap.addProfile(p2);
//		
//		Profile p3 = new ActivityPatternsProfile(log);
//		p3.setNormalizationMaximum(1.0d);
//		ap.addProfile(p3);
		// end of test code
		
		logger.debug("Doing the trace clustering...");
		DistanceMetric metric = new EuclideanDistance();
		ClusteringInput input = new ClusteringInput(ap, metric);
		AHCWrapper algorithm = new AHCWrapper();
		algorithm.setInput(input);
		algorithm.setClusteringMethod("Complete linkage");
		algorithm.doCluster();
		
		List<MyCluster> cs = new ArrayList<MyCluster>();
		List<MyCluster> noise = new ArrayList<MyCluster>();
		root = algorithm.getRoot();
		root.setGroupId(this.getId());
		idClusterMap.put(root.getID(), root);
		
		logger.debug("Trace clustering completed.");
	}
	
	public MyCluster getRoot() {
		return root;
	}
	
	public LogReader getRootLog() {
		return masterLog;
	}

	/**
	 * Returns the child clusters of the given cluster Id, if a valid cluster Id is given. If -1 is given as the 
	 * cluster Id, root cluster is returned.
	 * 
	 * @param clusterId A valid cluster Id or -1.
	 * @return Child clusters of the given cluster Id or the root cluster. Note that the child clusters may contain
	 * singletons or clusters with small number of traces, which may be considered as noise.
	 * @throws Exception 
	 */
	public Map<MyCluster, LogReader> getChildren(MyCluster cluster) throws Exception {
		
		logger.trace("Getting child clusters of the cluster: " + cluster.getID());
		
		Map<MyCluster, LogReader> children = new HashMap<MyCluster, LogReader>();
		if (MiningConfig.MIN_SPLITTABLE_LOG_SIZE > 0) {
			LogReader parentLog = getLog(cluster);
			if (parentLog.getInstances().size() < MiningConfig.MIN_SPLITTABLE_LOG_SIZE) {
				logger.trace("Log cluster: {} does not contain minimum of {} traces required for reclustering.", 
						cluster.getID(), MiningConfig.MIN_SPLITTABLE_LOG_SIZE);
				return children;
			}
		}
		
		if (cluster.getLeft() == null) {
			return children;
		}
		
		MyCluster leftChild = cluster.getLeft();
		leftChild.setGroupId(id);
		LogReader leftLog = getLog(leftChild);
		children.put(leftChild, leftLog);
		idClusterMap.put(leftChild.getID(), leftChild);
		
		MyCluster rightChild = cluster.getRight();
		rightChild.setGroupId(id);
		LogReader rightLog = getLog(rightChild);
		children.put(rightChild, rightLog);
		idClusterMap.put(rightChild.getID(), rightChild);
		
		return children;
	}
	
	public LogReader getLog(MyCluster cluster) throws Exception {
		
		List<Integer> traceIds = getTraceIds(cluster);
		
		int[] tids = new int[traceIds.size()];
		for (int i = 0; i < traceIds.size(); i++) {
			Integer tid = traceIds.get(i);
			if (tid - 1 < masterLogInstances) {
				tids[i] = tid - 1; // test. earlier tid[i] = tid;
			} else if (tid - 1 > masterLogInstances) {
				logger.error("Invalid trace ID: {} within trace Ids {}", tid, traceIds);
			}
		}
		
		LogReader clr = LogReaderFactory.createInstance(masterLog, tids);
		return clr;
	}
	
	private List<Integer> getTraceIds(MyCluster cluster) {
		List<Integer> traceIds = new ArrayList<Integer>();
		collectTraceIds(cluster, traceIds);
		return traceIds;
	}

	private void collectTraceIds(MyCluster cluster, List<Integer> traceIds) {
		if (cluster.getLeft() == null) {
			traceIds.add(cluster.getID());
		} else {
			collectTraceIds(cluster.getLeft(), traceIds);
			collectTraceIds(cluster.getRight(), traceIds);
		}
	}
}
