package org.prom5.analysis.dws;

import java.util.ArrayList;
import java.util.Random;

/**
 *  Defines data structures and procedures for projecting each trace in the feature space.
 *
 *  @author Gianluigi Greco, Antonella Guzzo
 *  @version 1.0
 */

public class VectorialPoint implements Comparable {
	double[] coordinates;
	int ID; //ID della traccia
	
	public static int PROJECTION_METHOD = 0;

	public VectorialPoint(Trace t, ArrayList features, int ID) {
		this.ID = ID;
		coordinates = new double[features.size()];
		for (int i = 0; i < coordinates.length; i++) {
			if (PROJECTION_METHOD == 0) {
				coordinates[i] = projectOriginal(t, (Feature) features.get(i));
			} else {
				coordinates[i] = projectDirect(t, (Feature) features.get(i));
			}
		}
	}

	public double distance(VectorialPoint v) {
		double ris = 0;
		for (int i = 0; i < coordinates.length; i++) {
			ris += (coordinates[i] - v.coordinates[i]) * (coordinates[i] - v.coordinates[i]);
		}
		return Math.sqrt(ris);
	}
	
	public double projectDirect(Trace t, Feature f) {
		
		int[] ts = t.getSequence();
		int[] fs = f.getBody().getSequence();
		
		double occurances = countOccurances(fs, ts);
		double possibleOccurances = Math.floor(ts.length / fs.length);
		
		double score = occurances / possibleOccurances;
		return score;
		
//		for (int i = 0; i < ts.length - fs.length; i++) {
//			boolean matchingSoFar = true;
//			for (int j = 0; j < fs.length && matchingSoFar; j++) {
//				if (fs[j] != ts[i + j]) {
//					matchingSoFar = false;
//				}
//			}
//			if (matchingSoFar) {
//				return 1d;
//			}
//		}
//		return 0d;
	}
	
	private int countOccurances(int[] fs, int[] ts) {
		int count = 0;
		
		for (int i = 0; i < ts.length - fs.length; i++) {
			boolean matchingSoFar = true;
			for (int j = 0; j < fs.length && matchingSoFar; j++) {
				if (fs[j] != ts[i + j]) {
					matchingSoFar = false;
				}
			}
			if (matchingSoFar) {
				count++;
				i = i + fs.length;
			}
		}
		
		return count;
	}

	public double projectOriginal(Trace t, Feature f) {
		Trace head = f.getHead();
		if (t.contains(head)) {
			return 0;
		}
		double occurrence = t.overlapDecaying(f.getBody());
		double whole = f.getBody().overlapDecaying(f.getBody());
		//double occurrence=t.overlap(f.getBody());
		//if (occurrence==f.getBody().size()) return 1; else return 0;
		//return occurrence/f.getBody().size();
		return occurrence / whole;
	}

	public int compareTo(Object o) {
		VectorialPoint vp = (VectorialPoint) o;
		for (int i = 0; i < coordinates.length; i++) {
			if (coordinates[i] < vp.coordinates[i]) {
				return -1;
			}
			if (coordinates[i] > vp.coordinates[i]) {
				return 1;
			}
		}
		return 0;
	}

	public String toString() {
		String ris = "ID:" + ID + " ";
		for (int i = 0; i < coordinates.length - 1; i++) {
			ris += coordinates[i] + ";";
		}
		ris += coordinates[coordinates.length - 1];
		return ris;
	}
}
