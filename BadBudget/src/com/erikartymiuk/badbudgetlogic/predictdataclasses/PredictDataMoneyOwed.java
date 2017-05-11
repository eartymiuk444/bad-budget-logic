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
	
	
	/**
	 * Constructor for the predict data money owed object. 
	 * @param date - the date this row has data on
	 * @param value - the value on the date
	 * @param nextDate - the next payment date to initialize this to
	 * @param nextInterestAccumulationDate - the date of the next interest accumulation
	 */
	public PredictDataMoneyOwed(Date date, double value, Date nextDate, Date nextInterestAccumulationDate)
	{
		super(date, value);
		this.nextPaymentDate = nextDate;
		this.nextInterestAccumulationDate = nextInterestAccumulationDate;
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
	
}
