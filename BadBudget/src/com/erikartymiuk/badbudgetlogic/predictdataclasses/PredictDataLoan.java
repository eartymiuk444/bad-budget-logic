package com.erikartymiuk.badbudgetlogic.predictdataclasses;

import java.util.Date;

public class PredictDataLoan extends PredictDataMoneyOwed {
	
	private double principal;
	private double interest;
	
	/**
	 * Constructor for the predict data loan object 
	 * @param date - the date this row has data on
	 * @param value - the value on the date
	 * @param nextDate - the next payment date to initialize this to
	 * @param nextInterestAccumulationDate - the next date interest should accumulate
	 * @param accumulatedInterest - the accumulated interest on this date
	 * @param principal - the principal amount of this loan on the date
	 * @param interest - the interest of the loan on this date
	 */
	public PredictDataLoan(Date date, double value, Date nextDate, Date nextInterestAccumulationDate, double accumulatedInterest, double principal, double interest)
	{
		super(date, value, nextDate, nextInterestAccumulationDate, accumulatedInterest);
		this.principal = principal;
		this.interest = interest;
	}

	public double getPrincipal() {
		return principal;
	}

	public void setPrincipal(double principal) {
		this.principal = principal;
	}

	public double getInterest() {
		return interest;
	}

	public void setInterest(double interest) {
		this.interest = interest;
	}

}
