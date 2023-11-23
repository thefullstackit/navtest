package com.ncb.ebanking.accounts.service;

import com.infosys.ebanking.accounts.service.AccountSummaryUtility;
import com.infosys.ebanking.common.EBIncidenceCodes;
import com.infosys.ebanking.common.EBTransactionContext;
import com.infosys.ebanking.common.EBankingConstants;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.ebanking.common.validators.UserEntityUtility;
import com.infosys.ebanking.types.TypesCatalogueConstants;
import com.infosys.ebanking.types.valueobjects.AccountCritVO;
import com.infosys.ebanking.types.valueobjects.AccountEnquiryVO;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.util.resource.PropertyUtil;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran;
import com.infosys.feba.framework.transaction.pattern.AbstractLocalInquiryTran;
import com.infosys.feba.framework.types.lists.FEBAArrayList;
import com.infosys.feba.framework.types.primitives.FEBAUnboundString;
import com.infosys.feba.framework.types.valueobjects.FEBAAVOFactory;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.infosys.fentbase.types.valueobjects.AccountVO;

public class CustomMerchantNumberServiceGetMerchantNumberImpl  extends AbstractLocalInquiryTran{

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0, IFEBAValueObject arg1,
			IFEBAValueObject arg2) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void process(FEBATransactionContext objTxnContext, IFEBAValueObject objInputOutput, IFEBAValueObject objTxnWM)
			throws  BusinessConfirmation, BusinessException {
		AccountEnquiryVO actEnquiryVO = (AccountEnquiryVO) objInputOutput;
		AccountCritVO criteria = actEnquiryVO.getCriteria();
		criteria.getAccountIDER().setAccountType("MTF");
		criteria.getAccountTypeER().setAccountType("MTF");
		/**
		 * invoke fetchFilteredAccounts of UserEntityUtility and get the list of
		 * accounts associated to the user
		 */
		AccountVO actVO = (AccountVO) FEBAAVOFactory
				.createInstance(TypesCatalogueConstants.AccountVO);
		final EBTransactionContext ebTransactionContext = (EBTransactionContext) objTxnContext;
		final AccountSummaryUtility accountSummaryUtility = AccountSummaryUtility
				.getInstance();
		if (null != criteria.getIsAccountidNickNameSearch()
				&& "YES".equalsIgnoreCase(criteria
						.getIsAccountidNickNameSearch().toString())) {
			ebTransactionContext.hashmap.put("AccountSummaryUX5Search",
					new FEBAUnboundString("Y"));
		}
		actVO = accountSummaryUtility.setValuesinAccountVO(actVO, criteria,
				ebTransactionContext);
		FEBAArrayList accountList=null;
		try {
			accountList = (FEBAArrayList) UserEntityUtility
					.fetchFilteredAccountsList(objTxnContext, actVO, actEnquiryVO
							.getCriteria().getAccountTypeIndicator());
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
		/**
		 * If user is corporate : Query on DVAC using DivisonAccessQuery to get
		 * the list of accounts for the division passed as input. For those
		 * account ids fetched from DVAC and those that match the account ids in
		 * AccountEnquiryVO.resultList make a List of AccountVOs. Replace the
		 * result list of AccountEnquiryVO with the prepared list of AccountVOs
		 */
		/*if (isCorporateUser(ebTransactionContext)) {
			try {
            	actEnquiryVO = accountSummaryUtility.getDivisionAccounts(
                         actEnquiryVO, ebTransactionContext);
            } catch (BusinessException e) {
            		if(e.getErrorCode() == EBankingErrorCodes.NO_ACCOUNTS_FOUND){
            			actEnquiryVO.getResultList().initialize();
            		}
            }
			if (actEnquiryVO.getResultList().size() == 0) {
				throw new BusinessException(
						objTxnContext,
						EBIncidenceCodes.NO_RECORDS_RETREIVED_AFTER_INTERSECTION,
						"", EBankingErrorCodes.NO_ACCOUNTS_FOUND);
			}
		}*/
	
	
private boolean isCorporateUser(
		final EBTransactionContext ebTransactionContext) {

	return ebTransactionContext.getUserType().getValue().equalsIgnoreCase(
			EBankingConstants.STRING_CORPORATE_USER);
}

}