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
	private boolean valueChangedByTransfer; //Indicates if the value of this savings account was impacted by a transfer (for use in determining goal validity)
	
	private double accumulatedInterest;
	
	/**
	 * Constructor for the predict date savings account. 
	 * @param date - the date this row has data on
	 * @param value - the value on the date
	 * @param nextDate - the next contribution date from the previous day (will be updated by prediction algorithm)
	 * @param accumulatedInterest - the accumulatedInterest on the date.
	 * @param valueChangedByTransfer - indicates if the value of a savings account was impacted by a transfer (for use in determining goal validity)
	 */
	public PredictDataSavingsAccount(Date date, double value, Date nextDate, Date nextInterestAccumulationDate, double accumulatedInterest, boolean valueChangedByTransfer)
	{
		super(date, value);
		this.nextContributionDate = nextDate;
		this.nextInterestAccumulationDate = nextInterestAccumulationDate;
		this.accumulatedInterest = accumulatedInterest;
		this.valueChangedByTransfer = valueChangedByTransfer;
	}

	public boolean isValueChangedByTransfer() {
		return valueChangedByTransfer;
	}

	public void setValueChangedByTransfer(boolean valueChangedByTransfer) {
		this.valueChangedByTransfer = valueChangedByTransfer;
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
	
	
	/**
	 * This gets the total accumulated interest throughout the history (i.e. from the start day of the prediction up
	 * to the day represented by this predict data row) for the savings account.
	 * @return - the total accumulated interest up to the day this row represents for the savings account attached to the predict data.
	 */
	public double getAccumulatedInterest() {
		return accumulatedInterest;
	}

	public void setAccumulatedInterest(double accumulatedInterest) {
		this.accumulatedInterest = accumulatedInterest;
	}
	
}
