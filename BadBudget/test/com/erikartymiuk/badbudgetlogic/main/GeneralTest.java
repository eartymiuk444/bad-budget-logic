package com.erikartymiuk.badbudgetlogic.main;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.erikartymiuk.badbudgetlogic.budget.Budget;
import com.erikartymiuk.badbudgetlogic.budget.BudgetItem;
import com.erikartymiuk.badbudgetlogic.budget.RemainAmountAction;

public class GeneralTest {
	
	@Test
	public void predictProratedTest() throws BadBudgetInvalidValueException
	{
		BadBudgetData bbd = new BadBudgetData();
		Account source = new Account("Source Account", 1000, false);
		Budget budget = new Budget(source, false, Calendar.SUNDAY, 1);
		
		//Monthly
		Date startDate = new GregorianCalendar(2016, Calendar.AUGUST, 15).getTime();
		BudgetItem item = new BudgetItem("Sample Item", 400, Frequency.monthly, startDate, null, true, source);
		budget.addBudgetItem(item);
		bbd.setBudget(budget);
		bbd.addAccount(source);
		
		Prediction.predict(bbd, startDate, startDate);
		assertTrue("Expected 780 and got "+source.getPredictData(0).value(), (int)source.getPredictData(0).value() == 780);
		bbd.clearPredictData();
		
		//Weekly
		BudgetItem item2 = new BudgetItem("Weekly Item", 200, Frequency.weekly, startDate, null, true, source);
		budget.addBudgetItem(item2);
		Prediction.predict(bbd, startDate, startDate);
		assertTrue("Expected 609 and got "+source.getPredictData(0).value(), (int)source.getPredictData(0).value() == 609);
		bbd.clearPredictData();

		//Yearly - Currently should take out full amount, may later change
		BudgetItem item3 = new BudgetItem("Yearly Item", 1000, Frequency.yearly, startDate, null, true, source);
		budget.addBudgetItem(item3);
		Prediction.predict(bbd, startDate, startDate);
		assertTrue("Expected -390 and got "+source.getPredictData(0).value(), (int)source.getPredictData(0).value() == -390);
	}
	
	@Test
	public void predictFullerTest() throws BadBudgetInvalidValueException
	{
		Account checking = new Account("Checking", 1000, false);
		Date startDate = new GregorianCalendar(2016, Calendar.AUGUST, 23).getTime();
		Contribution contribution = new Contribution(5, Frequency.daily);
		Date goalDate = Prediction.findGoalDate(startDate, contribution, 200, 5000);
		SavingsAccount savings = new SavingsAccount("Savings", 200, false, true, 5000, goalDate, contribution, checking, startDate, goalDate, false, 0);
		
		MoneyOwed debt = new MoneyOwed("Misc Debt", 10000, false, 0);
		Payment payment = new Payment(10, false, Frequency.monthly, checking, startDate, true, null, debt, null);
		debt.setupPayment(payment);
				
		MoneyGain gain = new MoneyGain("Gain", 2000, Frequency.weekly, startDate, null, checking);
		MoneyLoss loss = new MoneyLoss("Loss", 500, Frequency.biWeekly, startDate, null, checking);
		
		Budget budget = new Budget(checking, true, Calendar.SUNDAY, 1);
		BudgetItem item = new BudgetItem("Item", 5, Frequency.oneTime, startDate, startDate, false, checking);
		budget.addBudgetItem(item);
		
		BadBudgetData bbd = new BadBudgetData();
		bbd.setBudget(budget);
		bbd.addAccount(checking);
		bbd.addAccount(savings);
		bbd.addDebt(debt);
		bbd.addGain(gain);
		bbd.addLoss(loss);
		
		Date endDate = new GregorianCalendar(2116, Calendar.AUGUST, 23).getTime();
		Prediction.predict(bbd, startDate, endDate);
		
		int numDays = Prediction.numDaysBetween(startDate, endDate);
		
		double checkingLoss = 5 + 10 + 500 + 5;
		double checkingGain = 2000;
		
		double checkingExpected = 1000 - checkingLoss + checkingGain;
		assertTrue("Starting Value Not As Expected", checkingExpected == checking.getPredictData(0).value());
		
		checkingExpected = checkingExpected - 5;
		assertTrue("One Day Value Not As Expected", checkingExpected == checking.getPredictData(1).value());
		
		//Formula given num days = x: 1000 - 5 - 10 + 2000- 500 - 5 = 2480, so 2480 - 5*x-10*12*(x/365)+2000*x/7-500*x/14
		int x = Prediction.numDaysBetween(startDate, new GregorianCalendar(2017, Calendar.AUGUST, 23).getTime());
		checkingExpected = 2480-5*x-10*(int)Math.round(12*(x/365.25))+2000*(x/7)-500*(x/14);
		assertTrue("One Year Value Not As Expected. Expected: " + checkingExpected + 
				"\nFound: " + checking.getPredictData(365).value(), checkingExpected == checking.getPredictData(365).value());
		
		int goalDaysMax = Prediction.numDaysBetween(startDate, new GregorianCalendar(2019, Calendar.APRIL, 9).getTime());
		x = Prediction.numDaysBetween(startDate, new GregorianCalendar(2019, Calendar.AUGUST, 23).getTime());
		checkingExpected = 2480-5*goalDaysMax-10*(int)Math.round(12*(x/365.25))+2000*(x/7)-500*(x/14);
		
		assertTrue("3 Year Value Not As Expected. Expected: " + checkingExpected + 
				"\nFound: " + checking.getPredictData(x).value(), checkingExpected == checking.getPredictData(x).value());
		
		x = Prediction.numDaysBetween(startDate, new GregorianCalendar(2026, Calendar.AUGUST, 23).getTime());
		checkingExpected = 2480-5*goalDaysMax-10*(int)Math.round(12*(x/365.25))+2000*(x/7)-500*(x/14);
		
		assertTrue("10 Year Value Not As Expected. Expected: " + checkingExpected + 
				"\nFound: " + checking.getPredictData(x).value(), checkingExpected == checking.getPredictData(x).value());
		
		int paymentDaysMax = Prediction.numDaysBetween(startDate, new GregorianCalendar(2099, Calendar.NOVEMBER, 23).getTime());
		x = Prediction.numDaysBetween(startDate, new GregorianCalendar(2116, Calendar.AUGUST, 23).getTime());
		checkingExpected = 2480-5*goalDaysMax-10*(int)Math.round(12*(paymentDaysMax/365.25))+2000*(x/7)-500*(x/14);
		
		assertTrue("100 Year Value Not As Expected. Expected: " + checkingExpected + 
				"\nFound: " + checking.getPredictData(x).value(), checkingExpected == checking.getPredictData(x).value());
	}

	@Test
	public void predictBasicGoalDateTest() throws BadBudgetInvalidValueException 
	{
		Account checking = new Account("Checking Account", 100, true);
		
		Date startDate = new GregorianCalendar(2016, Calendar.NOVEMBER, 12).getTime();
		Contribution contribution = new Contribution(50, Frequency.daily);
		double currentAmount = 0;
		double goalAmount = 1000;
		
		Date goalDate = Prediction.findGoalDate(startDate, contribution, currentAmount, goalAmount);
		SavingsAccount savings = new SavingsAccount("Savings Account", 0, false, true, goalAmount, goalDate, contribution, checking, startDate, goalDate, false, 0);
		
		BadBudgetData bbd = new BadBudgetData();
		Budget budget = new Budget(checking, false, Calendar.SUNDAY, 1);
		bbd.setBudget(budget);
		bbd.addAccount(checking);
		bbd.addAccount(savings);
		Prediction.predict(bbd, startDate, goalDate);
		
		double runningTotal = savings.value();
		for (int i = 0; i <= Prediction.numDaysBetween(startDate, goalDate); i++)
		{
			runningTotal+=50;
			assertTrue(runningTotal == savings.getPredictData(i).value());
		}
		assertTrue("Hand Calculated goal date differs from actual: " + goalDate, Prediction.datesEqualUpToDay(goalDate, new GregorianCalendar(2016, Calendar.DECEMBER, 1).getTime()));
		
		bbd.clearPredictData();
		bbd.deleteAccountWithName("Savings Account");
		contribution = new Contribution(50, Frequency.monthly);
		goalDate = Prediction.findGoalDate(startDate, contribution, currentAmount, goalAmount);
		savings = new SavingsAccount("Savings Account", 0, false, true, goalAmount, goalDate, contribution, checking, startDate, goalDate, false, 0);
		bbd.addAccount(savings);
		Prediction.predict(bbd, startDate, goalDate);
		
		Date resultDate = Prediction.addDays(startDate, Prediction.numDaysBetween(startDate, goalDate));
		assertTrue("Dates not equal: Expected: " + goalDate + "\n" + "Result: " + resultDate, Prediction.datesEqualUpToDay(goalDate, resultDate));
		
		int index = Prediction.numDaysBetween(startDate, goalDate);
		assertTrue("Goal value incorrect: " + savings.getPredictData(index).value(), savings.getPredictData(index).value() >= goalAmount);
	}
	
	@Test
	public void numDayAddDaysTest()
	{
		Date startDate = new GregorianCalendar(2015, Calendar.FEBRUARY, 12).getTime();
		Date endDate = new GregorianCalendar(2015, Calendar.FEBRUARY, 28).getTime();
		
		Date resultDate = Prediction.addDays(startDate, Prediction.numDaysBetween(startDate, endDate));
		assertTrue("Dates not equal: Expected: " + endDate + "\n" + "Result: " + resultDate, Prediction.datesEqualUpToDay(endDate, resultDate));
		
		startDate = new GregorianCalendar(2015, Calendar.APRIL, 12).getTime();
		endDate = new GregorianCalendar(2015, Calendar.MAY, 12).getTime();
		
		resultDate = Prediction.addDays(startDate, Prediction.numDaysBetween(startDate, endDate));
		assertTrue("Dates not equal: Expected: " + endDate + "\n" + "Result: " + resultDate, Prediction.datesEqualUpToDay(endDate, resultDate));
		
		startDate = new GregorianCalendar(2015, Calendar.APRIL, 12).getTime();
		endDate = new GregorianCalendar(2016, Calendar.MAY, 12).getTime();
		
		resultDate = Prediction.addDays(startDate, Prediction.numDaysBetween(startDate, endDate));
		assertTrue("Dates not equal: Expected: " + endDate + "\n" + "Result: " + resultDate, Prediction.datesEqualUpToDay(endDate, resultDate));
		
		startDate = new GregorianCalendar(2015, Calendar.FEBRUARY, 12).getTime();
		endDate = new GregorianCalendar(2015, Calendar.FEBRUARY, 12).getTime();
		
		resultDate = Prediction.addDays(startDate, Prediction.numDaysBetween(startDate, endDate));
		assertTrue("Dates not equal: Expected: " + endDate + "\n" + "Result: " + resultDate, Prediction.datesEqualUpToDay(endDate, resultDate));
		
		startDate = new GregorianCalendar(2015, Calendar.FEBRUARY, 12).getTime();
		endDate = new GregorianCalendar(2015, Calendar.MARCH, 1).getTime();
		
		resultDate = Prediction.addDays(startDate, Prediction.numDaysBetween(startDate, endDate));
		assertTrue("Dates not equal: Expected: " + endDate + "\n" + "Result: " + resultDate, Prediction.datesEqualUpToDay(endDate, resultDate));
		
		startDate = new GregorianCalendar(2015, Calendar.FEBRUARY, 12).getTime();
		endDate = new GregorianCalendar(2015, Calendar.MARCH, 9).getTime();
		
		resultDate = Prediction.addDays(startDate, Prediction.numDaysBetween(startDate, endDate));
		assertTrue("Dates not equal: Expected: " + endDate + "\n" + "Result: " + resultDate, Prediction.datesEqualUpToDay(endDate, resultDate));
	}
	
	@Test
	public void addDaysTest()
	{
		Date start = new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime();
		Date expected = new GregorianCalendar(2015, Calendar.JANUARY, 2).getTime();
		Date resultDate = Prediction.addDays(start, 1);
		assertTrue("Add days 1 incorrect " + resultDate, Prediction.datesEqualUpToDay(expected, resultDate));
		
		expected = new GregorianCalendar(2015, Calendar.FEBRUARY, 1).getTime();
		resultDate = Prediction.addDays(start, 31);
		assertTrue("Add days 31 incorrect " + resultDate, Prediction.datesEqualUpToDay(expected, resultDate));
		
		expected = new GregorianCalendar(2015, Calendar.MARCH, 3).getTime();
		resultDate = Prediction.addDays(start, 61);
		assertTrue("Add days 61 incorrect " + resultDate, Prediction.datesEqualUpToDay(expected, resultDate));
	}
	
	@Test
	public void numDaysBetweenTest()
	{
		Date start = new GregorianCalendar(2015, Calendar.JANUARY, 31).getTime();
		Date end = new GregorianCalendar(2015, Calendar.FEBRUARY, 2).getTime();
		int result = Prediction.numDaysBetween(start, end);
		
		assertTrue("Num days result unexpected result " + result, result == 2);
		
		end = new GregorianCalendar(2016, Calendar.JANUARY, 31).getTime();
		result = Prediction.numDaysBetween(start, end);
		assertTrue("Num days result unexpected result " + result, result == 365);
		
		Date startDate1 = new GregorianCalendar(2015, Calendar.MARCH, 8).getTime();
		Date endDate1 = new GregorianCalendar(2015, Calendar.MARCH, 8).getTime();
		Date endDate2 = new GregorianCalendar(2015, Calendar.MARCH, 9).getTime();

		assertTrue("Days between a date and two different dates are equal!", 
				Prediction.numDaysBetween(startDate1, endDate1) != Prediction.numDaysBetween(startDate1, endDate2));
		
		Date startDateInitial = new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime();
		Calendar currStartCal = Calendar.getInstance();
		currStartCal.setTime(startDateInitial);
		for (int i = 0; i < 100; i++)
		{
			Date currStartDate = currStartCal.getTime();
			Calendar currEndCal = Calendar.getInstance();
			currEndCal.setTime(currStartDate);
			for (int j = 0; j < 500; j++)
			{
				Date currEndDate = currEndCal.getTime();
				assertTrue("Error index: " + i, Prediction.numDaysBetween(currStartDate, currEndDate) == j);
				currEndCal.add(Calendar.DAY_OF_MONTH, 1);
			}
			currStartCal.add(Calendar.DAY_OF_MONTH, 1);
		}
	}
	
	@Test
	public void predictBasicAccountGainTest() throws BadBudgetInvalidValueException
	{
		Date start = new GregorianCalendar(2020, Calendar.OCTOBER, 12).getTime();
		Date end = new GregorianCalendar(2020, Calendar.DECEMBER, 1).getTime();
		
		BadBudgetData bbd = new BadBudgetData();
		Account testSource = new Account("Test Source", 0, false);
		Budget budget = new Budget(testSource, false, Calendar.SUNDAY, 1);
		bbd.setBudget(budget);
		bbd.addAccount(testSource);
		
		Prediction.predict(bbd, start, end);
		for (int i = 0; i < Prediction.numDaysBetween(start, end); i++)
		{
			assertTrue(testSource.getPredictData(i).value() == testSource.value());
		}
		
		bbd.clearPredictData();
		
		MoneyGain gain = new MoneyGain("Sample Gain", 100, Frequency.daily, start, end, testSource);
		bbd.addGain(gain);
		Prediction.predict(bbd, start, end);
		double runningTotal = testSource.value();
		for (int i = 0; i < Prediction.numDaysBetween(start, end); i++)
		{
			runningTotal+=100;
			assertTrue("Running Total differs from predict total at index: " + i + ":" + testSource.getPredictData(i).value() + ":" + runningTotal, testSource.getPredictData(i).value() == runningTotal);
	
		}
	}
	
	@Test
	public void currAmountBudgetTest() throws BadBudgetInvalidValueException
	{
		Account checking = new Account("Checking", 1000, false);
		Date startDate = new GregorianCalendar(2016, Calendar.AUGUST, 23).getTime();
		Contribution contribution = new Contribution(5, Frequency.daily);
		Date goalDate = Prediction.findGoalDate(startDate, contribution, 200, 5000);
		SavingsAccount savings = new SavingsAccount("Savings", 200, false, true, 5000, goalDate, contribution, checking, startDate, goalDate, false, 0);
		
		MoneyOwed debt = new MoneyOwed("Misc Debt", 10000, false, 0);
		Payment payment = new Payment(10, false, Frequency.monthly, checking, startDate, true, null, debt, null);
		debt.setupPayment(payment);
				
		MoneyGain gain = new MoneyGain("Gain", 2000, Frequency.weekly, startDate, null, checking);
		MoneyLoss loss = new MoneyLoss("Loss", 500, Frequency.biWeekly, startDate, null, checking);
		
		Budget budget = new Budget(checking, true, Calendar.SUNDAY, 1);
		BudgetItem item = new BudgetItem("Item", 5, Frequency.oneTime, startDate, startDate, false, checking);
		budget.addBudgetItem(item);
		
		BudgetItem item2 = new BudgetItem("Item2", 50, Frequency.weekly, startDate, null, false, checking);
		item2.setRemainAmountAction(RemainAmountAction.disappear);
		budget.addBudgetItem(item2);
		
		BudgetItem item3 = new BudgetItem("Item3", 1, Frequency.daily, startDate, null, false, checking);
		item3.setRemainAmountAction(RemainAmountAction.accumulates);
		budget.addBudgetItem(item3);
		
		BudgetItem item4 = new BudgetItem("Item4", 100, Frequency.weekly, startDate, null, true, checking);
		item4.setRemainAmountAction(RemainAmountAction.accumulates);
		budget.addBudgetItem(item4);
		
		BadBudgetData bbd = new BadBudgetData();
		bbd.setBudget(budget);
		bbd.addAccount(checking);
		bbd.addAccount(savings);
		bbd.addDebt(debt);
		bbd.addGain(gain);
		bbd.addLoss(loss);
		
		assertTrue("Expected 0 got: " + item.getCurrAmount(), 0 == item.getCurrAmount());
		
		Date endDate = new GregorianCalendar(2017, Calendar.AUGUST, 23).getTime();
		Prediction.update(bbd, startDate, endDate);
		assertTrue("Expected 5.0 got: " + item.getCurrAmount(), 5.0 == item.getCurrAmount());
		assertTrue("Expected 50.0 got: " + item2.getCurrAmount(), 50.0 == item2.getCurrAmount());
		assertTrue("Expected 366.0 got: " + item3.getCurrAmount(), 366 == item3.getCurrAmount());
		assertTrue("Expected 5271.43 got " + item4.getCurrAmount(), 100*52+(100*(5.0/7.0)) == item4.getCurrAmount());
	}

}
