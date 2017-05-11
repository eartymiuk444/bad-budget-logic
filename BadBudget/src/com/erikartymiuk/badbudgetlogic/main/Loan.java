package com.erikartymiuk.badbudgetlogic.main;

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
	 * @param name - String descriptor of this loan
	 * @param debt - Current balance owed for this loan
	 * @param quicklook - Indicates if this loan should be considered for quicklook
	 * @param interestRate - the interest rate for this loan. will be applied daily either as simple
	 * 							or compound interest
	 * @param simpleInterest - indicates if the interest rate should be treated as simple or compound interest. ignored if interest rate is 0.
	 * @param principalBalance - the balance to treat as the principal for simple interest calculations. if interest rate 0 or simpleInterest is false then 
	 * 								this value is ignored. If principal balance is greater than the debt amount then it is set to the debt amount.
	 * @throws BadBudgetInvalidValueException 
	 */
	public Loan(String name, double debt, boolean quicklook, double interestRate, boolean simpleInterest, double principalBalance) throws BadBudgetInvalidValueException
	{
		super(name, debt, quicklook, interestRate);
		this.simpleInterest = simpleInterest;
		if (this.interestRate() != 0 && this.simpleInterest && this.principalBalance < this.amount())
		{
			this.principalBalance = principalBalance;
			this.interestAmount = this.amount() - this.principalBalance;
		}
		else
		{
			this.principalBalance = this.amount();
			this.interestAmount = 0;
		}
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
}
