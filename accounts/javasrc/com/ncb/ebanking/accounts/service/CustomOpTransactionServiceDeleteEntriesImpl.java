/*
 *
 * COPYRIGHT NOTICE:
 * Copyright (c) 2007 Infosys Technologies Limited, Electronic City,
 * Hosur Road, Bangalore - 560 100, India.
 * All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Infosys Technologies Ltd. ("Confidential Information"). You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Infosys.
 */
package com.ncb.ebanking.accounts.service;

import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.exception.DALException;
import com.infosys.feba.framework.common.logging.LogManager;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.dal.QueryOperator;
import com.infosys.feba.framework.transaction.pattern.AbstractLocalUpdateTran;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.ncb.ebanking.common.CustomEBankingQueryIdentifiers;
import com.ncb.ebanking.types.valueobjects.CustomTransHistoryReqDetailsVO;

/**
 * This class will delete the records for Transaction History
 * 
 * @author Abhishek_Rana01
 * @version 1.0
 * @since FEBA 2.0
 */

public class CustomOpTransactionServiceDeleteEntriesImpl extends
		AbstractLocalUpdateTran {
	/**
	 * Validations for the data submitted by the user.
	 */

	@Override
	public FEBAValItem[] prepareValidationsList(
			FEBATransactionContext objContext, IFEBAValueObject objInputOutput,
			IFEBAValueObject objTxnWM) throws BusinessException,
			BusinessConfirmation, CriticalException {
		return new FEBAValItem[]{};
	}

	@Override
	/**
	 * Method to prepare the data to be inserted into the SCDT table for batch
	 * to execute and process the it.
	 * 
	 * @author Abhishek_Rana01
	 */
	public void process(FEBATransactionContext transactionContext,
			IFEBAValueObject customVO, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		
		CustomTransHistoryReqDetailsVO detailsVO = (CustomTransHistoryReqDetailsVO)customVO;
		QueryOperator queryOperator = QueryOperator.openHandle(
				transactionContext,
				CustomEBankingQueryIdentifiers.DELETE_REQUEST_THIS);
		// Associate fields to the fdttRequestCountOperator
		queryOperator.associate("bankId", transactionContext.getBankId());
		queryOperator.associate("requestId", detailsVO.getRequestId());

		try {			
			queryOperator.deleteByCriteria(transactionContext);			
		} catch (DALException de) {
			LogManager.log(transactionContext, de.getMessage(), LogManager.MESSAGE);
		}


	}

}
