package org.prom6.plugins.causalnet.visualizer;

import org.prom6.models.causalnet.CausalNetAnnotations;

public class AnnotatedVisualizationSettings {

	private boolean colorScalingEvents;
	private boolean colorScalingTransitions;
	private String measureEvents;
	private String measureTransitions;
	
	public AnnotatedVisualizationSettings(){
		
		this.colorScalingEvents = true;
		this.colorScalingTransitions = true;
		this.measureEvents = CausalNetAnnotations.counterTask;
		this.measureTransitions = "";
	}

	public boolean isColorScalingEvents() { return colorScalingEvents; }
	public void setColorScalingEvents(boolean colorScalingEvents) { this.colorScalingEvents = colorScalingEvents; }

	public boolean isColorScalingTransitions() { return colorScalingTransitions; }
	public void setColorScalingTransitions(boolean colorScalingTransitions) { this.colorScalingTransitions = colorScalingTransitions; }

	public String getMeasureEvents() { return measureEvents; }
	public void setMeasureEvents(String measureEvents) { this.measureEvents = measureEvents; }

	public String getMeasureTransitions() { return measureTransitions; }
	public void setMeasureTransitions(String measureTransitions) { this.measureTransitions = measureTransitions; }
}
