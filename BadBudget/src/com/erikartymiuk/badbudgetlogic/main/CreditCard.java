package com.erikartymiuk.badbudgetlogic.main;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataMoneyOwed;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.TransactionHistoryItem;

/** A type of MoneyOwed. Represents credit cards which have the additional property of being able
 * to be a source for BudgetItems and moneyLosses. Currently, however, cannot be used to pay loans.
 */
public class CreditCard extends MoneyOwed implements Source
{
	public static final int GOAL_LIMIT_YEARS = 150;
	
	/** Constructor for the credit card class.
	 * 
	 * @param name - String descriptor of this credit card
	 * @param debt - Current negative balance on this credit card
	 * @param quicklook - indicates if this credit card should be considered for quicklook
	 * @param interestRate - the interest rate for this credit card. compounds daily.
	 * @throws BadBudgetInvalidValueException 
	 */
	public CreditCard(String name, double debt, boolean quicklook, double interestRate) throws BadBudgetInvalidValueException
	{
		super(name, debt, quicklook, interestRate);
	}

	/** Source interface implementation for credit cards. This will increase the debt on the credit card
	 * by the loss amount. 
	 * This is the predict loss implementation for use
	 * with the prediction algorithm. It processes the loss for this source by updating its
	 * prediction rows appropriately. This method also handles an add back to the source.
	 * 
	 * @param destinationDescription - a string description of what the funds are being used for. Used for the transaction history
	 * @param lossAmount - the amount of funds needed
	 * @param addBack - true if the destination is a budget item and that budget item has funds to add back to this credit card (reduces debt)
	 * @param addBackAmount - the amount to add back to the source (if applicable)
	 * @param dayIndex - the date as an index or offset from prediction start date
	 * 
	 */
	public void predictLossForDayIndex(String destinationDescription, double lossAmount, boolean addBack, double addBackAmount, int dayIndex) 
	{	
		PredictDataMoneyOwed pdmo = this.getPredictData(dayIndex);
		double originalCreditCardValue = pdmo.value();
		
		if (addBack && !(addBackAmount == 0))
		{
			String addBackSource = destinationDescription;
			
			pdmo.updateValue(pdmo.value() - addBackAmount);
			
			TransactionHistoryItem addBackHistoryItem = new TransactionHistoryItem(pdmo.date(), addBackAmount, TransactionHistoryItem.BUDGET_ITEM_ADD_BACK_ACTION, 
					addBackSource, -1, -1, TransactionHistoryItem.DEFAULT_DESTINATION_ACTION, this.name(), originalCreditCardValue, pdmo.value(), 
					false, true);
			pdmo.addHistoryItem(addBackHistoryItem);
			originalCreditCardValue = pdmo.value();
		}
		
		pdmo.updateValue(pdmo.value() + lossAmount);
		
		//Construct the transaction record for the source account and the debt account
		TransactionHistoryItem historyItemAccount = new TransactionHistoryItem(pdmo.date(), lossAmount, TransactionHistoryItem.CREDIT_CARD_SOURCE_ACTION,
				this.name(), originalCreditCardValue, pdmo.value(), TransactionHistoryItem.DEFAULT_DESTINATION_ACTION, 
				destinationDescription, -1, -1, 
				true, false);
		pdmo.addHistoryItem(historyItemAccount);
	}
	
	/**
	 * After running the prediction algorithm this method updates this credit card's values to 
	 * the value's it would have on the day represented by day index. If a goal is set
	 * it is assumed to be invalid (TODO verify/correct the last sentence, 4/7/2017)
	 * 
	 * @param predictEndDate - the date our prediction algorithm ends. Any interest calculations treat this + 1 as the start date.
	 * 						(note uses GOAL_LIMIT_YEARS as the limit for the goal date calculation)
	 * @param dayIndex - the day, as an offset from the start date used in the prediction algorithm, to update
	 * 						this credit cards values to.
	 */
	public void update(Date predictEndDate, int dayIndex)
	{
		PredictDataMoneyOwed pdmo = this.getPredictData(dayIndex);
		double uAmount = pdmo.value();
		this.changeAmount(uAmount);
		
		if (this.payment() != null) {
			Date uNextPaymentDate = pdmo.getNextPaymentDate();
			this.payment().setNextPaymentDate(uNextPaymentDate);

			if (this.payment().goalDate() != null) {
				Double currentAmount = this.amount();
				Double interestRate = this.interestRate();
				Double paymentAmount = this.payment().amount();

				// Go one past the prediction algorithms end date to start
				// interest accumulation
				Calendar todayCal = Calendar.getInstance();
				todayCal.setTime(predictEndDate);
				todayCal.add(Calendar.DAY_OF_YEAR, 1);
				Date interestStart = todayCal.getTime();

				int daysBeforeFirstPayment = Prediction.numDaysBetween(interestStart, this.payment().nextPaymentDate());

				// Calculate a limit date
				Calendar maxCal = new GregorianCalendar(todayCal.get(Calendar.YEAR) + GOAL_LIMIT_YEARS,
						todayCal.get(Calendar.MONTH), todayCal.get(Calendar.DAY_OF_MONTH));
				Date limitDate = maxCal.getTime();

				Date correctGoalDate = Prediction.findGoalDateCompoundInterest(this.payment().nextPaymentDate(),
						daysBeforeFirstPayment, paymentAmount, this.payment().frequency(), currentAmount, interestRate,
						limitDate);

				if (!Prediction.datesEqualUpToDay(this.payment().goalDate(), correctGoalDate)) {
					this.payment().setGoalDate(null);
				}
			}
			
		}
		
	}
}
