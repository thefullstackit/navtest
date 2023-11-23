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

import com.infosys.ebanking.common.EBankingConstants;
import com.infosys.ebanking.types.valueobjects.OpTxnHistoryCritVO;
import com.infosys.ebanking.types.valueobjects.OpTxnHistoryEnquiryVO;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.exception.DALException;
import com.infosys.feba.framework.common.logging.LogManager;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.dal.QueryOperator;
import com.infosys.feba.framework.tao.FEBATableOperatorException;
import com.infosys.feba.framework.transaction.pattern.AbstractLocalUpdateTran;
import com.infosys.feba.framework.types.lists.FEBAArrayList;
import com.infosys.feba.framework.types.primitives.FEBAUnboundChar;
import com.infosys.feba.framework.types.primitives.FEBAUnboundString;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.infosys.fentbase.types.primitives.Count;
import com.ncb.ebanking.common.CustomEBankingQueryIdentifiers;
import com.ncb.ebanking.tao.CDRTTAO;
import com.ncb.ebanking.tao.info.CDRTInfo;
import com.ncb.ebanking.types.valueobjects.CustomTransHistoryReqDetailsVO;

/**
 * This class will create fetch the criteria for transaction history downloads
 * 
 * @author Abhishek_Rana01
 * @version 1.0
 * @since FEBA 2.0
 */

public class CustomOpTransactionServiceFetchTxnHistoryImpl extends
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
		
		OpTxnHistoryEnquiryVO customEnqVO = (OpTxnHistoryEnquiryVO)customVO;
		CustomTransHistoryReqDetailsVO customDetailsVO = (CustomTransHistoryReqDetailsVO)customEnqVO.getExtensionVO();
		OpTxnHistoryCritVO critVO = customEnqVO.getCriteria();
		critVO.getAccountIDER().setAccountID(customDetailsVO.getAccountId());
		critVO.getAccountIDER().setAccountType(customDetailsVO.getAccountType());
		critVO.getAccountTypeER().setAccountType(customDetailsVO.getAccountType());
		critVO.getAccountIDER().setNickName(customDetailsVO.getAccountNickname());
		critVO.getAccountIDER().setCurrency(customDetailsVO.getCrnCode());
		critVO.getAccountIDER().setBranchCode(customDetailsVO.getBranchCode());
		critVO.getMainAccountTypeER().setMainAccountType(EBankingConstants.MAIN_ACCOUNT_TYPE_OPERATIVE);
		critVO.getAccountIDER().setBankCode(transactionContext.getBankId().getValue());
		critVO.getDateRangeVO().setFromDate(customDetailsVO.getFromDate());
		critVO.getDateRangeVO().setToDate(customDetailsVO.getToDate());
		if(customDetailsVO.getNoOfTxns().getValue() != 0)
		{
			critVO.setLastNTransactions(new Count(customDetailsVO.getNoOfTxns().getValue()));
		}
		customEnqVO.setCriteria(critVO);
		QueryOperator queryOperator = QueryOperator.openHandle(
				transactionContext,
				CustomEBankingQueryIdentifiers.FETCH_TXN_HIST_DET);
		// Associate fields to the fdttRequestCountOperator
		queryOperator.associate("bankId", transactionContext.getBankId());
		queryOperator.associate("requestId", customDetailsVO.getRequestId());
		try {

			FEBAArrayList<CustomTransHistoryReqDetailsVO> list = queryOperator.fetchList(transactionContext);
			customEnqVO.setResultList(list);
			CDRTTAO cdrtTAO = new CDRTTAO(transactionContext);
			CDRTInfo cdrtInfo = new CDRTInfo();
			try {
					cdrtInfo = CDRTTAO.select(transactionContext, customDetailsVO.getRequestId());
			} catch (FEBATableOperatorException e1) {
				LogManager.log(transactionContext, e1.getMessage(), LogManager.MESSAGE);
			}
			cdrtTAO.associateReqId(customDetailsVO.getRequestId());
			cdrtTAO.associateCookie(cdrtInfo.getCookie());
			cdrtTAO.associateNoOfDownloads(new FEBAUnboundChar(EBankingConstants.YES_FLAG));
			try {
				cdrtTAO.update(transactionContext);
			} catch (FEBATableOperatorException e1) {
				LogManager.log(transactionContext, e1.getMessage(), LogManager.MESSAGE);
			}

		} catch (DALException e) {
			// return true if no records are fetched from the database
			LogManager.log(transactionContext, e.getMessage(), LogManager.MESSAGE);
		} finally {
			// Close fdttRequestCountOperator handle
			queryOperator.closeHandle(transactionContext);
		}


	}

}
