package se.kth.ict.npj.hw2.server.objects;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * The UserStatistics class defines the schema into the
 * database for the user statistics. 
 *
 */
@Entity
public class UserStatistics implements Serializable {
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private long statisticsId;
	
	@Column(nullable=false)
	private int sellsNumber;
	@Column(nullable=false)
	private int buysNumber;
	
	public long getStatisticsId() {
		return statisticsId;
	}
	public void setStatisticsId(long statisticsId) {
		this.statisticsId = statisticsId;
	}
	public int getSellsNumber() {
		return sellsNumber;
	}
	public void setSellsNumber(int sellsNumber) {
		this.sellsNumber = sellsNumber;
	}
	public int getBuysNumber() {
		return buysNumber;
	}
	public void setBuysNumber(int buysNumber) {
		this.buysNumber = buysNumber;
	}
	
}
