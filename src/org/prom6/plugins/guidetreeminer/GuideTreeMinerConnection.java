package org.prom6.plugins.guidetreeminer;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.prom6.plugins.guidetreeminer.tree.GuideTree;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 14 July 2010 
 * @since 01 July 2010
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */

public class GuideTreeMinerConnection extends AbstractConnection {
	public final static String GUIDETREE = "Guide Tree";
	public final static String CLUSTERLOGOUTPUT = "Cluster Log Output";
	public final static String GUIDETREEINPUTLOG = "Input Log";
	public final static String GUIDETREEINPUT = "Guide Tree Input Settings";
	
	protected GuideTreeMinerConnection(GuideTree guideTree, ClusterLogOutput clusterLogOutput, GuideTreeMinerInput input, XLog log) {
		super("Guide Tree and related related mined information");
		put(GUIDETREE, guideTree);
		put(CLUSTERLOGOUTPUT, clusterLogOutput);
		put(GUIDETREEINPUT, input);
		put(GUIDETREEINPUTLOG, log);
	}

}
