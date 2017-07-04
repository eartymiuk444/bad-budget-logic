package com.erikartymiuk.badbudgetlogic.main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataMoneyGain;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataMoneyTransfer;

/**
 * Class representing a money transfer. Transfers occur between accounts (including savings accounts). The 
 * exception is a regular account to a savings account as this is reserved for contributions.
 * @author Erik Artymiuk
 *
 */
public class MoneyTransfer {
	
	private String transferDescription; //a unique description for this transfer
	
	private Account source; //The source account
	private Account destination; //The destination account (if source is regular account then destination cannot be savings, this is a contribution,
									//if source is a savings account then destination can be either regular or savings)
	
	private double amount; //The amount to transfer
	private Frequency frequency; //How often this transfer happens
	private Date nextTransfer; //The date the next transfer should be handled or null if no more transfers to handle
								//(null only valid for processed one time freq). Can occur after end date.
	private Date endDate; //The money transfer won't be processed after this date (can be processed on this date)
							//if null then transfer is ongoing
	
	private ArrayList<PredictDataMoneyTransfer> predictData; //The prediction data needed for making predictions. Populated during the prediction algorithm.
	
	/**
	 * MoneyTransfer constructor. Verifies the passed values form a valid transfer object first.
	 * 
	 * @param transferDescription - description for this transfer (cannot be null)
	 * @param source - the source account cannot be null
	 * @param destination - the destination account cannot be null
	 * @param amount - the amount of the transfer, >= 0
	 * @param frequency - How often the transfer happens, if one time nextTransfer and end must be equal (or nextTransfer null) cannot be null
	 * @param nextTransfer - the date of the next transfer, if null then no more deposits to handle (only valid with one time freq). 
	 * 							Can occur after end date.
	 * @param endDate - the date to stop processing the transfer. If frequency is one time, must match nextTransfer (unless nextTransfer null)
	 * @throws BadBudgetInvalidValueException 
	 */
	public MoneyTransfer(String transferDescription, Account source, Account destination, double amount, Frequency frequency, Date nextTransfer, Date endDate) throws BadBudgetInvalidValueException
	{
		int errorCode = verifyValues(transferDescription, source, destination, amount, frequency, nextTransfer, endDate);
		if (errorCode == 0)
		{
			this.transferDescription = transferDescription;
			this.source = source;
			this.destination = destination;
			this.amount = amount;
			this.frequency = frequency;
			this.nextTransfer = nextTransfer;
			this.endDate = endDate;
			
			this.predictData = new ArrayList<PredictDataMoneyTransfer>();
		}
		else
		{
			String errorMessage = "";
			switch (errorCode)
			{
				case 1:
				{
					errorMessage = BadBudgetInvalidValueException.TRANSFER_SOURCE_NOT_SET_E1;
					break;
				}
				case 2:
				{
					errorMessage = BadBudgetInvalidValueException.TRANSFER_DESTINATION_NOT_SET_E2;
					break;
				}
				case 3:
				{
					errorMessage = BadBudgetInvalidValueException.TRANSFER_CONTRIBUTION_RESERVED_E3;
					break;
				}
				case 4:
				{
					errorMessage = BadBudgetInvalidValueException.TRANSFER_AMOUNT_NEGATIVE_E4;
					break;
				}
				case 5:
				{
					errorMessage = BadBudgetInvalidValueException.TRANSFER_FREQUENCY_NOT_SET_E5;
					break;
				}
				case 6:
				{
					errorMessage = BadBudgetInvalidValueException.TRANSFER_ONETIME_END_NULL_E6;
					break;
				}
				case 7:
				{
					errorMessage = BadBudgetInvalidValueException.TRANSFER_NEXT_DEPOSIT_NULL_NOT_ONE_TIME_E7;
					break;
				}
				case 8:
				{
					errorMessage = BadBudgetInvalidValueException.TRANSFER_ONE_TIME_NEXT_DEPOSIT_MISMATCH_END_E8;
				}
				case 9:
				{
					errorMessage = BadBudgetInvalidValueException.TRANSFER_DESCRIPTION_NOT_SET_E9;
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
	
	public String getTransferDescription() {
		return transferDescription;
	}

	public void setTransferDescription(String transferDescription) {
		this.transferDescription = transferDescription;
	}

	/**
	 * Error checking method for a money transfer object. Reports the following errors using the corresponding error codes
	 * 		
	 * 		0 - Values are valid
	 * 		9 - Transfer description is not set.
	 * 		1 - Source is not set
	 * 		2 - Destination is not set
	 * 		3 - Source is not a savings account (a regular account), but destination is a savings account (this is reserved for contributions)
	 * 		4 - Amount is a negative value
	 * 		5 - Frequency is not set
	 * 		6 - Frequency is one time but end date is null (indicating ongoing)
	 * 		7 - Next transfer is null but frequency is not one time
	 * 		8 - Next transfer is not null and frequency is one time but end date doesn't match next transfer
	 * 		
	 * 
	 * @param source
	 * @param destination
	 * @param amount
	 * @param frequency
	 * @param nextTransfer
	 * @param endDate
	 * @return an integer representing an error in the data for the money gain or 0 if no errors were found
	 */
	public static int verifyValues(String transferDescription, Account source, Account destination, double amount, Frequency frequency, Date nextTransfer, Date endDate)
	{
		if (transferDescription == null)
		{
			return 9;
		}
		else if (source == null)
		{
			return 1;
		}
		else if (destination == null)
		{
			return 2;
		}
		else if (!(source instanceof SavingsAccount) && destination instanceof SavingsAccount)
		{
			return 3;
		}
		else if (amount < 0)
		{
			return 4;
		}
		else if (frequency == null)
		{
			return 5;
		}
		else if (frequency.equals(Frequency.oneTime) && endDate == null)
		{
			return 6;
		}
		else if (nextTransfer == null && !frequency.equals(Frequency.oneTime))
		{
			return 7;
		}
		else if (nextTransfer != null && frequency.equals(Frequency.oneTime) && !Prediction.datesEqualUpToDay(endDate, nextTransfer))
		{
			return 8;
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * Calculate the next transfer date using the given last transfer date and this
	 * money transfer's frequency.
	 * 
	 * @param lastTransferDate - the date the last transfer occurred
	 * 
	 * @return - the next transfer date using the given last transfer. Returns null if a one time transfer.
	 */
	public Date calculateNextTransfer(Date lastTransferDate)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastTransferDate);
		
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
	 * Add a row to the end of this money transfer objects predict data list.
	 * 
	 * @param mg - the predictData row to add (should correspond to a single date)
	 */
	public void addPredictData(PredictDataMoneyTransfer pdmt)
	{
		this.predictData.add(pdmt);
	}
	
	/**
	 * Add a row to the money transfer's predict data list at index
	 * @param index - the location to add the predict row at
	 * @param pdmt - the predict row to add
	 */
	public void setPredictData(int index, PredictDataMoneyTransfer pdmt)
	{
		if (index < this.predictData.size())
		{
			this.predictData.set(index, pdmt);
		}
		else
		{
			this.predictData.add(pdmt);
		}
	}
	
	/**
	 * Returns the row corresponding to the dayIndex passed in, in this moneyTransfer object
	 * PredictData list. MoneyTransfers predictData tells us when the nextTransfer will occur
	 * on the date corresponding to the row returned.
	 * 
	 * @param dayIndex - the number of days since the start that we want the data for. 
	 * @return PredictDataMoneyTransfer row in this object prediction list corresponding to the index
	 * 			passed in
	 */
	public PredictDataMoneyTransfer getPredictData(int dayIndex)
	{
		return this.predictData.get(dayIndex);
	}

	/* Getters and Setters */
	public Account getSource() {
		return source;
	}

	public void setSource(Account source) {
		this.source = source;
	}

	public Account getDestination() {
		return destination;
	}

	public void setDestination(Account destination) {
		this.destination = destination;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Frequency getFrequency() {
		return frequency;
	}

	public void setFrequency(Frequency frequency) {
		this.frequency = frequency;
	}

	public Date getNextTransfer() {
		return nextTransfer;
	}

	public void setNextTransfer(Date nextTransfer) {
		this.nextTransfer = nextTransfer;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	/**
	 * Empties (or Resets) this moneytransfers predict data list.
	 */
	public void clearPredictData()
	{
		this.predictData = new ArrayList<PredictDataMoneyTransfer>();
	}
	
	/**
	 * After running the prediction algorithm this method updates this transfer's next deposit date to 
	 * the value's it would have on the day represented by day index. 
	 * 
	 * @param dayIndex - the day, as an offset from the start date used in the prediction algorithm, to update
	 * 						this transfers values to.
	 */
	public void update(int dayIndex)
	{
		PredictDataMoneyTransfer pdmt = this.getPredictData(dayIndex);
		this.nextTransfer = pdmt.nextTransfer();
	}
	
	/**
	 * Updates the next transfer date for this gain.
	 * @param dayIndex - the day to update this gain to.
	 */
	public void updateNextDatesOnly(int dayIndex)
	{
		PredictDataMoneyTransfer pdmt = this.getPredictData(dayIndex);
		this.nextTransfer = pdmt.nextTransfer();
	}
}
