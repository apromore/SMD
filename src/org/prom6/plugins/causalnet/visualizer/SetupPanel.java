package org.prom6.plugins.causalnet.visualizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.prom6.models.causalnet.CausalNetAnnotations;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class SetupPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8408305033071764421L;
	private final AnnotatedVisualizationSettings settings;

	// ----------------------------------

	private JPanel nodesPanel, edgesPanel;
	private JLabel nodesTitle, edgesTitle;

	private JLabel n2, n3;
	private JLabel e1, e2;

	private JCheckBox nColor;
	private JComboBox nMeasure;

	private JCheckBox eColor;
	private JComboBox eMeasure;

	public SetupPanel(final AnnotatedVisualizationSettings settings, final AnnotatedScalableView panel) {
		
		SlickerFactory factory = SlickerFactory.instance();

		this.settings = settings;

		this.setLayout(null);

		this.nodesTitle = factory.createLabel("Events");
		this.nodesTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD,
				18));
		this.nodesTitle.setForeground(new Color(40, 40, 40));

		this.nodesPanel = factory.createRoundedPanel(15, Color.gray);
		this.nodesPanel.setLayout(null);

		this.n2 = factory.createLabel("Color scaling:");
		this.n2.setHorizontalAlignment(SwingConstants.RIGHT);
		this.n2.setForeground(new Color(40, 40, 40));
		this.n3 = factory.createLabel("Measure:");
		this.n3.setHorizontalAlignment(SwingConstants.RIGHT);
		this.n3.setForeground(new Color(40, 40, 40));

		this.nColor = factory.createCheckBox("", settings.isColorScalingEvents());
		this.nColor.setBackground(Color.GRAY);
		this.nColor.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				settings.setColorScalingEvents(nColor.isSelected());
				panel.redraw(settings);
			}
		});
		this.nMeasure = factory.createComboBox(new String[] { "None",
				"End Counter", "Frequency", "Start Counter" });
		this.nMeasure.setSelectedIndex(2);
		this.nMeasure.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				switch (nMeasure.getSelectedIndex()){

					case 1: {settings.setMeasureEvents(CausalNetAnnotations.counterEndTask); break;}
					case 2: {settings.setMeasureEvents(CausalNetAnnotations.counterTask); break;}
					case 3: {settings.setMeasureEvents(CausalNetAnnotations.counterStartTask); break;}
					default: {settings.setMeasureEvents(""); break;}
				}
				
//				settings.setMeasureEvents((String) nMeasure.getSelectedItem());
				panel.redraw(settings);
			}
		});

		this.nodesPanel.add(this.nodesTitle);
		this.nodesPanel.add(this.n2);
		this.nodesPanel.add(this.n3);
		this.nodesPanel.add(this.nColor);
		this.nodesPanel.add(this.nMeasure);

		this.nodesPanel.setBounds(0, 0, 255, 120);
		this.nodesTitle.setBounds(10, 10, 100, 30);
		this.n2.setBounds(20, 50, 90, 20);
		this.n3.setBounds(20, 80, 90, 20);
		this.nColor.setBounds(115, 50, 25, 20);
		this.nMeasure.setBounds(115, 80, 120, 20);

		// -------------------------------------------

		this.edgesTitle = factory.createLabel("Transitions");
		this.edgesTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD,
				18));
		this.edgesTitle.setForeground(new Color(40, 40, 40));

		this.e1 = factory.createLabel("Color scaling:");
		this.e1.setHorizontalAlignment(SwingConstants.RIGHT);
		this.e1.setForeground(new Color(40, 40, 40));
		this.e2 = factory.createLabel("Measure:");
		this.e2.setHorizontalAlignment(SwingConstants.RIGHT);
		this.e2.setForeground(new Color(40, 40, 40));

		this.eColor = factory.createCheckBox("", settings.isColorScalingTransitions());
		this.eColor.setBackground(Color.GRAY);
		this.eColor.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				settings.setColorScalingTransitions(eColor.isSelected());
				panel.redraw(settings);
			}
		});
		this.eMeasure = factory.createComboBox(new String[] { "None",
				"Dependency", "LDDependency" });
		this.eMeasure.setSelectedIndex(0);
		this.eMeasure.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				switch (eMeasure.getSelectedIndex()){
					
					case 1: {settings.setMeasureTransitions(CausalNetAnnotations.directDependency); break;}
					case 2: {settings.setMeasureTransitions(CausalNetAnnotations.longDistanceDependency); break;}
					default: {settings.setMeasureTransitions(""); break;}
				}
				
//				settings.setMeasureTransitions((String) eMeasure.getSelectedItem());
				panel.redraw(settings);
			}
		});

		this.edgesPanel = factory.createRoundedPanel(15, Color.gray);
		this.edgesPanel.setLayout(null);

		this.edgesPanel.add(this.edgesTitle);
		this.edgesPanel.add(this.e1);
		this.edgesPanel.add(this.e2);
		this.edgesPanel.add(this.eColor);
		this.edgesPanel.add(this.eMeasure);

		this.edgesPanel.setBounds(0, 125, 255, 120);
		this.edgesTitle.setBounds(10, 10, 100, 30);
		this.e1.setBounds(20, 50, 90, 20);
		this.e2.setBounds(20, 80, 90, 20);
		this.eColor.setBounds(115, 50, 25, 20);
		this.eMeasure.setBounds(115, 80, 120, 20);

		this.add(this.nodesPanel);
		this.add(this.edgesPanel);

		this.setBackground(Color.LIGHT_GRAY);
		this.setPreferredSize(new Dimension(255, 245));
	}

	public AnnotatedVisualizationSettings getSettings() { return this.settings; }
}

