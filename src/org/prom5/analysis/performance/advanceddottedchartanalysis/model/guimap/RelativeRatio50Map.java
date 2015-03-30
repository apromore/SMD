package org.prom5.analysis.performance.advanceddottedchartanalysis.model.guimap;
public class RelativeRatio50Map extends GuiMap {
	
	public String getKey(long time){
		return  String.valueOf((time)/50L);
	};
	
}
