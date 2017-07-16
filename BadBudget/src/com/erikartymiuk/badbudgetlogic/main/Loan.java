package com.erikartymiuk.badbudgetlogic.main;

import java.util.Date;

import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataLoan;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataMoneyOwed;

/* 
 * A type of MoneyOwed. Represents loans.
 */
public class Loan extends MoneyOwed 
{
	private boolean simpleInterest;
	private double principalBalance;
	private double interestAmount;
	
	/** Constructor for the Loan class.
	 * 
	 * @param name - name or description of the debt, cannot be null or empty
	 * @param debt - debt amount, cannot be negative
	 * @param quicklook - Indicates if this loan should be considered for quicklook
	 * @param interestRate - the interest rate of this debt. will be applied daily at rate/365.25 as either simple
	 * 							or compound interest
	 * 							0 indicates no interest rate should be considered and a negative value is invalid.
	 * @param simpleInterest - indicates if the interest rate should be treated as simple or compound interest. ignored if interest rate is 0.
	 * @param principalBalance - the balance to treat as the principal for simple interest calculations. if interest rate 0 or simpleInterest is false then 
	 * 								this value is ignored. Principal balance cannot be greater than the debt amount.
	 * @throws BadBudgetInvalidValueException 
	 */
	public Loan(String name, double debt, boolean quicklook, double interestRate, boolean simpleInterest, double principalBalance) throws BadBudgetInvalidValueException
	{
		super(name, debt, quicklook, interestRate);
		this.simpleInterest = simpleInterest;
		int error = verifyValues(debt, simpleInterest, principalBalance);
		if (error == 0)
		{
			this.principalBalance = principalBalance;
			this.interestAmount = this.amount() - this.principalBalance;
		}
		else
		{
			String message = "";
			switch (error)
			{
				case 1:
				{
					message = BadBudgetInvalidValueException.LOAN_PRINCIPAL_GREATER_DEBT;
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
	 * Private helper mehtod that verifies the values for loan specific debt values.
	 * Super should be used to check values of generic debts.
	 * 
	 * 		0 - Values are good
	 * 		1 - Loan is a simple interest loan and the principal amount is greater (strict) than the debt amount
	 * 
	 * @param debt - the debt amount
	 * @param simpleInterest
	 * @param principalBalance
	 * @return - an error code indicating various errors of loan specific debt values.
	 */
	private int verifyValues(double debt, boolean simpleInterest, double principalBalance)
	{
		if (simpleInterest && principalBalance > debt)
		{
			return 1;
		}
		return 0;
	}

	public boolean isSimpleInterest() {
		return simpleInterest;
	}

	public void setSimpleInterest(boolean simpleInterest) {
		this.simpleInterest = simpleInterest;
	}

	public double getPrincipalBalance() {
		return principalBalance;
	}

	public void setPrincipalBalance(double principalBalance) {
		this.principalBalance = principalBalance;
	}

	public double getInterestAmount() {
		return interestAmount;
	}

	public void setInterestAmount(double interestAmount) {
		this.interestAmount = interestAmount;
	}
	
	/**
	 * Adds the given row to this loan objects predict data list (at the end)
	 * 
	 * @param row - the predictData to add to the end of this objects list
	 */
	public void addPredictData(PredictDataLoan row)
	{
		super.addPredictData(row);
	}
	
	/**
	 * Add the passed row at index to this loan object predict data list.
	 * 
	 * @param index - the location to add the given row
	 * @param row - the row to add
	 */
	public void setPredictData(int index, PredictDataLoan row)
	{
		super.setPredictData(index, row);
	}
	
	/**
	 * Returns the PredictData at dayIndex for this loan
	 * 
	 * @param dayIndex - the date as an index off the starting date in the prediction algorithm
	 * @return the predict data row at the given index (corresponds to the data on a specific date for this loan)
	 */
	public PredictDataLoan getPredictData(int dayIndex)
	{
		return (PredictDataLoan) super.getPredictData(dayIndex);
	}
	
	/**
	 * Overridden update method for loans. Calls super first.
	 * After running the prediction algorithm this method updates this loan's values to 
	 * the value's it would have on the day represented by day index. Calls super first,
	 * then updates the principal and interest values to the value on the specified dayIndex.
	 * 
	 * @param predictEndDate - the date our prediction algorithm ends. Any interest calculations treat this + 1 day as the start date.
	 * @param dayIndex - the day, as an offset from the start date used in the prediction algorithm, to update
	 * 						this debts values to.
	 */
	public void update(Date predictEndDate, int dayIndex)
	{
		super.update(predictEndDate, dayIndex);
		PredictDataLoan pdl = this.getPredictData(dayIndex);
		
		double uPrincipal = pdl.getPrincipal();
		double uInterest = pdl.getInterest();
		
		this.principalBalance = uPrincipal;
		this.interestAmount = uInterest;
	}
}
