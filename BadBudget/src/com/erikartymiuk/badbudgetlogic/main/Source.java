package com.erikartymiuk.badbudgetlogic.main;

import java.util.Date;

/** A source can be used to pay for losses. Depending on the class implementing this it could either reduce a positive balance
 * (a checking account) or potentially increase a negative one (a credit card)
 */
public interface Source {
					
	/** A source needs to have funds or credit available so it can have money pulled from it for
	 * whatever purpose the funds are needed. This is the predict loss implementation for use
	 * with the prediction algorithm. It processes the loss for this source by updating its
	 * prediction rows appropriately (i.e. removing or adding to the sources funds or credit). This
	 * method should also be capable of handling an add back to the source.
	 * 
	 * @param destinationDescription - a string description of what the funds are being used for. Used for the transaction history
	 * @param lossAmount - the amount of funds needed
	 * @param addBack - true if the destination is a budget item and that budget item has funds to add back to the source
	 * @param addBackAmount - the amount to add back to the source (if applicable)
	 * @param dayIndex - the date as an index or offset from prediction start date
	 * 
	 */
	public void predictLossForDayIndex(String destinationDescription, double lossAmount, boolean addBack, double addBackAmount, int dayIndex);
	
	/**
	 * A source should be able to tells us a unique description of itself (its name).
	 * @return this source's unique name
	 */
	public String name();
}
