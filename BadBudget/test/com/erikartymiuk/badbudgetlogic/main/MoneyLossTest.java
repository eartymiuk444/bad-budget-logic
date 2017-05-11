package com.erikartymiuk.badbudgetlogic.main;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

public class MoneyLossTest {

	@Test
	public void testVerifyValues0() throws BadBudgetInvalidValueException 
	{
		String expense = "Sample Expense";
		double amount = 100;
		Frequency frequency = Frequency.weekly;
		Date startDate = new GregorianCalendar(2017, Calendar.JANUARY, 1).getTime();
		Date endDate = null;
		Account source = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyLoss.verifyValues(expense, amount, frequency, startDate, endDate, source);
		assertTrue("Unexpected error code: wanted 0 but got " + errorCode, errorCode == 0);
	}
	
	@Test
	public void testVerifyValuesMisc0() throws BadBudgetInvalidValueException 
	{
		String expense = "Sample Expense";
		double amount = 100;
		Frequency frequency = Frequency.weekly;
		Date startDate = new GregorianCalendar(2017, Calendar.JANUARY, 1).getTime();
		Date endDate = new GregorianCalendar(2017, Calendar.JANUARY, 1).getTime();
		Account source = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyLoss.verifyValues(expense, amount, frequency, startDate, endDate, source);
		assertTrue("Unexpected error code: wanted 0 but got " + errorCode, errorCode == 0);
	}
	
	//4 - End date is set but occurs before start date
	@Test
	public void testVerifyValuesValid1() throws BadBudgetInvalidValueException 
	{
		String expense = "Sample Expense";
		double amount = 100;
		Frequency frequency = Frequency.weekly;
		Date startDate = new GregorianCalendar(2017, Calendar.JANUARY, 1).getTime();
		Date endDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		Account source = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyLoss.verifyValues(expense, amount, frequency, startDate, endDate, source);
		assertTrue("Unexpected error code: wanted 0 but got " + errorCode, errorCode == 0);
	}
	
	//8 - Next loss not null and frequency one time but next loss doesn't match end date
	@Test
	public void testVerifyValues8And5() throws BadBudgetInvalidValueException 
	{
		String expense = "Sample Expense";
		double amount = 100;
		Frequency frequency = Frequency.oneTime;
		Date nextLoss = new GregorianCalendar(2017, Calendar.JANUARY, 1).getTime();
		Date endDate = new GregorianCalendar(2017, Calendar.JANUARY, 9).getTime();
		Account source = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyLoss.verifyValues(expense, amount, frequency, nextLoss, endDate, source);
		assertTrue("1: Unexpected error code: wanted 8 but got " + errorCode, errorCode == 8);
		
		endDate = null;
		errorCode = MoneyLoss.verifyValues(expense, amount, frequency, nextLoss, endDate, source);
		assertTrue("2: Unexpected error code: wanted 5 but got " + errorCode, errorCode == 5);
	}
}
