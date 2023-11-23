package com.custom.ebanking.moby.accounts.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * MobyLoanAccountInquiryServiceLoanInquiryImpl.java
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
import com.infosys.ebanking.moby.types.valueobjects.MobyLoanAccountInquiryParentVO;
import com.infosys.ebanking.moby.types.valueobjects.MobyLoanAccountInquiryVO;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.util.resource.PropertyUtil;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.hif.EBHostInvoker;
import com.infosys.feba.framework.transaction.pattern.AbstractHostUpdateTran;
import com.infosys.feba.framework.types.lists.FEBAArrayList;
import com.infosys.feba.framework.types.primitives.FEBAAmount;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValEngineConstants;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.ncb.ebanking.common.CustomEBRequestConstants;
import com.ncb.ebanking.types.valueobjects.CustomMobyLoanAccountInquiryVO;

/**
 * This class will validate all the details of the Loan account 
 * and then get the details of  the account
 */



public class CustomMobyLoanAccountInquiryServiceLoanInquiryImpl extends AbstractHostUpdateTran
{

	/**
	 * This method will prepare the validation list for all the validations to
	 * be done*/

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0,
			IFEBAValueObject arg1, IFEBAValueObject arg2)
					throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		MobyLoanAccountInquiryParentVO mobyLoanAccountInquiryParentVO = (MobyLoanAccountInquiryParentVO) arg1;
		final FEBAValItem val[] = new FEBAValItem[] {
				new FEBAValItem(MobyConstants.SBCA_ACCTID,
						mobyLoanAccountInquiryParentVO.getCriteria().getAcctId(),
						FEBAValEngineConstants.MANDATORY,
						FEBAValEngineConstants.INDEPENDENT,
						EBankingErrorCodes.ACC_ID_MANDATORY),
		};
		return val;
	}
	/**
	 * This method will do the get operation for the given Loan Account 
	 * 
	 */


	@Override
	protected void processHostData(FEBATransactionContext objContext,
			IFEBAValueObject objInputOutput, IFEBAValueObject objTxnWM)
					throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		MobyLoanAccountInquiryParentVO mobyLoanAccountInquiryParentVO = (MobyLoanAccountInquiryParentVO) objInputOutput;
		EBTransactionContext ebContext = (EBTransactionContext) objContext;

		MobyUtil mobyUtil = new MobyUtil();
		mobyLoanAccountInquiryParentVO.getCriteria().setReqUUID(mobyUtil.reqUuidGenerator());

		mobyLoanAccountInquiryParentVO.getCriteria().setBankId(PropertyUtil.getProperty(MobyConstants.CORE_BANK_ID, ebContext));
		mobyLoanAccountInquiryParentVO.getCriteria().setChannelId(PropertyUtil.getProperty(MobyConstants.FI_CHANNEL_ID_MBY, ebContext));
		mobyLoanAccountInquiryParentVO.getCriteria().setServiceRqstVersion(MobyConstants.FI_SERVICE_RQST_VERSION);
		mobyLoanAccountInquiryParentVO.getCriteria().setMessageDateTime(mobyUtil.messageDateTimeGenerator11x(ebContext));

		EBHostInvoker
		.processRequest(
				objContext,
				CustomEBRequestConstants.CUSTOM_MOBI_LOAN_ACCOUNT_INQUIRY,
				mobyLoanAccountInquiryParentVO);

		FEBAArrayList<MobyLoanAccountInquiryVO> valuelist = new  FEBAArrayList<MobyLoanAccountInquiryVO>();
		valuelist=mobyLoanAccountInquiryParentVO.getResultList();
		String date;
		int latestDatePRDEMIndex = 0;

		long currentTimeMillis = System.currentTimeMillis();

		Long datesDiff = 0L;

		Map<Integer,Long> pastDateDiffMap = new LinkedHashMap<Integer,Long>();
		Map<Integer,Long> futureDateDiffMap = new LinkedHashMap<Integer,Long>();
		for(int i=0;i<valuelist.size();i++)
		{
			MobyLoanAccountInquiryVO mobyLoanAccountList=valuelist.get(i);

			if(mobyLoanAccountList.getRespInstallmentIdArray().toString().equals("PRDEM"))
			{


				date=mobyLoanAccountList.getRespInstallStartDtArray().toString();
				SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
				try {
					Date currRecDate=sdf.parse(date);													

					datesDiff=currRecDate.getTime()-currentTimeMillis;
					if(datesDiff<0){

						pastDateDiffMap.put(i,Math.abs(datesDiff));

					}
					else{
						futureDateDiffMap.put(i,datesDiff);
					}


				} catch (ParseException e) {

					e.printStackTrace();
				}      
				
			}else 
				if(mobyLoanAccountList.getRespInstallmentIdArray().toString().equals("EIDEM"))
				{	   System.out.println("aj..inside EIDEM");     	
					MobyLoanAccountInquiryVO mobyLoanAccountEIDEM1=valuelist.get(0);
					CustomMobyLoanAccountInquiryVO customMobyLoanAccountInquiryVO = (CustomMobyLoanAccountInquiryVO) mobyLoanAccountEIDEM1.getExtensionVO();
					System.out.println("puru mobyLoanAccountEIDEM1:"+mobyLoanAccountEIDEM1);
					mobyLoanAccountInquiryParentVO.getCriteria().setRespMnthlyInstlmntAmount(mobyLoanAccountEIDEM1.getRespFlowAmtAmountValueArray());
					customMobyLoanAccountInquiryVO.setRespNextDemandDate(customMobyLoanAccountInquiryVO.getRespNextDemandDateArray());
					mobyLoanAccountEIDEM1.setExtensionVO(customMobyLoanAccountInquiryVO);
					System.out.println("aj..mobyLoanAccountEIDEM1 extension VO.."+mobyLoanAccountEIDEM1);
					//mobyLoanAccountInquiryParentVO.setExtensionVO(customMobyLoanAccountInquiryVO);
					mobyLoanAccountInquiryParentVO.getCriteria().setRespNoOfInstall(mobyLoanAccountEIDEM1.getRespNoOfInstallArray().getValue());
					mobyLoanAccountInquiryParentVO.getCriteria().setRespFlowAmtAmountValue(mobyLoanAccountEIDEM1.getRespFlowAmtAmountValueArray());
					mobyLoanAccountInquiryParentVO.getCriteria().setRespFlowAmtCurrencyCode(mobyLoanAccountEIDEM1.getRespFlowAmtCurrencyCodeArray());
					System.out.println("puru vo:MnthlyInstlmntAmount:EIDEM:"+mobyLoanAccountInquiryParentVO.getCriteria().getRespMnthlyInstlmntAmount());
					System.out.println("aj..mobyLoanAccountInquiryParentVO.."+mobyLoanAccountInquiryParentVO);
				} 
		}      	        


		if(pastDateDiffMap.size()!=0){
			latestDatePRDEMIndex=findLatestDateIndex(pastDateDiffMap);

		}
		else if(futureDateDiffMap.size()!=0){
			latestDatePRDEMIndex=findLatestDateIndex(futureDateDiffMap);
		}


		if(latestDatePRDEMIndex>=0){
			System.out.println("aj..inside PRDEM");  
			MobyLoanAccountInquiryVO mobyLoanAccountList=valuelist.get(latestDatePRDEMIndex);
			CustomMobyLoanAccountInquiryVO customMobyLoanAccountInquiryVOPRDEM = (CustomMobyLoanAccountInquiryVO) mobyLoanAccountList.getExtensionVO();
			customMobyLoanAccountInquiryVOPRDEM.setRespNextDemandDate(customMobyLoanAccountInquiryVOPRDEM.getRespNextDemandDateArray());
			//mobyLoanAccountList.setExtensionVO(customMobyLoanAccountInquiryVOPRDEM);
			
			mobyLoanAccountInquiryParentVO.getCriteria().setRespMnthlyInstlmntAmount(new FEBAAmount(mobyLoanAccountList.getRespFlowAmtAmountValueArray().getValue()));
			mobyLoanAccountInquiryParentVO.getCriteria().setRespNoOfInstall(mobyLoanAccountList.getRespNoOfInstallArray().getValue());
			mobyLoanAccountInquiryParentVO.getCriteria().setRespFlowAmtAmountValue(new FEBAAmount(mobyLoanAccountList.getRespFlowAmtAmountValueArray().getValue()));
			mobyLoanAccountInquiryParentVO.getCriteria().setRespFlowAmtCurrencyCode(mobyLoanAccountList.getRespFlowAmtCurrencyCodeArray().getValue());
			mobyLoanAccountInquiryParentVO.getCriteria().setExtensionVO(customMobyLoanAccountInquiryVOPRDEM);
			System.out.println("aj..mobyLoanAccountInquiryParentVO"+mobyLoanAccountInquiryParentVO);
			System.out.println("aj..mobyLoanAccountList"+mobyLoanAccountList);
			System.out.println("puru vo:MnthlyInstlmntAmount:PRDEM:"+mobyLoanAccountInquiryParentVO.getCriteria().getRespMnthlyInstlmntAmount());
		}

	}


	private int findLatestDateIndex(Map<Integer, Long> inputMap) {
		int index=0;
		Entry<Integer,Long> min = null;
		for(Entry<Integer,Long> entry:inputMap.entrySet()){
			if(min==null || min.getValue()>entry.getValue()){
				min = entry;
				index=entry.getKey();
			}
		}	
		return index;
	}
	@Override
	protected void processLocalData(FEBATransactionContext arg0,
			IFEBAValueObject arg1, IFEBAValueObject arg2)
					throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub

	}

}
