package com.erikartymiuk.badbudgetlogic.main;

import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.erikartymiuk.badbudgetlogic.budget.Budget;
import com.erikartymiuk.badbudgetlogic.budget.BudgetItem;
import com.erikartymiuk.badbudgetlogic.budget.RemainAmountAction;

public class UpdateNextDateOnlyTest {
	
	//AutoUpdate & AutoReset
	@Test
	public void test1() throws BadBudgetInvalidValueException {
		
		BadBudgetData bbd = new BadBudgetData();
		boolean autoReset = true;
		
		//3 months, 8 weeks
		Calendar startCal = new GregorianCalendar(2000, Calendar.MARCH, 12);
		Calendar endCal = new GregorianCalendar(2000, Calendar.MAY, 13);
		
		Account sourceAccount = new Account("Source Account", 0, false);
		Budget budget = new Budget(sourceAccount, autoReset, Calendar.SUNDAY, 1);
		BudgetItem item = new BudgetItem("Grocery", 50, Frequency.weekly, startCal.getTime(), endCal.getTime(), false, sourceAccount);
		item.setCurrAmount(35);
		item.setRemainAmountAction(RemainAmountAction.disappear);
		budget.addBudgetItem(item);
		
		bbd.setBudget(budget);
		bbd.addAccount(sourceAccount);
		
		Prediction.update(bbd, startCal.getTime(), endCal.getTime());
		
		assertTrueHelper("1", 50, item.getCurrAmount());
		assertTrueHelper("1", -50*9, sourceAccount.value());
	}
	
	//AutoReset
	@Test
	public void test2() throws BadBudgetInvalidValueException {
		
		BadBudgetData bbd = new BadBudgetData();
		boolean autoReset = true;
		
		//3 months, 8 weeks
		Calendar startCal = new GregorianCalendar(2000, Calendar.MARCH, 12);
		Calendar endCal = new GregorianCalendar(2000, Calendar.MAY, 13);
		
		Account sourceAccount = new Account("Source Account", 0, false);
		Budget budget = new Budget(sourceAccount, autoReset, Calendar.SUNDAY, 1);
		BudgetItem item = new BudgetItem("Grocery", 50, Frequency.weekly, startCal.getTime(), endCal.getTime(), false, sourceAccount);
		item.setCurrAmount(35);
		item.setRemainAmountAction(RemainAmountAction.disappear);
		budget.addBudgetItem(item);
		
		bbd.setBudget(budget);
		bbd.addAccount(sourceAccount);
		
		Prediction.updateNextDatesOnly(bbd, startCal.getTime(), endCal.getTime());
		
		assertTrueHelper("2", 50, item.getCurrAmount());
		assertTrueHelper("2", 0, sourceAccount.value());
	}
	
	//AutoUpdate
	@Test
	public void test3() throws BadBudgetInvalidValueException {
		
		BadBudgetData bbd = new BadBudgetData();
		boolean autoReset = false;
		
		//3 months, 8 weeks
		Calendar startCal = new GregorianCalendar(2000, Calendar.MARCH, 12);
		Calendar endCal = new GregorianCalendar(2000, Calendar.MAY, 13);
		
		Account sourceAccount = new Account("Source Account", 0, false);
		Budget budget = new Budget(sourceAccount, autoReset, Calendar.SUNDAY, 1);
		BudgetItem item = new BudgetItem("Grocery", 50, Frequency.weekly, startCal.getTime(), endCal.getTime(), false, sourceAccount);
		item.setCurrAmount(35);
		item.setRemainAmountAction(RemainAmountAction.disappear);
		budget.addBudgetItem(item);
		
		bbd.setBudget(budget);
		bbd.addAccount(sourceAccount);
		
		Prediction.update(bbd, startCal.getTime(), endCal.getTime());
		
		assertTrueHelper("3", 35, item.getCurrAmount());
		assertTrueHelper("3", -50*9, sourceAccount.value());
	}
	
	//Neither
	@Test
	public void test4() throws BadBudgetInvalidValueException {
		
		BadBudgetData bbd = new BadBudgetData();
		boolean autoReset = false;
		
		//3 months, 8 weeks
		Calendar startCal = new GregorianCalendar(2000, Calendar.MARCH, 12);
		Calendar endCal = new GregorianCalendar(2000, Calendar.MAY, 13);
		
		Account sourceAccount = new Account("Source Account", 0, false);
		Budget budget = new Budget(sourceAccount, autoReset, Calendar.SUNDAY, 1);
		BudgetItem item = new BudgetItem("Grocery", 50, Frequency.weekly, startCal.getTime(), endCal.getTime(), false, sourceAccount);
		item.setCurrAmount(35);
		item.setRemainAmountAction(RemainAmountAction.disappear);
		budget.addBudgetItem(item);
		
		bbd.setBudget(budget);
		bbd.addAccount(sourceAccount);
		
		Prediction.updateNextDatesOnly(bbd, startCal.getTime(), endCal.getTime());
		
		assertTrueHelper("4", 35, item.getCurrAmount());
		assertTrueHelper("4", 0, sourceAccount.value());
	}
	
	//Copied test that is modified
	@Test
	public void updateFullerTest() throws BadBudgetInvalidValueException
	{
		Date startCurrDate = new GregorianCalendar(2016, Calendar.SEPTEMBER, 1).getTime();
		
		Account a1 = new Account("a1", 0, false);
		Account a2 = new Account("a2", 10000, false);
		
		//$150 weekly contribution from a1 until hit 5000
		SavingsAccount sa1 = new SavingsAccount("sa1", 0, false, true, 5000, Prediction.findGoalDate(startCurrDate, new Contribution(150, Frequency.weekly), 0, 5000), 
				new Contribution(150, Frequency.weekly), a1, startCurrDate, Prediction.findGoalDate(startCurrDate, new Contribution(150, Frequency.weekly), 0, 5000), false, 0);
		//$50 contribution from a2 one time
		SavingsAccount sa2 = new SavingsAccount("sa2", 50, false, false, -1, null, 
				new Contribution(50, Frequency.oneTime), a2, startCurrDate, startCurrDate, false, 0);
		//$25 contribution from a2 daily. Start Sept 15th
		SavingsAccount sa3 = new SavingsAccount("sa3", 0, false, false, -1, null, 
				new Contribution(25, Frequency.daily), a2, new GregorianCalendar(2016, Calendar.SEPTEMBER, 15).getTime(), null, true, 0);
		
		//$300 monthly payment from a2 toward $7500 generic debt until payed off
		MoneyOwed d1 = new MoneyOwed("d1", 7500, false, 0);
		Payment p1 = new Payment(300, false, Frequency.monthly, a2, startCurrDate, false, Prediction.findGoalDate(startCurrDate, 300, Frequency.monthly, 7500, null), d1, Prediction.findGoalDate(startCurrDate, 300, Frequency.monthly, 7500, null));
		d1.setupPayment(p1);
		
		//$-1 payed off credit card every week from a1, starts on sept 16, debt start is $750
		CreditCard cc1 = new CreditCard("cc1", 750, false, 0);
		Payment p2 = new Payment(-1, true, Frequency.weekly, a1, new GregorianCalendar(2016, Calendar.SEPTEMBER, 16).getTime(), true, null, cc1, null);
		cc1.setupPayment(p2);
		
		//$50 biweekly payment from a1 to loan at $1250. Ongoing.
		Loan l1 = new Loan("l1", 1250, false, 0, false, 1250);
		Payment p3 = new Payment(50, false, Frequency.biWeekly, a1, startCurrDate, true, null, l1, null);
		l1.setupPayment(p3);
		
		//$1000 biweekly gain to a1
		MoneyGain g1 = new MoneyGain("g1", 1000, Frequency.biWeekly, startCurrDate, null, a1);
		
		//$10000 one time gain to a1 on Oct 1
		MoneyGain g2 = new MoneyGain("g2", 10000, Frequency.oneTime, new GregorianCalendar(2016, Calendar.OCTOBER, 1).getTime(), new GregorianCalendar(2016, Calendar.OCTOBER, 1).getTime(), a1);
		
		//$10 daily loss from a1
		MoneyLoss loss1 = new MoneyLoss("loss1", 10, Frequency.daily, startCurrDate, null, a1);
		
		//$500 monthly loss from a2, ends on oct 15 2017
		MoneyLoss loss2 = new MoneyLoss("loss2", 500, Frequency.monthly, startCurrDate, new GregorianCalendar(2017, Calendar.OCTOBER, 15).getTime(), a2);
		
		//Budget with source of cc1, resets at 12AM, Sunday, and the 1st
		Budget budget = new Budget(cc1, true, Calendar.SUNDAY, 1);
		
		//$100 monthly ongoing
		BudgetItem i1 = new BudgetItem("i1", 100, Frequency.monthly, startCurrDate, null, false, cc1);
		//$25 weekly ends Oct 19 2017, prorated start
		BudgetItem i2 = new BudgetItem("i2", 25, Frequency.weekly, startCurrDate, new GregorianCalendar(2017, Calendar.OCTOBER, 19).getTime(), true, cc1);
		//$75 biweekly start Oct 9 ongoing
		BudgetItem i3 = new BudgetItem("i3", 75, Frequency.biWeekly, new GregorianCalendar(2016, Calendar.OCTOBER, 9).getTime(), null, false, cc1);
		
		budget.addBudgetItem(i1);
		budget.addBudgetItem(i2);
		budget.addBudgetItem(i3);
		
		BadBudgetData bbd = new BadBudgetData();
		bbd.addAccount(a1);
		bbd.addAccount(a2);
		bbd.addAccount(sa1);
		bbd.addAccount(sa2);
		bbd.addAccount(sa3);
		bbd.addDebt(d1);
		bbd.addDebt(cc1);
		bbd.addDebt(l1);
		bbd.addGain(g1);
		bbd.addGain(g2);
		bbd.addLoss(loss1);
		bbd.addLoss(loss2);
		bbd.setBudget(budget);
		
		Prediction.updateNextDatesOnly(bbd, startCurrDate, startCurrDate);
		//double expectedValue = 1000 - 10 - 150 - 50;
		double actualValue = a1.value();
		assertTrue("day one fail a1 value expected " + a1.value() + " got " + actualValue, a1.value() == actualValue);
		
		//expectedValue = 10000 - 50 - 300 - 500;
		actualValue = a2.value();
		assertTrue("day one fail a2 value expected " + a2.value() + " got " + actualValue, a2.value() == actualValue);
		
		Calendar temp = Calendar.getInstance();
		temp.setTime(startCurrDate);
		temp.add(Calendar.DAY_OF_YEAR, 1);
		Date dayTwo = temp.getTime();
		Prediction.updateNextDatesOnly(bbd, startCurrDate, dayTwo);
		
		//expectedValue = 1000 - 10 - 150 - 50 - 10;
		actualValue = a1.value();
		assertTrue("day two fail a1 value expected " + a1.value() + " got " + actualValue, a1.value() == actualValue);
		
		Date april28 = new GregorianCalendar(2017, Calendar.APRIL, 28).getTime();
		Prediction.updateNextDatesOnly(bbd, dayTwo, april28);
		//Copied test... comments from copy below...
		
		//a1 starts with a value of 0
		//On april 20th we reach our goal of 5000 for our savings account. Meaning 5100 was taken out of a1 to reach that.
		//We've passed oct 1 so 10000 one time gain was made to a1
		//There are 17 biweeks between start and end (17th is on April 27th) so the loan had 17*50 paid from a1
		//Also 17 gains of 1000
		//There are 240 days between the start and end so a1 losses 240*10
		//The last payment to the credit card occurs on the 28th so we remove the total balance from a1:
			//750 + 100*8 + 75*15 + 25 * 34 + (3.0/7)*25
		double creditCard = 750 + 100 * 8 + 75 * 15 + 25 * 34 + (3.0/7)*25;
		//expectedValue = 0 - 5100 + 10000 - 18*50 + 18*1000 - 240*10 - (creditCard);
		actualValue = a1.value();
		assertTrue("April 28th fail, a1 value expected " + a1.value() + " got " + actualValue, a1.value() == actualValue);
	}

	private void assertTrueHelper(String id, double expected, double got)
	{
		assertTrue(id + ":Expected " + expected + " Got " + got, expected == got);
	}
	
}
