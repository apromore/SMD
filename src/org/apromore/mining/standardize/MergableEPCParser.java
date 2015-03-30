package org.apromore.mining.standardize;

import graph.Edge;
import graph.Graph;
import graph.Graphics;
import graph.Vertex;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 

import common.Settings;



public class MergableEPCParser{

	/**
	 * Read .pnml models. Add only functions (transition) and the 
	 * connectors.
	 * @param filename
	 * @return
	 */
	public static Graph readModelPNML(String filename){
		Graph graph = new Graph();
    	Element mainElement; 
		
    	try {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (new File(filename));

            // normalize text representation
            doc.getDocumentElement ().normalize ();
//            System.out.println ("Root element of the doc is " + 
//                 doc.getDocumentElement().getNodeName());


            NodeList models = doc.getElementsByTagName("pnml");

            Node mainModel = models.item(0);
            
            if(mainModel.getNodeType() == Node.ELEMENT_NODE){
            	
            	Node mainElementNode = ((Element)mainModel).getElementsByTagName("net").item(0);
            
	            if(mainElementNode.getNodeType() == Node.ELEMENT_NODE){
	            	mainElement = (Element)mainElementNode;
	            	
	            	graph.setNrOfFunctions(addElements("transition", graph, mainElement));
//	            	graph.setNrOfEvents(addElements("event", graph, mainElement));
	            	addPNMLEdges("arc", graph, mainElement);
	            	addGateways("place", graph, mainElement);
//	            	addGateways("or", graph, mainElement);
//	            	addGateways("xor", graph, mainElement);
	            	
	            	graph.linkVertices();
	            }
            }
    	}catch (SAXParseException err) {
    		System.out.println ("** Parsing error" + ", line " 
    				+ err.getLineNumber () + ", uri " + err.getSystemId ());
    		System.out.println(" " + err.getMessage ());

        }catch (SAXException e) {
        	Exception x = e.getException ();
        	((x == null) ? e : x).printStackTrace ();

		} 
        catch (Throwable t) {
        	t.printStackTrace ();
        }
        return graph;

	}
	
    public static Graph readModel(String filename){
    	Graph epcGraph = new Graph();
    	Element mainEPCElement; 
		
    	try {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (new File(filename));

            // normalize text representation
            doc.getDocumentElement ().normalize ();
//            System.out.println ("Root element of the doc is " + 
//                 doc.getDocumentElement().getNodeName());


            NodeList models = doc.getElementsByTagName("epc");

            Node mainEPCmodel = models.item(0);
            
            
            if(mainEPCmodel.getNodeType() == Node.ELEMENT_NODE){
            	mainEPCElement = (Element)mainEPCmodel;
            	epcGraph.setNrOfFunctions(addElements("function", epcGraph, mainEPCElement));
            	epcGraph.setNrOfEvents(addElements("event", epcGraph, mainEPCElement));
            	addEdges("arc", epcGraph, mainEPCElement);
            	addGateways("and", epcGraph, mainEPCElement);
            	addGateways("or", epcGraph, mainEPCElement);
            	addGateways("xor", epcGraph, mainEPCElement);
            	
            	epcGraph.linkVertices();
            	
            }
    	}catch (SAXParseException err) {
    		System.out.println ("** Parsing error" + ", line " 
    				+ err.getLineNumber () + ", uri " + err.getSystemId ());
    		System.out.println(" " + err.getMessage ());

        }catch (SAXException e) {
        	Exception x = e.getException ();
        	((x == null) ? e : x).printStackTrace ();

		} 
        catch (Throwable t) {
        	t.printStackTrace ();
        }
        return epcGraph;
        //System.exit (0);

    }//end of main

    public static String writeModel(Graph g) {
    	String outputEPML = null;
    	try {
    		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    		PrintWriter output = null;
    		output = new PrintWriter(outStream);
    		
    		// print header
    		output.println("<?xml version=\'1.0\' encoding=\'UTF-8\'?>");
    		output.println("<epml:epml xmlns:epml=\'http://www.epml.de\' " +
    				"xmlns:xsi=\'http://www.w3.org/2001/XMLSchema-instance\' xsi:schemaLocation=\'http://www.epml.de EPML_2.0.xsd\'>");
    		
    		output.println("\t<epc epcId=\'"+g.ID+"\' name=\'"+g.name+"\'>");
    		
    		// vertices
    		for (Vertex v : g.getVertices()) {
    			// functions
    			if (v.getType().equals(Vertex.Type.function)) {
//    				System.out.println(v.getID()+" - "+v.getLabel());
    				output.println("\t\t<function defRef=\'"+v.getID()+"\' id=\'"+v.getID()+"\'>");
    				output.println("\t\t\t<name>"+wrapLabels(v.getLabel())+"</name>");
    				HashMap<String, String> annotationMap = v.getAnnotationMap();
    				if (annotationMap != null && annotationMap.size() > 0) {
    					output.print("\t\t\t<attribute typeRef=\'annotation\' value=\'");
    					for (Entry<String, String> annotation : annotationMap.entrySet()) {
    						output.print(annotation.getKey().replace("\'", " ").replace("\"", " ")+":"+annotation.getValue().replace("\'",  " ").replace("\"", " ")+";");
    					}
    					output.print("\' />\n");
    				}
    				Graphics gr = v.getGraphics();
    				if (gr != null) {
        				output.println("\t\t\t<graphics>");
        				output.println("\t\t\t\t<position height=\'"+gr.getHeight()+"\' width=\'"
        						+gr.getWidth()+"\' x=\'"+gr.getX()+"\' y=\'"+gr.getY()+"\'/>");
        				output.println("\t\t\t</graphics>");
    				}
    				output.println("\t\t</function>");
    			}
    			else if (v.getType().equals(Vertex.Type.event)) {
    				output.println("\t\t<event defRef=\'"+v.getID()+"\' id=\'"+v.getID()+"\'>");
    				output.println("\t\t\t<name>"+wrapLabels(v.getLabel())+"</name>");
    				HashMap<String, String> annotationMap = v.getAnnotationMap();
    				if (annotationMap != null && annotationMap.size() > 0) {
    					output.print("\t\t\t<attribute typeRef=\'annotation\' value=\'");
    					for (Entry<String, String> annotation : annotationMap.entrySet()) {
    						output.print(annotation.getKey().replace("\'",  " ").replace("\"", " ")+":"+annotation.getValue().replace("\'",  " ").replace("\"", " ")+";");
    					}
    					output.print("\' />\n");
    				}
    				Graphics gr = v.getGraphics();
    				if (gr != null) {
        				output.println("\t\t\t<graphics>");
        				output.println("\t\t\t\t<position height=\'"+gr.getHeight()+"\' width=\'"
        						+gr.getWidth()+"\' x=\'"+gr.getX()+"\' y=\'"+gr.getY()+"\'/>");
        				output.println("\t\t\t</graphics>");
    				}

    				output.println("\t\t</event>");
    			}
    			else if (v.getType().equals(Vertex.Type.gateway)) {
    				if (v.getGWType().equals(Vertex.GWType.and)) {
        				output.println("\t\t<and id=\'"+v.getID()+"\'>");
        				output.println("\t\t\t<name/>");
        				HashMap<String, String> annotationMap = v.getAnnotationMap();
        				if (annotationMap != null && annotationMap.size() > 0) {
        					output.print("\t\t\t<attribute typeRef=\'annotation\' value=\'");
        					for (Entry<String, String> annotation : annotationMap.entrySet()) {
        						output.print(annotation.getKey().replace("\'",  " ").replace("\"", " ")+":"
        								+annotation.getValue().replace("\'",  " ").replace("\"", " ")+";");
        					}
        					output.print("\' />\n");
        				}
        				if (v.isAddedGW()) {
        					output.println("\t\t\t<attribute typeRef=\'added\' value=\'true\' />");
        				}
        				Graphics gr = v.getGraphics();
        				if (gr != null) {
            				output.println("\t\t\t<graphics>");
            				output.println("\t\t\t\t<position height=\'"+gr.getHeight()+"\' width=\'"
            						+gr.getWidth()+"\' x=\'"+gr.getX()+"\' y=\'"+gr.getY()+"\'/>");
            				output.println("\t\t\t</graphics>");
        				}
        				if (v.isConfigurable()) {
        					output.println("\t\t\t<configurableConnector/>");
        				}
        				output.println("\t\t</and>");
    				}
    				else if (v.getGWType().equals(Vertex.GWType.or)) {
        				output.println("\t\t<or id=\'"+v.getID()+"\'>");
        				output.println("\t\t\t<name/>");
        				HashMap<String, String> annotationMap = v.getAnnotationMap();
        				if (annotationMap != null && annotationMap.size() > 0) {
        					output.print("\t\t\t<attribute typeRef=\'annotation\' value=\'");
        					for (Entry<String, String> annotation : annotationMap.entrySet()) {
        						output.print(annotation.getKey().replace("\'",  " ").replace("\"", " ")
        								+":"+annotation.getValue().replace("\'",  " ").replace("\"", " ")+";");
        					}
        					output.print("\' />\n");
        				}
        				if (v.isAddedGW()) {
        					output.println("\t\t\t<attribute typeRef=\'added\' value=\'true\' />");
        				}
        				Graphics gr = v.getGraphics();
        				if (gr != null) {
            				output.println("\t\t\t<graphics>");
            				output.println("\t\t\t\t<position height=\'"+gr.getHeight()+"\' width=\'"
            						+gr.getWidth()+"\' x=\'"+gr.getX()+"\' y=\'"+gr.getY()+"\'/>");
            				output.println("\t\t\t</graphics>");
        				}
        				if (v.isConfigurable()) {
        					output.println("\t\t\t<configurableConnector/>");
        				}
        				output.println("\t\t</or>");
    				}
    				else if (v.getGWType().equals(Vertex.GWType.xor)) {
        				output.println("\t\t<xor id=\'"+v.getID()+"\'>");
        				output.println("\t\t\t<name/>");
        				HashMap<String, String> annotationMap = v.getAnnotationMap();
        				if (annotationMap != null && annotationMap.size() > 0) {
        					output.print("\t\t\t<attribute typeRef=\'annotation\' value=\'");
        					for (Entry<String, String> annotation : annotationMap.entrySet()) {
        						output.print(annotation.getKey().replace("\'",  " ").replace("\"", " ")
        								+":"+annotation.getValue().replace("\'",  " ").replace("\"", " ")+";");
        					}
        					output.print("\' />\n");
        				}
        				if (v.isAddedGW()) {
        					output.println("\t\t\t<attribute typeRef=\'added\' value=\'true\' />");
        				}

        				Graphics gr = v.getGraphics();
        				if (gr != null) {
            				output.println("\t\t\t<graphics>");
            				output.println("\t\t\t\t<position height=\'"+gr.getHeight()+"\' width=\'"
            						+gr.getWidth()+"\' x=\'"+gr.getX()+"\' y=\'"+gr.getY()+"\'/>");
            				output.println("\t\t\t</graphics>");
        				}
        				if (v.isConfigurable()) {
        					output.println("\t\t\t<configurableConnector/>");
        				}
        				output.println("\t\t</xor>");
    				}
    			}
    		}

    		for (Edge e : g.getEdges()) {
    			if (e.getFromVertex() == null || e.getToVertex() == null) {
    				continue;
    			}
				output.println("\t\t<arc id=\'"+e.getId()+"\'>");
				output.println("\t\t\t<flow source=\'"+e.getFromVertex()+"\' target=\'"+e.getToVertex()+"\'/>");
//				System.out.println("WRITE NODE LABELs "+e.getLabels().size());
//				System.out.println("WRITE NODE LABELs "+e.getFromG()+" "+e.getToG());
				if (e.isLabelAddedToModel()/* || (e.getLabels() != null && e.getLabels().size() > 0)*/) {
//					System.out.println("WRITE NODE LABELs "+e.getLabels().size());
					String edgeLabel = "";
					for (String s: e.getLabels()) {
						edgeLabel +=  s.replace("\'",  " ").replace("\"", " ") + ";";
					}
					if (edgeLabel.length() > 0) {
						edgeLabel = edgeLabel.substring(0, edgeLabel.length() - 1);
					}
					output.println("\t\t\t<attribute typeRef=\'annotation\' value=\'"+edgeLabel+"\' />");
					// TODO remove - just for testing
					output.println("\t\t\t<name>"+edgeLabel+"</name>");
				}
				Graphics gr1 = e.getFromG();
				Graphics gr2 = e.getToG();
				if (gr1 != null && gr2 != null) {
    				output.println("\t\t\t<graphics>");
    				output.println("\t\t\t\t<position  x=\'"+gr1.getX()+"\' y=\'"+gr1.getY()+"\'/>");
    				output.println("\t\t\t\t<position  x=\'"+gr2.getX()+"\' y=\'"+gr2.getY()+"\'/>");
    				output.println("\t\t\t</graphics>");
				}

				output.println("\t\t</arc>");
    		}
    		
    		output.println("\t</epc>");	
    		output.println("</epml:epml>");	
    		output.flush();
    		outputEPML = outStream.toString();
    		output.close();
    		outStream.close();
    	} catch (Exception e) {
			e.printStackTrace();
        }   
    	return outputEPML;
    }
    
    
    private static String wrapLabels(String s) {
    	StringTokenizer st = new StringTokenizer(s, " ");
    	String resultString = "";
    	String currentToken = "";
    	if (s.length() > 14) {
	    	while (st.hasMoreTokens()) {
	    		String next = st.nextToken();
	    		if (next.length() + 1 + currentToken.length() <= 14) {
	    			currentToken += " "+ next;
	//    			System.out.println("currentToken "+ currentToken);
	    		}
	    		else {
	    			resultString += currentToken + "\n";
	    			currentToken = next;
	    		}
	    	}
	    	if (currentToken.length() > 0 ) {
	    		resultString += currentToken + "\n";
	    	}
	    	
	    	// remove last newline
	    	resultString = resultString.substring(0, resultString.length()-1);
	//		System.out.println("resultString "+ currentToken);
    	}
    	else {
    		return s;
    	}
     	return resultString;
    }
    
    
    public static LinkedList<Graph> readModels(String epml, boolean print){
    	return readModels(epml, print, false);
    }
    
    public static LinkedList<Graph> readModels(String epml, boolean print, boolean readCoordinates){
    	
    	LinkedList<Graph> graphs = new LinkedList<Graph>();
    	
    	Element mainEPCElement; 
    	
		String outputfile = "files1/modelnames.txt";
		
    	try {
    		PrintWriter output = null;
    		if(print)  {
    			output = new PrintWriter(new FileWriter(outputfile));
    		}

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            InputStream epmlStream = IOUtils.toInputStream(epml);
            Document doc = docBuilder.parse(epmlStream);

            // normalize text representation
            doc.getDocumentElement ().normalize ();

            NodeList models = doc.getElementsByTagName("epc");

            for(int k = 0; k < models.getLength(); k++) {
            	
            	Graph epcGraph = new Graph();
	            Node mainEPCmodel = models.item(k);
	            
	            if(mainEPCmodel.getNodeType() == Node.ELEMENT_NODE){
	            	mainEPCElement = (Element)mainEPCmodel;
            		epcGraph.name = mainEPCElement.getAttribute("name");
            		epcGraph.ID = mainEPCElement.getAttribute("epcId");

	            	if (print){
	            		output.print(epcGraph.name+"\n");
	            	}
	            	
	            	epcGraph.setNrOfFunctions(addElements("function", epcGraph, readCoordinates, mainEPCElement));
	            	epcGraph.setNrOfEvents(addElements("event", epcGraph, readCoordinates, mainEPCElement));
	            	addEdges("arc", epcGraph, mainEPCElement, readCoordinates);
	            	addGateways("and", epcGraph, mainEPCElement, readCoordinates);
	            	addGateways("or", epcGraph, mainEPCElement, readCoordinates);
	            	addGateways("xor", epcGraph, mainEPCElement, readCoordinates);
	            	
	            	epcGraph.linkVertices();
	            	
	            }
	            graphs.add(epcGraph);
            }
    		if(print)  {
    			output.close();
    		}


    	}catch (IOException e) {
			e.printStackTrace();
        }catch (SAXParseException err) {
    		System.out.println ("** Parsing error" + ", line " 
    				+ err.getLineNumber () + ", uri " + err.getSystemId ());
    		System.out.println(" " + err.getMessage ());

        }catch (SAXException e) {
        	Exception x = e.getException ();
        	((x == null) ? e : x).printStackTrace ();

        }catch (Throwable t) {
        	t.printStackTrace ();
        }
        return graphs;
        //System.exit (0);

    }//end of main

    private static int addElements(String elementName, Graph epcGraph, Element mainEPCElement) { 
    	return addElements(elementName, epcGraph, false, mainEPCElement, false);
    }
    
    private static int addElements(String elementName, Graph epcGraph, boolean readCoordinates, Element mainEPCElement) { 
    	return addElements(elementName, epcGraph, readCoordinates, mainEPCElement, false);
    }

    private static int addElements(String elementName, Graph epcGraph, boolean readCoordinates, Element mainEPCElement, boolean removeSilent) { 

    	NodeList functions = mainEPCElement.getElementsByTagName(elementName);
    	int nrFns = 0;
    	
    	// add elements
    	 for(int s = 0; s < functions.getLength(); s++) {
    		 Node function = functions.item(s);
    		 if(function.getNodeType() == Node.ELEMENT_NODE) {
    			 Graphics g = null;
    			 
    			 // get ID
    			 Node id = functions.item(s).getAttributes().getNamedItem("id");
    			 
    			 Element functionElement = (Element) function;
    			 
    			 // read the coordinates from the file
    			 if (readCoordinates) {
    				 Node graphics = functionElement.getElementsByTagName("graphics").item(0);
    				 
    				 if(graphics.getNodeType() == Node.ELEMENT_NODE) {
        				 Element graphicsElement = (Element) graphics;

    					 Node position = graphicsElement.getElementsByTagName("position").item(0);
    					 if(position.getNodeType() == Node.ELEMENT_NODE) {
    						 
    						 g = new Graphics(Integer.parseInt(position.getAttributes().getNamedItem("height").getNodeValue()),
    								 Integer.parseInt(position.getAttributes().getNamedItem("width").getNodeValue()),
    								 Integer.parseInt(position.getAttributes().getNamedItem("x").getNodeValue()),
    								 Integer.parseInt(position.getAttributes().getNamedItem("y").getNodeValue()));
    					 }
    				 }
    			 }
    			 
    			 // get attributes
    			 // for events and functions we have only annotation attribute
    			 // so there exists only one attribuyte element
    			 HashMap<String, String> annotationMap = null;
    			 NodeList attributeList = functionElement.getElementsByTagName("attribute");
    			 if (attributeList != null && attributeList.getLength() > 0) {
	    			 Node attributes = attributeList.item(0);
					 if(attributes.getNodeType() == Node.ELEMENT_NODE) {
						 if("annotation".equals(attributes.getAttributes().getNamedItem("typeRef").getNodeValue())) {
							 annotationMap = parseAnnotationForEventsAndFunctions(attributes.getAttributes().getNamedItem("value").getNodeValue());
						 }
					 }
    			 }
    			 
    			 // get name
    			 Node name = functionElement.getElementsByTagName("name").item(0);
    			 
    			 if(name.getNodeType() == Node.ELEMENT_NODE) {
    				 Element nameElement = (Element) name;
    				 //(EPCVertexType type, String label, Integer ID)
    				 if (!removeSilent || removeSilent && !nameElement.getTextContent().trim().equals("tau")) {
    					 Vertex v = new Vertex((elementName.equalsIgnoreCase("function") || elementName.equalsIgnoreCase("transition")) ? Vertex.Type.function : Vertex.Type.event, Settings.removeSpaces(nameElement.getTextContent().trim().replace('\n', ' ').replace("\\n", " ")), id.getNodeValue());
    					 if (g != null) {
    						 v.setGraphics(g);
    					 }
    					 if (annotationMap != null) {
    						 v.addAnnotations(annotationMap);
    					 }
    					 
    					 epcGraph.addVertex(v);
    					 nrFns++;
    				 }
    			 }
    		 }
    	 }
    	 return removeSilent ? nrFns : functions.getLength();
    }
     
    // the annotation attribute for events and functions are in format
    // model1:name in model1;model2:name in model 2;
    // the names must not contain ';', otherwise the name parsing fails
    private static HashMap<String, String> parseAnnotationForEventsAndFunctions(String nodeValue) {
    	HashMap<String, String> annotationMap = new HashMap<String, String>();
    	if (nodeValue == null) {
    		return annotationMap;
    	}
    	
    	StringTokenizer st  = new StringTokenizer(nodeValue, ";");
    	while (st.hasMoreTokens()) {
    		String nextToken = st.nextToken();
    		int colon = nextToken.indexOf(':');
    		String modelName = nextToken.substring(0, colon);
    		String value = nextToken.substring(colon + 1, nextToken.length());
    		annotationMap.put(modelName, value);
    	}
    	
    	return annotationMap;
	}

	private static void addPNMLEdges(String elementName, Graph epcGraph, Element mainEPCElement) { 

    	NodeList functions = mainEPCElement.getElementsByTagName(elementName);
    	
    	// add elements
    	 for(int s = 0; s < functions.getLength() ; s++) {
    		 Node function = functions.item(s);
    		 if(function.getNodeType() == Node.ELEMENT_NODE) {
    			 // get ID
    			 Node id = functions.item(s).getAttributes().getNamedItem("id");
    			 Node source = functions.item(s).getAttributes().getNamedItem("source");
    			 Node target = functions.item(s).getAttributes().getNamedItem("target");

				 epcGraph.addEdge(new Edge(
						 source.getNodeValue(),
						 target.getNodeValue(),
						 id.getNodeValue()));
    		 }
    	 }
    }

    private static void addEdges(String elementName, Graph epcGraph, Element mainEPCElement) {
    	addEdges(elementName, epcGraph, mainEPCElement, false);
    }
    
    private static void addEdges(String elementName, Graph epcGraph, Element mainEPCElement, boolean readCoordinates) { 

    	NodeList functions = mainEPCElement.getElementsByTagName(elementName);
    	HashSet<String> graphLabels = new HashSet<String>();
    	
    	// add elements
    	 for(int s = 0; s < functions.getLength() ; s++) {
    		 Node function = functions.item(s);
    		 if(function.getNodeType() == Node.ELEMENT_NODE) {
    			 // get ID
    			 Node id = functions.item(s).getAttributes().getNamedItem("id");
    			 
    			 Element functionElement = (Element) function;
    			 // get name
    			 Node flow = functionElement.getElementsByTagName("flow").item(0);
    			 
    			 if(flow.getNodeType() == Node.ELEMENT_NODE) {
//    				 Element flowElement = (Element) flow;
    				 //(EPCVertexType type, String label, Integer ID)
    				 Graphics g1 = null;
    				 Graphics g2 = null;
        			 if (readCoordinates) {
        				 Node graphics = functionElement.getElementsByTagName("graphics").item(0);
        				 
        				 if(graphics.getNodeType() == Node.ELEMENT_NODE) {
            				 Element graphicsElement = (Element) graphics;
            				 NodeList nl = graphicsElement.getElementsByTagName("position");
            				 
//            				 System.out.println("READ EDGE COORDINATES "+nl.getLength());
            				 
        					 Node position1 = nl.item(0);
        					 Node position2 = nl.item(1);
        					 
        					 if(position1.getNodeType() == Node.ELEMENT_NODE && position2.getNodeType() == Node.ELEMENT_NODE) {
//        						 System.out.println("READ EDGE");
        						 g1 = new Graphics(0,
        								 0,
        								 Integer.parseInt(position1.getAttributes().getNamedItem("x").getNodeValue()),
        								 Integer.parseInt(position1.getAttributes().getNamedItem("y").getNodeValue()));
        						
        						 g2 = new Graphics(0,
        								 0,
        								 Integer.parseInt(position2.getAttributes().getNamedItem("x").getNodeValue()),
        								 Integer.parseInt(position2.getAttributes().getNamedItem("y").getNodeValue()));

        					 }
        				 }
        			 }

    				 
    				 String source = flow.getAttributes().getNamedItem("source").getNodeValue();
    				 String target = flow.getAttributes().getNamedItem("target").getNodeValue();
    				 
    				 if (!source.equals(target)) {
	    				 Edge toAdd = new Edge(source, target, id.getNodeValue());
	        			 // get name
	    				 
	    				 Node nameAttribute =  null;
	        			 NodeList attributeList = functionElement.getElementsByTagName("attribute");
	        			 if (attributeList != null && attributeList.getLength() > 0) {
	        				 nameAttribute = attributeList.item(0);
	        			 }
	        			 
	        			 if (g1 != null && g2 != null) {
	        				 toAdd.setFromG(g1);
	        				 toAdd.setToG(g2);
	        			 }

	        			 if(nameAttribute != null 
	        					 && nameAttribute.getNodeType() == Node.ELEMENT_NODE 
	        					 && "annotation".equals(nameAttribute.getAttributes().getNamedItem("typeRef").getNodeValue())) {
	        				 String name = nameAttribute.getAttributes().getNamedItem("value").getNodeValue();
	        				 StringTokenizer st = new StringTokenizer(name, ";");
	        				 while(st.hasMoreTokens()) {
	        					 String l = st.nextToken();
	        					 toAdd.addLabel(l);
	        					 graphLabels.add(l);
	        				 }
//	        				 System.out.println("NODE LABELs SIZE "+toAdd.getLabels().size());
	        				 if(toAdd.getLabels().size() > 0) {
	        					 toAdd.addLabelToModel();
	        				 }
	        			 }
	        			 
	    				 epcGraph.addEdge(toAdd);
	//    				 System.out.println("source: "+ flow.getAttributes().getNamedItem("source").getNodeValue()+" target: "+flow.getAttributes().getNamedItem("target").getNodeValue()+" ID:"+ Integer.valueOf(id.getNodeValue()));
	    			
    				 }

    			 }
    		 }
    	 }
    	 
    	 if (graphLabels.size() > 0) {
			 epcGraph.setGraphConfigurable();
			 epcGraph.addGraphLabels(graphLabels);
    	 }
    }

    private static void addGateways(String elementName, Graph epcGraph, Element mainEPCElement) { 
    	addGateways(elementName, epcGraph, mainEPCElement, false); 
    }
    
    private static void addGateways(String elementName, Graph epcGraph, Element mainEPCElement, boolean readCoordinates) { 

    	NodeList functions = mainEPCElement.getElementsByTagName(elementName);
    	
    	// add elements
    	 for(int s = 0; s < functions.getLength() ; s++) {
    		 Node function = functions.item(s);
    		 if(function.getNodeType() == Node.ELEMENT_NODE) {
    			 Graphics g = null;
    			 Element functionElement = (Element) function;
    			 
    			 // read the coordinates from the file
    			 if (readCoordinates) {
    				 Node graphics = functionElement.getElementsByTagName("graphics").item(0);
    				 
    				 if(graphics.getNodeType() == Node.ELEMENT_NODE) {
        				 Element graphicsElement = (Element) graphics;

    					 Node position = graphicsElement.getElementsByTagName("position").item(0);
    					 if(position.getNodeType() == Node.ELEMENT_NODE) {
    						 
    						 g = new Graphics(Integer.parseInt(position.getAttributes().getNamedItem("height").getNodeValue()),
    								 Integer.parseInt(position.getAttributes().getNamedItem("width").getNodeValue()),
    								 Integer.parseInt(position.getAttributes().getNamedItem("x").getNodeValue()),
    								 Integer.parseInt(position.getAttributes().getNamedItem("y").getNodeValue()));
    					 }
    				 }
    			 }
    			 
    			 // get attributes
    			 // for events and functions we have only annotation attribute
    			 // so there exists only one attribuyte element
    			 HashMap<String, String> annotationMap = null;
    			 boolean initialGW = true;
    			 NodeList attributes = functionElement.getElementsByTagName("attribute");
    			 
    			 if (attributes != null) {
	    			 for (int i = 0; i < attributes.getLength(); i++) {
	    				 Node attribute = attributes.item(i);
						 if(attribute.getNodeType() == Node.ELEMENT_NODE) {
							 if("annotation".equals(attribute.getAttributes().getNamedItem("typeRef").getNodeValue())) {
								 annotationMap = parseAnnotationForEventsAndFunctions(attribute.getAttributes().getNamedItem("value").getNodeValue());
							 }
						 } else if ("added".equals(attribute.getAttributes().getNamedItem("typeRef").getNodeValue())) {
							 // this was added gateway
							 if("true".equals(attribute.getAttributes().getNamedItem("value").getNodeValue())) {
								 initialGW = false;
							 }
						 }
	    			 }
    			 }
    			 // get ID
    			Node id = function.getAttributes().getNamedItem("id");
    			Vertex v = new Vertex(
    					elementName,
    					id.getNodeValue());
    			
    			// add annotations
    			if (annotationMap != null) {
    				v.addAnnotations(annotationMap);
    			}
    			
    			// this is initial gateway
    			if (initialGW) {
    				v.setInitialGW();
    			}
    			
    			NodeList config = functionElement.getElementsByTagName("configurableConnector");
    			
    			if (config.getLength() > 0) {
    				v.setConfigurable(true);
    			}
    			else {
    				v.setInitialGW();
    			}
			 
    			if (g != null) {
    				v.setGraphics(g);
    			}
    			epcGraph.addVertex(v);
    		 }
    	 }
    }

}
