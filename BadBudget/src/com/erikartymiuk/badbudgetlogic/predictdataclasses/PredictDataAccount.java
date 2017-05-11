package com.erikartymiuk.badbudgetlogic.predictdataclasses;
import java.util.*;

/**
 * This is the base account "row" in our list of values used in the prediction algorithm
 * After the prediction algorithm is run this row tells us the value of whatever
 * account it is a part of on the date. It also gives us the transaction history
 * on this date. (Would have to combine all rows to get full history)
 */
public class PredictDataAccount 
{
	private Date date; //This is the date of interest.
	private double value; //This is the value on the date of interest.
	private ArrayList<TransactionHistoryItem> transactionHistoryItems; //This is the transaction history (as a list of history items
																		//representing individual transactions) on the date of interest	
	/**
	 * Constructor for this class.
	 * @param date - the date of interest
	 * @param value - the value of the account on the date
	 */
	public PredictDataAccount(Date date, double value)
	{
		this.date = date;
		this.value = value;
		this.transactionHistoryItems = null; //will only initilize if something happens on this day
	}
	
	/**
	 * Add a new transaction item to the list of the transaction history
	 * up to this predictData's date
	 */
	public void addHistoryItem(TransactionHistoryItem historyItem)
	{
		if (this.transactionHistoryItems == null)
		{
			this.transactionHistoryItems = new ArrayList<TransactionHistoryItem>();
		}
		this.transactionHistoryItems.add(historyItem);
	}
	
	/* Getters and setters */
	public Date date()
	{
		return this.date;
	}
	
	public double value()
	{
		return this.value;
	}
	
	public List<TransactionHistoryItem> transactionHistory()
	{
		return this.transactionHistoryItems;
	}
	
	public void updateValue(double v)
	{
		this.value = v;
	}
}
