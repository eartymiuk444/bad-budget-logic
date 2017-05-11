package com.erikartymiuk.badbudgetlogic.predictdataclasses;

import java.util.*;

/**
 * Predict data for the money Gain object as used by the prediction algorithm
 * to keep track of when the next deposit will take place on any given date.
 */
public class PredictDataMoneyGain
{
	
	private Date date; //The date this object has data on 
	private Date nextDeposit; //The nextDeposit assuming it is the date
	
	/**
	 * Constructor for the MoneyGain predict row. 
	 * @param date - the date of this row that we have data for
	 * @param next - the date of the next contribution assuming it is currently
	 * 					the date of this row.
	 */
	public PredictDataMoneyGain(Date date, Date next)
	{
		this.date = date;
		this.nextDeposit = next;
	}
	
	/* Getters and Setters */
	public Date date()
	{
		return this.date;
	}
	
	public Date nextDeposit()
	{
		return this.nextDeposit;
	}
	
	public void updateNextDeposit(Date nextDeposit)
	{
		this.nextDeposit = nextDeposit;
	}
	
}
