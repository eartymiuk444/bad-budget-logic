package com.erikartymiuk.badbudgetlogic.main;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.erikartymiuk.badbudgetlogic.predictdataclasses.*;

/**
 * Class representing various types of money owed (credit cards, loans, other)
 * Credit card and loan objects should be part of the corresponding subclasses.
 * All other moneyOwed objects (classified as other or misc. to the user) should
 * be using this class directly
 */
public class MoneyOwed {
	
	private double interestRate;
		
	private String name; //A name or description of this debt, cannot be null or empty
	private double debtAmount; //How much is owed, must be greater than or equal to zero
	
	private Payment payment; //Payment being applied to this debt. Null if no payment being applied
	private ArrayList<PredictDataMoneyOwed> predictDataRows;	//For use with the prediction algorithm, tracks the next payment on all dates
																//also tracks the debt remaining on all dates along with the history list of transactions
																//Each row should be a single date, in order
	
	private boolean quicklook;
	
	/**
	 * Money Owed constructor. Sets up a debt with no payments
	 * @param name - name or description of the debt, cannot be null or empty
	 * @param debtAmount - debt amount, cannot be negative
	 * @param quicklook - indicates if this debt should be considered for quicklook
	 * @param interestRate - the interest rate of this debt. default is to compound daily the balance of the debt at this rate/365.25.
	 * 							0 indicates no interest rate should be considered and a negative value is invalid.
	 * @throws BadBudgetInvalidValueException
	 */
	public MoneyOwed(String name, double debtAmount, boolean quicklook, double interestRate) throws BadBudgetInvalidValueException
	{
		int error = verifyValues(name, debtAmount, interestRate);
		if (error == 0)
		{
			this.name = name;
			this.debtAmount = debtAmount;
			this.quicklook = quicklook;
			this.predictDataRows = new ArrayList<PredictDataMoneyOwed>();
			this.payment = null;
			this.interestRate = interestRate;
		}
		else
		{
			String message = "";
			switch (error)
			{
				case 1:
				{
					message = BadBudgetInvalidValueException.MONEY_OWED_NAME_INVALID;
					break;
				}
				case 2:
				{
					message = BadBudgetInvalidValueException.MONEY_OWED_AMOUNT_INVALID;
					break;
				}
				case 3:
				{
					message = BadBudgetInvalidValueException.MONEY_OWED_NEGATIVE_INTEREST_RATE;
					break;
				}
				default:
				{
					message = BadBudgetInvalidValueException.UNKNOWN_UNDEFINED;
					break;
				}
			}
			throw new BadBudgetInvalidValueException(message);
		}
	}
	
	/**
	 * Verifies the values for a potential debt (money owed) and returns an error code as an
	 * integer if an error is found. Returns 0 if no error is found. Possible error codes:
	 * 
	 * 		0 - Values are good
	 * 		1 - Name is null or is empty
	 * 		2 - Debt Amount is negative
	 * 		3 - Interest Rate is negative
	 * 
	 * @param name - the name to check cannot be null or empty
	 * @param debtAmount - the debt amount, must be greater or equal to 0
	 * @param interestRate - the interest rate, must be greater or equal to 0
	 * @return - an int indicating the error found or 0 if no error found
	 */
	public static int verifyValues(String name, double debtAmount, double interestRate)
	{
		//Name invalid
		if (name == null || name.equals(""))
		{
			return 1;
		}
		//Debt cannot be negative
		if (debtAmount < 0)
		{
			return 2;
		}
		//Interest Rate cannot be negative
		if (interestRate < 0)
		{
			return 3;
		}
		return 0;
	}
	
	/**
	 * Adds the given row to this debt objects predict data list (at the end)
	 * 
	 * @param row - the predictData to add to the end of this objects list
	 */
	public void addPredictData(PredictDataMoneyOwed row)
	{
		this.predictDataRows.add(row);
	}
	
	/**
	 * Add the passed row at index to this money owed object predict data list.
	 * 
	 * @param index - the location to add the given row
	 * @param row - the row to add
	 */
	public void setPredictData(int index, PredictDataMoneyOwed row)
	{
		if (index < this.predictDataRows.size())
		{
			this.predictDataRows.set(index, row);
		}
		else
		{
			this.predictDataRows.add(row);
		}
	}
	
	/**
	 * Returns the PredictData at dayIndex for this debt
	 * 
	 * @param dayIndex - the date as an index off the starting date in the prediction algorithm
	 * @return the predict data row at the given index (corresponds to the data on a specific date for this debt account)
	 */
	public PredictDataMoneyOwed getPredictData(int dayIndex)
	{
		return this.predictDataRows.get(dayIndex);
	}
	
	/**
	 * Schedule a payment. Replaces any old payment that
	 * was set previously. Ensures that the payment being setup,
	 * if not null, is attached to this debt
	 * @param p - the payment to schedule or null to remove any existing payment
	 * @throws BadBudgetInvalidValueException - thrown if the payment wasn't meant for this debt
	 */
	public void setupPayment(Payment p) throws BadBudgetInvalidValueException
	{
		if (p == null)
		{
			removePayment();
		}
		else
		{
			if (p.getDebt() != this)
			{
				String errorMessage = BadBudgetInvalidValueException.MONEY_OWED_PAYMENT_INVALID;
				throw new BadBudgetInvalidValueException(errorMessage);
			}
			else
			{
				this.payment = p;
			}
		}
	}
	
	/**
	 * Remove any payment that may be set for this money owed account. If
	 * no payment is set then this method does nothing.
	 */
	public void removePayment()
	{
		this.payment = null;
	}
	
	/* Getters and setters */
	public String name()
	{
		return this.name;
	}
	
	public double amount()
	{
		return this.debtAmount;
	}
	
	
	public void changeName(String newName)
	{
		this.name = newName;
	}
	
	public void changeAmount(double newAmount)
	{
		this.debtAmount = newAmount;
	}
	
	public Payment payment()
	{
		return this.payment;
	}
	
	/**
	 * Updates the quicklook value of these money owed object (a simple setter)
	 * @param quicklook - the new quicklook value
	 */
	public void setQuicklook(boolean quicklook)
	{
		this.quicklook = quicklook;
	}
	
	/**
	 * Empties (or Resets) this moneyoweds predict data list.
	 */
	public void clearPredictData()
	{
		this.predictDataRows = new ArrayList<PredictDataMoneyOwed>();
	}
	
	/**
	 * Getter for quicklook field
	 * 
	 * @return true if this debt should be considered for quicklook
	 * 
	 */
	public boolean quicklook()
	{
		return this.quicklook;
	}
	
	public double interestRate()
	{
		return this.interestRate;
	}
	
	public void setInterestRate(double interestRate)
	{
		this.interestRate = interestRate;
	}
	
	/**
	 * After running the prediction algorithm this method updates this debt's values to 
	 * the value's it would have on the day represented by day index. If a goal is set
	 * it is assumed to still be valid (override this method if that might not be true i.e creditCards).
	 * 
	 * @param predictEndDate - the date our prediction algorithm ends. Any interest calculations treat this + 1 day as the start date.
	 * @param dayIndex - the day, as an offset from the start date used in the prediction algorithm, to update
	 * 						this debts values to.
	 */
	public void update(Date predictEndDate, int dayIndex)
	{
		PredictDataMoneyOwed pdmo = this.predictDataRows.get(dayIndex);
		double uAmount = pdmo.value();
		this.debtAmount = uAmount;
		
		if (payment != null)
		{
			Date uNextPaymentDate = pdmo.getNextPaymentDate();
			payment.setNextPaymentDate(uNextPaymentDate);
			
			//Removed 3/16/2017 - code was to check if goal was still valid after the amount was updated. Now it
			//is assumed that it is. (Overridden in creditCard class).
			/*
			if (payment.goalDate() != null)
			{
				//Check if our goal is still valid
				Date goalDateAfterUpdate = Prediction.findGoalDate(payment.nextPaymentDate(), payment.amount(), payment.frequency(), uAmount, null);
				if (!Prediction.datesEqualUpToDay(payment.goalDate(), goalDateAfterUpdate))
				{
					//Goal is no longer valid, some other bbd object changed the debt amount. So clear the goal date
					payment.setGoalDate(null);
				}
			}
			*/
		}
		
	}
	
	/**
	 * Updates this debts next dates only. This applies only if the debt has a payment
	 * associated with it. If the payment has a goal date it is cleared
	 * @param dayIndex - the day to update this debt to
	 */
	public void updateNextDatesOnly(int dayIndex)
	{
		PredictDataMoneyOwed pdmo = this.predictDataRows.get(dayIndex);
		double uAmount = this.debtAmount;
		
		if (payment != null)
		{
			Date uNextPaymentDate = pdmo.getNextPaymentDate();
			payment.setNextPaymentDate(uNextPaymentDate);
			
			//Goal is likely no longer valid since we are not updating the amounts
			payment.setGoalDate(null);
			
			/* Changed 3/16/2017 - now assume that goal is invalid and always clear it
			if (payment.goalDate() != null)
			{
				//Goal is likely no longer valid since we are not updating the amounts
				Date goalDateAfterUpdate = Prediction.findGoalDate(payment.nextPaymentDate(), payment.amount(), payment.frequency(), uAmount, null);
				if (!Prediction.datesEqualUpToDay(payment.goalDate(), goalDateAfterUpdate))
				{
					//Goal is no longer valid
					payment.setGoalDate(null);
				}
			}
			*/
		}
	}

}
