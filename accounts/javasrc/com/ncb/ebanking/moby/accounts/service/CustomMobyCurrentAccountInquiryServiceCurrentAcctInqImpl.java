package com.ncb.ebanking.moby.accounts.service;

/**
 * MobyCurrentAccountInquiryServiceCurrentAcctInqImpl.java
 *
 * COPYRIGHT NOTICE:
 * Copyright (c) 2018 Infosys Technologies Limited.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Infosys Technologies Ltd. ("Confidential Information"). You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Infosys.
 */
import com.infosys.ebanking.common.EBTransactionContext;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.ebanking.moby.common.MobyConstants;
import com.infosys.ebanking.moby.common.MobyUtil;
import com.infosys.ebanking.moby.types.valueobjects.MobyCurrentAccountInquiryParentVO;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.util.resource.PropertyUtil;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.hif.EBHostInvoker;
import com.infosys.feba.framework.transaction.pattern.AbstractHostUpdateTran;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValEngineConstants;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.ncb.ebanking.common.CustomEBRequestConstants;

/**
 * This class will validate all the details of the account
 * and then get the details of the account
 */
 


public class CustomMobyCurrentAccountInquiryServiceCurrentAcctInqImpl extends AbstractHostUpdateTran {
    /**
     * This method will prepare the validation list for all the validations to
     * be done*/
	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0,
			IFEBAValueObject arg1, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		MobyCurrentAccountInquiryParentVO mobyCurrentAccountInquiryParentVO = (MobyCurrentAccountInquiryParentVO) arg1;
		final FEBAValItem val[] = new FEBAValItem[] {
				new FEBAValItem(MobyConstants.SBCA_ACCTID,
						mobyCurrentAccountInquiryParentVO.getCriteria().getAcctId(),
						FEBAValEngineConstants.MANDATORY,
						FEBAValEngineConstants.INDEPENDENT,
						EBankingErrorCodes.ACC_ID_MANDATORY),
		};
		return val;
		
	}

/**
     * This method will do the get operation for the mentioned account 
     * 
     */	
	
	@Override
	protected void processHostData(FEBATransactionContext objContext,
			IFEBAValueObject objInputOutput, IFEBAValueObject objTxnWM)
			throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		
		MobyCurrentAccountInquiryParentVO mobyCurrentAccountInquiryParentVO = (MobyCurrentAccountInquiryParentVO) objInputOutput;
		EBTransactionContext ebContext = (EBTransactionContext) objContext;
		MobyUtil mobyUtil = new MobyUtil();
		
		mobyCurrentAccountInquiryParentVO.getCriteria().setReqUUID(mobyUtil.reqUuidGenerator());
		mobyCurrentAccountInquiryParentVO.getCriteria().setGlobalUUID(mobyUtil.globalUuidGenerator11x());
		mobyCurrentAccountInquiryParentVO.getCriteria().setBankId(PropertyUtil.getProperty(MobyConstants.CORE_BANK_ID, ebContext));
		mobyCurrentAccountInquiryParentVO.getCriteria().setChannelId(PropertyUtil.getProperty(MobyConstants.FI_CHANNEL_ID_MBY, ebContext));
		mobyCurrentAccountInquiryParentVO.getCriteria().setServiceRqstVersion(MobyConstants.FI_SERVICE_RQST_VERSION);
		mobyCurrentAccountInquiryParentVO.getCriteria().setMessageDateTime(mobyUtil.messageDateTimeGenerator11x(ebContext));
		EBHostInvoker
		.processRequest(
				objContext,
				CustomEBRequestConstants.CUSTOM_MOBI_CURRENT_ACC_INQ,
				mobyCurrentAccountInquiryParentVO);
		
		
	}

	@Override
	protected void processLocalData(FEBATransactionContext arg0,
			IFEBAValueObject arg1, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		
	} 

}
