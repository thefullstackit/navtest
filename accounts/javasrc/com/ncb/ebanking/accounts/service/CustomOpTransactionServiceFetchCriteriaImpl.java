/*
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
import com.infosys.feba.framework.types.lists.FEBAArrayList;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.infosys.fentbase.types.primitives.CmCode;
import com.ncb.ebanking.common.CustomEBankingQueryIdentifiers;
import com.ncb.ebanking.types.valueobjects.CustomTransHistoryReqDetailsVO;
import com.ncb.ebanking.types.valueobjects.CustomTransactionHistoryEnquiryVO;

/**
 * This class will create fetch the criteria for transaction history downloads
 * 
 * @author Abhishek_Rana01
 * @version 1.0
 * @since FEBA 2.0
 */

public class CustomOpTransactionServiceFetchCriteriaImpl extends
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
	 * Method to call the DAL and fetch the transaction history criteria
	 * 
	 * @author Abhishek_Rana01
	 */
	public void process(FEBATransactionContext transactionContext,
			IFEBAValueObject customVO, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		
		CustomTransactionHistoryEnquiryVO customEnqVO = (CustomTransactionHistoryEnquiryVO)customVO;
		QueryOperator cdrtRequestCountOperator = QueryOperator.openHandle(
				transactionContext,
				CustomEBankingQueryIdentifiers.FETCH_TXN_HIST_REQ);
		// Associate fields to the fdttRequestCountOperator
		cdrtRequestCountOperator.associate("bankId", transactionContext.getBankId());
		cdrtRequestCountOperator.associate("requestId", customEnqVO.getCriteriaVO().getRequestId());
		final FEBAArrayList<CmCode> statusArrayList = new FEBAArrayList();
		statusArrayList.add(new CmCode("SUB"));
		statusArrayList.add(new CmCode("PRO"));
		cdrtRequestCountOperator.associate("status", statusArrayList);
		try {

			FEBAArrayList<CustomTransHistoryReqDetailsVO> list = cdrtRequestCountOperator
					.fetchList(transactionContext);
			customEnqVO.setReqResultList(list);

		} catch (DALException e) {
			// return true if no records are fetched from the database
			LogManager.log(transactionContext, e.getMessage(), LogManager.MESSAGE);
		} finally {
			// Close fdttRequestCountOperator handle
			cdrtRequestCountOperator.closeHandle(transactionContext);
		}


	}

}
