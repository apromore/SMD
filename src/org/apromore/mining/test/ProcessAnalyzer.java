package org.apromore.mining.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apromore.dao.ProcessDao;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.standardize.StandardizedCluster;
import org.apromore.service.ClusteringService;
import org.apromore.service.FragmentService;
import org.apromore.service.ProcessService;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ProcessAnalyzer {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessAnalyzer.class);
	
//	private static final String outpath = "/media/work/qut/process-mining/eval/tests4/t1/windscreen/pcm3/log4/analysis";
	private static final String outpath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/pcm/analysis";
//	private static final String stdclustersPath = "/media/work/qut/process-mining/eval/tests4/t1/windscreen/pcm3/log4/stdclusters.csv";
	private static final String stdclustersPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/pcm/stdclusters.csv";
	
	@Autowired
	private ClusteringService csrv;
	
	@Autowired @Qualifier("ProcessService")
	private ProcessService psrv;
	
	@Autowired @Qualifier("FragmentService")
	private FragmentService fsrv;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdoa;
	
	@Autowired
	private TestingComposer testingComposer;
	
	private FormattableEPCSerializer formattableEPCSerializer = new FormattableEPCSerializer();
	
	private Map<String, StandardizedCluster> standardizedClusters;
	private Map<String, List<String>> fragmentClusterMap;
	private List<String[]> replacements = new ArrayList<String[]>();
	private Queue<String> stdFragments = new LinkedList<String>();
	
	public void analyze(String processName) throws Exception {
		
		initialize();
		
		// create a new folder for the process in the outpath
		File pfolder = new File(outpath, processName);
		if (pfolder.exists()) {
			FileUtils.cleanDirectory(pfolder);
		} else {
			pfolder.mkdir();
		}
		
		// output whole process (without subprocess extraction)
		File fragmentsFolder = new File(pfolder, "fragments");
		fragmentsFolder.mkdir();
		String rootId = pdoa.getRootFragmentId(processName);
		CPF cpf = fsrv.getFragment(rootId, false);
		formattableEPCSerializer.serialize(cpf, new File(fragmentsFolder.getAbsolutePath(), "plainRoot.epml").getAbsolutePath());
		
		// output process with subprocesses
		Set<String> allSubprocesses = getAllFragments(standardizedClusters);
		CPF root = testingComposer.compose(rootId, allSubprocesses);
		formattableEPCSerializer.serialize(root, new File(fragmentsFolder.getAbsolutePath(), "root.epml").getAbsolutePath());
		
		while (!stdFragments.isEmpty()) {
			String fid = stdFragments.poll();
			CPF fragment = testingComposer.compose(fid, allSubprocesses);
			stdFragments.remove(fid);
			formattableEPCSerializer.serialize(fragment, new File(fragmentsFolder.getAbsolutePath(), fid + ".epml").getAbsolutePath());
		}
		
		// output replacement pairs
		File replacementsFolder = new File(pfolder, "replacements");
		replacementsFolder.mkdir();
		StringBuffer replacementInfo = new StringBuffer();
		for (String[] replacement : replacements) {
			String stdFid = replacement[0];
			String replacedFid = replacement[1];
			String involvedCid = replacement[2];
			replacementInfo.append(replacedFid + " replaced by " + stdFid + " in cluster " + involvedCid + "\n");
			CPF stdFragment = fsrv.getFragment(stdFid, false);
			formattableEPCSerializer.serialize(stdFragment, new File(replacementsFolder.getAbsolutePath(), stdFid + ".epml").getAbsolutePath());
			CPF replacedFragment = fsrv.getFragment(replacedFid, false);
			formattableEPCSerializer.serialize(replacedFragment, new File(replacementsFolder.getAbsolutePath(), replacedFid + ".epml").getAbsolutePath());
		}
		FileUtils.write(new File(pfolder, "replacements.txt"), replacementInfo.toString());
		
		
		// output cluster data to a file (bcr, std effort, etc)
		
	}

	private void initialize() throws Exception {
		
		standardizedClusters = new HashMap<String, StandardizedCluster>();
		fragmentClusterMap = new HashMap<String, List<String>>();

		File stdclustersFile = new File(stdclustersPath);
		List<String> lines = FileUtils.readLines(stdclustersFile);
		int i = 0;
		for (String line : lines) {
			i++;
			if (i == 1) {
				continue;
			}
			
			String[] parts = line.split(";");
			String cid = parts[0];
			StandardizedCluster stdCluster = new StandardizedCluster(cid);
			standardizedClusters.put(cid, stdCluster);
			
			String fidsPart = parts[1];
			List<String> fids = new ArrayList<String>();
			String[] fidParts = fidsPart.split(",");
			for (String fid : fidParts) {
				if (!fid.isEmpty()) {
					fids.add(fid);
				}
			}
			stdCluster.setFragmentIds(fids);
			
			String representativeFid = parts[2];
			stdCluster.setRepresentativeFragmentId(representativeFid);
			
			String sfidsPart = parts[3];
			List<String> sfids = new ArrayList<String>();
			String[] sfidParts = sfidsPart.split(",");
			for (String sfid : sfidParts) {
				if (!sfid.isEmpty()) {
					sfids.add(sfid);
				}
			}
			stdCluster.setStandardFragmentIds(sfids);
			
			for (String fid : fids) {
				List<String> fragmentClusters = new ArrayList<String>();
				fragmentClusters.add(cid);
				fragmentClusterMap.put(fid, fragmentClusters);
			}
		}
	}

	public String getStandardizedFragment(String fragmentId) {
		List<String> cids = fragmentClusterMap.get(fragmentId);
		if (cids == null) {
			return null;
		}
		
		StandardizedCluster stdCluster = standardizedClusters.get(cids.get(0));
		if (logger.isTraceEnabled()) {
			logger.trace("Standardizing fragment {} with {} in cluster {}", new Object[] {fragmentId, stdCluster.getStandardFragmentIds().get(0), cids.get(0)});
		}
		String standardFragmentId = stdCluster.getRepresentativeFragmentId(); 
		replacements.add(new String[] {standardFragmentId, fragmentId, cids.get(0)});
		stdFragments.add(standardFragmentId);
		return standardFragmentId;
	}
	
	private Set<String> getAllFragments(Map<String, StandardizedCluster> clusters) {
		Set<String> fragmentIds = new HashSet<String>();
		for (String cid : clusters.keySet()) {
			fragmentIds.addAll(clusters.get(cid).getFragmentIds());
		}
		return fragmentIds;
	}

}
