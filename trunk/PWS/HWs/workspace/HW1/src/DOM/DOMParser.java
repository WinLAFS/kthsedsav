package DOM;

import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			// Get a factory object for DocumentBuilder objects
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			// to make the parser a validating parse
			//factory.setValidating(true);
			// To parse a XML document with a namespace,
			factory.setNamespaceAware(true);

			// to ignore cosmetic whitespace between elements.
			factory.setIgnoringElementContentWhitespace(true);
			factory.setAttribute(
					"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
					"http://www.w3.org/2001/XMLSchema");
			// specifies the XML schema document to be used for validation.
			/*factory.setAttribute(
					"http://java.sun.com/xml/jaxp/properties/schemaSource",
					"Transcript.xsd");*/

			// Get a DocumentBuilder (parser) object

			DocumentBuilder builder = factory.newDocumentBuilder();

			// Parse the XML input file to create a
			// Document object that represents the
			// input XML file.
			// /
			Document cvXML = builder.parse(new File("XMLs/CV.xml"));
			Document transcriptXML = builder.parse(new File("XMLs/Transcript.xml"));
			Document employmentRecordXML = builder.parse(new File("XMLs/EmploymentRecord.xml"));
			Document companiesInfoXML = builder.parse(new File("XMLs/CompaniesInfo.xml"));
			
			//Create the new DOM tree
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            ////////////////////////
            //Creating the XML tree
            Element root = doc.createElement("profile");
            doc.appendChild(root);
//            
//            String name = cvXML.getElementsByTagName("name").item(0).getTextContent();
//            String sname = cvXML.getElementsByTagName("surname").item(0).getTextContent();
//            
//            Element nameEl = doc.createElement("name");
//            nameEl.setTextContent(name);
//            root.appendChild(nameEl);
//            Element snameEl = doc.createElement("surname");
//            snameEl.setTextContent(sname);
//            root.appendChild(snameEl);
           
           //from CV
            Element ele = cvXML.getDocumentElement();
            NodeList nl = ele.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
            	Node e = doc.importNode(nl.item(i), true);
            	root.appendChild(e);
            }
            
            //from Transcript
            Element degrees = doc.createElement("degrees");
            root.appendChild(degrees);
            Element ele1 = transcriptXML.getDocumentElement();
            NodeList nl1 = ele1.getChildNodes();
            for (int i = 0; i < nl1.getLength(); i++) {
            	Node e = doc.importNode(nl1.item(i), true);
            	degrees.appendChild(e);
            }
            
            //from Employement record
            Element records = doc.createElement("records");
            root.appendChild(records);
            Element ele2 = employmentRecordXML.getDocumentElement();
            NodeList nl2 = ele2.getChildNodes();
            for (int i = 0; i < nl2.getLength(); i++) {
            	Node e = doc.importNode(nl2.item(i), true);
            	records.appendChild(e);
            }
            
            NodeList recordsList = doc.getElementsByTagName("record");
            for (int i = 0; i < recordsList.getLength(); i++) {
            	String compName = recordsList.item(i).getAttributes().getNamedItem("companyName").getNodeValue();
            	System.err.println("");
            }
            
        
          //set up a transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            //create string from xml tree
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
            String xmlString = sw.toString();

            //print xml
            System.out.println("Here's the xml:\n\n" + xmlString);
            
            
			

			// Process the DOM tree, beginning with the
			// Document node to produce the output.
			// Invocation of processDocumentNode starts
			// a recursive process that processes the
			// entire DOM tree.
			// /
			// domParser.processNode(document);
			// OR
			//domParser.processTranscript(document.getFirstChild());

		} catch (Exception e) {

			e.printStackTrace(System.err);
		}// end catch

	}

}
