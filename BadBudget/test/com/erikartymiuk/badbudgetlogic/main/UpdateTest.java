package com.erikartymiuk.badbudgetlogic.main;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.erikartymiuk.badbudgetlogic.budget.Budget;
import com.erikartymiuk.badbudgetlogic.budget.BudgetItem;

public class UpdateTest {

	@Test
	public void updateSimpleSavingsOnly() throws BadBudgetInvalidValueException 
	{
		Account account = new Account("Account", 500, false);
		Contribution contribution = new Contribution(1, Frequency.daily);
		Date startDate = new GregorianCalendar(2016, Calendar.SEPTEMBER, 1).getTime();
		SavingsAccount savingsAccount = new SavingsAccount("Savings", 0, false, false, -1, null, contribution, account, startDate, null, true, 0);
		
		BadBudgetData bbd = new BadBudgetData();
		Budget budget = new Budget(account, false, Calendar.SUNDAY, 1);
		
		bbd.addAccount(account);
		bbd.addAccount(savingsAccount);
		bbd.setBudget(budget);
		
		Date currDate = new GregorianCalendar(2016, Calendar.SEPTEMBER, 1).getTime();
		Date endDate = new GregorianCalendar(2016, Calendar.SEPTEMBER, 1).getTime();

		Prediction.update(bbd, currDate, endDate);
		
		assertTrue("Same day update failed expected value of 499/1 but got " + account.value() + "/" + savingsAccount.value(), 
				account.value() == 499 && savingsAccount.value() == 1);
		
		Prediction.update(bbd, currDate, endDate);
		
		assertTrue("Same day update 2 failed: expected value of 499/1 but got " + account.value() + "/" + savingsAccount.value(), 
				account.value() == 499 && savingsAccount.value() == 1);
		
		endDate = new GregorianCalendar(2016, Calendar.SEPTEMBER, 4).getTime();
		Prediction.update(bbd, currDate, endDate);
		
		assertTrue("Sept 4 update failed: expected value of 496/4 but got " + account.value() + "/" + savingsAccount.value(), 
				account.value() == 496 && savingsAccount.value() == 4);
		
		Date startCurrDate = new GregorianCalendar(2016, Calendar.SEPTEMBER, 4).getTime();
		
		for (int i = 1; i < 365; i++)
		{
			Calendar temp = Calendar.getInstance();
			temp.setTime(startCurrDate);
			temp.add(Calendar.DAY_OF_YEAR, 1);
			startCurrDate = temp.getTime();;
			Prediction.update(bbd, startCurrDate, startCurrDate);
			assertTrue(i + " day(s) update failed: expected value of " + (496-i) + "/" + (4+i)+ " but got " + account.value() + "/" + savingsAccount.value(), 
					account.value() == 496-i && savingsAccount.value() == 4+i);
			
		}
		
		currDate = startCurrDate;
		Calendar temp = Calendar.getInstance();
		temp.setTime(currDate);
		temp.add(Calendar.YEAR, 1);
		endDate = temp.getTime();
		Prediction.update(bbd, currDate, endDate);
		
		double expectedValueAccount = 496-364-365;
		double expectedValueSavings = 4+364+365;
		
		assertTrue("One year update failed: expected value of -233/733 but got " + account.value() + "/" + savingsAccount.value(), 
				account.value() == expectedValueAccount && savingsAccount.value() == expectedValueSavings);
	}
	
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
		
		Prediction.update(bbd, startCurrDate, startCurrDate);
		double expectedValue = 1000 - 10 - 150 - 50;
		double actualValue = a1.value();
		assertTrue("day one fail a1 value expected " + expectedValue + " got " + actualValue, expectedValue == actualValue);
		
		expectedValue = 10000 - 50 - 300 - 500;
		actualValue = a2.value();
		assertTrue("day one fail a2 value expected " + expectedValue + " got " + actualValue, expectedValue == actualValue);
		
		Calendar temp = Calendar.getInstance();
		temp.setTime(startCurrDate);
		temp.add(Calendar.DAY_OF_YEAR, 1);
		Date dayTwo = temp.getTime();
		Prediction.update(bbd, startCurrDate, dayTwo);
		
		expectedValue = 1000 - 10 - 150 - 50 - 10;
		actualValue = a1.value();
		assertTrue("day two fail a1 value expected " + expectedValue + " got " + actualValue, expectedValue == actualValue);
		
		Date april28 = new GregorianCalendar(2017, Calendar.APRIL, 28).getTime();
		Prediction.update(bbd, dayTwo, april28);
		//a1 starts with a value of 0
		//On april 20th we reach our goal of 5000 for our savings account. Meaning 5100 was taken out of a1 to reach that.
		//We've passed oct 1 so 10000 one time gain was made to a1
		//There are 17 biweeks between start and end (17th is on April 27th) so the loan had 17*50 paid from a1
		//Also 17 gains of 1000
		//There are 240 days between the start and end so a1 losses 240*10
		//The last payment to the credit card occurs on the 28th so we remove the total balance from a1:
			//750 + 100*8 + 75*15 + 25 * 34 + (3.0/7)*25
		double creditCard = 750 + 100 * 8 + 75 * 15 + 25 * 34 + (3.0/7)*25;
		expectedValue = 0 - 5100 + 10000 - 18*50 + 18*1000 - 240*10 - (creditCard);
		actualValue = a1.value();
		assertTrue("April 28th fail, a1 value expected " + expectedValue + " got " + actualValue, expectedValue == actualValue);
	}
	
	@Test
	public void updatePredictMatchTest() throws BadBudgetInvalidValueException
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
		
		//Run 1000 days, updating 1 day at a time
		Calendar currCalendar = Calendar.getInstance();
		currCalendar.setTime(startCurrDate);
		Date lastEnd = startCurrDate;
		
		for (int currDay = 0; currDay < 1000; currDay++)
		{
			int dayIndex = Prediction.numDaysBetween(lastEnd, currCalendar.getTime());
			
			Prediction.predict(bbd, lastEnd, currCalendar.getTime());
			double account1Value = a1.getPredictData(dayIndex).value();
			double account2Value = a2.getPredictData(dayIndex).value();
			double savingsAccount1Value = sa1.getPredictData(dayIndex).value();
			double savingsAccount2Value = sa2.getPredictData(dayIndex).value();
			double savingsAccount3Value = sa3.getPredictData(dayIndex).value();
			double debt1Value = d1.getPredictData(dayIndex).value();
			double creditCard1Value = cc1.getPredictData(dayIndex).value();
			double loan1Value = l1.getPredictData(dayIndex).value();
			Date gain1NextDeposit = g1.getPredictData(dayIndex).nextDeposit();
			Date gain2NextDeposit = g2.getPredictData(dayIndex).nextDeposit();
			Date loss1Next = loss1.getPredictData(dayIndex).nextLoss();
			Date loss2Next = loss2.getPredictData(dayIndex).nextLoss();
			
			Prediction.update(bbd, lastEnd, currCalendar.getTime());
			
			assertTrueHelper("1", account1Value, a1.value());
			assertTrueHelper("2", account2Value, a2.value());
			assertTrueHelper("3", savingsAccount1Value, sa1.value());
			assertTrueHelper("4", savingsAccount2Value, sa2.value());
			assertTrueHelper("5", savingsAccount3Value, sa3.value());
			assertTrueHelper("6", debt1Value, d1.amount());
			assertTrueHelper("7", creditCard1Value, cc1.amount());
			assertTrueHelper("8", loan1Value, l1.amount());
			assertTrueHelper("9", gain1NextDeposit, g1.nextDeposit());
			assertTrueHelper("10", gain2NextDeposit, g2.nextDeposit());
			assertTrueHelper("11", loss1Next, loss1.nextLoss());
			assertTrueHelper("12", loss2Next, loss2.nextLoss());

			lastEnd = currCalendar.getTime();
			currCalendar.add(Calendar.DAY_OF_YEAR, 1);
		}
	}
	
	private void assertTrueHelper(String id, double expected, double got)
	{
		assertTrue(id + ":Expected " + expected + " Got " + got, expected == got);
	}
	
	private void assertTrueHelper(String id, Date expected, Date got)
	{
		if (expected == null || got == null)
		{
			assertTrue(id + ":Expected " + expected + " Got "+ got, expected == got);
		}
		else
		{
			assertTrue(id + ":Expected " + expected + " Got" + got, Prediction.datesEqualUpToDay(expected, got));
		}
	}
	
	@Test
	public void updatePredictMatchTest2() throws BadBudgetInvalidValueException
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
		
		Calendar endCalCalc = Calendar.getInstance();
		endCalCalc.setTime(startCurrDate);
		endCalCalc.add(Calendar.DAY_OF_YEAR, 365*10);
		Prediction.predict(bbd, startCurrDate, endCalCalc.getTime());
		
		Account ua1 = new Account("a1", 0, false);
		Account ua2 = new Account("a2", 10000, false);
		
		//$150 weekly contribution from a1 until hit 5000
		SavingsAccount usa1 = new SavingsAccount("sa1", 0, false, true, 5000, Prediction.findGoalDate(startCurrDate, new Contribution(150, Frequency.weekly), 0, 5000), 
				new Contribution(150, Frequency.weekly), ua1, startCurrDate, Prediction.findGoalDate(startCurrDate, new Contribution(150, Frequency.weekly), 0, 5000), false, 0);
		//$50 contribution from a2 one time
		SavingsAccount usa2 = new SavingsAccount("sa2", 50, false, false, -1, null, 
				new Contribution(50, Frequency.oneTime), ua2, startCurrDate, startCurrDate, false, 0);
		//$25 contribution from a2 daily. Start Sept 15th
		SavingsAccount usa3 = new SavingsAccount("sa3", 0, false, false, -1, null, 
				new Contribution(25, Frequency.daily), ua2, new GregorianCalendar(2016, Calendar.SEPTEMBER, 15).getTime(), null, true, 0);
		
		//$300 monthly payment from a2 toward $7500 generic debt until payed off
		MoneyOwed ud1 = new MoneyOwed("d1", 7500, false, 0);
		Payment up1 = new Payment(300, false, Frequency.monthly, ua2, startCurrDate, false, Prediction.findGoalDate(startCurrDate, 300, Frequency.monthly, 7500, null), ud1, Prediction.findGoalDate(startCurrDate, 300, Frequency.monthly, 7500, null));
		ud1.setupPayment(up1);
		
		//$-1 payed off credit card every week from a1, starts on sept 16, debt start is $750
		CreditCard ucc1 = new CreditCard("cc1", 750, false, 0);
		Payment up2 = new Payment(-1, true, Frequency.weekly, ua1, new GregorianCalendar(2016, Calendar.SEPTEMBER, 16).getTime(), true, null, ucc1, null);
		ucc1.setupPayment(up2);
		
		//$50 biweekly payment from a1 to loan at $1250. Ongoing.
		Loan ul1 = new Loan("l1", 1250, false, 0, false, 1250);
		Payment up3 = new Payment(50, false, Frequency.biWeekly, ua1, startCurrDate, true, null, ul1, null);
		ul1.setupPayment(up3);
		
		//$1000 biweekly gain to a1
		MoneyGain ug1 = new MoneyGain("g1", 1000, Frequency.biWeekly, startCurrDate, null, ua1);
		
		//$10000 one time gain to a1 on Oct 1
		MoneyGain ug2 = new MoneyGain("g2", 10000, Frequency.oneTime, new GregorianCalendar(2016, Calendar.OCTOBER, 1).getTime(), new GregorianCalendar(2016, Calendar.OCTOBER, 1).getTime(), ua1);
		
		//$10 daily loss from a1
		MoneyLoss uloss1 = new MoneyLoss("loss1", 10, Frequency.daily, startCurrDate, null, ua1);
		
		//$500 monthly loss from a2, ends on oct 15 2017
		MoneyLoss uloss2 = new MoneyLoss("loss2", 500, Frequency.monthly, startCurrDate, new GregorianCalendar(2017, Calendar.OCTOBER, 15).getTime(), ua2);
		
		//Budget with source of cc1, resets at 12AM, Sunday, and the 1st
		Budget ubudget = new Budget(ucc1, true, Calendar.SUNDAY, 1);
		
		//$100 monthly ongoing
		BudgetItem ui1 = new BudgetItem("i1", 100, Frequency.monthly, startCurrDate, null, false, ucc1);
		//$25 weekly ends Oct 19 2017, prorated start
		BudgetItem ui2 = new BudgetItem("i2", 25, Frequency.weekly, startCurrDate, new GregorianCalendar(2017, Calendar.OCTOBER, 19).getTime(), true, ucc1);
		//$75 biweekly start Oct 9 ongoing
		BudgetItem ui3 = new BudgetItem("i3", 75, Frequency.biWeekly, new GregorianCalendar(2016, Calendar.OCTOBER, 9).getTime(), null, false, ucc1);
		
		ubudget.addBudgetItem(ui1);
		ubudget.addBudgetItem(ui2);
		ubudget.addBudgetItem(ui3);
		
		BadBudgetData ubbd = new BadBudgetData();
		ubbd.addAccount(ua1);
		ubbd.addAccount(ua2);
		ubbd.addAccount(usa1);
		ubbd.addAccount(usa2);
		ubbd.addAccount(usa3);
		ubbd.addDebt(ud1);
		ubbd.addDebt(ucc1);
		ubbd.addDebt(ul1);
		ubbd.addGain(ug1);
		ubbd.addGain(ug2);
		ubbd.addLoss(uloss1);
		ubbd.addLoss(uloss2);
		ubbd.setBudget(ubudget);
		
		//Run update first day
		int currUpdateDayIndex = 0;
		Prediction.update(ubbd, startCurrDate, startCurrDate);
		
		assertTrueHelper("1", a1.getPredictData(currUpdateDayIndex).value(), ua1.value());
		assertTrueHelper("2", a2.getPredictData(currUpdateDayIndex).value(), ua2.value());
		assertTrueHelper("3", sa1.getPredictData(currUpdateDayIndex).value(), usa1.value());
		assertTrueHelper("4", sa2.getPredictData(currUpdateDayIndex).value(), usa2.value());
		assertTrueHelper("5", sa3.getPredictData(currUpdateDayIndex).value(), usa3.value());
		assertTrueHelper("6", d1.getPredictData(currUpdateDayIndex).value(), ud1.amount());
		assertTrueHelper("7", cc1.getPredictData(currUpdateDayIndex).value(), ucc1.amount());
		assertTrueHelper("8", l1.getPredictData(currUpdateDayIndex).value(), ul1.amount());
		assertTrueHelper("9", g1.getPredictData(currUpdateDayIndex).nextDeposit(), ug1.nextDeposit());
		assertTrueHelper("10", g2.getPredictData(currUpdateDayIndex).nextDeposit(), ug2.nextDeposit());
		assertTrueHelper("11", loss1.getPredictData(currUpdateDayIndex).nextLoss(), uloss1.nextLoss());
		assertTrueHelper("12", loss2.getPredictData(currUpdateDayIndex).nextLoss(), uloss2.nextLoss());
		
		//Update 2nd day
		currUpdateDayIndex = 1;
		Calendar endCalCalc2 = Calendar.getInstance();
		endCalCalc2.setTime(startCurrDate);
		endCalCalc2.add(Calendar.DAY_OF_YEAR, 1);
		Prediction.update(ubbd, startCurrDate, endCalCalc2.getTime());
		
		assertTrueHelper("1", a1.getPredictData(currUpdateDayIndex).value(), ua1.value());
		assertTrueHelper("2", a2.getPredictData(currUpdateDayIndex).value(), ua2.value());
		assertTrueHelper("3", sa1.getPredictData(currUpdateDayIndex).value(), usa1.value());
		assertTrueHelper("4", sa2.getPredictData(currUpdateDayIndex).value(), usa2.value());
		assertTrueHelper("5", sa3.getPredictData(currUpdateDayIndex).value(), usa3.value());
		assertTrueHelper("6", d1.getPredictData(currUpdateDayIndex).value(), ud1.amount());
		assertTrueHelper("7", cc1.getPredictData(currUpdateDayIndex).value(), ucc1.amount());
		assertTrueHelper("8", l1.getPredictData(currUpdateDayIndex).value(), ul1.amount());
		assertTrueHelper("9", g1.getPredictData(currUpdateDayIndex).nextDeposit(), ug1.nextDeposit());
		assertTrueHelper("10", g2.getPredictData(currUpdateDayIndex).nextDeposit(), ug2.nextDeposit());
		assertTrueHelper("11", loss1.getPredictData(currUpdateDayIndex).nextLoss(), uloss1.nextLoss());
		assertTrueHelper("12", loss2.getPredictData(currUpdateDayIndex).nextLoss(), uloss2.nextLoss());
		
		//update every 10th day
		for (int i = 0; i < 40; i++)
		{
			currUpdateDayIndex = currUpdateDayIndex+10;
			Date lastEnd = endCalCalc2.getTime();
			endCalCalc2.add(Calendar.DAY_OF_YEAR, 10);
			Prediction.update(ubbd, lastEnd, endCalCalc2.getTime());
			
			assertTrueHelper("1", a1.getPredictData(currUpdateDayIndex).value(), ua1.value());
			assertTrueHelper("2", a2.getPredictData(currUpdateDayIndex).value(), ua2.value());
			assertTrueHelper("3", sa1.getPredictData(currUpdateDayIndex).value(), usa1.value());
			assertTrueHelper("4", sa2.getPredictData(currUpdateDayIndex).value(), usa2.value());
			assertTrueHelper("5", sa3.getPredictData(currUpdateDayIndex).value(), usa3.value());
			assertTrueHelper("6", d1.getPredictData(currUpdateDayIndex).value(), ud1.amount());
			assertTrueHelper("7", cc1.getPredictData(currUpdateDayIndex).value(), ucc1.amount());
			assertTrueHelper("8", l1.getPredictData(currUpdateDayIndex).value(), ul1.amount());
			assertTrueHelper("9", g1.getPredictData(currUpdateDayIndex).nextDeposit(), ug1.nextDeposit());
			assertTrueHelper("10", g2.getPredictData(currUpdateDayIndex).nextDeposit(), ug2.nextDeposit());
			assertTrueHelper("11", loss1.getPredictData(currUpdateDayIndex).nextLoss(), uloss1.nextLoss());
			assertTrueHelper("12", loss2.getPredictData(currUpdateDayIndex).nextLoss(), uloss2.nextLoss());
		}
		
		//update every 5th day
		for (int i = 0; i < 40; i++)
		{
			currUpdateDayIndex = currUpdateDayIndex+5;
			Date lastEnd = endCalCalc2.getTime();
			endCalCalc2.add(Calendar.DAY_OF_YEAR, 5);
			Prediction.update(ubbd, lastEnd, endCalCalc2.getTime());
			
			assertTrueHelper("1", a1.getPredictData(currUpdateDayIndex).value(), ua1.value());
			assertTrueHelper("2", a2.getPredictData(currUpdateDayIndex).value(), ua2.value());
			assertTrueHelper("3", sa1.getPredictData(currUpdateDayIndex).value(), usa1.value());
			assertTrueHelper("4", sa2.getPredictData(currUpdateDayIndex).value(), usa2.value());
			assertTrueHelper("5", sa3.getPredictData(currUpdateDayIndex).value(), usa3.value());
			assertTrueHelper("6", d1.getPredictData(currUpdateDayIndex).value(), ud1.amount());
			assertTrueHelper("7", cc1.getPredictData(currUpdateDayIndex).value(), ucc1.amount());
			assertTrueHelper("8", l1.getPredictData(currUpdateDayIndex).value(), ul1.amount());
			assertTrueHelper("9", g1.getPredictData(currUpdateDayIndex).nextDeposit(), ug1.nextDeposit());
			assertTrueHelper("10", g2.getPredictData(currUpdateDayIndex).nextDeposit(), ug2.nextDeposit());
			assertTrueHelper("11", loss1.getPredictData(currUpdateDayIndex).nextLoss(), uloss1.nextLoss());
			assertTrueHelper("12", loss2.getPredictData(currUpdateDayIndex).nextLoss(), uloss2.nextLoss());
		}
		
		//update every day
		for (int i = 0; i < 400; i++)
		{
			currUpdateDayIndex = currUpdateDayIndex+1;
			Date lastEnd = endCalCalc2.getTime();
			endCalCalc2.add(Calendar.DAY_OF_YEAR, 1);
			Prediction.update(ubbd, lastEnd, endCalCalc2.getTime());
			
			assertTrueHelper("1", a1.getPredictData(currUpdateDayIndex).value(), ua1.value());
			assertTrueHelper("2", a2.getPredictData(currUpdateDayIndex).value(), ua2.value());
			assertTrueHelper("3", sa1.getPredictData(currUpdateDayIndex).value(), usa1.value());
			assertTrueHelper("4", sa2.getPredictData(currUpdateDayIndex).value(), usa2.value());
			assertTrueHelper("5", sa3.getPredictData(currUpdateDayIndex).value(), usa3.value());
			assertTrueHelper("6", d1.getPredictData(currUpdateDayIndex).value(), ud1.amount());
			assertTrueHelper("7", cc1.getPredictData(currUpdateDayIndex).value(), ucc1.amount());
			assertTrueHelper("8", l1.getPredictData(currUpdateDayIndex).value(), ul1.amount());
			assertTrueHelper("9", g1.getPredictData(currUpdateDayIndex).nextDeposit(), ug1.nextDeposit());
			assertTrueHelper("10", g2.getPredictData(currUpdateDayIndex).nextDeposit(), ug2.nextDeposit());
			assertTrueHelper("11", loss1.getPredictData(currUpdateDayIndex).nextLoss(), uloss1.nextLoss());
			assertTrueHelper("12", loss2.getPredictData(currUpdateDayIndex).nextLoss(), uloss2.nextLoss());
		}
		
		//update every 30 days
		for (int i = 0; i < 85; i++)
		{
			currUpdateDayIndex = currUpdateDayIndex+30;
			Date lastEnd = endCalCalc2.getTime();
			endCalCalc2.add(Calendar.DAY_OF_YEAR, 30);
			Prediction.update(ubbd, lastEnd, endCalCalc2.getTime());
			
			assertTrueHelper("1", a1.getPredictData(currUpdateDayIndex).value(), ua1.value());
			assertTrueHelper("2", a2.getPredictData(currUpdateDayIndex).value(), ua2.value());
			assertTrueHelper("3", sa1.getPredictData(currUpdateDayIndex).value(), usa1.value());
			assertTrueHelper("4", sa2.getPredictData(currUpdateDayIndex).value(), usa2.value());
			assertTrueHelper("5", sa3.getPredictData(currUpdateDayIndex).value(), usa3.value());
			assertTrueHelper("6", d1.getPredictData(currUpdateDayIndex).value(), ud1.amount());
			assertTrueHelper("7", cc1.getPredictData(currUpdateDayIndex).value(), ucc1.amount());
			assertTrueHelper("8", l1.getPredictData(currUpdateDayIndex).value(), ul1.amount());
			assertTrueHelper("9", g1.getPredictData(currUpdateDayIndex).nextDeposit(), ug1.nextDeposit());
			assertTrueHelper("10", g2.getPredictData(currUpdateDayIndex).nextDeposit(), ug2.nextDeposit());
			assertTrueHelper("11", loss1.getPredictData(currUpdateDayIndex).nextLoss(), uloss1.nextLoss());
			assertTrueHelper("12", loss2.getPredictData(currUpdateDayIndex).nextLoss(), uloss2.nextLoss());
		}
	}

}
