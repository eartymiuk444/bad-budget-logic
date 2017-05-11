package com.erikartymiuk.badbudgetlogic.main;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.plaf.synth.SynthSeparatorUI;

import org.junit.Test;

import com.erikartymiuk.badbudgetlogic.budget.Budget;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataSavingsAccount;

public class InterestTest {

	@Test
	public void creditCardInterestSimplePredictTest() throws BadBudgetInvalidValueException {
		
		BadBudgetData bbd = new BadBudgetData();
		CreditCard creditCard = new CreditCard("Credit Card", 2000, false, 0.1599);
		Budget budget = new Budget(creditCard, true, Calendar.SUNDAY, 1);
		
		bbd.addDebt(creditCard);
		bbd.setBudget(budget);
		
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar endCal = new GregorianCalendar(2018, Calendar.FEBRUARY, 1);
		
		Prediction.predict(bbd, startCal.getTime(), endCal.getTime());
		int dayIndex = Prediction.numDaysBetween(startCal.getTime(), endCal.getTime());
		
		assertTrueHelper("", creditCard.amount() * Math.pow(1+0.1599/365.25, dayIndex), creditCard.getPredictData(dayIndex).value());
	}
	
	@Test
	public void loanInterestSimplePredictTest() throws BadBudgetInvalidValueException
	{
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("Account", 0, false);
		Budget budget = new Budget(account, true, Calendar.SUNDAY, 1);

		Loan loan = new Loan("Loan", 32500, false, 0.055, true, 32500);
		Loan compoundLoan = new Loan("Compound Loan", 32500, false, 0.055, false, 32500);
		
		bbd.addAccount(account);
		bbd.addDebt(loan);
		bbd.addDebt(compoundLoan);
		bbd.setBudget(budget);
		
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar endCal = new GregorianCalendar(2018, Calendar.FEBRUARY, 1);
		
		Prediction.predict(bbd, startCal.getTime(), endCal.getTime());
		int dayIndex = Prediction.numDaysBetween(startCal.getTime(), endCal.getTime());
		
		assertTrueHelper("", loan.getPrincipalBalance()*(0.055/365.25)*(dayIndex), loan.getPredictData(dayIndex).getInterest());
		assertTrueHelper("", compoundLoan.getPrincipalBalance()*Math.pow((1+0.055/365.25), dayIndex), 
				compoundLoan.getPredictData(dayIndex).getPrincipal());
	}
	
	@Test
	public void loanInterestPredictTest0() throws BadBudgetInvalidValueException
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.NOVEMBER, 19);
		Calendar endCal = new GregorianCalendar(2025, Calendar.FEBRUARY, 19);
		Calendar payCal = new GregorianCalendar(2017, Calendar.DECEMBER, 19);
		
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("Account", 0, false);
		Budget budget = new Budget(account, true, Calendar.SUNDAY, 1);

		Loan loan = new Loan("Loan", 3389.44, false, 0.072, true, 3200);
	
		Payment payment = new Payment(50, false, Frequency.monthly, account, payCal.getTime(), true, null, loan, null);
		loan.setupPayment(payment);
		
		bbd.addAccount(account);
		bbd.addDebt(loan);
		bbd.setBudget(budget);
		
		Prediction.predict(bbd, startCal.getTime(), endCal.getTime());
		int dayIndex = Prediction.numDaysBetween(startCal.getTime(), endCal.getTime());
		
		assertTrueHelper("", -4350.0, account.getPredictData(dayIndex).value());
	}
	
	@Test
	public void loanInterestPredictTest1() throws BadBudgetInvalidValueException
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.NOVEMBER, 19);
		Calendar payCal = new GregorianCalendar(2017, Calendar.DECEMBER, 19);
		Calendar endCal = new GregorianCalendar(2027, Calendar.NOVEMBER, 19);
		
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("account", 0, false);
		Budget budget = new Budget(account, true, Calendar.SUNDAY, 1);
		
		Loan loan = new Loan("Loan", 34422.99, false, 0.0625, true, 34422.99);
		Payment payment = new Payment(386.50, false, Frequency.monthly, account, payCal.getTime(), true, null, loan, null);
		loan.setupPayment(payment);
		
		bbd.setBudget(budget);
		bbd.addAccount(account);
		bbd.addDebt(loan);
		
		Prediction.predict(bbd, startCal.getTime(), endCal.getTime());
		
		int dayIndex = Prediction.numDaysBetween(startCal.getTime(), endCal.getTime());
		
		double interest = Prediction.findSimpleInterestPaid(payCal.getTime(), 29, 386.50, Frequency.monthly, 34422.99, 0.0625, 34422.99, null);
		//Sanity checked with online loan calculator
		assertTrueHelper("", interest+34422.99, Math.abs(account.getPredictData(dayIndex).value()));
	}
	
	@Test
	public void findGoal1()
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.NOVEMBER, 19);
		Date goalDate = Prediction.findGoalDateCompoundInterest(startCal.getTime(), 0, 400, Frequency.monthly, 34422.99, 0.0625, null);
		Calendar excelDate = new GregorianCalendar(2027, Calendar.APRIL, 19);
		assertTrue(Prediction.datesEqualUpToDay(goalDate, excelDate.getTime()));
	}
	
	@Test
	public void findGoal2()
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Date goalDate = Prediction.findGoalDateCompoundInterest(startCal.getTime(), 0, 400, Frequency.monthly, 400, 0.0625, null);
		assertTrue(Prediction.datesEqualUpToDay(goalDate, startCal.getTime()));
	}
	
	@Test
	public void findGoal3()
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Date goalDate = Prediction.findGoalDateCompoundInterest(startCal.getTime(), 0, 400, Frequency.monthly, 500, 0.0625, null);
		Calendar endCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		endCal.add(Calendar.MONTH, 1);
		assertTrue(Prediction.datesEqualUpToDay(goalDate, endCal.getTime()));
	}
	
	@Test
	public void findGoal4()
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Date goalDate = Prediction.findGoalDateCompoundInterest(startCal.getTime(), 0, 400, Frequency.daily, 500, 0.0625, null);
		Calendar endCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		endCal.add(Calendar.DAY_OF_MONTH, 1);
		assertTrue(Prediction.datesEqualUpToDay(goalDate, endCal.getTime()));
	}
	
	@Test
	public void findGoal5()
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Date goalDate = Prediction.findGoalDateCompoundInterest(startCal.getTime(), 0, 500, Frequency.monthly, 10000, 0.10, null);
		Calendar endCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		endCal.add(Calendar.MONTH, 21);
		assertTrue(Prediction.datesEqualUpToDay(goalDate, endCal.getTime()));
	}

	@Test(expected=BadBudgetInvalidValueException.class)
	public void canCreateInvalidInterestPaymentTest() throws BadBudgetInvalidValueException
	{
		Account account = new Account("account", 100, false);
		MoneyOwed debt = new MoneyOwed("debt", 500, false, 0);
		
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar invalidEndCal = new GregorianCalendar(2017, Calendar.JANUARY, 2);
		
		Payment payment = new Payment(100, false, Frequency.monthly, account, startCal.getTime(), false, invalidEndCal.getTime(), debt, invalidEndCal.getTime());
		debt.setupPayment(payment);
	}
	
	@Test
	public void canCreateInvalidInterestPaymentTest2() throws BadBudgetInvalidValueException
	{
		Account account = new Account("account", 100, false);
		MoneyOwed debt = new MoneyOwed("debt", 500, false, 0.01);
		
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar invalidEndCal = new GregorianCalendar(2017, Calendar.JANUARY, 2);
		
		Payment payment = new Payment(100, false, Frequency.monthly, account, startCal.getTime(), false, invalidEndCal.getTime(), debt, invalidEndCal.getTime());
		debt.setupPayment(payment);
	}
	
	@Test
	public void findSimpleGoal() throws BadBudgetInvalidValueException
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar expectedGoalDate = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		//Used covantage loan calculator to sanity check
		expectedGoalDate.add(Calendar.MONTH, 119);
		Calendar goalLimitCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		goalLimitCal.add(Calendar.YEAR, 500);
		Date goalDate = Prediction.findGoalDateSimpleInterest(startCal.getTime(), 0, 455.81, Frequency.monthly, 42000, 0.055, 42000, goalLimitCal.getTime());
		assertTrue(Prediction.datesEqualUpToDay(expectedGoalDate.getTime(), goalDate));
	}
	
	@Test
	public void findSimpleGoal2() throws BadBudgetInvalidValueException
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar expectedGoalDate = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		//Used covantage loan calculator to sanity check
		expectedGoalDate.add(Calendar.MONTH, 479);
		Calendar goalLimitCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		goalLimitCal.add(Calendar.YEAR, 500);
		Date goalDate = Prediction.findGoalDateSimpleInterest(startCal.getTime(), 31, 15.00, Frequency.monthly, 1000, 0.18, 1000, goalLimitCal.getTime());
		assertTrue(Prediction.numDaysBetween(goalDate, expectedGoalDate.getTime()) < 365 * 5);
	}
	
	@Test
	public void findSimpleGoalNull() throws BadBudgetInvalidValueException
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar goalLimitCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		goalLimitCal.add(Calendar.YEAR, 500);
		Date goalDate = Prediction.findGoalDateSimpleInterest(startCal.getTime(), 0, 1, Frequency.monthly, 1000, 0.18, 1000, goalLimitCal.getTime());
		assertTrue(goalDate == null);
	}
	
	@Test
	public void findSimpleInterestPaid1() throws BadBudgetInvalidValueException
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar goalLimitCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		goalLimitCal.add(Calendar.YEAR, 500);
		double interestPaid = Prediction.findSimpleInterestPaid(startCal.getTime(), 31, 386.06, Frequency.monthly, 34000, 0.065, 34000, goalLimitCal.getTime());
		assertTrue(interestPaid - 12327.84 < 100);
	}
	
	@Test
	public void findCompoundInterestPaid1() throws BadBudgetInvalidValueException
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar goalLimitCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		goalLimitCal.add(Calendar.YEAR, 500);
		double interestPaid = Prediction.findCompoundInterestPaid(startCal.getTime(), 31, 386.06, Frequency.monthly, 34000, 0.065, goalLimitCal.getTime());
		assertTrue(interestPaid - 12372 < 5);
	}
	
	@Test
	public void interestConsistentCheck() throws BadBudgetInvalidValueException
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.NOVEMBER, 19);
		Calendar payCal = new GregorianCalendar(2017, Calendar.DECEMBER, 19);
		
		Date goalDate = Prediction.findGoalDateCompoundInterest(payCal.getTime(), 29, 386.50, Frequency.monthly, 34000, 0.0625, null);
				
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("account", 0, false);
		Budget budget = new Budget(account, true, Calendar.SUNDAY, 1);
		
		Loan loan = new Loan("Loan", 34000, false, 0.0625, false, -1);
		Payment payment = new Payment(386.50, false, Frequency.monthly, account, payCal.getTime(), true, null, loan, null);
		loan.setupPayment(payment);
		
		bbd.setBudget(budget);
		bbd.addAccount(account);
		bbd.addDebt(loan);
		
		Prediction.predict(bbd, startCal.getTime(), goalDate);

		int dayIndex = Prediction.numDaysBetween(startCal.getTime(), goalDate);
		double expectedInterest = Prediction.findCompoundInterestPaid(payCal.getTime(), 29, 386.50, Frequency.monthly, 34000, 0.0625, null);
		assertTrueHelper("", Math.abs(account.getPredictData(dayIndex).value()), 34000 + expectedInterest);
	}
	
	@Test
	public void interestSimpleConsistentCheck() throws BadBudgetInvalidValueException
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.NOVEMBER, 19);
		Calendar payCal = new GregorianCalendar(2017, Calendar.DECEMBER, 19);
		
		Date goalDate = Prediction.findGoalDateSimpleInterest(payCal.getTime(), 29, 386.50, Frequency.monthly, 34000, 0.0625, 34000, null);
				
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("account", 0, false);
		Budget budget = new Budget(account, true, Calendar.SUNDAY, 1);
		
		Loan loan = new Loan("Loan", 34000, false, 0.0625, true, 34000);
		Payment payment = new Payment(386.50, false, Frequency.monthly, account, payCal.getTime(), true, null, loan, null);
		loan.setupPayment(payment);
		
		bbd.setBudget(budget);
		bbd.addAccount(account);
		bbd.addDebt(loan);
		
		Prediction.predict(bbd, startCal.getTime(), goalDate);

		int dayIndex = Prediction.numDaysBetween(startCal.getTime(), goalDate);
		double expectedInterest = Prediction.findSimpleInterestPaid(payCal.getTime(), 29, 386.50, Frequency.monthly, 34000, 0.0625, 34000, null);
		assertTrueHelper("", Math.abs(account.getPredictData(dayIndex).value()), 34000 + expectedInterest);
	}
	
	@Test
	public void savingsInterestTest1() throws BadBudgetInvalidValueException
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar endCal = new GregorianCalendar(2057, Calendar.JANUARY, 1);
		
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("account", 0, false);
		Budget budget = new Budget(account, true, Calendar.SUNDAY, 1);
		
		bbd.setBudget(budget);
		bbd.addAccount(account);
		
		Contribution contribution = new Contribution(1, Frequency.yearly);
		SavingsAccount savingsAccount = new SavingsAccount("savings", 3000, false, false, -1, null, contribution, account, startCal.getTime(), null, true, 0.07);
		
		bbd.addAccount(savingsAccount);
		
		Prediction.predict(bbd, startCal.getTime(), endCal.getTime());
		
		int dayIndex = Prediction.numDaysBetween(startCal.getTime(), endCal.getTime());
		
		//Sanity checked with online calc
		assertTrueHelper("", 49163.586563353354, savingsAccount.getPredictData(dayIndex).value());
		
		Calendar updateStart = new GregorianCalendar(2017, Calendar.JANUARY, 31);
		Calendar updateEnd = new GregorianCalendar(2017, Calendar.FEBRUARY, 1);
		Prediction.update(bbd, startCal.getTime(), updateEnd.getTime());
	}
	
	@Test
	public void savingsInterestUpdateTest1() throws BadBudgetInvalidValueException
	{
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar endCal = new GregorianCalendar(2057, Calendar.JANUARY, 1);
		
		BadBudgetData bbd2 = new BadBudgetData();
		Account account2 = new Account("account", 0, false);
		Budget budget2 = new Budget(account2, true, Calendar.SUNDAY, 1);
		bbd2.setBudget(budget2);
		bbd2.addAccount(account2);
		Contribution contribution2 = new Contribution(1, Frequency.yearly);
		SavingsAccount savingsAccount2 = new SavingsAccount("savings", 3000, false, false, -1, null, contribution2, account2, startCal.getTime(), null, true, 0.07);
		bbd2.addAccount(savingsAccount2);
		
		Prediction.predict(bbd2, startCal.getTime(), endCal.getTime());
		
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("account", 0, false);
		Budget budget = new Budget(account, true, Calendar.SUNDAY, 1);
		bbd.setBudget(budget);
		bbd.addAccount(account);
		Contribution contribution = new Contribution(1, Frequency.yearly);
		SavingsAccount savingsAccount = new SavingsAccount("savings", 3000, false, false, -1, null, contribution, account, startCal.getTime(), null, true, 0.07);
		bbd.addAccount(savingsAccount);
		
		
		Calendar currCal = Calendar.getInstance();
		currCal.setTime(startCal.getTime());
		Prediction.update(bbd, currCal.getTime(), currCal.getTime());

		//Compare each day in predict to corresponding update
		for (int dayIndex = 0; dayIndex <= Prediction.numDaysBetween(startCal.getTime(), endCal.getTime()); dayIndex++)
		{
			PredictDataSavingsAccount pdsa = savingsAccount2.getPredictData(dayIndex);
			assertTrueHelper("", pdsa.value(), savingsAccount.value());
			Date currDate = currCal.getTime();
			currCal.add(Calendar.DAY_OF_MONTH, 1);
			Prediction.update(bbd, currDate, currCal.getTime());
		}
	}
	
	@Test
	public void findGoalSavingsWithInterest1() throws BadBudgetInvalidValueException
	{
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("account", 0, false);
		Budget budget = new Budget(account, false, Calendar.SUNDAY, 1);
		bbd.addAccount(account);
		bbd.setBudget(budget);
		
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Contribution contribution = new Contribution(100, Frequency.monthly);
		SavingsAccount savingsAccount = new SavingsAccount("savings", 0, false, false, -1, null, 
				new Contribution(100, Frequency.monthly), account, startCal.getTime(), null, true, 0.05);
		bbd.addAccount(savingsAccount);
		

		Date goalDate = Prediction.findGoalDateWithInterest(startCal.getTime(), startCal.getTime(), contribution, 0, 100000, 0.05, null);
		Prediction.predict(bbd, startCal.getTime(), goalDate);
		int dayIndex = Prediction.numDaysBetween(startCal.getTime(), goalDate);
		
		assertTrue(savingsAccount.getPredictData(dayIndex-1).value() < 100000);
		assertTrue(savingsAccount.getPredictData(dayIndex).value() > 100000);
	}
	
	@Test
	public void findGoalSavingsWithInterest2() throws BadBudgetInvalidValueException
	{
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("account", 0, false);
		Budget budget = new Budget(account, false, Calendar.SUNDAY, 1);
		bbd.addAccount(account);
		bbd.setBudget(budget);
		
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Contribution contribution = new Contribution(1, Frequency.yearly);
		SavingsAccount savingsAccount = new SavingsAccount("savings", 75000, false, false, -1, null, 
				new Contribution(1, Frequency.yearly), account, startCal.getTime(), null, true, 0.50);
		bbd.addAccount(savingsAccount);
		

		Date goalDate = Prediction.findGoalDateWithInterest(startCal.getTime(), startCal.getTime(), contribution, 75000, 100000, 0.50, null);
		Prediction.predict(bbd, startCal.getTime(), goalDate);
		int dayIndex = Prediction.numDaysBetween(startCal.getTime(), goalDate);
		
		assertTrue(savingsAccount.getPredictData(dayIndex-1).value() < 100000);
		assertTrue(savingsAccount.getPredictData(dayIndex).value() > 100000);

	}
	
	@Test
	public void findInterestEarnedTest() throws BadBudgetInvalidValueException
	{
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("account", 0, false);
		Budget budget = new Budget(account, false, Calendar.SUNDAY, 1);
		bbd.addAccount(account);
		bbd.setBudget(budget);
		
		Calendar contributionCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar eveCal = new GregorianCalendar(2016, Calendar.DECEMBER, 31);

		Calendar endCal = new GregorianCalendar(2056, Calendar.DECEMBER, 31);
		Contribution contribution = new Contribution(100, Frequency.monthly);
		SavingsAccount savingsAccount = new SavingsAccount("savings", 0, false, false, -1, null, 
				contribution, account, contributionCal.getTime(), null, true, 0.05);
		bbd.addAccount(savingsAccount);
		
		Prediction.predict(bbd, eveCal.getTime(), endCal.getTime());
		int dayIndex = Prediction.numDaysBetween(eveCal.getTime(), endCal.getTime());
		
		//Sanity check using online calculator
		double interestEarned = Prediction.findInterestEarned(eveCal.getTime(), contributionCal.getTime(), contribution, 0, 153237.86, 0.05, null);
		assertTrue(interestEarned - 105237.86 < 1);
	}
	
	@Test
	public void findContriTest1() throws BadBudgetInvalidValueException
	{
		Calendar cal = new GregorianCalendar(2017, Calendar.JANUARY, 5);
		Calendar goalCal = new GregorianCalendar(2017, Calendar.JANUARY, 30);
		
		double initialAmount = 0;
		double goalAmount = 500;
		double interest = 0.055;
		Frequency freq = Frequency.daily;
		
		double contriAmount = Prediction.findContributionAmount(cal.getTime(), cal.getTime(), 
				freq, initialAmount, goalAmount, interest, goalCal.getTime());
		
		Date goalDate = Prediction.findGoalDateWithInterest(cal.getTime(), cal.getTime(), 
				new Contribution(contriAmount+.00001, freq), initialAmount, goalAmount, interest, null);
		assertTrue(Prediction.datesEqualUpToDay(goalDate, goalCal.getTime()));
	}
	
	@Test
	public void findContriTest2() throws BadBudgetInvalidValueException
	{
		Calendar cal = new GregorianCalendar(2017, Calendar.JANUARY, 5);
		Calendar goalCal = new GregorianCalendar(2017, Calendar.FEBRUARY, 6);
		
		double initialAmount = 0;
		double goalAmount = 500;
		double interest = 0.055;
		Frequency freq = Frequency.daily;
		
		double contriAmount = Prediction.findContributionAmount(cal.getTime(), cal.getTime(), 
				freq, initialAmount, goalAmount, interest, goalCal.getTime());
		
		Date goalDate = Prediction.findGoalDateWithInterest(cal.getTime(), cal.getTime(), 
				new Contribution(contriAmount+.00001, freq), initialAmount, goalAmount, interest, null);
		assertTrue(Prediction.datesEqualUpToDay(goalDate, goalCal.getTime()));
	}
	
	@Test
	public void findContriTest3() throws BadBudgetInvalidValueException
	{
		Calendar cal = new GregorianCalendar(2017, Calendar.JANUARY, 5);
		Calendar goalCal = new GregorianCalendar(2017, Calendar.FEBRUARY, 6);
		
		double initialAmount = 0;
		double goalAmount = 500;
		double interest = 0.055;
		Frequency freq = Frequency.yearly;
		
		double contriAmount = Prediction.findContributionAmount(cal.getTime(), cal.getTime(), 
				freq, initialAmount, goalAmount, interest, goalCal.getTime());
		
		
		Date goalDate = Prediction.findGoalDateWithInterest(cal.getTime(), cal.getTime(), 
				new Contribution(contriAmount+.00001, freq), initialAmount, goalAmount, interest, null);
		goalCal.set(Calendar.DAY_OF_MONTH, 1);
		assertTrue(Prediction.datesEqualUpToDay(goalDate, goalCal.getTime()));
	}
	
	@Test
	public void findContriTest4() throws BadBudgetInvalidValueException
	{
		Calendar cal = new GregorianCalendar(2017, Calendar.JANUARY, 5);
		Calendar goalCal = new GregorianCalendar(2027, Calendar.FEBRUARY, 6);
		
		double initialAmount = 0;
		double goalAmount = 500;
		double interest = 0.055;
		Frequency freq = Frequency.yearly;
		
		double contriAmount = Prediction.findContributionAmount(cal.getTime(), cal.getTime(), 
				freq, initialAmount, goalAmount, interest, goalCal.getTime());
		
		
		Date goalDate = Prediction.findGoalDateWithInterest(cal.getTime(), cal.getTime(), 
				new Contribution(contriAmount+.00001, freq), initialAmount, goalAmount, interest, null);
		goalCal.set(Calendar.DAY_OF_MONTH, 1);
		assertTrue(Prediction.datesEqualUpToDay(goalDate, goalCal.getTime()));
	}
	
	@Test
	public void findContriTest5() throws BadBudgetInvalidValueException
	{
		Calendar cal = new GregorianCalendar(2017, Calendar.JANUARY, 5);
		Calendar goalCal = new GregorianCalendar(2017, Calendar.DECEMBER, 6);
		
		double initialAmount = 0;
		double goalAmount = 500;
		double interest = 0.055;
		Frequency freq = Frequency.yearly;
		
		double contriAmount = Prediction.findContributionAmount(cal.getTime(), cal.getTime(), 
				freq, initialAmount, goalAmount, interest, goalCal.getTime());
				
		Date goalDate = Prediction.findGoalDateWithInterest(cal.getTime(), cal.getTime(), 
				new Contribution(contriAmount+.00001, freq), initialAmount, goalAmount, interest, null);
		goalCal.set(Calendar.DAY_OF_MONTH, 1);
		assertTrue(Prediction.datesEqualUpToDay(goalDate, goalCal.getTime()));
	}
	
	@Test
	public void findContriTest6() throws BadBudgetInvalidValueException
	{
		Calendar cal = new GregorianCalendar(2018, Calendar.JANUARY, 5);
		Calendar goalCal = new GregorianCalendar(2057, Calendar.JANUARY, 5);
		
		double initialAmount = 0;
		double goalAmount = 500000;
		double interest = 0.055;
		Frequency freq = Frequency.monthly;
		
		double contriAmount = Prediction.findContributionAmount(cal.getTime(), cal.getTime(), 
				freq, initialAmount, goalAmount, interest, goalCal.getTime());
		
		
		Date goalDate = Prediction.findGoalDateWithInterest(cal.getTime(), cal.getTime(), 
				new Contribution(contriAmount+.00001, freq), initialAmount, goalAmount, interest, null);
		assertTrue(Prediction.datesEqualUpToDay(goalDate, goalCal.getTime()));
	}
	
	@Test
	public void findGoalAmount1() throws BadBudgetInvalidValueException
	{
		Calendar cal = new GregorianCalendar(2018, Calendar.JANUARY, 1);
		Calendar goalCal = new GregorianCalendar(2057, Calendar.JANUARY, 1);
		
		double initialAmount = 0;
		double interest = 0.055;
		Contribution contribution = new Contribution(100, Frequency.monthly);
		
		double goalAmount = Prediction.findGoalAmount(cal.getTime(), cal.getTime(), contribution, 0, 0.055, goalCal.getTime());
		
		double contriAmount = Prediction.findContributionAmount(cal.getTime(), cal.getTime(), Frequency.monthly, initialAmount, goalAmount, interest, goalCal.getTime());
		assertTrue(Math.abs(contriAmount - contribution.getContribution()) < 0.0001);
	}
	
	@Test
	public void findGoalAmount2() throws BadBudgetInvalidValueException
	{
		Calendar cal = new GregorianCalendar(2018, Calendar.JANUARY, 1);
		Calendar goalCal = new GregorianCalendar(2027, Calendar.MARCH, 15);
		
		double initialAmount = 0;
		double interest = 0.055;
		Frequency freq = Frequency.weekly;
		Contribution contribution = new Contribution(25, freq);
		
		double goalAmount = Prediction.findGoalAmount(cal.getTime(), cal.getTime(), contribution, 0, 0.055, goalCal.getTime());
		
		double contriAmount = Prediction.findContributionAmount(cal.getTime(), cal.getTime(), 
				freq, initialAmount, goalAmount, interest, goalCal.getTime());
		assertTrue(Math.abs(contriAmount - contribution.getContribution()) < 0.0001);
	}
	
	@Test
	public void findGoalAmount3() throws BadBudgetInvalidValueException
	{
		Calendar cal = new GregorianCalendar(2018, Calendar.JANUARY, 1);
		Calendar goalCal = new GregorianCalendar(2027, Calendar.MARCH, 15);
		
		double initialAmount = 0;
		double interest = 0.055;
		Frequency freq = Frequency.weekly;
		Contribution contribution = new Contribution(25, freq);
		
		double goalAmount = Prediction.findGoalAmount(cal.getTime(), cal.getTime(), contribution, 0, 0.055, goalCal.getTime());
		
		double contriAmount = Prediction.findContributionAmount(cal.getTime(), cal.getTime(), 
				freq, initialAmount, goalAmount, interest, goalCal.getTime());
		Date goalDate = Prediction.findGoalDateWithInterest(cal.getTime(), cal.getTime(), 
				new Contribution(contribution.getContribution() + 0.000001, freq), initialAmount, goalAmount, interest, null);
		assertTrue(Prediction.datesEqualUpToDay(goalDate, goalCal.getTime()));
		assertTrue(Math.abs(contriAmount - contribution.getContribution()) < 0.0001);
	}
	
	@Test
	public void findPaymentAmount1() throws BadBudgetInvalidValueException
	{
		Calendar cal = new GregorianCalendar(2018, Calendar.JANUARY, 1);
		Calendar goalCal = new GregorianCalendar(2025, Calendar.DECEMBER, 7);
		
		double debtAmount = 5000;
		double interestRate = 0.1599;
		Frequency freq = Frequency.monthly;
		
		double paymentAmount = Prediction.findPaymentAmountCompoundInterest(cal.getTime(), 0, goalCal.getTime(), freq, debtAmount, interestRate);
		
		Date goalDate = Prediction.findGoalDateCompoundInterest(cal.getTime(), 0, paymentAmount, freq, debtAmount, interestRate, null);
		double interestPaid = Prediction.findCompoundInterestPaid(cal.getTime(), 0, paymentAmount, freq, debtAmount, interestRate, null);

		goalCal.set(Calendar.DAY_OF_MONTH, 1);
		
		assertTrue(Prediction.datesEqualUpToDay(goalDate, goalCal.getTime()));
	}
	
	@Test
	public void findSmplePayment1() throws BadBudgetInvalidValueException
	{
		//3/12/2017 - Putting off implementation of find payment simple interest, this is initial setup for test
		Calendar cal = new GregorianCalendar(2018, Calendar.JANUARY, 1);
		Calendar goalCal = new GregorianCalendar(2028, Calendar.JANUARY, 1);
		
		double principal = 34000;
		double debtAmount = 34000;
		double interestRate = 0.065;
		Frequency freq = Frequency.monthly;
		
		//double paymentAmount = Prediction.findPaymentAmountSimpleInterest(cal.getTime(), 0, goalCal.getTime(), freq, debtAmount, interestRate, principal);
		//Date actualGoal = Prediction.findGoalDateSimpleInterest(cal.getTime(), 0, paymentAmount+.0001, freq, debtAmount, interestRate, principal, null);
		//System.out.println(paymentAmount);
		//System.out.println(actualGoal);
	}
	
	private void assertTrueHelper(String message, double value1, double value2)
	{
		assertTrue(message + "Expected: " + value1 + "\nRecieved: " + value2, Math.abs(value1-value2) < 0.00001);
	}
}
