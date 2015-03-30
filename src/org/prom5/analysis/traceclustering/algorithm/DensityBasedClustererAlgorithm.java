package org.prom5.analysis.traceclustering.algorithm;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import org.deckfour.slickerbox.components.SmoothPanel;
import org.prom5.framework.ui.Message;
import org.prom5.framework.util.GUIPropertyListEnumeration;

import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.clusterers.FarthestFirst;
import weka.clusterers.MakeDensityBasedClusterer;
import weka.clusterers.SimpleKMeans;

/**
 * @author Minseok Song
 */
public class DensityBasedClustererAlgorithm  extends WekaAlgorithm {
	
	protected GUIPropertyListEnumeration clusterBox;
	
	public DensityBasedClustererAlgorithm()
	{
		super("Density Based Clustering",
				"Density Based Clustering allows the user to specify"
				+ " the number of clusters. The algorithm will return"
				+ " the number of clusters which users want.");
		clusters = null;
		clusterer = new MakeDensityBasedClusterer();
		
	}

	protected void doCluster()
	{
		try{
			
			Clusterer clusterer2 = null; 
			if(clusterBox.getValue().equals("K-means")){
				clusterer2 = new SimpleKMeans();
				((SimpleKMeans)clusterer2).setSeed(randomSeedBox.getValue());
				((MakeDensityBasedClusterer)clusterer).setClusterer(clusterer2);
				((MakeDensityBasedClusterer)clusterer).setNumClusters(clusterSizeBox.getValue());
			} else if(clusterBox.getValue().equals("Farthest First")){
				clusterer2 = new FarthestFirst();
				((FarthestFirst)clusterer2).setSeed(randomSeedBox.getValue());
				((MakeDensityBasedClusterer)clusterer).setClusterer(clusterer2);
			} else {
				clusterer2 = new EM();
				((EM)clusterer2).setSeed(randomSeedBox.getValue());
				((MakeDensityBasedClusterer)clusterer).setClusterer(clusterer2);
				((MakeDensityBasedClusterer)clusterer).setNumClusters(clusterSizeBox.getValue());
			}

			clusterer.buildClusterer(data);
			assignInstace();
			
		} catch (Exception c)
		{
			Message.add("Weka Error: " + c.toString(), Message.ERROR);
		}		
	}
	
	protected SmoothPanel getMenuPanel()
	{
		SmoothPanel menuPanel = new SmoothPanel();
		menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.PAGE_AXIS));
		
		menuPanel.add(clusterSizeBox.getPropertyPanel());		
		menuPanel.add(randomSeedBox.getPropertyPanel());
		ArrayList<String> values = new ArrayList<String>();
		values.add("K-means");
		values.add("Farthest First");
		values.add("EM");
		clusterBox = new GUIPropertyListEnumeration("Clusterer to wrap =",null,values,null,110);
		menuPanel.add(clusterBox.getPropertyPanel());

		startButton = new JButton("cluster");
				startButton.setOpaque(false);
				startButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
				startButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						clusterSize = clusterSizeBox.getValue();
						cluster();
					  }
			});
		menuPanel.add(startButton);
		return menuPanel;
	}
}
