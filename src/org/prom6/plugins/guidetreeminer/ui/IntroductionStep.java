package org.prom6.plugins.guidetreeminer.ui;

import org.prom6.plugins.guidetreeminer.settings.GuideTreeMinerSettingsListener;

import info.clearthought.layout.TableLayout;

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
public class IntroductionStep extends myStep {
	GuideTreeMinerSettingsListener listener;
	
	public IntroductionStep(){
		initComponents();
	}
	
	private void initComponents() {
    	double size[][] = {{TableLayout.FILL},{TableLayout.FILL}};
    	setLayout(new TableLayout(size));
    	String body = "<p>This wizard will guide you through the process of configuring this plugin.</p>";
    	body += "<p>The configuration options for guide tree miner can be divided into three categories:<ol>";
    	body += "<li>options for configuring the type of feature, </li>";
    	body += "<li>options for configuring the similarity/distance metric, and </li>";
    	body += "<li>options for configuring the tree generation algorithm </li></ol>";
    	body += "The wizard will allow you to configure these three categories in the given order.</p>";
    	add(SlickerFactory.instance().createLabel("<html><h1>Introduction</h1>" + body), "0, 0, l, t");
	}
	
	public boolean precondition() {
		return true;
	}

	public void setListener(GuideTreeMinerSettingsListener listener){
		this.listener = listener;
	}
	
	public void readSettings() {

	}

}
