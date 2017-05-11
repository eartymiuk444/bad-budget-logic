package com.erikartymiuk.badbudgetlogic.predictdataclasses;

import java.util.*;

/**
 * Predict data for the money loss object as used by the prediction algorithm
 * to keep track of when the next loss will take place on any given date.
 */
public class PredictDataMoneyLoss
{
	
	private Date date; //The date this object has data on 
	private Date nextLoss; //The next loss assuming it is the date
	
	/**
	 * Constructor for the MoneyLoss predict row. 
	 * @param date - the date of this row that we have data for
	 * @param next - the date of the next loss assuming it is currently
	 * 					the date of this row.
	 */
	public PredictDataMoneyLoss(Date date, Date next)
	{
		this.date = date;
		this.nextLoss = next;
	}
	
	/* Getters and Setters */
	public Date date()
	{
		return this.date;
	}
	
	public Date nextLoss()
	{
		return this.nextLoss;
	}
	
	public void updateNextLoss(Date nextLoss)
	{
		this.nextLoss = nextLoss;
	}
	
}
