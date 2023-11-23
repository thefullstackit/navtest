package com.ncb.ebanking.accounts.service;

import com.infosys.ebanking.common.EBIncidenceCodes;
import com.infosys.ebanking.common.EBTransactionContext;
import com.infosys.ebanking.common.EBankingConstants;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.ebanking.types.TypesCatalogueConstants;
import com.infosys.ebanking.types.valueobjects.AccountEnquiryVO;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.hif.EBHostInvoker;
import com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran;
import com.infosys.feba.framework.types.primitives.FEBAUnboundString;
import com.infosys.feba.framework.types.valueobjects.FEBAAVOFactory;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.infosys.fentbase.types.valueobjects.RiskEvaluationVO;
import com.infosys.fentbase.user.service.AdaptiveAuthHelper;
import com.ncb.ebanking.common.RSAConstants;
import com.ncb.ebanking.hif.CustomRequestConstants;
import com.ncb.ebanking.types.valueobjects.CustomAccountEnquiryVO;
import com.ncb.ebanking.types.valueobjects.CustomRiskEvaluationVO;

public class CustomAccountRSAServiceQueryImpl extends
AbstractHostInquiryTran {

	/**
	 * (non-Javadoc)
	 *
	 * @see com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran#prepareValidationsList(com.infosys.feba.framework.commontran.context.FEBATransactionContext,
	 *      com.infosys.feba.framework.types.valueobjects.IFEBAValueObject,
	 *      com.infosys.feba.framework.types.valueobjects.IFEBAValueObject)
	 */
	public FEBAValItem[] prepareValidationsList(
			FEBATransactionContext objContext, IFEBAValueObject objInputOutput,
			IFEBAValueObject objTxnWM) throws BusinessException,
			BusinessConfirmation, CriticalException {
		return null;
	}

	/**
	 * Is used to check whether the user is a retail user or not.
	 *
	 * If its a retail user then the host call is made.
	 *
	 *
	 * @see com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran#processHostData(com.infosys.feba.framework.commontran.context.FEBATransactionContext,
	 *
	 * com.infosys.feba.framework.types.valueobjects.IFEBAValueObject,
	 *
	 * com.infosys.feba.framework.types.valueobjects.IFEBAValueObject)
	 */
	protected void processHostData(FEBATransactionContext objContext,
			IFEBAValueObject objInputOutput, IFEBAValueObject objTxnWM)
	throws BusinessException, BusinessConfirmation, CriticalException {
		EBTransactionContext ebcontext = (EBTransactionContext) objContext;
		 AccountEnquiryVO acntEnqVO = (AccountEnquiryVO) objInputOutput;
		 RiskEvaluationVO riskVO = (RiskEvaluationVO) FEBAAVOFactory
         .createInstance(TypesCatalogueConstants.RiskEvaluationVO);
		 CustomRiskEvaluationVO customRiskVO = (CustomRiskEvaluationVO)riskVO.getExtensionVO();
		//final EBTransactionContext objEBTxnContext = (EBTransactionContext) objContext;
		// Checks whether the user is a retail user or not.
		
		if (ebcontext.getUserType().getValue().equals(
				String.valueOf(EBankingConstants.RETAIL_USER))|| ebcontext.getUserType().getValue().equals(
						String.valueOf(EBankingConstants.CORPORATE_USER))){
			//invoking the host call.
			try{
				/*EBHostInvoker.processRequest(objContext,
						CustomRequestConstants.ACC_QUERY_REQUEST,
						acntEnqVO);*/
				customRiskVO.setAction("VIEW_STATEMENT");
				EBHostInvoker.processRequest(objContext,
						CustomRequestConstants.ANALYZE_RISK_REQUEST,
						riskVO);
				
				
				
				
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				System.out.println("Post Analyze SIT 11 "+riskVO);
/*				if (riskVO
						.getRiskAssessmentData()
						.getAdvice()
						.getValue()
						.equals(RSAConstants.RSA_CHALLENGE_REQUEST_ACTION)) {
				if (customRiskVO
						.getRsaUserStatus()
						.toString()
						.equalsIgnoreCase(
								RSAConstants.RSA_USER_STATUS_VERIFIED)) {*/
				if (customRiskVO
						.getRsaUserStatus()
						.toString()
						.equalsIgnoreCase(
								RSAConstants.RSA_USER_STATUS_VERIFIED))
				{
				System.out.println("Post Analyze SIT 11 "+riskVO);
				if (riskVO
						.getRiskAssessmentData()
						.getAdvice()
						.getValue()
						.equals(RSAConstants.RSA_CHALLENGE_REQUEST_ACTION)) {
					
					FEBAUnboundString challengeType = (FEBAUnboundString)riskVO.getExtensionVO().getFieldByName("credentialType");
					CustomAccountEnquiryVO customAcntEnqVO = (CustomAccountEnquiryVO)acntEnqVO.getExtensionVO();
					
					if(challengeType != null && challengeType.toString().equals("OTP"))
					{
						
						EBHostInvoker.processRequest(objContext,
							CustomRequestConstants.RSA_OOB_PHONE_REQUEST,
							riskVO);
						System.out.println("Risk VO Account "+riskVO);
						if(riskVO.getExtensionVO().getFieldByName("challengeStatusCodeOOB") != null  
								&& riskVO.getExtensionVO().getFieldByName("challengeStatusCodeOOB").toString().equals("SUCCESS"))
						{
							System.out.println("Account Statment Challenge Raised ");
							customAcntEnqVO.setRsaUserStatus(challengeType);
							customAcntEnqVO.setAuthenticationToken((FEBAUnboundString)riskVO.getExtensionVO().getFieldByName("challengePayloadTokenOOB"));
							acntEnqVO.setExtensionVO(customAcntEnqVO);
							System.out.println("Custom Accnt Enq VO "+acntEnqVO);
						}
					}
					else if(challengeType != null && challengeType.toString().equals("SECURID")){
						customAcntEnqVO.setRsaUserStatus(challengeType);
						acntEnqVO.setExtensionVO(customAcntEnqVO);
					}
				}else if (riskVO.getRiskAssessmentData()
						.getAdvice().getValue()
						.equals(RSAConstants.RSA_ALLOW_REQUEST_ACTION) || riskVO.getRiskAssessmentData()
						.getAdvice().getValue()
						.equals(RSAConstants.RSA_REVIEW_REQUEST_ACTION)) {
					riskVO
							.getRiskAssessmentData()
							.setAdvice(
									Character
											.toString(EBankingConstants.ADVICE_ALLOW));
				} else if (riskVO.getRiskAssessmentData()
						.getAdvice().getValue()
						.equals(RSAConstants.RSA_DENY_REQUEST_ACTION)) {
					riskVO
							.getRiskAssessmentData()
							.setAdvice(
									Character
											.toString(EBankingConstants.ADVICE_DENY));
					AdaptiveAuthHelper.getInstance()
					.handleAuthenticationFailure(ebcontext);
					
						throw new BusinessException(
								objContext,
								EBIncidenceCodes.ADAPTIVE_AUTH_TRANSACTION_DENIED,
								"Adaptive authorization failed",
								EBankingErrorCodes.TRANSACTION_NOT_ALLOWED_CONTACT_CUST_CARE);
						
						

				}
				
				
			} 
				

				/*else if (customRiskVO
						.getRsaUserStatus()
						.toString()
						.equalsIgnoreCase(
								RSAConstants.RSA_USER_STATUS_UNVERIFIED)) {
					System.out.println("Post Analyze SIT 123 "+riskVO);
					// Throw an Error stating to contact bank admin
						System.out.println("User Status is Unverified");
						FEBAHashList additionalList = new FEBAHashList();
						GenericFieldVO genFieldVO = (GenericFieldVO) FEBAAVOFactory
								.createInstance(TypesCatalogueConstants.GenericFieldVO);
						genFieldVO.setSName("secondaryAuthMode");
						genFieldVO.setSValue("UNVE");
						additionalList.put("secondaryAuthMode",
								genFieldVO);
						riskVO
								.setAdditionalData(additionalList);
						riskVO
						.getRiskAssessmentData()
						.setAdvice(
								Character
										.toString(EBankingConstants.ADVICE_INCREASE_AUTH));
					//createUser(ebContext, customRiskEvaluationVO);
					
					
				}
			} else if (customRiskVO
					.getRsaUserStatus()
					.toString()
					.equalsIgnoreCase(
							RSAConstants.RSA_USER_STATUS_LOCKOUT)) {
				throw new BusinessException(
						true,
						ebcontext,
						NCBCEBIncidenceCodes.USER_LOCKOUT,
						"User is Locked for Security Purpose. Please Contact Customer Care.",
						null, NCBCEBankingErrorCodes.USER_LOCKOUT,
						null, null);
			}*/
				
				
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				
				
				
				
				
				//IF Challenge is RSA and credential Type is OTP call Challenge Request
				
			}catch (BusinessConfirmation e) {
				throw new CriticalException(
						(EBTransactionContext) objContext,
						EBIncidenceCodes.ADAPTIVE_AUTH_LOGIN_DENIED,
						"Adaptive authentication failed",
						EBankingErrorCodes.LOGIN_NOT_ALLOWED, e);

			}
			
			/* If response is enrolled Analyze Call with eventType VIEW_STATEMENT*/
			
		}
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran#processLocalData(com.infosys.feba.framework.commontran.context.FEBATransactionContext,
	 *      com.infosys.feba.framework.types.valueobjects.IFEBAValueObject,
	 *      com.infosys.feba.framework.types.valueobjects.IFEBAValueObject)
	 */
	protected void processLocalData(FEBATransactionContext objContext,
			IFEBAValueObject objInputOutput, IFEBAValueObject objTxnWM)
	throws BusinessException, BusinessConfirmation, CriticalException {

	}

	/**
		 *
		 * If there are any expections thrown from the host it try to fetch data from local db before throwing any exception
		 *

	 */
	 public void localProcess(FEBATransactionContext ojContext,
	            IFEBAValueObject objInputOutput, IFEBAValueObject bjTxnWM)
	            throws BusinessException, BusinessConfirmation, CriticalException {

			



	    }
}
