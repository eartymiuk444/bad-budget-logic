package com.erikartymiuk.badbudgetlogic.main;
import java.util.*;

import com.erikartymiuk.badbudgetlogic.budget.Budget;
import com.erikartymiuk.badbudgetlogic.budget.BudgetItem;

/**
 * Class representing a users data. All accounts (including savings accounts),
 * all MoneyOwed debts, all MoneyGains, all money losses, all money transfers, and
 * all budget data.
 */
public class BadBudgetData 
{
	private HashMap<String, Account> accounts; //All of the user's accounts as a map with the key being the account name
	private HashMap<String, MoneyOwed> debts; //All the user's MoneyOwed (debts) as a map
	private HashMap<String, MoneyGain> gains; //All the user's MoneyGains as a map
	private HashMap<String, MoneyLoss> losses; //All the user's MoneyLosses as a map
	private HashMap<String, MoneyTransfer> transfers; //All the user's MoneyTransfers as a map
	
	private Budget budget;	//The user's budget
	
	/**
	 * BadBudgetData constructor. Sets up all accounts, debts, gains, losses,
	 * and transfers as empty. Any accounts, debts, and gains need to be added individually.
	 * The budget (and budgetItems) also needs to be added separately.
	 */
	public BadBudgetData()
	{
		this.accounts = new HashMap<String, Account>();
		this.debts = new HashMap<String, MoneyOwed>();
		this.gains = new HashMap<String, MoneyGain>();
		this.losses = new HashMap<String, MoneyLoss>();
		this.transfers = new HashMap<String, MoneyTransfer>();
		
		this.budget = null;
	}
	
	/**
	 * Sets this object to have the passed in budget
	 * @param budget - the budget to setup for this user's data
	 */
	public void setBudget(Budget budget)
	{
		this.budget = budget;
	}
	
	/**
	 * Returns this user's budget
	 * @return this user's budget
	 */
	public Budget getBudget()
	{
		return this.budget;
	}
	
	/**
	 * Adds this account to the Data's list (map) of accounts
	 * 
	 * @param account - the account to add
	 */
	public void addAccount(Account account)
	{
		this.accounts.put(account.name(), account);
	}
	
	/**
	 * Add this debt (moneyOwed object) to the Data's list (map)
	 * of debts
	 * 
	 * @param debt - the MoneyOwed (debt) to add
	 */
	public void addDebt(MoneyOwed debt)
	{
		this.debts.put(debt.name(), debt);
	}
	
	/**
	 * Add this gain (moneyGain object) to the Data's list (map) of
	 * gains
	 * 
	 * @param gain - the MoneyGain (gain) to add
	 */
	public void addGain(MoneyGain gain)
	{
		this.gains.put(gain.sourceDescription(), gain);
	}
	
	/**
	 * Add this loss (moneyLoss object) to the Data's list (map) of
	 * losses
	 * 
	 * @param loss - the MoneyLoss (loss) to add
	 */
	public void addLoss(MoneyLoss loss)
	{
		this.losses.put(loss.expenseDescription(), loss);
	}
	
	/**
	 * Add this transfer to the Data's list (map) of transfers
	 * @param transfer - the MoneyTransfer to add
	 */
	public void addTransfer(MoneyTransfer transfer)
	{
		this.transfers.put(transfer.getTransferDescription(), transfer);
	}
	
	/*
	 * Getters
	 */
	
	/**
	 * Returns a list of all the accounts in no particular order
	 * @return - a list of accounts in this BB data object
	 */
	public ArrayList<Account> getAccounts()
	{
		
		return new ArrayList<Account>(this.accounts.values());
	}
	
	/**
	 * Returns a list of this BB data's debts in no particular order
	 * @return - a list of debts (money owed objects)
	 */
	public ArrayList<MoneyOwed> getDebts()
	{
		return new ArrayList<MoneyOwed>(this.debts.values());
	}
	
	/**
	 * Returns a list of this BB data objects gains in no particular order
	 * @return - a list of gains
	 */
	public ArrayList<MoneyGain> getGains()
	{
		return new ArrayList<MoneyGain>(this.gains.values());
	}
	
	/**
	 * Returns a list of this bb data's losses in no particular order
	 * @return -  a list of losses
	 */
	public ArrayList<MoneyLoss> getLosses()
	{
		return new ArrayList<MoneyLoss>(this.losses.values());
	}
	
	/**
	 * Returns a list of this bb data's transfers in no particular order
	 * @return - a list of transfers
	 */
	public ArrayList<MoneyTransfer> getTransfers()
	{
		return new ArrayList<MoneyTransfer>(this.transfers.values());
	}
	
	/* 
	 * Getters of a single item using the name/description of that object
	 */
	
	/**
	 * Given an accounts name, this method returns the account object, if it
	 * exists, in the data's collection of accounts
	 * 
	 * @return the account with the given name or null if the account isn't part of
	 * 	this bb data.
	 */
	public Account getAccountWithName(String name)
	{
		return this.accounts.get(name);
	}
	
	/**
	 * Given the name of a debt (money owed object) this method returns the moneyOwed object
	 * in this BB data object (if it exists).
	 * 
	 * @param name - the name of the debt
	 * @return - the MoneyOwed object corresponding to the debt or null if it isn't in this BB data object
	 */
	public MoneyOwed getDebtWithName(String name)
	{
		return this.debts.get(name);
	}
	
	/**
	 * Given the description of a gain, this method returns the gain object in this BB data
	 * object, if present.
	 * 
	 * @param description - the description of the gain object to return
	 * @return - the gain object or null if not present
	 */
	public MoneyGain getGainWithDescription(String description)
	{
		return this.gains.get(description);
	}
	
	/**
	 * Given the description of a loss, this method returns the loss object in
	 * this BB data object, if present.
	 * 
	 * @param description - the description of the loss to search for
	 * @return - the loss in this BB data object or null if it isn't present
	 */
	public MoneyLoss getLossWithDescription(String description)
	{
		return this.losses.get(description);
	}
	
	/**
	 * Given the description of a transfer, this method returns the transfer object in
	 * this BB data object, if present.
	 * @param description - the description of the transfer to search for
	 * @return - the transfer in this BB data object or null if it isn't present
	 */
	public MoneyTransfer getTransferWithDescription(String description)
	{
		return this.transfers.get(description);
	}
	
	/**
	 * Remove the account with the given name from the collection of accounts. If
	 * no such account exists false is returned.
	 * @param name - the name of the account to delete from our data
	 * @return true if the delete succeeded, false if no account with the passed name
	 * 			exists in our data
	 */
	public boolean deleteAccountWithName(String name)
	{
		if (this.accounts.remove(name) != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Given a debt name, this method deletes the debt from our bad budget data object, if
	 * it exists. 
	 * @param name - the name of the debt to delete
	 * @return true if the debt exists in our data, false if it does not and couldn't be removed
	 */
	public boolean deleteDebtWithName(String name)
	{
		if (this.debts.remove(name) != null)
		{
			return true;
		}
		else
		{
			return false;
		}	}
	
	/**
	 * Given a description of a gain, we attempt to remove that gain from our bad budget data
	 * object.
	 * @param description - the description (treated as a key) of the gain to remove
	 * @return - true if this item was removed successfully, false if a gain with this
	 * 				description didn't exist in our data
	 */
	public boolean deleteGainWithDescription(String description)
	{
		if (this.gains.remove(description) != null)
		{
			return true;
		}
		else
		{
			return false;
		}	}
	
	/**
	 * Given a loss description (treated as a key) of a loss, this method attempts to remove
	 * that loss from the bad budget data.
	 * @param description - the description of the loss to remove
	 * @return - true if the loss was successfully found and removed, false if a loss
	 * 				with this description could not be removed.
	 */
	public boolean deleteLossWithDescription(String description)
	{
		if (this.losses.remove(description) != null)
		{
			return true;
		}
		else
		{
			return false;
		}	
		
	}
	
	
	/**
	 * Given a transfer description (treated as a key) of a transfer, this method attempts to remove
	 * that transfer from the bad budget data.
	 * @param description - the description of the transfer to remove
	 * @return - true if the transfer was successfully found and removed, false if a transfer
	 * 				with this description could not be removed.
	 */
	public boolean deleteTransferWithDescription(String description)
	{
		if (this.transfers.remove(description) != null)
		{
			return true;
		}
		else
		{
			return false;
		}	
		
	}
	
	/**
	 * Clears all the prediction data from all the items in this bad budget data object.
	 */
	public void clearPredictData()
	{
		for (Account a : this.getAccounts())
		{
			a.clearPredictData();
		}
		for (MoneyGain mg : this.getGains())
		{
			mg.clearPredictData();
		}
		for (MoneyLoss ml : this.getLosses())
		{
			ml.clearPredictData();
		}
		for (MoneyTransfer mt : this.getTransfers())
		{
			mt.clearPredictData();
		}
		for (MoneyOwed mo : this.getDebts())
		{
			mo.clearPredictData();
		}
		for (BudgetItem bi : this.getBudget().getAllBudgetItems().values())
		{
			bi.clearPredictData();
		}
	}
	
	/**
	 * Searches for the Source with the given name in this bbd. If found returns that Source.
	 * returns null if the name isn't a source in our bbd. Possible sources are Accounts and Credit Cards.
	 * If an account and cc exist with the passed name the account is returned as the source.
	 * @param name - the name of the source to search for
	 * @return the Source with the given name
	 */
	public Source getSourceWithName(String name)
	{
		Source source = this.getAccountWithName(name);
		if (source != null)
		{
			return source;
		}
		else
		{
			MoneyOwed debt = this.getDebtWithName(name);
			if (debt instanceof CreditCard)
			{
				return (CreditCard) debt;
			}
		}
		return null;
	}
	
	/**
	 * Returns a list of all this bbd's sources (includes all accounts and all credit cards)
	 * @return a list of this bbd's sources
	 */
	public ArrayList<Source> getSources()
	{
		ArrayList<Source> sources = new ArrayList<Source>();
		sources.addAll(this.getAccounts());
		for (MoneyOwed debt : this.getDebts())
		{
			if (debt instanceof CreditCard)
			{
				CreditCard cc = (CreditCard) debt;
				sources.add(cc);
			}
		}
		return sources;
	}
	
	/**
	 * Gets all of the bbd's object sources but excludes any savings accounts
	 * @return a list of this bbd's sources excluding savings accounts
	 */
	public ArrayList<Source> getSourcesExcludeSavingAccounts()
	{
		ArrayList<Source> sources = new ArrayList<Source>();
		for (Account account : this.getAccounts())
		{
			if (!(account instanceof SavingsAccount))
			{
				sources.add(account);
			}
		}
		for (MoneyOwed debt : this.getDebts())
		{
			if (debt instanceof CreditCard)
			{
				CreditCard cc = (CreditCard) debt;
				sources.add(cc);
			}
		}
		return sources;
	}
	
	/**
	 * Searches for the Source with the given name in this bbd but doesn't consider any savings accounts. 
	 * If found returns that Source returns null if the name isn't a source in our bbd. 
	 * Possible sources are Accounts (excluding SavingsAccounts here) and Credit Cards.
	 * If an non-savings account and cc exist with the passed name the account is returned as the source.
	 * @param name - the name of the source to search for
	 * @return the Source with the given name
	 */
	public Source getSourceWithNameExcludeSavingAccounts(String name)
	{
		Source source = this.getAccountWithName(name);
		if (source != null && !(source instanceof SavingsAccount))
		{
			return source;
		}
		else
		{
			MoneyOwed debt = this.getDebtWithName(name);
			if (debt instanceof CreditCard)
			{
				return (CreditCard) debt;
			}
		}
		return null;
	}
	
}
