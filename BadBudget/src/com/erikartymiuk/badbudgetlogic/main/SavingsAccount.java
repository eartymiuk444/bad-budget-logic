package com.erikartymiuk.badbudgetlogic.main;

import java.text.SimpleDateFormat;
import java.util.*;

import com.erikartymiuk.badbudgetlogic.predictdataclasses.*;

/**
 * A subclass of the account class representing a savings account.
 * Saving accounts will have a goal amount, goal date, and
 * contribution or just a contribution. If these don't make sense
 * to specify then the account should be a regular
 * cash account.
 *	
 * Any two for goal amount, goal date, and
 * contribution restricts the third. For example:
 * 
 * If goal amount specified with:
 * 	Goal Date - then contribution fixed
 * 	Contribution - then goal date fixed
 *
 */

public class SavingsAccount extends Account 
{
	private double interestRate;

	private boolean goalSet; //Indicates if this savings account has a goal set. Requires goal amount and date to be
								//set
	
	private double goal; //Goal amount. Needs to be consistent with goal date and contribution.
							//should be -1 if goal is not set
	
	private Date goalDate; //The goal date for the account reaching the goal amount.  
							//It happens (or exceeds value) on this date (null indicates no goal)
							//Needs to be consistent with goal amount and contribution
	
	private Contribution contribution; //Regular contribution to this account.
										//required for all savings accounts
	
	private Account sourceAccount; //The account (can be a savings account)
									//that the contribution will be withdrawn from.
									//cannot be null
		
	private Date endDate; //The date the contribution to this account ends. If goal set this must match goal date.
							//If ongoing true then must be null. If ongoing false cannot be null.
	
	private boolean ongoing; //True if contribution ongoing. If true end date must be null and no goal can be set
								//If false end date must be set

	private Date nextContribution; //The date the next contribution is scheduled to happen. 
			//Value can be null if a one time contribution processed. Can occur after end date but if goal set
			//then goal must have been reached
	
	/** 
	 * Constructor for a SavingsAccount. Has various restrictions in addition to checks defined by Account
	 * (see verifyValues for detailed specification)
	 * 
	 * @param name - User defined descriptor/name for this account
	 * @param value - Current value of account
	 * @param quickLook - Specifies if this account is in the prediction quickLook
	 * @param goalSet - Indicates if this savings account has a goal set. Requires goal amount and date to be
						set
	 * @param goalAmount - Goal amount. Needs to be consistent with goal date and contribution.
						should be -1 if goal is not set. Must be a positive value
	 * @param goalDate - The goal date for the account reaching the goal amount.  
							It happens (or exceeds value) on this date. If goalSet is false must be null
							Needs to be consistent with goal amount and contribution
	 * @param contribution - Regular contribution to this account.
								required for all savings accounts
	 * @param sourceAccount - The account (can be a savings account)
								that the contribution will be withdrawn from.
								cannot be null
	 * @param nextContribution - The date the next contribution is scheduled to happen. 
	 * @param endDate - The date the contribution to this account ends. If goal set this must match goal date.
							If ongoing true then must be null. If ongoing false cannot be null.
	 * @param ongoing - True if contribution ongoing. If true end date must be null and no goal can be set
							If false end date must be set
	   @param interestRate - the expected rate of return for this savings account. 0 indicates no interest accumulation. Negative values are invalid. 
	   							Will be compounded monthly on the 1st of the month
	 * 
	 * @throws BadBudgetInvalidValueException 
	 */
	public SavingsAccount(String name, double value, boolean quickLook, 
			boolean goalSet, double goalAmount, Date goalDate,
			Contribution contribution, Account sourceAccount, Date nextContribution, Date endDate, boolean ongoing, double interestRate)
			throws BadBudgetInvalidValueException 
	{
		super(name, value, quickLook);
		
		int error = verifyValues(value, goalSet, goalAmount, goalDate, contribution, sourceAccount, nextContribution, endDate, ongoing, interestRate);
		if (error == 0)
		{
			this.goalSet = goalSet;
			this.goal = goalAmount;
			this.goalDate = goalDate;
			this.contribution = contribution;
			this.sourceAccount = sourceAccount;
			this.nextContribution = nextContribution;
			this.endDate = endDate;
			this.ongoing = ongoing;
			this.interestRate = interestRate;
			
		}
		else
		{
			String message = "";
			switch (error)
			{
				case 1:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E1;
					break;
				}
				case 2:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E2;
					break;
				}
				case 3:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E3;
					break;
				}
				case 4:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E4;
					break;
				}
				case 5:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E5;
					break;
				}
				case 6:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E6;
					break;
				}
				case 7:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E7;
					break;
				}
				case 8:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E8;
					break;
				}
				case 9:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E9;
					break;
				}
				case 10:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E10;
					break;
				}
				case 11:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E11;
					break;
				}
				case 12:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E12;
					break;
				}
				case 13:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E13;
					break;
				}
				case 14:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E14;
					break;
				}
				case 15:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E15;
					break;
				}
				case 16:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E16;
					break;
				}
				case 17:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E17;
					break;
				}
				case 18:
				{
					message = BadBudgetInvalidValueException.SAVING_ACCOUNT_E18;
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
	 * Verifies values being used for the creation of a savings account. Returns an error code indicating a specific error that
	 * is being violated.
	 * @param currentValue - the starting value of a potential savings account
	 * @param goalSet - should indicate if goal is being set
	 * @param goalAmount - goal amount being set
	 * @param goalDate - goal date being set
	 * @param contribution - contribution (amount and frequency) being set
	 * @param sourceAccount - the source for the contribution
	 * @param nextContribution - the initial contribution date
	 * @param end - the end date
	 * @param ongoing - whether the contribution is ongoing or not
	 * @param interestRate - interestRate is less than 0
	 * @return - an error code indicating any errors found in the values for this savings account
	 * 
	 *				0 - Values are valid
	 * 				1 - Current value is less than 0
	 * 				2 - Goal is set but goal amount <= 0
	 * 				3 - Goal is not set but goal amount is not -1
	 * 				4 - Goal is set but goal date is null
	 * 				5 - Goal is not set but goal date isn't null
	 * 				6 - Contribution is null
	 * 				7 - Contribution is one time but goal is set
	 * 				8 - Source account is null
	 * 				9 - Contribution is not one time but next contribution is null
	 * 				17 - Next contribution comes after end/goal date but current amount not enough to meet goal
	 *				10 - Inconsistency found between starting value, goal amount, goal date, and contribution (3/15/2017 - removed this check)
	 *				11 - Goal set but end date is null
	 *				12 - Goal set but end date is not equal to goal date
	 *				13 - frequency is one time but end date is null
	 *				14 - frequency is one time and next contribution is not null but end date doesn't equal next contribution
	 *	 			15 - Ongoing is true but end date is set
	 * 				16 - Ongoing is false but end date is not set
	 * 				18 - Interest rate is less than 0
	 * 
	 */
	public static int verifyValues(double currentValue, boolean goalSet, double goalAmount, Date goalDate, 
			Contribution contribution, Account sourceAccount, Date nextContribution, Date end, boolean ongoing, double interestRate)
	{		
		if (currentValue < 0)
		{
			return 1;
		}
		else if (goalSet && goalAmount <= 0)
		{
			return 2;
		}
		else if (!goalSet && goalAmount != -1)
		{
			return 3;
		}
		else if (goalSet && goalDate == null)
		{
			return 4;
		}
		else if (!goalSet && goalDate != null)
		{
			return 5;
		}
		else if (contribution == null)
		{
			return 6;
		}
		else if (contribution.getFrequency() == Frequency.oneTime && goalSet)
		{
			return 7;
		}
		else if (sourceAccount == null)
		{
			return 8;
		}
		else if (nextContribution == null && contribution.getFrequency() != Frequency.oneTime)
		{
			return 9;
		}
		else if (goalSet && Prediction.numDaysBetween(nextContribution, goalDate) < 0 && currentValue < goalAmount)
		{
			return 17;
		}
		//TODO - removed check for consistency 3/15/2017, might want to add checks back as it could be done. (error 10)
		else if (goalSet && end == null)
		{
			return 11;
		}
		else if (goalSet && !Prediction.datesEqualUpToDay(end, goalDate))
		{
			return 12;
		}
		else if (contribution.getFrequency() == Frequency.oneTime && end == null)
		{
			return 13;
		}
		else if (contribution.getFrequency() == Frequency.oneTime && 
				nextContribution != null && 
				!Prediction.datesEqualUpToDay(end, nextContribution))
		{
			return 14;
		}
		else if (ongoing && end != null)
		{
			return 15;
		}
		else if (!ongoing && end == null)
		{
			return 16;
		}
		else if (interestRate < 0)
		{
			return 18;
		}
		else
		{
			return 0;
		}
	}
	
	/** Returns the indicated index in the list of predict data rows.
	 * 
	 * @param i - the index to retrieve from the list (corresponds to a date)
	 * 
	 * @return - Returns the row from the list the prediction data on the day corresponding to the index
	 */
	public PredictDataSavingsAccount getPredictData(int i)
	{
		return (PredictDataSavingsAccount) super.getPredictData(i);
	}
	
	/** Sets a row in this savings account predictData list. It is up to the prediction algorithm
	 * to correctly maintain this list during the course of its run.
	 * 
	 * @param index - the index of the list element to set, should correspond to a date
	 * @param pdsa - the predictDataSavingsAccount to set this element to
	 * 
	 */
	public void setPredictData(int index, PredictDataSavingsAccount pdsa)
	{
		super.setPredictData(index, pdsa);
	}
	
	/**
	 * Adds the specified predict data to the end of this savings account predict data list
	 * 
	 * @param pdsa - the predict data to add
	 */
	public void addPredictData(PredictDataSavingsAccount pdsa)
	{
		super.addPredictData(pdsa);
	}
	
	/**
	 * Take money from source account and transfer to savings. Will do the transfer even if
	 * source account goes negative. Behavior is undefined if there is no nextContribution.
	 */
	public void handleNextContribution()
	{
		//Get contribution amount and frequency
		double amount = this.contribution.getContribution();
		Frequency f = this.contribution.getFrequency();
		
		//Withdraw this amount from the source account.
		this.sourceAccount.withdraw(amount);
		//Deposit this amount into this account
		this.deposit(amount);
		
		//Check to see if we have a goal amount and if so was it met
		if (this.goal != -1)
		{
			//Check to see if goal was met
			if (this.value() >= this.goal)
			{
				//Set next contribution to null
				this.nextContribution = null;
			}
			else
			{
				//If we haven't met the goal then calculate the next contribution
				this.nextContribution = this.contribution.nextContributionDate(this.nextContribution);
			}
		}
		else
		{
			this.nextContribution = this.contribution.nextContributionDate(this.nextContribution);
		}
	}
	
	/**
	 * Indicates if this savings account has contributions still coming in
	 * 
	 * @return true if there are more contributions to be processed false otherwise
	 */
	public boolean hasNextContribution()
	{
		if (this.nextContribution != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Changing either the goal amount, date, or contribution could result in changes to the
	 * others. Thus we only update one if we are also given the value for the other two.
	 * (Although there is an option for just contribution if no goals exist)
	 * Verifying that they sync up must be handled elsewhere.
	 * 
	 * @param goalAmount - the amount to reach in this account
	 * @param goalDate - the new date this account will have the goal amount by
	 * @param contribution - the new contribution amount
	 */
	public void updateGoalAndContribution(double goalAmount, Date goalDate, Contribution contribution)
	{
		this.goal = goalAmount;
		this.goalDate = goalDate;
		this.contribution = contribution;
	}
	
	/* Getters and setters */
	public double goal()
	{
		return this.goal;
	}
	
	public Contribution contribution()
	{
		return this.contribution;
	}
	
	public Date endDate()
	{
		return this.endDate;
	}
	
	public Date goalDate()
	{
		return this.goalDate;
	}
	
	public Date nextContribution()
	{
		return this.nextContribution;
	}
	
	public Account sourceAccount()
	{
		return this.sourceAccount;
	}
	
	public double getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(double interestRate) {
		this.interestRate = interestRate;
	}
	
	/** 
	 * Only changed by user input 
	 */
	public void changeEndDate(Date endDate)
	{
		this.endDate = endDate;
	}
	
	/**
	 * Setter for the next contribution date. No error checking done.
	 * @param next - the next contribution date
	 */
	public void changeNextContribution(Date next)
	{
		this.nextContribution = next;
	}
	
	/**
	 * Should only be called if this account doesn't have any goals, just a contribution
	 */
	public void updateContribution(Contribution c)
	{
		this.contribution = c;
	}
	
	/**
	 * Update (or set) this savings account source. The source for a savings account
	 * can be a regular or another savings account.
	 * 
	 * @param account - the new source account for this savings account
	 */
	public void updateSourceAccount(Account account)
	{
		this.sourceAccount = account;
	}
	
	/**
	 * Overridden to string method for a savings account. Dumps this savings account values in
	 * a human readable format. Example: 
	 * 
	 * 	Vanilla Account Values:
	 * 		Name: Savings Account, Value: 0.0, Quicklook: false
	 * 
	 * 	Savings Specific Values:
	 * 		Goal Values:
	 *			Goal Set: true, Goal Amount: 500.0, Goal Date: May-15-2019
	 *	
	 *	Contribution Values:
	 *		Frequency: daily, Amount: 1.0
	 *
	 *	Source: Account
	 *	Start Date: Jan-01-2018
	 *	End Date: May-15-2019
	 *
	 * @return - a string representing this savings account
	 */
	public String toString()
	{
		String savingsAccountString = 	"Vanilla Account Values:\n\t" + 
										"Name: " + this.name() + ", Value: " + this.value() + ", Quicklook: " + this.quickLook() + "\n\n" +
										"Savings Specific Values:\n\t" +
										"Goal Values:\n\t\t" +
										"Goal Set: " + this.goalSet + ", Goal Amount: " + this.goal + ", Goal Date: " + dateToString(this.goalDate) + "\n\n" +
										"Contribution Values:\n\t" + 
										"Frequency: " + this.contribution.getFrequency() + ", Amount: " + this.contribution.getContribution() + "\n" +
										"Source: " + this.sourceAccount.name() + "\n" +
										"Next Contribution Date: " + dateToString(this.nextContribution) + "\n" +
										"End Date: " + dateToString(this.endDate);
		return savingsAccountString;
	}
	
	/**
	 * Private helper to make date strings formatted as MMM-dd-yyyy if non-null
	 * and to otherwise return 'null' if it is null.
	 * @param date - date to convert
	 * @return - date in string format as MMM-dd-yyyy or 'null' if passed null.
	 */
	private String dateToString(Date date)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("MMM-dd-yyyy");
		if (date != null)
		{
			return formatter.format(date);
		}
		else
		{
			return "null";
		}
	}
	
	/**
	 * After running the prediction algorithm this method updates this savings account's values to 
	 * the value's it would have on the day represented by day index. 
	 * 
	 * @param dayIndex - the day, as an offset from the start date used in the prediction algorithm, to update
	 * 						this saving accounts values to.
	 */
	public void update(int dayIndex)
	{
		super.update(dayIndex);
		PredictDataSavingsAccount pdsa = this.getPredictData(dayIndex);
		Date uNextContributionDate = pdsa.getNextContributionDate();
		this.nextContribution = uNextContributionDate;
	}
	
	/**
	 * Updates this savings accounts next dates only. If a goal was set it is assumed to no longer be
	 * valid and is cleared.
	 * @param dayIndex - the date to update this savings account to
	 */
	public void updateNextDatesOnly(int dayIndex)
	{
		super.updateNextDatesOnly(dayIndex);
		PredictDataSavingsAccount pdsa = this.getPredictData(dayIndex);
		Date uNextContributionDate = pdsa.getNextContributionDate();
		this.nextContribution = uNextContributionDate;
		
		//Clear the goal
		this.goalSet = false;
		this.goal = -1;
		this.goalDate = null;
		
		/* Changed 3/16/2017 - assume now that a set goal is no longer valid and clear it rather than check.
		if (this.goalSet)
		{
			//Goal may no longer valid since we are not updating the amounts
			Date goalDateAfterUpdate = Prediction.findGoalDate(this.nextContribution, this.contribution, this.value(), this.goal());
			if (!Prediction.datesEqualUpToDay(this.goalDate(), goalDateAfterUpdate))
			{
				//Goal is no longer valid
				this.goalSet = false;
				this.goal = -1;
				this.goalDate = null;
			}
		}
		*/	
	}
}
