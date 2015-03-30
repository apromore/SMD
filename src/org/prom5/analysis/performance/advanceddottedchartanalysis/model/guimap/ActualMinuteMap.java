package org.prom5.analysis.performance.advanceddottedchartanalysis.model.guimap;

public class ActualMinuteMap extends GuiMap {
	
	public String getKey(long time){
		return  String.valueOf((time+timeOffset)/60000L);
	};
	
}
