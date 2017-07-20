package com.erikartymiuk.badbudgetlogic.predictdataclasses;
import java.util.*;

/**
 * Predict Data for a moneyOwed object/account. Along with the base account predict data
 * a money owed account needs to track the next date of its payment (if it has one)
 */
public class PredictDataMoneyOwed extends PredictDataAccount 
{	
	private Date nextPaymentDate; //The next payment date
	private Date nextInterestAccumulationDate;
	
	private double accumulatedInterest;

	/**
	 * Constructor for the predict data money owed object. 
	 * @param date - the date this row has data on
	 * @param value - the value on the date
	 * @param nextDate - the next payment date to initialize this to
	 * @param nextInterestAccumulationDate - the date of the next interest accumulation
	 * @param accumulatedInterest - the accumulated interest on the date
	 */
	public PredictDataMoneyOwed(Date date, double value, Date nextDate, Date nextInterestAccumulationDate, double accumulatedInterest)
	{
		super(date, value);
		this.nextPaymentDate = nextDate;
		this.nextInterestAccumulationDate = nextInterestAccumulationDate;
		this.accumulatedInterest = accumulatedInterest;
	}
	
	/** Method to update the date a payment will next be processed. 
	 * @param next - the next payment date to update to
	 */
	public void updateNextPaymentDate(Date next)
	{
		this.nextPaymentDate = next;
	}
	
	/** Returns the next date for the payment on this day
	 * 
	 */
	public Date getNextPaymentDate()
	{
		return this.nextPaymentDate;
	}
	
	public Date getNextInterestAccumulationDate() {
		return nextInterestAccumulationDate;
	}

	public void setNextInterestAccumulationDate(Date nextInterestAccumulationDate) {
		this.nextInterestAccumulationDate = nextInterestAccumulationDate;
	}
	
	/**
	 * This gets the total accumulated interest throughout the history (i.e. from the start day of the prediction up
	 * to the day represented by this predict data row) for the debt.
	 * @return - the total accumulated interest up to the day this row represents for the debt attached to the predict data.
	 */
	public double getAccumulatedInterest() {
		return accumulatedInterest;
	}

	public void setAccumulatedInterest(double accumulatedInterest) {
		this.accumulatedInterest = accumulatedInterest;
	}
	
}
