/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jobservice;

import companiesClient.CompaniesWS;
import companiesClient.CompaniesWSService;
import employmentClient.EmploymentOffice;
import employmentClient.EmploymentOfficeService;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.WebServiceRef;
import jaxbgenerated.companiesinfo.Companies;
import jaxbgenerated.companiesinfo.Company;
import jaxbgenerated.cv.CV;
import jaxbgenerated.employment.Records;
import jaxbgenerated.employment.Records.Record;
import jaxbgenerated.profile.ObjectFactory;
import jaxbgenerated.profile.Profile;
import jxbgenerated.transcript.Degree;
import jxbgenerated.transcript.Degrees;
import jxbgenerated.transcript.Grade;
import jxbgenerated.transcript.Grades;
import recruitmentClient.FindJobsResponse;
import recruitmentClient.RecruiterCompany;
import recruitmentClient.RecruiterCompanyService;
import universityClient.UniversityWS;
import universityClient.UniversityWSService;

/**
 *
 * @author Shum
 */
@WebService()
public class JobService {
//    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_11983/JobServiceCompany/RecruiterCompanyService.wsdl")
//    private RecruiterCompanyService service;

    /**
     * Web service operation
     */
    @WebMethod(operationName = "createProfile")
    public String createProfile(String cv) {
        try {
            ServicesFetch sf = new ServicesFetch();

            //Parsing CV
            JAXBContext jcCV = JAXBContext.newInstance("jaxbgenerated.cv");
            Unmarshaller unmarshallerCV = jcCV.createUnmarshaller();
            CV cvData = (CV) unmarshallerCV.unmarshal(new ByteArrayInputStream(cv.getBytes()));

            //Getting and parsing transcripts
            URL universityUrl = new URL(sf.fetchUniversityService());
//            URL universityUrl = new URL(sf.fetchUniversityService());
//            URL universityUrl = getWSDLURL("http://localhost:11983/JobServiceCompany/universityWSService?wsdl");
            UniversityWSService universityService = new UniversityWSService(universityUrl);
            UniversityWS universityPort = universityService.getUniversityWSPort();
            String universityTranscriptsStr = universityPort.getDegree(cvData.getSurname());

            JAXBContext jcTranscript = JAXBContext.newInstance("jxbgenerated.transcript");
            Unmarshaller unmarshallerTranscript = jcTranscript.createUnmarshaller();
            Degrees degrees = (Degrees) unmarshallerTranscript.unmarshal(new ByteArrayInputStream(universityTranscriptsStr.getBytes()));

            //Getting and parsing employment records
            URL employmentUrl = new URL(sf.fetchEmploymentOfficeService());
//            URL employmentUrl = getWSDLURL("http://localhost:11983/JobServiceCompany/EmploymentOfficeService?wsdl");
            EmploymentOfficeService employmentService = new EmploymentOfficeService(employmentUrl);
            EmploymentOffice employmentPort = employmentService.getEmploymentOfficePort();
            String employmentStr = employmentPort.getEmploymentRecord(cvData.getPersonumme());

            JAXBContext jcEmployment = JAXBContext.newInstance("jaxbgenerated.employment");
            Unmarshaller unmarshallerEmployment = jcEmployment.createUnmarshaller();
            Records records = (Records) unmarshallerEmployment.unmarshal(new ByteArrayInputStream(employmentStr.getBytes()));

            //Getting and parsing companies info
            ArrayList<Companies> companiesList = new ArrayList<Companies>();

            URL companiesUrl = getWSDLURL("http://localhost:11983/JobServiceCompany/CompaniesWSService?wsdl");
            CompaniesWSService companiesService = new CompaniesWSService(companiesUrl);
            CompaniesWS companiesPort = companiesService.getCompaniesWSPort();

            List<Record> recordsList = records.getRecord();
            Iterator<Record> rIt = recordsList.iterator();
            while (rIt.hasNext()) {
                String companiesStr = companiesPort.getCompanyInfo(rIt.next().getCompanyName());
                JAXBContext jcCompanies = JAXBContext.newInstance("jaxbgenerated.companiesinfo");
                Unmarshaller unmarshallerCompanies = jcCompanies.createUnmarshaller();
                Companies companies = (Companies) unmarshallerCompanies.unmarshal(new ByteArrayInputStream(companiesStr.getBytes()));
                companiesList.add(companies);
            }

            //Logic!!!
            //creating XML
            ObjectFactory profileFactory = new ObjectFactory();


            Profile profileComplex = profileFactory.createProfile();
            profileComplex.setBirthDate(cvData.getBirthDate());
            profileComplex.setName(cvData.getName());
            profileComplex.setSurname(cvData.getSurname());

            jaxbgenerated.profile.Degrees profileDegrees = profileFactory.createDegrees();
            List<Degree> degreeList = degrees.getDegree();
            Iterator<Degree> degreeIt = degreeList.iterator();
            while (degreeIt.hasNext()) {
                jaxbgenerated.profile.Degree profileDegree = profileFactory.createDegree();
                Degree degree = degreeIt.next();
                profileDegree.setEndYear(degree.getEndYear());
                profileDegree.setStartYear(degree.getStartYear());
                profileDegree.setSubject(degree.getSubject());
                profileDegree.setTitle(degree.getTitle());
                profileDegree.setUniversity(degree.getUniversity());
                Grades grades = degree.getGrades();
                List<Grade> gradeList = grades.getGrade();
                Iterator<Grade> gradeIt = gradeList.iterator();
                jaxbgenerated.profile.Grades profileGrades = profileFactory.createGrades();
                profileDegree.setGrades(profileGrades);
                double sum = 0;
                while (gradeIt.hasNext()) {
                    jaxbgenerated.profile.Grade profileGrade = profileFactory.createGrade();
                    Grade grade = gradeIt.next();
                    profileGrade.setGradeVal(grade.getGradeVal());
                    sum = sum + new Double(grade.getGradeVal());
                    profileGrade.setCourseID(grade.getCourseID());
                    profileGrades.getGrade().add(profileGrade);
                }
                double gpa = sum / profileGrades.getGrade().size();
                profileDegree.setGpa(new BigDecimal(gpa));
                profileDegrees.getDegree().add(profileDegree);
            }
            profileComplex.setDegrees(profileDegrees);

            jaxbgenerated.profile.Records profileRecords = profileFactory.createRecords();
            List<Record> recordList = records.getRecord();
            Iterator<Record> recordIt = recordList.iterator();
            while (recordIt.hasNext()) {
                jaxbgenerated.profile.Records.Record profileRecord = profileFactory.createRecordsRecord();
                Record record = recordIt.next();
                profileRecord.setCompanyName(record.getCompanyName());
                profileRecord.setFromDate(record.getFromDate());
                profileRecord.setPosition(record.getPosition());
                profileRecord.setToDate(record.getToDate());


                Iterator<Companies> compIt = companiesList.iterator();
                while (compIt.hasNext()) {
                    Companies companies = compIt.next();
                    Company company = companies.getCompany().get(0);
                    if (profileRecord.getCompanyName().equalsIgnoreCase(company.getCompanyName())) {
                        profileRecord.setSite(company.getSite());
                    }
                }
                profileRecords.getRecord().add(profileRecord);
            }
            profileComplex.setRecords(profileRecords);

            JAXBContext profileContext = JAXBContext.newInstance("jaxbgenerated.profile");
            Marshaller marshaller = profileContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.kth.se/profile profile.xsd");
            StringWriter sw = new StringWriter();
            marshaller.marshal(profileComplex, sw);

            URL url = new URL(sf.fetchRecruiterService());
//            URL url = getWSDLURL("http://localhost:11983/JobServiceCompany/RecruiterCompanyService?wsdl");
            RecruiterCompanyService service1 = new RecruiterCompanyService(url);
            RecruiterCompany s1 = service1.getRecruiterCompanyPort();
            List<String> keyw = new ArrayList<String>();
            keyw.add("Software");
            keyw.add("Greece");
            System.out.println(s1.findJobs(keyw));

            callAsynCallback(keyw);
            
            return sw.toString();

        } catch (Exception ex) {
            Logger.getLogger(JobService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cv;
    }

    private static URL getWSDLURL(String urlStr) {
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return url;
    }

    
    private static void callAsynCallback(List<String> keys){

        try { // Call Web Service Operation(async. callback)
            recruitmentClient.RecruiterCompanyService service = new recruitmentClient.RecruiterCompanyService();
            recruitmentClient.RecruiterCompany port = service.getRecruiterCompanyPort();
            // TODO initialize WS operation arguments here
            java.util.List<java.lang.String> arg0 = keys;
            javax.xml.ws.AsyncHandler<recruitmentClient.FindJobsResponse> asyncHandler = new javax.xml.ws.AsyncHandler<recruitmentClient.FindJobsResponse>() {
                public void handleResponse(javax.xml.ws.Response<recruitmentClient.FindJobsResponse> response) {
                    try {
                        // TODO process asynchronous response here
                        System.out.println("Result from async call  = \n"+ ((FindJobsResponse)response.get()).getReturn());
                    } catch(Exception ex) {
                        // TODO handle exception
                    }
                }
            };
            java.util.concurrent.Future<? extends java.lang.Object> result = port.findJobsAsync(arg0, asyncHandler);
            while(!result.isDone()) {
                System.out.println("WAITING");
                Thread.sleep(10);
            }
        } catch (Exception ex) {
            // TODO handle custom exceptions here
        }

    }
}
