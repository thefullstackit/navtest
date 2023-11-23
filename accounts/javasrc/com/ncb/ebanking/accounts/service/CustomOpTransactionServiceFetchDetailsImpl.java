package com.ncb.ebanking.accounts.service;

import com.infosys.ebanking.common.EBIncidenceCodes;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.exception.DALException;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.dal.QueryOperator;
import com.infosys.feba.framework.transaction.pattern.AbstractLocalInquiryTran;
import com.infosys.feba.framework.types.lists.FEBAArrayList;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.infosys.fentbase.types.primitives.CmCode;
import com.ncb.ebanking.types.valueobjects.CustomTransHistoryReqDetailsVO;
import com.ncb.ebanking.types.valueobjects.CustomTransactionHistoryEnquiryVO;

public class CustomOpTransactionServiceFetchDetailsImpl extends	AbstractLocalInquiryTran 
{
	@SuppressWarnings("unchecked")
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext objContext, IFEBAValueObject objInputOutput,IFEBAValueObject objTxnWM) 
	throws BusinessException,BusinessConfirmation, CriticalException
	{
		return new FEBAValItem[]{};
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public void process(FEBATransactionContext ebContext,IFEBAValueObject objInputOutput, IFEBAValueObject objTxnWM)
			throws BusinessException, BusinessConfirmation, CriticalException 
	{
		/*final EBTransactionContext ebTranContext = (EBTransactionContext) ebContext;*/
		
		FEBAArrayList<CustomTransHistoryReqDetailsVO> txnList = new FEBAArrayList();
		
		CustomTransactionHistoryEnquiryVO custTxnEnquiryVO = (CustomTransactionHistoryEnquiryVO)objInputOutput;
		QueryOperator queryOperator = QueryOperator.openHandle(ebContext,"CustomFetchTxnHistoryRequests");
		queryOperator.associate("bankId",  ebContext.getBankId());
		queryOperator.associate("userId",ebContext.getUserId());
		queryOperator.associate("orgId",ebContext.getCorpId());		
		final FEBAArrayList<CmCode> statusArrayList = new FEBAArrayList();
		statusArrayList.add(new CmCode("SUB"));
		statusArrayList.add(new CmCode("PRO"));
		statusArrayList.add(new CmCode("COM"));
		queryOperator.associate("status", statusArrayList);
		try 
		{
			txnList = queryOperator.fetchList(ebContext);
		} 
		catch (DALException e) 
		{
			throw new BusinessException(ebContext,
                EBIncidenceCodes.NO_RECORDS_FOUND,
                "No Records fetched from dal Query",
                EBankingErrorCodes.NO_RECORDS_FOUND);
		}
		if (txnList.size()==0) 
		{
			throw new BusinessException(ebContext,
                EBIncidenceCodes.NO_RECORDS_FOUND,
                "No Records fetched from dal Query",
                EBankingErrorCodes.NO_RECORDS_FOUND);
		}
		
		custTxnEnquiryVO.setReqResultList(txnList);
		
	}
    
	   
    
}