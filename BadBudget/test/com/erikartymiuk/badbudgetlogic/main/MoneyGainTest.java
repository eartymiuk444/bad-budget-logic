package com.erikartymiuk.badbudgetlogic.main;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

public class MoneyGainTest {

	@Test
	public void testVerifyValues0() throws BadBudgetInvalidValueException 
	{
		String source = "Sample Source";
		double amount = 500;
		Frequency frequency = Frequency.biWeekly;
		Date startDate = new GregorianCalendar(2017, Calendar.AUGUST, 17).getTime();
		Date endDate = null;
		Account destination = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyGain.verifyValues(source, amount, frequency, startDate, endDate, destination);
		assertTrue("Unexpected error code: wanted 0 but got " + errorCode, errorCode == 0);
	}
	
	@Test
	public void testVerifyValuesMisc0() throws BadBudgetInvalidValueException 
	{
		String source = "Sample Source";
		double amount = 500;
		Frequency frequency = Frequency.biWeekly;
		Date startDate = new GregorianCalendar(2017, Calendar.AUGUST, 17).getTime();
		Date endDate = new GregorianCalendar(2017, Calendar.AUGUST, 17).getTime();
		Account destination = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyGain.verifyValues(source, amount, frequency, startDate, endDate, destination);
		assertTrue("Unexpected error code: wanted 0 but got " + errorCode, errorCode == 0);
	}
	
	@Test
	public void testVerifyValuesValid2() throws BadBudgetInvalidValueException 
	{
		String source = "Sample Source";
		double amount = 500;
		Frequency frequency = Frequency.biWeekly;
		Date startDate = new GregorianCalendar(2017, Calendar.AUGUST, 17).getTime();
		Date endDate = new GregorianCalendar(2017, Calendar.AUGUST, 16).getTime();
		Account destination = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyGain.verifyValues(source, amount, frequency, startDate, endDate, destination);
		assertTrue("Unexpected error code: wanted 0 but got " + errorCode, errorCode == 0);
	}
	
	@Test
	public void testVerifyValues5() throws BadBudgetInvalidValueException 
	{
		String source = "Sample Source";
		double amount = 500;
		Frequency frequency = Frequency.oneTime;
		Date startDate = new GregorianCalendar(2017, Calendar.AUGUST, 17).getTime();
		Date endDate = null;
		Account destination = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyGain.verifyValues(source, amount, frequency, startDate, endDate, destination);
		assertTrue("Unexpected error code: wanted 5 but got " + errorCode, errorCode == 5);
	}
	
	@Test
	public void testVerifyValuesValid1() throws BadBudgetInvalidValueException 
	{
		String source = "Sample Source";
		double amount = 500;
		Frequency frequency = Frequency.oneTime;
		Date nextDeposit = new GregorianCalendar(2017, Calendar.AUGUST, 17).getTime();
		Date endDate = new GregorianCalendar(2017, Calendar.AUGUST, 17).getTime();
		Account destination = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyGain.verifyValues(source, amount, frequency, nextDeposit, endDate, destination);
		assertTrue("Unexpected error code: wanted 0 but got " + errorCode, errorCode == 0);
	}
	
	@Test
	public void testVerifyValues8() throws BadBudgetInvalidValueException 
	{
		String source = "Sample Source";
		double amount = 500;
		Frequency frequency = Frequency.oneTime;
		Date nextDeposit = new GregorianCalendar(2017, Calendar.AUGUST, 17).getTime();
		Date endDate = new GregorianCalendar(2017, Calendar.AUGUST, 11).getTime();
		Account destination = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyGain.verifyValues(source, amount, frequency, nextDeposit, endDate, destination);
		assertTrue("Unexpected error code: wanted 8 but got " + errorCode, errorCode == 8);
	}
	
	@Test
	public void testVerifyValues8_2() throws BadBudgetInvalidValueException 
	{
		String source = "Sample Source";
		double amount = 500;
		Frequency frequency = Frequency.oneTime;
		Date nextDeposit = new GregorianCalendar(2017, Calendar.AUGUST, 17).getTime();
		Date endDate = new GregorianCalendar(2017, Calendar.AUGUST, 18).getTime();
		Account destination = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyGain.verifyValues(source, amount, frequency, nextDeposit, endDate, destination);
		assertTrue("Unexpected error code: wanted 8 but got " + errorCode, errorCode == 8);
	}
	
	@Test
	public void testVerifyValues7() throws BadBudgetInvalidValueException 
	{
		String source = "Sample Source";
		double amount = 500;
		Frequency frequency = Frequency.weekly;
		Date nextDeposit = null;
		Date endDate = new GregorianCalendar(2017, Calendar.AUGUST, 18).getTime();
		Account destination = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyGain.verifyValues(source, amount, frequency, nextDeposit, endDate, destination);
		assertTrue("Unexpected error code: wanted 7 but got " + errorCode, errorCode == 7);
	}
	
	@Test
	public void testVerifyValuesValid3() throws BadBudgetInvalidValueException 
	{
		String source = "Sample Source";
		double amount = 500;
		Frequency frequency = Frequency.oneTime;
		Date nextDeposit = null;
		Date endDate = new GregorianCalendar(2017, Calendar.AUGUST, 18).getTime();
		Account destination = new Account("Sample Account", 200, false);
		
		int errorCode = MoneyGain.verifyValues(source, amount, frequency, nextDeposit, endDate, destination);
		assertTrue("Unexpected error code: wanted 0 but got " + errorCode, errorCode == 0);
	}
}
