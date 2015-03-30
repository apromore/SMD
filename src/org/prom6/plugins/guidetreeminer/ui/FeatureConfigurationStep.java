package org.prom6.plugins.guidetreeminer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.prom6.plugins.guidetreeminer.settings.GuideTreeMinerSettingsListener;
import org.prom6.plugins.guidetreeminer.swingx.ScrollableGridLayout;
import org.prom6.plugins.guidetreeminer.types.GTMFeatureType;

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
public class FeatureConfigurationStep extends myStep {
	JPanel featureTypePanel;
	JPanel sequenceFeaturePanel;
	JPanel alphabetFeaturePanel;
	JPanel featureCountTypePanel;
	
	JPanel featureSelectionPanel;
	
	JRadioButton wholeTraceRadioButton, sequenceFeatureTypeRadioButton, alphabetFeatureTypeRadioButton;
	JCheckBox chooseBaseFeatureCheckBox;
	
	JCheckBox individualEventSequenceCheckBox;
	JCheckBox kGramCheckBox;
	JCheckBox tandemRepeatSequenceCheckBox;
	JCheckBox maximalRepeatSequenceCheckBox;
	JCheckBox superMaximalRepeatSequenceCheckBox;
	JCheckBox nearSuperMaximalRepeatSequenceCheckBox;
	
	JCheckBox individualEventAlphabetCheckBox;
	JCheckBox tandemRepeatAlphabetCheckBox;
	JCheckBox maximalRepeatAlphabetCheckBox;
	JCheckBox superMaximalRepeatAlphabetCheckBox;
	JCheckBox nearSuperMaximalRepeatAlphabetCheckBox;
	
	JRadioButton nominalFeatureRadioButton, numericFeatureRadioButton;
	

	JCheckBox frequencyCountCheckBox, instanceCountCheckBox, minAlphabetSizeCheckBox, maxAlphabetSizeCheckBox;
	JSlider frequencyCountSlider, instanceSlider, minAlphabetSizeSlider, maxAlphabetSizeSlider;
	JLabel frequencyCountLabel, instanceThresholdLabel, minAlphabetSizeThresholdLabel, maxAlphabetSizeThresholdLabel;
	
	int kGramValue;
	int minInstanceCountPercentageThreshold;
	int minFrequencyThreshold;
	int minAlphabetSizeThresholdValue, maxAlphabetSizeThresholdValue;
	
	GuideTreeMinerSettingsListener listener;
	
	public FeatureConfigurationStep(){
		initComponents();
	}
	
	private void initComponents(){
		final ScrollableGridLayout featureConfigurationLayout = new ScrollableGridLayout(this, 1, 10, 0, 0);
		
		featureConfigurationLayout.setRowFixed(0, true);
		featureConfigurationLayout.setRowFixed(1, true);
		
		this.setLayout(featureConfigurationLayout);
		
		JLabel headerLabel = SlickerFactory.instance().createLabel("<html><h1>Feature Configuration Step</h1>");
		featureConfigurationLayout.setPosition(headerLabel, 0, 0);
		add(headerLabel);
		
		buildFeatureTypePanel();
		featureConfigurationLayout.setPosition(featureTypePanel, 0, 1);
		add(featureTypePanel);
		
		Component verticalStrut1 = Box.createVerticalStrut(10);
		featureConfigurationLayout.setPosition(verticalStrut1, 0, 3);
		add(verticalStrut1);
		
		buildSequenceFeaturePanel();
		buildAlphabetFeaturePanel();
		
		featureConfigurationLayout.setPosition(sequenceFeaturePanel, 0, 4);
		add(sequenceFeaturePanel);
		
		featureConfigurationLayout.setPosition(alphabetFeaturePanel, 0, 5);
		add(alphabetFeaturePanel);
		
		Component verticalStrut2 = Box.createVerticalStrut(10);
		featureConfigurationLayout.setPosition(verticalStrut2, 0, 6);
		add(verticalStrut2);
		
		buildFeatureCountTypePanel();
		featureConfigurationLayout.setPosition(featureCountTypePanel, 0, 7);
		add(featureCountTypePanel);
		
		Component verticalStrut3 = Box.createVerticalStrut(10);
		featureConfigurationLayout.setPosition(verticalStrut3, 0, 8);
		add(verticalStrut3);
		
		buildFeatureSelectionPanel();
		featureConfigurationLayout.setPosition(featureSelectionPanel, 0, 9);
		add(featureSelectionPanel);	
	}
	
	private void buildFeatureTypePanel(){
		featureTypePanel = SlickerFactory.instance().createRoundedPanel();
		featureTypePanel.setBorder(BorderFactory.createTitledBorder("Select Feature Type"));
		
		ScrollableGridLayout featureTypePanelLayout = new ScrollableGridLayout(featureTypePanel,1,4,0,0);
		featureTypePanelLayout.setRowFixed(0, true);
		featureTypePanelLayout.setRowFixed(1, true);
		featureTypePanelLayout.setRowFixed(2, true);
		featureTypePanelLayout.setRowFixed(3, true);
		
		featureTypePanel.setLayout(featureTypePanelLayout);
		
		wholeTraceRadioButton = SlickerFactory.instance().createRadioButton("Entire Trace");
		wholeTraceRadioButton.setSelected(false);
		
		sequenceFeatureTypeRadioButton = SlickerFactory.instance().createRadioButton("Sequence Feature");
		sequenceFeatureTypeRadioButton.setSelected(true);
		alphabetFeatureTypeRadioButton = SlickerFactory.instance().createRadioButton("Alphabet Feature");
		
		ButtonGroup featureTypeButtonGroup = new ButtonGroup();
		featureTypeButtonGroup.add(wholeTraceRadioButton);
		featureTypeButtonGroup.add(sequenceFeatureTypeRadioButton);
		featureTypeButtonGroup.add(alphabetFeatureTypeRadioButton);
		
		featureTypePanelLayout.setPosition(wholeTraceRadioButton, 0, 0);
		featureTypePanel.add(wholeTraceRadioButton);
		
		featureTypePanelLayout.setPosition(sequenceFeatureTypeRadioButton, 0, 1);
		featureTypePanel.add(sequenceFeatureTypeRadioButton);
		
		featureTypePanelLayout.setPosition(alphabetFeatureTypeRadioButton, 0, 2);
		featureTypePanel.add(alphabetFeatureTypeRadioButton);
		
		chooseBaseFeatureCheckBox = SlickerFactory.instance().createCheckBox("Consider Base Features", true);
		featureTypePanelLayout.setPosition(chooseBaseFeatureCheckBox, 0, 3);
		featureTypePanel.add(chooseBaseFeatureCheckBox);
		
		wholeTraceRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(wholeTraceRadioButton.isSelected()){
					alphabetFeaturePanel.setVisible(false);
					sequenceFeaturePanel.setVisible(false);
					featureCountTypePanel.setVisible(false);
					chooseBaseFeatureCheckBox.setVisible(false);
					featureSelectionPanel.setVisible(false);
					listener.setFeatureTypeOption(GTMFeatureType.WholeTrace);
					revalidate();
					repaint();
				}
			}
		});
		sequenceFeatureTypeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(sequenceFeatureTypeRadioButton.isSelected()){
					alphabetFeaturePanel.setVisible(false);
					sequenceFeaturePanel.setVisible(true);
					featureCountTypePanel.setVisible(true);
					chooseBaseFeatureCheckBox.setVisible(true);
					featureSelectionPanel.setVisible(true);
					adjustAlphabetSizeSelection();
					listener.setFeatureTypeOption(GTMFeatureType.Sequence);
					revalidate();
					repaint();
				}
			}
		});
		
		alphabetFeatureTypeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(alphabetFeatureTypeRadioButton.isSelected()){
					sequenceFeaturePanel.setVisible(false);
					alphabetFeaturePanel.setVisible(true);
					featureCountTypePanel.setVisible(true);
					chooseBaseFeatureCheckBox.setVisible(true);
					featureSelectionPanel.setVisible(true);
					listener.setFeatureTypeOption(GTMFeatureType.Alphabet);
					adjustAlphabetSizeSelection();
					revalidate();
					repaint();
				}
				
			}
		});
	}
	
	private void buildSequenceFeaturePanel(){
		sequenceFeaturePanel = SlickerFactory.instance().createRoundedPanel();
		sequenceFeaturePanel.setBorder(BorderFactory.createTitledBorder("Select Sequence Features"));
		
		ScrollableGridLayout sequenceFeaturePanelLayout = new ScrollableGridLayout(sequenceFeaturePanel, 2, 6, 0, 0);
		sequenceFeaturePanelLayout.setColumnFixed(0, true);
		
		sequenceFeaturePanelLayout.setRowFixed(0, true);
		sequenceFeaturePanelLayout.setRowFixed(1, true);
		sequenceFeaturePanelLayout.setRowFixed(2, true);
		sequenceFeaturePanelLayout.setRowFixed(3, true);
		sequenceFeaturePanelLayout.setRowFixed(4, true);
		sequenceFeaturePanelLayout.setRowFixed(5, true);
		sequenceFeaturePanel.setLayout(sequenceFeaturePanelLayout);
		
		
		final JPanel kGramPanel = SlickerFactory.instance().createRoundedPanel();
		final JSlider kGramValueSlider = SlickerFactory.instance().createSlider(JSlider.HORIZONTAL);
		final JLabel kGramValueLabel = SlickerFactory.instance().createLabel("<HTML>  3  </HTML>");
		kGramValueSlider.setMinimum(2);
		kGramValueSlider.setMaximum(6);
		kGramValueSlider.setValue(3);
		kGramValue = 3;
		kGramPanel.add(kGramValueSlider);
		kGramPanel.add(kGramValueLabel);
		kGramPanel.setVisible(false);
		
		
		individualEventSequenceCheckBox = SlickerFactory.instance().createCheckBox("Individual Event", false); 
		kGramCheckBox = SlickerFactory.instance().createCheckBox("K-Gram", false);
		tandemRepeatSequenceCheckBox = SlickerFactory.instance().createCheckBox("Tandem Repeat", false);
		maximalRepeatSequenceCheckBox = SlickerFactory.instance().createCheckBox("Maximal Repeat", false);
		superMaximalRepeatSequenceCheckBox = SlickerFactory.instance().createCheckBox("Super Maximal Repeat", false);
		nearSuperMaximalRepeatSequenceCheckBox = SlickerFactory.instance().createCheckBox("Near Super Maximal Repeat", false);
		
		tandemRepeatSequenceCheckBox.setSelected(true);
		maximalRepeatSequenceCheckBox.setSelected(true);
		superMaximalRepeatSequenceCheckBox.setEnabled(false);
		nearSuperMaximalRepeatSequenceCheckBox.setEnabled(false);
		
		final Color enabledForeGroundColor = maximalRepeatSequenceCheckBox.getForeground();
		final Color disabledForeGroundColor = Color.gray; 
		superMaximalRepeatSequenceCheckBox.setForeground(disabledForeGroundColor);
		nearSuperMaximalRepeatSequenceCheckBox.setForeground(disabledForeGroundColor);
		
		sequenceFeaturePanelLayout.setPosition(individualEventSequenceCheckBox, 0, 0);
		sequenceFeaturePanel.add(individualEventSequenceCheckBox);
		
		sequenceFeaturePanelLayout.setPosition(kGramCheckBox, 0, 1);
		sequenceFeaturePanel.add(kGramCheckBox);
		
		sequenceFeaturePanelLayout.setPosition(kGramPanel, 1, 1);
		sequenceFeaturePanel.add(kGramPanel);
		
		sequenceFeaturePanelLayout.setPosition(tandemRepeatSequenceCheckBox, 0, 2);
		sequenceFeaturePanel.add(tandemRepeatSequenceCheckBox);
		
		sequenceFeaturePanelLayout.setPosition(maximalRepeatSequenceCheckBox, 0, 3);
		sequenceFeaturePanel.add(maximalRepeatSequenceCheckBox);
		
		sequenceFeaturePanelLayout.setPosition(superMaximalRepeatSequenceCheckBox, 0, 4);
		sequenceFeaturePanel.add(superMaximalRepeatSequenceCheckBox);
		
		sequenceFeaturePanelLayout.setPosition(nearSuperMaximalRepeatSequenceCheckBox, 0, 5);
		sequenceFeaturePanel.add(nearSuperMaximalRepeatSequenceCheckBox);
		
		kGramCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(kGramCheckBox.isSelected()){
					kGramPanel.setVisible(true);
				}else{
					kGramPanel.setVisible(false);
				}
				revalidate();
				repaint();
			}
		});
		
		kGramValueSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(!kGramValueSlider.getValueIsAdjusting()){
					kGramValue = kGramValueSlider.getValue();
					kGramValueLabel.setText("<HTML>  "+kGramValue+"  </HTML>");
				}
			}
		});
		
		maximalRepeatSequenceCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(maximalRepeatSequenceCheckBox.isSelected()){
					superMaximalRepeatSequenceCheckBox.setSelected(false);
					nearSuperMaximalRepeatSequenceCheckBox.setSelected(false);
					superMaximalRepeatSequenceCheckBox.setEnabled(false);
					nearSuperMaximalRepeatSequenceCheckBox.setEnabled(false);
					superMaximalRepeatSequenceCheckBox.setForeground(disabledForeGroundColor);
					nearSuperMaximalRepeatSequenceCheckBox.setForeground(disabledForeGroundColor);
				}else{
					superMaximalRepeatSequenceCheckBox.setEnabled(true);
					nearSuperMaximalRepeatSequenceCheckBox.setEnabled(true);
					superMaximalRepeatSequenceCheckBox.setForeground(enabledForeGroundColor);
					nearSuperMaximalRepeatSequenceCheckBox.setForeground(enabledForeGroundColor);
				}
			}
		});
		
		nearSuperMaximalRepeatSequenceCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(nearSuperMaximalRepeatSequenceCheckBox.isSelected()){
					superMaximalRepeatSequenceCheckBox.setSelected(false);
					superMaximalRepeatSequenceCheckBox.setEnabled(false);
					superMaximalRepeatSequenceCheckBox.setForeground(disabledForeGroundColor);
				}else{
					superMaximalRepeatSequenceCheckBox.setEnabled(true);
					superMaximalRepeatSequenceCheckBox.setForeground(enabledForeGroundColor);
				}
			}
		});
	}
	
	private void buildAlphabetFeaturePanel(){
		alphabetFeaturePanel = SlickerFactory.instance().createRoundedPanel();
		alphabetFeaturePanel.setBorder(BorderFactory.createTitledBorder("Select Alphabet Features"));
		
		ScrollableGridLayout alphabetFeaturePanelLayout = new ScrollableGridLayout(alphabetFeaturePanel, 1, 5, 0, 0);
		alphabetFeaturePanelLayout.setColumnFixed(0, true);
		
		alphabetFeaturePanelLayout.setRowFixed(0, true);
		alphabetFeaturePanelLayout.setRowFixed(1, true);
		alphabetFeaturePanelLayout.setRowFixed(2, true);
		alphabetFeaturePanelLayout.setRowFixed(3, true);
		alphabetFeaturePanelLayout.setRowFixed(4, true);
		
		alphabetFeaturePanel.setLayout(alphabetFeaturePanelLayout);
		
		individualEventAlphabetCheckBox = SlickerFactory.instance().createCheckBox("Individual Event", false); 
		tandemRepeatAlphabetCheckBox = SlickerFactory.instance().createCheckBox("Tandem Repeat Alphabet", false);
		maximalRepeatAlphabetCheckBox = SlickerFactory.instance().createCheckBox("Maximal Repeat Alphabet", false);
		superMaximalRepeatAlphabetCheckBox = SlickerFactory.instance().createCheckBox("Super Maximal Repeat Alphabet", false);
		nearSuperMaximalRepeatAlphabetCheckBox = SlickerFactory.instance().createCheckBox("Near Super Maximal Repeat Alphabet", false);
		
		tandemRepeatAlphabetCheckBox.setSelected(true);
		maximalRepeatAlphabetCheckBox.setSelected(true);
		
		final Color enabledForeGroundColor = maximalRepeatAlphabetCheckBox.getForeground();
		final Color disabledForeGroundColor = Color.gray; 
		superMaximalRepeatAlphabetCheckBox.setForeground(disabledForeGroundColor);
		nearSuperMaximalRepeatAlphabetCheckBox.setForeground(disabledForeGroundColor);
		
		alphabetFeaturePanelLayout.setPosition(individualEventAlphabetCheckBox, 0, 0);
		alphabetFeaturePanel.add(individualEventAlphabetCheckBox);
		
		alphabetFeaturePanelLayout.setPosition(tandemRepeatAlphabetCheckBox, 0, 1);
		alphabetFeaturePanel.add(tandemRepeatAlphabetCheckBox);
		
		alphabetFeaturePanelLayout.setPosition(maximalRepeatAlphabetCheckBox, 0, 2);
		alphabetFeaturePanel.add(maximalRepeatAlphabetCheckBox);
		
		alphabetFeaturePanelLayout.setPosition(superMaximalRepeatAlphabetCheckBox, 0, 3);
		alphabetFeaturePanel.add(superMaximalRepeatAlphabetCheckBox);
		
		alphabetFeaturePanelLayout.setPosition(nearSuperMaximalRepeatAlphabetCheckBox, 0, 4);
		alphabetFeaturePanel.add(nearSuperMaximalRepeatAlphabetCheckBox);
		
		maximalRepeatAlphabetCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(maximalRepeatAlphabetCheckBox.isSelected()){
					superMaximalRepeatAlphabetCheckBox.setSelected(false);
					nearSuperMaximalRepeatAlphabetCheckBox.setSelected(false);

					superMaximalRepeatAlphabetCheckBox.setEnabled(false);
					nearSuperMaximalRepeatAlphabetCheckBox.setEnabled(false);
					superMaximalRepeatAlphabetCheckBox.setForeground(disabledForeGroundColor);
					nearSuperMaximalRepeatAlphabetCheckBox.setForeground(disabledForeGroundColor);
				}else{
					superMaximalRepeatAlphabetCheckBox.setEnabled(true);
					nearSuperMaximalRepeatAlphabetCheckBox.setEnabled(true);
					superMaximalRepeatAlphabetCheckBox.setForeground(enabledForeGroundColor);
					nearSuperMaximalRepeatAlphabetCheckBox.setForeground(enabledForeGroundColor);
				}
			}
		});
		
		nearSuperMaximalRepeatAlphabetCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(nearSuperMaximalRepeatAlphabetCheckBox.isSelected()){
					superMaximalRepeatAlphabetCheckBox.setSelected(false);
					
					superMaximalRepeatAlphabetCheckBox.setEnabled(false);
					superMaximalRepeatAlphabetCheckBox.setForeground(disabledForeGroundColor);
				}else{
					superMaximalRepeatAlphabetCheckBox.setEnabled(true);
					superMaximalRepeatAlphabetCheckBox.setForeground(enabledForeGroundColor);
				}
			}
		});
		
		alphabetFeaturePanel.setVisible(false);
	}
	
	private void buildFeatureCountTypePanel(){
		featureCountTypePanel = SlickerFactory.instance().createRoundedPanel();
		featureCountTypePanel.setBorder(BorderFactory.createTitledBorder("Select Feature Count Type"));
	
		ScrollableGridLayout featureCountTypePanelLayout = new ScrollableGridLayout(featureCountTypePanel, 3, 1, 0, 0);

		featureCountTypePanelLayout.setColumnFixed(0, true);
		featureCountTypePanelLayout.setRowFixed(0, true);

		featureCountTypePanel.setLayout(featureCountTypePanelLayout);
		
		nominalFeatureRadioButton = SlickerFactory.instance().createRadioButton("Nominal");
		numericFeatureRadioButton = SlickerFactory.instance().createRadioButton("Numeric");
		
		
		ButtonGroup featureCountTypeButtonGroup = new ButtonGroup();
		featureCountTypeButtonGroup.add(nominalFeatureRadioButton);
		featureCountTypeButtonGroup.add(numericFeatureRadioButton);
		nominalFeatureRadioButton.setSelected(true);
		
		featureCountTypePanelLayout.setPosition(nominalFeatureRadioButton, 0, 0);
		featureCountTypePanel.add(nominalFeatureRadioButton);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		featureCountTypePanelLayout.setPosition(horizontalStrut, 1, 0);
		featureCountTypePanel.add(horizontalStrut);
		
		featureCountTypePanelLayout.setPosition(numericFeatureRadioButton, 2, 0);
		featureCountTypePanel.add(numericFeatureRadioButton);

		nominalFeatureRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(nominalFeatureRadioButton.isSelected()){
					
				}
			}
		});
		
		numericFeatureRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(numericFeatureRadioButton.isSelected()){
					
				}
			}
		});
	}
	
	private void buildFeatureSelectionPanel(){
		featureSelectionPanel = SlickerFactory.instance().createRoundedPanel();
		featureSelectionPanel.setBorder(BorderFactory.createTitledBorder("Filter Features"));
		
		ScrollableGridLayout featureSelectionPanelLayout = new ScrollableGridLayout(featureSelectionPanel, 3, 4, 10, 0);
		featureSelectionPanelLayout.setRowFixed(0, true);
		featureSelectionPanelLayout.setRowFixed(1, true);
		
		featureSelectionPanelLayout.setColumnFixed(0, true);
		featureSelectionPanelLayout.setColumnFixed(1, true);
		featureSelectionPanelLayout.setColumnFixed(2, true);
		
		featureSelectionPanel.setLayout(featureSelectionPanelLayout);
		
//		GridBagLayout featureSelectionGBLayout = new GridBagLayout();
//		featureSelectionPanel.setLayout(featureSelectionGBLayout);

//		final GridBagConstraints c = new GridBagConstraints();
//		c.fill = GridBagConstraints.HORIZONTAL;
//
//		c.gridwidth = 1;
		frequencyCountCheckBox = SlickerFactory.instance().createCheckBox("Frequency", false);
//		c.gridx = 0;
//		c.gridy = 0;
//		c.anchor = GridBagConstraints.LINE_START;
//		c.insets = new Insets(5, 0, 0, 10);
//		featureSelectionPanel.add(frequencyCountCheckBox, c);
		
		frequencyCountSlider = SlickerFactory.instance().createSlider(JSlider.HORIZONTAL);
		frequencyCountSlider.setMinimum(1);
		frequencyCountSlider.setMaximum(100);
		frequencyCountSlider.setValue(20);
		
		Dimension d = frequencyCountSlider.getPreferredSize();
		frequencyCountSlider.setPreferredSize(new Dimension(d.width / 2, d.height));
		frequencyCountSlider.setEnabled(false);
//		c.gridx = 1;
//		c.gridy = 0;
//		c.insets = new Insets(5, 0, 0, 10);
//		featureSelectionPanel.add(frequencyCountSlider, c);

		frequencyCountLabel = new JLabel("20");
		frequencyCountLabel.setEnabled(false);
//		c.gridx = 2;
//		c.gridy = 0;
//		c.insets = new Insets(5, 0, 0, 10);
//		featureSelectionPanel.add(frequencyCountLabel, c);

		featureSelectionPanelLayout.setPosition(frequencyCountCheckBox, 0, 0);
		featureSelectionPanel.add(frequencyCountCheckBox);
		
		featureSelectionPanelLayout.setPosition(frequencyCountSlider, 1, 0);
		featureSelectionPanel.add(frequencyCountSlider);
		
		featureSelectionPanelLayout.setPosition(frequencyCountLabel, 2, 0);
		featureSelectionPanel.add(frequencyCountLabel);
		
		frequencyCountCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (frequencyCountCheckBox.isSelected()) {
					frequencyCountSlider.setEnabled(true);
					frequencyCountLabel.setEnabled(true);
					minFrequencyThreshold = frequencyCountSlider.getValue();
				} else {
					frequencyCountSlider.setEnabled(false);
					frequencyCountLabel.setEnabled(false);
					minFrequencyThreshold = 0;
				}
			}

		});

		frequencyCountSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				frequencyCountLabel.setText(frequencyCountSlider.getValue() + "");
				minFrequencyThreshold = frequencyCountSlider.getValue();
			}
		});

		instanceCountCheckBox = SlickerFactory.instance().createCheckBox("Instance %", false);
	
//		c.gridx = 0;
//		c.gridy = 1;
//		c.insets = new Insets(5, 0, 0, 10);
//		featureSelectionPanel.add(instanceCountCheckBox, c);

		instanceSlider = SlickerFactory.instance().createSlider(JSlider.HORIZONTAL);
		instanceSlider.setMinimum(1);
		instanceSlider.setMaximum(100);
		instanceSlider.setValue(20);
		
		d = instanceSlider.getPreferredSize();
		instanceSlider.setPreferredSize(new Dimension(d.width / 2, d.height));
		instanceSlider.setEnabled(false);
//		c.gridx = 1;
//		c.gridy = 1;
//		c.insets = new Insets(5, 0, 0, 10);
//		featureSelectionPanel.add(instanceSlider, c);

		instanceThresholdLabel = new JLabel("10");
		instanceThresholdLabel.setEnabled(false);
//		c.gridx = 2;
//		c.gridy = 1;
//		c.insets = new Insets(5, 0, 0, 10);
//		featureSelectionPanel.add(instanceThresholdLabel, c);

		featureSelectionPanelLayout.setPosition(instanceCountCheckBox, 0, 1);
		featureSelectionPanel.add(instanceCountCheckBox);
		
		featureSelectionPanelLayout.setPosition(instanceSlider, 1, 1);
		featureSelectionPanel.add(instanceSlider);
		
		featureSelectionPanelLayout.setPosition(instanceThresholdLabel, 2, 1);
		featureSelectionPanel.add(instanceThresholdLabel);
		
		instanceCountCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (instanceCountCheckBox.isSelected()) {
					instanceSlider.setEnabled(true);
					instanceThresholdLabel.setEnabled(true);
					minInstanceCountPercentageThreshold = instanceSlider.getValue();
				} else {
					instanceSlider.setEnabled(false);
					instanceThresholdLabel.setEnabled(false);
					minInstanceCountPercentageThreshold = 0;
				}
			}
		});

		instanceSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				instanceThresholdLabel.setText(instanceSlider.getValue() + "");
				minInstanceCountPercentageThreshold = instanceSlider.getValue();
			}
		});

		minAlphabetSizeCheckBox = SlickerFactory.instance().createCheckBox("Min. Alphabet Size", false);
//		c.gridx = 0;
//		c.gridy = 2;
//		c.insets = new Insets(5, 0, 0, 10);
//		featureSelectionPanel.add(minAlphabetSizeCheckBox, c);

		minAlphabetSizeSlider = SlickerFactory.instance().createSlider(JSlider.HORIZONTAL);
		minAlphabetSizeSlider.setMinimum(0);
		minAlphabetSizeSlider.setMaximum(10);
		minAlphabetSizeSlider.setValue(0);
		
		d = minAlphabetSizeSlider.getPreferredSize();
		minAlphabetSizeSlider.setPreferredSize(new Dimension(d.width / 2, d.height));
		minAlphabetSizeSlider.setEnabled(false);
//		c.gridx = 1;
//		c.gridy = 2;
//		c.insets = new Insets(5, 0, 0, 10);
//		featureSelectionPanel.add(minAlphabetSizeSlider, c);

		minAlphabetSizeThresholdLabel = new JLabel("0");
		minAlphabetSizeThresholdLabel.setEnabled(false);
//		c.gridx = 2;
//		c.gridy = 2;
//		c.insets = new Insets(5, 0, 0, 10);
//		featureSelectionPanel.add(minAlphabetSizeThresholdLabel, c);

		featureSelectionPanelLayout.setPosition(minAlphabetSizeCheckBox, 0, 2);
		featureSelectionPanel.add(minAlphabetSizeCheckBox);
		
		featureSelectionPanelLayout.setPosition(minAlphabetSizeSlider, 1, 2);
		featureSelectionPanel.add(minAlphabetSizeSlider);
		
		featureSelectionPanelLayout.setPosition(minAlphabetSizeThresholdLabel, 2, 2);
		featureSelectionPanel.add(minAlphabetSizeThresholdLabel);
		
		minAlphabetSizeCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (minAlphabetSizeCheckBox.isSelected()) {
					minAlphabetSizeSlider.setEnabled(true);
					minAlphabetSizeThresholdLabel.setEnabled(true);
					minAlphabetSizeThresholdValue = minAlphabetSizeSlider.getValue();
				} else {
					minAlphabetSizeSlider.setEnabled(false);
					minAlphabetSizeThresholdLabel.setEnabled(false);
					minAlphabetSizeThresholdValue = 0;
				}
			}
		});

		minAlphabetSizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				minAlphabetSizeThresholdLabel.setText(minAlphabetSizeSlider.getValue() + "");
				minAlphabetSizeThresholdValue = minAlphabetSizeSlider.getValue();
			}
		});
		
		maxAlphabetSizeCheckBox = SlickerFactory.instance().createCheckBox("Max. Alphabet Size",false);
//		c.gridx = 0;
//		c.gridy = 3;
//		c.insets = new Insets(5, 0, 0, 10);
//		featureSelectionPanel.add(maxAlphabetSizeCheckBox, c);

		maxAlphabetSizeSlider = SlickerFactory.instance().createSlider(JSlider.HORIZONTAL);
		maxAlphabetSizeSlider.setMinimum(0);
		maxAlphabetSizeSlider.setMaximum(10);
		maxAlphabetSizeSlider.setValue(10);
		
		d = maxAlphabetSizeSlider.getPreferredSize();
		maxAlphabetSizeSlider.setPreferredSize(new Dimension(d.width / 2, d.height));
		maxAlphabetSizeSlider.setEnabled(false);
//		c.gridx = 1;
//		c.gridy = 3;
//		c.insets = new Insets(5, 0, 0, 10);
//		featureSelectionPanel.add(maxAlphabetSizeSlider, c);

		maxAlphabetSizeThresholdLabel = new JLabel("10");
		maxAlphabetSizeThresholdLabel.setEnabled(false);
//		c.gridx = 2;
//		c.gridy = 3;
//		c.insets = new Insets(5, 0, 0, 10);
//		featureSelectionPanel.add(maxAlphabetSizeThresholdLabel, c);

		featureSelectionPanelLayout.setPosition(maxAlphabetSizeCheckBox, 0, 3);
		featureSelectionPanel.add(maxAlphabetSizeCheckBox);
		
		featureSelectionPanelLayout.setPosition(maxAlphabetSizeSlider, 1, 3);
		featureSelectionPanel.add(maxAlphabetSizeSlider);
		
		featureSelectionPanelLayout.setPosition(maxAlphabetSizeThresholdLabel, 2, 3);
		featureSelectionPanel.add(maxAlphabetSizeThresholdLabel);
		
		maxAlphabetSizeCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (maxAlphabetSizeCheckBox.isSelected()) {
					maxAlphabetSizeSlider.setEnabled(true);
					maxAlphabetSizeThresholdLabel.setEnabled(true);
					maxAlphabetSizeThresholdValue = maxAlphabetSizeSlider.getValue();
				} else {
					maxAlphabetSizeSlider.setEnabled(false);
					maxAlphabetSizeThresholdLabel.setEnabled(false);
					maxAlphabetSizeThresholdValue = 10;
				}
			}
		});

		maxAlphabetSizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				maxAlphabetSizeThresholdLabel.setText(maxAlphabetSizeSlider.getValue() + "");
				maxAlphabetSizeThresholdValue = maxAlphabetSizeSlider.getValue();
			}
		});	
		
	}
	
	private void adjustAlphabetSizeSelection(){
		if(sequenceFeatureTypeRadioButton.isSelected()){
			minAlphabetSizeCheckBox.setEnabled(false);
			maxAlphabetSizeCheckBox.setEnabled(false);
			minAlphabetSizeSlider.setEnabled(false);
			maxAlphabetSizeSlider.setEnabled(false);
			minAlphabetSizeThresholdLabel.setEnabled(false);
			maxAlphabetSizeThresholdLabel.setEnabled(false);
		}else{
			minAlphabetSizeCheckBox.setEnabled(true);
			maxAlphabetSizeCheckBox.setEnabled(true);
			minAlphabetSizeSlider.setEnabled(true);
			maxAlphabetSizeSlider.setEnabled(true);
			minAlphabetSizeThresholdLabel.setEnabled(true);
			maxAlphabetSizeThresholdLabel.setEnabled(true);
		}
	}
	
	public boolean precondition()  {
		return true;
	}

	public void setListener(GuideTreeMinerSettingsListener listener){
		this.listener = listener;
	}
	
	@Override
	public void readSettings() {
		readSelectedFeatures();
		readFeatureSelectionOptions();
		listener.setFrequencyCount(nominalFeatureRadioButton.isSelected());
		listener.setBaseFeatures(chooseBaseFeatureCheckBox.isSelected());
	}
	
	private void readSelectedFeatures(){
		listener.clearFeatureSelection();
		if(sequenceFeatureTypeRadioButton.isSelected()){
			listener.setFeatureType(GTMFeatureType.Sequence);
			readSelectedSequenceFeatures();
		}else if(alphabetFeatureTypeRadioButton.isSelected()){
			listener.setFeatureType(GTMFeatureType.Alphabet);
			readSelectedAlphabetFeatures();
		}else{
			listener.setFeatureType(GTMFeatureType.WholeTrace);
		}
	}
	
	private void readSelectedSequenceFeatures(){
		if(individualEventSequenceCheckBox.isSelected()){
			listener.featureSelectionChanged(individualEventSequenceCheckBox.getText(), true);
		}else{
			listener.featureSelectionChanged(individualEventSequenceCheckBox.getText(), false);
		}
		
		if(kGramCheckBox.isSelected()){
			listener.featureSelectionChanged(kGramCheckBox.getText(), true);
			listener.setKGramValue(kGramValue);
		}else{
			listener.featureSelectionChanged(kGramCheckBox.getText(), false);
		}
		
		if(tandemRepeatSequenceCheckBox.isSelected()){
			listener.featureSelectionChanged(tandemRepeatSequenceCheckBox.getText(), true);
		}else{
			listener.featureSelectionChanged(tandemRepeatSequenceCheckBox.getText(), false);
		}
		
		if(maximalRepeatSequenceCheckBox.isSelected()){
			listener.featureSelectionChanged(maximalRepeatSequenceCheckBox.getText(), true);
		}else{
			listener.featureSelectionChanged(maximalRepeatSequenceCheckBox.getText(), false);
		}
		
		if(superMaximalRepeatSequenceCheckBox.isSelected()){
			listener.featureSelectionChanged(superMaximalRepeatSequenceCheckBox.getText(), true);
		}else{
			listener.featureSelectionChanged(superMaximalRepeatSequenceCheckBox.getText(), false);
		}
		
		if(nearSuperMaximalRepeatSequenceCheckBox.isSelected()){
			listener.featureSelectionChanged(nearSuperMaximalRepeatSequenceCheckBox.getText(), true);
		}else{
			listener.featureSelectionChanged(nearSuperMaximalRepeatSequenceCheckBox.getText(), false);
		}
	}
	
	private void readSelectedAlphabetFeatures(){
		if(individualEventAlphabetCheckBox.isSelected()){
			listener.featureSelectionChanged(individualEventAlphabetCheckBox.getText(), true);
		}else{
			listener.featureSelectionChanged(individualEventAlphabetCheckBox.getText(), false);
		}
		
		if(tandemRepeatAlphabetCheckBox.isSelected()){
			listener.featureSelectionChanged(tandemRepeatAlphabetCheckBox.getText(), true);
		}else{
			listener.featureSelectionChanged(tandemRepeatAlphabetCheckBox.getText(), false);
		}
		
		if(maximalRepeatAlphabetCheckBox.isSelected()){
			listener.featureSelectionChanged(maximalRepeatAlphabetCheckBox.getText(), true);
		}else{
			listener.featureSelectionChanged(maximalRepeatAlphabetCheckBox.getText(), false);
		}
		
		if(superMaximalRepeatAlphabetCheckBox.isSelected()){
			listener.featureSelectionChanged(superMaximalRepeatAlphabetCheckBox.getText(), true);
		}else{
			listener.featureSelectionChanged(superMaximalRepeatAlphabetCheckBox.getText(), false);
		}
		
		if(nearSuperMaximalRepeatAlphabetCheckBox.isSelected()){
			listener.featureSelectionChanged(nearSuperMaximalRepeatAlphabetCheckBox.getText(), true);
		}else{
			listener.featureSelectionChanged(nearSuperMaximalRepeatAlphabetCheckBox.getText(), false);
		}
	}

	private void readFeatureSelectionOptions(){
		if(frequencyCountCheckBox.isSelected()){
			listener.setMininumFrequencyCountThreshold(minFrequencyThreshold);
		}else{
			listener.setMininumFrequencyCountThreshold(0);
		}
		
		if(instanceCountCheckBox.isSelected()){
			listener.setMininumInstancePercentageCountThreshold(minInstanceCountPercentageThreshold);
		}else{
			listener.setMininumInstancePercentageCountThreshold(0);
		}
		
		if(minAlphabetSizeCheckBox.isSelected()){
			listener.setMinimumAlphabetSizeThreshold(minAlphabetSizeThresholdValue);
		}else{
			listener.setMinimumAlphabetSizeThreshold(0);
		}
		
		if(maxAlphabetSizeCheckBox.isSelected()){
			listener.setMaximumAlphabetSizeThreshold(maxAlphabetSizeThresholdValue);
		}else{
			listener.setMaximumAlphabetSizeThreshold(20);
		}
	}
}
