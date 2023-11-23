/**
 * 
 */
package com.ncb.ebanking.accounts.service;

import com.infosys.ebanking.common.EBIncidenceCodes;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.ebanking.common.validators.UserEntityUtility;
import com.infosys.ebanking.types.TypesCatalogueConstants;
import com.infosys.ebanking.types.primitives.AccountTypeIndicator;
import com.infosys.ebanking.types.valueobjects.AccountEnquiryVO;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran;
import com.infosys.feba.framework.types.lists.FEBAArrayList;
import com.infosys.feba.framework.types.valueobjects.FEBAAVOFactory;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.infosys.fentbase.types.valueobjects.AccountVO;

/**
 * @author Rama_Ujjina
 *
 */
public class CustomCreditCardServiceFetchDetailsImpl extends AbstractHostInquiryTran{
	
	@SuppressWarnings("deprecation")
	@Override
	protected void processHostData(FEBATransactionContext objTxnContext, IFEBAValueObject objInputOutput,
			IFEBAValueObject objTxnWM) throws BusinessException, BusinessConfirmation, CriticalException {

		
		AccountEnquiryVO actEnquiryVO = (AccountEnquiryVO) objInputOutput;

		AccountVO actVO = (AccountVO) FEBAAVOFactory
				.createInstance(TypesCatalogueConstants.AccountVO);
		
		actVO.getAccountTypeER().setAccountType(actEnquiryVO.getCriteria().getAccountTypeER().getAccountType());
		if(null != actEnquiryVO.getCriteria().getAccountIDER().getAccountID())
		{
			actVO.getAccountIDER().setAccountID(actEnquiryVO.getCriteria().getAccountIDER().getAccountID());
		}
		FEBAArrayList accountList=null;
		try {
			accountList = (FEBAArrayList) UserEntityUtility
					.fetchFilteredAccountsList(objTxnContext, actVO, new AccountTypeIndicator("O"));
		} catch (BusinessException e) {
				 throw new BusinessException(objTxnContext,
                  EBIncidenceCodes.NO_ACCOUNTS_FOUND,"",
                  EBankingErrorCodes.NO_ACCOUNTS_FOUND);
		} catch (CriticalException e) {
			 throw new BusinessException(objTxnContext,
                  EBIncidenceCodes.NO_ACCOUNTS_FOUND,"",
                  EBankingErrorCodes.NO_ACCOUNTS_FOUND);
		} 
		actEnquiryVO.setResultList(accountList);
    	
	}

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0, IFEBAValueObject arg1,
			IFEBAValueObject arg2) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void processLocalData(FEBATransactionContext arg0, IFEBAValueObject arg1, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		
	}


}
