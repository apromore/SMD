package org.apromore.mining.complexity;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.MiningConfig;
import org.apromore.service.utils.EPCDeserializer;

public class ComplexityEvaluatorTool {
	
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	
	public static void main(String[] args) {
		
//		String folderPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/pcm/subprocesses";
//		String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/pcm/analysis/eval_sp.csv";
		
		String folderPath = "/home/chathura/projects/qut/mining/apromore-mining/tests/t_smd/run_105_ws2";
		String outPath = "/home/chathura/projects/qut/mining/apromore-mining/tests/t_smd/temp1/1568_smd_s/data.csv";
		
//		String folderPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/tc/processes";
//		String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/tc/s_subprocesses_data.csv";
		
		ComplexityEvaluatorTool evaluatorTool = new ComplexityEvaluatorTool();
		evaluatorTool.computeAll(folderPath);
//		evaluatorTool.writeComplexities(folderPath, outPath);
	}
	
	public void computeAll(String mainBasePath) {
		try {
			File mainBase = new File(mainBasePath);
			File[] bases = mainBase.listFiles();
			String allData = "\n\nMethod, Num processes, Num subprocesses, Total models, Rep size, Time, Models above threshold, Avg, Min, Max, CFC, ACD, Density, CNC\n";
			for (File base : bases) {
				if (!base.isDirectory()) {
					continue;
				}
				System.out.println("Processing: " + base.getAbsolutePath());
				String baseData = computeComplexities(base);
				allData += baseData;
			}
			File mainOutput = new File(mainBase, "allData.csv");
			if (mainOutput.exists()) {
				FileUtils.deleteQuietly(mainOutput);
			}
			FileUtils.write(mainOutput, allData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String computeComplexities(File base) throws Exception {
		
		File[] fs = base.listFiles();
		File processesFolder = null;
		File subprocessesFolder = null;
		File timeDataFile = null;
		for (File f : fs) {
			if (f.isDirectory() && f.getName().equals("processes")) {
				processesFolder = f;
			}
			if (f.isDirectory() && f.getName().equals("subprocesses")) {
				subprocessesFolder = f;
			}
			if (!f.isDirectory() && !f.getName().startsWith("data") && f.getName().endsWith("csv")) {
				timeDataFile = f;
			}
		}
		
		long totalTime = getTotalTime(timeDataFile);
		
		File combinedFolder = processesFolder;
		int numProcesses = processesFolder.list().length;
		int numSubprocesses = 0;
		if (subprocessesFolder != null) {
			numSubprocesses = subprocessesFolder.list().length;
			combinedFolder = new File(base, "combined");
			if (combinedFolder.exists()) {
				FileUtils.deleteDirectory(combinedFolder);
			}
			combinedFolder.mkdir();
			FileUtils.copyDirectory(processesFolder, combinedFolder);
			FileUtils.copyDirectory(subprocessesFolder, combinedFolder);
		}
		String summaryLine = base.getName();
		String baseSummary = writeComplexities(combinedFolder.getAbsolutePath(), new File(base, "data.csv").getAbsolutePath(), totalTime, numProcesses, numSubprocesses);
		summaryLine += "," + baseSummary;
		return summaryLine;
	}

	private long getTotalTime(File timeDataFile) {
		try {
			List<String> data = FileUtils.readLines(timeDataFile);
			if (data != null && data.size() > 0) {
				String line = data.get(0);
				String[] parts = line.split(",");
				String timeString = parts[1];
				double timeS = Double.parseDouble(timeString);
				double timeMS = timeS * 1000;
				return (long) timeMS;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void writeComplexities(String folderPath, String outPath) {
		String[] complexities = computeComplexitiesFromFolders(folderPath, 0, 0, 0);
		try {
			FileUtils.write(new File(outPath), complexities[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeComplexities(String folderPath, String outPath, long duration) {
		String[] complexities = computeComplexitiesFromFolders(folderPath, duration, 0, 0);
		try {
			FileUtils.write(new File(outPath), complexities[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String writeComplexities(String folderPath, String outPath, long duration, int numProcesses, int numSubprocesses) {
		String[] complexities = computeComplexitiesFromFolders(folderPath, duration, numProcesses, numSubprocesses);
		try {
			File outFile = new File(outPath);
			if (outFile.exists()) {
				FileUtils.deleteQuietly(outFile);
			}
			FileUtils.write(outFile, complexities[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return complexities[1];
	}
	
	public String computeComplexities(String folderPath) {
		
		StringBuffer b = new StringBuffer();
		String header = "Model_Name,N,CFC,Density,Avg connector degree,CNC\n";
		b.append(header);
		File folder = new File(folderPath);
		File[] files = folder.listFiles();
		for (File f : files) {
			CPF model = epcDeserializer.deserializeFile(f.getAbsolutePath());
			int noajs = ComplexityCalculator.getNOAJS(model);
			double cnc = ComplexityCalculator.getCNC(model);
			int mcc = ComplexityCalculator.getMCC(model);
			double cfc = ComplexityCalculator.getCFC(model);
			double density = ComplexityCalculator.getDensity(model);
			double averageConnectorDegree = ComplexityCalculator.getAverageConnectorDegree(model);
			if (density > 0) {
				String data = f.getName() + "," + noajs + "," + cfc + "," + density + "," + averageConnectorDegree + "," + cnc + "\n";
				b.append(data);
			}
		}
		
		return b.toString();
	}
	
	private double round(double value) {
		return (double)Math.round(value * 100) / 100;
	}
	
	public String[] computeComplexitiesFromFolders(String folderPath, long duration, int numProcesses, int numSubprocesses) {
		
		double durationMins = duration / 60000;
		
		// changed method to examine first level folders of the given folder
		StringBuffer b = new StringBuffer();
		String header = "Model_Name,N,CFC,Density,Avg connector degree,CNC\n";
		b.append(header);
		File folder = new File(folderPath);
		File[] files = folder.listFiles();
		int minN = Integer.MAX_VALUE;
		int maxN = 0;
		int totalN = 0;
		double totalCNC = 0;
		double totalCFC = 0;
		double totalDensity = 0;
		double totalACD = 0;
		int modelsAboveThreshold = 0;
		for (File f : files) {
			if (f.isDirectory()) {
				File[] sfs = f.listFiles();
				for (File sf : sfs) {
					CPF model = epcDeserializer.deserializeFile(sf.getAbsolutePath());
					int noajs = ComplexityCalculator.getNOAJS(model);
					totalN += noajs;
					if (noajs > MiningConfig.COMPLEXITY_MATRIC_N) {
						modelsAboveThreshold++;
					}
					
					double cnc = ComplexityCalculator.getCNC(model);
					totalCNC += cnc;
					
					int mcc = ComplexityCalculator.getMCC(model);
					
					double cfc = ComplexityCalculator.getCFC(model);
					totalCFC += cfc;
					
					double density = ComplexityCalculator.getDensity(model);
					totalDensity += density;
					
					double averageConnectorDegree = ComplexityCalculator.getAverageConnectorDegree(model);
					totalACD += averageConnectorDegree;
					
					if (density > 0) {
						String data = sf.getName() + "," + noajs + "," + cfc + "," + density + "," + averageConnectorDegree + "," + cnc + "\n";
						b.append(data);
					}
				}
			} else {
				CPF model = epcDeserializer.deserializeFile(f.getAbsolutePath());
				int noajs = ComplexityCalculator.getNOAJS(model);
				totalN += noajs;
				if (noajs < minN) {
					minN = noajs;
				}
				
				if (noajs > maxN) {
					maxN = noajs;
				}
				
				double cnc = ComplexityCalculator.getCNC(model);
				totalCNC += cnc;
				
				int mcc = ComplexityCalculator.getMCC(model);
				
				double cfc = ComplexityCalculator.getCFC(model);
				totalCFC += cfc;
				
				double density = ComplexityCalculator.getDensity(model);
				totalDensity += density;
				
				double averageConnectorDegree = ComplexityCalculator.getAverageConnectorDegree(model);
				totalACD += averageConnectorDegree;
				
				if (density > 0) {
					String data = f.getName() + "," + noajs + "," + cfc + "," + density + "," + averageConnectorDegree + "," + cnc + "\n";
					b.append(data);
				}
			}
		}
		
		int totalModels = files.length;
		double avgN = (double) totalN / totalModels;
		double avgCNC = totalCNC / totalModels;
		double avgDensity = totalDensity / totalModels;
		double avgCFC = totalCFC / totalModels;
		double avgACD = totalACD / totalModels;
		String avgs = "\n\navg" + "," + avgN + "," + avgCFC + "," + avgDensity + "," + avgACD + "," + avgCNC + "\n";
		b.append(avgs);
		String min = "min," + minN + "\n";
		b.append(min);
		String max = "max," + maxN + "\n";
		b.append(max);
		String sum = "sum," + totalN + "\n";
		b.append(sum);
		b.append("duration (ms)," + duration + "\n");
		
		String timeValue = round(durationMins) + " min (" + duration + " ms)";
		String summary = numProcesses + "," + numSubprocesses + "," + totalModels + "," + totalN + "," + timeValue + "," + modelsAboveThreshold + "," + round(avgN) + "," + minN + "," + maxN + "," + round(avgCFC) + "," + round(avgACD) + "," + round(avgDensity) + "," + round(avgCNC) + "\n";
		b.append("\n\nNum processes, Num subprocesses, Total models, Rep size, Time, Models above threshold, Avg, Min, Max, CFC, ACD, Density, CNC\n");
		b.append(summary);
		return new String[] {b.toString(), summary};
//		return b.toString();
	}

}
