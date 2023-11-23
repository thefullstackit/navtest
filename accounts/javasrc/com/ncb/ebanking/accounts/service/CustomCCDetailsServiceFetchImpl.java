package com.ncb.ebanking.accounts.service;

import com.infosys.ebanking.common.EBTransactionContext;
import com.infosys.ebanking.common.EBankingConstants;
import com.infosys.ebanking.types.primitives.TransactionOrigin;
import com.infosys.ebanking.types.valueobjects.CCDetailsVO;
import com.infosys.ebanking.types.valueobjects.CCTXNCriteriaVO;
import com.infosys.feba.framework.common.FEBAIncidenceCodes;
import com.infosys.feba.framework.common.exception.AdditionalParam;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.hif.EBHostInvoker;
import com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran;
import com.infosys.feba.framework.types.lists.FEBAArrayList;
import com.infosys.feba.framework.types.primitives.FEBAAmount;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.ncb.ebanking.common.CustomEBIncidenceCodes;
import com.ncb.ebanking.common.CustomEBankingErrorCodes;
import com.ncb.ebanking.types.valueobjects.CustomCCDetailsVO;
import com.ncb.ebanking.types.valueobjects.CustomCCTxnDetailsVO;

public class CustomCCDetailsServiceFetchImpl extends AbstractHostInquiryTran {

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0, IFEBAValueObject arg1,
			IFEBAValueObject arg2) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub test45
		return new FEBAValItem[]{};
	}

	@Override
	protected void processHostData(FEBATransactionContext objTxnContext, IFEBAValueObject objInputOutput,
			IFEBAValueObject objTxnWM)throws BusinessException, BusinessConfirmation, CriticalException {

			CCDetailsVO ccDetailsVO = (CCDetailsVO) objInputOutput;
			CustomCCDetailsVO customVO = (CustomCCDetailsVO)ccDetailsVO.getExtensionVO();
			long timeSeed = System.nanoTime();
	        double randSeed = Math.random() * 1000; // random number generation
	        long midSeed = (long) (timeSeed * randSeed); // mixing up the time and
	        String s = midSeed + "";
	        String messageId = s.substring(0, 9);
	        customVO.setMessageId(messageId);
	        ccDetailsVO.setExtensionVO(customVO);
	    	EBHostInvoker.processRequest(objTxnContext, "CustomGetTicketIdRequest", ccDetailsVO);
	    	customVO = (CustomCCDetailsVO) ccDetailsVO.getExtensionVO();
			String ticktId = customVO.getTicketId().toString() ;
			if (null != ticktId && ticktId.trim().length()!=0){
				CCTXNCriteriaVO criteriaVO=  customVO.getCriteria();
				EBTransactionContext ebCtx = (EBTransactionContext)objTxnContext;
				
//				if(criteriaVO.getTransactionDate()!=null){
//					FEBADate fromDate= criteriaVO.getTransactionDate().getFromDate();
////					String dateFormat = fromDate.getDateFormat();
//					Date fromDateVal = new Date();
//					SimpleDateFormat sourceFormat = null;
//					if(ebCtx.getUserDateFormat()!=null){
//						 sourceFormat= new SimpleDateFormat(ebCtx.getUserDateFormat().getValue());
//					}
//					
//					try {
//						fromDateVal = sourceFormat.parse(fromDate.toString());
//					} catch (ParseException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
//					String formattedDate = targetFormat.format(fromDateVal);
//					fromDate = new FEBADate(formattedDate);
//					System.out.println("fromdate.."+ fromDate);
//				}
				if(null != customVO.getCriteria() && null != customVO.getCriteria().getDateRange() && !customVO.getCriteria().getDateRange().getValue().equals(EBankingConstants.BLANK)){
					try{
						int noOfTxns = Integer.parseInt(customVO.getCriteria().getDateRange().getValue());
					}catch(NumberFormatException e){
						 throw new BusinessException(true,objTxnContext,
								 FEBAIncidenceCodes.INVALID_INPUT_TYPE,
								 "No of txns should be numeric",null,
								 3007, null);
					}
				}
					 
				EBHostInvoker.processRequest(objTxnContext, "CustomGetCCDetailsRequest", ccDetailsVO);
				customVO = (CustomCCDetailsVO)ccDetailsVO.getExtensionVO();
				FEBAArrayList<CustomCCTxnDetailsVO> txnList = customVO.getResultList();
				FEBAArrayList<CustomCCTxnDetailsVO> newTxnList = new FEBAArrayList<CustomCCTxnDetailsVO>();
				FEBAArrayList<CustomCCTxnDetailsVO> filteredTxnList = new FEBAArrayList<CustomCCTxnDetailsVO>();
				if(null != txnList && txnList.size()>0){
					for(int i=0; i<txnList.size();i++){
						CustomCCTxnDetailsVO ccTxnDetailsVO = (CustomCCTxnDetailsVO)txnList.get(i);
						if(null != ccTxnDetailsVO.getStGeneral() && ccTxnDetailsVO.getStGeneral().getValue().equalsIgnoreCase("POST"))
						{
							filteredTxnList.add(ccTxnDetailsVO);
						}
					}
					customVO.setResultList(filteredTxnList);
				}
				txnList = customVO.getResultList();
				if(null != txnList && txnList.size()>0){
					if(null != customVO.getCriteria() && null != customVO.getCriteria().getTxnQriginER().getCode() ){
						TransactionOrigin catId = customVO.getCriteria().getTxnQriginER().getCode();
						if(!catId.getValue().equals(EBankingConstants.BLANK)){
							if(catId.getValue().equals("C")){
								for(int i=0; i<txnList.size();i++){
									if(null != txnList.get(i).getBillingCurrencyAmount() && txnList.get(i).getBillingCurrencyAmount().getValue().contains("-")){
										newTxnList.addObject(txnList.get(i));
									}
								}
								customVO.setResultList(newTxnList);
							}else if(catId.getValue().equals("D")){
								for(int i=0; i<txnList.size();i++){
									if(null != txnList.get(i).getBillingCurrencyAmount() && !txnList.get(i).getBillingCurrencyAmount().getValue().contains("-")){
										newTxnList.addObject(txnList.get(i));
									}
							}
								customVO.setResultList(newTxnList);
						}
					}
				}
					
					if(null != customVO.getCriteria() && null != customVO.getCriteria().getFromOriginalAmount() && customVO.getCriteria().getFromOriginalAmount().getAmountValue() != 0.0){
						for(int i=0; i<txnList.size();i++){
							
							String amount= txnList.get(i).getBillingCurrencyAmount().getValue();
							if(amount.startsWith("-")){
								amount=amount.substring(1, amount.length());
							}
							Double amountVal = Double.parseDouble(amount);
							if(null != amountVal && amountVal>customVO.getCriteria().getFromOriginalAmount().getAmountValue() && amountVal<customVO.getCriteria().getToOriginalAmount().getAmountValue()){
								newTxnList.addObject(txnList.get(i));
							}
								
						}
						customVO.setResultList(newTxnList);
					}
			}
			if (null != customVO.getAccountNumber() && customVO.getAccountNumber().getValue().trim().length() != 0){
				EBHostInvoker.processRequest(objTxnContext, "CustomGetCCEntityDetailsRequest", ccDetailsVO);
			
				//Calculate avialable credit limit to set in vo. CreditLimit - Balance
				customVO = (CustomCCDetailsVO) ccDetailsVO.getExtensionVO();
				double amt = ccDetailsVO.getTotalCreditLimit().getAmountValue() - customVO.getCurrentBalanace().getAmountValue();
				ccDetailsVO.setAvailableCreditLimit(new FEBAAmount(ccDetailsVO.getTotalCreditLimit().getCurrencyCodeValue(),amt));
			}
			else 
			{
				throw new BusinessException(true,objTxnContext,
		         		CustomEBIncidenceCodes.UNABLE_TO_PROCESS_REQ,
		         		"Issue with the response received.",
		         		null,CustomEBankingErrorCodes.UNABLE_TO_PROCESS_REQUEST, null, (AdditionalParam)null);
			}
			
		}
		else 
		{
			throw new BusinessException(true,objTxnContext,
		       		CustomEBIncidenceCodes.UNABLE_TO_PROCESS_REQ,
		       		"Issue with the response received.",
		       		null,CustomEBankingErrorCodes.UNABLE_TO_PROCESS_REQUEST, null, (AdditionalParam)null);
		}
	}

		
	

	@Override
	protected void processLocalData(FEBATransactionContext arg0, IFEBAValueObject arg1, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		
	}

}
