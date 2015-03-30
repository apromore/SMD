//Om Ganesayanamaha
package org.prom6.plugins.guidetreeminer.algorithm;

import java.util.List;

import org.prom6.plugins.guidetreeminer.tree.GuideTree;
import org.prom6.plugins.guidetreeminer.tree.GuideTreeNode;
import org.prom6.plugins.guidetreeminer.types.AHCJoinType;
import org.prom6.plugins.guidetreeminer.types.SimilarityDistanceMetricType;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 June 2009
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */
public class AgglomerativeHierarchicalClustering {
	public class Cluster {
		int cardinality; // the number of elements in this cluster
		Cluster left, right; // the left and right child (cluster) of this node
		float[] clusterDistanceSimilarityArray; // the distance/similarity of this cluster with other clusters

		public Cluster(float[] clusterDistanceSimilarityArray) {
			this.cardinality = 1;
			this.clusterDistanceSimilarityArray = clusterDistanceSimilarityArray;
			this.left = null;
			this.right = null;
		}
		
		public Cluster(Cluster left, Cluster right, float[] clusterDistanceSimilarityArray) {
			this.left = left;
			this.right = right;
			this.cardinality = left.cardinality+right.cardinality;
			this.clusterDistanceSimilarityArray = clusterDistanceSimilarityArray;
		}
		
		public boolean live(){
			return this.clusterDistanceSimilarityArray != null;
		}
		
		public void kill(){
			this.clusterDistanceSimilarityArray = null;
		}
	}
	
	int K; // number of clusters created so far
	int N; // the number of items in the data set (for clustering)
	Cluster[] clusters; // the nodes in the hierarchy
	GuideTreeNode[] nodes;
	int[][] itemsJoined; // the items (can be individual elements or clusters already formed) joined (as a cluster) in an iteration
	
	
	
	AHCJoinType clusterCriteria;
	SimilarityDistanceMetricType clusterType;
	
	GuideTree guideTree;
	
	public AgglomerativeHierarchicalClustering(List<String> encodedTraceList, float[][] distanceSimilarityMatrix, SimilarityDistanceMetricType clusterType, AHCJoinType clusterCriteria){
		this.clusterType = clusterType;
		this.clusterCriteria = clusterCriteria;
		
		N = distanceSimilarityMatrix.length;
		
		itemsJoined = new int[N-1][2]; // since there are n items, the tree would contain n leaves and n-1 inner nodes (total 2n-1 nodes)
		clusters = new Cluster[2*N-1];
		nodes = new GuideTreeNode[2*N-1];
		// Create the leaf nodes (clusters)
		
		for(int i = 0; i < N; i++){
			clusters[i] = new Cluster(distanceSimilarityMatrix[i]);
			nodes[i] = new GuideTreeNode(encodedTraceList.get(i), 0);
			nodes[i].setStep(N);
		}
		
		K = N;
		while(K < 2*N-1){
			findAndJoin();
		}
		
		guideTree = new GuideTree(nodes[2*N-2], N);
	}
	
	private void findAndJoin(){
		// the index (i,j) for the maximum similarity nodes
		int minMaxI = -1, minMaxJ = -1;
		float maxSimilarity = Float.NEGATIVE_INFINITY;
		float minDistance = Float.POSITIVE_INFINITY;
		float s, d;
		
		if(clusterType.equals(SimilarityDistanceMetricType.Similarity)){
			// scan through all the nodes formed so far and pick the two with maximum similarity
			for(int i = 0; i < K; i++){
				if(clusters[i].live()){
					for(int j = 0; j < i; j++){
						if(clusters[j].live()){
							s = distSim(i,j);
							if(s > maxSimilarity){
								minMaxI = i;
								minMaxJ = j;
								maxSimilarity = s;
							}
						}
					}
				}
			}
		}else{
			// scan through all the nodes formed so far and pick the two with minimum distance
			for(int i = 0; i < K; i++){
				if(clusters[i].live()){
					for(int j = 0; j < i; j++){
						if(clusters[j].live()){
							d = distSim(i,j);
							if(d < minDistance){
								minMaxI = i;
								minMaxJ = j;
								minDistance = d;
							}
						}
					}
				}
			}
		}
		
		itemsJoined[K - N][0] = minMaxI;
		itemsJoined[K - N][1] = minMaxJ;
		
		nodes[K] = new GuideTreeNode(K+"");
		nodes[K].setChildren(nodes[minMaxI], nodes[minMaxJ]);
		nodes[K].setStep(2*N-1-K);
		
		switch(clusterCriteria){
			case MinVariance:
				joinMinVariance(minMaxI, minMaxJ);
				break;
			case SingleLinkage:
				joinSingleLinkage(minMaxI, minMaxJ);
				break;
			case CompleteLinkage:
				joinCompleteLinkage(minMaxI, minMaxJ);
				break;
			case AverageLinkage:
				joinAverageLinkage(minMaxI, minMaxJ);
				break;
			case CentroidLinkage:
				joinCentroid(minMaxI, minMaxJ);
				break;
		}
	}
	
	private float distSim(int i, int j){
		return clusters[Math.max(i, j)].clusterDistanceSimilarityArray[Math.min(i, j)];
	}
	
	private void joinMinVariance(int i, int j){
		// join clusters i and j to form a new cluster K
		float[] clusterDistanceSimilarityArray = new float[K];
		for (int m = 0; m < K; m++) {
			if (clusters[m].live() && m != j && m != i) {
				clusterDistanceSimilarityArray[m] = ((float) (clusters[i].cardinality + clusters[m].cardinality))
						/ (clusters[i].cardinality + clusters[j].cardinality + clusters[m].cardinality)
						* distSim(m, i)
						+ ((float) (clusters[j].cardinality + clusters[m].cardinality))
						/ (clusters[i].cardinality + clusters[j].cardinality + clusters[m].cardinality)
						* distSim(m, j)
						- ((float) (clusters[m].cardinality))
						/ (clusters[i].cardinality + clusters[j].cardinality + clusters[m].cardinality)
						* distSim(i, j);
			}
		}
		clusters[K] = new Cluster(clusters[i], clusters[j], clusterDistanceSimilarityArray);
		clusters[i].kill();
		clusters[j].kill();
		K++;
	}
	
	private void joinAverageLinkage(int i, int j) {
		// Join clusters i and j to form a new cluster K
		float[] clusterDistanceSimilarityArray = new float[K];

		for (int m = 0; m < K; m++) {
			if (clusters[m].live() && m != j && m != i) {
				clusterDistanceSimilarityArray[m] = (distSim(i, m) * clusters[i].cardinality 
						+ distSim(j,m)	* clusters[j].cardinality)
						/ (clusters[i].cardinality + clusters[j].cardinality);
			}
		}
		clusters[K] = new Cluster(clusters[i], clusters[j], clusterDistanceSimilarityArray);
		clusters[i].kill();
		clusters[j].kill();
		K++;
	}

	private void joinSingleLinkage(int i, int j) {
		// Join clusters i and j to form a new cluster K
		float[] clusterDistanceSimilarityArray = new float[K];
		
		for (int m = 0; m < K; m++) {
			if (clusters[m].live() && m != j && m != i) {
				clusterDistanceSimilarityArray[m] = Math.min(distSim(i, m), distSim(j, m));
			}
		}
		clusters[K] = new Cluster(clusters[i], clusters[j], clusterDistanceSimilarityArray);
		clusters[i].kill();
		clusters[j].kill();
		K++;
	}
	
	private void joinCompleteLinkage(int i, int j) {
		// Join clusters i and j to form a new cluster K
		float[] clusterDistanceSimilarityArray = new float[K];
		
		for (int m = 0; m < K; m++) {
			if (clusters[m].live() && m != j && m != i) {
				clusterDistanceSimilarityArray[m] = Math.max(distSim(i, m), distSim(j, m));
			}
		}
		clusters[K] = new Cluster(clusters[i], clusters[j], clusterDistanceSimilarityArray);
		clusters[i].kill();
		clusters[j].kill();
		K++;
	}

	private void joinCentroid(int i, int j) {
		// Join clusters i and j to form a new cluster K
		float[] clusterDistanceSimilarityArray = new float[K];
		
		for (int m = 0; m < K; m++) {
			if (clusters[m].live() && m != j && m != i) {
				clusterDistanceSimilarityArray[m] = (clusters[i].cardinality * distSim(m, i)
						+ clusters[j].cardinality * distSim(m, j) - ((clusters[i].cardinality
						* clusters[j].cardinality * distSim(i, j)) / ((clusters[i].cardinality + clusters[j].cardinality))))
						/  (clusters[i].cardinality + clusters[j].cardinality);
			}
		}
		clusters[K] = new Cluster(clusters[i], clusters[j], clusterDistanceSimilarityArray);
		clusters[i].kill();
		clusters[j].kill();
		K++;
	}

	public Cluster[] getClusters() {
		return clusters;
	}

	public int[][] getItemsJoined() {
		return itemsJoined;
	}
	
	public GuideTree getGuideTree(){
		return guideTree;
	}
	
}
