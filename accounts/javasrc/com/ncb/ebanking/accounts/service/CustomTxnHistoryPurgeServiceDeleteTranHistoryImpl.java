/**

 * CustomTxnHistoryPurgeServiceDeleteTranHistoryImpl.java

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
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.exception.DALException;
import com.infosys.feba.framework.common.logging.LogManager;
import com.infosys.feba.framework.common.util.DateUtil;
import com.infosys.feba.framework.common.util.resource.PropertyUtil;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.dal.QueryOperator;
import com.infosys.feba.framework.transaction.pattern.AbstractLocalUpdateTran;
import com.infosys.feba.framework.types.lists.FEBAArrayList;
import com.infosys.feba.framework.types.primitives.FEBADate;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.ncb.ebanking.common.CustomEBankingQueryIdentifiers;
import com.ncb.ebanking.types.valueobjects.CustomTransactionHistoryEnquiryVO;

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

public class CustomTxnHistoryPurgeServiceDeleteTranHistoryImpl extends AbstractLocalUpdateTran {

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0, IFEBAValueObject arg1,
			IFEBAValueObject arg2) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		return new FEBAValItem[]{};
	}

	@Override
	public void process(FEBATransactionContext objContext, IFEBAValueObject objInputOutput, IFEBAValueObject objTxnWM)
			throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		CustomTransactionHistoryEnquiryVO enqVo = (CustomTransactionHistoryEnquiryVO) objInputOutput;

		EBTransactionContext ebCtx = (EBTransactionContext) objContext;
		int txnHistoryExpPeriod = Integer.parseInt(PropertyUtil.getProperty("CUSTOM_TXN_HISTORY_EXPIRY_PERIOD_IN_DAYS", ebCtx));
		
		FEBADate curDate = new FEBADate(DateUtil.getCurrentDate(objContext));
		curDate.getDecrementDate(EBankingConstants.DAYS_FEBADATE, txnHistoryExpPeriod);
		
		final QueryOperator queryOperator = QueryOperator.openHandle(
                objContext,CustomEBankingQueryIdentifiers.FETCH_CRDT_TRAN_HISTORY_REQUEST);
		
        try {
            queryOperator.associate(EBankingConstants.QUERY_PARAM_BANK_ID, ebCtx.getBankId());
    		queryOperator.associate("rCreTime", curDate);
    		FEBAArrayList resultList = queryOperator.fetchList(objContext);
    		enqVo.setReqResultList(resultList);
        }
        catch (DALException dalEx){
           LogManager.log(objContext, dalEx.getMessage(), LogManager.MESSAGE);
        }
        finally {
           queryOperator.closeHandle(objContext);
        }
	}
	
}
