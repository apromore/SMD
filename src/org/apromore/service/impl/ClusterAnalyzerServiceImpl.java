package org.apromore.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apromore.dao.ClusteringDao;
import org.apromore.dao.model.ClusterInfo;
import org.apromore.service.ClusterAnalyzerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ClusterAnalyzerServiceImpl implements ClusterAnalyzerService {
	
	Logger log = LoggerFactory.getLogger(ClusterAnalyzerServiceImpl.class);

	@Autowired @Qualifier("ClusteringDao")
    private ClusteringDao clusteringDao;
	
	@Override
	public void serializeClusterAssignments(OutputStream out) {
	
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out));
		
		List<ClusterInfo> cs = clusteringDao.getAllClusters();
		for (ClusterInfo c : cs) {
			StringBuffer cline = new StringBuffer();
			cline.append(c.getClusterId());
			List<String> fids = clusteringDao.getFragmentIds(c.getClusterId());
			for (String fid : fids) {
				cline.append("," + fid);
			}
			try {
				w.write(cline.toString() + "\n");
			} catch (IOException e) {
				String msg = "Failed to write cluster information to the output stream.";
				log.error(msg, e);
			}
		}
		
		try {
			w.flush();
			w.close();
		} catch (IOException e) {
			String msg = "Failed to finalize writing clustering info.";
			log.error(msg, e);
		}
	}
	
	private Map<String, List<String>> read(File file) {
		Map<String, List<String>> cs = new HashMap<String, List<String>>();
		
		try {
			List<String> lines = FileUtils.readLines(file);
			for (String l : lines) {
				String[] ps = l.split(",");
				if (ps.length > 1) {
					String cid = ps[0];
					List<String> members = new ArrayList<String>();
					for (int i = 1; i < ps.length; i++) {
						members.add(ps[i]);
					}
					cs.put(cid, members);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return cs;
	}
	
	@Override
	public Map<String, String> getIdenticalClusters(File f1, File f2) {
		Map<String, List<String>> cs1 = read(f1);
		Map<String, List<String>> cs2 = read(f2);
		analyzeCounts(cs1, cs2);
		return getIdenticalClusters(cs1, cs2);
	}
	
	@Override
	public Map<String, String> getCoveredClusters(File f1, File f2) {
		Map<String, List<String>> cs1 = read(f1);
		Map<String, List<String>> cs2 = read(f2);
		return getCoveredClusters(cs1, cs2);
	}
	
	@Override
	public Map<String, String> getNCoveredClusters(File f1, File f2, int n) {
		Map<String, List<String>> cs1 = read(f1);
		Map<String, List<String>> cs2 = read(f2);
		return getNCoveredClusters(cs1, cs2, n);
	}
	
	@Override
	public List<String> getDisjointClusters(File f1, File f2) {
		Map<String, List<String>> cs1 = read(f1);
		Map<String, List<String>> cs2 = read(f2);
		return getDisjointClusters(cs1, cs2);
	}
	
	private Map<String, String> getIdenticalClusters(Map<String, List<String>> g1, Map<String, List<String>> g2) {
		
		Map<String, String> mappings = new HashMap<String, String>();
		
		for (String cidg1 : g1.keySet()) {
			List<String> fidsg1 = g1.get(cidg1);
			
			for (String cidg2 : g2.keySet()) {
				List<String> fidsg2 = g2.get(cidg2);
				
				if (fidsg1.containsAll(fidsg2) && fidsg2.containsAll(fidsg1)) {
					mappings.put(cidg1, cidg2);
				}
			}
		}
		
		return mappings;
	}
	
	private List<String> getDisjointClusters(Map<String, List<String>> g1, Map<String, List<String>> g2) {
		
		List<String> disjoints = new ArrayList<String>();
		
		for (String cidg1 : g1.keySet()) {
			List<String> fidsg1 = g1.get(cidg1);
			boolean disjoint = true;
			
			for (String cidg2 : g2.keySet()) {
				List<String> fidsg2 = g2.get(cidg2);
				
				if (!Collections.disjoint(fidsg1, fidsg2)) {
					disjoint = false;
					break;
				}
			}
			
			if (disjoint) {
				disjoints.add(cidg1);
			}
		}
		
		return disjoints;
	}
	
	private Map<String, String> getCoveredClusters(Map<String, List<String>> g1, Map<String, List<String>> g2) {
		
		Map<String, String> mappings = new HashMap<String, String>();
		
		for (String cidg1 : g1.keySet()) {
			List<String> fidsg1 = g1.get(cidg1);
			
			for (String cidg2 : g2.keySet()) {
				List<String> fidsg2 = g2.get(cidg2);
				
				if (fidsg1.containsAll(fidsg2) && !fidsg2.containsAll(fidsg1)) {
					mappings.put(cidg1, cidg2);
				}
			}
		}
		
		return mappings;
	}
	
	private Map<String, String> getNCoveredClusters(Map<String, List<String>> g1, Map<String, List<String>> g2, int n) {
		
		Map<String, String> mappings = new HashMap<String, String>();
		
		for (String cidg1 : g1.keySet()) {
			List<String> fidsg1 = g1.get(cidg1);
			
			for (String cidg2 : g2.keySet()) {
				List<String> fidsg2 = g2.get(cidg2);
				
				if (fidsg1.containsAll(fidsg2) && fidsg2.containsAll(fidsg1)) {
				} else {
					int cover = 0;
					for (String g2member : fidsg2) {
						if (fidsg1.contains(g2member)) {
							cover++;
							if (cover == n) {
								mappings.put(cidg1, cidg2);
							}
						}
					}
				}
			}
		}
		
		return mappings;
	}
	
	private void analyzeCounts(Map<String, List<String>> g1, Map<String, List<String>> g2) {
		
		int all = g1.size();
		int identical = 0;
		Set<String> disjoint = new HashSet<String>(g1.keySet());
		Set<String> oneSharing = new HashSet<String>();
		Set<String> twoSharing = new HashSet<String>();
		int oneSharing2 = 0;
		
		for (String cidg1 : g1.keySet()) {
			List<String> fidsg1 = g1.get(cidg1);
			
			for (String cidg2 : g2.keySet()) {
				List<String> fidsg2 = g2.get(cidg2);
				
				if (fidsg1.containsAll(fidsg2) && fidsg2.containsAll(fidsg1)) {
					identical++;
					disjoint.remove(cidg1);
				} else {
					if (!Collections.disjoint(fidsg1, fidsg2)) {
						disjoint.remove(cidg1);
						
						oneSharing2++;
						
						int cover = 0;
						for (String g2member : fidsg2) {
							if (fidsg1.contains(g2member)) {
								cover++;
								if (cover == 1) {
									oneSharing.add(cidg1);
									break;
								}
							}
						}

						cover = 0;
						for (String g2member : fidsg2) {
							if (fidsg1.contains(g2member)) {
								cover++;
								if (cover == 2) {
									twoSharing.add(cidg1);
									break;
								}
							}
						}
					} 
				}
			}
		}
		
		System.out.println(all + " | " + identical + " | " + disjoint.size() + " | " + oneSharing.size() + " | " + oneSharing2);
	}
}
