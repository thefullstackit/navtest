package com.ncb.ebanking.accounts.service;

import com.infosys.ebanking.common.EBIncidenceCodes;
import com.infosys.ebanking.common.EBTransactionContext;
import com.infosys.ebanking.common.EBankingConstants;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.ebanking.tao.CTDTTAO;
import com.infosys.ebanking.tao.info.CTDTInfo;
import com.infosys.ebanking.types.TypesCatalogueConstants;
import com.infosys.ebanking.types.primitives.CityCode;
import com.infosys.ebanking.types.valueobjects.AccountEnquiryVO;
import com.infosys.ebanking.types.valueobjects.AccountsRequestVO;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.logging.LogManager;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.hif.EBHostInvoker;
import com.infosys.feba.framework.tao.FEBATableOperatorException;
import com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran;
import com.infosys.feba.framework.types.lists.FEBAArrayList;
import com.infosys.feba.framework.types.lists.IFEBAList;
import com.infosys.feba.framework.types.primitives.FEBAUnboundString;
import com.infosys.feba.framework.types.valueobjects.FEBAAVOFactory;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.infosys.fentbase.common.FBAConstants;
import com.infosys.fentbase.common.FBATransactionContext;
import com.infosys.fentbase.common.validators.RMUserEntityUtility;
import com.infosys.fentbase.tao.info.CUSRInfo;
import com.infosys.fentbase.types.primitives.AccountId;
import com.infosys.fentbase.types.primitives.CustomerId;
import com.infosys.fentbase.types.primitives.EntityName;
import com.infosys.fentbase.types.valueobjects.AccountVO;
import com.infosys.fentbase.user.CUSRTableUtility;
import com.ncb.ebanking.common.CustomEBRequestConstants;
import com.ncb.ebanking.types.valueobjects.CustomAccountVO;

public class CustomInsuranceServiceFetchDetailsImpl extends AbstractHostInquiryTran {

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0, IFEBAValueObject arg1,
			IFEBAValueObject arg2) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void processHostData(FEBATransactionContext objTxnContext, IFEBAValueObject objInputOutput,
			IFEBAValueObject objTxnWM) throws BusinessException, BusinessConfirmation, CriticalException {

		AccountEnquiryVO actEnquiryVO = (AccountEnquiryVO) objInputOutput;

		AccountsRequestVO accountsRequestVO = (AccountsRequestVO) FEBAAVOFactory
				.createInstance(TypesCatalogueConstants.AccountsRequestVO);
        FBATransactionContext ebCtx  = (FBATransactionContext)objTxnContext;
        CUSRTableUtility cusrTableUtility =  CUSRTableUtility.getInstance(); 
        CustomerId cifid = ((CUSRInfo)cusrTableUtility.getUserRecord(ebCtx,objTxnContext.getUserId() , objTxnContext.getOrgId(), objTxnContext.getBankId())).getCustId(); 
        if(accountsRequestVO!=null && accountsRequestVO.getCriteria()!=null && accountsRequestVO.getCriteria().getCustomerIDER()!=null){
        	accountsRequestVO.getCriteria().getCustomerIDER().setCustomerID(cifid);
        }
       
        AccountVO acctVOObj = (AccountVO) FEBAAVOFactory
				.createInstance("com.infosys.fentbase.types.valueobjects.AccountVO");
		IFEBAList userEntityList = null;
       
    	FEBAArrayList<AccountVO> newResultList1 = new FEBAArrayList<AccountVO>();
		try {
			userEntityList = (FEBAArrayList) RMUserEntityUtility
					.fetchAccounts(ebCtx, new EntityName(
							FBAConstants.ACCOUNT_ENTITY));
			
			for (int i = 0; i < userEntityList.size(); i++) {
				 acctVOObj = (AccountVO) userEntityList.get(i);
			        if (acctVOObj.getAccountIDER().getAccountType().toString().equalsIgnoreCase("INS")) {
			        	newResultList1.addObject(acctVOObj);
			        }
			}
			if(newResultList1.size()==0)
			{
				 throw new BusinessException(objTxnContext,
                  EBIncidenceCodes.NO_ACCOUNTS_FOUND,"",
                  EBankingErrorCodes.NO_ACCOUNTS_FOUND);
			}
		} catch (BusinessException e) {
			 throw new BusinessException(objTxnContext,
                  EBIncidenceCodes.NO_ACCOUNTS_FOUND,"",
                  EBankingErrorCodes.NO_ACCOUNTS_FOUND);
		}
    	
    	if(newResultList1!=null && newResultList1.size()>0){
    		EBHostInvoker.processRequest(objTxnContext, CustomEBRequestConstants.INSURANCE_LIST_REQUEST, accountsRequestVO);

    		FEBAArrayList resultList = accountsRequestVO.getResultList();
    		for(int i=0; i<resultList.size();i++){
				AccountVO accountVO =  (AccountVO)resultList.get(i);
				CustomAccountVO customVO = (CustomAccountVO)accountVO.getExtensionVO();
				
				if(customVO.getCurrencyValue()!=null && customVO.getAccBalance()!=null && !customVO.getCurrencyValue().toString().isEmpty() && !customVO.getAccBalance().toString().isEmpty()){
					//FEBAAmount balanceAmount = new FEBAAmount(customVO.getCurrencyValue().toString(), Double.parseDouble(customVO.getAccBalance().toString()));
					FEBAUnboundString balanceAmount = customVO.getAccBalance();
					customVO.setBalanceNew(balanceAmount);
				}
				if(null!=customVO.getCityBen() && !EBankingConstants.BLANK.equals(customVO.getCityBen()))
				{
					customVO.setCityBenDesc(getCityDesc(objTxnContext, new CityCode(customVO.getCityBen().getValue())));
				}
				if(null!=customVO.getCityMain() && !EBankingConstants.BLANK.equals(customVO.getCityMain()))
				{
					customVO.setCityMainDesc(getCityDesc(objTxnContext, new CityCode(customVO.getCityMain().getValue())));
				}
				if(null!=customVO.getCityIns() && !EBankingConstants.BLANK.equals(customVO.getCityIns()))
				{
					customVO.setCityInsDesc(getCityDesc(objTxnContext, new CityCode(customVO.getCityIns().getValue())));
				}
				if(null!=customVO.getCityPay() && !EBankingConstants.BLANK.equals(customVO.getCityPay()))
				{
					customVO.setCityPayDesc(getCityDesc(objTxnContext, new CityCode(customVO.getCityPay().getValue())));
				}

    		}
		FEBAArrayList  finalArrayList = new FEBAArrayList();
		
		for (int i = 0; i < newResultList1.size(); i++) {
    	  AccountVO accountVO =  (AccountVO)newResultList1.get(i);
    	  AccountId accountId=accountVO.getAccountIDER().getAccountID();
    	   for (int j = 0; j < resultList.size(); j++) {
    		   AccountVO accountVO1 =  (AccountVO)resultList.get(j);
    		   AccountId accountId1=accountVO1.getAccountIDER().getAccountID();
    	       if (accountId!=null && accountId1!=null && accountId.getValue().equals(accountId1.getValue())) {
    	    	  finalArrayList.addObject(accountVO1);	  
    	       }	
    	   }
    	  
		}
      if(finalArrayList!=null && finalArrayList.size()==0)
		{
          throw new BusinessException(objTxnContext,
                  EBIncidenceCodes.NO_ACCOUNTS_FOUND,"",
                  EBankingErrorCodes.NO_ACCOUNTS_FOUND);
		}
      actEnquiryVO.setResultList(finalArrayList);
	}
}

	@Override
	protected void processLocalData(FEBATransactionContext arg0, IFEBAValueObject arg1, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
	}
	
	public String getCityDesc(FEBATransactionContext objTxnContext, CityCode cityCode)
	{
		CTDTInfo ctdtInfo = new CTDTInfo();
		try
		{
			ctdtInfo = CTDTTAO.select(objTxnContext, objTxnContext.getBankId(), objTxnContext.getLangId(), cityCode);
		}
		catch(FEBATableOperatorException e)
		{
			LogManager.log(objTxnContext, e.getMessage(), LogManager.MESSAGE);
		}
		if(null!=ctdtInfo.getCityDesc() && !EBankingConstants.BLANK.equals(ctdtInfo.getCityDesc()))
		{
			return ctdtInfo.getCityDesc().getValue();
		}
		else
		{
			return EBankingConstants.BLANK;
		}
	}

}
