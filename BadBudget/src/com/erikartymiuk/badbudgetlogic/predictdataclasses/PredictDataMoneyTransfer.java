package com.erikartymiuk.badbudgetlogic.predictdataclasses;

import java.util.*;

/**
 * Predict data for the money transfer object as used by the prediction algorithm
 * to keep track of when the next transfer will take place on any given date.
 */
public class PredictDataMoneyTransfer
{
	
	private Date date; //The date this object has data on 
	private Date nextTransfer; //The nextTransfer assuming it is the date
	
	/**
	 * Constructor for the MoneyTransfer predict row. 
	 * @param date - the date of this row that we have data for
	 * @param next - the date of the next transfer assuming it is currently
	 * 					the date of this row.
	 */
	public PredictDataMoneyTransfer(Date date, Date next)
	{
		this.date = date;
		this.nextTransfer = next;
	}
	
	/* Getters and Setters */
	public Date date()
	{
		return this.date;
	}
	
	public Date nextTransfer()
	{
		return this.nextTransfer;
	}
	
	public void updateNextTransfer(Date nextTransfer)
	{
		this.nextTransfer = nextTransfer;
	}
	
}
