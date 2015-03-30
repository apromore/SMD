package matching.algos;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import nl.tue.tm.is.graph.SimpleGraph;
import nl.tue.tm.is.graph.TwoVertices;
import nl.tue.tm.is.ptnet.PTNet;

public class SimilarityFlooding extends DistanceAlgoAbstr implements DistanceAlgo{
	double[][] matrix;
	double[][] matrixp;
	SimpleGraph g1, g2;
	int nrOfFloodings = 1;
	
	// the vertices always do not have correct indexes
	// especially in case of epml diagrams, when we remove gateways
	// and sometimes also events
	LinkedList<Integer> g1VertexMap = new LinkedList<Integer>();
	LinkedList<Integer> g2VertexMap = new LinkedList<Integer>();
	
	public SimilarityFlooding(){
		
	}
	
	public void initFlooding(Helper helper, SimpleGraph graph1, SimpleGraph graph2) {
		g1 = graph1;
		g2 = graph2;
		
		g1VertexMap.clear();
		
		g2VertexMap.clear();
//		System.out.println(">>>> "+graph1.getVertices().size() + " "+g1VertexMap.size()+" "+ graph2.getVertices().size()+ " "+g2VertexMap.size());

		g1VertexMap.addAll(g1.getVertices());
		g2VertexMap.addAll(g2.getVertices());
		
//		for(Integer i : g1VertexMap) {
//			System.out.println(i);
//		}

//		System.out.println("______________________");
//		for(Integer i : graph1.getVertices()) {
//			System.out.println(i);
//		}

		
		matrix = new double[graph1.getVertices().size()][graph2.getVertices().size()];
		matrixp = new double[graph1.getVertices().size()][graph2.getVertices().size()];
		
//		System.out.println(">>>> "+graph1.getVertices().size() + " "+g1VertexMap.size()+" "+ graph2.getVertices().size()+ " "+g2VertexMap.size());
		for (Integer i : g1VertexMap) {
			for (Integer j : g2VertexMap) {
//				System.out.println("\t>>>> "+i+" "+ g1VertexMap.indexOf(i) + " "+ j + " "+ g2VertexMap.indexOf(j));
				matrix[g1VertexMap.indexOf(i)][g2VertexMap.indexOf(j)] = helper.matchingCost(graph1.getLabel(i), graph2.getLabel(j));
//				System.out.println(graph1.getLabel(i)+" <> "+ graph2.getLabel(j)+ " " + matrix[g1VertexMap.indexOf(i)][g2VertexMap.indexOf(j)]);
			}
		}
//		printMatrix(matrix);
//		findBestMapping();
	}
	public SimilarityFlooding(Helper helper, SimpleGraph graph1,
			SimpleGraph graph2) {
		double vweight = 0.2;
		double sweight = 0.1;
		double eweight = 0.7;					
		double ledcutoff = 0.5;
		double usepuredistance = 0.0; //0.0 represents 'false', 1.0 represents 'true'
		double prunewhen = 100.0;
		double pruneto = 10.0;
		double useepsilon = 1.0; //0.0 represents 'false', 1.0 represents 'true'

		Object weights[] = {"vweight",vweight,"sweight",sweight,"eweight",eweight,"ledcutoff",ledcutoff,"usepuredistance",usepuredistance,"prunewhen",prunewhen,"pruneto",pruneto,"useepsilon",useepsilon};
		setWeight(weights);

		init(graph1, graph2);
		initFlooding(helper, graph1, graph2);
	}
	public void flood() {
		double max = 0.0; 
		for (int n = 0; n < 3; n++) {
			max = 0.0;
			for (Integer i : g1VertexMap) {
				for (Integer j : g2VertexMap) {
					double presetsum = 0.0;
					double postsetsum = 0.0;
					for (Integer a : g1.preSet(i))
						for (Integer b : g2.preSet(j))
							presetsum += matrix[g1VertexMap.indexOf(a)][g2VertexMap.indexOf(b)];
					for (Integer a : g1.postSet(i))
						for (Integer b : g2.postSet(j))
							postsetsum += matrix[g1VertexMap.indexOf(a)][g2VertexMap.indexOf(b)];
					
					double number =  matrix[g1VertexMap.indexOf(i)][g2VertexMap.indexOf(j)] 
					                                                + (g1.preSet(i).size() == 0 || g2.preSet(j).size() == 0 ? 
					                                                		0 : presetsum/(g1.preSet(i).size() * g2.preSet(j).size())) 
					                                                + (g1.postSet(i).size() == 0|| g2.postSet(j).size() == 0 ? 
					                                                		0 : postsetsum/(g1.postSet(i).size() * g2.postSet(j).size()));
					matrixp[g1VertexMap.indexOf(i)][g2VertexMap.indexOf(j)] = number;
					if (number > max)
						max = number;
				}
			}

			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[0].length; j++) {
					matrix[i][j] = matrixp[i][j] / max;
					matrixp[i][j] = matrix[i][j];
				}
			}
		}
	}
	
	public void printMatrix(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.print(" "+ matrix[i][j]);
			}
			System.out.print("\n");
		}
	}

	public void printMatrix() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.printf("%1.2f ", matrix[i][j]);
			}
			System.out.println();
		}
	}
	public void printMatrixp() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.printf("%1.2f ", matrixp[i][j]);
			}
			System.out.println();
		}
	}
	
	public void printMatchings() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (matrix[i][j] >= ledcutoff)
					System.out.printf("(%s, %s) %1.2f\n ", g1.getLabel(g1VertexMap.get(i)), g2.getLabel(g2VertexMap.get(j)), matrix[i][j]);
			}
		}
	}
	
	public double findBestMapping() {
	
		Set<TwoVertices> resultMapping = new HashSet<TwoVertices>();
		int dim = g1VertexMap.size() > g2VertexMap.size() ? g1VertexMap.size() : g2VertexMap.size();
		double[][] matrixDupl = new double[dim][dim];
		
		for(int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				if(i >= matrix.length || j >= matrix[0].length) {
					matrixDupl[i][j] = 0;
				}
				else {
					matrixDupl[i][j] = (-1)*matrix[i][j];
			
				}
			}
		}

//		printMatrix(matrixDupl);
		int[][] result = HungarianAlgorithm.computeAssignments(matrixDupl);
		
		for(int i = 0; i < result.length; i++) {
			if (result[i][0] < g1VertexMap.size() && result[i][1] < g2VertexMap.size()) {
				double pairCost = matrix[result[i][0]][result[i][1]];
//				System.out.println(g1.getLabel(g1VertexMap.get(result[i][0]))+" <> "+ g2.getLabel(g2VertexMap.get(result[i][1]))+ " "+pairCost);
				if (pairCost > ledcutoff){
//					System.out.println("\t" + g1.getLabel(g1VertexMap.get(result[i][0]))+" <> "+ g2.getLabel(g2VertexMap.get(result[i][1]))+ " "+pairCost);
					resultMapping.add(new TwoVertices(g1VertexMap.get(result[i][0]), g2VertexMap.get(result[i][1])));
				}
			}
		}

		double resultWeight = editDistance(resultMapping);
		
//		System.out.println("Graph weight "+ resultWeight);
		return resultWeight;
	}

	public static void main(String[] args) throws Exception {
		String[] files = { /*"_pair01", */"_pair02"/*, "_pair03", "_pair06",
				"_pair05", "_pair06", "_pair07", "_pair08", "_pair09",
				"_pair10", "_pair11", "_pair12", "_pair13","_pair14",
				"_pair15", "_pair16", "_pair17", "_pair18", "_pair19",
				"_pair20" */};
		String prefix = "models/taskpairs/";

		for (String f : files) {
			// System.out.println(f+"--------------------");					String compareResFileName  = "files2/_pair" + (k < 10? "0"+k : k);
			
			FileInputStream fstream = new FileInputStream(prefix+f);
			
			DataInputStream in = new DataInputStream(fstream);
			
			BufferedReader buf = new BufferedReader(new InputStreamReader(in));
			String[] sm = { buf.readLine() };
			String[] dm = { buf.readLine() };
			HashMap<Integer, TwoVertices> mappings;

			for (String s : sm) {
				PTNet serchPNet = PTNet.loadPNML(prefix + s);
				SimpleGraph searchgraph = new SimpleGraph(serchPNet);
				for (String d : dm) {
					PTNet docPNet = PTNet.loadPNML(prefix + d);
					SimpleGraph docgraph = new SimpleGraph(docPNet);
					PNHelper2 helper = new PNHelper2();
					SimilarityFlooding simflood = new SimilarityFlooding(helper, searchgraph,
							docgraph);

					
					System.out.println("****************");
					System.out.println("Search models:\n" +searchgraph);
					System.out.println("****************");
					System.out.println("Document models:\n" +docgraph);
					System.out.println("****************");
					simflood.printMatchings();
					System.out.println("****************");

					simflood.printMatrix();
					
					simflood.flood();
					System.out.println("****************-----");
//					simflood.printMatrix();
					
					simflood.printMatchings();
					System.out.println("****************");

					simflood.findBestMapping();
					break;
				}
				break;
			}
			break;
		}
	}
	
	public void setNumberOfFloodings(int nr) {
		nrOfFloodings = nr;
	}
	
	public double compute(SimpleGraph graph1, SimpleGraph graph2) {
		PNHelper2 helper = new PNHelper2();
		init(graph1, graph2);
		initFlooding(helper, graph1, graph2);
		
		int flood = 1;
		while (flood <= nrOfFloodings) {
			flood();
			flood++;
		}
		
		return findBestMapping();
	}

}
