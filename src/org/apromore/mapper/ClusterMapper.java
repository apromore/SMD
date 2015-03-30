/**
 * 
 */
package org.apromore.mapper;

import java.util.List;
import java.util.Map;

import org.apromore.common.FSConstants;
import org.apromore.dao.model.ClusterInfo;
import org.apromore.dao.model.ClusteringSummary;
import org.apromore.model.ClusterFilterType;
import org.apromore.model.ClusterSettingsType;
import org.apromore.model.ClusterSummaryType;
import org.apromore.model.ClusterType;
import org.apromore.model.ClusteringParameterType;
import org.apromore.model.ClusteringSummaryType;
import org.apromore.model.ConstrainedProcessIdsType;
import org.apromore.model.FragmentData;
import org.apromore.model.PairDistancesType;
import org.apromore.model.PiarDistanceType;
import org.apromore.model.ProcessAssociationsType;
import org.apromore.service.model.Cluster;
import org.apromore.service.model.ClusterFilter;
import org.apromore.service.model.ClusterSettings;
import org.apromore.service.model.MemberFragment;
import org.apromore.service.model.ProcessAssociation;
import org.apromore.toolbox.clustering.algorithms.dbscan.DBSCANClusterSettings;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentPair;

/**
 * @author Chathura C. Ekanayake
 *
 */
public class ClusterMapper {
	
	public static ClusterSummaryType convertClusterInfoToClusterSummaryType(ClusterInfo c) {
		ClusterSummaryType ct = new ClusterSummaryType();
		ct.setClusterId(c.getClusterId());
		ct.setClusterSize(c.getSize());
		ct.setMedoidId(c.getMedoidId());
		ct.setAvgFragmentSize(c.getAvgFragmentSize());
		ct.setStandardizationEffort(c.getStandardizingEffort());
		ct.setRefactoringGain(c.getRefactoringGain());
		ct.setBCR(c.getBCR());
		return ct;
	}
	
	public static ClusterType convertClusterToClusterType(Cluster c) {
		
		ClusterType ct = new ClusterType();
		ct.setClusterId(c.getClusterInfo().getClusterId());
		ct.setClusterSize(c.getClusterInfo().getSize());
		ct.setMedoidId(c.getClusterInfo().getMedoidId());
		ct.setAvgFragmentSize(c.getClusterInfo().getAvgFragmentSize());
		ct.setStandardizationEffort(c.getClusterInfo().getStandardizingEffort());
		ct.setRefactoringGain(c.getClusterInfo().getRefactoringGain());
		ct.setBCR(c.getClusterInfo().getBCR());
		
		List<MemberFragment> fs = c.getFragments();
		for (MemberFragment f : fs) {
			FragmentData fd = new FragmentData();
			fd.setFragmentId(f.getFragmentId());
			fd.setFragmentSize(f.getFragmentSize());
			fd.setDistance(f.getDistance());
			
			List<ProcessAssociation> pas = f.getProcessAssociations();
			for (ProcessAssociation pa : pas) {
				ProcessAssociationsType patype = new ProcessAssociationsType();
				patype.setProcessId(pa.getProcessId());
				patype.setProcessName(pa.getProcessName());
				patype.setBranchName(pa.getProcessBranchName());
				patype.setProcessVersionId(pa.getProcessVersionId());
				patype.setProcessVersionNumber(pa.getProcessVersionNumber());
				fd.getProcessAssociations().add(patype);
			}
			
			ct.getFragments().add(fd);
		}
		
		return ct;
	}
	
	public static ClusterFilter convertClusterFilterTypeToClusterFilter(ClusterFilterType cftype) {
		ClusterFilter filter = new ClusterFilter();
		filter.setMinClusterSize(cftype.getMinClusterSize());
		filter.setMaxClusterSize(cftype.getMaxClusterSize());
		filter.setMinAverageFragmentSize(cftype.getMinAvgFragmentSize());
		filter.setMaxAverageFragmentSize(cftype.getMaxAvgFragmentSize());
		filter.setMinBCR(cftype.getMinBCR());
		filter.setMaxBCR(cftype.getMaxBCR());
		return filter;
	}

	/**
	 * @param clusterSettings
	 * @return
	 */
	public static ClusterSettings convertClusterSettingsTypeToClusterSettings(ClusterSettingsType clusterSettingsType) {
		
//		ClusterSettings clusterSettings = null;
//		if (FSConstants.DBSCAN.equals(clusterSettingsType.getAlgorithm())) {
//			DBSCANClusterSettings dbscanSettings = new DBSCANClusterSettings();
//			List<ClusteringParameterType> params = clusterSettingsType.getClusteringParams();
//			for (ClusteringParameterType param : params) {
//				if ("maxdistance".equalsIgnoreCase(param.getParamName())) {
//					double maxDistance = Double.parseDouble(param.getParmaValue());
//					dbscanSettings.setMaxNeighborGraphEditDistance(maxDistance);
//				}
//			}
//			ConstrainedProcessIdsType cpidsType = clusterSettingsType.getConstrainedProcessIds();
//			if (cpidsType != null) {
//				List<Integer> constrainedProcessIds = cpidsType.getProcessId();
//				if (constrainedProcessIds != null && !constrainedProcessIds.isEmpty()) {
//					dbscanSettings.setConstrainedProcessIds(constrainedProcessIds);
//				}
//			}
//			clusterSettings = dbscanSettings;
//			
//		}
		
		ClusterSettings clusterSettings = new ClusterSettings();
		clusterSettings.setAlgorithm(clusterSettingsType.getAlgorithm());
		List<ClusteringParameterType> params = clusterSettingsType.getClusteringParams();
		for (ClusteringParameterType param : params) {
			if ("maxdistance".equalsIgnoreCase(param.getParamName())) {
				double maxDistance = Double.parseDouble(param.getParmaValue());
				clusterSettings.setMaxNeighborGraphEditDistance(maxDistance);
			}
		}
		ConstrainedProcessIdsType cpidsType = clusterSettingsType.getConstrainedProcessIds();
		if (cpidsType != null) {
			List<Integer> constrainedProcessIds = cpidsType.getProcessId();
			if (constrainedProcessIds != null && !constrainedProcessIds.isEmpty()) {
				clusterSettings.setConstrainedProcessIds(constrainedProcessIds);
			}
		}
		
		return clusterSettings;
	}

	/**
	 * @param s
	 * @return
	 */
	public static ClusteringSummaryType convertClusteringSummaryToClusteringSummaryType(ClusteringSummary s) {
		ClusteringSummaryType st = new ClusteringSummaryType();
		st.setNumClusters(s.getNumClusters());
		st.setMinClusterSize(s.getMinClusterSize());
		st.setMaxClusterSize(s.getMaxClusterSize());
		st.setMinAvgFragmentSize(s.getMinAvgFragmentSize());
		st.setMaxAvgFragmentSize(s.getMaxAvgFragmentSize());
		st.setMinBCR(s.getMinBCR());
		st.setMaxBCR(s.getMaxBCR());
		return st;
	}

	public static PairDistancesType convertPairDistancesToPairDistancesType(Map<FragmentPair, Double> pairDistances) {
		PairDistancesType pdt = new PairDistancesType();
		for (FragmentPair pair : pairDistances.keySet()) {
			PiarDistanceType p = new PiarDistanceType();
			p.setFragmentId1(pair.getFid1());
			p.setFragmentId2(pair.getFid2());
			p.setDistance(pairDistances.get(pair));
			pdt.getPiarDistance().add(p);
		}
		return pdt;
	}
}
