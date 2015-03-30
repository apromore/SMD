package org.prom6.plugins.guidetreeminer.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.prom6.plugins.guidetreeminer.settings.GuideTreeMinerSettingsListener;
import org.prom6.plugins.guidetreeminer.swingx.ScrollableGridLayout;
import org.prom6.plugins.guidetreeminer.types.DistanceMetricType;
import org.prom6.plugins.guidetreeminer.types.GTMFeatureType;
import org.prom6.plugins.guidetreeminer.types.SimilarityDistanceMetricType;
import org.prom6.plugins.guidetreeminer.types.SimilarityMetricType;

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
public class SimilarityDistanceConfigurationStep extends myStep {
	JPanel metricTypePanel;
	JPanel similarityMetricPanel, distanceMetricPanel, genericEditDistancePanel;
	JRadioButton similarityMetricRadioButton, distanceMetricRadioButton;
	JRadioButton fScoreSimilarityRadioButton;
	JRadioButton euclideanDistanceRadioButton, hammingDistanceRadioButton;
	JRadioButton levenshteinDistanceRadioButton, genericEditDistanceRadioButton;
	
	GuideTreeMinerSettingsListener listener;
	GTMFeatureType featureType;
	
	boolean isDeriveSubstitutionScore, isDeriveIndelScore;
	String substitutionScoreFileName;
	String indelScoreFileName;
	
	public SimilarityDistanceConfigurationStep(){
		initComponents();
	}
	
	private void initComponents(){
		ScrollableGridLayout similarityDistanceConfigurationPanelLayout = new ScrollableGridLayout(this, 1, 4, 0, 0);
		similarityDistanceConfigurationPanelLayout.setRowFixed(0, true);
		similarityDistanceConfigurationPanelLayout.setRowFixed(1, true);
		this.setLayout(similarityDistanceConfigurationPanelLayout);
		
		JLabel headerLabel = SlickerFactory.instance().createLabel("<html><h1>Similarity/Distance Metric Configuration Step</h1>");
		similarityDistanceConfigurationPanelLayout.setPosition(headerLabel, 0, 0);
		add(headerLabel);
		
		buildMetricTypePanel();
		similarityDistanceConfigurationPanelLayout.setPosition(metricTypePanel, 0, 1);
		add(metricTypePanel);
		
		buildSimilarityMetricPanel();
		buildDistanceMetricPanel();
		
		similarityDistanceConfigurationPanelLayout.setPosition(similarityMetricPanel, 0, 2);
		add(similarityMetricPanel);
		
		similarityDistanceConfigurationPanelLayout.setPosition(distanceMetricPanel, 0, 3);
		add(distanceMetricPanel);
	}
	
	private void buildMetricTypePanel(){
		metricTypePanel = SlickerFactory.instance().createRoundedPanel();
		metricTypePanel.setBorder(BorderFactory.createTitledBorder("Select Metric Type"));
		
		ScrollableGridLayout metricConfigurationPanelLayout = new ScrollableGridLayout(metricTypePanel, 1, 2, 0, 0);
		metricConfigurationPanelLayout.setRowFixed(0, true);
		metricConfigurationPanelLayout.setRowFixed(1, true);
		
		metricTypePanel.setLayout(metricConfigurationPanelLayout);
		similarityMetricRadioButton = SlickerFactory.instance().createRadioButton("Similarity");
		
		distanceMetricRadioButton = SlickerFactory.instance().createRadioButton("Distance");
		distanceMetricRadioButton.setSelected(true);
		
		ButtonGroup metricTypeButtonGroup = new ButtonGroup();
		metricTypeButtonGroup.add(similarityMetricRadioButton);
		metricTypeButtonGroup.add(distanceMetricRadioButton);
		
		metricConfigurationPanelLayout.setPosition(similarityMetricRadioButton, 0, 0);
		metricTypePanel.add(similarityMetricRadioButton);
		
		metricConfigurationPanelLayout.setPosition(distanceMetricRadioButton, 0, 1);
		metricTypePanel.add(distanceMetricRadioButton);
		
		similarityMetricRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(similarityMetricRadioButton.isSelected()){
					distanceMetricPanel.setVisible(false);
					similarityMetricPanel.setVisible(true);
					revalidate();
					repaint();
				}
			}
		});
		
		distanceMetricRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(distanceMetricRadioButton.isSelected()){
					distanceMetricPanel.setVisible(true);
					similarityMetricPanel.setVisible(false);
					revalidate();
					repaint();
				}
				
			}
		});
	}

	private void buildSimilarityMetricPanel(){
		similarityMetricPanel = SlickerFactory.instance().createRoundedPanel();
		similarityMetricPanel.setBorder(BorderFactory.createTitledBorder("Similarity Metrics"));
		
		ScrollableGridLayout similarityMetricPanelLayout = new ScrollableGridLayout(similarityMetricPanel, 1, SimilarityMetricType.values().length, 0, 0);
		similarityMetricPanel.setLayout(similarityMetricPanelLayout);
		
		fScoreSimilarityRadioButton = SlickerFactory.instance().createRadioButton("F-Score");
		fScoreSimilarityRadioButton.setSelected(true);
		
		ButtonGroup similarityMetricButtonGroup = new ButtonGroup();
		similarityMetricButtonGroup.add(fScoreSimilarityRadioButton);
		
		similarityMetricPanelLayout.setPosition(fScoreSimilarityRadioButton, 0, 0);
		similarityMetricPanel.add(fScoreSimilarityRadioButton);
		
		similarityMetricPanel.setVisible(false);
	}
	
	private void buildDistanceMetricPanel(){
		distanceMetricPanel = SlickerFactory.instance().createRoundedPanel();
		distanceMetricPanel.setBorder(BorderFactory.createTitledBorder("Distance Metrics"));
		
		ScrollableGridLayout distanceMetricPanelLayout = new ScrollableGridLayout(distanceMetricPanel, 1, DistanceMetricType.values().length+1, 0, 0);
		distanceMetricPanelLayout.setRowFixed(0, true);
		distanceMetricPanelLayout.setRowFixed(1, true);
		distanceMetricPanelLayout.setRowFixed(2, true);
		distanceMetricPanelLayout.setRowFixed(3, true);
		distanceMetricPanelLayout.setRowFixed(4, true);
		distanceMetricPanelLayout.setColumnFixed(0, true);
		
		distanceMetricPanel.setLayout(distanceMetricPanelLayout);
		
		euclideanDistanceRadioButton = SlickerFactory.instance().createRadioButton("Euclidean Distance");
		euclideanDistanceRadioButton.setSelected(true);
		hammingDistanceRadioButton = SlickerFactory.instance().createRadioButton("Hamming Distance");
		levenshteinDistanceRadioButton = SlickerFactory.instance().createRadioButton("Levenshtein Distance");
		genericEditDistanceRadioButton = SlickerFactory.instance().createRadioButton("Generic Edit Distance");
		levenshteinDistanceRadioButton.setVisible(false);
		genericEditDistanceRadioButton.setVisible(false);
		
		buildGenericEditDistancePanel();
		genericEditDistancePanel.setVisible(false);
		ButtonGroup distanceMetricButtonGroup = new ButtonGroup();
		distanceMetricButtonGroup.add(euclideanDistanceRadioButton);
		distanceMetricButtonGroup.add(hammingDistanceRadioButton);
		distanceMetricButtonGroup.add(levenshteinDistanceRadioButton);
		distanceMetricButtonGroup.add(genericEditDistanceRadioButton);
		
		distanceMetricPanelLayout.setPosition(euclideanDistanceRadioButton, 0, 0);
		distanceMetricPanel.add(euclideanDistanceRadioButton);
		
		distanceMetricPanelLayout.setPosition(hammingDistanceRadioButton, 0, 1);
		distanceMetricPanel.add(hammingDistanceRadioButton);
		
		distanceMetricPanelLayout.setPosition(levenshteinDistanceRadioButton, 0, 2);
		distanceMetricPanel.add(levenshteinDistanceRadioButton);
		
		distanceMetricPanelLayout.setPosition(genericEditDistanceRadioButton, 0, 3);
		distanceMetricPanel.add(genericEditDistanceRadioButton);
		
		distanceMetricPanelLayout.setPosition(genericEditDistancePanel, 0, 4);
		distanceMetricPanel.add(genericEditDistancePanel);
		
		levenshteinDistanceRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(levenshteinDistanceRadioButton.isSelected()){
					genericEditDistancePanel.setVisible(false);
				}
			}
		});
		
		genericEditDistanceRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(genericEditDistanceRadioButton.isSelected()){
					genericEditDistancePanel.setVisible(true);
				}
			}
		});

		hammingDistanceRadioButton.setVisible(false);
	}
	
	public boolean precondition() {
		return true;
	}

	private void buildGenericEditDistancePanel(){
		genericEditDistancePanel = SlickerFactory.instance().createRoundedPanel();
		
		GridBagLayout genericEditDistancePanelGBLayout = new GridBagLayout();
		genericEditDistancePanel.setLayout(genericEditDistancePanelGBLayout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
	
		JLabel substitutionScoresLabel = SlickerFactory.instance().createLabel("Substition Scores ");
		final JRadioButton deriveSubstitutionScoresRadioButton = SlickerFactory.instance().createRadioButton("Derive");
		final JRadioButton loadSubstitutionScoresRadioButton = SlickerFactory.instance().createRadioButton("Load");
		deriveSubstitutionScoresRadioButton.setSelected(true);
		isDeriveSubstitutionScore = true;
		
		
		ButtonGroup substitutionScoresButtonGroup = new ButtonGroup();
		substitutionScoresButtonGroup.add(deriveSubstitutionScoresRadioButton);
		substitutionScoresButtonGroup.add(loadSubstitutionScoresRadioButton);
		
		final JLabel substituitionScoreFileNameLabel = SlickerFactory.instance().createLabel(System.getProperty("user.home"));
		substituitionScoreFileNameLabel.setVisible(false);
		
		final JButton chooseSubstitutionScoreFileButton = SlickerFactory.instance().createButton("Choose File");
		chooseSubstitutionScoreFileButton.setEnabled(false);
		chooseSubstitutionScoreFileButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
				int returnVal = fileChooser.showOpenDialog(new JFrame());
				if(returnVal == JFileChooser.APPROVE_OPTION){
					substitutionScoreFileName = fileChooser.getSelectedFile().getAbsolutePath();
					substituitionScoreFileNameLabel.setText(substitutionScoreFileName);
					substituitionScoreFileNameLabel.setVisible(true);
				}
			}
		});
		
		
		
		JLabel indelScoresLabel = SlickerFactory.instance().createLabel("Indel Scores ");
		final JRadioButton deriveIndelScoresRadioButton = SlickerFactory.instance().createRadioButton("Derive");
		final JRadioButton loadIndelScoresRadioButton = SlickerFactory.instance().createRadioButton("Load");
		deriveIndelScoresRadioButton.setSelected(true);
		isDeriveIndelScore = true;
		ButtonGroup indelScoresButtonGroup = new ButtonGroup();
		indelScoresButtonGroup.add(deriveIndelScoresRadioButton);
		indelScoresButtonGroup.add(loadIndelScoresRadioButton);
		
		final JLabel indelRightGivenLeftScoreFileNameLabel = SlickerFactory.instance().createLabel(System.getProperty("user.home"));
		indelRightGivenLeftScoreFileNameLabel.setVisible(false);
		
		final JButton chooseIndelRightGivenLeftScoreFileButton = SlickerFactory.instance().createButton("Choose Indel Right Given Left File");
		chooseIndelRightGivenLeftScoreFileButton.setEnabled(false);
		chooseIndelRightGivenLeftScoreFileButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
				int returnVal = fileChooser.showOpenDialog(new JFrame());
				if(returnVal == JFileChooser.APPROVE_OPTION){
					indelScoreFileName = fileChooser.getSelectedFile().getAbsolutePath();
					indelRightGivenLeftScoreFileNameLabel.setText(indelScoreFileName);
					indelRightGivenLeftScoreFileNameLabel.setVisible(true);
				}
			}
		});
		
		deriveSubstitutionScoresRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(deriveSubstitutionScoresRadioButton.isSelected()){
					chooseSubstitutionScoreFileButton.setEnabled(false);
					substituitionScoreFileNameLabel.setVisible(false);
					isDeriveSubstitutionScore = true;
				}
			}
		});
		
		loadSubstitutionScoresRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(loadSubstitutionScoresRadioButton.isSelected()){
					chooseSubstitutionScoreFileButton.setEnabled(true);
//					substituitionScoreFileNameLabel.setVisible(true);
					isDeriveSubstitutionScore = false;
				}
			}
		});
		
		deriveIndelScoresRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(deriveIndelScoresRadioButton.isSelected()){
					chooseIndelRightGivenLeftScoreFileButton.setEnabled(false);
					indelRightGivenLeftScoreFileNameLabel.setVisible(false);
					isDeriveIndelScore = true;
				}
			}
		});
		
		loadIndelScoresRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(loadIndelScoresRadioButton.isSelected()){
					chooseIndelRightGivenLeftScoreFileButton.setEnabled(true);
					isDeriveIndelScore = false;
				}
			}
		});
		
		c.gridx = 0;
		c.gridy = 0;
		c.insets  = new Insets(0, 0, 5, 10);
		genericEditDistancePanel.add(substitutionScoresLabel, c);
		
		c.gridx = 1;
		c.gridy = 0;
		c.insets  = new Insets(0, 0, 5, 10);
		genericEditDistancePanel.add(deriveSubstitutionScoresRadioButton, c);
		
		c.gridx = 2;
		c.gridy = 0;
		c.insets  = new Insets(0, 0, 5, 10);
		genericEditDistancePanel.add(loadSubstitutionScoresRadioButton, c);
		
		c.gridx = 3;
		c.gridy = 0;
		c.insets  = new Insets(0, 0, 5, 10);
		genericEditDistancePanel.add(chooseSubstitutionScoreFileButton, c);
		
		c.gridx = 4;
		c.gridy = 0;
		c.insets  = new Insets(0, 0, 5, 10);
		genericEditDistancePanel.add(substituitionScoreFileNameLabel, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.insets  = new Insets(0, 0, 5, 10);
		genericEditDistancePanel.add(indelScoresLabel, c);

		c.gridx = 1;
		c.gridy = 1;
		c.insets  = new Insets(0, 0, 5, 10);
		genericEditDistancePanel.add(deriveIndelScoresRadioButton, c);
		
		c.gridx = 2;
		c.gridy = 1;
		c.insets  = new Insets(0, 0, 5, 10);
		genericEditDistancePanel.add(loadIndelScoresRadioButton, c);
		
		c.gridx = 3;
		c.gridy = 1;
		c.insets  = new Insets(0, 0, 5, 10);
		genericEditDistancePanel.add(chooseIndelRightGivenLeftScoreFileButton, c);
		
		c.gridx = 4;
		c.gridy = 1;
		c.insets  = new Insets(0, 0, 5, 10);
		genericEditDistancePanel.add(indelRightGivenLeftScoreFileNameLabel, c);
	}
	
	public void setFeatureType(GTMFeatureType featureType){
		this.featureType = featureType;
		if(featureType.equals(GTMFeatureType.WholeTrace)){
			similarityMetricRadioButton.setVisible(false);
			similarityMetricPanel.setVisible(false);
			distanceMetricRadioButton.setSelected(true);
			distanceMetricPanel.setVisible(true);
			euclideanDistanceRadioButton.setVisible(false);
//			hammingDistanceRadioButton.setVisible(false);
			levenshteinDistanceRadioButton.setVisible(true);
			genericEditDistanceRadioButton.setVisible(true);
			genericEditDistancePanel.setVisible(true);
			genericEditDistanceRadioButton.setSelected(true);
		}else{
			similarityMetricRadioButton.setVisible(true);
			similarityMetricPanel.setVisible(similarityMetricRadioButton.isSelected());
			distanceMetricPanel.setVisible(distanceMetricRadioButton.isSelected());
//			similarityMetricRadioButton.setSelected(false);
			genericEditDistanceRadioButton.setVisible(false);
			genericEditDistancePanel.setVisible(false);
			levenshteinDistanceRadioButton.setVisible(false);
			euclideanDistanceRadioButton.setVisible(true);
//			hammingDistanceRadioButton.setVisible(true);
			euclideanDistanceRadioButton.setSelected(true);
		}
	}
	
	public void setListener(GuideTreeMinerSettingsListener listener){
		this.listener = listener;
	}
	
	public void readSettings() {
		if(similarityMetricRadioButton.isSelected()){
			listener.setSimilarityDistanceType(SimilarityDistanceMetricType.Similarity);
			if(fScoreSimilarityRadioButton.isSelected())
				listener.setSimilarityMetricType(SimilarityMetricType.FScore);
		}else{
			listener.setSimilarityDistanceType(SimilarityDistanceMetricType.Distance);
			if(euclideanDistanceRadioButton.isSelected())
				listener.setDistanceMetricType(DistanceMetricType.Euclidean);
			else if(hammingDistanceRadioButton.isSelected())
				listener.setDistanceMetricType(DistanceMetricType.Hamming);
			else if(levenshteinDistanceRadioButton.isSelected()){
				listener.setDistanceMetricType(DistanceMetricType.LevenshteinDistance);
			}else if(genericEditDistanceRadioButton.isSelected()){
				listener.setDistanceMetricType(DistanceMetricType.GenericEditDistance);
				listener.setIsDeriveSubstitutionScores(isDeriveSubstitutionScore);
				listener.setIsDeriveIndelScores(isDeriveIndelScore);
				listener.setSubstitutionScoreFileName(substitutionScoreFileName);
				listener.setIndelScoreFileName(indelScoreFileName);
			}
		}
	}

}
