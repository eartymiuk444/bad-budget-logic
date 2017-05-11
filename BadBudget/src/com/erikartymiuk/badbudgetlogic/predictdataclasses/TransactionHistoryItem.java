package com.erikartymiuk.badbudgetlogic.predictdataclasses;

import java.util.Date;

/**
 * Class storing information needed to fully describe a bad budget transaction that occurred.
 * @author Erik Artymiuk
 */
public class TransactionHistoryItem {
	
	public final static String DEFAULT_SOURCE_ACTION = "from";
	public final static String DEFAULT_DESTINATION_ACTION = "to";
	
	public final static String ACCOUNT_SOURCE_ACTION = "withdrawn from";
	public final static String ACCOUNT_DESTINATION_ACTION = "deposited to";
	public final static String SAVINGS_ACCOUNT_DESTINATION_ACTION = "contributed to";
	
	public final static String CREDIT_CARD_SOURCE_ACTION = "credited from";	
	public final static String DEBT_DESTINATION_ACTION = "payed to";
	
	public final static String BUDGET_ITEM_ADD_BACK_ACTION = "added back from";
		
	private Date transactionDate;
	private double transactionAmount;
	private String sourceActionString;
	private String transactionSource;
	private double sourceOriginal;
	private double sourceUpdated;
	
	private String destinationActionString;
	private String transactionDestination;
	private double destinationOriginal;
	private double destinationUpdated;

	private boolean sourceCanShowChange;
	private boolean destinationCanShowChange;
	
	/**
	 * Constructor for a TransactionHistoryItem
	 * @param transactionDate - the date this transaction occurred on
	 * @param transactionAmount - the amount involved in the transaction, that moved from source to destination
	 * @param sourceActionString - an action word/phrase describing the movement "from" source
	 * @param transactionSource - a string representation of source
	 * @param sourceOriginal - if applicable (i.e. source can show change is set) then should be set to the value of source before the transaction occurred
	 * @param sourceUpdated - if applicable (i.e. source can show change is set) then should be set to the value of source after the transaction occurred
	 * @param destinationActionString - an action word/phrase describing the movement "to" the destination
	 * @param transactionDestination - a string representation of the destination
	 * @param destinationOriginal -  if applicable (i.e. destination can show change is set) then should be set to the value of destination before the transaction occurred
	 * @param destinationUpdated - if applicable (i.e. destination can show change is set) then should be set to the value of destination after the transaction occurred
	 * @param sourceCanShowChange - true if it makes sense to show a change in the source's value (i.e. accounts but not gains)
	 * @param destinationCanShowChange - true if it make sense to show a change in the destination's value (i.e. savings accounts but not losses)
	 * 
	 * The general format should make sense using the passed parameters:
	 * 
	 * transactionAmount sourceActionString source(sourceOriginal->sourceUpdated) 
	 * destinationActionString transactionDestination(destinationOriginal->destinationUpdated)
	 * 
	 * Examples:
	 * 
	 * 500 from Cash(1000->500) to Emergency(0->500)
	 * 500 from Cash(1000->500) payed to Chase Credit(700->200)
	 * 200 from Job deposited into Checking(300->500)
	 * 
	 */
	public TransactionHistoryItem(Date transactionDate, double transactionAmount, String sourceActionString, String transactionSource, double sourceOriginal,
			double sourceUpdated, String destinationActionString, String transactionDestination, double destinationOriginal, double destinationUpdated,
			boolean sourceCanShowChange, boolean destinationCanShowChange)
	{
		this.transactionDate = transactionDate;
		this.transactionAmount = transactionAmount;
		this.sourceActionString = sourceActionString;
		this.transactionSource = transactionSource;
		this.sourceOriginal = sourceOriginal;
		this.sourceUpdated = sourceUpdated;
		
		this.destinationActionString = destinationActionString;
		this.transactionDestination = transactionDestination;
		this.destinationOriginal = destinationOriginal;
		this.destinationUpdated = destinationUpdated;
		
		this.sourceCanShowChange = sourceCanShowChange;
		this.destinationCanShowChange = destinationCanShowChange;
	}

	public Date getTransactionDate()
	{
		return transactionDate;
	}
	
	public double getTransactionAmount() {
		return transactionAmount;
	}

	public String getSourceActionString() {
		return sourceActionString;
	}

	public String getTransactionSource() {
		return transactionSource;
	}

	public double getSourceOriginal() {
		return sourceOriginal;
	}

	public double getSourceUpdated() {
		return sourceUpdated;
	}

	public String getDestinationActionString() {
		return destinationActionString;
	}

	public String getTransactionDestination() {
		return transactionDestination;
	}

	public double getDestinationOriginal() {
		return destinationOriginal;
	}

	public double getDestinationUpdated() {
		return destinationUpdated;
	}

	public boolean isSourceCanShowChange() {
		return sourceCanShowChange;
	}

	public boolean isDestinationCanShowChange() {
		return destinationCanShowChange;
	}
	
	
}
