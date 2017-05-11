package com.erikartymiuk.badbudgetlogic.predictdataclasses;
import java.util.*;

/**
 * Predict Data for a savings account. Along with the base account predict data
 * a savings account needs to track the next date for its contribution.
 */
public class PredictDataSavingsAccount extends PredictDataAccount 
{	
	private Date nextInterestAccumulationDate;
	private Date nextContributionDate; //The date of the savings account next contribution for a particular day
	/**
	 * Constructor for the predict date savings account. 
	 * @param date - the date this row has data on
	 * @param value - the value on the date
	 * @param nextDate - the next contribution date from the previous day (will be updated by prediction algorithm)
	 */
	public PredictDataSavingsAccount(Date date, double value, Date nextDate, Date nextInterestAccumulationDate)
	{
		super(date, value);
		this.nextContributionDate = nextDate;
		this.nextInterestAccumulationDate = nextInterestAccumulationDate;
	}

	public Date getNextInterestAccumulationDate() {
		return nextInterestAccumulationDate;
	}

	public void setNextInterestAccumulationDate(Date nextInterestAccumulationDate) {
		this.nextInterestAccumulationDate = nextInterestAccumulationDate;
	}

	/** Method to update the date a contribution will next be processed.
	 * @param next - the next contribution date to update to
	 * 
	 */
	public void updateContributionDate(Date next)
	{
		this.nextContributionDate = next;
	}
	
	/** 
	 * Returns the next date for the contribution
	 */
	public Date getNextContributionDate()
	{
		return this.nextContributionDate;
	}
	
}
