package com.erikartymiuk.badbudgetlogic.budget;

import java.util.Calendar;
import java.util.Date;

import com.erikartymiuk.badbudgetlogic.main.BadBudgetInvalidValueException;
import com.erikartymiuk.badbudgetlogic.main.Frequency;
import com.erikartymiuk.badbudgetlogic.main.MoneyLoss;
import com.erikartymiuk.badbudgetlogic.main.Source;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataBudgetItem;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataMoneyLoss;

/**
 * Class representing an item that should be tracked and budgeted regularly.
 * Subclass of MoneyLoss
 */
public class BudgetItem extends MoneyLoss
{
	private boolean proratedStart;	//Indicates if the budgetAmount should be prorated given remaining time before reset and how 
										//much time has passed
	
	private double plusAmount;		//How much the plus button increases the amount remaining
	private double minusAmount;		//How much the minus button decreases the amount remaining
	
	private double currAmount;		//The current amount remaining
	
	private RemainAmountAction remainAmountAction;
			
	/**
	 * Constructor for a Budget Item
	 * 
	 * @param description - description of the new item to be budgeted, cannot be null or empty
	 * @param budgetAmount - the amount to allocate to this budget item per cycle, cannot be negative
	 * @param frequency - the frequency for resets, if one time end must match start 
	 * @param startDate - when to begin considering this item as having started, cannot be null
	 * @param endDate - when to stop this items consideration, if frequency one time must match start date
	 * @param proratedStart - indicates if initial start amount should be prorated, can only be true if
	 * 							frequency is weekly, monthly, or TODO yearly
	 * @param source - this budgetItem's source of funding, cannot be null
	 * @throws BadBudgetInvalidValueException 
	 */
	public BudgetItem(String description, double budgetAmount, Frequency frequency, Date startDate, Date endDate, 
			boolean proratedStart, Source source) throws BadBudgetInvalidValueException
	{
		super(description, budgetAmount, frequency, startDate, endDate, source);
		
		int errorCode = BudgetItem.verifyValues(frequency, proratedStart);
		if (errorCode == 0)
		{
			this.proratedStart = proratedStart;
			
			//Default Values can be changed by accessor methods
			this.plusAmount = 1.0;
			this.minusAmount = -1.0;
			this.currAmount = 0;
			this.remainAmountAction = RemainAmountAction.accumulates;
		}
		else
		{
			String errorMessage = "";
			switch (errorCode)
			{
				case 1:
				{
					errorMessage = BadBudgetInvalidValueException.BUDGET_ITEM_PRORATED_ERROR_E1;
					break;
				}
				default:
				{
					break;
				}
			}
			throw new BadBudgetInvalidValueException(errorMessage);
		}
		
	}
	
	/**
	 * Checks the values for validity specific to budget items. If an error is found returns an error code
	 * indicating which error, if not 0 is returned. Possible errors and the return code are:
	 * 
	 * 		0 - Values are valid
	 * 		1 - Prorated start is true but frequency is not valid for a prorated start.
	 * 
	 * @param frequency - frequency of a budget item
	 * @param proratedStart - prorated start status of a potential budget item
	 * @return an error code inidcating an error or 0 if no error was found
	 */
	public static int verifyValues(Frequency frequency, boolean proratedStart)
	{
		//Invalid frequency for prorated start
		if (proratedStart && !(frequency.equals(Frequency.weekly) || frequency.equals(Frequency.monthly) || frequency.equals(Frequency.yearly)))
		{
			return 1;
		}
		else 
		{
			return 0;
		}
	}
	
	/**
	 * Increase the plus button amount
	 * @param increase - the amount of increase
	 */
	public void increasePlus(double increase)
	{
		this.plusAmount+=increase;
	}
	
	/**
	 * Decrease the plus button amount
	 * @param decrease - the amount of decrease
	 */
	public void decreasePlus(double decrease)
	{
		this.plusAmount-=decrease;
	}
	
	/**
	 * Increase the minus button amount
	 * @param increase - the amount of increase
	 */
	public void increaseMinus(double increase)
	{
		this.minusAmount+=increase;
	}
	
	/**
	 * Decrease the minus button amount
	 * @param decrease - the amount to decrease
	 */
	public void decreaseMinus(double decrease)
	{
		this.minusAmount-=decrease;
	}

	/* Getters and Setters */
	public String getDescription() {
		return this.expenseDescription();
	}

	public boolean isProratedStart() {
		return proratedStart;
	}

	public void setProratedStart(boolean proratedStart) {
		this.proratedStart = proratedStart;
	}

	public double getPlusAmount() {
		return plusAmount;
	}

	public void setPlusAmount(double plusAmount) {
		this.plusAmount = plusAmount;
	}

	public double getMinusAmount() {
		return minusAmount;
	}

	public void setMinusAmount(double minusAmount) {
		this.minusAmount = minusAmount;
	}

	public double getCurrAmount() {
		return currAmount;
	}

	public void setCurrAmount(double currAmount) {
		this.currAmount = currAmount;
	}
	
	public RemainAmountAction remainAmountAction() {
		return this.remainAmountAction;
	}

	public void setRemainAmountAction(RemainAmountAction remainAmountAction) {
		this.remainAmountAction = remainAmountAction;
	}
	
	/**
	 * Adds the given row to this budget items predict data list (at the end)
	 * 
	 * @param row - the predictData to add to the end of this objects list
	 */
	public void addPredictData(PredictDataBudgetItem row)
	{
		super.addPredictData(row);
	}
	
	/**
	 * Add the row at index to the predict data list of this budget item
	 * 
	 * @param index - the location to add the row
	 * @param row - the row to add to the predict data list
	 */
	public void setPredictData(int index, PredictDataBudgetItem row)
	{
		super.setPredictData(index, row);
	}
	
	/**
	 * Returns the PredictData at dayIndex for this budget item
	 * 
	 * @param dayIndex - the date as an index off the starting date in the prediction algorithm
	 * @return the predict data row at the given index (corresponds to the data on a specific date for this budget item)
	 */
	public PredictDataBudgetItem getPredictData(int dayIndex)
	{
		return (PredictDataBudgetItem) super.getPredictData(dayIndex);
	}
	
	/**
	 * After running the prediction algorithm this method updates this budget item's next withdrawal date to 
	 * the value it would have on the day represented by day index. Also if auto reset is set this method
	 * sets the current amount for this budget item based on if a loss occurs during the period specified taking
	 * into account the remain action of this item.
	 * TODO 10/10, This is implying that a budget item is matched with a budget for its reset values, may want to resolve this conflict
	 * 
	 * @param dayIndex - the day, as an offset from the start date used in the prediction algorithm, to update
	 * 						this budget items values to.
	 * @param autoReset - specifies if this budget item should be reset on updates.
	 */
	public void update(int dayIndex, boolean autoReset)
	{
		super.update(dayIndex);
		
		if (autoReset)
		{
			//Check for any losses that occur during the update-prediction period and set the curr amount
			//as necessary
			for (int i = 0; i <= dayIndex; i++)
			{
				double lossAmountToday = this.getPredictData(i).getLossAmountToday();
				if (lossAmountToday != -1)
				{
					if (this.remainAmountAction == RemainAmountAction.accumulates)
					{
						this.setCurrAmount(this.currAmount+lossAmountToday);
					}
					else if (this.remainAmountAction == RemainAmountAction.disappear)
					{
						this.setCurrAmount(lossAmountToday);
					}
					else if (this.remainAmountAction == RemainAmountAction.addBack)
					{
						this.setCurrAmount(lossAmountToday);
					}
					else
					{
						//Default to accumulation
						this.setCurrAmount(this.currAmount+lossAmountToday);
					}
				}
			}
		}
	}
	
	/**
	 * This method uses the results of a prediction algorithm run to update the next dates
	 * of this budget item. If auto reset is set it also updates the remaining amount of this
	 * budget item looking at losses that occurred on days throughout the prediction run and also
	 * considering the remain action of the budget item. A remain action of addBack is not valid for
	 * a budget item if this method is being called and the behavior in this case is undefined.
	 * @param dayIndex - the day to update our budget item to
	 * @param autoReset - specifies if this budget item should be reset on updates.
	 */
	public void updateNextDatesOnly(int dayIndex, boolean autoReset)
	{
		super.updateNextDatesOnly(dayIndex);
		if (autoReset)
		{
			//Check for any losses that occur during the update-prediction period and set the curr amount
			//as necessary
			for (int i = 0; i <= dayIndex; i++)
			{
				double lossAmountToday = this.getPredictData(i).getLossAmountToday();
				if (lossAmountToday != -1)
				{
					if (this.remainAmountAction == RemainAmountAction.accumulates)
					{
						this.setCurrAmount(this.currAmount+lossAmountToday);
					}
					else if (this.remainAmountAction == RemainAmountAction.disappear)
					{
						this.setCurrAmount(lossAmountToday);
					}
					else if (this.remainAmountAction == RemainAmountAction.addBack)
					{
						//Shouldn't change values of account/debt if only updating next dates
						//TODO 1/12/2017 - throw exception?
						this.setCurrAmount(lossAmountToday);
					}
					else
					{
						//Default to accumulation
						this.setCurrAmount(this.currAmount+lossAmountToday);
					}
				}
			}
		}
	}
}
