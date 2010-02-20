/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recruitercompany;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author saibbot
 */
@WebService()
public class RecruiterCompany {

    public String findJobs(String[] keywords) {
        try {
            // Get a factory object for DocumentBuilder objects
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document jobsXML = builder.parse(this.getClass().getResourceAsStream("/recruitercompany/jobadvertisment.xml"));
            //Create the new DOM tree
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            ////////////////////////
            //Creating the XML tree
            Element root = doc.createElement("jobs");
            root.setAttribute("xmlns", "http://xml.netbeans.org/schema/jobadvertisment");
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttribute("xsi:schemaLocation", "http://xml.netbeans.org/schema/jobadvertisment jobadvertisment.xsd");
            doc.appendChild(root);

            Element jobs = jobsXML.getDocumentElement();
            NodeList jobList = jobs.getChildNodes();
            for (int i = 0; i < jobList.getLength(); i++) {
                Node job = jobList.item(i);
                if (job.getNodeType() != Node.TEXT_NODE) {
                    NodeList keysList = job.getChildNodes();
                    for (int j = 0; j < keysList.getLength(); j++) {
                        Node key = keysList.item(j);
                        if (key.getNodeType() != Node.TEXT_NODE) {
                            String text = key.getTextContent().toLowerCase();
                            for (String s : keywords) {
                                if (text.indexOf(s.toLowerCase()) >= 0) {
//                                    root.appendChild(job);
                                    Node e = doc.importNode(job, true);
                                    root.appendChild(e);
                                    break;
                                }
                            }
                        }
                        
                    }
                }

            }

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

            return xmlString;
        } catch (TransformerException ex) {
            Logger.getLogger(RecruiterCompany.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(RecruiterCompany.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RecruiterCompany.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(RecruiterCompany.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
