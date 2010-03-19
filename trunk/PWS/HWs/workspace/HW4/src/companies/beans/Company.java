package companies.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Company {
	private String name;
	private String site;
	private String address;
	private String phone;
	
	public Company(String name, String site, String address, String phone) {
		super();
		this.name = name;
		this.site = site;
		this.address = address;
		this.phone = phone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Company(){}
}
