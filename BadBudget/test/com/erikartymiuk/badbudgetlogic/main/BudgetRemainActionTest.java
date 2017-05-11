package com.erikartymiuk.badbudgetlogic.main;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;

import com.erikartymiuk.badbudgetlogic.budget.Budget;
import com.erikartymiuk.badbudgetlogic.budget.BudgetItem;
import com.erikartymiuk.badbudgetlogic.budget.RemainAmountAction;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataAccount;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataBudgetItem;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataMoneyOwed;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.TransactionHistoryItem;

public class BudgetRemainActionTest {

	@Test
	public void remainTest1() throws BadBudgetInvalidValueException {
		
		//22 days
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 12);
		Calendar endCal = new GregorianCalendar(2017, Calendar.FEBRUARY, 2);
		
		BadBudgetData bbd = new BadBudgetData();
		
		Account account = new Account("Account", 50, false);
		CreditCard card = new CreditCard("Card", 100, false, 0);
		MoneyLoss loss = new MoneyLoss("cable", 160, Frequency.monthly, startCal.getTime(), null, card);
		
		Budget budget = new Budget(card, true, Calendar.SUNDAY, 1);
		BudgetItem item1 = new BudgetItem("grocery", 35, Frequency.weekly, 
				startCal.getTime(), null, false, card);
		item1.setRemainAmountAction(RemainAmountAction.addBack);
		
		BudgetItem item2 = new BudgetItem("snack", 3, Frequency.daily, startCal.getTime(), null, false, card);
		item2.setRemainAmountAction(RemainAmountAction.accumulates);
		
		budget.addBudgetItem(item1);
		budget.addBudgetItem(item2);
		
		bbd.addAccount(account);
		bbd.addDebt(card);
		bbd.addLoss(loss);
		bbd.setBudget(budget);
		
		Prediction.update(bbd, startCal.getTime(), endCal.getTime());
		
		int dayIndex = 
				Prediction.numDaysBetween(startCal.getTime(), endCal.getTime());
		
		PredictDataBudgetItem pdbi1 = item1.getPredictData(dayIndex);
		PredictDataBudgetItem pdbi2 = item2.getPredictData(dayIndex);
		
		assertTrue(item2.getCurrAmount() == 66.0);
		//Net value
		assertTrueHelper("1", 50 -100 - 160, account.value() - card.amount() + item2.getCurrAmount() + item1.getCurrAmount());
	
		//printResults(item1, dayIndex);
		//printResults(card, dayIndex);
		
	}
	
	

	@Test
	public void remainTest2() throws BadBudgetInvalidValueException {
		
		//22 days
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 12);
		Calendar endCal = new GregorianCalendar(2017, Calendar.FEBRUARY, 2);
		
		BadBudgetData bbd = new BadBudgetData();
		
		Account account = new Account("Account", 50, false);
		CreditCard card = new CreditCard("Card", 100, false, 0);
		MoneyLoss loss = new MoneyLoss("cable", 160, Frequency.monthly, startCal.getTime(), null, card);
		
		Budget budget = new Budget(card, true, Calendar.SUNDAY, 1);
		BudgetItem item1 = new BudgetItem("grocery", 35, Frequency.weekly, 
				startCal.getTime(), null, false, account);
		item1.setRemainAmountAction(RemainAmountAction.addBack);
		
		BudgetItem item2 = new BudgetItem("snack", 3, Frequency.daily, startCal.getTime(), null, false, account);
		item2.setRemainAmountAction(RemainAmountAction.accumulates);
		
		budget.addBudgetItem(item1);
		budget.addBudgetItem(item2);
		
		bbd.addAccount(account);
		bbd.addDebt(card);
		bbd.addLoss(loss);
		bbd.setBudget(budget);
		
		Prediction.update(bbd, startCal.getTime(), endCal.getTime());
		
		int dayIndex = 
				Prediction.numDaysBetween(startCal.getTime(), endCal.getTime());
		
		assertTrue(item2.getCurrAmount() == 66.0);
		//Net value
		assertTrueHelper("2", 50 -100 - 160, account.value() - card.amount() + item2.getCurrAmount() + item1.getCurrAmount());
	}
	
	@Test
	public void remainTest3() throws BadBudgetInvalidValueException {
		
		//22 days
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 12);
		Calendar endCal = new GregorianCalendar(2017, Calendar.FEBRUARY, 2);
		
		BadBudgetData bbd = new BadBudgetData();
		
		Account account = new Account("Account", 50, false);
		CreditCard card = new CreditCard("Card", 100, false, 0);
		MoneyLoss loss = new MoneyLoss("cable", 160, Frequency.monthly, startCal.getTime(), null, card);
		
		Budget budget = new Budget(card, true, Calendar.SUNDAY, 1);
		BudgetItem item1 = new BudgetItem("grocery", 35, Frequency.weekly, 
				startCal.getTime(), null, false, account);
		item1.setRemainAmountAction(RemainAmountAction.addBack);
		
		BudgetItem item2 = new BudgetItem("snack", 3, Frequency.daily, startCal.getTime(), null, false, account);
		item2.setRemainAmountAction(RemainAmountAction.disappear);
		
		budget.addBudgetItem(item1);
		budget.addBudgetItem(item2);
		
		bbd.addAccount(account);
		bbd.addDebt(card);
		bbd.addLoss(loss);
		bbd.setBudget(budget);
		
		Prediction.update(bbd, startCal.getTime(), endCal.getTime());
		
		int dayIndex = 
				Prediction.numDaysBetween(startCal.getTime(), endCal.getTime());
		
		assertTrueHelper("3", 50 -100 - 160 - 3*21, account.value() - card.amount() + item2.getCurrAmount() + item1.getCurrAmount());
	}
	
	@Test
	public void remainTestGain4() throws BadBudgetInvalidValueException {
		
		//13 months
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 12);
		Calendar endCal = new GregorianCalendar(2018, Calendar.FEBRUARY, 2);
		
		BadBudgetData bbd = new BadBudgetData();
		
		Account account = new Account("Account", 50, false);
		CreditCard card = new CreditCard("Card", 100, false, 0);
		MoneyLoss loss1 = new MoneyLoss("cable", 160, Frequency.monthly, startCal.getTime(), null, account);
		MoneyLoss loss2 = new MoneyLoss("weird phone", 50, Frequency.monthly, startCal.getTime(), null, card);
		MoneyGain gain = new MoneyGain("gain", 500, Frequency.monthly, startCal.getTime(), null, account);
		
		Budget budget = new Budget(card, true, Calendar.SUNDAY, 1);
		BudgetItem item1 = new BudgetItem("grocery", 35, Frequency.weekly, 
				startCal.getTime(), null, false, account);
		item1.setRemainAmountAction(RemainAmountAction.addBack);
		
		BudgetItem item2 = new BudgetItem("snack", 3, Frequency.daily, startCal.getTime(), null, false, account);
		item2.setRemainAmountAction(RemainAmountAction.addBack);
		
		BudgetItem item3 = new BudgetItem("necessity", 30, Frequency.biWeekly, startCal.getTime(), null, false, account);
		item3.setRemainAmountAction(RemainAmountAction.addBack);
		
		budget.addBudgetItem(item1);
		budget.addBudgetItem(item2);
		budget.addBudgetItem(item3);
		
		bbd.addAccount(account);
		bbd.addDebt(card);
		bbd.addLoss(loss1);
		bbd.addLoss(loss2);
		bbd.addGain(gain);
		bbd.setBudget(budget);
		
		Prediction.update(bbd, startCal.getTime(), endCal.getTime());
		
		int dayIndex = 
				Prediction.numDaysBetween(startCal.getTime(), endCal.getTime());
		
		PredictDataBudgetItem pdbi1 = item1.getPredictData(dayIndex);
		PredictDataBudgetItem pdbi2 = item2.getPredictData(dayIndex);
		
		assertTrueHelper("4", 50 - 100 - 13*160 + 13*500 - 50*13, account.value() - card.amount() + item1.getCurrAmount() + item2.getCurrAmount() + item3.getCurrAmount());
	}
	
	@Test
	public void remainTestDebtPay5() throws BadBudgetInvalidValueException {
		
		//13 months
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 12);
		Calendar endCal = new GregorianCalendar(2018, Calendar.FEBRUARY, 2);
		
		BadBudgetData bbd = new BadBudgetData();
		
		Account account = new Account("Account", 50, false);
		CreditCard card = new CreditCard("Card", 100, false, 0);
		Payment payment = new Payment(25, false, Frequency.monthly, account, startCal.getTime(), true, null, card, null);
		card.setupPayment(payment);
		
		MoneyLoss loss1 = new MoneyLoss("cable", 160, Frequency.monthly, startCal.getTime(), null, account);
		MoneyLoss loss2 = new MoneyLoss("weird phone", 50, Frequency.monthly, startCal.getTime(), null, card);
		MoneyGain gain = new MoneyGain("gain", 500, Frequency.monthly, startCal.getTime(), null, account);
		
		Budget budget = new Budget(card, true, Calendar.SUNDAY, 1);
		
		BudgetItem item1 = new BudgetItem("grocery", 35, Frequency.weekly, startCal.getTime(), null, false, card);
		item1.setRemainAmountAction(RemainAmountAction.addBack);
		
		BudgetItem item2 = new BudgetItem("snack", 3, Frequency.daily, startCal.getTime(), null, false, card);
		item2.setRemainAmountAction(RemainAmountAction.addBack);
		
		BudgetItem item3 = new BudgetItem("necessity", 30, Frequency.biWeekly, startCal.getTime(), null, false, card);
		item3.setRemainAmountAction(RemainAmountAction.addBack);
		
		budget.addBudgetItem(item1);
		budget.addBudgetItem(item2);
		budget.addBudgetItem(item3);
		
		bbd.addAccount(account);
		bbd.addDebt(card);
		bbd.addLoss(loss1);
		bbd.addLoss(loss2);
		bbd.addGain(gain);
		bbd.setBudget(budget);
		
		Prediction.update(bbd, startCal.getTime(), endCal.getTime());
		
		int dayIndex = 
				Prediction.numDaysBetween(startCal.getTime(), endCal.getTime());
		//printResults(card, dayIndex);
		assertTrueHelper("5", 50 - 100 - 13*160 + 13*500 - 50*13, account.value() - card.amount() + item1.getCurrAmount() + item2.getCurrAmount() + item3.getCurrAmount());
	}
	
	private void assertTrueHelper(String id, double expected, double got)
	{
		assertTrue(id + ":Expected " + expected + " Got " + got, expected == got);
	}
	
	//Can print history of account and debts. Also a history of budget items changes each day in its remaining value
		private static void printResults(Object badBudgetObject, int dayIndex)
		{
			if (badBudgetObject instanceof MoneyOwed)
			{
				for (int i = 0; i <= dayIndex; i++)
				{
					MoneyOwed debt = (MoneyOwed) badBudgetObject;
					PredictDataMoneyOwed pdmo = debt.getPredictData(i);
					for (TransactionHistoryItem historyItem : pdmo.transactionHistory())
					{
						String historyString = historyItem.getTransactionDate().toString() + ": " + historyItem.getTransactionAmount() + " " +
								historyItem.getSourceActionString() + " " + historyItem.getTransactionSource();
						if (historyItem.isSourceCanShowChange())
						{
							historyString += "(" + historyItem.getSourceOriginal() + "->" + historyItem.getSourceUpdated() + ")";
						}
						historyString += historyItem.getDestinationActionString() + " " + historyItem.getTransactionDestination();
						if (historyItem.isDestinationCanShowChange())
						{
							historyString += "(" + historyItem.getDestinationOriginal() + "->" + historyItem.getDestinationUpdated() + ")";
						}
						System.out.println(historyString);
					}
				}
			}
			else if (badBudgetObject instanceof Account)
			{
				for (int i = 0; i <= dayIndex; i++)
				{
					Account account = (Account) badBudgetObject;
					PredictDataAccount pda = account.getPredictData(i);
					for (TransactionHistoryItem historyItem : pda.transactionHistory())
					{
						String historyString = historyItem.getTransactionDate().toString() + ": " + historyItem.getTransactionAmount() + " " +
								historyItem.getSourceActionString() + " " + historyItem.getTransactionSource();
						if (historyItem.isSourceCanShowChange())
						{
							historyString += "(" + historyItem.getSourceOriginal() + "->" + historyItem.getSourceUpdated() + ")";
						}
						historyString += historyItem.getDestinationActionString() + " " + historyItem.getTransactionDestination();
						if (historyItem.isDestinationCanShowChange())
						{
							historyString += "(" + historyItem.getDestinationOriginal() + "->" + historyItem.getDestinationUpdated() + ")";
						}
						System.out.println(historyString);
					}
				}
			}
			else if (badBudgetObject instanceof BudgetItem)
			{
				for (int i = 0; i <= dayIndex; i++)
				{
					BudgetItem item = (BudgetItem) badBudgetObject;
					PredictDataBudgetItem pdbi = item.getPredictData(i);
					
					String change = pdbi.date() + " " + item.expenseDescription() + " :" + pdbi.getOriginalAmount() + "->" + pdbi.getUpdatedAmount();
					System.out.println(change);
				}
			}
		}
}
