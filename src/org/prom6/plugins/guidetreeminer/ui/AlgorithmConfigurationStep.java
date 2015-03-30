package org.prom6.plugins.guidetreeminer.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.prom6.plugins.guidetreeminer.settings.GuideTreeMinerSettingsListener;
import org.prom6.plugins.guidetreeminer.swingx.ScrollableGridLayout;
import org.prom6.plugins.guidetreeminer.types.AHCJoinType;
import org.prom6.plugins.guidetreeminer.types.LearningAlgorithmType;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 June 2010
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */

@SuppressWarnings("serial")
public class AlgorithmConfigurationStep extends myStep {
	JPanel algorithmPanel;
	JPanel ahcPanel;
	JPanel outputClusterLogsPanel;
	
	JRadioButton ahcRadioButton, neighborJoiningRadioButton;
	JRadioButton singleLinkageRadioButton, completeLinkageRadioButton, averageLinkageRadioButton, centroidLinkageRadioButton, minVarianceRadioButton;
	
	JCheckBox outputClusterLogsCheckBox;
	JLabel noClustersLabel;
	JTextField noClustersTextField;
	GuideTreeMinerSettingsListener listener;
	
	public AlgorithmConfigurationStep(){
		initComponents();
	}
	
	private void initComponents(){
		ScrollableGridLayout algorithmConfigurationPanelLayout = new ScrollableGridLayout(this, 1, 4, 0, 0);
		algorithmConfigurationPanelLayout.setRowFixed(0, true);
		algorithmConfigurationPanelLayout.setRowFixed(1, true);
		this.setLayout(algorithmConfigurationPanelLayout);
		
		JLabel headerLabel = SlickerFactory.instance().createLabel("<html><h1>Guide Tree Building Algorithm Configuration Step</h1>");
		algorithmConfigurationPanelLayout.setPosition(headerLabel, 0, 0);
		add(headerLabel);
		
		buildAlgorithmPanel();
		
		algorithmConfigurationPanelLayout.setPosition(algorithmPanel, 0, 1);
		add(algorithmPanel);
		
		buildAHCPanel();
		buildOutputClusterLogsPanel();
		algorithmConfigurationPanelLayout.setPosition(ahcPanel, 0, 2);
		add(ahcPanel);
		algorithmConfigurationPanelLayout.setPosition(outputClusterLogsPanel, 0, 3);
		add(outputClusterLogsPanel);
		
	}
	
	private void buildAlgorithmPanel(){
		algorithmPanel = SlickerFactory.instance().createRoundedPanel();
		algorithmPanel.setBorder(BorderFactory.createTitledBorder("Select Algorithm"));
		
		ScrollableGridLayout algorithmPanelLayout = new ScrollableGridLayout(algorithmPanel, 1, 2, 0, 0);
		algorithmPanelLayout.setRowFixed(0, true);
		algorithmPanelLayout.setRowFixed(1, true);
		
		algorithmPanel.setLayout(algorithmPanelLayout);
		ahcRadioButton = SlickerFactory.instance().createRadioButton("Agglomerative Hierarchical Clustering");
		ahcRadioButton.setSelected(true);
		
		neighborJoiningRadioButton = SlickerFactory.instance().createRadioButton("Neighbor Joining");
		neighborJoiningRadioButton.setVisible(false);
		
		ButtonGroup methodButtonGroup = new ButtonGroup();
		methodButtonGroup.add(ahcRadioButton);
		methodButtonGroup.add(neighborJoiningRadioButton);
		
		algorithmPanelLayout.setPosition(ahcRadioButton, 0, 0);
		algorithmPanel.add(ahcRadioButton);
		
		algorithmPanelLayout.setPosition(neighborJoiningRadioButton, 0, 1);
		algorithmPanel.add(neighborJoiningRadioButton);
		
		ahcRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(ahcRadioButton.isSelected()){
					ahcPanel.setVisible(true);
					outputClusterLogsPanel.setVisible(true);
					revalidate();
					repaint();
				}
			}
		});
		
		neighborJoiningRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(neighborJoiningRadioButton.isSelected()){
					ahcPanel.setVisible(false);
					outputClusterLogsPanel.setVisible(false);
					revalidate();
					repaint();
				}
			}
		});
	}
	
	private void buildAHCPanel(){
		ahcPanel = SlickerFactory.instance().createRoundedPanel();
		ahcPanel.setBorder(BorderFactory.createTitledBorder("Select Join Type for Agglomerative Hierarchical Clustering"));
		
		ScrollableGridLayout ahcPanelLayout = new ScrollableGridLayout(ahcPanel, 1, 5, 0, 0);
		ahcPanelLayout.setRowFixed(0, true);
		ahcPanelLayout.setRowFixed(1, true);
		ahcPanelLayout.setRowFixed(2, true);
		ahcPanelLayout.setRowFixed(3, true);
		ahcPanelLayout.setRowFixed(4, true);
		ahcPanel.setLayout(ahcPanelLayout);
		
		singleLinkageRadioButton = SlickerFactory.instance().createRadioButton("Single Linkage");
		completeLinkageRadioButton = SlickerFactory.instance().createRadioButton("Complete Linkage");
		averageLinkageRadioButton = SlickerFactory.instance().createRadioButton("Average Linkage (UPGMA)");
		centroidLinkageRadioButton = SlickerFactory.instance().createRadioButton("Centroid Linkage");
		minVarianceRadioButton = SlickerFactory.instance().createRadioButton("Min. Variance");
		minVarianceRadioButton.setSelected(true);
		
		ButtonGroup joinTypeButtonGroup = new ButtonGroup();
		joinTypeButtonGroup.add(singleLinkageRadioButton);
		joinTypeButtonGroup.add(completeLinkageRadioButton);
		joinTypeButtonGroup.add(averageLinkageRadioButton);
		joinTypeButtonGroup.add(centroidLinkageRadioButton);
		joinTypeButtonGroup.add(minVarianceRadioButton);
		
		ahcPanelLayout.setPosition(singleLinkageRadioButton, 0, 0);
		ahcPanel.add(singleLinkageRadioButton);
		
		ahcPanelLayout.setPosition(completeLinkageRadioButton, 0, 1);
		ahcPanel.add(completeLinkageRadioButton);
		
		ahcPanelLayout.setPosition(averageLinkageRadioButton, 0, 2);
		ahcPanel.add(averageLinkageRadioButton);
		
		ahcPanelLayout.setPosition(centroidLinkageRadioButton, 0, 3);
		ahcPanel.add(centroidLinkageRadioButton);
		
		ahcPanelLayout.setPosition(minVarianceRadioButton, 0, 4);
		ahcPanel.add(minVarianceRadioButton);
	}
	
	private void buildOutputClusterLogsPanel(){
		outputClusterLogsPanel = SlickerFactory.instance().createRoundedPanel();
		outputClusterLogsPanel.setBorder(BorderFactory.createTitledBorder("Select Cluster Output Options"));
	
		ScrollableGridLayout outputClusterLogsPanelLayout = new ScrollableGridLayout(outputClusterLogsPanel, 2, 5, 0, 0);
		outputClusterLogsPanelLayout.setRowFixed(0, true);
		outputClusterLogsPanelLayout.setRowFixed(1, true);
		outputClusterLogsPanel.setLayout(outputClusterLogsPanelLayout);
		
		outputClusterLogsCheckBox = SlickerFactory.instance().createCheckBox("Output Logs for each cluster", true);
		noClustersLabel = SlickerFactory.instance().createLabel("Select Number of Clusters");
		noClustersTextField = new JTextField("4  ");
		
		outputClusterLogsPanelLayout.setPosition(outputClusterLogsCheckBox, 0, 0);
		outputClusterLogsPanel.add(outputClusterLogsCheckBox);
		
		outputClusterLogsPanelLayout.setPosition(noClustersLabel, 0, 1);
		outputClusterLogsPanel.add(noClustersLabel);
		
		outputClusterLogsPanelLayout.setPosition(noClustersTextField, 1, 1);
		outputClusterLogsPanel.add(noClustersTextField);
		
		outputClusterLogsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(outputClusterLogsCheckBox.isSelected()){
					noClustersLabel.setVisible(true);
					noClustersTextField.setVisible(true);
				}else{
					noClustersLabel.setVisible(false);
					noClustersTextField.setVisible(false);
				}
				
			}
		});
		
	}
	
	public boolean precondition() {
		return true;
	}

	public void setListener(GuideTreeMinerSettingsListener listener){
		this.listener = listener;
	}
	
	public void readSettings() {
		if(ahcRadioButton.isSelected()){
			listener.setLearningAlgorithmType(LearningAlgorithmType.AHC);
			if(outputClusterLogsCheckBox.isSelected()){
				listener.setOutputClusterLogs(true);
				listener.setNumberOfClusters(new Integer(noClustersTextField.getText().trim()).intValue());
			}else{
				listener.setOutputClusterLogs(false);
			}
			
		}else if(neighborJoiningRadioButton.isSelected())
			listener.setLearningAlgorithmType(LearningAlgorithmType.NeighborJoining);
		
		if(singleLinkageRadioButton.isSelected())
			listener.setAHCJoinType(AHCJoinType.SingleLinkage);
		else if(completeLinkageRadioButton.isSelected())
			listener.setAHCJoinType(AHCJoinType.CompleteLinkage);
		else if(centroidLinkageRadioButton.isSelected())
			listener.setAHCJoinType(AHCJoinType.CentroidLinkage);
		else if(averageLinkageRadioButton.isSelected())
			listener.setAHCJoinType(AHCJoinType.AverageLinkage);
		else if(minVarianceRadioButton.isSelected())
			listener.setAHCJoinType(AHCJoinType.MinVariance);
	}

}
