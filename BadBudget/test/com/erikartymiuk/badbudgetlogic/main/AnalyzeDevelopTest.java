package com.erikartymiuk.badbudgetlogic.main;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.erikartymiuk.badbudgetlogic.budget.Budget;

public class AnalyzeDevelopTest {

	@Test
	public void sanityTest1() throws BadBudgetInvalidValueException {
		
		Calendar todayCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 11);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 11);
		
		BadBudgetData bbd = new BadBudgetData();
		
		Account account = new Account("account", 0, false);
		Budget budget = new Budget(account, false, Calendar.SUNDAY, 1);
		
		bbd.setBudget(budget);
		
		MoneyGain gain = new MoneyGain("Gain", 985.91, Frequency.biWeekly, nextDateCal.getTime(), endDateCal.getTime(), account);
		bbd.addGain(gain);
		
		MoneyLoss github = new MoneyLoss("github", 7, Frequency.monthly, nextDateCal.getTime(), endDateCal.getTime(), account);
		MoneyLoss gym = new MoneyLoss("gym", 33, Frequency.monthly, nextDateCal.getTime(), endDateCal.getTime(), account);
		MoneyLoss internet = new MoneyLoss("internet", 33, Frequency.monthly, nextDateCal.getTime(), endDateCal.getTime(), account);
		MoneyLoss phone = new MoneyLoss("phone", 45, Frequency.monthly, nextDateCal.getTime(), endDateCal.getTime(), account);
		MoneyLoss prescription = new MoneyLoss("prescription", 35, Frequency.monthly, nextDateCal.getTime(), endDateCal.getTime(), account);
		MoneyLoss rent = new MoneyLoss("rent", 599, Frequency.monthly, nextDateCal.getTime(), endDateCal.getTime(), account);
		MoneyLoss tv = new MoneyLoss("tv", 30, Frequency.monthly, nextDateCal.getTime(), endDateCal.getTime(), account);
		MoneyLoss utilities = new MoneyLoss("utilities", 20, Frequency.monthly, nextDateCal.getTime(), endDateCal.getTime(), account);
		MoneyLoss insurance = new MoneyLoss("insurance", 500, Frequency.yearly, nextDateCal.getTime(), endDateCal.getTime(), account);

		bbd.addLoss(github);
		bbd.addLoss(gym);
		bbd.addLoss(internet);
		bbd.addLoss(phone);
		bbd.addLoss(prescription);
		bbd.addLoss(rent);
		bbd.addLoss(tv);
		bbd.addLoss(utilities);
		bbd.addLoss(insurance);
		
		double netGain = Prediction.analyzeNetGainAtFreq(bbd, Frequency.yearly, todayCal.getTime());
		double netLoss = Prediction.analyzeNetLossAtFreq(bbd, Frequency.yearly, todayCal.getTime());
		
		assertTrue(netGain > 20000);
		assertTrue(netLoss > 0 && netLoss > 10000);
	}
	
	//CNE - yes
	//C=chosen, N=next, E=End
	@Test
	public void considerableDateTest1y() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 10);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 11);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 12);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.weekly);
		assertTrue(considerable);
	}
	
	//CNE - no
	//C=chosen, N=next, E=End
	@Test
	public void considerableDateTest1n() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 10);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 18);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 12);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.weekly);
		assertTrue(!considerable);
	}
	
	//CEN
	@Test
	public void considerableDateTest2() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 10);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 12);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 11);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.weekly);
		assertTrue(!considerable);
	}
	
	//NCE
	@Test
	public void considerableDateTest3() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 11);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 12);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.weekly);
		assertTrue(considerable);
	}
	
	//NEC
	@Test
	public void considerableDateTest4() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 12);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 11);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.weekly);
		assertTrue(!considerable);
	}
	
	//ECN
	@Test
	public void considerableDateTest5() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 11);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 12);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.weekly);
		assertTrue(!considerable);
	}
	
	//ENC
	@Test
	public void considerableDateTest6() {
		
		Calendar chosenCal = new GregorianCalendar(2018, Calendar.MAY, 12);

		Calendar nextDateCal = new GregorianCalendar(2018, Calendar.MAY, 11);
		Calendar endDateCal = new GregorianCalendar(2018, Calendar.MAY, 10);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.weekly);
		assertTrue(!considerable);
	}
	
	//(CN)E
	//(XX) = same day
	@Test
	public void considerableDateTest7() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 10);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 11);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.daily);
		assertTrue(considerable);
	}
	
	//E(CN)
	@Test
	public void considerableDateTest10() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 11);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 11);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.daily);
		assertTrue(!considerable);
	}
	
	//(CE)N - - actually no (next after end)
	@Test
	public void considerableDateTest11y() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 10);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 21);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.monthly);
		assertTrue(!considerable);
	}
	
	//(CE)N - no
	@Test
	public void considerableDateTest11n() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 10);

		Calendar nextDateCal = new GregorianCalendar(2018, Calendar.MAY, 11);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.monthly);
		assertTrue(!considerable);
	}
	
	//N(CE)
	@Test
	public void considerableDateTest8() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 11);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 11);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.weekly);
		assertTrue(considerable);
	}
	
	//(EN)C
	@Test
	public void considerableDateTest9() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 11);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.daily);
		assertTrue(!considerable);
	}
	
	//C(EN) - yes
	@Test
	public void considerableDateTest12y() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 10);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 11);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 11);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.daily);
		assertTrue(considerable);
	}
	
	//C(EN) - no
	@Test
	public void considerableDateTest12n() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 10);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 18);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 18);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.weekly);
		assertTrue(!considerable);
	}
	
	//(CNE)
	@Test
	public void considerableDateTest13() {
		
		Calendar chosenCal = new GregorianCalendar(2017, Calendar.MAY, 10);

		Calendar nextDateCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		Calendar endDateCal = new GregorianCalendar(2017, Calendar.MAY, 10);
		
		boolean considerable = Prediction.considerableNextDate(chosenCal.getTime(), nextDateCal.getTime(), endDateCal.getTime(), Frequency.daily);
		assertTrue(considerable);
	}
	
	@Test
	public void adjustAnalyzeTest2() throws BadBudgetInvalidValueException 
	{
		Calendar todayCal = new GregorianCalendar(2017, Calendar.APRIL, 15);
		Calendar nextPayCal = new GregorianCalendar(2017, Calendar.MAY, 1);
		Calendar chosenCal = new GregorianCalendar(2025, Calendar.MAY, 1);
		
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("account", 0, false);
		CreditCard debt = new CreditCard("card", 3000, false, 0.1599);
		Payment payment = new Payment(1250, false, Frequency.monthly, account, nextPayCal.getTime(), true, null, debt, null);
		debt.setupPayment(payment);
		
		Budget budget = new Budget(debt, false, Calendar.SUNDAY, 1);
		bbd.setBudget(budget);
		bbd.addDebt(debt);
		bbd.addAccount(account);
		
		double paymentsMonthly = Prediction.analyzeNetPaymentsAtFreq(bbd, Frequency.monthly, chosenCal.getTime(), todayCal.getTime(), null);
		assertTrue(paymentsMonthly==0.0);
		
		double paymentsYearly = Prediction.analyzeNetPaymentsAtFreq(bbd, Frequency.yearly, chosenCal.getTime(), todayCal.getTime(), null);
		assertTrue(paymentsYearly==0.0);
	}
	
	@Test
	public void adjustAnalyzeTest3() throws BadBudgetInvalidValueException 
	{
		Calendar todayCal = new GregorianCalendar(2017, Calendar.APRIL, 15);
		Calendar nextPayCal = new GregorianCalendar(2017, Calendar.MAY, 1);
		Calendar chosenCal = new GregorianCalendar(2025, Calendar.MAY, 1);
		
		BadBudgetData bbd = new BadBudgetData();
		Account account = new Account("account", 0, false);
		CreditCard debt = new CreditCard("card", 3000, false, 0.1599);
		Payment payment = new Payment(1250, false, Frequency.monthly, account, nextPayCal.getTime(), true, null, debt, null);
		debt.setupPayment(payment);
		MoneyLoss loss = new MoneyLoss("loss", 800, Frequency.monthly, nextPayCal.getTime(), null, debt);
		
		Budget budget = new Budget(debt, false, Calendar.SUNDAY, 1);
		bbd.setBudget(budget);
		bbd.addDebt(debt);
		bbd.addAccount(account);
		bbd.addLoss(loss);
		
		double paymentsMonthly = Prediction.analyzeNetPaymentsAtFreq(bbd, Frequency.monthly, chosenCal.getTime(), todayCal.getTime(), null);
		assertTrue(paymentsMonthly==800.0);
		
		double paymentsYearly = Prediction.analyzeNetPaymentsAtFreq(bbd, Frequency.yearly, chosenCal.getTime(), todayCal.getTime(), null);
		assertTrue(paymentsYearly==800*12);
		
		double paymentsDaily = Prediction.analyzeNetPaymentsAtFreq(bbd, Frequency.daily, chosenCal.getTime(), todayCal.getTime(), null);
		assertTrue(Math.abs(paymentsDaily-(9600/365.25)) < 0.0001);
	}
}
