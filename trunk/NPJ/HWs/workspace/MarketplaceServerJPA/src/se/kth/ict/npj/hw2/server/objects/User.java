package se.kth.ict.npj.hw2.server.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * The User entity class defines the schema of a User
 * into the DB of the system.
 *
 */
@Entity
public class User implements Serializable{
	@Id
	private String username;
	@Column(nullable=false)
	private String password;
	@Column(nullable=false)
	private String userURL;
	private List<Item> sellingItemList;
	
	public String getUserURL() {
		return userURL;
	}
	public void setUserURL(String userURL) {
		this.userURL = userURL;
	}
	@JoinColumn(nullable=false)
	private UserStatistics userStatistics;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="userstatistics")
	public UserStatistics getUserStatistics() {
		return userStatistics;
	}
	public void setUserStatistics(UserStatistics userStatistics) {
		this.userStatistics = userStatistics;
	}
	@OneToMany(cascade=CascadeType.REMOVE, mappedBy="user", targetEntity=se.kth.ict.npj.hw2.server.objects.Item.class)
	@JoinColumn(nullable=true, updatable=true,referencedColumnName="seller")
	public List<Item> getSellingItemList() {
		return sellingItemList;
	}
	public void setSellingItemList(ArrayList<Item> sellingItemList) {
		this.sellingItemList = sellingItemList;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
