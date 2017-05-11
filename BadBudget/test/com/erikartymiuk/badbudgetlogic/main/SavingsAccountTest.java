package com.erikartymiuk.badbudgetlogic.main;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.erikartymiuk.badbudgetlogic.budget.Budget;

public class SavingsAccountTest {
	
	@Test
	public void findGoalDateTest() throws BadBudgetInvalidValueException 
	{
		Date startDate = new GregorianCalendar(2016, Calendar.SEPTEMBER, 1).getTime();
		Contribution contribution = new Contribution(500, Frequency.oneTime);
		double currentAmount = 0;
		double goalAmount = 500;
		Date resultDate = Prediction.findGoalDate(startDate, contribution, currentAmount, goalAmount);
		assertTrue("Expected null but got " + resultDate, resultDate == null);
	}
	
	/*
	 * 				0 - Values are valid
	 * 				1 - Current value is less than 0
	 * 				2 - Goal is set but goal amount <= 0
	 * 				3 - Goal is not set but goal amount is not -1
	 * 				4 - Goal is set but goal date is null
	 * 				5 - Goal is not set but goal date isn't null
	 * 				6 - Contribution is null
	 * 				7 - Contribution is one time but goal is set
	 * 				8 - Source account is null
	 * 				9 - Contribution is not one time but next contribution is null
	 *				10 - Inconsistency found between starting value, goal amount, goal date, and contribution
	 *				11 - Goal set but end date is null
	 *				12 - Goal set but end date is not equal to goal date
	 *				13 - frequency is one time but end date is null
	 *				14 - frequency is one time and next contribution is not null but end date doesn't equal next contribution
	 *	 			15 - Ongoing is true but end date is set
	 * 				16 - Ongoing is false but end date is not set
	 * */
	
	@Test
	//1 - Current value is less than 0
	public void verifyValues1() throws BadBudgetInvalidValueException
	{
		
		double saValue = -1;
		boolean goalSet = true;
		double goalAmount = -1;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = Prediction.findGoalDate(nextContribution, contribution, saValue, goalAmount);
		Date endDate = goalDate;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 1 Got " + errorCode, errorCode == 1);
	}
	
	@Test
	//2 - Goal is set but goal amount <= 0
	public void verifyValues2() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = true;
		double goalAmount = -1;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = Prediction.findGoalDate(nextContribution, contribution, saValue, goalAmount);
		Date endDate = goalDate;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 2 Got " + errorCode, errorCode == 2);
	}
	
	@Test
	//3 - Goal is not set but goal amount is not -1
	public void verifyValues3() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = false;
		double goalAmount = 1000;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = null;
		Date endDate = goalDate;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 3 Got " + errorCode, errorCode == 3);
	}
	
	@Test
	//4 - Goal is set but goal date is null
	public void verifyValues4() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = true;
		double goalAmount = 1000;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = null;
		Date endDate = goalDate;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 4 Got " + errorCode, errorCode == 4);
	}
	
	
	
	@Test
	//5 - Goal is not set but goal date isn't null
	public void verifyValues5() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = false;
		double goalAmount = -1;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = new GregorianCalendar(2017, Calendar.SEPTEMBER, 15).getTime();
		Date endDate = goalDate;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 5 Got " + errorCode, errorCode == 5);
	}
	
	@Test
	//6 - Contribution is null
	public void verifyValues3_1() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = false;
		double goalAmount = -1;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = null;
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = null;
		Date endDate = goalDate;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 6 Got " + errorCode, errorCode == 6);
	}
	
	@Test
	//7 - Contribution is one time but goal is set
	public void verifyValues7() throws BadBudgetInvalidValueException
	{
		double saValue = 0;
		boolean goalSet = true;
		double goalAmount = 500;
		double contributionAmount = 100;
		Frequency frequency = Frequency.oneTime;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = Prediction.findGoalDate(nextContribution, new Contribution(contributionAmount, Frequency.weekly), saValue, goalAmount);
		Date endDate = goalDate;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 7 Got " + errorCode, errorCode == 7);
	}
	
	@Test
	//8 - Source account is null
	public void verifyValues8() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = false;
		double goalAmount = -1;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = null;
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = null;
		Date endDate = goalDate;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 8 Got " + errorCode, errorCode == 8);
	}
	
	@Test
	public void verifyValuesValid_1() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = false;
		double goalAmount = -1;
		double contributionAmount = 100;
		Frequency frequency = Frequency.oneTime;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = null;
		Date goalDate = null;
		Date endDate = new GregorianCalendar(2016, Calendar.SEPTEMBER, 15).getTime();
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 0 Got " + errorCode, errorCode == 0);
	}
	
	@Test
	//9 - Contribution is not one time but next contribution is null
	public void verifyValues9() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = false;
		double goalAmount = -1;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = null;
		Date goalDate = null;
		Date endDate = new GregorianCalendar(2016, Calendar.SEPTEMBER, 15).getTime();
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 9 Got " + errorCode, errorCode == 9);
	}
	
	@Test
	//10 - Inconsistency found between starting value, goal amount, goal date, and contribution
	public void verifyValues8_1() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = true;
		double goalAmount = 1000;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = new GregorianCalendar(2017, Calendar.SEPTEMBER, 12).getTime();
		Date endDate = new GregorianCalendar(2017, Calendar.SEPTEMBER, 12).getTime();
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		//TODO removing this check but could add back in, using new methods 3/15/2017
		//assertTrue("Expected 10 Got " + errorCode, errorCode == 10);
	}		
	
	@Test
	//11 - Goal set but end date is null
	public void verifyValues11() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = true;
		double goalAmount = 1000;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = Prediction.findGoalDate(nextContribution, contribution, saValue, goalAmount);
		Date endDate = null;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 11 Got " + errorCode, errorCode == 11);
	}
	
	@Test
	//12 - Goal set but end date is not equal to goal date
	public void verifyValues12() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = true;
		double goalAmount = 1000;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = Prediction.findGoalDate(nextContribution, contribution, saValue, goalAmount);
		Date endDate = new GregorianCalendar(2017, Calendar.SEPTEMBER, 12).getTime();
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 12 Got " + errorCode, errorCode == 12);
	}
	
	@Test
	//13 - frequency is one time but end date is null
	public void verifyValues13() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = false;
		double goalAmount = -1;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = null;
		Date goalDate = null;
		Date endDate = new GregorianCalendar(2016, Calendar.SEPTEMBER, 15).getTime();
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 13 Got " + errorCode, errorCode != 13);
	}
	
	@Test
	//14 - frequency is one time and next contribution is not null but end date doesn't equal next contribution
	public void verifyValues14() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = false;
		double goalAmount = -1;
		double contributionAmount = 100;
		Frequency frequency = Frequency.oneTime;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = null;
		Date endDate = new GregorianCalendar(2016, Calendar.SEPTEMBER, 13).getTime();;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 14 Got " + errorCode, errorCode == 14);
	}
	
	@Test
	//15 - Ongoing is true but end date is set
	public void verifyValues15() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = false;
		double goalAmount = -1;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = null;
		Date endDate = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		boolean ongoing = true;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 15 Got " + errorCode, errorCode == 15);
	}
	
	@Test
	//16 - Ongoing is false but end date is not set
	public void verifyValues16() throws BadBudgetInvalidValueException
	{
		
		double saValue = 0;
		boolean goalSet = false;
		double goalAmount = -1;
		double contributionAmount = 100;
		Frequency frequency = Frequency.weekly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 12).getTime();
		Date goalDate = null;
		Date endDate = null;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 16 Got " + errorCode, errorCode == 16);
	}
	
	@Test
	public void verifyValuesMisc1() throws BadBudgetInvalidValueException
	{
		double saValue = 0;
		boolean goalSet = true;
		double goalAmount = 10000;
		double contributionAmount = 200;
		Frequency frequency = Frequency.monthly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 24).getTime();
		Date goalDate = Prediction.findGoalDate(nextContribution, contribution, saValue, goalAmount);
		Date endDate = goalDate;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		SavingsAccount sa = new SavingsAccount("sa", saValue, false, 
				goalSet, goalAmount, goalDate, contribution, 
				sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 0 Got " + errorCode, errorCode == 0);
		
		BadBudgetData bbd = new BadBudgetData();
		Budget budget = new Budget(sourceAccount, false, Calendar.SUNDAY, 1);
		
		bbd.addAccount(sourceAccount);
		bbd.addAccount(sa);
		bbd.setBudget(budget);
		
		Prediction.update(bbd, nextContribution, goalDate);

		errorCode = SavingsAccount.verifyValues(sa.value(), goalSet, sa.goal(), sa.goalDate(), 
				sa.contribution(), sa.sourceAccount(), sa.nextContribution(), sa.endDate(), ongoing, 0);
		assertTrue("Expected 0 Got " + errorCode, errorCode == 0);
	}
	
	@Test
	public void verifyValuesMisc2() throws BadBudgetInvalidValueException
	{
		double saValue = 10000;
		boolean goalSet = true;
		double goalAmount = 10000;
		double contributionAmount = 200;
		Frequency frequency = Frequency.monthly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 24).getTime();
		Date goalDate = new GregorianCalendar(2015, Calendar.SEPTEMBER, 24).getTime();
		Date endDate = goalDate;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 0 Got " + errorCode, errorCode == 0);
	}
	
	@Test
	public void verifyValues17() throws BadBudgetInvalidValueException
	{
		double saValue = 9999;
		boolean goalSet = true;
		double goalAmount = 10000;
		double contributionAmount = 200;
		Frequency frequency = Frequency.monthly;
		Contribution contribution = new Contribution(contributionAmount, frequency);
		Account sourceAccount = new Account("a", 0, false);
		Date nextContribution = new GregorianCalendar(2016, Calendar.SEPTEMBER, 24).getTime();
		Date goalDate = new GregorianCalendar(2015, Calendar.SEPTEMBER, 24).getTime();
		Date endDate = goalDate;
		boolean ongoing = false;
		
		int errorCode = SavingsAccount.verifyValues(saValue, goalSet, goalAmount, goalDate, 
				contribution, sourceAccount, nextContribution, endDate, ongoing, 0);
		
		assertTrue("Expected 17 Got " + errorCode, errorCode == 17);
	}
	
	//TODO - stub code could turn into test or delete 3/15/2017
	public void savingsAccountPredictTest() throws BadBudgetInvalidValueException
	{
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("Name", 0, false);
		Budget budget = new Budget(account, false, Calendar.SUNDAY, 1);
		bbd.setBudget(budget);
		
		Calendar startCal = new GregorianCalendar(2017, Calendar.JUNE, 1);
		Calendar todayCal = new GregorianCalendar(2017, Calendar.MARCH, 15);
		double goalAmount = 400243.167;
		Contribution contribution = new Contribution(300, Frequency.monthly);
		double interestRate = 0.045;
		Date goalDate = Prediction.findGoalDateWithInterest(todayCal.getTime(), startCal.getTime(), contribution, 0, goalAmount, interestRate, null);
		System.out.println(goalDate);
		Calendar futureCal = Calendar.getInstance();
		futureCal.setTime(goalDate);
		futureCal.add(Calendar.YEAR, 15);
		SavingsAccount sa = new SavingsAccount("Savings", 0, false, true, goalAmount, goalDate, contribution, account, startCal.getTime(), goalDate, false, interestRate);
		
		bbd.addAccount(account);
		bbd.addAccount(sa);
		
		Prediction.predict(bbd, todayCal.getTime(), futureCal.getTime());
		System.out.println(sa.getPredictData(Prediction.numDaysBetween(todayCal.getTime(), futureCal.getTime())).value());
		System.out.println(account.getPredictData(Prediction.numDaysBetween(todayCal.getTime(), futureCal.getTime())).value());

	}
}
