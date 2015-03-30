package org.prom6.plugins.causalnet.miner.gui;



import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.prom6.plugins.causalnet.miner.settings.HeuristicsMinerSettings;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ParametersPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6804610998661748174L;

	private HeuristicsMinerSettings settings;
	
	private JPanel thresholdsPanel, optionsPanel;
	
	private JLabel thresholdTitle, optionsTitle;
	private JLabel l1, l2, l3, l4, l5, l6, l7, l8;
	private NiceIntegerSlider t1, t2, t3, t4, t5;
	private JCheckBox c1, c2, c3;

	public ParametersPanel(){
		
		this.settings = new HeuristicsMinerSettings();
		this.init();
	}
	
	public ParametersPanel(HeuristicsMinerSettings settings){
		
		this.settings = settings;
		this.init();
	}
	
	private void init(){

		SlickerFactory factory = SlickerFactory.instance();
		SlickerDecorator decorator = SlickerDecorator.instance();
		
		this.thresholdsPanel = factory.createRoundedPanel(15, Color.gray);
		this.optionsPanel = factory.createRoundedPanel(15, Color.gray);
		
		this.thresholdTitle = factory.createLabel("Thresholds");
		this.thresholdTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 18));
		this.thresholdTitle.setForeground(new Color(40,40,40));
		
		this.optionsTitle = factory.createLabel("Options");
		this.optionsTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 18));
		this.optionsTitle.setForeground(new Color(40,40,40));
		
		this.t1 = factory.createNiceIntegerSlider("", 0, 100, (int) (this.settings.getRelativeToBestThreshold() * 100), Orientation.HORIZONTAL);
		this.t2 = factory.createNiceIntegerSlider("", 0, 100, (int) (this.settings.getDependencyThreshold() * 100), Orientation.HORIZONTAL);
		this.t3 = factory.createNiceIntegerSlider("", 0, 100, (int) (this.settings.getL1lThreshold() * 100), Orientation.HORIZONTAL);
		this.t4 = factory.createNiceIntegerSlider("", 0, 100, (int) (this.settings.getL2lThreshold() * 100), Orientation.HORIZONTAL);
		this.t5 = factory.createNiceIntegerSlider("", 0, 100, (int) (this.settings.getLongDistanceThreshold() * 100), Orientation.HORIZONTAL);
		
		this.l1 = factory.createLabel("Relative-to-best");
		this.l1.setHorizontalAlignment(SwingConstants.RIGHT);
		this.l1.setForeground(new Color(40,40,40));
		this.l2 = factory.createLabel("Dependency");
		this.l2.setHorizontalAlignment(SwingConstants.RIGHT);
		this.l2.setForeground(new Color(40,40,40));
		this.l3 = factory.createLabel("Length-one-loops");
		this.l3.setHorizontalAlignment(SwingConstants.RIGHT);
		this.l3.setForeground(new Color(40,40,40));
		this.l4 = factory.createLabel("Length-two-loops");
		this.l4.setHorizontalAlignment(SwingConstants.RIGHT);
		this.l4.setForeground(new Color(40,40,40));
		this.l5 = factory.createLabel("Long distance");
		this.l5.setHorizontalAlignment(SwingConstants.RIGHT);
		this.l5.setForeground(new Color(40,40,40));
		this.l6 = factory.createLabel("All tasks connected:");
		this.l6.setHorizontalAlignment(SwingConstants.RIGHT);
		this.l6.setForeground(new Color(40,40,40));
		this.l7 = factory.createLabel("Long distance dependencies:");
		this.l7.setHorizontalAlignment(SwingConstants.RIGHT);
		this.l7.setForeground(new Color(40,40,40));
		this.l8 = factory.createLabel("Unique start and end tasks:");
		this.l8.setHorizontalAlignment(SwingConstants.RIGHT);
		this.l8.setForeground(new Color(40,40,40));
		
		this.c1 = new JCheckBox();
		this.c1.setBackground(Color.GRAY);
		this.c1.setSelected(settings.isUseAllConnectedHeuristics());
		decorator.decorate(this.c1);
		this.c2 = new JCheckBox();
		this.c2.setBackground(Color.GRAY);
		this.c2.setSelected(settings.isUseLongDistanceDependency());
		decorator.decorate(this.c2);
		this.c3 = new JCheckBox();
		this.c3.setBackground(Color.GRAY);
		this.c3.setSelected(settings.isUseUniqueStartEndTasks());
		decorator.decorate(this.c3);
				
		this.thresholdsPanel.setLayout(null);
		this.thresholdsPanel.add(this.thresholdTitle);
		this.thresholdsPanel.add(this.l1);
		this.thresholdsPanel.add(this.t1);
		this.thresholdsPanel.add(this.l2);
		this.thresholdsPanel.add(this.t2);
		this.thresholdsPanel.add(this.l3);
		this.thresholdsPanel.add(this.t3);
		this.thresholdsPanel.add(this.l4);
		this.thresholdsPanel.add(this.t4);
		this.thresholdsPanel.add(this.l5);
		this.thresholdsPanel.add(this.t5);
		
		this.optionsPanel.setLayout(null);
		this.optionsPanel.add(this.optionsTitle);
		this.optionsPanel.add(this.l6);
		this.optionsPanel.add(this.c1);
		this.optionsPanel.add(this.l7);
		this.optionsPanel.add(this.c2);
		this.optionsPanel.add(this.l8);
		this.optionsPanel.add(this.c3);
		
		this.thresholdsPanel.setBounds(0, 0, 520, 210);
		this.thresholdTitle.setBounds(10, 10, 200, 30);
		this.l1.setBounds(20, 50, 100, 20);
		this.l2.setBounds(20, 80, 100, 20);
		this.l3.setBounds(20, 110, 100, 20);
		this.l4.setBounds(20, 140, 100, 20);
		this.l5.setBounds(20, 170, 100, 20);
		this.t1.setBounds(122, 50, 360, 20);
		this.t2.setBounds(122, 80, 360, 20);
		this.t3.setBounds(122, 110, 360, 20);
		this.t4.setBounds(122, 140, 360, 20);
		this.t5.setBounds(122, 170, 360, 20);
		
		this.optionsPanel.setBounds(0, 220, 520, 150);
		this.optionsTitle.setBounds(10, 10, 200, 30);
		this.l6.setBounds(20, 50, 170, 20);
		this.l7.setBounds(20, 80, 170, 20);
		this.l8.setBounds(20, 110, 170, 20);
		this.c1.setBounds(192, 50, 25, 20);
		this.c2.setBounds(192, 80, 25, 20);
		this.c3.setBounds(192, 110, 25, 20);

		this.setBackground(Color.LIGHT_GRAY);
		
		this.setLayout(null);
		this.add(this.thresholdsPanel);
		this.add(this.optionsPanel);
		this.validate();
		this.repaint();
	}
	
	public void copySettings(HeuristicsMinerSettings settings){
		
		this.t1.setValue((int) (settings.getRelativeToBestThreshold() * 100d));
		this.t2.setValue((int) (settings.getDependencyThreshold() * 100d));
		this.t3.setValue((int) (settings.getL1lThreshold() * 100d));
		this.t4.setValue((int) (settings.getL2lThreshold() * 100d));
		this.t5.setValue((int) (settings.getLongDistanceThreshold() * 100d));
		this.c1.setSelected(settings.isUseAllConnectedHeuristics());
		this.c2.setSelected(settings.isUseLongDistanceDependency());
		this.c3.setSelected(settings.isUseUniqueStartEndTasks());
	}
	
	public HeuristicsMinerSettings getSettings(){ 
		
		this.settings.setRelativeToBestThreshold(this.t1.getValue() / 100d);
		this.settings.setDependencyThreshold(this.t2.getValue() / 100d);
		this.settings.setL1lThreshold(this.t3.getValue() / 100d);
		this.settings.setL2lThreshold(this.t4.getValue() / 100d);
		this.settings.setLongDistanceThreshold(this.t5.getValue() / 100d);
		this.settings.setUseAllConnectedHeuristics(this.c1.isSelected());
		this.settings.setUseLongDistanceDependency(this.c2.isSelected());
		this.settings.setUseUniqueStartEndTasks(this.c3.isSelected());
		
		return this.settings; 
	}
	
	public void setEnabled(boolean status){
		
		this.t1.setEnabled(status);
		this.t2.setEnabled(status);
		this.t3.setEnabled(status);
		this.t4.setEnabled(status);
		this.t5.setEnabled(status);
		this.c1.setEnabled(status);
		this.c2.setEnabled(status);
		this.c3.setEnabled(status);
	}
	
	public boolean equals(HeuristicsMinerSettings settings){
		
		boolean equals = false;
		
		if(settings.getRelativeToBestThreshold() == (this.t1.getValue() / 100d)){
		
			if(settings.getDependencyThreshold() == (this.t2.getValue() / 100d)){
				
				if(settings.getL1lThreshold() == (this.t3.getValue() / 100d)){
					
					if(settings.getL2lThreshold() == (this.t4.getValue() / 100d)){
						
						if(settings.getLongDistanceThreshold() == (this.t5.getValue() / 100d)) equals = true;
					}
				}
			}
		}
		
		if(equals){
			
			if(settings.isUseAllConnectedHeuristics() != this.c1.isSelected()) return false;
			if(settings.isUseLongDistanceDependency() != this.c2.isSelected()) return false;
			if(settings.isUseUniqueStartEndTasks() != this.c3.isSelected()) return false;
		}
		
		return equals;
	}
	
	public void resize(boolean bigSize){
		
		int offset = 0;
	
		if(!bigSize) offset = 205;
		
		this.thresholdsPanel.setBounds(0, 0, 520 - offset, 210);
		this.optionsPanel.setBounds(0, 220, 520 - offset, 150);

		this.t1.setBounds(122, 50, 360 - offset, 20);
		this.t2.setBounds(122, 80, 360 - offset, 20);
		this.t3.setBounds(122, 110, 360 - offset, 20);
		this.t4.setBounds(122, 140, 360 - offset, 20);
		this.t5.setBounds(122, 170, 360 - offset, 20);
	}
}
