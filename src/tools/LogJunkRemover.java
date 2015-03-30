package tools;

import java.io.File;
import java.io.StringWriter;

import javax.xml.bind.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bsh.Remote;

public class LogJunkRemover {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String basePath = "/home/chathura/projects/qut/mining/apromore-mining/tests/t_smd/run_105_ws2";
		LogJunkRemover logJunkRemover = new LogJunkRemover();
		try {
//			logJunkRemover.clean(basePath);
			logJunkRemover.fixNames(basePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		try {
//			logJunkRemover.removeJunk(new File("/home/chathura/projects/qut/mining/apromore-mining/tests/t_smd/temp1/77_1.mxml"));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private void clean(String basePath) throws Exception {
		File base = new File(basePath);
		File[] ds = base.listFiles();
		for (File d : ds) {
			if (d.isDirectory() && d.getName().startsWith("bpi")) {
				File logs = new File(d, "logs");
				File cleanedLogs = new File(d, "logs2");
				cleanedLogs.mkdir();
				System.out.println("Processing: " + d.getName());
				
				File[] logFiles = logs.listFiles();
				for (File logFile : logFiles) {
					File outFile = new File(cleanedLogs, logFile.getName());
					removeJunk2(logFile, outFile);
				}
			}
		}
	}
	
	private void fixNames(String basePath) throws Exception {
		File base = new File(basePath);
		File[] ds = base.listFiles();
		for (File d : ds) {
			if (d.isDirectory() && d.getName().startsWith("bpi")) {
				File logs = new File(d, "logs");
				FileUtils.deleteDirectory(logs);
				File newLogs = new File(d, "logs2");
				newLogs.renameTo(new File(d, "logs"));
			}
		}
	}
	
	private void removeJunk2(File logFile, File outFile) throws Exception {
		
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
//		NodeList links = (NodeList) xpath.evaluate("rootNode/link", element,
//		    XPathConstants.NODESET);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(logFile);
		
		String expression = "/WorkflowLog/Process/Data";
		Node dataNode = (Node) xpath.compile(expression).evaluate(doc, XPathConstants.NODE);
		if (dataNode != null) {
			((Node) dataNode).getParentNode().removeChild(dataNode);
		}
		
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);

		String xmlString = result.getWriter().toString();
		FileUtils.write(outFile, xmlString);
	}

	private void removeJunk(File logFile, File outFile) throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(logFile);
		Node dataNode = (Node) doc.getElementsByTagName("Data").item(0);
		// Remove the node
		((Node) dataNode).getParentNode().removeChild(dataNode);
		
		Node processNode = (Node) doc.getElementsByTagName("Data").item(0);

		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);

		String xmlString = result.getWriter().toString();
		FileUtils.write(outFile, xmlString);
	}

}
