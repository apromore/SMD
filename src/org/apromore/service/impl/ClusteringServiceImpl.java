/**
 * 
 */
package org.apromore.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tue.tm.is.graph.SimpleGraph;

import org.apache.commons.io.FileUtils;
import org.apromore.common.Constants;
import org.apromore.common.FSConstants;
import org.apromore.dao.ClusteringDao;
import org.apromore.dao.ContentDao;
import org.apromore.dao.EdgeDao;
import org.apromore.dao.NodeDao;
import org.apromore.dao.dao.model.ContentDO;
import org.apromore.dao.model.ClusterInfo;
import org.apromore.dao.model.ClusteringSummary;
import org.apromore.dao.model.FragmentVersion;
import org.apromore.dao.model.GNode;
import org.apromore.dao.model.ProcessFragmentMap;
import org.apromore.exception.LockFailedException;
import org.apromore.exception.RepositoryException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfAndGateway;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.graph.JBPT.CpfMessage;
import org.apromore.graph.JBPT.CpfNode;
import org.apromore.graph.JBPT.CpfOrGateway;
import org.apromore.graph.JBPT.CpfTask;
import org.apromore.graph.JBPT.CpfTimer;
import org.apromore.graph.JBPT.CpfXorGateway;
import org.apromore.mining.EvaluatorUtil;
import org.apromore.service.ClusteringService;
import org.apromore.service.FragmentService;
import org.apromore.service.helper.SimpleGraphWrapper;
import org.apromore.service.model.Cluster;
import org.apromore.service.model.ClusterFilter;
import org.apromore.service.model.ClusterSettings;
import org.apromore.service.model.MemberFragment;
import org.apromore.service.model.ProcessAssociation;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentPair;
import org.apromore.toolbox.clustering.algorithms.dbscan.InMemoryClusterer;
import org.jbpt.pm.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import clustering.DMatrix;
import clustering.dissimilarity.measure.GEDDissimCalc;
import clustering.hac.HACClusterer;
import clustering.incremental.IncrementalDissimMatrixGenerator;

/**
 * @author Chathura C. Ekanayake
 *
 */
public class ClusteringServiceImpl implements ClusteringService {
	
	private static final Logger logger = LoggerFactory.getLogger(ClusteringServiceImpl.class);

	@Autowired @Qualifier("ClusteringDao")
    private ClusteringDao clusteringDao;
	
	@Autowired
	private FragmentService fragmentService;
	
	@Autowired
	private InMemoryClusterer dbscanClusterer;
	
	@Autowired
	private HACClusterer hacClusterer;
	
	@Autowired @Qualifier("DMatrix")
    private DMatrix dmatrix;
	
	@Autowired
	private IncrementalDissimMatrixGenerator incrementalGED;
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void computeGEDMatrix() {
		
		EvaluatorUtil.gedStart();
		
		try {
			clusteringDao.clearDistances();
			dmatrix.compute();
		} catch (Exception e) {
			logger.error("An error occurred while computing the GED matrix for the first time. This could result in lesser number of clusters. PLEASE RERUN THE COMPUTATION.", e);
			e.printStackTrace();
		}
		
		EvaluatorUtil.gedEnd();
	}
	
	@Override
	public void appendGEDMatrix(Collection<String> newRoots) {
		
		EvaluatorUtil.gedStart();
		
		try {
			incrementalGED.computeDissimilarity(newRoots);
		} catch (Exception e) {
			logger.error("An error occurred while appending the GED matrix. This could result in lesser number of clusters. PLEASE RERUN THE COMPUTATION.", e);
			e.printStackTrace();
		}
		
		EvaluatorUtil.gedEnd();
	}
	
	/* (non-Javadoc)
	 * @see org.apromore.service.ClusteringService#getClusters()
	 */
	@Override
	public List<ClusterInfo> getClusters() {
		return clusteringDao.getAllClusters();
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void cluster(ClusterSettings settings) throws RepositoryException {
		
		EvaluatorUtil.cloneDetectionStart();
		
		clusteringDao.clearClusters();
		
		if (FSConstants.DBSCAN.equals(settings.getAlgorithm())) {
			dbscanClusterer.clusterRepository(settings);
		} else if (FSConstants.HAC.equals(settings.getAlgorithm())) {
			hacClusterer.clusterRepository(settings);
		}
		
		EvaluatorUtil.cloneDetectionEnd();
	}
	
	@Override
	public ClusteringSummary getClusteringSummary() {
		ClusteringSummary summary = null;
		try {
			summary =  clusteringDao.getClusteringSummary();
		} catch (Exception e) {
			summary = new ClusteringSummary(new Long(0), new Integer(0), new Integer(0), 
					new Float(0), new Float(0), new Double(0), new Double(0));
		}
		return summary;
	}
	
	@Override
	public Map<FragmentPair, Double> getPairDistances(List<String> fragmentIds) throws RepositoryException {
		
		Map<FragmentPair, Double> pairDistances = new HashMap<FragmentPair, Double>();
		
		for (int i = 0; i < fragmentIds.size() - 1; i++) {
			for (int j = i + 1; j < fragmentIds.size(); j++) {
				String fid1 = fragmentIds.get(i);
				String fid2 = fragmentIds.get(j);
				double distance = clusteringDao.getDistance(fid1, fid2);
				if (distance < 0) {
					
					try {
						CPF g1 = fragmentService.getFragment(fid1, false);
						CPF g2 = fragmentService.getFragment(fid2, false);
						
						SimpleGraph sg1 = new SimpleGraphWrapper(g1);
						SimpleGraph sg2 = new SimpleGraphWrapper(g2);
						
						GEDDissimCalc calc = new GEDDissimCalc(1, 0.4);
						distance = calc.compute(sg1, sg2);
						
					} catch (LockFailedException e) {
						throw new RepositoryException(e);
					}
				}
				pairDistances.put(new FragmentPair(fid1, fid2), distance);
			}
		}
		
		return pairDistances;
	}
	
	@Override
	public List<ClusterInfo> getClusterSummaries(ClusterFilter filter) {
		return clusteringDao.getFilteredClusters(filter);
	}
	
	@Override
	public Cluster getCluster(String clusterId) {
		
		ClusterInfo cinfo = clusteringDao.getClusterSummary(clusterId);
		
		Cluster c = new Cluster();
		c.setClusterInfo(cinfo);
		List<FragmentVersion> fs = clusteringDao.getFragments(clusterId);
		for (FragmentVersion f : fs) {
			MemberFragment fragment = new MemberFragment(f.getFragmentVersionId());
			fragment.setFragmentSize(f.getFragmentSize());
			Set<ProcessFragmentMap> pmap = f.getProcessFragmentMaps();
			for (ProcessFragmentMap m : pmap) {
				String pmvid = Integer.toString(m.getProcessModelVersion().getProcessModelVersionId());
				int pmvNumber = m.getProcessModelVersion().getVersionNumber();
				String branchName = m.getProcessModelVersion().getProcessBranch().getBranchName();
				String processId = Integer.toString(m.getProcessModelVersion().getProcessBranch().getProcess().getProcessId());
				String processName = m.getProcessModelVersion().getProcessBranch().getProcess().getName();
				
				ProcessAssociation pa = new ProcessAssociation();
				pa.setProcessVersionId(pmvid);
				pa.setProcessVersionNumber(Integer.toString(pmvNumber));
				pa.setProcessBranchName(branchName);
				pa.setProcessId(processId);
				pa.setProcessName(processName);
				fragment.getProcessAssociations().add(pa);
			}
			double distance = clusteringDao.getDistance(cinfo.getMedoidId(), f.getFragmentVersionId());
			fragment.setDistance(distance);
			c.addFragment(fragment);
		}
		return c;
	}

	/* (non-Javadoc)
	 * @see org.apromore.service.ClusteringService#getClusters(org.apromore.service.model.ClusterFilter)
	 */
//	@Transactional (propagation = Propagation.REQUIRED)
	@Override
	public List<Cluster> getClusters(ClusterFilter filter) {
		
		List<Cluster> clusters = new ArrayList<Cluster>();
		List<ClusterInfo> cinfos = clusteringDao.getFilteredClusters(filter);
		for (ClusterInfo cinfo : cinfos) {
			Cluster c = new Cluster();
			c.setClusterInfo(cinfo);
			List<FragmentVersion> fs = clusteringDao.getFragments(cinfo.getClusterId());
			for (FragmentVersion f : fs) {
				MemberFragment fragment = new MemberFragment(f.getFragmentVersionId());
				fragment.setFragmentSize(f.getFragmentSize());
				Set<ProcessFragmentMap> pmap = f.getProcessFragmentMaps();
				for (ProcessFragmentMap m : pmap) {
					String pmvid = Integer.toString(m.getProcessModelVersion().getProcessModelVersionId());
					int pmvNumber = m.getProcessModelVersion().getVersionNumber();
					String branchName = m.getProcessModelVersion().getProcessBranch().getBranchName();
					String processId = Integer.toString(m.getProcessModelVersion().getProcessBranch().getProcess().getProcessId());
					String processName = m.getProcessModelVersion().getProcessBranch().getProcess().getName();
					
					ProcessAssociation pa = new ProcessAssociation();
					pa.setProcessVersionId(pmvid);
					pa.setProcessVersionNumber(Integer.toString(pmvNumber));
					pa.setProcessBranchName(branchName);
					pa.setProcessId(processId);
					pa.setProcessName(processName);
					fragment.getProcessAssociations().add(pa);
				}
				double distance = clusteringDao.getDistance(cinfo.getMedoidId(), f.getFragmentVersionId());
				fragment.setDistance(distance);
				c.addFragment(fragment);
			}
			clusters.add(c);
		}
		return clusters;
	}
	
	@Override
	public List<String> getFragmentIds(String clusterId) {
		return clusteringDao.getFragmentIds(clusterId);
	}

	public void setFragmentService(FragmentService fragmentService) {
		this.fragmentService = fragmentService;
	}

	public void setClusteringDao(ClusteringDao clusteringDao) {
		this.clusteringDao = clusteringDao;
	}

	public void setDbscanClusterer(InMemoryClusterer dbscanClusterer) {
		this.dbscanClusterer = dbscanClusterer;
	}
	
	@Override
	public void serializeClusters(String outPath) {
		
		FormattableEPCSerializer formattableEPCSerializer = new FormattableEPCSerializer();
		try {
			File outFolder = new File(outPath);
			FileUtils.cleanDirectory(outFolder);
			
			Collection<ClusterInfo> cs = getClusters();
			for (ClusterInfo c : cs) {
				File cFolder = new File(outFolder, c.getClusterId());
				cFolder.mkdir();
				
				Collection<String> fids = getFragmentIds(c.getClusterId());
				for (String fid : fids) {
					CPF f = fragmentService.getFragment(fid, false);
					String epml = formattableEPCSerializer.serializeToString(f);
					File fragmentFile = new File(cFolder, fid + ".epml");
					FileUtils.write(fragmentFile, epml);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to serialize all clusters to {}", outPath);
		}
		
	}
}
