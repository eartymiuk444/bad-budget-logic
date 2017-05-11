package com.erikartymiuk.badbudgetlogic.budget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.erikartymiuk.badbudgetlogic.main.BadBudgetInvalidValueException;
import com.erikartymiuk.badbudgetlogic.main.Frequency;
import com.erikartymiuk.badbudgetlogic.main.Prediction;
import com.erikartymiuk.badbudgetlogic.main.Source;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataMoneyLoss;

/** 
 * Class representing the user's total budget. Consists of a list of items to be budgeted,
 * and also a set of preferences for the users budget.
 */
public class Budget 
{
	private HashMap<String, BudgetItem> items;	//A map of the users budget items. The key is the budget items description
	
	private boolean autoReset;				//Indicates if the user would like their budgetItems to automatically reset
	private Source budgetSource;			//The source for all budget items
	private int weeklyResetTime;	//For items that have a frequency of weekly, the day the user would like them to reset on at 12:00AM (if auto)
										//should match the specs for DAY_OF_WEEK given by Calendar class.
	private int monthlyResetTime;	//For items that have a frequency of monthly, the day of the month the user would like them to reset on (if auto)
										//should match the specs for DAY_OF_MONTH given by Calendar class.
	
	
	/**
	 * Constructor for an empty budget starting with the specified settings
	 * 
	 * @param budgetSource - the source for all budgetItems in this budget, cannot be null
	 * @param autoReset - indicates if we should auto reset budget items
	 * @param weeklyReset - For items that have a frequency of weekly, the day the user would like them to reset on at 12:00AM (if auto)
										should match the specs for DAY_OF_WEEK given by Calendar class.
	 * @param monthlyReset - For items that have a frequency of monthly, the day of the month the user would like them to reset on (if auto)
										should match the specs for DAY_OF_MONTH given by Calendar class.
	 * @throws BadBudgetInvalidValueException 
	 */
	public Budget(Source budgetSource, boolean autoReset,
			int weeklyReset, int monthlyReset) throws BadBudgetInvalidValueException
	{
		int errorCode = verifyValues(budgetSource);
		if (errorCode == 0)
		{
			this.budgetSource = budgetSource;
			this.autoReset = autoReset;
			this.weeklyResetTime = weeklyReset;
			this.monthlyResetTime = monthlyReset;
			this.items = new HashMap<String, BudgetItem>();
		}
		else
		{
			String errorMsg = "";
			switch (errorCode)
			{
				case 1:
				{
					errorMsg = BadBudgetInvalidValueException.BUDGET_SOURCE_NOT_SET_E1;
					break;
				}
				default:
				{
					errorMsg = BadBudgetInvalidValueException.UNKNOWN_UNDEFINED;
					break;
				}
			}
			throw new BadBudgetInvalidValueException(errorMsg);
		}		
	}
	
	/**
	 * Error checking method for a Budget. Ensures that the budget source and manual reset button are both set.
	 * Returns an error code indicating an error if one is found or 0 if none are found. Possible error codes
	 * are:
	 * 		0 - Values are valid
	 * 		1 - Budget source is not set
	 * 
	 * @param budgetSource - the budget source
	 * @return - an int indicating an error or 0 if no errors were found
	 */
	public static int verifyValues(Source budgetSource)
	{
		if (budgetSource == null)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * Given a budget item and the date of the last handled loss this method calculates the loss date
	 * that occurs after the handled loss using the reset values defined by this budget object
	 * 
	 * @param item - the item we want the next loss calculated for. We use this items frequency
	 * @param lastLoss - the date of the last handled loss, We use this as the starting point for 
	 * 						calculating the next loss date.
	 * @return - the date of the next loss of item given that the last handled loss was lastLoss
	 * 				or null if their is no more losses
	 */
	public Date calculateNextLoss(BudgetItem item, Date lastLoss)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(lastLoss);
		
		switch (item.lossFrequency())
		{
			case biWeekly:
			{
				calendar.add(Calendar.WEEK_OF_YEAR, 2);
				return calendar.getTime();
			}
			case daily:
			{
				calendar.add(Calendar.DAY_OF_YEAR, 1);
				return calendar.getTime();
			}
			case monthly:
			{
				int startYear = calendar.get(Calendar.YEAR);
				int startMonth = calendar.get(Calendar.MONTH);
				Date sameMonthReset = sameMonthReset(startYear, startMonth);

				boolean startOccursBefore = Prediction.numDaysBetween(lastLoss, sameMonthReset) > 0;
				
				if (startOccursBefore)
				{
					return sameMonthReset;
				}
				else
				{
					Calendar cal = Calendar.getInstance();
					cal.setTime(sameMonthReset);
					cal.add(Calendar.MONTH, 1);
					return cal.getTime();
				}
			}
			case oneTime:
			{
				return null;
			}
			case weekly:
			{
				int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
				int dayDiff = weekDay - this.weeklyResetTime;
				if (dayDiff == 0)
				{
					calendar.add(Calendar.WEEK_OF_YEAR, 1);
				}
				if (dayDiff > 0)
				{
					calendar.add(Calendar.DAY_OF_WEEK, 7-dayDiff);
				}
				else if (dayDiff < 0)
				{
					calendar.add(Calendar.DAY_OF_WEEK, -dayDiff);
				}
				
				return calendar.getTime();
			}
			case yearly:
			{
				calendar.add(Calendar.YEAR, 1);
				return calendar.getTime();			
			}
			default:
			{
				return null;
			}
		}
	}
	
	/**
	 * This method returns the loss amount for the given prorated item and 
	 * the date of the current (unhandled) loss. For unhandled frequencies
	 * the full loss amount of the item is returned. (Currently allowed frequencies
	 * are weekly, monthly, and TODO yearly)
	 * @param item - the item to find the prorated loss amount for
	 * @param currentLoss - the current date we are interested in for handling our loss
	 * @return the prorated loss amount for weekly, monthly, and yearly items else the full
	 * 			loss amount
	 */
	public double lossAmount(BudgetItem item, Date currentLoss)
	{
		Date nextLoss = this.calculateNextLoss(item, currentLoss);
		int daysRemaining = Prediction.numDaysBetween(currentLoss, nextLoss);
		int totalDays = -1;
		
		switch (item.lossFrequency())
		{
			case weekly:
			{
				totalDays = 7;
				break;
			}
			case monthly:
			{
				Calendar previousLossCal = Calendar.getInstance();
				previousLossCal.setTime(nextLoss);
				previousLossCal.add(Calendar.MONTH, -1);
				totalDays = Prediction.numDaysBetween(previousLossCal.getTime(), nextLoss);
				break;
			}
			case yearly:
			{
				Calendar previousLossCal = Calendar.getInstance();
				previousLossCal.setTime(nextLoss);
				previousLossCal.add(Calendar.YEAR, -1);
				totalDays = Prediction.numDaysBetween(previousLossCal.getTime(), nextLoss);
				break;
			}
			default:
			{
				//Unhandled frequency will return full amount
				return item.lossAmount();
			}
		}
		return (((double) daysRemaining)/totalDays)*item.lossAmount();
	}
	
	/**
	 * Private helper method. Given year and month of a start date this method returns what the reset date
	 * would be in that same month. Typically it is simply the reset day of that month. But
	 * if the reset day is greater than the number of days in the current month
	 * then it is the last day of that month.
	 * 
	 * @param year - the year the start date is in
	 * @param month - the month the start date is in 
	 * 
	 * @return - the date of the reset in the same month as the given month
	 */
	private Date sameMonthReset(int year, int month)
	{
		Calendar cal = new GregorianCalendar(year, month, 1);
		int numDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		if (this.monthlyResetTime <= numDays)
		{
			cal.set(Calendar.DAY_OF_MONTH, this.monthlyResetTime);
		}
		else
		{
			cal.set(Calendar.DAY_OF_MONTH, numDays);
		}
		return cal.getTime();
	}

	/**
	 * Adds this budgetItem to the budgets items
	 * @param budgetItem - the budget item to add
	 */
	public void addBudgetItem(BudgetItem budgetItem)
	{
		String key = budgetItem.getDescription();
		items.put(key, budgetItem);
	}
	
	/**
	 * Gets the budgetItem with the given name/description (key)
	 * @param description - the description of the budget item to retrieve
	 * @return BudgetItem - the corresponding budget item or null if no such item exists
	 */
	public BudgetItem retrieveBudgetItem(String description)
	{
		return items.get(description);
	}
	
	/**
	 * Gets all of the users budget items as a map. The key is the budget item's description
	 * 
	 * @return a map of all the user's budget items
	 */
	public Map<String, BudgetItem> getAllBudgetItems()
	{
		return this.items;
	}
	
	/* Getters and Setters */
	
	public Source getBudgetSource() {
		return budgetSource;
	}

	/**
	 * Updates the budget source for this budget. Also takes care of updating any necessary links of
	 * budget items to the new budget source.
	 * @param budgetSource - the Source for this budget
	 */
	public void setBudgetSource(Source budgetSource) 
	{
		this.budgetSource = budgetSource;
		for (BudgetItem item : this.items.values())
		{
			item.setSource(budgetSource);
		}
	}

	public void setAutoReset(boolean autoReset)
	{
		this.autoReset = autoReset;
	}
	
	public boolean isAutoReset()
	{
		return this.autoReset;
	}
	
	/**
	 * For items that have a frequency of weekly, the day the user would like them to reset on at 12:00AM (if auto)
	 * should match the specs for DAY_OF_WEEK given by Calendar class.
	 * @return - the day of the week this budget's weekly items reset
	 */
	public int getWeeklyReset()
	{
		return this.weeklyResetTime;
	}
	
	/**
	 * For items that have a frequency of monthly, the day of the month the user would like them to reset on (if auto)
	 * should match the specs for DAY_OF_MONTH given by Calendar class.
	 * @return - the reset day of the month for monthly budget items
	 */
	public int getMonthlyReset()
	{
		return this.monthlyResetTime;
	}
	
	/**
	 * Setter for the weekly reset time.
	 * @param weekly - the day of the week (should match DAY_OF_WEEK in Calendar specs) to reset weekly
	 * budget items
	 */
	public void setWeeklyReset(int weekly)
	{
		this.weeklyResetTime = weekly;
	}
	
	/**
	 * Setter for the monthly reset time
	 * @param monthly - the day of the month (1-31 should match DAY_OF_MONTH given by Calendar class)
	 * to reset monthly budget items
	 */
	public void setMonthlyReset(int monthly)
	{
		this.monthlyResetTime = monthly;
	}
}
