package com.erikartymiuk.badbudgetlogic.main;
import java.text.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.swing.plaf.synth.SynthSeparatorUI;

import com.erikartymiuk.badbudgetlogic.budget.*;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.*;

/** 
 * Main class used for the predict feature of BadBudget. This class is a static class
 * which primary method is the "predict" method which given the users data, current date,
 * and a target date will populate the user objects predict data so that all days
 * between current and target are populated with correct values.
 */
public class Prediction {
	
	public static final double NUM_DAYS_IN_YEAR = 365.25;
	
	/**
	 * Main method for the prediction class. Static method that will populate all the user objects
	 * with correct values for days between currentDate and targetDate. A new call to predict using
	 * a previously used budget data object refreshes the predict data lists of the corresponding
	 * bad budget data objects. To continue a previous call without recalculating all previous dates
	 * call the predictContinue method. This method should not be called if the prediction data is
	 * planning to be used to update the base bbd objects. (call update instead).
	 * 
	 * The order objects are handled on each day is defined as:
	 * Contributions
	 * Gains
	 * Debts
	 * Losses
	 * Budget
	 * 
	 * @param currentUserValues - All of the user's current accounts to be used and have their
	 * 								predict data populated
	 * @param currentDate - The day the user's data is on (should be the current day)
	 * @param targetDate - Any day past currentDate that the user would like to know what their
	 * 						accounts value is on that day.
	 */
	public static void predict(BadBudgetData currentUserValues, Date currentDate, Date targetDate)
	{	
		predict(currentUserValues, currentDate, targetDate, false);
	}
	
	/**
	 * Private helper method that allows running the prediction algorithm with or without the plan of
	 * using the resulting data to update the base bad budget data objects. 
	 * @param currentUserValues - All of the user's current accounts to be used and have their
	 * 								predict data populated
	 * @param currentDate - The day the user's data is on (should be the current day)
	 * @param targetDate - Any day past currentDate that the user would like to know what their
	 * 						accounts value is on that day.
	 * @param considerBudgetRemainValues - set this if the prediction data will be used to update the base bbd objects. This
	 * 					has the algorithm take into account the remain action of the budget items, whereas
	 * 					if set to false the algorithm assumes that the full budget item amount will be lost
	 * 					from the source (not added back or accumulated).
	 */
	private static void predict(BadBudgetData currentUserValues, Date currentDate, Date targetDate, boolean considerBudgetRemainValues)
	{
		//Loop through each day starting with the currentDate up to the targetDate
		int numDays = numDaysBetween(currentDate, targetDate);
		for (int dayIndex = 0; dayIndex <= numDays; dayIndex++)
		{
			//First initialize all rows for this day index
			initializePredictRowsForDayIndex(currentUserValues, dayIndex, currentDate);
			//After the row data is initialized, we can check for any contributions that occur on this day
			handleContributionsForDayIndex(currentUserValues.getAccounts(), dayIndex, currentDate);
			//Next we check for any moneyGains that occur on this day
			handleGainsForDayIndex(currentUserValues.getGains(), dayIndex, currentDate);
			//Next is any payments being applied to moneyOwed objects
			handleDebtsPaymentsForDayIndex(currentUserValues.getDebts(), dayIndex, currentDate);
			//Handle any losses that occur
			handleLossesForDayIndex(currentUserValues.getLosses(), dayIndex, currentDate);
			//Handle the budget items
			handleBudgetForDayIndex(currentUserValues.getBudget(), dayIndex, currentDate, considerBudgetRemainValues);
			//Handle debts interest calculations
			handleDebtsInterestForDayIndex(currentUserValues.getDebts(), dayIndex, currentDate);
			//Handle savings interest accumulation
			handleSavingsInterestForDayIndex(currentUserValues.getAccounts(), dayIndex, currentDate);
		}
	}
	
	/**
	 * An additional prediction method used to continue a previous predict (or predict continue) call without recalculating all the computation
	 * already done. Given the original bounds and a new target this predict picks up where a previous call to predict or
	 * predictContinue left off, populating the prediction rows for all the bbd objects up to and including the newTarget date.
	 * Note this should not be called for updating the base bbd objects, rather call a single update.
	 * 
	 * @param currentUserValues - the bad budget data that has already been populated by a previous call to predict or predict continue
	 * @param originalStart - the original start date passed to the initial predict call.
	 * @param lastTarget - the date we have prediction data up to already from previous calls to predict and/or predict continue
	 * @param newTarget - the date we would like to continue the prediction up to now.
	 */
	public static void predictContinue(BadBudgetData currentUserValues, Date originalStart, Date lastTarget, Date newTarget)
	{
		//Figure out the day index we left off on, and the dayIndex we should now go to
		//Then proceed identically as before using the new dayIndex and bounds
		int previousDayIndex = numDaysBetween(originalStart, lastTarget);
		int newDayIndex = numDaysBetween(originalStart, newTarget);
		for (int dayIndex = previousDayIndex+1; dayIndex <= newDayIndex; dayIndex++)
		{
			//First initialize all rows for this day index
			initializePredictRowsForDayIndex(currentUserValues, dayIndex, originalStart);
			//After the row data is initialized, we can check for any contributions that occur on this day
			handleContributionsForDayIndex(currentUserValues.getAccounts(), dayIndex, originalStart);
			//Next we check for any moneyGains that occur on this day
			handleGainsForDayIndex(currentUserValues.getGains(), dayIndex, originalStart);
			//Next is any payments being applied to moneyOwed objects
			handleDebtsPaymentsForDayIndex(currentUserValues.getDebts(), dayIndex, originalStart);
			//Handle any losses that occur
			handleLossesForDayIndex(currentUserValues.getLosses(), dayIndex, originalStart);
			//Handle the budget items
			handleBudgetForDayIndex(currentUserValues.getBudget(), dayIndex, originalStart, false);
			//Handle debts interest calculations
			handleDebtsInterestForDayIndex(currentUserValues.getDebts(), dayIndex, originalStart);
			//Handle savings interest accumulation
			handleSavingsInterestForDayIndex(currentUserValues.getAccounts(), dayIndex, originalStart);
		}
	}
	
	/**
	 * Private helper method. Checks each budgetItem in the user's budget and handles any losses that
	 * occur on the day specified by dayIndex. Takes into account proration and if set then
	 * also considers the remain action including add back and accumulation.
	 * A budget should only be drawing from one source. 
	 * 
	 * @param userBudget - the user's budget
	 * @param dayIndex - an index (or offset) specifying the date we are interested in
	 * @param startDate - the date the prediction algorithm started
	 * @param considerBudgetRemainValues - Specifies if the algorithm should take into account a budget item's remain
	 * 					amount action. If so it considers addbacks and accumulation. If false then
	 * 					it is assumed all remaining amount of the budget item is fully lost.
	 */
	private static void handleBudgetForDayIndex(Budget userBudget, int dayIndex, Date startDate, boolean considerBudgetRemainValues)
	{
		for (BudgetItem currItem : userBudget.getAllBudgetItems().values())
		{
			PredictDataBudgetItem pdbi = currItem.getPredictData(dayIndex);
			Date currentLoss = pdbi.nextLoss();
			Date rowDate = pdbi.date();
			
			//Check to see if we've gone past the end date.
			boolean endDatePast = false;
			if (currItem.endDate() != null)
			{
				endDatePast = numDaysBetween(rowDate, currItem.endDate()) < 0;
			}
			
			if (!endDatePast && currentLoss != null && datesEqualUpToDay(currentLoss, rowDate)) 
			{
				//The loss occurs on this date
				//Need to remove/add the amount of the loss from source
				Source lossSource = currItem.source();
				
				double lossAmount = -1.0;
				
				if (currItem.isProratedStart())
				{
					lossAmount = userBudget.lossAmount(currItem, currentLoss);
				}
				else
				{
					lossAmount = currItem.lossAmount();
				}
				
				//If we are not updating or auto reset is off, we assume all of the budget items value
				//is spent before reset. (i.e. the value remain amount essentially disappears)
				if (!considerBudgetRemainValues)
				{
					lossSource.predictLossForDayIndex(currItem.expenseDescription(), lossAmount, false, -1, dayIndex);
					pdbi.setUpdatedAmount(lossAmount);
					
				}
				//If we are running this prediction with the intent of updating and auto reset is on, 
				//then we need to consider the set RemainAmountAction for each budget item
				else
				{
					//Net 0 (lossSource loses lossAmount, budgetItem gains loss amount)
					if (currItem.remainAmountAction() == RemainAmountAction.accumulates)
					{
						lossSource.predictLossForDayIndex(currItem.expenseDescription(), lossAmount, false, -1, dayIndex);
						pdbi.setUpdatedAmount(pdbi.getOriginalAmount() + lossAmount);
					}
					//Net negative pdbi.getOriginalAmount() (loss source loses loss amount, budget item loses original amount but gains loss amount)
					else if (currItem.remainAmountAction() == RemainAmountAction.disappear)
					{
						lossSource.predictLossForDayIndex(currItem.expenseDescription(), lossAmount, false, -1, dayIndex);
						pdbi.setUpdatedAmount(lossAmount);
					}
					//Net 0 (source gains pdbi original amount, loses loss amount, budget item loses original amount and gains loss amount)
					else if (currItem.remainAmountAction() == RemainAmountAction.addBack)
					{
						lossSource.predictLossForDayIndex(currItem.expenseDescription(), lossAmount, true, pdbi.getOriginalAmount(), dayIndex);
						pdbi.setUpdatedAmount(lossAmount);
					}
					else
					{
						//Default to accumulation
						lossSource.predictLossForDayIndex(currItem.expenseDescription(), lossAmount, false, -1,dayIndex);
						pdbi.setUpdatedAmount(pdbi.getOriginalAmount() + lossAmount);
					}
				}
							
				Date updatedNextLoss = userBudget.calculateNextLoss(currItem, currentLoss);
				
				pdbi.updateNextLoss(updatedNextLoss);
				pdbi.setLossAmountToday(lossAmount);
			}
		}
	}
	
	/**
	 * Private helper method. Checks for any losses to see if they occur on the date specified by dayIndex. If
	 * one is found that does it is handled. (i.e. the predict rows are updated to remove the loss from its source and the next loss
	 * date is updated).
	 * 
	 * @param losses - the list of the users MoneyLoss objects
	 * @param dayIndex - the date (as an index or offset from the start date) that we are interested in
	 * @param startDate - the start date of the prediction algorithm
	 */
	private static void handleLossesForDayIndex(ArrayList<MoneyLoss> losses, int dayIndex, Date startDate)
	{
		for (MoneyLoss currLoss : losses)
		{
			PredictDataMoneyLoss pdml = currLoss.getPredictData(dayIndex);
			Date nextLoss = pdml.nextLoss();
			Date rowDate = pdml.date();
			
			//Check to see if we've gone past the end date.
			boolean endDatePast = false;
			if (currLoss.endDate() != null)
			{
				endDatePast = numDaysBetween(rowDate, currLoss.endDate()) < 0;
			}
			
			if (!endDatePast && nextLoss != null && datesEqualUpToDay(nextLoss, rowDate))
			{
				//The loss occurs on this date
				//Need to remove/add the amount of the loss from source
				Source lossSource = currLoss.source();
				lossSource.predictLossForDayIndex(currLoss.expenseDescription(), currLoss.lossAmount(), false, -1, dayIndex);
				
				Date nextLossUpdate = currLoss.calculateNextLoss(nextLoss);
				pdml.updateNextLoss(nextLossUpdate);				
			}
		}
	}
	
	/**
	 * Checks any savings accounts and handles any interest accumulation that occurs on the day specified by day index.
	 * Interest is only considered if the interest rate for a particular savings account is not zero. Interest is handled
	 * once a month.
	 * @param accounts - the accounts to consider for interest accumulation
	 * @param dayIndex - the day index to look at
	 * @param startDate - the start of the prediction algorithm
	 */
	private static void handleSavingsInterestForDayIndex(ArrayList<Account> accounts, int dayIndex, Date startDate)
	{
		for (Account currAccount : accounts)
		{
			if (currAccount instanceof SavingsAccount)
			{
				SavingsAccount currSavingsAccount = (SavingsAccount)currAccount;
				if (currSavingsAccount.getInterestRate() != 0)
				{
					PredictDataSavingsAccount pdsa = currSavingsAccount.getPredictData(dayIndex);
					Date rowDate = pdsa.date();
					Date nextInterestDate = pdsa.getNextInterestAccumulationDate();
					if (Prediction.datesEqualUpToDay(rowDate, nextInterestDate))
					{
						pdsa.updateValue(pdsa.value() + pdsa.value() * currSavingsAccount.getInterestRate()/12.0);
						Calendar cal = Calendar.getInstance();
						cal.setTime(nextInterestDate);
						cal.add(Calendar.MONTH, 1);
						pdsa.setNextInterestAccumulationDate(cal.getTime());
					}
				}
			}
		}
	}
	
	/**
	 * Loops through all debts and handles any interest accumulation on the day specified by day index.
	 * @param debts - the debts to handle
	 * @param dayIndex - the day index to check for interest accumulation
	 * @param startDate - the start date of the prediction being run
	 */
	private static void handleDebtsInterestForDayIndex(ArrayList<MoneyOwed> debts, int dayIndex, Date startDate)
	{
		for (MoneyOwed currDebt : debts)
		{
			if (currDebt.interestRate() != 0)
			{
				PredictDataMoneyOwed predictDataAll = currDebt.getPredictData(dayIndex);
				Date rowDate = predictDataAll.date();
				Date nextInterestDate = predictDataAll.getNextInterestAccumulationDate();
				if (Prediction.datesEqualUpToDay(rowDate, nextInterestDate))
				{
					if (currDebt instanceof CreditCard)
					{
						CreditCard currCreditCard = (CreditCard) currDebt;						
						predictDataAll.updateValue(predictDataAll.value() * (1+currCreditCard.interestRate()/NUM_DAYS_IN_YEAR));
					}
					else if (currDebt instanceof Loan)
					{
						Loan currLoan = (Loan) currDebt;
						PredictDataLoan pdl = (PredictDataLoan)predictDataAll;
	
						if (currLoan.isSimpleInterest())
						{
							double simpleInterest = pdl.getPrincipal() * (currLoan.interestRate()/NUM_DAYS_IN_YEAR);
							
							pdl.updateValue(pdl.value() + simpleInterest);
							pdl.setInterest(pdl.getInterest()+simpleInterest);
						}
						else
						{	
							double compoundedInterest = pdl.value() * currLoan.interestRate()/NUM_DAYS_IN_YEAR;
							
							pdl.updateValue(pdl.value() + compoundedInterest);
							pdl.setPrincipal(pdl.value());
							pdl.setInterest(0);
						}
					}
					else
					{
						PredictDataMoneyOwed pdmo = currDebt.getPredictData(dayIndex);
						pdmo.updateValue(currDebt.amount() * (1+currDebt.interestRate()/NUM_DAYS_IN_YEAR));
					}
					
					Calendar updateNextInterestCal = Calendar.getInstance();
					updateNextInterestCal.setTime(nextInterestDate);
					updateNextInterestCal.add(Calendar.DAY_OF_YEAR, 1);
					predictDataAll.setNextInterestAccumulationDate(updateNextInterestCal.getTime());
				}
			}
		}
	}
	
	/**
	 * Private helper method. For a particular day, specified as an index from the startDate, this method
	 * check if any payments for debts occur on this day and handles them if they do
	 * 
	 * @param debts - the list of the users debts to check 
	 * @param dayIndex - the date to check for any debt payments (as an index)
	 * @param startDate - the date the prediction start (typically the current day)
	 * 
	 */
	private static void handleDebtsPaymentsForDayIndex(ArrayList<MoneyOwed> debts, int dayIndex, Date startDate)
	{
		for (MoneyOwed currDebt : debts)
		{
			//Only have to take action if this debt has a payment attached to it
			if (currDebt.payment() != null)
			{	
				//If there is an active payment, check to see if a payment occurs on the current date
				PredictDataMoneyOwed pdmo = currDebt.getPredictData(dayIndex);
				Date nextPayment = pdmo.getNextPaymentDate();
				Date rowDate = pdmo.date();
				
				//Make sure we are not past the end date - 1/11/2017
				boolean endDatePast = false;
				if (currDebt.payment().endDate() != null)
				{
					endDatePast = numDaysBetween(rowDate, currDebt.payment().endDate()) < 0;
				}
				
				if (!endDatePast && nextPayment != null && datesEqualUpToDay(nextPayment, rowDate))
				{
					//A payment should occur on this date
					Payment payment = currDebt.payment();
					
					//Typical Case
					//Remove amount from source account and lower debt by the same amount
					double newDebtAmount = pdmo.value() - payment.amount();
					double paymentMade = payment.amount();				
					
					//check if we should payoff entire balance or...
					//check if lowering debt by amount results in negative balance
					//In either case reduce debt to zero and the payment is the value of the debt
					if (payment.payOff() || newDebtAmount < 0)
					{
						newDebtAmount = 0;
						paymentMade = pdmo.value();
					}
					
					//Handle payments for loans where part is applied to interest and part to principal for simple interest loans
					if (currDebt.interestRate() != 0)
					{
						if (currDebt instanceof Loan)
						{
							Loan currLoan = (Loan) currDebt;
							
							if (currLoan.isSimpleInterest())
							{
								PredictDataLoan pdl = currLoan.getPredictData(dayIndex);
								
								if (newDebtAmount == 0)
								{
									pdl.setPrincipal(0);
									pdl.setInterest(0);
								}
								else
								{
									if (pdl.getInterest() > paymentMade)
									{
										pdl.setInterest(pdl.getInterest()-paymentMade);
									}
									else
									{
										double paymentToPrincipal = paymentMade - pdl.getInterest();
										pdl.setInterest(0);
										if (paymentToPrincipal > pdl.getPrincipal())
										{
											pdl.setPrincipal(0);
										}
										else
										{
											pdl.setPrincipal(pdl.getPrincipal()-paymentToPrincipal);
										}
									}
								}
							}
						}
					}	
					//End added payment handling for simple daily interest loan
					
					Account sourceAccount = payment.sourceAccount();
					PredictDataAccount pda = sourceAccount.getPredictData(dayIndex);
					
					double originalSourceValue = pda.value();
					double originalDestinationValue = pdmo.value();
					
					pda.updateValue(pda.value() - paymentMade);
					pdmo.updateValue(newDebtAmount);
										
					Date newPaymentDate = payment.determineNextPayment(pdmo.getNextPaymentDate());
					pdmo.updateNextPaymentDate(newPaymentDate);
					
					//Construct the transaction record for the source account and the debt account
					TransactionHistoryItem historyItem = new TransactionHistoryItem(rowDate, paymentMade, TransactionHistoryItem.ACCOUNT_SOURCE_ACTION, sourceAccount.name(), originalSourceValue, pda.value(), 
							TransactionHistoryItem.DEBT_DESTINATION_ACTION, currDebt.name(), originalDestinationValue, pdmo.value(), 
							true, true);
					pda.addHistoryItem(historyItem);
					pdmo.addHistoryItem(historyItem);
				}
			}
		}
	}
	
	/** Private helper method. For a particular day, check for any money gains that occur and handle them
	 * 
	 * @param gains - a list of the gains to consider
	 * @param dayIndex - the day to consider given as an index (offset from the startDate)
	 * @param startDate - the day the prediction started (typically the current day)
	 * 
	 */
	private static void handleGainsForDayIndex(ArrayList<MoneyGain> gains, int dayIndex, Date startDate)
	{
		for (MoneyGain mg : gains)
		{				
			PredictDataMoneyGain pdmg = mg.getPredictData(dayIndex);
			Date nextDeposit = pdmg.nextDeposit();
			Date rowDate = pdmg.date();
			Date endDate = mg.endDate();
			
			//Check to see if we've hit the end date if it is set
			boolean endDateHit = (endDate != null && numDaysBetween(rowDate, endDate) < 0);		
			
			if (!endDateHit && nextDeposit != null && datesEqualUpToDay(nextDeposit, rowDate))
			{
				//A money gain happens on this day.
				PredictDataAccount pda = mg.destinationAccount().getPredictData(dayIndex);
				
				double originalDestinationValue = pda.value();
				
				pda.updateValue(pda.value() + mg.gainAmount());
				pdmg.updateNextDeposit(mg.calculateNextDeposit(pdmg.nextDeposit()));
				
				//Construct the transaction record for the source account and the debt account
				TransactionHistoryItem historyItem = new TransactionHistoryItem(rowDate, mg.gainAmount(), TransactionHistoryItem.DEFAULT_SOURCE_ACTION, mg.sourceDescription(), -1, -1, 
						TransactionHistoryItem.ACCOUNT_DESTINATION_ACTION, mg.destinationAccount().name(), originalDestinationValue, pda.value(), 
						false, true);
				pda.addHistoryItem(historyItem);
			}
		}
	}

	/**	Private Helper method. For a particular day, check for any contributions on that day and handle any that occur.
	 * 
	 * @param accounts - a list of the user's cash account
	 * @param dayIndex - the int specifying the day index (days past the startDate)
	 * @param startDate - the day the prediction started (should be the current day)
	 * 
	 */
	private static void handleContributionsForDayIndex(ArrayList<Account> accounts, int dayIndex, Date startDate)
	{
		for (Account a : accounts)
		{
			//Only need to consider SavingsAccounts
			if (a instanceof SavingsAccount)
			{
				SavingsAccount sa = (SavingsAccount) a;
				PredictDataSavingsAccount pdsa = sa.getPredictData(dayIndex);
				Date nextContribution = pdsa.getNextContributionDate();
				Date rowDate = pdsa.date();
				
				//Check to see if we've gone past the end date if it is set (not ongoing)
				Date endDate = sa.endDate();
				boolean endDateHit = (endDate != null && numDaysBetween(rowDate, endDate) < 0);		

				if (!endDateHit && nextContribution != null && datesEqualUpToDay(nextContribution, rowDate))
				{
					//Need to handle this contribution
					Contribution contribution = sa.contribution();
					PredictDataAccount pda = sa.sourceAccount().getPredictData(dayIndex);
					
					//update the values (remove from source account and add to savings account)
					double originalSourceValue = pda.value();
					double originalDestinationValue = pdsa.value();
					
					pdsa.updateValue(pdsa.value()+contribution.getContribution());
					pda.updateValue(pda.value()-contribution.getContribution());
					
					//update the nextContribution for the PredictSavingsAccountRow
					pdsa.updateContributionDate(contribution.nextContributionDate(nextContribution));
					
					//Construct the transaction record for the source account and the debt account
					TransactionHistoryItem historyItem = new TransactionHistoryItem(rowDate, contribution.getContribution(), 
							TransactionHistoryItem.ACCOUNT_SOURCE_ACTION, sa.sourceAccount().name(), originalSourceValue, pda.value(), 
							TransactionHistoryItem.SAVINGS_ACCOUNT_DESTINATION_ACTION, sa.name(), originalDestinationValue, pdsa.value(), 
							true, true);
					pda.addHistoryItem(historyItem);
					pdsa.addHistoryItem(historyItem);
				}
			}
		}
	}
		
	
	/** Private Helper Method. Initializes all the predict rows (on a given day) needed to perform the prediction algorithm.
	 * Previous days rows should have already been handled so that the next day's rows can be initialized correctly. Unless
	 * it is the first day.
	 * 
	 * @param data - the user's data
	 * @param dayIndex - index specifying which day we are currently on (days from starting Date)
	 * @param startingDate - the date the prediction was kicked off on
	 * 
	 */
	private static void initializePredictRowsForDayIndex(BadBudgetData data, int dayIndex, Date startingDate)
	{
		//Get a hold of the relevant user accounts
		ArrayList<Account> accounts = data.getAccounts();
		ArrayList<MoneyGain> gains = data.getGains();
		ArrayList<MoneyOwed> debts = data.getDebts();
		ArrayList<MoneyLoss> losses = data.getLosses();
				
		Budget budget = data.getBudget();
		
		//initialize the predict data rows
		for (Account a : accounts)
		{
			//Use the previous day's data (or the accounts data for 0 index) to initialize the new row
			if (a instanceof SavingsAccount)
			{
				SavingsAccount sa = (SavingsAccount) a;
				if (dayIndex == 0)
				{
					//Savings accounts can have interest that accumulates. This sets the first interest accumulation date to be
					//the 1st of the month of the next month after the start date of a prediction. (Thus a start date of Feb 1 and Feb 12 
					//results in interest accumulation first occurring on March 1. 
					Calendar firstOfNextMonth = Calendar.getInstance();
					firstOfNextMonth.setTime(startingDate);
					firstOfNextMonth.set(Calendar.DAY_OF_MONTH, 1);
					firstOfNextMonth.add(Calendar.MONTH, 1);
										
					PredictDataSavingsAccount firstRow = new PredictDataSavingsAccount(startingDate, 
																						sa.value(), sa.nextContribution(), firstOfNextMonth.getTime());
					sa.setPredictData(dayIndex, firstRow);
					//sa.addPredictData(firstRow);
				}
				else
				{
					PredictDataSavingsAccount yesterdayRow = sa.getPredictData(dayIndex - 1);
					PredictDataSavingsAccount newRow = new PredictDataSavingsAccount(addDays(yesterdayRow.date(), 1), yesterdayRow.value(), 
																					yesterdayRow.getNextContributionDate(), yesterdayRow.getNextInterestAccumulationDate());
					sa.setPredictData(dayIndex, newRow);
					//sa.addPredictData(newRow);
				}
			}
			else
			{
				if (dayIndex == 0)
				{
					PredictDataAccount firstRow = new PredictDataAccount(startingDate, a.value());
					a.setPredictData(dayIndex, firstRow);
					//a.addPredictData(firstRow);
				}
				else
				{
					PredictDataAccount yesterdayRow = a.getPredictData(dayIndex - 1);
					
					PredictDataAccount newRow = new PredictDataAccount(addDays(yesterdayRow.date(), 1), yesterdayRow.value());
					a.setPredictData(dayIndex, newRow);
					//a.addPredictData(newRow);
				}
			}
		}
		
		//Initialize the money gains next
		for (MoneyGain mg : gains)
		{
			if (dayIndex == 0)
			{
				PredictDataMoneyGain pdmg = new PredictDataMoneyGain(startingDate, mg.nextDeposit());
				//mg.addPredictData(pdmg);
				mg.setPredictData(dayIndex, pdmg);
			}
			else
			{
				PredictDataMoneyGain yesterdayRow = mg.getPredictData(dayIndex - 1);
				PredictDataMoneyGain newRow = new PredictDataMoneyGain(addDays(yesterdayRow.date(), 1), yesterdayRow.nextDeposit());
				
				//mg.addPredictData(newRow);
				mg.setPredictData(dayIndex, newRow);
			}
		}
		
		//Initialize the money owed (debt) objects next
		for (MoneyOwed currDebt : debts)
		{	
			if (!(currDebt instanceof Loan))
			{
				if (dayIndex == 0)
				{
					PredictDataMoneyOwed pdmo = null;
					
					//Interest accumulation for debts is initialized to the day after the starting date,
					//then should occur daily after that
					Calendar tmrwCal = Calendar.getInstance();
					tmrwCal.setTime(startingDate);
					tmrwCal.add(Calendar.DAY_OF_YEAR, 1);
					
					if (currDebt.payment() == null)
					{
						pdmo = new PredictDataMoneyOwed(startingDate, currDebt.amount(), null, tmrwCal.getTime());
					}
					else
					{
						pdmo = new PredictDataMoneyOwed(startingDate, currDebt.amount(), currDebt.payment().nextPaymentDate(), tmrwCal.getTime());
					}
					
					//currDebt.addPredictData(pdmo);
					currDebt.setPredictData(dayIndex, pdmo);
				}
				else
				{
					PredictDataMoneyOwed yesterdayRow = currDebt.getPredictData(dayIndex - 1);
					PredictDataMoneyOwed newRow = new PredictDataMoneyOwed(addDays(yesterdayRow.date(), 1), yesterdayRow.value(), 
							yesterdayRow.getNextPaymentDate(), yesterdayRow.getNextInterestAccumulationDate());
					//currDebt.addPredictData(newRow);
					currDebt.setPredictData(dayIndex, newRow);
				}
			}
			else
			{
				Loan currLoan = (Loan) currDebt;
				if (dayIndex == 0)
				{
					PredictDataLoan pdl = null;
					
					//Interest accumulation for debts is initialized to the day after the starting date,
					//then should occur daily after that
					Calendar tmrwCal = Calendar.getInstance();
					tmrwCal.setTime(startingDate);
					tmrwCal.add(Calendar.DAY_OF_YEAR, 1);
					
					Date tempNextPayment = null;
					if (currLoan.payment() != null)
					{
						tempNextPayment = currLoan.payment().nextPaymentDate();
					}
					
					if (currLoan.interestRate() != 0 && currLoan.isSimpleInterest())
					{
						pdl = new PredictDataLoan(startingDate, currLoan.amount(), tempNextPayment, tmrwCal.getTime(), 
								currLoan.getPrincipalBalance(), currLoan.getInterestAmount());
					}
					else
					{
						pdl = new PredictDataLoan(startingDate, currLoan.amount(), tempNextPayment, tmrwCal.getTime(),
								currLoan.amount(), 0);
					}
					currLoan.setPredictData(dayIndex, pdl);
				}
				else
				{
					PredictDataLoan yesterdayRow = currLoan.getPredictData(dayIndex - 1);
					PredictDataLoan newRow = new PredictDataLoan(addDays(yesterdayRow.date(), 1), 
							yesterdayRow.value(), yesterdayRow.getNextPaymentDate(), yesterdayRow.getNextInterestAccumulationDate(), 
							yesterdayRow.getPrincipal(), yesterdayRow.getInterest());
					currLoan.setPredictData(dayIndex, newRow);
				}
			}
			
		}
		
		//Initialize the money loss objects next
		for (MoneyLoss currLoss : losses)
		{
			PredictDataMoneyLoss pdml = null;
			if (dayIndex == 0)
			{
				pdml = new PredictDataMoneyLoss(startingDate, currLoss.nextLoss());
				//currLoss.addPredictData(pdml);
				currLoss.setPredictData(dayIndex, pdml);
			}
			else
			{
				PredictDataMoneyLoss yesterdayRow = currLoss.getPredictData(dayIndex - 1);
				PredictDataMoneyLoss newRow = new PredictDataMoneyLoss(addDays(yesterdayRow.date(), 1), yesterdayRow.nextLoss());
				//currLoss.addPredictData(newRow);
				currLoss.setPredictData(dayIndex, newRow);
			}
		}
		
		//Also initialize losses from all the budgetItems in the user's budget
		for (BudgetItem currItem : budget.getAllBudgetItems().values())
		{
			PredictDataBudgetItem pdbi = null;
			if (dayIndex == 0)
			{
				pdbi = new PredictDataBudgetItem(startingDate, currItem.nextLoss(), -1);
				pdbi.setOriginalAmount(currItem.getCurrAmount());
				pdbi.setUpdatedAmount(currItem.getCurrAmount());
				//currItem.addPredictData(pdml);
				currItem.setPredictData(dayIndex, pdbi);
			}
			else
			{
				PredictDataBudgetItem yesterdayRow = currItem.getPredictData(dayIndex - 1);
				PredictDataBudgetItem newRow = new PredictDataBudgetItem(addDays(yesterdayRow.date(), 1), yesterdayRow.nextLoss(), -1);
				newRow.setOriginalAmount(yesterdayRow.getUpdatedAmount());
				newRow.setUpdatedAmount(yesterdayRow.getUpdatedAmount());
				//currItem.addPredictData(newRow);
				currItem.setPredictData(dayIndex, newRow);
			}
		}
	}
	
	/** Adds the given number of days to the given day and returns the resulting Date
	 * 
	 * @param day - the starting date
	 * 
	 * @return - the number of days to add to the given date
	 */
	public static Date addDays(Date day, int numDays)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		cal.add(Calendar.DAY_OF_MONTH, numDays);
		return cal.getTime();
	}
	
	/** Method to compare two dates and see if they are the same day
	 * up to granularity of day. (i.e. hours, minutes, etc. doesn't matter).
	 * 
	 * @param date1 - one of the dates to compare
	 * @param date2 - the other date to compare
	 * 
	 * @return boolean - true if the dates are equal up to their days, false otherwise.
	 * 
	 */
	public static boolean datesEqualUpToDay(Date date1, Date date2)
	{
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		
		int year1 = cal1.get(Calendar.YEAR);
		int month1 = cal1.get(Calendar.MONTH);
		int day1 = cal1.get(Calendar.DAY_OF_MONTH);
		
		int year2 = cal2.get(Calendar.YEAR);
		int month2 = cal2.get(Calendar.MONTH);
		int day2 = cal2.get(Calendar.DAY_OF_MONTH);
		
		boolean equal = (year1 == year2 && month1 == month2 && day1 == day2);
		return equal;
	}
	
	/** Helper method used to convert an index in the prediction list to a date
	 * Needs to know the start date to work from. The date returned is truncated, i.e its
	 * granularity is only the day of the year.
	 * 
	 * @param startDate - the date to add the index to
	 * @param index - the index to convert to a date
	 * 
	 * @return - returns the Date that the index (using the startDate) refers to
	 */
	public static Date convertIndexToDate(Date startDate, int index)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.add(Calendar.DAY_OF_MONTH, index);
		GregorianCalendar gc = new GregorianCalendar(cal.get(Calendar.YEAR), 
									cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		return gc.getTime();
	}
	
	/** Helper method to find the number of days between two dates,
	 * after truncating the dates to be 00:00. Can be used to check if one
	 * date occurs before the other (up to the day) by checking for >,<, = to zero
	 * 
	 * @param day1 - the sooner date
	 * @param day2 - the later date
	 * 
	 * @return int - the number of days between the two dates (if days apart greater than int
	 * 					behavior undefined)
	 */
	public static int numDaysBetween(Date day1, Date day2)
	{
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(day1);
		
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(day2);
		
		Date truncatedDate1 = new GregorianCalendar(cal1.get(Calendar.YEAR), 
				cal1.get(Calendar.MONTH), cal1.get(Calendar.DAY_OF_MONTH)).getTime();
		
		Date truncatedDate2 = new GregorianCalendar(cal2.get(Calendar.YEAR), 
				cal2.get(Calendar.MONTH), cal2.get(Calendar.DAY_OF_MONTH)).getTime();
		
		long diff = truncatedDate2.getTime() - truncatedDate1.getTime();
		long dayDiff = Math.round(diff/(double)(1000*60*60*24));
		return (int) dayDiff;
	}
	
	/** Given the next contribution date, recurring contribution (amount and frequency that is not one time), goal amount, 
	 * and current amount this method calculates what end date would have us reaching the goal amount.
	 * 
	 * @param nextContribution - the date the next contribution occurs adding to the current amount
	 * @param contribution - the contribution to consider in reaching the goal (frequency must be recurring, undefined if not)
	 * @param currentAmount - the starting amount
	 * @param goalAmount - the goal amount
	 *  
	 * @return Date - the day we reach or overreach our goal amount
	 * 
	 */
	public static Date findGoalDate(Date nextContribution, Contribution contribution, double currentAmount, double goalAmount)
	{
		//Frequency should be recurring
		if (contribution.getFrequency().equals(Frequency.oneTime))
		{
			return null;
		}
		
		Date endDate = nextContribution;
		currentAmount+=contribution.getContribution();

		while (!(currentAmount >= goalAmount))
		{
			//Make a contribution
			currentAmount+=contribution.getContribution();
			endDate = contribution.nextContributionDate(endDate);
		}
		return endDate;
	}
	
	//TODO 3/12/2017
	/**
	 * Unimplemented
	 * @param startDate
	 * @param daysBeforeFirstPayment
	 * @param goalDate
	 * @param freq
	 * @param debtAmount
	 * @param interestRate
	 * @param principal
	 * @return
	 */
	public static double findPaymentAmountSimpleInterest(Date startDate, int daysBeforeFirstPayment, Date goalDate,
			Frequency freq, double debtAmount, double interestRate, double principal)
	{
		double interestRateConstant = (interestRate/NUM_DAYS_IN_YEAR);
		double initialInterest = principal * (interestRateConstant * daysBeforeFirstPayment);
		double interest = (debtAmount - principal) + initialInterest;
		
		Calendar currentPaymentCal = Calendar.getInstance();
		currentPaymentCal.setTime(startDate);
		
		ArrayList<Integer> numDaysBetweenPayments = new ArrayList<Integer>();
		
		while (Prediction.numDaysBetween(currentPaymentCal.getTime(), goalDate) >= 0)
		{
			Date lastPaymentDate = currentPaymentCal.getTime();
			switch (freq)
			{
				case daily:
				{
					currentPaymentCal.add(Calendar.DAY_OF_YEAR, 1);
					break;
				}
				case weekly:
				{
					currentPaymentCal.add(Calendar.WEEK_OF_YEAR, 1);
					break;
				}
				case biWeekly:
				{
					currentPaymentCal.add(Calendar.WEEK_OF_YEAR, 2);
					break;
				}
				case monthly:
				{
					currentPaymentCal.add(Calendar.MONTH, 1);
					break;
				}
				case yearly:
				{
					currentPaymentCal.add(Calendar.YEAR, 1);
					break;
				}
			}
			if (Prediction.numDaysBetween(currentPaymentCal.getTime(), goalDate) >= 0)
			{
				int tempDaysBtn = Prediction.numDaysBetween(lastPaymentDate, currentPaymentCal.getTime());
				numDaysBetweenPayments.add(tempDaysBtn);
			}
		}
	
		double[] principalSavedFactors = new double[numDaysBetweenPayments.size() + 1];
		for (int i = 0; i <= numDaysBetweenPayments.size(); i++)
		{
			if (i == 0)
			{
				principalSavedFactors[i] = 1;
			}
			else
			{
				principalSavedFactors[i] = principalSavedFactors[i-1] * (1+interestRateConstant*numDaysBetweenPayments.get(numDaysBetweenPayments.size() - i));
			}
		}
		
		double[] principalSavedSums = new double[numDaysBetweenPayments.size() + 1];
		for (int i = 0; i <= numDaysBetweenPayments.size(); i++)
		{
			if (i == 0)
			{
				principalSavedSums[i] = 1;
			}
			else
			{
				principalSavedSums[i] = principalSavedSums[i-1] + principalSavedFactors[i];
			}
		}
		
		double[] interestSavedSums = new double[numDaysBetweenPayments.size()];
		for (int i = 0; i < numDaysBetweenPayments.size(); i++)
		{
			if (i == 0)
			{
				interestSavedSums[i] = 0;
			}
			else
			{
				interestSavedSums[i] = interestSavedSums[i-1] + numDaysBetweenPayments.get(i);
			}
		}
		
		double smallestDiff = Double.MAX_VALUE;
		double bestInterestPayment = -1;
		double bestPrincipalPayment = -1;
		
		for (int currInterestPayments = 0; currInterestPayments <= numDaysBetweenPayments.size(); currInterestPayments++)
		{
			double interestPayment = Double.MAX_VALUE;
			double principalPayment = Double.MAX_VALUE;
			
			double principalTop = principal * principalSavedFactors[numDaysBetweenPayments.size() - currInterestPayments];
			double principalBottom = principalSavedSums[numDaysBetweenPayments.size() - currInterestPayments];
			
			principalPayment = principalTop/principalBottom;
			
			if (currInterestPayments == 0)
			{
				if (interest == 0)
				{
					interestPayment = 0;
				}
			}
			else
			{
				double interestTop = interest + principal*interestRateConstant*interestSavedSums[currInterestPayments - 1];
				double interestBottom = currInterestPayments;
				interestPayment = interestTop/interestBottom;
			}
			
			
			System.out.println("Num Interest Payment :" + currInterestPayments);
			System.out.println("Interest Payment: " + interestPayment);
			System.out.println("Principal Payment: " + principalPayment);

			double payDiff = Math.abs(interestPayment-principalPayment);
			if (payDiff < smallestDiff)
			{
				smallestDiff = payDiff;
				bestInterestPayment = interestPayment;
				bestPrincipalPayment = principalPayment;
			}
		}
		
		if (bestInterestPayment > bestPrincipalPayment)
		{
			return bestInterestPayment;
		}
		else
		{
			return bestPrincipalPayment;
		}
	}
	
	/**
	 * Finds the necessary payment amount to pay the debt down to zero by the goal date taking into account
	 * interest that compounds daily
	 * @param startDate - the date of the first payment
	 * @param daysBeforeFirstPayment - the days remaining prior to the first payment where interest can accumulate
	 * @param goalDate - the date we want our debt to be 0
	 * @param freq - the frequency of our payment
	 * @param debtAmount - the starting value of our debt
	 * @param interestRate - the interest rate of our debt
	 * @return - the payment amount required to pay this debt down to 0 by the goal date
	 */
	public static double findPaymentAmountCompoundInterest(Date startDate, int daysBeforeFirstPayment, Date goalDate,
			Frequency freq, double debtAmount, double interestRate)
	{
		double interestRateConstant = (1+interestRate/NUM_DAYS_IN_YEAR);
		
		Calendar currentPaymentCal = Calendar.getInstance();
		currentPaymentCal.setTime(startDate);
		
		ArrayList<Integer> numDaysBetweenPayments = new ArrayList<Integer>();
		int totalDaysBetween = 0;
		
		while (Prediction.numDaysBetween(currentPaymentCal.getTime(), goalDate) >= 0)
		{
			Date lastPaymentDate = currentPaymentCal.getTime();
			switch (freq)
			{
				case daily:
				{
					currentPaymentCal.add(Calendar.DAY_OF_YEAR, 1);
					break;
				}
				case weekly:
				{
					currentPaymentCal.add(Calendar.WEEK_OF_YEAR, 1);
					break;
				}
				case biWeekly:
				{
					currentPaymentCal.add(Calendar.WEEK_OF_YEAR, 2);
					break;
				}
				case monthly:
				{
					currentPaymentCal.add(Calendar.MONTH, 1);
					break;
				}
				case yearly:
				{
					currentPaymentCal.add(Calendar.YEAR, 1);
					break;
				}
			}
			if (Prediction.numDaysBetween(currentPaymentCal.getTime(), goalDate) >= 0)
			{
				int tempDaysBtn = Prediction.numDaysBetween(lastPaymentDate, currentPaymentCal.getTime());
				numDaysBetweenPayments.add(tempDaysBtn);
				totalDaysBetween += tempDaysBtn;
			}
		}
		
		double top = debtAmount * Math.pow(interestRateConstant, daysBeforeFirstPayment + totalDaysBetween);
		double bottom = 0;
		int runningDaysBetween = totalDaysBetween;
		for (int i = 0; i < numDaysBetweenPayments.size(); i++)
		{
			bottom+=Math.pow(interestRateConstant, runningDaysBetween);
			runningDaysBetween-=numDaysBetweenPayments.get(i);
		}
		bottom+=1;
		
		double paymentAmount = top/bottom;
		return paymentAmount;
	}
	
	/**
	 * Finds the needed regular contribution amount to hit a goal by the requested goal date taking into
	 * account the expected return rate.
	 * @param startDate - the date interest can start to accumulate on the savings. Will be the first of the next month
	 * 						when it begins
	 * @param nextContribution - the initial contribution date
	 * @param contributionFreq - the contribution frequency
	 * @param currentAmount - the current amount in the savings account
	 * @param goalAmount - the goal amount
	 * @param interestRate - the interest rate
	 * @param goalDate - the goal date we would like to hit
	 * @return the needed contribution amount to hit our goal amount by the goal date
	 */
	public static double findContributionAmount(Date startDate, Date nextContribution, Frequency contributionFreq, 
			double currentAmount, double goalAmount, double interestRate, Date goalDate)
	{
		//Figure out all the dates of contributions and interest accumulation
		ArrayList<Date> contributionDates = new ArrayList<Date>();
		ArrayList<Date> interestDates = new ArrayList<Date>();
		
		Calendar currentContributionCal = Calendar.getInstance();
		currentContributionCal.setTime(nextContribution);
		
		while (Prediction.numDaysBetween(currentContributionCal.getTime(), goalDate) >= 0)
		{
			contributionDates.add(currentContributionCal.getTime());
			switch (contributionFreq)
			{
				case daily:
				{
					currentContributionCal.add(Calendar.DAY_OF_YEAR, 1);
					break;
				}
				case weekly:
				{
					currentContributionCal.add(Calendar.WEEK_OF_YEAR, 1);
					break;
				}
				case biWeekly:
				{
					currentContributionCal.add(Calendar.WEEK_OF_YEAR, 2);
					break;
				}
				case monthly:
				{
					currentContributionCal.add(Calendar.MONTH, 1);
					break;
				}
				case yearly:
				{
					currentContributionCal.add(Calendar.YEAR, 1);
					break;
				}
			}
		}
		
		Calendar interestCal = Calendar.getInstance();
		interestCal.setTime(startDate);
		interestCal.add(Calendar.MONTH, 1);
		interestCal.set(Calendar.DAY_OF_MONTH, 1);
		
		while (Prediction.numDaysBetween(interestCal.getTime(), goalDate) >= 0)
		{
			interestDates.add(interestCal.getTime());
			interestCal.add(Calendar.MONTH, 1);
		}
		
		//Working forward figure out how many contributions are between each interest date.
		ArrayList<Integer> contributionsBetween = new ArrayList<Integer>();
		int currContributionNumber = 0;
		
		int contributionIndex = 0;
		int interestIndex = 0;
		
		while (interestIndex < interestDates.size())
		{
			int contributionToInterestDays = -1;
			if (contributionIndex < contributionDates.size())
			{
				Date contributionDate = contributionDates.get(contributionIndex);
				Date interestDate = interestDates.get(interestIndex);
				contributionToInterestDays = Prediction.numDaysBetween(contributionDate, interestDate);
			}
			
			//Contribution before (same day as) interest
			if (contributionToInterestDays >= 0)
			{
				currContributionNumber++;
				contributionIndex++;
			}
			//Contribution after interest date or no more contributions
			else
			{
				contributionsBetween.add(currContributionNumber);
				currContributionNumber = 0;
				interestIndex++;
			}
		}
		
		//add trailing contributions
		contributionsBetween.add(contributionDates.size() - contributionIndex);
		
		//Calculate the needed contribution amount
		double interestConstant = (1+interestRate/12.0);
		
		double top = goalAmount - currentAmount*Math.pow(interestConstant, interestDates.size());
		double bottom = 0;
		for (int i = 0; i <= interestDates.size(); i++)
		{
			bottom+=(contributionsBetween.get(i)*Math.pow(interestConstant, interestDates.size() - i));
		}
		
		double contributionAmount = top/bottom;
		return contributionAmount;
	}
	
	/**
	 * Find the goal amount given the goalDate and contribution. Takes into account interest accumulation.
	 * @param startDate - the date to begin considering interest accumulation. Will begin on the first of the next month
	 * @param nextContribution - the date of the first regular contribution
	 * @param contribution - the regular contribution
	 * @param currentAmount - the initial amount
	 * @param interestRate - the expected interest rate
	 * @param goalDate - the goal date where we stop contributions
	 * @return - the goal amount
	 */
	public static double findGoalAmount(Date startDate, Date nextContribution, Contribution contribution, 
			double currentAmount, double interestRate, Date goalDate)
	{
		Calendar nextContributionCal = Calendar.getInstance();
		nextContributionCal.setTime(nextContribution);
		
		Calendar nextInterestCal = Calendar.getInstance();
		nextInterestCal.setTime(startDate);
		nextInterestCal.set(Calendar.DAY_OF_MONTH, 1);
		nextInterestCal.add(Calendar.MONTH, 1);
		
		while (Prediction.numDaysBetween(nextContributionCal.getTime(), goalDate) >= 0 || 
				Prediction.numDaysBetween(nextInterestCal.getTime(), goalDate) >= 0)
		{
			//Make a contribution
			if (Prediction.numDaysBetween(nextContributionCal.getTime(), nextInterestCal.getTime()) >= 0)
			{
				currentAmount += contribution.getContribution();
				nextContributionCal.setTime(contribution.nextContributionDate(nextContributionCal.getTime()));
			}
			//Accumulate interest
			else
			{
				currentAmount *= (1+interestRate/12.0);
				nextInterestCal.add(Calendar.MONTH, 1);
			}
		}
		return currentAmount;
	}
	
	/**
	 * Calculates the goal date of a savings account with a contribution and interest accumulation
	 * @param startDate - the start date to use for the initial interest accumulation
	 * @param nextContribution - the first contribution date
	 * @param contribution - the contribution amount and frequency
	 * @param currentAmount - the current amount in the savings account
	 * @param goalAmount - the goal amount
	 * @param interestRate - the expected interest rate return of the savings account
	 * @param goalLimit - the goal date limit, set to null if no limit is to be considered (can result in infinite loop)
	 * @return the goal date if found when the goal amount is reached or null if the goal date is not found
	 * 				before hitting the limit set.
	 */
	public static Date findGoalDateWithInterest(Date startDate, Date nextContribution, Contribution contribution, 
			double currentAmount, double goalAmount, double interestRate, Date goalLimit)
	{
		//Frequency should be recurring
		if (contribution.getFrequency().equals(Frequency.oneTime))
		{
			return null;
		}
		
		Calendar nextInterestCalendar = Calendar.getInstance();
		nextInterestCalendar.setTime(startDate);
		nextInterestCalendar.set(Calendar.DAY_OF_MONTH, 1);
		nextInterestCalendar.add(Calendar.MONTH, 1);
		
		Date potentialGoalDate = null;
		while (!(currentAmount >= goalAmount))
		{	
			if (Prediction.numDaysBetween(nextInterestCalendar.getTime(), nextContribution) > 0)
			{
				//Handle the interest accumulation
				if (goalLimit != null && Prediction.numDaysBetween(nextInterestCalendar.getTime(), goalLimit) < 0)
				{
					return null;
				}
				currentAmount = currentAmount + currentAmount * interestRate/12.0;
				potentialGoalDate = nextInterestCalendar.getTime();
				nextInterestCalendar.add(Calendar.MONTH, 1);
			}
			else
			{
				//Make a contribution
				if (goalLimit != null && Prediction.numDaysBetween(nextContribution, goalLimit) < 0)
				{
					return null;
				}
				currentAmount = currentAmount + contribution.getContribution();
				potentialGoalDate = nextContribution;
				nextContribution = contribution.nextContributionDate(nextContribution);
			}
		}
		
		return potentialGoalDate;
	}
	
	/**
	 * Calculates the interest earned on a savings account with interest accumulation given the goal amount that is
	 * trying to be reached.
	 * @param startDate - the date to start consideration of interest accumulation. Interest accrues every month starting the 1st of the
	 * 						next month after the start month
	 * @param nextContribution - the date of the first contribution
	 * @param contribution - the contribution amount and frequency
	 * @param currentAmount - the current amount of this savings account
	 * @param goalAmount - the goal amount
	 * @param interestRate - the interest rate of the savings account
	 * @param goalLimit - the limit that we should stop searching for a goal at (or null if no limit is to be considered)
	 * @return the interest accrued in reaching the goal amount or -1 if the goal amount isn't reached by the limit.
	 */
	public static double findInterestEarned(Date startDate, Date nextContribution, Contribution contribution, 
			double currentAmount, double goalAmount, double interestRate, Date goalLimit)
	{
		//Frequency should be recurring
		double interestEarned = 0;
		if (contribution.getFrequency().equals(Frequency.oneTime))
		{
			return -1;
		}
		
		Calendar nextInterestCalendar = Calendar.getInstance();
		nextInterestCalendar.setTime(startDate);
		nextInterestCalendar.set(Calendar.DAY_OF_MONTH, 1);
		nextInterestCalendar.add(Calendar.MONTH, 1);
		
		while (!(currentAmount >= goalAmount))
		{	
			if (Prediction.numDaysBetween(nextInterestCalendar.getTime(), nextContribution) > 0)
			{
				//Handle the interest accumulation
				if (goalLimit != null && Prediction.numDaysBetween(nextInterestCalendar.getTime(), goalLimit) < 0)
				{
					return -1;
				}
				double tempInterestEarned = currentAmount * interestRate/12.0;
				interestEarned+=tempInterestEarned;
				currentAmount = currentAmount + tempInterestEarned;
				nextInterestCalendar.add(Calendar.MONTH, 1);
			}
			else
			{
				//Make a contribution
				if (goalLimit != null && Prediction.numDaysBetween(nextContribution, goalLimit) < 0)
				{
					return -1;
				}
				currentAmount = currentAmount + contribution.getContribution();
				nextContribution = contribution.nextContributionDate(nextContribution);
			}
		}
		
		return interestEarned;
	}
	
	/**
	 * Given a startDate, payment amount and frequency, and the current debt amount,
	 * this method finds the date the debt would be reduced to zero
	 * 
	 * @param startDate
	 * @param paymentAmount
	 * @param freq
	 * @param debtAmount
	 * @param goalLimit - the date not to look past for our goal or null if no limit should be considered (could result in infinite loop)
	 * 
	 * @return Date - the date the debt would be reduced to zero, or null if not found before the goal limit set
	 * 
	 * @deprecated - use findGoalDateSimpleInterest or findGoalDateCompoundInterest, 
	 * 					this method does not take into account the interest rate
	 */
	public static Date findGoalDate(Date startDate, double paymentAmount, Frequency freq, double debtAmount, Date goalLimit)
	{
		//Make initial payment
		Date nextPayment = startDate;
		debtAmount-=paymentAmount;
		while (debtAmount > 0)
		{
			if (goalLimit != null && Prediction.numDaysBetween(nextPayment, goalLimit) < 0)
			{
				return null;
			}
			//Make a payment until debt is reduced to 0
			debtAmount-=paymentAmount;
			nextPayment = Payment.determineNextPayment(nextPayment, freq);
		}
		return nextPayment;
	}
	
	/**
	 * Finds the total interest paid when paying off a loan using the given payment amount and interest rate.
	 * If the loan isn't paid off by the goal limit set then this method return -1.
	 * @param startDate - the date payments begin
	 * @param daysBeforeFirstPayment - the days before the first payment where interest will accrue
	 * @param paymentAmount - the regular payment amount to make
	 * @param freq - the frequency of the payment
	 * @param debtAmount - the amount of the debt (interest and principal)
	 * @param interestRate - the interest rate for the loan
	 * @param principal - the principal amount of the loan (debtAmount - principal is interest)
	 * @param goalLimit - the limit for our goal date (or null if not limit set)
	 * @return the interest that will be paid over the paying off of this loan or -1 if it isn't paid off by the limit set.
	 */
	public static double findSimpleInterestPaid(Date startDate, int daysBeforeFirstPayment, double paymentAmount,
			Frequency freq, double debtAmount, double interestRate, double principal, Date goalLimit)
	{
		double totalInterestPaid = 0;
		double currInterest = debtAmount - principal;
		
		//Add in initial interest
		currInterest = currInterest + principal * (interestRate/NUM_DAYS_IN_YEAR) * daysBeforeFirstPayment;
		
		//Make the first payment
		if (paymentAmount > currInterest)
		{
			double paymentRemain = paymentAmount - currInterest;
			totalInterestPaid += currInterest;
			currInterest = 0;
			principal-=paymentRemain;
		}
		else
		{
			totalInterestPaid += paymentAmount;
			currInterest-=paymentAmount;
		}
		
		Date lastPayment = startDate;
		Date nextPayment = Payment.determineNextPayment(startDate, freq);
		int daysBeforeNextPayment = Prediction.numDaysBetween(lastPayment, nextPayment);

		//Keep applying payments until principal is reduced to zero or the next payment date goes past our limit
		while (principal > 0)
		{
			//If the next payment date is past the goal limit date
			//than we give up on finding a goal date and return null
			if (goalLimit != null && Prediction.numDaysBetween(nextPayment, goalLimit) < 0)
			{
				return -1;
			}
			
			currInterest = currInterest + principal * (interestRate/NUM_DAYS_IN_YEAR) * daysBeforeNextPayment;
			if (paymentAmount > currInterest)
			{
				double paymentRemain = paymentAmount - currInterest;
				totalInterestPaid += currInterest;
				currInterest = 0;
				principal-=paymentRemain;
			}
			else
			{
				//Only paying down the interest
				totalInterestPaid += paymentAmount;
				currInterest-=paymentAmount;
			}
			
			lastPayment = nextPayment;
			nextPayment = Payment.determineNextPayment(lastPayment, freq);
			daysBeforeNextPayment = Prediction.numDaysBetween(lastPayment, nextPayment);
		}
		
		return totalInterestPaid;
	}
	
	/**
	 * Calculate a goal date for a simple daily interest loan. If the goal isn't found by the specified limit then this method
	 * returns null.
	 * @param startDate - the date the first payment is made
	 * @param daysBeforeFirstPayment - days before the first payment is made and interest is accumulated on the principal
	 * @param paymentAmount - the amount to make for each payment (first applied to interest then to principal)
	 * @param freq - how often the payment is made
	 * @param debtAmount - the current total amount of the loan debt (principal and interest)
	 * @param interestRate - the interest rate of the loan
	 * @param principal - the principal amount of the loan used to calculate interest from (debt amount - principal is the starting interest)
	 * @param goalLimit - the date not to look past for a goal or null if no limit to be considered
	 * @return - the goal date if found before the specified limit or null if not found
	 */
	public static Date findGoalDateSimpleInterest(Date startDate, int daysBeforeFirstPayment, double paymentAmount,
			Frequency freq, double debtAmount, double interestRate, double principal, Date goalLimit)
	{
		double currInterest = debtAmount - principal;
		
		//Add in initial interest
		currInterest = currInterest + principal * (interestRate/NUM_DAYS_IN_YEAR) * daysBeforeFirstPayment;
		
		//Make the first payment
		if (paymentAmount > currInterest)
		{
			double paymentRemain = paymentAmount - currInterest;
			currInterest = 0;
			principal-=paymentRemain;
		}
		else
		{
			currInterest-=paymentAmount;
		}
		
		Date lastPayment = startDate;
		Date nextPayment = Payment.determineNextPayment(startDate, freq);
		int daysBeforeNextPayment = Prediction.numDaysBetween(lastPayment, nextPayment);

		//Keep applying payments until principal is reduced to zero or the next payment date goes past our limit
		while (principal > 0)
		{
			//If the next payment date is past the goal limit date
			//than we give up on finding a goal date and return null
			if (goalLimit != null && Prediction.numDaysBetween(nextPayment, goalLimit) < 0)
			{
				return null;
			}
			
			currInterest = currInterest + principal * (interestRate/NUM_DAYS_IN_YEAR) * daysBeforeNextPayment;
			if (paymentAmount > currInterest)
			{
				double paymentRemain = paymentAmount - currInterest;
				currInterest = 0;
				principal-=paymentRemain;
			}
			else
			{
				//Only paying down the interest
				currInterest-=paymentAmount;
			}
			
			lastPayment = nextPayment;
			nextPayment = Payment.determineNextPayment(lastPayment, freq);
			daysBeforeNextPayment = Prediction.numDaysBetween(lastPayment, nextPayment);
		}
		
		return lastPayment;
	}
	
	/**
	 * Calculates how much would be paid in interest over the life of a loan.
	 * @param startDate - the first day that a payment will be applied to this debt and begin recurring
	 * @param daysBeforeFirstPayment - the days before the first payment occurs (interest is calculated for each of these days with no payments being applied).
	 * @param paymentAmount - the amount of the payment that is made regularly
	 * @param freq - the frequency of the payment
	 * @param debtAmount - the amount of the debt that we want reduced to zero with our recurring payment
	 * @param interestRate - the interest rate that will be considered in applying interest compounded daily
	 * @param goalLimit - the date not to search past for our goal or null if no limit set
	 * @return the interest that will be paid while paying a debt down to 0 or -1 if no goal is found before limit is hit
	 * 
	 */
	public static double findCompoundInterestPaid(Date startDate, int daysBeforeFirstPayment, double paymentAmount,
			Frequency freq, double debtAmount, double interestRate, Date goalLimit)
	{
		double totalInterest = 0;
		//Add in initial interest and apply first payment
		double tempDebtAmount = debtAmount;
		debtAmount = debtAmount * Math.pow(1+interestRate/NUM_DAYS_IN_YEAR, daysBeforeFirstPayment);
		totalInterest += (debtAmount - tempDebtAmount);
		debtAmount -= paymentAmount;
				
		Date lastPayment = startDate;
		Date nextPayment = Payment.determineNextPayment(startDate, freq);
		int daysBeforeNextPayment = Prediction.numDaysBetween(lastPayment, nextPayment);
				
		//Keep applying payments until debt is reduced to zero
		while (debtAmount > 0)
		{
			if (goalLimit != null && Prediction.numDaysBetween(nextPayment, goalLimit) < 0)
			{
				return -1;
			}
			tempDebtAmount = debtAmount;
			debtAmount = debtAmount * Math.pow(1+interestRate/NUM_DAYS_IN_YEAR,  daysBeforeNextPayment);			
			totalInterest += (debtAmount - tempDebtAmount);
			debtAmount-=paymentAmount;
						
			lastPayment = nextPayment;
			nextPayment = Payment.determineNextPayment(lastPayment, freq);
			daysBeforeNextPayment = Prediction.numDaysBetween(lastPayment, nextPayment);			
		}
		
		return totalInterest;
	}
	
	/**
	 * Calculates when a debt payment will be reduced to zero taking into account interest that is assumed to be compounded daily, and
	 * a payment that is being made regularly to the debt. This also assumes that the debt amount is fixed and won't change otherwise the
	 * goal date may no longer be valid.
	 * @param startDate - the first day that a payment will be applied to this debt and begin recurring
	 * @param daysBeforeFirstPayment - the days before the first payment occurs (interest is calculated for each of these days with no payments being applied).
	 * @param paymentAmount - the amount of the payment that is made regularly
	 * @param freq - the frequency of the payment
	 * @param debtAmount - the amount of the debt that we want reduced to zero with our recurring payment
	 * @param interestRate - the interest rate that will be considered in applying interest compounded daily
	 * @param goalLimit - the date not to search past for our goal or null if no limit set
	 * @return the date that the debt will be reduced to zero using the payment or null if no goal found before hitting the goal limit
	 */
	public static Date findGoalDateCompoundInterest(Date startDate, int daysBeforeFirstPayment, double paymentAmount,
			Frequency freq, double debtAmount, double interestRate, Date goalLimit)
	{
		//Add in initial interest and apply first payment
		debtAmount = debtAmount * Math.pow(1+interestRate/NUM_DAYS_IN_YEAR, daysBeforeFirstPayment) - paymentAmount;
		Date lastPayment = startDate;
		Date nextPayment = Payment.determineNextPayment(startDate, freq);
		int daysBeforeNextPayment = Prediction.numDaysBetween(lastPayment, nextPayment);
				
		//Keep applying payments until debt is reduced to zero
		while (debtAmount > 0)
		{

			if (goalLimit != null && Prediction.numDaysBetween(nextPayment, goalLimit) < 0)
			{
				return null;
			}
			debtAmount = debtAmount * Math.pow(1+interestRate/NUM_DAYS_IN_YEAR,  daysBeforeNextPayment);
			debtAmount-=paymentAmount;
			lastPayment = nextPayment;
			nextPayment = Payment.determineNextPayment(lastPayment, freq);
			daysBeforeNextPayment = Prediction.numDaysBetween(lastPayment, nextPayment);
		}
		
		return lastPayment;
	}
	
	/**
	 * Utility method. Converts the amount at origFreq to a new frequency. Every
	 * frequency except for oneTime can be converted from one to another.
	 * 
	 * @param amount - the original amount at the original frequency
	 * @param origFreq - the original frequency we are converting from
	 * @param convertFreq - the new frequency we wish to convert to
	 * @return double - the new amount at the converted frequency. returns -1 if oneTime is
	 * 						passed as one of the frequencies.
	 */
	public static double toggle(double amount, Frequency origFreq, Frequency convertFreq)
	{
		//First convert the amount at the origFreq to a daily freq
		double dailyAmount = amount;
		switch (origFreq)
		{
			case daily:
			{
				break;
			}
			case weekly:
			{
				dailyAmount/=7;
				break;
			}
			case biWeekly:
			{
				dailyAmount/=14;
				break;
			}
			case monthly:
			{
				dailyAmount = (dailyAmount*12)/NUM_DAYS_IN_YEAR;
				break;
			}
			case yearly:
			{
				dailyAmount/=NUM_DAYS_IN_YEAR;
				break;
			}
			default:
			{
				//One time is the only possible case remaining
				return -1;
			}
		}
		
		//Now convert from daily to the specified convertFreq
		switch (convertFreq)
		{
			case daily:
			{
				return dailyAmount;
			}
			case weekly:
			{
				return dailyAmount * 7;
			}
			case biWeekly:
			{
				return dailyAmount * 14;
			}
			case monthly:
			{
				return (dailyAmount * NUM_DAYS_IN_YEAR)/12;
			}
			case yearly:
			{
				return dailyAmount * NUM_DAYS_IN_YEAR;
			}
			default:
			{
				//One time is the only possible case remaining
				return -1;
			}
		}
	}
	
	/*
	 * This is an unfinished method. More thought needed yet to see what analysis (multiple?) might work best.
	 * Currently this method takes only gains and losses (including budget items) and converts each item
	 * to an individual amount at the given frequency. Then it does a net to get a sense of the total gain/loss
	 * of the data. It doesn't return anything but rather prints the results to standard out.
	 */
	public static void analyze1(BadBudgetData data, Frequency freq)
	{		
		ArrayList<Account> accounts = data.getAccounts();
		ArrayList<MoneyGain> gains = data.getGains();
		ArrayList<MoneyOwed> debts = data.getDebts();
		ArrayList<MoneyLoss> losses = data.getLosses();
		Budget budget = data.getBudget();
		
		//Convert all gains and losses (including budget items) to daily amounts and do a net of all
		HashMap<String, Double> dailyAmounts = new HashMap<String, Double>();
		
		for (MoneyGain currGain : gains)
		{
			double dailyAmount = toggle(currGain.gainAmount(), currGain.gainFrequency(), freq);
			dailyAmounts.put(currGain.sourceDescription(), dailyAmount);
		}
		for (MoneyLoss currLoss : losses)
		{
			double dailyAmount = toggle(currLoss.lossAmount(), currLoss.lossFrequency(), freq);
			dailyAmount = -dailyAmount;
			dailyAmounts.put(currLoss.expenseDescription(), dailyAmount);
		}
		for (BudgetItem currItem : budget.getAllBudgetItems().values())
		{
			double dailyAmount = toggle(currItem.lossAmount(), currItem.lossFrequency(), freq);
			dailyAmount = -dailyAmount;
			dailyAmounts.put(currItem.expenseDescription(), dailyAmount);
		}
		
		double net = 0;
		
		for (String descriptionKey : dailyAmounts.keySet())
		{
			double currAmount = dailyAmounts.get(descriptionKey);
			net+=currAmount;
			System.out.println(descriptionKey + ": " + currAmount);
		}
		System.out.println("Net: " + net);
	}
	
	/**
	 * Determines if a bbd object with the given dates should be considered as having valid dates for the
	 * given chosen user date at the given frequency.
	 * To be valid the end date for a bbd object must come after
	 * the chosen date (not strict) (which should be restricted to being on or after today's date)
	 * End date can also be null indicating an ongoing transaction. 
	 * Then either the next date must be before or on the chosen date or 
	 * one freq period before the next date must be before or on the chosen date.
	 * 
	 * This method should be called only when considering bbd objects that have been updated to today's
	 * date. (i.e. the next date shouldn't be on today's date unless the end date was hit in the past).
	 * 
	 * @param chosenDate - the user selected date (assumed to be on or after the date of today)
	 * @param nextDate - the next date of a bbd object transaction
	 * @param endDate - the end date of a bbd object transaction
	 * @param freq - the frequency of a bbd object transaction
	 * @return - whether these dates represent a considerable next date for a bbd object transaction
	 */
	public static boolean considerableNextDate(Date chosenDate, Date nextDate, Date endDate, Frequency freq)
	{		
		if (chosenDate == null || nextDate == null || freq == null || 
				!(endDate == null || Prediction.numDaysBetween(nextDate, endDate) >= 0))
		{
			return false;
		}
		
		Calendar prevCal = Calendar.getInstance();
		prevCal.setTime(nextDate);
		switch (freq)
		{
			case oneTime:
			{
				return false;
			}
			case daily:
			{
				prevCal.add(Calendar.DAY_OF_YEAR, -1);
				break;
			}
			case weekly:
			{
				prevCal.add(Calendar.WEEK_OF_YEAR, -1);
				break;
			}
			case biWeekly:
			{
				prevCal.add(Calendar.WEEK_OF_YEAR, -2);
				break;
			}
			case monthly:
			{
				prevCal.add(Calendar.MONTH, -1);
				break;
			}
			case yearly:
			{
				prevCal.add(Calendar.YEAR, -1);
				break;
			}
			default:
			{
				return false;
			}
		}
		Date prevDate = prevCal.getTime();
		boolean validEndDate = endDate == null || Prediction.numDaysBetween(chosenDate, endDate) >= 0;
		boolean validNextDate;

		if (Prediction.numDaysBetween(chosenDate, nextDate) > 0)
		{
			validNextDate = Prediction.numDaysBetween(prevDate, chosenDate) >= 0;
		}
		else
		{
			validNextDate = true;
		}
		return validNextDate && validEndDate;
	}
	
	/**
	 * Given a bad budget data object, a frequency, and a chosen date; returns the gain per that frequency
	 * of the bad budget data's gains excluding any that have ended or are not within a frequency period
	 * back from the next date. (see considerableNextDate for more on considerable dates)
	 * 
	 * @param bbd - the bad budget data object to consider gains for
	 * @param freq - the frequency to see our net gains at
	 * @param chosenDate - the user chosen date (should be greater equal or greater than today's date) to
	 * 						see the net gain amount for.
	 * @return the net gain at the given frequency for gains inside of the considerable range
	 */
	public static double analyzeNetGainAtFreq(BadBudgetData bbd, Frequency freq, Date chosenDate)
	{
		double netGainAtFreq = 0;
		for (MoneyGain currGain : bbd.getGains())
		{
			if (considerableNextDate(chosenDate, currGain.nextDeposit(), currGain.endDate(), currGain.gainFrequency()))
			{
				double freqAmount = toggle(currGain.gainAmount(), currGain.gainFrequency(), freq);
				netGainAtFreq += freqAmount;
			}
		}
		return netGainAtFreq;
	}
	
	/**
	 * Given a bad budget data object, a frequency, and a chosen date; returns the loss per that frequency
	 * of the bad budget data's losses excluding any that have ended or are not within a frequency period
	 * back from the next date. Includes losses from budget items. (see considerableNextDate for more on considerable dates)
	 * 
	 * @param bbd - the bad budget data object to consider
	 * @param freq - the frequency to see our net losses at
	 * @param chosenDate - the user chosen date (should be greater equal or greater than today's date) to
	 * 						see the net amount for.
	 * @return the net loss at the given frequency for losses inside of the considerable range
	 */
	public static double analyzeNetLossAtFreq(BadBudgetData bbd, Frequency freq, Date chosenDate)
	{
		double netLossAtFreq = 0;
		ArrayList<MoneyLoss> allLosses = new ArrayList<MoneyLoss>();
		allLosses.addAll(bbd.getLosses());
		allLosses.addAll(bbd.getBudget().getAllBudgetItems().values());
		
		for (MoneyLoss currLoss : allLosses)
		{
			if (considerableNextDate(chosenDate, currLoss.nextLoss(), currLoss.endDate(), currLoss.lossFrequency()))
			{
				double freqAmount = toggle(currLoss.lossAmount(), currLoss.lossFrequency(), freq);
				netLossAtFreq += freqAmount;
			}
		}
		 
		return netLossAtFreq;
	}
	
	/**
	 * Given a bad budget data object, a frequency, and a chosen date; returns the account losses per that frequency
	 * of the bad budget data's losses excluding any that have ended or are not within a frequency period
	 * back from the next date. Includes losses from budget items. (see considerableNextDate for more on considerable dates)
	 * 
	 * @param bbd - the bad budget data object to consider
	 * @param freq - the frequency to see our net losses at
	 * @param chosenDate - the user chosen date (should be greater equal or greater than today's date) to
	 * 						see the net amount for.
	 * @return the net loss at the given frequency for losses inside of the considerable range
	 */
	public static double analyzeNetAccountLossAtFreq(BadBudgetData bbd, Frequency freq, Date chosenDate)
	{
		double netLossAtFreq = 0;
		ArrayList<MoneyLoss> allLosses = new ArrayList<MoneyLoss>();
		allLosses.addAll(bbd.getLosses());
		allLosses.addAll(bbd.getBudget().getAllBudgetItems().values());
		
		for (MoneyLoss currLoss : allLosses)
		{
			if (currLoss.source() instanceof Account && 
					considerableNextDate(chosenDate, currLoss.nextLoss(), currLoss.endDate(), currLoss.lossFrequency()))
			{
				double freqAmount = toggle(currLoss.lossAmount(), currLoss.lossFrequency(), freq);
				netLossAtFreq += freqAmount;
			}
		}
		 
		return netLossAtFreq;
	}
	
	/**
	 * Given a bad budget data object, a frequency, and a chosen date; returns the credit card losses per that frequency
	 * of the bad budget data's losses excluding any that have ended or are not within a frequency period
	 * back from the next date. Includes losses from budget items. (see considerableNextDate for more on considerable dates)
	 * 
	 * @param bbd - the bad budget data object to consider
	 * @param freq - the frequency to see our net losses at
	 * @param chosenDate - the user chosen date (should be greater equal or greater than today's date) to
	 * 						see the net amount for.
	 * @return the net loss at the given frequency for losses inside of the considerable range
	 */
	public static double analyzeNetCreditCardLossAtFreq(BadBudgetData bbd, Frequency freq, Date chosenDate)
	{
		double netLossAtFreq = 0;
		ArrayList<MoneyLoss> allLosses = new ArrayList<MoneyLoss>();
		allLosses.addAll(bbd.getLosses());
		allLosses.addAll(bbd.getBudget().getAllBudgetItems().values());
		
		for (MoneyLoss currLoss : allLosses)
		{
			if (currLoss.source() instanceof CreditCard && 
					considerableNextDate(chosenDate, currLoss.nextLoss(), currLoss.endDate(), currLoss.lossFrequency()))
			{
				double freqAmount = toggle(currLoss.lossAmount(), currLoss.lossFrequency(), freq);
				netLossAtFreq += freqAmount;
			}
		}
		 
		return netLossAtFreq;
	}
	
	/**
	 * Given a bad budget data object, a frequency, and a chosen date; returns the payments per that frequency
	 * of the bad budget data's debts excluding any payments that have ended or are not within a frequency period
	 * back from the next payment date. (see considerableNextDate for more on considerable dates). Debts with
	 * payments set to payoff are included in this amount (0 if not a creditCard otherwise see analyzeCreditCard
	 * MoneyOut). Finally overpayments are considered by using findGoalDateCompoundInterest for a switch date of
	 * when the payment switchs from full payments to paying the losses to that debt (Credit Cards are only debt
	 * that will be nonzero).
	 * @param bbd - the bad budget data object to consider payments for
	 * @param freq - the frequency to see our net payments at
	 * @param chosenDate - the user chosen date (should be greater equal or greater than today's date) to
	 * 						see the net amount for.
	 * @param today - the date to consider as today's date, used for calculating the goal date/switch date
	 * @param limitDate - the limit to place on the search for a switch date of full payments to 0 or loss payments
	 * @return the net payment at the given frequency for payments inside of the considerable range
	 */
	public static double analyzeNetPaymentsAtFreq(BadBudgetData bbd, Frequency freq, Date chosenDate, Date today, Date limitDate)
	{
		double netPaymentAtFreq = 0;
		for (MoneyOwed currDebt : bbd.getDebts())
		{
			double paymentAtFreq = Prediction.analyzeSingleDebtPaymentAtFreq(currDebt, 
					bbd, freq, chosenDate, today, limitDate);
			if (paymentAtFreq != -1)
			{
				netPaymentAtFreq += paymentAtFreq;
			}
		}
		return netPaymentAtFreq;
	}
	
	/**
	 * Given a bad budget data object, a frequency, and a chosen date; returns the contribution per that frequency
	 * of the bad budget data's contributions excluding any that have ended or are not within a frequency period
	 * back from the next date. (see considerableNextDate for more on considerable dates)
	 * 
	 * @param bbd - the bad budget data object to consider
	 * @param freq - the frequency to see our net contributions at
	 * @param chosenDate - the user chosen date (should be greater equal or greater than today's date) to
	 * 						see the net amount for.
	 * @return the net contribution at the given frequency for contributions inside of the considerable range
	 */
	public static double analyzeNetContributionsAtFreq(BadBudgetData bbd, Frequency freq, Date chosenDate)
	{
		double netContributionAtFreq = 0;
		for (Account currAccount : bbd.getAccounts())
		{
			if (currAccount instanceof SavingsAccount)
			{
				SavingsAccount currSavingsAccount = (SavingsAccount)currAccount;
				Contribution currContribution = currSavingsAccount.contribution();
				if (currContribution != null && considerableNextDate(chosenDate, currSavingsAccount.nextContribution(), 
						currSavingsAccount.endDate(), currSavingsAccount.contribution().getFrequency()))
				{
					double freqAmount = toggle(currContribution.getContribution(), currContribution.getFrequency(), freq);
					netContributionAtFreq+=freqAmount;
				}
			}
		}
		return netContributionAtFreq;
	}
	
	/**
	 * Returns the net gains subtracted from the net losses using analyzeNetGainAtFreq and analyzeNetLossAtFreq.
	 * Net Flow.
	 * @param bbd - the bad budget data to calculate net gains minus losss for
	 * @param freq - the freq at which to consider the gains minus losses for
	 * @param chosenDate - the user chosen date (should be greater equal or greater than today's date) to
	 * 						see the net amount for.
	 * @return the net gain - loss amount at the given freq for bbd objects
	 */
	public static double analyzeGainsLosses(BadBudgetData bbd, Frequency freq, Date chosenDate)
	{
		return analyzeNetGainAtFreq(bbd, freq, chosenDate) - analyzeNetLossAtFreq(bbd, freq, chosenDate);
	}
	
	/**
	 * Returns the net gains subtract the net account losses, payments, and contributions using analyzeNetGainAtFreq,
	 * analyzeNetAccountLossAtFreq, analyzeNetPaymentsAtFreq, and analyzeNetContributionsAtFreq.
	 * @param bbd - the bad budget data to calculate for
	 * @param freq - the freq to consider
	 * @param chosenDate - the user chosen date (should be greater equal or greater than today's date) to
	 * 						see the net amount for.
	 * @param today - date to consider as today's date
	 * @param limitDate - the limit date to use when trying to find a switch date for payments
	 * @return gains - account losses - payments - contributions for the bbd objects at the given freq.
	 */
	public static double analyzeCashFlow(BadBudgetData bbd, Frequency freq, Date chosenDate, Date today, Date limitDate)
	{
		return analyzeNetGainAtFreq(bbd, freq, chosenDate) -
				analyzeNetAccountLossAtFreq(bbd, freq, chosenDate) -
				analyzeNetPaymentsAtFreq(bbd, freq, chosenDate, today, limitDate) - 
				analyzeNetContributionsAtFreq(bbd, freq, chosenDate);
	}
	
	/**
	 * Returns the debt flow (the analyzeNetCreditCardLossesAtFreq - analyzeNetPaymentsAtFreq)
	 * @param bbd - the bad budget data to calculate for
	 * @param freq - the freq to consider
	 * @param chosenDate - the user chosen date (should be greater equal or greater than today's date) to
	 * 						see the net amount for.
	 * @param today - date to consider as today's date
	 * @param limitDate - the limit date for the switch date for payments
	 * @return the debt flow
	 */
	public static double analyzeDebtFlow(BadBudgetData bbd, Frequency freq, Date chosenDate, Date today, Date limitDate)
	{
		return analyzeNetCreditCardLossAtFreq(bbd, freq, chosenDate) - 
				analyzeNetPaymentsAtFreq(bbd, freq, chosenDate, today, limitDate);
	}
	
	/**
	 * Looks through all ways that money can leave. These ways include losses (including
	 * losses from budget items), payments to debts, and contributions to savings accounts. For each way money leaves this method
	 * gets that money out source and adds it to a running net of money out at the specified freq and within a considerable date.
	 * Returns a map from a source to the net money out from that source for the given freq and within the considerable date. 
	 * For sources that are sources for debt payments that have payoff specified: it should be noted that the payoff amount is
	 * excluded from the money out result.(To account for the payoff amount the user would need to consider the debt as a source itself).
	 * @param bbd - the bad budget data
	 * @param freq - the frequency to consider
	 * @param chosenDate - the date to consider for considerable dates. (see considerableNextDate for more)
	 * @param today - the date to consider as today's date (used for payment switch date)
	 * @param limitDate - the date to limit the search for our switch date to.
	 * @return a map of sources to the money leaving through that source at the given freq and within the considerable date range
	 */
	public static HashMap<Source, Double> analyzeSourceMoneyOut(BadBudgetData bbd, Frequency freq, Date chosenDate,
			Date today, Date limitDate)
	{
		HashMap<Source, Double> sourcesMoneyOut = new HashMap<>();
		
		ArrayList<MoneyLoss> allLosses = new ArrayList<MoneyLoss>();
		allLosses.addAll(bbd.getLosses());
		allLosses.addAll(bbd.getBudget().getAllBudgetItems().values());
		
		//Check all losses and budget items. Add their sources to the map.
		for (MoneyLoss currLoss : allLosses)
		{
			if (considerableNextDate(chosenDate, currLoss.nextLoss(), currLoss.endDate(), currLoss.lossFrequency()))
			{
				Source source = currLoss.source();
				Double currSourceMoneyOut = sourcesMoneyOut.get(source);
				
				double freqAmount = toggle(currLoss.lossAmount(), currLoss.lossFrequency(), freq);

				if (currSourceMoneyOut == null)
				{
					sourcesMoneyOut.put(source, freqAmount);
				}
				else
				{
					sourcesMoneyOut.put(source, currSourceMoneyOut + freqAmount);
				}
			}
		}
		
		//Consider all savings accounts. Add the sources for their contributions to the map.
		for (Account currAccount : bbd.getAccounts())
		{
			if (currAccount instanceof SavingsAccount)
			{
				SavingsAccount currSavingsAccount = (SavingsAccount)currAccount;
				Contribution currContribution = currSavingsAccount.contribution();
				if (currContribution != null && 
						considerableNextDate(chosenDate, currSavingsAccount.nextContribution(), currSavingsAccount.endDate(), freq))
				{
					Account accountSource = currSavingsAccount.sourceAccount();
					Double currSourceMoneyOut = sourcesMoneyOut.get(accountSource);
					
					double freqAmount = toggle(currContribution.getContribution(), currContribution.getFrequency(), freq);
					
					if (currSourceMoneyOut == null)
					{
						sourcesMoneyOut.put(accountSource, freqAmount);
					}
					else
					{
						sourcesMoneyOut.put(accountSource, currSourceMoneyOut + freqAmount);
					}
				}
			}
		}
		
		//Consider all debts.
		for (MoneyOwed currDebt : bbd.getDebts())
		{
			double paymentAtFreq = Prediction.analyzeSingleDebtPaymentAtFreq(currDebt, 
					bbd, freq, chosenDate, today, limitDate);
			
			if (paymentAtFreq != -1)
			{
				Account accountSource = currDebt.payment().sourceAccount();
				Double currSourceMoneyOut = sourcesMoneyOut.get(accountSource);
								
				if (currSourceMoneyOut == null)
				{
					sourcesMoneyOut.put(accountSource, paymentAtFreq);
				}
				else
				{
					sourcesMoneyOut.put(accountSource, currSourceMoneyOut + paymentAtFreq);
				}
			}
		}
		
		return sourcesMoneyOut;
	}
	
	/**
	 * Private helper method that looks at a single debt and consider's its payment. If
	 * it has a considerable payment this method returns the payment amount for the given
	 * chosen date at the given frequency. If it does not have a considerable payment this method returns -1;
	 * @param debt - the single debt to consider
	 * @param bbd - the bad budget data to use for calculating a credit cards money out
	 * @param freq - the frequency to see our payment at
	 * @param chosenDate - the user chosen date (should be greater equal or greater than today's date) to
	 * 						see the payment amount for.
	 * @param today - the date to consider as today's date, used for calculating the goal date/switch date
	 * @param limitDate - the limit to place on the search for a switch date of full payments to 0 or loss payments
	 * @return returns the payment amount for the given
	 * chosen date at the given frequency. If it does not have a considerable payment this method returns -1
	 */
	private static double analyzeSingleDebtPaymentAtFreq(MoneyOwed debt, BadBudgetData bbd,
			Frequency freq, Date chosenDate, Date today, Date limitDate)
	{
		Payment currPayment = debt.payment();
		double paymentAtFreq = -1;
		if (currPayment != null && considerableNextDate(chosenDate, currPayment.nextPaymentDate(), currPayment.endDate(), currPayment.frequency()))
		{
			paymentAtFreq = 0.0;
			if (currPayment.payOff())
			{
				if (debt instanceof CreditCard)
				{
					paymentAtFreq = Prediction.analyzeCreditCardMoneyOut((CreditCard)debt, bbd, freq, chosenDate);
				}
			}
			else
			{
				double payAmt = currPayment.amount();
				double lossesAmt = 0;
				if (debt instanceof CreditCard)
				{
					lossesAmt = Prediction.analyzeCreditCardMoneyOut((CreditCard)debt, bbd, currPayment.frequency(), chosenDate);
				}
				
				Date switchDate = null;
				double toggleAmt = payAmt;
				
				if (lossesAmt < payAmt && currPayment.goalDate() == null)
				{						
					switchDate = Prediction.findGoalDateCompoundInterest(currPayment.nextPaymentDate(), 
							Prediction.numDaysBetween(today, currPayment.nextPaymentDate()), payAmt - lossesAmt, 
							currPayment.frequency(), debt.amount(), debt.interestRate(), limitDate);
					
					if (switchDate != null && Prediction.numDaysBetween(switchDate, chosenDate) > 0)
					{
						toggleAmt = lossesAmt;
					}
				}
				
				paymentAtFreq = toggle(toggleAmt, currPayment.frequency(), freq);
			}
		}
		return paymentAtFreq;
	}
	
	/**
	 * Returns the money coming out of a credit card for the given freq and chosen date. Includes money going
	 * toward losses and budget items.
	 * @param creditCard - the credit card to consider
	 * @param bbd - the bad budget data object
	 * @param freq - the frequency to see the credit card's money out
	 * @param chosenDate - the date to consider for the credit card's money out
	 * @return the money out of a credit card for the given freq and chosen date.
	 */
	public static double analyzeCreditCardMoneyOut(CreditCard creditCard, BadBudgetData bbd, Frequency freq, Date chosenDate)
	{	
		double freqAmount = 0.0;
		
		ArrayList<MoneyLoss> allLosses = new ArrayList<MoneyLoss>();
		allLosses.addAll(bbd.getLosses());
		allLosses.addAll(bbd.getBudget().getAllBudgetItems().values());
		
		//Check all losses and budget items. If their source is the credit card we are interested in we add the freqAmt to the net amount
		for (MoneyLoss currLoss : allLosses)
		{
			Source source = currLoss.source();
			if (source instanceof CreditCard && source.name().equals(creditCard.name()) && 
					considerableNextDate(chosenDate, currLoss.nextLoss(), currLoss.endDate(), currLoss.lossFrequency()))
			{
				freqAmount += toggle(currLoss.lossAmount(), currLoss.lossFrequency(), freq);
			}
		}
		
		return freqAmount;
	}
	
	/**
	 * Runs a prediction from currDate to endDate. Considers remainAmounts if
	 * auto reset is on.
	 * Uses the results of this prediction to update all of 
	 * the bbd objects in bbd. Does not clear the prediction data before returning,
	 * allowing user access to history up to the update date. 
	 * @param bbd - the objects to run the update on
	 * @param currDate - the start date for our prediction/update (note used by debts for the start date, namely credit cards)
	 * @param endDate - the stopping date (inclusive) for our prediction
	 */
	public static void update(BadBudgetData bbd, Date currDate, Date endDate)
	{
		boolean autoReset = bbd.getBudget().isAutoReset();

		if (autoReset)
		{
			//Run a prediction with considerRemainAmount set
			Prediction.predict(bbd, currDate, endDate, true);
		}
		else
		{
			Prediction.predict(bbd, currDate, endDate, false);
		}
		int dayIndex = Prediction.numDaysBetween(currDate, endDate);
		for (Account currAccount : bbd.getAccounts())
		{
			currAccount.update(dayIndex);
		}
		for (MoneyOwed currDebt : bbd.getDebts())
		{
			currDebt.update(endDate, dayIndex);
		}
		for (MoneyGain currGain : bbd.getGains())
		{
			currGain.update(dayIndex);
		}
		for (MoneyLoss currLoss : bbd.getLosses())
		{
			currLoss.update(dayIndex);
		}
		for (BudgetItem currItem : bbd.getBudget().getAllBudgetItems().values())
		{
			currItem.update(dayIndex, autoReset);
		}
	}
	
	/**
	 * Only updates a bbd object's next dates, and also dependent goal dates if necessary. Budget items
	 * current amount is set depending on whether autoReset is set in the budget. If not set then
	 * the budget items only have their next dates updated. If set then the remain action is considered and
	 * the curr amount of the items is updated accordingly.
	 * @param bbd - the bad budget data to update the next dates for
	 * @param currDate - the date considered as the start
	 * @param endDate - the date to end our update on
	 */
	public static void updateNextDatesOnly(BadBudgetData bbd, Date currDate, Date endDate)
	{
		boolean autoReset = bbd.getBudget().isAutoReset();

		if (autoReset)
		{
			//Run a prediction and consider remain action
			Prediction.predict(bbd, currDate, endDate, true);
		}
		else
		{
			Prediction.predict(bbd, currDate, endDate, false);
		}
		
		int dayIndex = Prediction.numDaysBetween(currDate, endDate);
		for (Account currAccount : bbd.getAccounts())
		{
			currAccount.updateNextDatesOnly(dayIndex);
		}
		for (MoneyOwed currDebt : bbd.getDebts())
		{
			currDebt.updateNextDatesOnly(dayIndex);
		}
		for (MoneyGain currGain : bbd.getGains())
		{
			currGain.updateNextDatesOnly(dayIndex);
		}
		for (MoneyLoss currLoss : bbd.getLosses())
		{
			currLoss.updateNextDatesOnly(dayIndex);
		}
		for (BudgetItem currItem : bbd.getBudget().getAllBudgetItems().values())
		{
			currItem.updateNextDatesOnly(dayIndex, autoReset);
		}
	}
}
