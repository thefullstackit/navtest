package com.ncb.ebanking.moby.accounts.service;

import com.infosys.ebanking.common.EBIncidenceCodes;
import com.infosys.ebanking.common.EBTransactionContext;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.ebanking.moby.common.MobyConstants;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.logging.LogManager;
import com.infosys.feba.framework.common.util.resource.PropertyUtil;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.hif.EBHostInvoker;
import com.infosys.feba.framework.transaction.pattern.AbstractHostUpdateTran;
import com.infosys.feba.framework.types.primitives.FEBAUnboundString;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.ncb.ebanking.common.CustomEBRequestConstants;
import com.ncb.ebanking.transaction.util.CustomMobyUtil;
import com.ncb.ebanking.types.valueobjects.CustomMobiRateCodeInquiryVO;

public class CustomMobiRateCodeServiceProcessImpl extends AbstractHostUpdateTran {

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0, IFEBAValueObject arg1,
			IFEBAValueObject arg2) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void processHostData(FEBATransactionContext objContext, IFEBAValueObject objInputOutput, IFEBAValueObject objTxnWM)
			throws BusinessException, BusinessConfirmation, CriticalException {
		
		EBTransactionContext ebContext = (EBTransactionContext) objContext;
		CustomMobiRateCodeInquiryVO customMobiRateCodeInquiryVO = (CustomMobiRateCodeInquiryVO) objInputOutput;
		CustomMobyUtil mobyUtil = new CustomMobyUtil();
		customMobiRateCodeInquiryVO.setReqUUID(new FEBAUnboundString(mobyUtil.reqUuidGenerator()));
		customMobiRateCodeInquiryVO.setBankId(new FEBAUnboundString(PropertyUtil.getProperty(MobyConstants.CORE_BANK_ID, ebContext)));
		customMobiRateCodeInquiryVO.setChannelId(new FEBAUnboundString(PropertyUtil.getProperty(MobyConstants.FI_CHANNEL_ID_MBY, ebContext)));
		customMobiRateCodeInquiryVO.setServiceRqstVersion(new FEBAUnboundString(MobyConstants.FI_SERVICE_RQST_VERSION));
		customMobiRateCodeInquiryVO.setMessageDateTime(new FEBAUnboundString(mobyUtil.messageDateTimeGenerator11x(ebContext)));
		try{
		EBHostInvoker.processRequest(objContext, CustomEBRequestConstants.CUSTOM_MOBI_RATE_CODE_INQ, customMobiRateCodeInquiryVO);
		}
		catch(Exception e)
       	{
       		LogManager.log(objContext, e.getMessage(), LogManager.MESSAGE);
			throw new BusinessException(objContext,
					EBIncidenceCodes.HOST_FAILURE_RESPONSE ,
					EBankingErrorCodes.HOST_NOT_AVAILABLE ); 
       	}
		/*if(customMobiRateCodeInquiryVO.getExceptionDesc() != null && !customMobiRateCodeInquiryVO.getExceptionDesc().toString().isEmpty()){
			String errorTagDesc = customMobiRateCodeInquiryVO.getExceptionDesc().toString();
			System.out.println("aj..CustomMobiRateCodeServiceProcessImpl..errorDesc.."+errorTagDesc);
			String[] inputErrorRecords=errorTagDesc.split(Pattern.quote("^"));
			int inputErrorRecordsSize= inputErrorRecords.length;
			String finalErrType="",finalErrCode="",finalErrDesc="";
			for(int y=0;y<inputErrorRecordsSize;y++){
				
				String identifier, erCode="",erDesc="",erType="";
				String errorRecord =  inputErrorRecords[y];
				String[] errorRecordDetails = errorRecord.split(Pattern.quote("~"));
				System.out.println("aj..CustomMobiRateCodeServiceProcessImpl..errorRecordDetails.."+errorRecordDetails);
				for(int i=0;i<errorRecordDetails.length;i++){
					
					
					if(errorRecordDetails.length>0){
						identifier=errorRecordDetails[0];
						erCode=errorRecordDetails[1];
						erDesc=errorRecordDetails[2];
						System.out.println("aj..CustomMobiRateCodeServiceProcessImpl..erCode.."+erCode+"for number .."+ i);
						System.out.println("aj..CustomMobiRateCodeServiceProcessImpl..erDesc.."+erDesc+"for number .."+ i);
						if(identifier.equalsIgnoreCase("ErrorCode")){
							erType="BE";
						}
						else if (identifier.equalsIgnoreCase("WarningCode")){
							erType="BW";
						}
						else if (identifier.equalsIgnoreCase("ExceptionCode")){
							erType="BX";
						}
						System.out.println("aj..CustomMobiRateCodeServiceProcessImpl..erType.."+erType+"for number .."+ i);
						
						
					}//nested if
					
				}//nested for
				finalErrType=finalErrType+"|"+erType;
				finalErrCode=finalErrCode+"|"+erCode;
				finalErrDesc=finalErrDesc+"|"+erDesc;
				System.out.println("aj..CustomMobiRateCodeServiceProcessImpl..finalErrType.."+finalErrType+"for number ..");
				System.out.println("aj..CustomMobiRateCodeServiceProcessImpl..finalErrCode.."+finalErrCode+"for number ..");
				System.out.println("aj..CustomMobiRateCodeServiceProcessImpl..finalErrDesc.."+finalErrDesc+"for number ..");
			}//for
			customMobiRateCodeInquiryVO.setExceptionCode(new FEBAUnboundString(finalErrCode));
			customMobiRateCodeInquiryVO.setExceptionType(new FEBAUnboundString(finalErrType));
			customMobiRateCodeInquiryVO.setExceptionDesc(new FEBAUnboundString(finalErrDesc));
		}//if*/
		
	}

	@Override
	protected void processLocalData(FEBATransactionContext arg0, IFEBAValueObject arg1, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		
	}

}
