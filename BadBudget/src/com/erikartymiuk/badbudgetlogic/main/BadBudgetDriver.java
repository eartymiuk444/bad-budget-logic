package com.erikartymiuk.badbudgetlogic.main;
import java.util.*;

import com.erikartymiuk.badbudgetlogic.budget.*;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.PredictDataAccount;
import com.erikartymiuk.badbudgetlogic.predictdataclasses.TransactionHistoryItem;

public class BadBudgetDriver {
	
	public static void main(String[] args) throws BadBudgetInvalidValueException
	{
		System.out.println("Starting Bad Budget Driver");
		
		Account account = new Account("account", 100, false);
		MoneyOwed debt = new MoneyOwed("debt", 500, false, 0.01);
		
		Calendar startCal = new GregorianCalendar(2017, Calendar.JANUARY, 1);
		Calendar invalidEndCal = new GregorianCalendar(2016, Calendar.JANUARY, 1);
		
		Payment payment = new Payment(100, false, Frequency.monthly, account, startCal.getTime(), false, invalidEndCal.getTime(), debt, invalidEndCal.getTime());
		
		System.out.println("Ending Bad Budget Driver");
	}
	
	public static void printHistory(BadBudgetData bbd, Date startDate, Date endDate)
	{
		
		for (Account a : bbd.getAccounts())
		{
			System.out.println("History for: " + a.name());
			ArrayList<TransactionHistoryItem> aggHistory = new ArrayList<TransactionHistoryItem>();
			for (int i = 0; i <= Prediction.numDaysBetween(startDate, endDate); i++)
			{
				List<TransactionHistoryItem> temp = a.getPredictData(i).transactionHistory();
				if (temp != null)
				{
					for (TransactionHistoryItem s : temp)
					{
						aggHistory.add(s);
					}
				}
			}
			
			for (TransactionHistoryItem s : aggHistory)
			{
				System.out.println(s.getTransactionAmount()+s.getSourceActionString()+s.getTransactionSource()+
									s.getSourceOriginal()+s.getSourceUpdated()+s.getDestinationActionString()+s.getTransactionDestination()+
									s.getDestinationOriginal()+s.getDestinationUpdated());
			}
			System.out.println();
			System.out.println();
		}
		
		for (MoneyOwed currDebt : bbd.getDebts())
		{
			System.out.println("History for: " + currDebt.name());
			ArrayList<TransactionHistoryItem> aggHistory = new ArrayList<TransactionHistoryItem>();
			for (int i = 0; i <= Prediction.numDaysBetween(startDate, endDate); i++)
			{
				List<TransactionHistoryItem> temp = currDebt.getPredictData(i).transactionHistory();
				if (temp != null)
				{
					for (TransactionHistoryItem s : temp)
					{
						aggHistory.add(s);
					}
				}
			}
			
			for (TransactionHistoryItem s : aggHistory)
			{
				System.out.println(s.getTransactionAmount()+s.getSourceActionString()+s.getTransactionSource()+
						s.getSourceOriginal()+s.getSourceUpdated()+s.getDestinationActionString()+s.getTransactionDestination()+
						s.getDestinationOriginal()+s.getDestinationUpdated());
			}
			System.out.println();
			System.out.println();
		}
		
	}
	
}
