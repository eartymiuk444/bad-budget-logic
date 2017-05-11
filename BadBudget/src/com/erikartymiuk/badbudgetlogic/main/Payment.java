package com.erikartymiuk.badbudgetlogic.main;
import java.util.*;

/**
 *  Class representing various payments for any debt accounts (money owed) 
 */
public class Payment 
{	
	private double amount; //payment amount: will pay up to this amount on the next payment date. must be a positive
	  						//value unless payoff is set, in which case must be set to -1.
	private boolean payOff; //Indicates if every payment should pay entire balance,
							//amount must be -1 if set. Cannot have payoff set and a goal
	private Frequency frequency; //how often this payment should be applied. cannot be null. if one time end date must match the nextPayment (unless np null)
								//ongoing must be false, and no goal can be set.
	private Account sourceAccount; //The account the payment balance will be withdrawn from.
									//Cannot be null
	private Date nextPaymentDate; //when to next make this payment. can only be null for a handled one time frequency
	private boolean ongoing; //indicates if payments should keep occurring. if set end date must be null. cannot have this set and 
 								//a one time frequency or a goal set
	private Date endDate; //will make payments up to, inclusive, this date. if ongoing not set then cannot be null. if goal date is set then must
							//then must match goal date.
	
	private MoneyOwed debt; //The debt this payment is being applied to. Cannot be null.
	private Date goalDate; //Date balance will be zero if debt stays constant. Must be consistent with
							//the payment amount and frequency, and the starting debt amount.
							//Restrictions include: payoff false, frequency not one time, end date = goal date,
							//ongoing false.

/**
 * 
 * @param amount - payment amount: will pay up to this amount on the next payment date. must be a positive
 * 					value unless payoff is set, in which case must be set to -1.
 * @param payoff - indicates if every payment should pay entire balance. if set amount must be -1. cannot have payoff
 * 					set and a goal.
 * @param frequency - how often this payment should be applied. cannot be null. if one time end date must match the nextPayment (unless np null)
 * 						ongoing must be false, and no goal can be set.
 * @param sourceAccount - the account the payment balance will be withdrawn from. cannot be null.
 * @param nextPayment - when to next make this payment. can only be null for a handled one time frequency
 * @param ongoing - indicates if payments should keep occurring. if set end date must be null. cannot have this set and 
 * 						a one time frequency or a goal set
 * @param endDate - will make payments up to, inclusive, this date. if ongoing not set then cannot be null. if goal date is set then must
 * 						then must match goal date.
 * @param debt - the debt this payment is being applied to cannot be null.
 * @param goalDate - date balance will be zero if debts stays constant. must be consistent with the payment amount and frequency, and the
 * 						starting debt amount. restrictions include: payoff false, frequency not one-time, end date equal to goal date,
 * 						and ongoing false.
 * @throws BadBudgetInvalidValueException
 */
	public Payment(double amount, boolean payoff, Frequency frequency, Account sourceAccount, 
			Date nextPayment, boolean ongoing, Date endDate, MoneyOwed debt, Date goalDate) throws BadBudgetInvalidValueException
	{
		int errorCode = verifyValues(amount, payoff, frequency, sourceAccount, nextPayment, ongoing, endDate, debt, goalDate);
		if (errorCode == 0)
		{
			this.amount = amount;
			this.payOff = payoff;
			this.frequency = frequency;
			this.sourceAccount = sourceAccount;
			this.nextPaymentDate = nextPayment;
			this.ongoing = ongoing;
			this.endDate = endDate;
			this.debt = debt;
			this.goalDate = goalDate;
		}
		else
		{
			String errorMessage = "";
			switch (errorCode)
			{
				case 1:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E1;
					break;
				}
				case 2:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E2;
					break;
				}
				case 3:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E3;
					break;
				}
				case 4:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E4;
					break;
				}
				case 5:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E5;
					break;
				}
				case 6:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E6;
					break;
				}
				case 7:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E7;
					break;
				}
				case 8:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E8;
					break;
				}
				case 9:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E9;
					break;
				}
				case 10:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E10;
					break;
				}
				case 11:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E11;
					break;
				}
				case 12:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E12;
					break;
				}
				case 13:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E13;
					break;
				}
				case 14:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E14;
					break;
				}
				case 15:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E15;
					break;
				}
				case 16:
				{
					errorMessage = BadBudgetInvalidValueException.PAYMENT_E16;
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
	 * Verifies that the passed values form a valid payment. Checks for the following errors and if
	 * found returns the corresponding integer error code.
	 * 			
	 * 			0 - Values are all valid
	 * 			1 - Payoff true but payment amount not -1
	 * 			2 - Payment amount <= 0
	 * 			3 - Payoff true and goalDate not null
	 * 			4 - Frequency is not set
	 * 			5 - Next payment is null and frequency is not one time
	 * 			6 - Goal date not null and next payment after goal date and debt amount not 0
	 * 			7 - Frequency one time and nextPayment not null and nextPayment not equal to endDate
	 * 			8 - Frequency one time and goal date not null
	 * 			9 - Ongoing true and frequency one time
	 * 			10 - Source account is not set
	 * 			11 - Ongoing is true but end date set
	 * 			12 - Ongoing is true but goal date set
	 * 			13 - Ongoing is false but end date is not set
	 * 			14 - Debt is not set
	 * 			15 - Ongoing false and goal date not null and goal date not equal to end date
	 * 			16 - Goal date not null and next payment before or on goal date, no interest rate to consider, and debt, frequency, goal date, next payment and payment amount are inconsistent
	 * 
	 * @param paymentAmount - payment amount: will pay up to this amount on the next payment date. must be a positive
 * 					value unless payoff is set, in which case must be set to -1.
	 * @param payoff - indicates if every payment should pay entire balance. if set amount must be -1. cannot have payoff
 * 					set and a goal.
	 * @param frequency - how often this payment should be applied. cannot be null. if one time end date must match the nextPayment (unless np null)
 * 						ongoing must be false, and no goal can be set.
	 * @param sourceAccount - the account the payment balance will be withdrawn from. cannot be null.
	 * @param nextPayment - when to next make this payment. can only be null for a handled one time frequency
	 * @param ongoing - indicates if payments should keep occurring. if set end date must be null. cannot have this set and 
 * 						a one time frequency or a goal set
	 * @param endDate - will make payments up to, inclusive, this date. if ongoing not set then cannot be null. if goal date is set then must
 * 						then must match goal date.
	 * @param debt - the debt this payment is being applied to cannot be null.
	 * @param goalDate - date balance will be zero if debts stays constant. must be consistent with the payment amount and frequency, and the
 * 						starting debt amount. restrictions include: payoff false, frequency not one-time, end date equal to goal date,
 * 						and ongoing false.
	 * @return
	 */
	public static int verifyValues(double paymentAmount, boolean payoff, Frequency frequency, Account sourceAccount, 
			Date nextPayment, boolean ongoing, Date endDate, MoneyOwed debt, Date goalDate)
	{
		
		//Payoff & ongoing can be expressed as paymentAmount and endDate so make sure consistent
		
		//1 - Payoff true but payment amount not -1
		if (payoff && paymentAmount != -1)
		{
			return 1;
		}
		//2 - Payment amount <= 0
		else if (!payoff && paymentAmount <= 0)
		{
			return 2;
		}
		//13 - Ongoing is false but end date is not set
		else if (!ongoing && endDate == null)
		{
			return 13;
		}
		//11 - Ongoing is true but end date set
		else if (ongoing && endDate != null)
		{
			return 11;
		}
		
		//Check for null values
		
		//4 - Frequency is not set
		else if (frequency == null)
		{
			return 4;
		}
		//10 - Source account is not set
		else if (sourceAccount == null)
		{
			return 10;
		}
		//14 - Debt is not set
		else if (debt == null)
		{
			return 14;
		}
		
		/* Know we have the above and work our way down as follows:
		 * 
		 * 
		  	debt,
		  	frequency,
		  	goalDate,
		  	nextPayment,
		 	paymentAmount<=>payoff,
		 	ongoing<=>endDate, 
		 */
		
		//Possible errors debt
		//Possible errors debt & frequency
		//Possible errors debt, frequency, and goalDate
			//Frequency one time and goal date not null
		
		//Possible errors debt, frequency, goalDate, and nextPayment
			//Next payment is null and frequency is not one time
			//Goal date not null and next payment after goal date and debt amount not 0

		//Possible errors debt, frequency, goalDate, nextPayment, and payoff/paymentAmount
			//Payoff true and goalDate not null
			//Goal date not null and next payment before or on goal date and debt, frequency, goal date, next payment and payment amount are inconsistent
			
		//Possible errors debt, frequency, goalDate, nextPayment, payoff/paymentAmount, and ongoing/endDate
			//Ongoing true and goal date not null
			//Ongoing false and goal date not null and goal date not equal to end date
			//Ongoing true and frequency one time
			//Frequency one time and nextPayment not null and nextPayment not equal to endDate
		
		//8 - Frequency one time and goal date not null
		else if (frequency.equals(Frequency.oneTime) && goalDate != null)
		{
			return 8;
		}
		//5 - Next payment is null and frequency is not one time
		else if (nextPayment == null && frequency != Frequency.oneTime)
		{
			return 5;
		}
		//6 - Goal date not null and next payment after goal date and debt amount not 0
		else if (goalDate != null && (Prediction.numDaysBetween(nextPayment, goalDate) < 0) && debt.amount() != 0)
		{
			return 6;
		}
		//3 - Payoff true and goalDate not null
		else if (payoff && goalDate != null)
		{
			return 3;
		}
		//16 - Goal date not null and next payment before or on goal date, there is no interest rate to consider 
		//and debt, frequency, goal date, next payment and payment amount are inconsistent
		else if (goalDate != null && (Prediction.numDaysBetween(nextPayment, goalDate) >= 0) && debt.interestRate() == 0 &&
				!Prediction.datesEqualUpToDay(goalDate, Prediction.findGoalDate(nextPayment, paymentAmount, frequency, debt.amount(), null)))
		{
			return 16;
		}
		//12 - Ongoing true and goal date not null
		else if (ongoing && goalDate != null)
		{
			return 12;
		}
		//15 - Ongoing false and goal date not null and goal date not equal to end date
		else if (!ongoing && goalDate != null && !Prediction.datesEqualUpToDay(endDate, goalDate))
		{
			return 15;
		}
		//9 - Ongoing true and frequency one time
		else if (ongoing && frequency.equals(Frequency.oneTime))
		{
			return 9;
		}
		//7 - Frequency one time and nextPayment not null and nextPayment not equal to endDate
		else if (frequency.equals(Frequency.oneTime) && nextPayment != null && !Prediction.datesEqualUpToDay(nextPayment, endDate))
		{
			return 7;
		}

		//0 - Values are all valid
		else
		{
			return 0;
		}
	}
	
	/**
	 * Withdraws funds from source of this payment and reduces debt
	 * balance by that amount. Next payment date is updated. Should make
	 * sure next payment takes place on the day intended by checking nextPaymentDate
	 * 
	 * @param debt - the money owed account to apply this payment to
	 */
	public void processPayment(MoneyOwed debt)
	{
		//Check to see if we are paying off entirely
		if (payOff)
		{
			this.sourceAccount.withdraw(debt.amount());
			debt.changeAmount(debt.amount() - debt.amount());
		}
		else
		{
			this.sourceAccount.withdraw(this.amount);
			debt.changeAmount(debt.amount() - this.amount);
		}
		
		//Need to update next payment date
		this.nextPaymentDate = determineNextPayment(this.nextPaymentDate);
		
		//Check if the next date occurs after the end or goal date
		//and if so set nextPayment date to null (no more payment would
		//be made unless user updates something)
		if (this.nextPaymentDate != null)
		{
			if (endDate != null)
			{
				if (this.nextPaymentDate.after(endDate))
				{
					this.nextPaymentDate = null;
				}
			}
			else if (goalDate != null)
			{
				if (this.nextPaymentDate.after(goalDate))
				{
					this.nextPaymentDate = null;
				}
			}
		}		
	}
	
	/**
	 * Using the passed in date as a starting date
	 * this method adds the frequency onto that and returns that date
	 * returns null if the payment was only one time.
	 * 
	 * @param lastPaymentDate - the date of the last payment (where we calculate off of for the next payment)
	 * @return the next date to process a payment or null if the payment frequency is one time
	 */
	public Date determineNextPayment(Date lastPaymentDate)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastPaymentDate);
		
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
	 * Using the passed in date as a starting date
	 * this method adds the frequency onto that and returns that date
	 * returns null if the frequency is one time only
	 * 
	 * @param lastPaymentDate - the date of the last payment (where we calculate off of for the next payment)
	 * @return the next date to process a payment or null if the frequency is one time
	 */
	public static Date determineNextPayment(Date lastPaymentDate, Frequency freq)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastPaymentDate);
		
		//Switch on all possible frequencies
		switch (freq) 
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
	
	public double amount()
	{
		return this.amount;
	}
	
	public boolean payOff()
	{
		return this.payOff;
	}
	
	public Frequency frequency()
	{
		return this.frequency;
	}
	
	public Date nextPaymentDate()
	{
		return this.nextPaymentDate;
	}
	
	public Date endDate()
	{
		return this.endDate;
	}
	
	public Date goalDate()
	{
		return this.goalDate;
	}
	
	public boolean ongoing()
	{
		return this.ongoing;
	}
	
	public Account sourceAccount()
	{
		return this.sourceAccount;
	}
	
	public void updateAmount(double a)
	{
		this.amount = a;
	}
	
	public void payOff(boolean p)
	{
		this.payOff = p;
	}
	
	public void frequency(Frequency f)
	{
		this.frequency = f;
	}
	
	public void setEndDate(Date e)
	{
		this.endDate = e;
	}
	
	public void setGoalDate(Date g)
	{
		this.goalDate = g;
	}
	
	public void setOngoing(boolean o)
	{
		this.ongoing = o;
	}
	
	public void setSourceAccount(Account a)
	{
		this.sourceAccount = a;
	}
	
	public MoneyOwed getDebt()
	{
		return this.debt;
	}
	
	public void setNextPaymentDate(Date nextPaymentDate)
	{
		this.nextPaymentDate = nextPaymentDate;
	}
}
