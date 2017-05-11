package com.erikartymiuk.badbudgetlogic.main;
import java.util.Calendar;
import java.util.Date;

/**
 * Class representing a contribution, defined by an amount of money given
 * and how often it is given (frequency).
 * 
 */
public class Contribution 
{	
	private double contribution;	//Amount of the contribution
	private Frequency frequency;	//How often the contribution is processed
	
	/**
	 * Contribution constructor. Verifies values
	 * 
	 * @param contribution - amount of the contribution, must be greater (strict) than 0
	 * @param frequency - how often this contribution happens, cannot be null
	 * @throws BadBudgetInvalidValueException 
	 * 
	 */
	public Contribution(double contribution, Frequency frequency) throws BadBudgetInvalidValueException
	{
		int error = verifyValues(contribution, frequency);
		if (error == 0)
		{
			this.contribution = contribution;
			this.frequency = frequency;
		}
		else
		{
			String message = "";
			if (error == 1)
			{
				message = BadBudgetInvalidValueException.CONTRIBUTION_AMOUNT_NON_POSITIVE;
			}
			else if (error == 2)
			{
				message = BadBudgetInvalidValueException.CONTRIBUTION_FREQUENCY_NULL;
			}
			else
			{
				message = BadBudgetInvalidValueException.UNKNOWN_UNDEFINED;
			}
			throw new BadBudgetInvalidValueException(message);
		}
	}
	
	/**
	 * Verifies a contributions values
	 * @param contrAmount - the amount of the contr
	 * @param freq - the frequency of the contribution
	 * @return an error code indicating various errors that occur:
	 * 			0 - Values are valid
	 * 			1 - Contribution amount is not positive
	 * 			2 - Frequency is null
	 */
	public static int verifyValues(double contrAmount, Frequency freq)
	{
		if (contrAmount <= 0)
		{
			return 1;
		}
		else if (freq == null)
		{
			return 2;
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * Given a starting date for the last contribution this method returns what the next contribution date
	 * would be.
	 * 
	 * @param startDate - the date the last contribution occurred
	 * @return the next contribution date using this contribution's frequency
	 * 			can return null if a oneTime contribution
	 */
	public Date nextContributionDate(Date startDate)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		
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
	
	/*
	 * Getters and setters
	 */
	public double getContribution()
	{
		return this.contribution;
	}
	
	public Frequency getFrequency()
	{
		return this.frequency;
	}
	
	/* These setters should only be called in conjunction with a check
	 * for updating other necessary Savings variables (goalDate, goalAmount) 
	 */
	public void setContributionAmount(double amount)
	{
		this.contribution = amount;
	}
	
	public void setFrequency(Frequency f)
	{
		this.frequency = f;
	}
}
