package com.erikartymiuk.badbudgetlogic.main;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.erikartymiuk.badbudgetlogic.budget.Budget;

public class PaymentTest {
	
	/**
	 * 			0 - Values are all valid
	 * 			1 - Payoff true but payment amount not -1
	 * 			2 - Payment amount <= 0
	 * 			3 - Payoff true and goalDate not null
	 * 			4 - Frequency is not set
	 * 			5 - Next payment is null and frequency is not one time
	 * 			6 - Goal date not null and next payment after goal date and debt amount not 0
	 * 			7 - Frequency one time and nextPayment not null and nextPayment not equal to endDate
	 * 			8 - Frequency one time and goal date not null
	 * 			9 - Ongoing true and frequency one time
	 * 			10 - Source account is not set
	 * 			11 - Ongoing is true but end date set
	 * 			12 - Ongoing is true but goal date set
	 * 			13 - Ongoing is false but end date is not set
	 * 			14 - Debt is not set
	 * 			15 - Ongoing false and goal date not null and goal date not equal to end date
	 * 			16 - Goal date not null and next payment before or on goal date and debt, frequency, goal date, next payment and payment amount are inconsistent
	 */
	
	@Test
	public void testVerifyValuesMisc0() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Big Debt", 30000, true, 0);
		
		//Source Account
		Account sourceAccount = new Account("Checking", 9000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2017, Calendar.AUGUST, 17).getTime();
		
		//Frequency
		Frequency frequency = Frequency.monthly;

		//Payment
		double paymentAmount = 300;
		boolean payoff = false;
		boolean ongoing = false;
		Date goalDate = Prediction.findGoalDate(startDate, paymentAmount, frequency, debt.amount(), null);
		Date endDate = goalDate;
		
		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 0 got " + errorCode, errorCode == 0);
		}
	
	//0 - values are error free
	@Test
	public void testVerifyValues0() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = false;
		Date goalDate = Prediction.findGoalDate(startDate, paymentAmount, frequency, debt.amount(), null);
		Date endDate = goalDate;

		
		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 0 got " + errorCode, errorCode == 0);
	}

	//1 - Payoff true but payment amount not -1
	@Test
	public void testVerifyValues1() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Payment
		double paymentAmount = 10;
		boolean payoff = true;
		Frequency frequency = Frequency.weekly;
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		boolean ongoing = true;
		Date endDate = null;
		Date goalDate = null;

		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 1 got " + errorCode, errorCode == 1);
	}
	
	//2 - Payment amount <= 0
	@Test
	public void testVerifyValues2() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Payment
		double paymentAmount = -10;
		boolean payoff = false;
		Frequency frequency = Frequency.weekly;
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		boolean ongoing = true;
		Date goalDate = null;
		Date endDate = null;

		
		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 2 got " + errorCode, errorCode == 2);
	}
	
	//3 - Payoff true and goal set
	@Test
	public void testVerifyValues3() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = -1;
		boolean payoff = true;
		boolean ongoing = false;
		Date endDate = new GregorianCalendar(2017, Calendar.JANUARY, 1).getTime();
		Date goalDate = endDate;

		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 3 got " + errorCode, errorCode == 3);
	}
	
	//4 - Frequency is null
	@Test
	public void testVerifyValues4() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = null;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = true;
		Date endDate = null;
		Date goalDate = null;

		
		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 4 got " + errorCode, errorCode == 4);
	}
	
	//5 - Next payment is null and frequency is not one time
	@Test
	public void testVerifyValues5() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = null;
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = true;
		Date endDate = null;
		Date goalDate = null;

		
		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 5 got " + errorCode, errorCode == 5);
	}
	
	//6 - Goal date not null and next payment after goal date and debt amount not 0
	@Test
	public void testVerifyValues6() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		Date nextPayment = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = false;
		Date endDate = new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime();
		Date goalDate = new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime();

		
		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, nextPayment, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 6 got " + errorCode, errorCode == 6);
	}
	
	//7 - Frequency one time and nextPayment not null and nextPayment not equal to endDate
	@Test
	public void testVerifyValues7() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.oneTime;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = false;
		Date endDate = new GregorianCalendar(2016, Calendar.JANUARY, 2).getTime();
		Date goalDate = null;

		
		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 7 got " + errorCode, errorCode == 7);
	}
	
	//8 - Frequency is one time but goal date is set
	@Test
	public void testVerifyValues8() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.oneTime;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = false;
		Date endDate = startDate;
		Date goalDate = endDate;

		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 8 got " + errorCode, errorCode == 8);
	}
	
	//9 - Frequency is one time but ongoing is true
	@Test
	public void testVerifyValues9() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.oneTime;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = true;
		Date endDate = null;
		Date goalDate = null;

		
		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 9 got " + errorCode, errorCode == 9);
	}
	
	//10 - Source account is not set
	@Test
	public void testVerifyValues10() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = null;
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = true;
		Date endDate = null;
		Date goalDate = null;

		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 10 got " + errorCode, errorCode == 10);
	}
	
	//11 - Ongoing is true but end date set
	@Test
	public void testVerifyValues11() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = true;
		Date endDate = startDate;
		Date goalDate = null;

		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 11 got " + errorCode, errorCode == 11);
	}
	
	//12 - Ongoing is true but goal date set
	@Test
	public void testVerifyValues12() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = true;
		Date endDate = null;
		Date goalDate = Prediction.findGoalDate(startDate, paymentAmount, frequency, debt.amount(), null);

		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 12 got " + errorCode, errorCode == 12);
	}
	
	//13 - Ongoing is false but end date is not set
	@Test
	public void testVerifyValues13() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = false;
		Date endDate = null;
		Date goalDate = null;

		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 13 got " + errorCode, errorCode == 13);
	}
	
	//14 - Debt is not set
	@Test
	public void testVerifyValues14() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = null;
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = true;
		Date endDate = null;
		Date goalDate = null;

		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 14 got " + errorCode, errorCode == 14);
	}
	
	//15 - Ongoing false and goal date not null and goal date not equal to end date
	@Test
	public void testVerifyValues15() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = false;
		Date endDate = new GregorianCalendar(2017, Calendar.JANUARY, 1).getTime();
		Date goalDate = Prediction.findGoalDate(startDate, paymentAmount, frequency, debt.amount(), null);

		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 15 got " + errorCode, errorCode == 15);
	}
	
	//16 - Goal is not consistent
	@Test
	public void testVerifyValues16() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		//Start Date
		Date startDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.yearly;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = false;
		Date endDate = new GregorianCalendar(2017, Calendar.JANUARY, 1).getTime();
		Date goalDate = endDate;

		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, startDate, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 16 got " + errorCode, errorCode == 16);
	}
	
	@Test
	public void testMisc16() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 20, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		Date nextPayment = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = false;
		Date endDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		Date goalDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();

		
		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, nextPayment, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 16 got " + errorCode, errorCode == 16);
	}
	
	@Test
	public void testMisc16_1() throws BadBudgetInvalidValueException 
	{
		//Debt
		MoneyOwed debt = new MoneyOwed("Sample Debt", 10, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		Date nextPayment = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 10;
		boolean payoff = false;
		boolean ongoing = false;
		Date endDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		Date goalDate = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();

		
		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, nextPayment, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 0 got " + errorCode, errorCode == 0);
	}
	
	@Test
	public void testUpdateWithLoss() throws BadBudgetInvalidValueException 
	{
		//Debt
		CreditCard debt = new CreditCard("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		Date nextPayment = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 100;
		boolean payoff = false;
		boolean ongoing = false;
		Date goalDate = Prediction.findGoalDate(nextPayment, paymentAmount, frequency, debt.amount(), null);
		Date endDate = goalDate;
		
		
		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, nextPayment, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 0 got " + errorCode, errorCode == 0);
		
		Payment payment = new Payment(paymentAmount, false, frequency, sourceAccount, nextPayment, false, endDate, debt, goalDate);
		
		debt.setupPayment(payment);
		
		BadBudgetData bbd = new BadBudgetData();
		bbd.addDebt(debt);
		bbd.addAccount(sourceAccount);
		bbd.setBudget(new Budget(sourceAccount, false, Calendar.SUNDAY, 1));
		
		MoneyLoss loss = new MoneyLoss("loss", 5, Frequency.weekly, nextPayment, null, debt);
		bbd.addLoss(loss);
		
		Prediction.update(bbd, nextPayment, goalDate);
		errorCode = Payment.verifyValues(debt.payment().amount(), debt.payment().payOff(), debt.payment().frequency(), debt.payment().sourceAccount(), 
				debt.payment().nextPaymentDate(), debt.payment().ongoing(), debt.payment().endDate(), debt, debt.payment().goalDate());
		assertTrue("Unexpected error code: expected 0 got " + errorCode, errorCode == 0);
	}
	
	@Test
	public void testUpdateWithLossBeforeGoal() throws BadBudgetInvalidValueException 
	{
		//Debt
		CreditCard debt = new CreditCard("Sample Debt", 1000, false, 0);
		
		//Source Account
		Account sourceAccount = new Account("Test Account", 5000, false);
		
		Date nextPayment = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
		
		//Frequency
		Frequency frequency = Frequency.weekly;

		//Payment
		double paymentAmount = 100;
		boolean payoff = false;
		boolean ongoing = false;
		Date goalDate = Prediction.findGoalDate(nextPayment, paymentAmount, frequency, debt.amount(), null);
		Date endDate = goalDate;
		
		
		int errorCode = Payment.verifyValues(paymentAmount, payoff, frequency, sourceAccount, nextPayment, ongoing, endDate, debt, goalDate);
		assertTrue("Unexpected error code: expected 0 got " + errorCode, errorCode == 0);
		
		Payment payment = new Payment(paymentAmount, false, frequency, sourceAccount, nextPayment, false, endDate, debt, goalDate);
		
		debt.setupPayment(payment);
		
		BadBudgetData bbd = new BadBudgetData();
		bbd.addDebt(debt);
		bbd.addAccount(sourceAccount);
		bbd.setBudget(new Budget(sourceAccount, false, Calendar.SUNDAY, 1));
		
		MoneyLoss loss = new MoneyLoss("loss", 5, Frequency.weekly, nextPayment, null, debt);
		bbd.addLoss(loss);
		
		Prediction.update(bbd, nextPayment, new GregorianCalendar(2016, Calendar.FEBRUARY, 4).getTime());
		errorCode = Payment.verifyValues(debt.payment().amount(), debt.payment().payOff(), debt.payment().frequency(), debt.payment().sourceAccount(), 
				debt.payment().nextPaymentDate(), debt.payment().ongoing(), debt.payment().endDate(), debt, debt.payment().goalDate());
		assertTrue("Unexpected error code: expected 0 got " + errorCode, errorCode == 0);
	}
}
