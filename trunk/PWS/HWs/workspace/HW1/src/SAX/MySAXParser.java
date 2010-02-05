package SAX;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MySAXParser {

	static Document doc = null;
	static Element root = null;
	static Element nameEl = null;
	static Element degreesEl = null;
	static Element degreeEl = null;
	static Element gradesEl = null;
	static Element gradeEl = null;
	static Element recordsEl = null;
	static Element recordEl = null;
	static String curElement="";
	static String curCompany="";

	public static void main(String[] args) {
		try {
			// prepare writer
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			doc = docBuilder.newDocument();
			root = doc.createElement("profile");
			doc.appendChild(root);

			// Create Parser
			SAXParserFactory saxpf = SAXParserFactory.newInstance();
			SAXParser saxp = saxpf.newSAXParser();

			saxp.parse("XMLs/CV.xml", new MyCVParser());
			saxp.parse("XMLs/Transcript.xml", new MyTranscriptParser());
			
			saxp.parse("XMLs/EmploymentRecord.xml", new MyEmploymentRecordParser());
			saxp.parse("XMLs/CompaniesInfo.xml", new MyCompaniesInfoParser());
			

			// set up a transformer
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			// create string from xml tree
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			String xmlString = sw.toString();

			// print xml
			System.out.println("Here's the xml:\n\n" + xmlString);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	static class MyCompaniesInfoParser extends DefaultHandler {

		public MyCompaniesInfoParser() {
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if(qName.equalsIgnoreCase("company")){
				curCompany = attributes.getValue(0);
			} else if(qName.equalsIgnoreCase("site")){
				if(!curCompany.equals("")){
					curElement = "site";
				}
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if(curElement.equalsIgnoreCase("site")){
				 NodeList recordsList = doc.getElementsByTagName("record");
		            for (int i = 0; i < recordsList.getLength(); i++) {
		            	String compName = recordsList.item(i).getAttributes().getNamedItem("companyName").getNodeValue();
		            	if (curCompany.equalsIgnoreCase(compName)) {
		            		Element site = doc.createElement("site");
		            		site.setTextContent(curCompany);
		            		recordsList.item(i).appendChild(site);
		            	}
		            }
		            curElement = "";
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
		}

		public void startDocument() throws SAXException {
		}

		public void endDocument() throws SAXException {
		}
	}

	static class MyEmploymentRecordParser extends DefaultHandler {

		public MyEmploymentRecordParser() {
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if(qName.equalsIgnoreCase("records")){
				curElement = qName;
				recordsEl = doc.createElement("records");
				root.appendChild(recordsEl);
				curElement = "";
			} else if(qName.equalsIgnoreCase("record")){
				curElement = qName;
				recordEl = doc.createElement("record");
				recordEl.setAttribute("companyName", attributes.getValue(0));
				recordsEl.appendChild(recordEl);
				curElement = "";
			} else if(qName.equalsIgnoreCase("fromDate")){
				curElement = qName;
				nameEl = doc.createElement("fromDate");
			} else if(qName.equalsIgnoreCase("toDate")){
				curElement = qName;
				nameEl = doc.createElement("toDate");
			} else if(qName.equalsIgnoreCase("position")){
				curElement = qName;
				nameEl = doc.createElement("position");
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if(curElement.equalsIgnoreCase("fromDate")){
				nameEl.setTextContent((new String(ch)).substring(start, start+length));
				recordEl.appendChild(nameEl);
				curElement = "";
			} else if(curElement.equalsIgnoreCase("toDate")){
				nameEl.setTextContent((new String(ch)).substring(start, start+length));
				recordEl.appendChild(nameEl);
				curElement = "";
			} else if(curElement.equalsIgnoreCase("position")){
				nameEl.setTextContent((new String(ch)).substring(start, start+length));
				recordEl.appendChild(nameEl);
				curElement = "";
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
		}

		public void startDocument() throws SAXException {
		}

		public void endDocument() throws SAXException {
		}
	}

	static class MyTranscriptParser extends DefaultHandler {

		public MyTranscriptParser() {
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if(qName.equalsIgnoreCase("degrees")){
				curElement = qName;
				degreesEl = doc.createElement("degrees");
				root.appendChild(degreesEl);
				curElement = "";
			} else if(qName.equalsIgnoreCase("degree")){
				curElement = qName;
				degreeEl = doc.createElement("degree");
				degreesEl.appendChild(degreeEl);
				curElement = "";
			} else if(qName.equalsIgnoreCase("university")){
				curElement = qName;
				nameEl = doc.createElement("university");
			} else if(qName.equalsIgnoreCase("startYear")){
				curElement = qName;
				nameEl = doc.createElement("startYear");
			} else if(qName.equalsIgnoreCase("endYear")){
				curElement = qName;
				nameEl = doc.createElement("endYear");
			} else if(qName.equalsIgnoreCase("subject")){
				curElement = qName;
				nameEl = doc.createElement("subject");
			} else if(qName.equalsIgnoreCase("grades")){
				curElement = qName;
				gradesEl = doc.createElement("grades");
				degreeEl.appendChild(gradesEl);
				curElement = "";
			} else if(qName.equalsIgnoreCase("grade")){
				curElement = qName;
				gradeEl = doc.createElement("grade");
				gradeEl.setAttribute("courseID", attributes.getValue(0));
				gradesEl.appendChild(gradeEl);
				curElement = "";
			} else if(qName.equalsIgnoreCase("gradeVal")){
				curElement = qName;
				nameEl = doc.createElement("gradeVal");
			} 
			
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if(curElement.equalsIgnoreCase("university")){
				nameEl.setTextContent((new String(ch)).substring(start, start+length));
				degreeEl.appendChild(nameEl);
				curElement = "";
			} else if(curElement.equalsIgnoreCase("startYear")){
				nameEl.setTextContent((new String(ch)).substring(start, start+length));
				degreeEl.appendChild(nameEl);
				curElement = "";
			} else if(curElement.equalsIgnoreCase("endYear")){
				nameEl.setTextContent((new String(ch)).substring(start, start+length));
				degreeEl.appendChild(nameEl);
				curElement = "";
			} else if(curElement.equalsIgnoreCase("subject")){
				nameEl.setTextContent((new String(ch)).substring(start, start+length));
				degreeEl.appendChild(nameEl);
				curElement = "";
			} else if(curElement.equalsIgnoreCase("gradeVal")){
				nameEl.setTextContent((new String(ch)).substring(start, start+length));
				gradeEl.appendChild(nameEl);
				curElement = "";
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
		}

		public void startDocument() throws SAXException {
		}

		public void endDocument() throws SAXException {
		}
	}

	static class MyCVParser extends DefaultHandler {

		public MyCVParser() {
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if(qName.equalsIgnoreCase("name")){
				curElement = qName;
				nameEl = doc.createElement("name");
			} else if(qName.equalsIgnoreCase("surname")){
				curElement = qName;
				nameEl = doc.createElement("surname");
			} else if(qName.equalsIgnoreCase("birthDate")){
				curElement = qName;
				nameEl = doc.createElement("birthDate");
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if(curElement.equalsIgnoreCase("name")){
				nameEl.setTextContent((new String(ch)).substring(start, start+length));
				root.appendChild(nameEl);
				curElement = "";
			} else if(curElement.equalsIgnoreCase("surname")){
				nameEl.setTextContent((new String(ch)).substring(start, start+length));
				root.appendChild(nameEl);
				curElement = "";
			} else if(curElement.equalsIgnoreCase("birthDate")){
				nameEl.setTextContent((new String(ch)).substring(start, start+length));
				root.appendChild(nameEl);
				curElement = "";
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			
		}

		public void startDocument() throws SAXException {
		}

		public void endDocument() throws SAXException {
		}
	}

}
