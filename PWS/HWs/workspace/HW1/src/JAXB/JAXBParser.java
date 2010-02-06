package JAXB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import JAXB.CV.BaseData;
import JAXB.CV.BaseDataExtra;
import JAXB.CV.CV;
import JAXB.CompaniesInfo.Companies;
import JAXB.CompaniesInfo.Company;
import JAXB.CompaniesInfo.ObjectFactory;
import JAXB.EmploymentRecord.Records;
import JAXB.EmploymentRecord.Records.Record;
import JAXB.Transcript.Degree;
import JAXB.Transcript.Degrees;
import JAXB.Transcript.Grade;
import JAXB.Transcript.Grades;
import JAXB.profile.Profile;
import JAXB.profile.ProfileComplex;

public class JAXBParser {

	/**
	 * @param args
	 * @throws JAXBException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws JAXBException, FileNotFoundException {
		
		//creating objects
		JAXBContext jcCompaniesInfo = JAXBContext.newInstance("JAXB.CompaniesInfo");
		Unmarshaller unmarshallerCompaniesInfo = jcCompaniesInfo.createUnmarshaller();
		Companies companies = (Companies) unmarshallerCompaniesInfo.unmarshal(new File("XMLs/CompaniesInfo.xml"));

		JAXBContext jcCV = JAXBContext.newInstance("JAXB.CV");
		Unmarshaller unmarshallerCV = jcCV.createUnmarshaller();
		Object obj = unmarshallerCV.unmarshal(new File("XMLs/CV.xml"));
		System.out.println(obj.getClass());
		CV baseData = (CV) unmarshallerCV.unmarshal(new File("XMLs/CV.xml"));
		
		JAXBContext jcEmploymentRecord = JAXBContext.newInstance("JAXB.EmploymentRecord");
		Unmarshaller unmarshallerEmploymentRecord = jcEmploymentRecord.createUnmarshaller();
		Records records = (Records) unmarshallerEmploymentRecord.unmarshal(new File("XMLs/EmploymentRecord.xml"));
		
		JAXBContext jcTranscript = JAXBContext.newInstance("JAXB.Transcript");
		Unmarshaller unmarshallerTranscript = jcTranscript.createUnmarshaller();
		Degrees degrees = (Degrees) unmarshallerTranscript.unmarshal(new File("XMLs/Transcript.xml"));
		
		//creating XML
		JAXB.profile.ObjectFactory profileFactory = new JAXB.profile.ObjectFactory();
		
		
		Profile profileComplex = profileFactory.createProfile();
		profileComplex.setBirthDate(baseData.getBirthDate());
		profileComplex.setName(baseData.getName());
		profileComplex.setSurname(baseData.getSurname());
		
		JAXB.profile.Degrees profileDegrees = profileFactory.createDegrees();
		List<Degree> degreeList= degrees.getDegree();
		Iterator<Degree> degreeIt = degreeList.iterator();
		while(degreeIt.hasNext()){
			JAXB.profile.Degree profileDegree = profileFactory.createDegree();
			Degree degree = degreeIt.next();
			profileDegree.setEndYear(degree.getEndYear());
			profileDegree.setStartYear(degree.getStartYear());
			profileDegree.setSubject(degree.getSubject());
			profileDegree.setTitle(degree.getTitle());
			profileDegree.setUniversity(degree.getUniversity());
			Grades grades = degree.getGrades();
			List<Grade> gradeList = grades.getGrade();
			Iterator<Grade> gradeIt = gradeList.iterator();
			JAXB.profile.Grades profileGrades = profileFactory.createGrades();
			profileDegree.setGrades(profileGrades);
			double sum = 0;
			while(gradeIt.hasNext()){
				JAXB.profile.Grade profileGrade = profileFactory.createGrade();
				Grade grade = gradeIt.next();
				profileGrade.setGradeVal(grade.getGradeVal());
				sum = sum + new Double(grade.getGradeVal());
				profileGrade.setCourseID(grade.getCourseID());
				profileGrades.getGrade().add(profileGrade);
			}
			double gpa = sum/profileGrades.getGrade().size();
			profileDegree.setGpa(new BigDecimal(gpa));
			profileDegrees.getDegree().add(profileDegree);
		}
		profileComplex.setDegrees(profileDegrees);
		
		JAXB.profile.Records profileRecords = profileFactory.createRecords();
		List<Record> recordList = records.getRecord();
		Iterator<Record> recordIt = recordList.iterator();
		while(recordIt.hasNext()){
			JAXB.profile.Records.Record profileRecord = profileFactory.createRecordsRecord();
			Record record = recordIt.next();
			profileRecord.setCompanyName(record.getCompanyName());
			profileRecord.setFromDate(record.getFromDate());
			profileRecord.setPosition(record.getPosition());
			profileRecord.setToDate(record.getToDate());
			
			List<Company> companyList = companies.getCompany();
			Iterator<Company> compIt = companyList.iterator();
			while(compIt.hasNext()){
				Company company = compIt.next();
				if(profileRecord.getCompanyName().equalsIgnoreCase(company.getCompanyName())){
					profileRecord.setSite(company.getSite());
				}
			}
			profileRecords.getRecord().add(profileRecord);
		}
		profileComplex.setRecords(profileRecords);
		
		JAXBContext profileContext = JAXBContext.newInstance("JAXB.profile");
		Marshaller marshaller = profileContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
		marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.kth.se/profile profile.xsd");
		marshaller.marshal(profileComplex, new FileOutputStream("XMLs/profile_JAXB.xml"));



	}

}
