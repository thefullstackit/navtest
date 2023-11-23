/**

 * CustomTxnHistoryPurgeServiceUpdateStatusImpl.java

 * @since Mar 5, 2020

 * @author Praveen_B08

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

import com.infosys.ebanking.common.EBTransactionContext;
import com.infosys.ebanking.common.EBankingConstants;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.exception.DALException;
import com.infosys.feba.framework.common.exception.TranCommitBusinessException;
import com.infosys.feba.framework.common.logging.LogManager;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.dal.QueryOperator;
import com.infosys.feba.framework.tao.FEBATableOperatorException;
import com.infosys.feba.framework.transaction.pattern.AbstractLocalUpdateTran;
import com.infosys.feba.framework.types.primitives.FEBAUnboundString;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.ncb.ebanking.common.CustomEBankingQueryIdentifiers;
import com.ncb.ebanking.tao.CDRTTAO;
import com.ncb.ebanking.tao.info.CDRTInfo;
import com.ncb.ebanking.types.valueobjects.CustomTransHistoryReqDetailsVO;

/**
 *
 *
 *
 * This class is delete the entries in CDDT table after the records are expired <BR>
 *
 * @author Praveen_B08
 *
 * @version 1.0
 *
 * @since FEBA 2.0
 *
 */

public class CustomTxnHistoryPurgeServiceUpdateStatusImpl extends AbstractLocalUpdateTran {

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0, IFEBAValueObject arg1,
			IFEBAValueObject arg2) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		return new FEBAValItem[]{};
	}

	@Override
	public void process(FEBATransactionContext objContext, IFEBAValueObject objInputOutput, IFEBAValueObject objTxnWM)
			throws BusinessException, BusinessConfirmation, CriticalException {

		CustomTransHistoryReqDetailsVO detailsVO = (CustomTransHistoryReqDetailsVO) objInputOutput;
		EBTransactionContext ebCtx = (EBTransactionContext) objContext;
		
		CDRTTAO cdrtTao = new CDRTTAO(objContext);
		cdrtTao.associateBankId(objContext.getBankId());
		cdrtTao.associateReqId(detailsVO.getRequestId());
		cdrtTao.associateReqStatus(new FEBAUnboundString("EXP"));
		try {
			CDRTInfo cdrtInfo = CDRTTAO.select(objContext, detailsVO.getRequestId());
			cdrtTao.associateCookie(cdrtInfo.getCookie());
			cdrtTao.update(objContext);
		} catch (FEBATableOperatorException e) {
			LogManager.log(objContext, e.getMessage(), LogManager.MESSAGE);
			throw new TranCommitBusinessException(objContext,
                    "CDRTU00001",
                    "Update in CDRT failed",
                    EBankingErrorCodes.UPDATE_NOT_POSSIBLE, e);
		}

		final QueryOperator queryOperator = QueryOperator.openHandle(
                objContext,CustomEBankingQueryIdentifiers.DELETE_REQUEST_THIS);
    	queryOperator.associate(EBankingConstants.QUERY_PARAM_BANK_ID, ebCtx.getBankId());
    	queryOperator.associate("requestId", detailsVO.getRequestId());
    	//call the query operator delete method
    	try {
			queryOperator.deleteByCriteria(objContext);
		} catch (DALException e) {
			 throw new TranCommitBusinessException(
	                    objContext,
	                    "DAL Exception while purging data from CDDT",
	                    "DAL Exception while purging data from CDDT",
	                    EBankingErrorCodes.RECORD_NOT_DELETED, e);
		}

		
	}
	
}
