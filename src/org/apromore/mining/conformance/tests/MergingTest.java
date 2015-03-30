package org.apromore.mining.conformance.tests;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apromore.graph.JBPT.CPF;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.FormattableEPCSerializer;

import graph.Graph;
import merge.MergingPaper;

import common.EPCModelParser;

public class MergingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MergingTest mergingTest = new MergingTest();
//		mergingTest.merge();
//		mergingTest.mergeMany();
//		mergingTest.mergeClusters();
		mergingTest.mergeModels();
	}
	
	public void mergeModels() {
		String model1 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/merges3/f3.epml";
		String model2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/merges3/f4.epml";
		String outmodel = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/merges3/f3_f4.epml";
		merge(model1, model2, outmodel);
	}
	
	public void mergeClusters() {
		String clustersPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/clusters2";
		String mergesPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/merges2";
		File mergesFolder = new File(mergesPath);
		try {
			FileUtils.cleanDirectory(mergesFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File clustersFolder = new File(clustersPath);
		File[] cs = clustersFolder.listFiles();
		int count = 0;
		for (File clusterFolder : cs) {
			count++;
			System.out.println("----- Merging cluster: " + clusterFolder.getName() + ". " + count + " out of " + cs.length + "-----");
			
			File[] epcFiles = clusterFolder.listFiles();
			String mergeOutPath = new File(mergesFolder, clusterFolder.getName() + ".epml").getAbsolutePath();
//			System.out.println(epcFiles[0].getName() + " and " + epcFiles[1].getName());
			
			mergeCluster(clusterFolder.getAbsolutePath(), mergeOutPath);
//			merge2(clusterFolder.getAbsolutePath(), mergeOutPath);
//			merge(epcFiles[0].getAbsolutePath(), epcFiles[1].getAbsolutePath(), mergeOutPath);
			
			if (clusterFolder.getName().equals("20")) {
				System.out.println("Cluster 20 processed");
//				break;
			}
		}
	}
	
	public void mergeCluster(String clusterPath, String outPath) {
		File clusterFolder = new File(clusterPath);
		mergeModels(clusterFolder.listFiles(), outPath);
	}
	
	public void mergeModels(File[] models, String outPath) {

		File currentModel = models[0];
		for (int i = 1; i < models.length; i++) {
			File mergingModel = models[i];
			System.out.println("Merging: " + mergingModel.getName() + " with " + currentModel.getName());
			merge(currentModel.getAbsolutePath(), mergingModel.getAbsolutePath(), outPath);
			currentModel = new File(outPath);
		}
	}
	
	public void merge(String model1, String model2, String outModel) {
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		Graph.cleanGraphIDs();
		
		Graph g1 = EPCModelParser.readModels(model1, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		Graph g2 = EPCModelParser.readModels(model2, false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();

		MergingPaper mp = new MergingPaper();
		Graph merged = mp.mergeModels(g1, g2);
		
		EPCModelParser.writeModel(outModel, merged);
		
		try {
			String m3 = FileUtils.readFileToString(new File(outModel));
			m3 = m3.replaceAll("&", "-");
			FileUtils.write(new File(outModel), m3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		CPF cpf = new EPCDeserializer().deserializeFile(outModel);
		new FormattableEPCSerializer().serialize(cpf, outModel);
	}
	
	public void mergeMany() {
		for (int i = 0; i < 20; i++) {
			merge();
		}
	}
	
	public void merge() {
		
//		String model1 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/1An_ka9y.epml";
		String model1 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/clusters1/17/F389273.epml";
		
//		String model2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/1An_kc5k.epml";
		String model2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/clusters1/17/F389288.epml";
		
		String mergeInputPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/clusters1/20";
		String model3 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/f1_f2.epml";
		
		File mergeInput = new File(mergeInputPath);
		File[] inputs = mergeInput.listFiles();
		model1 = inputs[0].getAbsolutePath();
		model2 = inputs[1].getAbsolutePath();
		
		Graph.cleanGraphIDs();
		
		Graph g1 = EPCModelParser.readModels(model1, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		Graph g2 = EPCModelParser.readModels(model2, false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();

		MergingPaper mp = new MergingPaper();
		Graph merged = mp.mergeModels(g1, g2);
		
		EPCModelParser.writeModel(model3, merged);
		System.out.println("Completed the merging.");
		
		try {
			String m3 = FileUtils.readFileToString(new File(model3));
			m3 = m3.replaceAll("&", "-");
			FileUtils.write(new File(model3), m3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Completed replacement.");
		
		CPF cpf = new EPCDeserializer().deserializeFile(model3);
		new FormattableEPCSerializer().serialize(cpf, model3);
		System.out.println("Serialized the formatted epc.");
		
		System.out.println(g1.name+"\t"+g1.getVertices().size()+"\t"+g2.name+"\t"+g2.getVertices().size()+"\t"+
				merged.mergetime+"\t"+merged.beforeReduction+"\t"+((double)merged.beforeReduction/(double)(g1.getVertices().size()+g2.getVertices().size()))
				+"\t"+merged.getVertices().size()+"\t"+((double)merged.getVertices().size()/(double)(g1.getVertices().size()+g2.getVertices().size())));
	}
	
	public void merge2(String mergeInputPath, String model3) {
		
//		String model1 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/1An_ka9y.epml";
		String model1 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/clusters1/17/F389273.epml";
		
//		String model2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/1An_kc5k.epml";
		String model2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/clusters1/17/F389288.epml";
		
//		String mergeInputPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/clusters1/20";
//		String model3 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/m1/f1_f2.epml";
		
		File mergeInput = new File(mergeInputPath);
		File[] inputs = mergeInput.listFiles();
		model1 = inputs[0].getAbsolutePath();
		model2 = inputs[1].getAbsolutePath();
		
		Graph.cleanGraphIDs();
		
		Graph g1 = EPCModelParser.readModels(model1, false).get(0);
		g1.removeEmptyNodes();
		g1.reorganizeIDs();
		
		Graph g2 = EPCModelParser.readModels(model2, false).get(0);
		g2.removeEmptyNodes();
		g2.reorganizeIDs();
		
		g1.addLabelsToUnNamedEdges();
		g2.addLabelsToUnNamedEdges();

		MergingPaper mp = new MergingPaper();
		Graph merged = mp.mergeModels(g1, g2);
		
		EPCModelParser.writeModel(model3, merged);
		System.out.println("Completed the merging.");
		
		try {
			String m3 = FileUtils.readFileToString(new File(model3));
			m3 = m3.replaceAll("&", "-");
			FileUtils.write(new File(model3), m3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Completed replacement.");
		
		CPF cpf = new EPCDeserializer().deserializeFile(model3);
		new FormattableEPCSerializer().serialize(cpf, model3);
		System.out.println("Serialized the formatted epc.");
		
		System.out.println(g1.name+"\t"+g1.getVertices().size()+"\t"+g2.name+"\t"+g2.getVertices().size()+"\t"+
				merged.mergetime+"\t"+merged.beforeReduction+"\t"+((double)merged.beforeReduction/(double)(g1.getVertices().size()+g2.getVertices().size()))
				+"\t"+merged.getVertices().size()+"\t"+((double)merged.getVertices().size()/(double)(g1.getVertices().size()+g2.getVertices().size())));
	}

}
