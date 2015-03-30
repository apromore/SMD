package org.prom6.plugins.causalnet.visualizer;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.freehep.util.export.ExportDialog;

import com.fluxicon.slickerbox.components.SlickerButton;

public class ExportPanel extends JPanel {

	private static final long serialVersionUID = 7153768335241741777L;

	private SlickerButton exportButton;
	
	protected final AnnotatedScalableView mainView;

	public ExportPanel(AnnotatedScalableView view){
		
		this.mainView = view;
		
		double size[][] = { { 10, TableLayoutConstants.FILL, 10 }, { 10, TableLayoutConstants.FILL, 10 } };
		setLayout(new TableLayout(size));
		exportButton = new SlickerButton("Export view...");
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				export();
			}
		});
		this.add(exportButton, "1, 1");
		
		this.setPreferredSize(new Dimension(100,50));
	}
	
	private void export() {
		
		ExportDialog export = new ExportDialog();
		export.showExportDialog(this, "Export view as ...", this.mainView.getScalable().getComponent(), "View");
	}
}
