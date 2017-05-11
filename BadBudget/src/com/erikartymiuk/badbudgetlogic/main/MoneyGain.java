package com.erikartymiuk.badbudgetlogic.main;
import java.util.*;

import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataMoneyGain;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataMoneyOwed;

/**
 * Class representing any sort of money gain, one time or recurring. 
 * Examples include a regular paycheck, a one time financial aid refund,
 * etc.
 */
public class MoneyGain 
{
	private String source; //String description of where the money is coming from
	private double amount; //How much is being gained
	private Frequency frequency; //How often is this gain happening
	private Date endDate; //The money gain won't be processed after this date (can be processed on this date)
							//if null then gain is ongoing
	
	private Account destination; //What account to put the amount in (not a savings account)
	
	private Date nextDeposit; //The date the next deposit should be handled. null if no more deposits to handle. (only valid for processed one time freq)
								//can occur after end date.
	
	private ArrayList<PredictDataMoneyGain> predictData; //The prediction data needed for making predictions. Populated during the prediction algorithm.
	
	/**
	 * MoneyGain constructor. Verifies the passed values form a valid gain object first.
	 * 
	 * @param source - Description of the source of this gain, cannot be null
	 * @param amount - the amount of the gain, >= 0
	 * @param frequency - How often the gain happens, if one time nextDeposit and end must be equal (or nextDeposit null) cannot be null
	 * @param nextDeposit - the date of the next deposit, if null then no more deposits to handle only valid with one time freq. 
	 * 							can occur after end date
	 * @param endDate - the date to stop processing the gain. If frequency is one time, must match nextDeposit (unless nextDeposit null)
	 * @param destination - the destination account, cannot be null
	 * @throws BadBudgetInvalidValueException 
	 */
	public MoneyGain(String source, double amount, Frequency frequency, Date nextDeposit, Date endDate, Account destination) throws BadBudgetInvalidValueException
	{
		int errorCode = verifyValues(source, amount, frequency, nextDeposit, endDate, destination);
		if (errorCode == 0)
		{
			this.source = source;
			this.amount = amount;
			this.frequency = frequency;
			this.nextDeposit = nextDeposit;
			this.endDate = endDate;
			this.destination = destination;
			
			this.predictData = new ArrayList<PredictDataMoneyGain>();
		}
		else
		{
			String errorMessage = "";
			switch (errorCode)
			{
				case 1:
				{
					errorMessage = BadBudgetInvalidValueException.GAIN_SOURCE_NOT_SET_E1;
					break;
				}
				case 2:
				{
					errorMessage = BadBudgetInvalidValueException.GAIN_AMOUNT_NEGATIVE_E2;
					break;
				}
				case 3:
				{
					errorMessage = BadBudgetInvalidValueException.GAIN_FREQUENCY_NOT_SET_E3;
					break;
				}
				case 5:
				{
					errorMessage = BadBudgetInvalidValueException.GAIN_ONETIME_END_NULL_E5;
					break;
				}
				case 7:
				{
					errorMessage = BadBudgetInvalidValueException.GAIN_NEXT_DEPOSIT_NULL_NOT_ONE_TIME_E7;
					break;
				}
				case 8:
				{
					errorMessage = BadBudgetInvalidValueException.GAIN_ONE_TIME_NEXT_DEPOSIT_MISMATCH_END_E8;
					break;
				}
				case 6:
				{
					errorMessage = BadBudgetInvalidValueException.GAIN_DESTINATION_NOT_SET_E6;
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
	 * Error checking method for a money gain object. Reports the following errors using the corresponding error codes
	 * 		
	 * 		0 - Values are valid
	 * 		1 - Source is not set
	 * 		2 - Amount is a negative value
	 * 		3 - Frequency is not set
	 * 		5 - Frequency is one time but end date is null (indicating ongoing)
	 * 		7 - Next deposit is null but frequency is not one time
	 * 		8 - Next deposit is not null and frequency is one time but end date doesn't match next deposit
	 * 		6 - Destination is not set
	 * 
	 * @param source
	 * @param amount
	 * @param frequency
	 * @param nextDeposit
	 * @param endDate
	 * @param destination
	 * @return an integer representing an error in the data for the money gain or 0 if no errors were found
	 */
	public static int verifyValues(String source, double amount, Frequency frequency, Date nextDeposit, Date endDate, Account destination)
	{
		if (source == null)
		{
			return 1;
		}
		else if (amount < 0)
		{
			return 2;
		}
		else if (frequency == null)
		{
			return 3;
		}
		else if (frequency.equals(Frequency.oneTime) && endDate == null)
		{
			return 5;
		}
		else if (nextDeposit == null && !frequency.equals(Frequency.oneTime))
		{
			return 7;
		}
		else if (nextDeposit != null && frequency.equals(Frequency.oneTime) && !Prediction.datesEqualUpToDay(endDate, nextDeposit))
		{
			return 8;
		}
		else if (destination == null)
		{
			return 6;
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * Processes the next deposit. Takes the amount of the money gain and deposits it
	 * into the destination account. Updates the next deposit date using the frequency.
	 * Should check nextDeposit date first to verify this is desired.
	 */
	public void handleNextDeposit()
	{
		this.destination.deposit(this.amount);
		this.nextDeposit = this.calculateNextDeposit(this.nextDeposit);
	}
	
	/**
	 * Calculate the next deposit date using the given last deposit date and this
	 * money gain's frequency.
	 * 
	 * @param lastDepositDate - the date the last deposit occurred
	 * 
	 * @return - the next deposit date using the given last deposit. Returns null if a one time deposit.
	 */
	public Date calculateNextDeposit(Date lastDepositDate)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastDepositDate);
		
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
	 * Add a row to the end of this money gain objects predict data list.
	 * 
	 * @param mg - the predictData row to add (should correspond to a single date)
	 */
	public void addPredictData(PredictDataMoneyGain mg)
	{
		this.predictData.add(mg);
	}
	
	/**
	 * Add a row to the money gain's predict data list at index
	 * @param index - the location to add the predict row at
	 * @param mg - the predict row to add
	 */
	public void setPredictData(int index, PredictDataMoneyGain mg)
	{
		if (index < this.predictData.size())
		{
			this.predictData.set(index, mg);
		}
		else
		{
			this.predictData.add(mg);
		}
	}
	
	/**
	 * Returns the row corresponding to the dayIndex passed in, in this moneyGain object
	 * PredictData list. MoneyGains predictData tells us when the nextDeposit will occur
	 * on the date corresponding to the row returned.
	 * 
	 * @param dayIndex - the number of days since the start that we want the data for. 
	 * @return PredictDataMoneyGain row in this object prediction list corresponding to the index
	 * 			passed in
	 */
	public PredictDataMoneyGain getPredictData(int dayIndex)
	{
		return this.predictData.get(dayIndex);
	}
	
	/* Getters and setters */
	
	public String sourceDescription()
	{
		return this.source;
	}
	
	public double gainAmount()
	{
		return this.amount;
	}
	
	public Frequency gainFrequency()
	{
		return this.frequency;
	}
	
	public Date endDate()
	{
		return this.endDate;
	}
	
	public Account destinationAccount()
	{
		return this.destination;
	}
	
	public Date nextDeposit()
	{
		return this.nextDeposit;
	}
	
	public void setSourceDescription(String s)
	{
		this.source = s;
	}
	
	public void setGainAmount(double amount)
	{
		this.amount = amount;
	}
	
	public void setGainFrequency(Frequency f)
	{
		this.frequency = f;
	}
	
	public void setEndDate(Date end)
	{
		this.endDate = end;
	}
	
	public void setDestination(Account d)
	{
		this.destination = d;
	}
	
	public void setNextDeposit(Date next)
	{
		this.nextDeposit = next;
	}
	
	/**
	 * Empties (or Resets) this moneygains predict data list.
	 */
	public void clearPredictData()
	{
		this.predictData = new ArrayList<PredictDataMoneyGain>();
	}
	
	/**
	 * After running the prediction algorithm this method updates this gain's next deposit date to 
	 * the value's it would have on the day represented by day index. 
	 * 
	 * @param dayIndex - the day, as an offset from the start date used in the prediction algorithm, to update
	 * 						this gains values to.
	 */
	public void update(int dayIndex)
	{
		PredictDataMoneyGain pdmg = this.getPredictData(dayIndex);
		this.nextDeposit = pdmg.nextDeposit();
	}
	
	/**
	 * Updates the next gain date for this gain.
	 * @param dayIndex - the day to update this gain to.
	 */
	public void updateNextDatesOnly(int dayIndex)
	{
		PredictDataMoneyGain pdmg = this.getPredictData(dayIndex);
		this.nextDeposit = pdmg.nextDeposit();
	}

}
