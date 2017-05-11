package com.erikartymiuk.badbudgetlogic.main;

/**
 * Exception indicating an error in the creation of a bad budget object
 * @author Erik Artymiuk
 *
 */
public class BadBudgetInvalidValueException extends Exception {
	
	public static final String UNKNOWN_UNDEFINED = "UNKNOWN UNDEFINED ERROR";
	
	/* Account error messages */
	public static final String ACCOUNT_NAME_NULL = "ACCOUNT NAME NULL";
	public static final String ACCOUNT_NAME_EMPTY = "ACCOUNT NAME EMPTY";
	
	/* Contribution error messages */
	public static final String CONTRIBUTION_AMOUNT_NON_POSITIVE = "CONTRIBUTION AMOUNT NON-POSITIVE";
	public static final String CONTRIBUTION_FREQUENCY_NULL = "CONTRIBUTION FREQUENCY NULL";
	 
	 /* Savings account error messages 
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
	 *				10 - Inconsistency found between starting value, goal amount, goal date, and contribution
	 *				11 - Goal set but end date is null
	 *				12 - Goal set but end date is not equal to goal date
	 *				13 - frequency is one time but end date is null
	 *				14 - frequency is one time and next contribution is not null but end date doesn't equal next contribution
	 *	 			15 - Ongoing is true but end date is set
	 * 				16 - Ongoing is false but end date is not set
	 * 				18 - interest rate is less than 0
	 * 
	  * 
	  * 
	  * */
	 public static final String SAVING_ACCOUNT_E1 = "Current value is less than 0";
	 public static final String SAVING_ACCOUNT_E2 = "Goal is set but goal amount <= 0";
	 public static final String SAVING_ACCOUNT_E3 = "Goal is not set but goal amount is not -1";
	 public static final String SAVING_ACCOUNT_E4 = "Goal is set but goal date is null";
	 public static final String SAVING_ACCOUNT_E5 = "Goal is not set but goal date isn't null";
	 public static final String SAVING_ACCOUNT_E6 = "Contribution is null";
	 public static final String SAVING_ACCOUNT_E7 = "Contribution is one time but goal is set";
	 public static final String SAVING_ACCOUNT_E8 = "Source account is null";
	 public static final String SAVING_ACCOUNT_E9 = "Contribution is not one time but next contribution is null";
	 public static final String SAVING_ACCOUNT_E10 = "Inconsistency found between starting value, goal amount, goal date, and contribution";
	 public static final String SAVING_ACCOUNT_E11 = "Goal set but end date is null";
	 public static final String SAVING_ACCOUNT_E12 = "Goal set but end date is not equal to goal date";
	 public static final String SAVING_ACCOUNT_E13 = "frequency is one time but end date is null";
	 public static final String SAVING_ACCOUNT_E14 = "frequency is one time and next contribution is not null but end date doesn't equal next contribution";
	 public static final String SAVING_ACCOUNT_E15 = "Ongoing is true but end date is set";
	 public static final String SAVING_ACCOUNT_E16 = "Ongoing is false but end date is not set";
	 public static final String SAVING_ACCOUNT_E17 = "Next contribution comes after end/goal date but current amount not enough to meet goal";
	 public static final String SAVING_ACCOUNT_E18 = "Interest rate is less than 0";
	 
	 
	 /* Money Owed error messages */
	 public static final String MONEY_OWED_NAME_INVALID = "MONEY OWED NAME NULL OR EMPTY";
	 public static final String MONEY_OWED_AMOUNT_INVALID = "MONEY OWED AMOUNT IS A NEGATIVE VALUE";
	 public static final String MONEY_OWED_PAYMENT_INVALID = "PAYMENT NOT MEANT FOR THIS DEBT";
	 public static final String MONEY_OWED_NEGATIVE_INTEREST_RATE = "INTEREST RATE SHOULD BE POSITIVE OR ZERO";
	 
	 /* Payment error messages 
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
	 * 			16 - Goal date not null and next payment before or on goal date, no interest rate to consider,
	 * 					 and debt, frequency, goal date, next payment and payment amount are inconsistent
	  * 
	  * */
	 public static final String PAYMENT_E1 = "Payoff true but payment amount not -1";
	 public static final String PAYMENT_E2 = "Payment amount <= 0";
	 public static final String PAYMENT_E3 = "Payoff true and goalDate not null";
	 public static final String PAYMENT_E4 = "Frequency is not set";
	 public static final String PAYMENT_E5 = "Next payment is null and frequency is not one time";
	 public static final String PAYMENT_E6 = "Goal date not null and next payment after goal date and debt amount not 0";
	 public static final String PAYMENT_E7 = "Frequency one time and nextPayment not null and nextPayment not equal to endDate";
	 public static final String PAYMENT_E8 = "Frequency one time and goal date not null";
	 public static final String PAYMENT_E9 = "Ongoing true and frequency one time";
	 public static final String PAYMENT_E10 = "Source account is not set";
	 public static final String PAYMENT_E11 = "Ongoing is true but end date set";
	 public static final String PAYMENT_E12 = "Ongoing is true but goal date set";
	 public static final String PAYMENT_E13 = "Ongoing is false but end date is not set";
	 public static final String PAYMENT_E14 = "Debt is not set";
	 public static final String PAYMENT_E15 = "Ongoing false and goal date not null and goal date not equal to end date";
	 public static final String PAYMENT_E16 = "Goal date not null and next payment before or on goal date, no interest rate to consider, "
	 												+ "and debt, frequency, goal date, next payment and payment amount are inconsistent";

	 /* Money Gain error messages 
	  * 
	 *		0 - Values are valid
	 * 		1 - Source is not set
	 * 		2 - Amount is a negative value
	 * 		3 - Frequency is not set
	 * 		5 - Frequency is one time but end date is null (indicating ongoing)
	 * 		7 - Next deposit is null but frequency is not one time
	 * 		8 - Next deposit is not null and frequency is one time but end date doesn't match next deposit
	 * 		6 - Destination is not set
	  * */
	 public static final String GAIN_SOURCE_NOT_SET_E1 = "Source is not set";
	 public static final String GAIN_AMOUNT_NEGATIVE_E2 = "Amount is a negative value";
	 public static final String GAIN_FREQUENCY_NOT_SET_E3 = "Frequency is not set";
	 public static final String GAIN_ONETIME_END_NULL_E5 = "Frequency is one time but end date is null (indicating ongoing)";
	 public static final String GAIN_NEXT_DEPOSIT_NULL_NOT_ONE_TIME_E7 = "Next deposit is null but frequency is not one time";
	 public static final String GAIN_ONE_TIME_NEXT_DEPOSIT_MISMATCH_END_E8 = "Next deposit is not null and frequency is one time but end date doesn't match next deposit";
	 public static final String GAIN_DESTINATION_NOT_SET_E6 = "Destination is not set";

	 /* Money Loss error messages 
	  * 
	 * 		0 - Values are valid
	 * 		1 - The expense string is not set or is empty
	 * 		2 - The amount is negative
	 * 		5 - Frequency is one time but end date is null
	 * 		7 - Next loss is null but frequency is not one time (next loss null indicates one time payment already processed)
	 * 		8 - Next loss not null and frequency one time but next loss doesn't match end date
	 * 		6 - Source isn't set
	  * 
	  * */
	 public static final String LOSS_EXPENSE_NOT_SET_E1 = "The expense string is not set or is empty";
	 public static final String LOSS_AMOUNT_NEGATIVE_E2 = "The amount is negative";
	 public static final String LOSS_ONE_TIME_END_DATE_NULL_E5 = "Frequency is one time but end date is null";
	 public static final String LOSS_NEXT_LOSS_NULL_NOT_ONE_TIME_E7 = "Next loss is null but frequency is not one time (next loss null indicates one time payment already processed)";
	 public static final String LOSS_NEXT_LOSS_END_DATE_MISMATCH_E8 = "Next loss not null and frequency one time but next loss doesn't match end date";
	 public static final String LOSS_SOURCE_NOT_SET_E6 = "Source isn't set";
	 
	 /* Budget Item error messages */
	 public static final String BUDGET_ITEM_PRORATED_ERROR_E1 = "The prorated item does not have a frequency of weekly, monthly, or yearly";
	 
	 /* Budget error messages */
	 public static final String BUDGET_SOURCE_NOT_SET_E1 = "Budget source is not set";

	/**
	 * Constructor for BadBudgetInvalidValueException
	 */
	public BadBudgetInvalidValueException()
	{
		super();
	}
	
	/**
	 * Constructor for BadBudgetInvalidValueException that takes a message
	 * @param message - a message describing the error that occurred
	 */
	public BadBudgetInvalidValueException(String message)
	{
		super(message);
	}
}
