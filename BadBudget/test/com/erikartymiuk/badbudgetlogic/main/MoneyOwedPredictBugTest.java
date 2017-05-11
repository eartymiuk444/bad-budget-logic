package com.erikartymiuk.badbudgetlogic.main;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.erikartymiuk.badbudgetlogic.budget.Budget;

public class MoneyOwedPredictBugTest {

	@Test
	public void testFindGoalDateCompoundInterest() throws BadBudgetInvalidValueException {
		
		Calendar startCal = new GregorianCalendar(2017, Calendar.APRIL, 1);
		Calendar payCal = new GregorianCalendar(2017, Calendar.MAY, 1);
		
		//29 days
		int daysBeforePay = Prediction.numDaysBetween(startCal.getTime(), payCal.getTime()) - 1;
		
		Calendar calGoal = new GregorianCalendar(2050, Calendar.APRIL, 1);
		
		double paymentAmt = Prediction.findPaymentAmountCompoundInterest(payCal.getTime(), daysBeforePay, calGoal.getTime(), Frequency.monthly, 10000, 0.1);
		
		BadBudgetData bbd = new BadBudgetData();
		Account a = new Account("a", 0, false);
		Budget b = new Budget(a, false, Calendar.SUNDAY, 1);
		CreditCard c = new CreditCard("c", 10000, false, 0.1);
		Payment p = new Payment(paymentAmt+0.0000001, false, Frequency.monthly, a, payCal.getTime(), false, calGoal.getTime(), c, calGoal.getTime());
		c.setupPayment(p);
		
		bbd.addAccount(a);
		bbd.setBudget(b);
		bbd.addDebt(c);
		Prediction.predict(bbd, startCal.getTime(), calGoal.getTime());
		
		int dayIndex = Prediction.numDaysBetween(startCal.getTime(), calGoal.getTime());
		//We hit the goal date exactly on the day we should and not any sooner
		assertTrue(c.getPredictData(dayIndex).value() == 0);
		assertTrue(c.getPredictData(dayIndex-1).value() != 0);
	}
	
	@Test
	public void testFindGoalDateSimpleInterest() throws BadBudgetInvalidValueException {
		
		Calendar startCal = new GregorianCalendar(2017, Calendar.APRIL, 1);
		Calendar payCal = new GregorianCalendar(2017, Calendar.MAY, 1);
		
		//29 days
		int daysBeforePay = Prediction.numDaysBetween(startCal.getTime(), payCal.getTime()) - 1;
		double paymentAmount = 99.96314;
		Date goalDate = Prediction.findGoalDateSimpleInterest(payCal.getTime(), daysBeforePay, 
				paymentAmount, Frequency.monthly, 10000, 0.1, 10000, null);
				
		BadBudgetData bbd = new BadBudgetData();
		Account a = new Account("a", 0, false);
		Budget b = new Budget(a, false, Calendar.SUNDAY, 1);
		Loan l = new Loan("l", 10000, false, 0.1, true, 10000);
		Payment p = new Payment(paymentAmount, false, Frequency.monthly, a, payCal.getTime(), false, goalDate, l, goalDate);
		l.setupPayment(p);
		
		bbd.addAccount(a);
		bbd.setBudget(b);
		bbd.addDebt(l);
		Prediction.predict(bbd, startCal.getTime(), goalDate);
		
		int dayIndex = Prediction.numDaysBetween(startCal.getTime(), goalDate);
		//We hit the goal date exactly on the day we should and not any sooner
		assertTrue(l.getPredictData(dayIndex).value() == 0);
		assertTrue(l.getPredictData(dayIndex-1).value() != 0);
	}
}
