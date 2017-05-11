package com.erikartymiuk.badbudgetlogic.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.erikartymiuk.badbudgetlogic.predictdataclasses.*;

/**
 * The basic account class representing cash accounts.
 * Examples would include a checking account, cash, retirement accounts, 
 * and, a savings account although a savings account should be
 * a member of the subclass SavingsAccount, as it allows
 * for setting up regular contributions.
 * 
 */
public class Account implements Source
{
	
	private String name; //User entered descriptor of this account		
	private double value; //Current amount of money in this account
	private boolean quicklook; //Indicates if this shows up in the quickLook box for predictions.
	
	private ArrayList<PredictDataAccount> predictDataRows; //For use with the prediction algorithm. Keeps track
												//of this accounts value on each date between the start
												//and targetDate. Each row should hold data for a single day.

	/** Constructor for cash accounts (excludes savings accounts)
	 * 
	 *  @param name - User description of account (non-null and not empty)
	 *  @param value - Initial value of account
	 *  @param quicklook - Should this value show up in quickLook predictions
	 * @throws BadBudgetInvalidValueException 
	 *   
	 */
	public Account (String name, double value, boolean quicklook) throws BadBudgetInvalidValueException
	{
		super();
		int error = verifyValues(name);
		if (error == 0)
		{
			this.name = name;
			this.value = value;
			this.quicklook = quicklook;
			this.predictDataRows = new ArrayList<PredictDataAccount>();
		}
		else
		{
			String message = "";
			if (error == 1)
			{
				message = BadBudgetInvalidValueException.ACCOUNT_NAME_NULL;
			}
			else if (error == 2)
			{
				message = BadBudgetInvalidValueException.ACCOUNT_NAME_EMPTY;
			}
			else
			{
				message = BadBudgetInvalidValueException.UNKNOWN_UNDEFINED;
			}
			throw new BadBudgetInvalidValueException(message);
		}
	}
	
	/**
	 * Verifies that the values on account creation are valid.
	 * @param name - the name of the account being created
	 * @return an error code indicating various error states
	 * 			0 - Values are error free
	 * 			1 - The name is null
	 * 			2 - The name is empty
	 */
	public static int verifyValues(String name)
	{
		if (name == null)
		{
			return 1;
		}
		else if (name.equals(""))
		{
			return 2;
		}
		else
		{
			return 0;
		}
	}
	
	/** Get the PredictData at row index. Each row of the predict data list
	 * should hold data for a single day. It is up to the prediction algorithm
	 * to correctly populate and retrieve items from this list.
	 * @param index - the index representing the day since the start day that we want
	 * 					to examine.
	 * 
	 */
	public PredictDataAccount getPredictData(int index)
	{
		return this.predictDataRows.get(index);
	}
	
	/** Sets a row in this accounts predictData list. It is up to the prediction algorithm
	 * to correctly maintain this list during the course of its run.
	 * 
	 * @param index - the index of the list element to set, should correspond to a date
	 * @param pda - the predictData to set this element to
	 * 
	 */
	public void setPredictData(int index, PredictDataAccount pda)
	{
		if (index < this.predictDataRows.size())
		{
			this.predictDataRows.set(index, pda);
		}
		else
		{
			this.predictDataRows.add(pda);
		}
	}
	
	/**
	 * Adds the specified predict data to the end of this account predict data list
	 * 
	 * @param pda - the predict data to add
	 */
	public void addPredictData(PredictDataAccount pda)
	{
		this.predictDataRows.add(pda);
	}
	
	/** Returns the predict data row for the given day. Should first call the predict method of
	 * the prediction class with day between the start and target dates that the prediction was
	 * done with.
	 * 
	 * @param day - the day to retrieve the prediction for
	 * 
	 * @return - the predict data for the given day
	 */
	public PredictDataAccount getPrediction(Date day)
	{
		int index = Prediction.numDaysBetween(this.predictDataRows.get(0).date(), day);
		return this.predictDataRows.get(index);
	}
	
	/**
	 * Deposit money (increase an accounts value).
	 * 
	 * @param amount - the amount to increase the account's value
	 * 
	 */
	public void deposit(double amount)
	{
		this.value += amount;
	}
	
	/**
	 * Withdraw money (decrease an accounts value). Returns true if account remains positive (or 0)
	 * after withdrawal. Returns negative if account went negative after withdrawal.
	 * Withdrawal still occurs.
	 * 
	 * @param amount - the amount to decrease the account's value by
	 * @return - indicates if this withdrawal caused the account to go negative
	 * 
	 */
	public boolean withdraw(double amount)
	{
		this.value -= amount;
		if (this.value >= 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/* Getters and setters */
	public boolean quickLook()
	{
		return this.quicklook;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setValue(double v)
	{
		this.value = v;
	}
	
	public void setQuickLook(boolean quickLook)
	{
		this.quicklook = quickLook;
	}
	
	public String name()
	{
		return this.name;
	}
	
	public double value()
	{
		return this.value;
	}

	/**Source Interface implementation method. For accounts the account value is reduced by the lossAmount on
	 * the day specified by dayIndex. This is the predict loss implementation for use
	 * with the prediction algorithm. It processes the loss for this source by updating its
	 * prediction rows appropriately. This
	 * method also handles an add back to the source.
	 * 
	 * @param destinationDescription - a string description of what the funds are being used for. Used for the transaction history
	 * @param lossAmount - the amount of funds needed
	 * @param addBack - true if the destination is a budget item and that budget item has funds to add back to the source
	 * @param addBackAmount - the amount to add back to the source (if applicable)
	 * @param dayIndex - the date as an index or offset from prediction start date
	 * 
	 */
	public void predictLossForDayIndex(String destinationDescription, double lossAmount, boolean addBack, double addBackAmount, int dayIndex) 
	{
		PredictDataAccount pda = this.getPredictData(dayIndex);
		double originalAccountValue = pda.value();
		
		if (addBack && !(addBackAmount == 0))
		{
			String addBackSource = destinationDescription;

			pda.updateValue(pda.value() + addBackAmount);			
			TransactionHistoryItem addBackHistoryItem = new TransactionHistoryItem(pda.date(), addBackAmount, TransactionHistoryItem.BUDGET_ITEM_ADD_BACK_ACTION,
					addBackSource, -1, -1, TransactionHistoryItem.DEFAULT_DESTINATION_ACTION, this.name(), originalAccountValue, pda.value(),
					false, true);
			pda.addHistoryItem(addBackHistoryItem);
			
			originalAccountValue = pda.value();
		}
		
		pda.updateValue(pda.value() - lossAmount);
		
		TransactionHistoryItem historyItemAccount = new TransactionHistoryItem(pda.date(), lossAmount, TransactionHistoryItem.ACCOUNT_SOURCE_ACTION,
				this.name, originalAccountValue, pda.value(), TransactionHistoryItem.DEFAULT_DESTINATION_ACTION, 
				destinationDescription, -1, -1, 
				true, false);
		pda.addHistoryItem(historyItemAccount);
	}
	
	/**
	 * Empties this account's predict data list. (Both the vanilla and source rows)
	 * 
	 */
	public void clearPredictData()
	{
		this.predictDataRows = new ArrayList<PredictDataAccount>();
	}

	/**
	 * After running the prediction algorithm this method updates this account's values to 
	 * the value's it would have on the day represented by day index. 
	 * 
	 * @param dayIndex - the day, as an offset from the start date used in the prediction algorithm, to update
	 * 						this accounts values to.
	 */
	public void update(int dayIndex)
	{
		PredictDataAccount pda = this.predictDataRows.get(dayIndex);
		double uValue = pda.value();
		this.value = uValue;
	}
	
	/**
	 * Updates this account next dates only. Since a vanilla account has no next
	 * dates this method does nothing.
	 * @param dayIndex - the day index to update to
	 */
	public void updateNextDatesOnly(int dayIndex)
	{

	}
}
