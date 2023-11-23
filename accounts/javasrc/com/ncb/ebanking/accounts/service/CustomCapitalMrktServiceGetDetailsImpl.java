package com.ncb.ebanking.accounts.service;
/**
 * 
 */

import java.util.Iterator;

import com.infosys.ebanking.common.EBIncidenceCodes;
import com.infosys.ebanking.common.EBTransactionContext;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.ebanking.types.valueobjects.AccountEnquiryVO;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.transaction.pattern.AbstractLocalInquiryTran;
import com.infosys.feba.framework.types.lists.FEBAArrayList;
import com.infosys.feba.framework.types.lists.IFEBAList;
import com.infosys.feba.framework.types.valueobjects.FEBAAVOFactory;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.infosys.fentbase.common.FBAConstants;
import com.infosys.fentbase.common.validators.RMUserEntityUtility;
import com.infosys.fentbase.types.primitives.AccountId;
import com.infosys.fentbase.types.primitives.EntityName;
import com.infosys.fentbase.types.valueobjects.AccountVO;
import com.infosys.fentbase.types.valueobjects.IAccountVO;

/**
 * @author Rama_Ujjina
 *
 */
public class CustomCapitalMrktServiceGetDetailsImpl extends AbstractLocalInquiryTran{

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0, IFEBAValueObject arg1,
			IFEBAValueObject arg2) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void process(FEBATransactionContext objContext, IFEBAValueObject objInputOutput, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		
		EBTransactionContext ebContextObj = (EBTransactionContext) objContext;
		AccountEnquiryVO actEnquiryVO = (AccountEnquiryVO)  objInputOutput;
		// Declare the variables
		AccountVO acctVOObj = (AccountVO) FEBAAVOFactory
				.createInstance("com.infosys.fentbase.types.valueobjects.AccountVO");
		IFEBAList userEntityList = null;
		
		try {
			/* Fetching the account details from UEDT */
			userEntityList = (FEBAArrayList) RMUserEntityUtility
					.fetchAccounts(ebContextObj, new EntityName(
							FBAConstants.ACCOUNT_ENTITY));
			System.out.println("userEntityList::::::"+userEntityList);
			FEBAArrayList<AccountVO> newResultList = new FEBAArrayList();
			
			for (int i = 0; i < userEntityList.size(); i++) {
				 acctVOObj = (AccountVO) userEntityList.get(i);
		            if (acctVOObj.getAccountTypeER().getAccountType().toString()
		                    .equalsIgnoreCase("OIP") && acctVOObj.getAccountStatus().toString().equalsIgnoreCase("001")) {
		               // resultantAccountVO.set(uedtAccountVO);
		            	/*acctVOObj.setAccntNum(acctVOObj.getAccountIDER().getAccountID());
		            	acctVOObj.setAccType(acctVOObj.getAccountIDER().getAccountType());*/
		            	newResultList.addObject(acctVOObj);
		            }
			}
			actEnquiryVO.setResultList(newResultList);
			
			

			System.out.println("newResultList:::::::::" + newResultList);
			
		} catch (BusinessException e) {
			throw new BusinessException(ebContextObj,
					EBIncidenceCodes.NO_RECORDS_RETREIVED_FROM_UEDT,
					EBankingErrorCodes.INVALID_CREDIT_CARD_NUMBER);
		}
		
	}
	
	

}
