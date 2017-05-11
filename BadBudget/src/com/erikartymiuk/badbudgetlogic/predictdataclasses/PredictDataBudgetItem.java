package com.erikartymiuk.badbudgetlogic.predictdataclasses;

import java.util.Date;
/**
 *
 * Predict data for a budget item as used by the prediction algorithm
 * to keep track of when the next loss will take place on any given date
 * along with how much of a loss occurred on that date (if a loss did occur).
 *
 * @author Erik Artymiuk
 *
 */
public class PredictDataBudgetItem extends PredictDataMoneyLoss {

	private double lossAmountToday;	//-1 indicates no loss occurred on this day
	
	/*
	 * The original value on this day and an updated value if a loss occurs
	 */
	private double originalAmount;
	private double updatedAmount;
	
	/**
	 * Constructor for the Predict Data for a budget item. Extension of the Money Loss
	 * predict data but also includes the lossAmountToday which indicates how much
	 * of a loss on this day came from the budget item this is attached to.
	 * @param date - the date this predict row stands for
	 * @param next - the next loss date on the date
	 * @param lossAmountToday - the loss that occurs/occurred on this date
	 */
	public PredictDataBudgetItem(Date date, Date next, double lossAmountToday) {
		super(date, next);
		this.lossAmountToday = lossAmountToday;
	}
	
	/**
	 * Constructor for the Predict Data for a budget item. Extension of the Money Loss
	 * predict data but also includes the lossAmountToday which indicates how much
	 * of a loss on this day came from the budget item this is attached to. No loss
	 * amount today is specified so it is set to -1 (i.e no loss occurred/occurs on this
	 * day).
	 * @param date - the date this predict row stands for
	 * @param next - the next loss date on the date
	 */
	public PredictDataBudgetItem(Date date, Date next) {
		super(date, next);
		this.lossAmountToday = -1;
	}
	
	/**
	 * Indicates how much of a loss occurred on this day due to this budget item.
	 * Returns -1 if no loss occurred on this day
	 * @return the loss amount that occurred on this day or -1 if no loss occurred
	 */
	public double getLossAmountToday()
	{
		return this.lossAmountToday;
	}
	
	/**
	 * Set how much of a loss occurs on the date associated with this pdbi row
	 * @param lossAmountToday how much of loss occurs/occurred on this date, -1 if no loss
	 * 							does.
	 */
	public void setLossAmountToday(double lossAmountToday)
	{
		this.lossAmountToday = lossAmountToday;
	}

	public double getOriginalAmount() {
		return originalAmount;
	}

	public void setOriginalAmount(double originalAmount) {
		this.originalAmount = originalAmount;
	}

	public double getUpdatedAmount() {
		return updatedAmount;
	}

	public void setUpdatedAmount(double updatedAmount) {
		this.updatedAmount = updatedAmount;
	}
	
	
}
