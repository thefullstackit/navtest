package com.ncb.ebanking.accounts.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.infosys.ebanking.common.EBIncidenceCodes;
import com.infosys.ebanking.common.EBTransactionContext;
import com.infosys.ebanking.common.EBankingConstants;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.ebanking.hif.EBRequestConstants;
import com.infosys.ebanking.moby.common.MobyConstants;
import com.infosys.ebanking.types.TypesCatalogueConstants;
import com.infosys.ebanking.types.valueobjects.OpTxnHistoryCritVO;
import com.infosys.ebanking.types.valueobjects.OpTxnHistoryDetailsVO;
import com.infosys.ebanking.types.valueobjects.OpTxnHistoryEnquiryVO;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.util.DateUtil;
import com.infosys.feba.framework.common.util.resource.PropertyUtil;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.hif.EBHostInvoker;
import com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran;
import com.infosys.feba.framework.types.primitives.FEBADate;
import com.infosys.feba.framework.types.primitives.FEBAUnboundInt;
import com.infosys.feba.framework.types.primitives.FEBAUnboundString;
import com.infosys.feba.framework.types.valueobjects.BusinessInfoVO;
import com.infosys.feba.framework.types.valueobjects.FEBAAVOFactory;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.ncb.ebanking.accounts.util.CustomMobiAcctStatePojo;
import com.ncb.ebanking.transaction.util.CustomMobyUtil;
import com.ncb.ebanking.transaction.util.MobiSendMail;
import com.ncb.ebanking.types.valueobjects.CustomMobiStmtReqVO;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

public class CustomMobiStmtReqServiceGenerateImpl extends AbstractHostInquiryTran{
	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0, IFEBAValueObject arg1,
			IFEBAValueObject arg2) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void processHostData(FEBATransactionContext context, IFEBAValueObject inputOutputVOObj, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		EBTransactionContext ebContext = (EBTransactionContext)context;
		CustomMobiStmtReqVO customMobiStmtReqVO = (CustomMobiStmtReqVO)inputOutputVOObj;
		OpTxnHistoryEnquiryVO txnHistoryEnqVo = (OpTxnHistoryEnquiryVO)FEBAAVOFactory
				.createInstance(TypesCatalogueConstants.OpTxnHistoryEnquiryVO);
		OpTxnHistoryCritVO txnHistoryCritVo = txnHistoryEnqVo.getCriteria();
		String noOfTxns = "";
		String startDateString = "";
		String endDateString = "";
		FEBADate fromDate = new FEBADate();
		FEBADate toDate = new FEBADate();
		
		boolean fetchBasedOnLastNTxns = false;
		boolean fetchBasedOnDateRange = false;
		System.out.println("in moby file impl.11111.."+customMobiStmtReqVO.getNoOfTxns());
		if(customMobiStmtReqVO.getNoOfTxns() != null && !customMobiStmtReqVO.getNoOfTxns().toString().equals("")) {
			System.out.println("in moby file impl.11..");
			fetchBasedOnLastNTxns = true;
			noOfTxns = customMobiStmtReqVO.getNoOfTxns().toString();
			System.out.println("in moby file impl.11a..");
			fromDate = new FEBADate(
	                PropertyUtil.getProperty("TXN_HISTORY_FROM_DATE",context));
			System.out.println("in moby file impl.11d..");

			toDate = new FEBADate(DateUtil.currentDate(context));
			System.out.println("in moby file impl.11.s.");
			txnHistoryCritVo.setLastNTransactions(noOfTxns);
			System.out.println("in moby file impl.11.f.");
			
		}
		
		else{
			fetchBasedOnDateRange = true;
			System.out.println("in moby file impl.11.f1.");
			startDateString = customMobiStmtReqVO.getStartDate().toString();
			System.out.println("in moby file impl.11.f."+startDateString);
			fromDate = new FEBADate(startDateString);
			System.out.println("in moby file impl.11.f2."+fromDate);
			endDateString = customMobiStmtReqVO.getEndDate().toString();
			System.out.println("in moby file impl.11.f3.");
			toDate = new FEBADate(endDateString);
			System.out.println("in moby file impl.11.f4.");
		}
		//fromDate.setDateFormat("dd/MM/yyyy");
		//toDate.setDateFormat("dd/MM/yyyy");
		
		
		txnHistoryCritVo.getDateRangeVO().setFromDate(fromDate);
		txnHistoryCritVo.getDateRangeVO().setToDate(toDate);
		txnHistoryCritVo.getAccountIDER().setAccountID(customMobiStmtReqVO.getAccountNumber().toString());
		String accountId = txnHistoryEnqVo.getCriteria().getAccountIDER().getAccountID().toString();
		String dateFrom=txnHistoryEnqVo.getCriteria().getDateRangeVO().getFromDate().toString();
		String dateTo=txnHistoryEnqVo.getCriteria().getDateRangeVO().getToDate().toString();
		String lastNTransactions=txnHistoryEnqVo.getCriteria().getLastNTransactions().toString();
		System.out.println("accountId>>>>>>>>"+accountId);
		txnHistoryCritVo.getAccountIDER().setBranchCode(customMobiStmtReqVO.getBranchId().toString());
		txnHistoryEnqVo.setCriteria(txnHistoryCritVo);
		try{
		EBHostInvoker.processRequest(context,EBRequestConstants.OP_TRANSACTION_HISTORY_REQUEST, txnHistoryEnqVo);
		if(txnHistoryEnqVo.getResultList().size()==0){
			throw new BusinessException(
					context,
					EBIncidenceCodes.NO_TRANSACTIONS_WERE_FOUND_FOR_THIS_ACCOUNT,
					"No Accounts Found",
					EBankingErrorCodes.NO_TRANSACTIONS_FOUND);
		}
		}
		catch (BusinessException ce) {


			final BusinessInfoVO infoDetails = (BusinessInfoVO) FEBAAVOFactory

			.createInstance(TypesCatalogueConstants.BusinessInfoVO);

			if(ce.getErrorCode()==EBankingErrorCodes.NO_TRANSACTIONS_FOUND){
				throw ce;
				}

			//FIError fierror = new FIError();

			/* IF no data is returned by FI */


			if (ce.getErrorCode() == EBankingErrorCodes.INVALID_FI_RESPONSE) {

				FEBAUnboundInt errorCode = null;

				/* if no data is found */

				int startIndex = ce.getAdditionalMessage().indexOf(
						"[FI Error Code :");

				int endIndex = ce.getAdditionalMessage().indexOf("][");

				try {

					errorCode = new FEBAUnboundInt(Integer.parseInt(ce

					.getAdditionalMessage().substring(startIndex + 16,

					endIndex)));

				}

				catch (StringIndexOutOfBoundsException se) {

					throw new BusinessException(
							context,

							EBIncidenceCodes.WE_ARE_UNABLE_TO_PROCESS_YOUR_REQUEST_RIGHT_NOW_PLEASE_TRY_AFTER_SOMETIME,

							EBankingErrorCodes.UNABLE_TO_PROCESS_REQUEST,se);

				}

				if (errorCode.getValue() == EBankingErrorCodes.NO_RECORDS_FOUND) {

					throw new BusinessException(
							context,
							EBIncidenceCodes.NO_TRANSACTIONS_WERE_FOUND_FOR_THIS_ACCOUNT,
							"No Accounts Found",
							EBankingErrorCodes.NO_TRANSACTIONS_FOUND, ce);

				}

				else {

					/* if any other business exception */

					throw new BusinessException(

							context,

							EBIncidenceCodes.WE_ARE_UNABLE_TO_PROCESS_YOUR_REQUEST_RIGHT_NOW_PLEASE_TRY_AFTER_SOMETIME,

							"Unable to process request",

							errorCode.getValue(),ce);

				}
			}else {
				throw ce;
			}

			/* Incase the Offline Handler is throwing business exception */

		}

		catch (CriticalException e) {

			throw new BusinessException(
					context,
					EBIncidenceCodes.UNABLE_TO_PROCESS_REQUEST_PLEASE_TRY_AFTER_SOME_TIME,

					EBankingErrorCodes.UNABLE_TO_PROCESS_REQUEST);

		}
		
		System.out.println("in moby file impl.2.."+txnHistoryEnqVo);
		int size=txnHistoryEnqVo.getResultList().size();
		System.out.println("list size >>>>>>>>>> "+size);
		ArrayList<CustomMobiAcctStatePojo> statementList = new ArrayList<CustomMobiAcctStatePojo>();
		String logoPath = PropertyUtil.getProperty("SEAL_PATH", context);
		String sealPath = PropertyUtil.getProperty("LOGO_PATH", context);
		String jasperFilePath = PropertyUtil.getProperty("MACCSM_JASPER_PATH", context);
		String subJasperFilePath = PropertyUtil.getProperty("MACCSM_SUB_JASPER_ROOT_PATH", context);
		//String pdfExportPath = PropertyUtil.getProperty("MACCSM_PDF_EXPORT_PATH", context);
		JasperReport jasperReport,jasperReportP;
		
		for(int k=size-1;k>=0;k--){
			
			CustomMobiAcctStatePojo customMobiAcctStatePojo = new CustomMobiAcctStatePojo();
			OpTxnHistoryDetailsVO opTxnHistoryDetailsVo=(OpTxnHistoryDetailsVO) txnHistoryEnqVo.getResultList().get(k);
			String amountType = opTxnHistoryDetailsVo.getAmountType().toString();
			String txnAmount="";
			if(amountType.equals(EBankingConstants.AMOUNT_TYPE_DEBIT)){
				txnAmount=opTxnHistoryDetailsVo.getTransactionAmount().toString();
				txnAmount="- "+txnAmount;
			}
			else
				txnAmount=opTxnHistoryDetailsVo.getTransactionAmount().toString();
			customMobiAcctStatePojo.setTxnDate(opTxnHistoryDetailsVo.getTransactionDate().toString());
			customMobiAcctStatePojo.setAmount(txnAmount);
			customMobiAcctStatePojo.setBalance(opTxnHistoryDetailsVo.getTransactionBalance().toString());
			customMobiAcctStatePojo.setRemarks(opTxnHistoryDetailsVo.getTransactionRemarks().toString());
			String natureOfTransaction = opTxnHistoryDetailsVo.getNatureOfTransaction().toString();
			
			System.out.println("aj..natureOfTransaction "+k+" "+ natureOfTransaction);
			System.out.println("aj..opTxnHistoryDetailsVo "+k+" "+opTxnHistoryDetailsVo.getAmountType());
			statementList.add(customMobiAcctStatePojo);
			System.out.println("printing statementList"+statementList);
		}
		
			String address = customMobiStmtReqVO.getAddress().toString();
			String accType = customMobiStmtReqVO.getAccountType().toString();
			String custName = customMobiStmtReqVO.getCustomerName().toString();
			Map param = new HashMap();
			String subReportPath=subJasperFilePath+File.separator;
			param.put("SUBREPORT_DIR",subReportPath);
			param.put("LogoPath", logoPath);
		
			param.put("Number", accountId);
			param.put("dataHolder", statementList);
			param.put("DateFrom", dateFrom);
			param.put("DateTo", dateTo);
			//param.put("TransactionFor", transactionFor);
			param.put("LastNTransactions", lastNTransactions);
			param.put("custName", custName);
			param.put("accType", accType);
			param.put("address", address);
			try {
			jasperReportP= (JasperReport)JRLoader.loadObject(jasperFilePath);
			jasperReport = (JasperReport)JRLoader.loadObject(jasperFilePath);
			System.out.println("jasperfile path is "+jasperFilePath);
			System.out.println("sub jasperfile path is "+subReportPath);
			String userPasswordPdf=customMobiStmtReqVO.getPassword().toString();
			System.out.println("userPasswordPdf>>>>>>>>"+userPasswordPdf);
			String ownerPasswordPdf=PropertyUtil.getProperty("PDF_OWNER_PASSWORD", context);
			System.out.println("ownerPasswordPdf>>>>>>>>"+ownerPasswordPdf);
			jasperReport.setProperty("net.sf.jasperreports.export.pdf.encrypted", "true");
			jasperReport.setProperty("net.sf.jasperreports.export.pdf.128.bit.key", "true");
			jasperReport.setProperty("net.sf.jasperreports.export.pdf.owner.password", ownerPasswordPdf);
			jasperReport.setProperty("net.sf.jasperreports.export.pdf.user.password", userPasswordPdf);
			JasperPrint jasperPrint,jasperPrintP;
			System.out.println(jasperReport.toString());
			System.out.println("jasper test 1");
			System.out.println("jasper test 2");
			System.out.println("jasper test 3");
			
				jasperPrint=JasperFillManager.fillReport(jasperReport, param,  new JREmptyDataSource());
				jasperPrintP=JasperFillManager.fillReport(jasperReportP, param,  new JREmptyDataSource());
				System.out.println("jasper test 5.1");
				//JasperExportManager.exportReportToPdfFile(jasperPrint, pdfExportPath);
				System.out.println("jasper test 5.2");
				byte[] base64EncodedDataWithoutEncryption = Base64.encodeBase64(JasperExportManager.exportReportToPdf(jasperPrintP), true);
				byte[] base64EncodedData = Base64.encodeBase64(JasperExportManager.exportReportToPdf(jasperPrint), true);
				System.out.println("jasper test 5.3");
				
				String isemail = customMobiStmtReqVO.getEmailReqd().toString();
				String emailSentFlag ="N";
				MobiSendMail mobiSendEmail = new MobiSendMail();
				String serviceID = customMobiStmtReqVO.getServiceId().toString();
				customMobiStmtReqVO.setOutputPdf(new FEBAUnboundString(new String(base64EncodedDataWithoutEncryption)));
				if (isemail.equalsIgnoreCase("E")){
					try {
						customMobiStmtReqVO.setOutputPdf(new FEBAUnboundString(new String(base64EncodedData)));
						System.out.println("test 1");
						customMobiStmtReqVO.setEmailSentFlag(new FEBAUnboundString(emailSentFlag));
						mobiSendEmail.sendMail(context,customMobiStmtReqVO);
						System.out.println("test 2");
						emailSentFlag="Y";
						customMobiStmtReqVO.setEmailSentFlag(new FEBAUnboundString(emailSentFlag));
						System.out.println("test 3");
					} catch (Exception e) {
						System.out.println("test 4");
						customMobiStmtReqVO.setEmailSentFlag(new FEBAUnboundString(emailSentFlag));
						// TODO Auto-generated catch block
						System.out.println("test 5");
						e.printStackTrace();
					}
				}
				customMobiStmtReqVO.setOutputPdf(new FEBAUnboundString(new String(base64EncodedDataWithoutEncryption)));
			} 
			
			catch (JRException e) {
				// TODO Auto-generated catch block
				System.out.println("inside JR exception");
				e.printStackTrace();
			}
			catch(Exception t){
				System.out.println("inside general  exception");
				t.printStackTrace();
			}
			String waiveFlag = customMobiStmtReqVO.getWaiveFlag().toString();
			if(waiveFlag.equals("N")) {
				//host call for charge amount
				System.out.println("in moby file impl.2.waiveFlag."+waiveFlag);
				CustomMobyUtil mobyUtil = new CustomMobyUtil();
				customMobiStmtReqVO.setReqUUID(new FEBAUnboundString(mobyUtil.reqUuidGenerator()));
				customMobiStmtReqVO.setBankId(new FEBAUnboundString(PropertyUtil.getProperty(MobyConstants.CORE_BANK_ID, ebContext)));
				customMobiStmtReqVO.setChannelId(new FEBAUnboundString(PropertyUtil.getProperty(MobyConstants.FI_CHANNEL_ID_MBY, ebContext)));
				customMobiStmtReqVO.setServiceRqstVersion(new FEBAUnboundString(MobyConstants.FI_SERVICE_RQST_VERSION));
				customMobiStmtReqVO.setMessageDateTime(new FEBAUnboundString(mobyUtil.messageDateTimeGenerator11x(ebContext)));
				EBHostInvoker.processRequest(context,"CustomMobiChargeForStatementRequest", customMobiStmtReqVO);
				String status = customMobiStmtReqVO.getChargeResultStatus().toString();
				System.out.println("aj..status.."+status);
				String chargeAmt= customMobiStmtReqVO.getChargeAmount().toString();
				String tranId = customMobiStmtReqVO.getTranId().toString();
				
				System.out.println("in moby file impl.2.status."+status);
				
			}
			System.out.println("jasper test 6");
		}
	

	@Override
	protected void processLocalData(FEBATransactionContext arg0, IFEBAValueObject arg1, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		
	}

}