/**
 * 
 */
package com.ncb.ebanking.accounts.service;

import com.infosys.ebanking.common.EBIncidenceCodes;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.hif.EBHostInvoker;
import com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.ncb.ebanking.common.CustomEBRequestConstants;
import com.ncb.ebanking.types.valueobjects.CustomCCEnquiryVO;

/**
 * @author Rama_Ujjina
 *
 */
public class CustomCreditCardServiceFetchBalancesImpl extends AbstractHostInquiryTran{
	
	protected void processHostData(FEBATransactionContext objTxnContext, IFEBAValueObject objInputOutput,
			IFEBAValueObject objTxnWM) throws BusinessException, BusinessConfirmation, CriticalException {

		
		CustomCCEnquiryVO ccEnquiryVO = (CustomCCEnquiryVO) objInputOutput;
		try
		{
			EBHostInvoker.processRequest(objTxnContext, CustomEBRequestConstants.CUSTOM_CREDIT_CARD_BAL_REQUEST, ccEnquiryVO);
		}
		catch(Exception e)
		{
			throw new BusinessException(objTxnContext, EBIncidenceCodes.BALANCE_COULD_NOT_BE_FETCHED, "Balance not fetched", EBankingErrorCodes.BALANCE_NOT_AVAILABLE); 
		}
        if(ccEnquiryVO.getBalanceResultList().size()==0)
        {
        	throw new BusinessException(objTxnContext, EBIncidenceCodes.BALANCE_COULD_NOT_BE_FETCHED, "Balance not fetched", EBankingErrorCodes.BALANCE_NOT_AVAILABLE);
        	
			
		} 
    	
	}

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0, IFEBAValueObject arg1,
			IFEBAValueObject arg2) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		return new FEBAValItem[]{};
	}

	@Override
	protected void processLocalData(FEBATransactionContext arg0, IFEBAValueObject arg1, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		
	}


}
