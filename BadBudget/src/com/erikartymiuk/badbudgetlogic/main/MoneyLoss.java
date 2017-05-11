package com.erikartymiuk.badbudgetlogic.main;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataMoneyGain;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataMoneyLoss;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataMoneyOwed;

/**
 * Class representing a form of money loss. Money losses can be paid using cash or
 * a credit card. A budget item is a type of loss but if it well be tracked it should
 * be a member of the budgetItem class. This class is similar to a money gain object
 */
public class MoneyLoss 
{
	private String expense; //Description of the loss
	private double amount;	//How much the loss is
	private Frequency frequency; //How often the loss occurs
	private Date endDate;	//When to stop processing this loss
	private Date nextLoss;	//The date the next loss will occur
	
	//The source of payment for this loss. Can be either an account
	//or a moneyOwed-creditCard object.
	private Source source;
	
	private ArrayList<PredictDataMoneyLoss> predictData;	//For use with the prediction algorithm. A list of the next losses for
								//all dates between start and target
	
	/** 
	 * Constructor for a MoneyLoss object.
	 * 
	 * @param expense - Description of the money loss ("Cable, Electric, Rent, Phone"), cannot be empty or null
	 * @param amount - the amount of the loss, cannot be negative
	 * @param frequency - how often to process this loss, if one time then end date must match next loss (unless next loss null)
	 * @param nextLoss - The next date to process this loss (can only be null if freq one time indicating one time loss handled)
	 * @param endDate - the last possible date this loss will be processed, if one time freq then must match next loss (unless next loss null)
	 * @param source - the source to use as payment for this loss. Can be a cash account or a credit card, cannot be null
	 * @throws BadBudgetInvalidValueException - if the data contains errors
	 */
	public MoneyLoss(String expense, double amount, Frequency frequency, Date nextLoss, Date endDate, Source source) throws BadBudgetInvalidValueException
	{
		int errorCode = verifyValues(expense, amount, frequency, nextLoss, endDate, source);
		if (errorCode == 0)
		{
			this.expense = expense;
			this.amount = amount;
			this.frequency = frequency;
			this.nextLoss = nextLoss;
			this.endDate = endDate;
			this.source = source;
			
			this.predictData = new ArrayList<PredictDataMoneyLoss>();
		}
		else
		{
			String errorMessage = "";
			switch (errorCode)
			{
				case 1:
				{
					errorMessage = BadBudgetInvalidValueException.LOSS_EXPENSE_NOT_SET_E1;
					break;
				}
				case 2:
				{
					errorMessage = BadBudgetInvalidValueException.LOSS_AMOUNT_NEGATIVE_E2;
					break;
				}
				case 5:
				{
					errorMessage = BadBudgetInvalidValueException.LOSS_ONE_TIME_END_DATE_NULL_E5;
					break;
				}
				case 7:
				{
					errorMessage = BadBudgetInvalidValueException.LOSS_NEXT_LOSS_NULL_NOT_ONE_TIME_E7;
					break;
				}
				case 8:
				{
					errorMessage = BadBudgetInvalidValueException.LOSS_NEXT_LOSS_END_DATE_MISMATCH_E8;
				}
				case 6:
				{
					errorMessage = BadBudgetInvalidValueException.LOSS_SOURCE_NOT_SET_E6;
					break;
				}
				default:
				{
					errorMessage = BadBudgetInvalidValueException.UNKNOWN_UNDEFINED;
					break;
				}
			}
			throw new BadBudgetInvalidValueException(errorMessage);
		}
	}
	
	/**
	 * Error checking for values that will be used to create a money loss object. Returns an error code that corresponds to an error
	 * found or 0 if no errors are found. The following are the codes and a description of the error corresponding to it:
	 * 
	 * 		0 - Values are valid
	 * 		1 - The expense string is not set or is empty
	 * 		2 - The amount is negative
	 * 		5 - Frequency is one time but end date is null
	 * 		7 - Next loss is null but frequency is not one time (next loss null indicates one time payment already processed)
	 * 		8 - Next loss not null and frequency one time but next loss doesn't match end date
	 * 		6 - Source isn't set
	 * 
	 * @param expense - Description of the money loss ("Cable, Electric, Rent, Phone"), cannot be empty or null
	 * @param amount - the amount of the loss, cannot be negative
	 * @param frequency - how often to process this loss, if one time then end date must match next loss (unless next loss null) 
	 * @param nextLoss - The date to next process this loss
	 * @param endDate - the last possible date this loss will be processed, if one time freq then should match next loss date (unless nl null)
	 * @param source - the source to use as payment for this loss. Can be a cash account or a credit card, cannot be null
	 * @return an error code corresponding to an error found or 0 if no errors were found
	 */
	public static int verifyValues(String expense, double amount, Frequency frequency, Date nextLoss, Date endDate, Source source)
	{
		if (expense == null || expense.equals(""))
		{
			return 1;
		}
		else if (amount < 0)
		{
			return 2;
		}
		else if (frequency.equals(Frequency.oneTime) && endDate == null)
		{
			return 5;
		}
		else if (nextLoss == null && !frequency.equals(Frequency.oneTime))
		{
			return 7;
		}
		else if (nextLoss != null && frequency.equals(Frequency.oneTime) && !Prediction.datesEqualUpToDay(endDate, nextLoss))
		{
			return 8;
		}
		else if (source == null)
		{
			return 6;
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * Indicates if the nextLoss occurs on the given day.
	 * 
	 * @return - true if the loss occurs on the same day (and year) as the given date, false otherwise
	 */
	public boolean lossToday(Date date)
	{
		if (this.nextLoss == null)
		{
			return false;
		}
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date);
		cal2.setTime(this.nextLoss);
		if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Processes the next loss by removing amount from the source account. Calling method should
	 * first ensure that the loss is occurring on the day intended using the 'lossToday' method.
	 */
	public void processLoss()
	{
		//this.source.predictWithdrawOnDay(this.amount, this.nextLoss, this.expense);
		this.nextLoss = this.calculateNextLoss(this.nextLoss);
	}
	
	/** Using the given loss date and the frequency of this object returns what the next loss
	 * date would be.
	 * 
	 * @param currLoss - the date the last loss occurred to calculate off of for the next loss
	 * 
	 * @return - the date of the next loss after the current next loss date
	 */
	public Date calculateNextLoss(Date currLoss)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(currLoss);
		
		//Switch on all possible frequencies
		switch (this.frequency) 
		{
		case oneTime:
			return null;
		case daily:
			cal.add(Calendar.DAY_OF_MONTH, 1);
			return cal.getTime();
		case weekly:
			cal.add(Calendar.WEEK_OF_YEAR, 1);
			return cal.getTime();
		case biWeekly:
			cal.add(Calendar.WEEK_OF_YEAR, 2);
			return cal.getTime();
		case monthly:
			cal.add(Calendar.MONTH, 1);
			return cal.getTime();
		case yearly:
			cal.add(Calendar.YEAR, 1);
			return cal.getTime();
		default:
			return null;
		}
	}
	
	/**
	 * Adds the given row to this loss objects predict data list (at the end)
	 * 
	 * @param row - the predictData to add to the end of this objects list
	 */
	public void addPredictData(PredictDataMoneyLoss row)
	{
		this.predictData.add(row);
	}
	
	/**
	 * Add the row at index to the predict data list of this money loss object
	 * 
	 * @param index - the location to add the row
	 * @param row - the row to add to the predict data list
	 */
	public void setPredictData(int index, PredictDataMoneyLoss row)
	{
		if (index < this.predictData.size())
		{
			this.predictData.set(index, row);
		}
		else
		{
			this.predictData.add(row);
		}
	}
	
	/**
	 * Returns the PredictData at dayIndex for this loss
	 * 
	 * @param dayIndex - the date as an index off the starting date in the prediction algorithm
	 * @return the predict data row at the given index (corresponds to the data on a specific date for this loss)
	 */
	public PredictDataMoneyLoss getPredictData(int dayIndex)
	{
		return this.predictData.get(dayIndex);
	}
	
/* Getters and setters */
	
	public String expenseDescription()
	{
		return this.expense;
	}
	
	public double lossAmount()
	{
		return this.amount;
	}
	
	public Frequency lossFrequency()
	{
		return this.frequency;
	}
	
	public Date endDate()
	{
		return this.endDate;
	}
	
	public Source source()
	{
		return this.source;
	}
	
	public Date nextLoss()
	{
		return this.nextLoss;
	}
	
	public void setExpenseDescription(String e)
	{
		this.expense = e;
	}
	
	public void setLossAmount(double amount)
	{
		this.amount = amount;
	}
	
	public void setLossFrequency(Frequency f)
	{
		this.frequency = f;
	}
	
	public void setEndDate(Date end)
	{
		this.endDate = end;
	}
	
	public void setSource(Source s)
	{
		this.source = s;
	}
	
	public void setNextLoss(Date next)
	{
		this.nextLoss = next;
	}
	
	/**
	 * Empties (or Resets) this moneylosses predict data list.
	 */
	public void clearPredictData()
	{
		this.predictData = new ArrayList<PredictDataMoneyLoss>();
	}
	
	/**
	 * After running the prediction algorithm this method updates this loss's next withdrawal date to 
	 * the value it would have on the day represented by day index. 
	 * 
	 * @param dayIndex - the day, as an offset from the start date used in the prediction algorithm, to update
	 * 						this losses values to.
	 */
	public void update(int dayIndex)
	{
		PredictDataMoneyLoss pdml = this.getPredictData(dayIndex);
		this.nextLoss = pdml.nextLoss();
	}

	/**
	 * Updates the next loss date for this loss.
	 * @param dayIndex - the day to update this loss to.
	 */
	public void updateNextDatesOnly(int dayIndex)
	{
		PredictDataMoneyLoss pdml = this.getPredictData(dayIndex);
		this.nextLoss = pdml.nextLoss();
	}
}
