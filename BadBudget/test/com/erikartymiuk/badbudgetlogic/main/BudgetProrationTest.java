package com.erikartymiuk.badbudgetlogic.main;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.erikartymiuk.badbudgetlogic.budget.Budget;
import com.erikartymiuk.badbudgetlogic.budget.BudgetItem;

public class BudgetProrationTest {
	
	@Test
	public void calculateNextLossTest() throws BadBudgetInvalidValueException
	{
		Account account = new Account("Account", 0, false);
		Budget budget = new Budget(account, true, Calendar.SUNDAY, 1);
		
		Date lossDate1 = new GregorianCalendar(2016, Calendar.SEPTEMBER, 15).getTime();
		Date lossDate2 = new GregorianCalendar(2016, Calendar.SEPTEMBER, 1).getTime();
		Date lossDate3 = new GregorianCalendar(2016, Calendar.SEPTEMBER, 5).getTime();
		Date lossDate4 = new GregorianCalendar(2016, Calendar.SEPTEMBER, 28).getTime();
		Date lossDate5 = new GregorianCalendar(2016, Calendar.SEPTEMBER, 30).getTime();

		BudgetItem oneTimeItem = new BudgetItem("OneTime Item", 10, Frequency.oneTime, lossDate1, lossDate1, false, account);
		BudgetItem dailyItem = new BudgetItem("Daily Item", 20, Frequency.daily, lossDate1, null, false, account);
		BudgetItem weeklyItem = new BudgetItem("Weekly Item", 30, Frequency.weekly, lossDate1, null, true, account);
		BudgetItem biWeeklyItem = new BudgetItem("BiWeekly Item", 40, Frequency.biWeekly, lossDate1, null, false, account);
		BudgetItem monthlyItem = new BudgetItem("Monthly Item", 50, Frequency.monthly, lossDate1, null, true, account);
		BudgetItem yearlyItem = new BudgetItem("Yearly Item", 60, Frequency.yearly, lossDate1, null, true, account);

		Date calculatedNextLoss = null;
		
		//Loss Date 1
		calculatedNextLoss = budget.calculateNextLoss(oneTimeItem, lossDate1);
		assertTrue("Expected null but got: " + calculatedNextLoss, calculatedNextLoss == null);
		
		calculatedNextLoss = budget.calculateNextLoss(dailyItem, lossDate1);
		assertTrueHelper("", new GregorianCalendar(2016, Calendar.SEPTEMBER, 16).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(weeklyItem, lossDate1);
		assertTrueHelper("", new GregorianCalendar(2016, Calendar.SEPTEMBER, 18).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(biWeeklyItem, lossDate1);
		assertTrueHelper("", new GregorianCalendar(2016, Calendar.SEPTEMBER, 29).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(monthlyItem, lossDate1);
		assertTrueHelper("", new GregorianCalendar(2016, Calendar.OCTOBER, 1).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(yearlyItem, lossDate1);
		assertTrueHelper("", new GregorianCalendar(2017, Calendar.SEPTEMBER, 15).getTime(), calculatedNextLoss);
		
		//Loss Date 2
		calculatedNextLoss = budget.calculateNextLoss(oneTimeItem, lossDate2);
		assertTrue("Expected null but got: " + calculatedNextLoss, calculatedNextLoss == null);
		
		calculatedNextLoss = budget.calculateNextLoss(dailyItem, lossDate2);
		assertTrueHelper("Daily2: ", new GregorianCalendar(2016, Calendar.SEPTEMBER, 2).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(weeklyItem, lossDate2);
		assertTrueHelper("Weekly2: ", new GregorianCalendar(2016, Calendar.SEPTEMBER, 4).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(biWeeklyItem, lossDate2);
		assertTrueHelper("Bi-Weekly2: ", new GregorianCalendar(2016, Calendar.SEPTEMBER, 15).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(monthlyItem, lossDate2);
		assertTrueHelper("Monthly2: ", new GregorianCalendar(2016, Calendar.OCTOBER, 1).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(yearlyItem, lossDate2);
		assertTrueHelper("Yearly2: ", new GregorianCalendar(2017, Calendar.SEPTEMBER, 1).getTime(), calculatedNextLoss);
		
		//Loss Date 3 - Sept 5 2016
		calculatedNextLoss = budget.calculateNextLoss(oneTimeItem, lossDate3);
		assertTrue("Expected null but got: " + calculatedNextLoss, calculatedNextLoss == null);
		
		calculatedNextLoss = budget.calculateNextLoss(dailyItem, lossDate3);
		assertTrueHelper("Daily3: ", new GregorianCalendar(2016, Calendar.SEPTEMBER, 6).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(weeklyItem, lossDate3);
		assertTrueHelper("Weekly3: ", new GregorianCalendar(2016, Calendar.SEPTEMBER, 11).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(biWeeklyItem, lossDate3);
		assertTrueHelper("Bi-Weekly3: ", new GregorianCalendar(2016, Calendar.SEPTEMBER, 19).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(monthlyItem, lossDate3);
		assertTrueHelper("Monthly3: ", new GregorianCalendar(2016, Calendar.OCTOBER, 1).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(yearlyItem, lossDate3);
		assertTrueHelper("Yearly3: ", new GregorianCalendar(2017, Calendar.SEPTEMBER, 5).getTime(), calculatedNextLoss);
		
		//Loss Date 4 - Sept 28 2016
		calculatedNextLoss = budget.calculateNextLoss(oneTimeItem, lossDate4);
		assertTrue("Expected null but got: " + calculatedNextLoss, calculatedNextLoss == null);
		
		calculatedNextLoss = budget.calculateNextLoss(dailyItem, lossDate4);
		assertTrueHelper("Daily4: ", new GregorianCalendar(2016, Calendar.SEPTEMBER, 29).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(weeklyItem, lossDate4);
		assertTrueHelper("Weekly4: ", new GregorianCalendar(2016, Calendar.OCTOBER, 2).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(biWeeklyItem, lossDate4);
		assertTrueHelper("Bi-Weekly4: ", new GregorianCalendar(2016, Calendar.OCTOBER, 12).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(monthlyItem, lossDate4);
		assertTrueHelper("Monthly4: ", new GregorianCalendar(2016, Calendar.OCTOBER, 1).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(yearlyItem, lossDate4);
		assertTrueHelper("Yearly4: ", new GregorianCalendar(2017, Calendar.SEPTEMBER, 28).getTime(), calculatedNextLoss);
		
		//Loss Date 4 - Sept 30 2016
		calculatedNextLoss = budget.calculateNextLoss(oneTimeItem, lossDate5);
		assertTrue("Expected null but got: " + calculatedNextLoss, calculatedNextLoss == null);
		
		calculatedNextLoss = budget.calculateNextLoss(dailyItem, lossDate5);
		assertTrueHelper("Daily5: ", new GregorianCalendar(2016, Calendar.OCTOBER, 1).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(weeklyItem, lossDate5);
		assertTrueHelper("Weekly5: ", new GregorianCalendar(2016, Calendar.OCTOBER, 2).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(biWeeklyItem, lossDate5);
		assertTrueHelper("Bi-Weekly5: ", new GregorianCalendar(2016, Calendar.OCTOBER, 14).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(monthlyItem, lossDate5);
		assertTrueHelper("Monthly5: ", new GregorianCalendar(2016, Calendar.OCTOBER, 1).getTime(), calculatedNextLoss);
		
		calculatedNextLoss = budget.calculateNextLoss(yearlyItem, lossDate5);
		assertTrueHelper("Yearly5: ", new GregorianCalendar(2017, Calendar.SEPTEMBER, 30).getTime(), calculatedNextLoss);
	}
	
	private void assertTrueHelper(String message, Date date1, Date date2)
	{
		assertTrue(message + "Expected: " + date1 + "\nRecieved: " + date2, Prediction.datesEqualUpToDay(date1, date2));
	}
}
