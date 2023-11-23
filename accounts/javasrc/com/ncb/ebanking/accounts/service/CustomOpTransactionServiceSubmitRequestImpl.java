/*
 *
 * COPYRIGHT NOTICE:
 * Copyright (c) 2007 Infosys Technologies Limited, Electronic City,
 * Hosur Road, Bangalore - 560 100, India.
 * All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Infosys Technologies Ltd. ("Confidential Information"). You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Infosys.
 */
package com.ncb.ebanking.accounts.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.infosys.ebanking.common.EBIncidenceCodes;
import com.infosys.ebanking.common.EBankingBatchConstants;
import com.infosys.ebanking.common.EBankingConstants;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.ebanking.common.validators.DateVal;
import com.infosys.ebanking.common.validators.UserEntityUtility;
import com.infosys.ebanking.types.TypesCatalogueConstants;
import com.infosys.ebanking.types.valueobjects.OpTxnHistoryCritVO;
import com.infosys.ebanking.types.valueobjects.OpTxnHistoryEnquiryVO;
import com.infosys.feba.framework.batch.FEBABatchConstants;
import com.infosys.feba.framework.common.ErrorCodes;
import com.infosys.feba.framework.common.FEBAIncidenceCodes;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.exception.DALException;
import com.infosys.feba.framework.common.util.DateUtil;
import com.infosys.feba.framework.common.util.resource.PropertyUtil;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.dal.QueryOperator;
import com.infosys.feba.framework.tao.FEBATableOperatorException;
import com.infosys.feba.framework.transaction.pattern.AbstractLocalUpdateTran;
import com.infosys.feba.framework.types.FEBAStringBuilder;
import com.infosys.feba.framework.types.FEBATypesUtility;
import com.infosys.feba.framework.types.lists.FEBAArrayList;
import com.infosys.feba.framework.types.primitives.BatchPriority;
import com.infosys.feba.framework.types.primitives.FEBADate;
import com.infosys.feba.framework.types.primitives.FEBAUnboundChar;
import com.infosys.feba.framework.types.primitives.FEBAUnboundInt;
import com.infosys.feba.framework.types.primitives.FEBAUnboundLong;
import com.infosys.feba.framework.types.primitives.FEBAUnboundString;
import com.infosys.feba.framework.types.primitives.HolidayOption;
import com.infosys.feba.framework.types.primitives.NumberOfOccurrences;
import com.infosys.feba.framework.types.primitives.ScheduleName;
import com.infosys.feba.framework.types.primitives.SchedulePattern;
import com.infosys.feba.framework.types.primitives.TaskData;
import com.infosys.feba.framework.types.primitives.TaskDescription;
import com.infosys.feba.framework.types.primitives.TaskName;
import com.infosys.feba.framework.types.primitives.TaskStatus;
import com.infosys.feba.framework.types.primitives.Time;
import com.infosys.feba.framework.types.valueobjects.BusinessInfoVO;
import com.infosys.feba.framework.types.valueobjects.FEBAAVOFactory;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValEngineConstants;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.infosys.fentbase.tao.SCDTTAO;
import com.infosys.fentbase.types.primitives.AccountId;
import com.infosys.fentbase.types.primitives.CmCode;
import com.infosys.fentbase.types.valueobjects.AccountVO;
import com.infosys.fentbase.types.valueobjects.DateRangeVO;
import com.ncb.ebanking.common.CustomEBankingErrorCodes;
import com.ncb.ebanking.common.CustomEBankingQueryIdentifiers;
import com.ncb.ebanking.tao.CDRTTAO;
import com.ncb.ebanking.types.valueobjects.CustomTransHistoryReqDetailsVO;

/**
 * This class will create download request for the Accounts Transaction History
 * for longer duration.
 * 
 * @author Abhishek_Rana01
 * @version 1.0
 * @since FEBA 2.0
 */

public class CustomOpTransactionServiceSubmitRequestImpl extends
		AbstractLocalUpdateTran {
	/**
	 * Validations for the data submitted by the user.
	 */

	@Override
	public FEBAValItem[] prepareValidationsList(
			FEBATransactionContext objContext, IFEBAValueObject objInputOutput,
			IFEBAValueObject objTxnWM) throws BusinessException,
			BusinessConfirmation, CriticalException {
		OpTxnHistoryEnquiryVO txnHistoryEnquiryVO = (OpTxnHistoryEnquiryVO) objInputOutput;

		OpTxnHistoryCritVO txnHistoryCritVO = txnHistoryEnquiryVO.getCriteria();

		FEBAValItem vls[] = new FEBAValItem[] {

				new FEBAValItem("criteria.dateRangeVO.fromDate",
						txnHistoryCritVO.getDateRangeVO(), DateVal
								.getInstance(),
						FEBAValEngineConstants.DEPENDENT,
						EBankingErrorCodes.FROM_DATE_GREATER_THAN_TO_DATE),

				new FEBAValItem("criteria.accountIDER.accountID",
						txnHistoryCritVO.getAccountIDER().getAccountID(),
						FEBAValEngineConstants.MANDATORY,
						FEBAValEngineConstants.INDEPENDENT,
						EBankingErrorCodes.ACCOUNT_ID_MANDATORY),

		};
		return vls;
	}

	@Override
	/**
	 * Method to prepare the data to be inserted into the SCDT table for batch
	 * to execute and process the it.
	 * 
	 * @author Abhishek_Rana01
	 */
	public void process(FEBATransactionContext transactionContext,
			IFEBAValueObject optxnhistoryEnquiryVo, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {

		if (validateNumberOfRequest(transactionContext,
				optxnhistoryEnquiryVo, arg2)) {
			insertIntoCDRT(transactionContext, optxnhistoryEnquiryVo, arg2);
		}

	}

	/**
	 * This method will validate the limit of download request, requested by the
	 * user. User is not allowed to request more then the specified limit. After
	 * successful validation CDRT and SCDT entries will be inserted into the
	 * table. boolean
	 * 
	 * @param transactionContext
	 * @param optxnhistoryEnquiryVo
	 * @param arg2
	 * @return
	 * @throws BusinessException
	 * @throws BusinessConfirmation
	 * @throws CriticalException
	 */
	private boolean validateNumberOfRequest(
			FEBATransactionContext transactionContext,
			IFEBAValueObject optxnhistoryEnquiryVo, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {

		// Create an instance of QueryOperator, assign it to
		// fdttRequestCountOperator
		QueryOperator cdrtRequestCountOperator = QueryOperator.openHandle(
				transactionContext,
				CustomEBankingQueryIdentifiers.FETCH_TXN_HIST_REQ);
		OpTxnHistoryCritVO criteriaVO = ((OpTxnHistoryEnquiryVO)optxnhistoryEnquiryVo).getCriteria();
		FEBADate curDate = new FEBADate(DateUtil.getCurrentDate(transactionContext));
		// Associate fields to the fdttRequestCountOperator
		cdrtRequestCountOperator.associate("bankId", transactionContext.getBankId());
		cdrtRequestCountOperator.associate("orgId", transactionContext.getOrgId());
		cdrtRequestCountOperator.associate("userId", transactionContext.getUserId());
		final FEBAArrayList<CmCode> statusArrayList = new FEBAArrayList();
		statusArrayList.add(new CmCode("SUB"));
		statusArrayList.add(new CmCode("PRO"));
		statusArrayList.add(new CmCode("COM"));
		cdrtRequestCountOperator.associate("status", statusArrayList);
		cdrtRequestCountOperator.associate("rCreTime", curDate);
		try {
			int CUSTOM_NO_DOWNLOAD_REQUEST = Integer.valueOf(PropertyUtil
					.getProperty("CUSTOM_NO_DOWNLOAD_REQUEST",
							transactionContext));
			FEBAArrayList<CustomTransHistoryReqDetailsVO> list = cdrtRequestCountOperator
					.fetchList(transactionContext);
			
			int numberOfRequestInitiated = list.size();
			/**
			 * logic to validate maximum number of records in a day requested by
			 * user and subsequent request should not be raised between given
			 * interval of time in properties.
			 */
			int timediff = (int) (DateUtil.currentServerDate().getTime() - list
					.get(0).getRCreTime().getTimestampValue().getTime());
			long diffMinutes = timediff / (60 * 1000) % 60;
			long diffHours = timediff / (60 * 60 * 1000) % 24;
			long diffDays = timediff / (24 * 60 * 60 * 1000);
			if (diffHours == 0 && diffDays == 0 && diffMinutes < 90) {
				throw new BusinessException(
						true, transactionContext,
						"You have already initiated a request.Please wait before initiating again.",
						FEBAIncidenceCodes.MAX_POST_SIZE,
						null, CustomEBankingErrorCodes.REQ_SUBMITTED_WAIT, null, null);
			} else if (numberOfRequestInitiated >= CUSTOM_NO_DOWNLOAD_REQUEST) {
				throw new BusinessException(true, transactionContext,
						"The maximum limit of download request has exceeded.",
						FEBAIncidenceCodes.MAX_POST_SIZE,
						null, CustomEBankingErrorCodes.MAX_REQ_REACHED, null, null);
			} 

			for(int i=0;i<list.size();i++)
			{
				CustomTransHistoryReqDetailsVO customReqDetailsVO = list.get(i);
				if(criteriaVO.getLastNTransactions().getValue() != 0)
				{
					if(customReqDetailsVO.getAccountId().equals(criteriaVO.getAccountIDER().getAccountID()) && customReqDetailsVO.getNoOfTxns().getValue()==criteriaVO.getLastNTransactions().getValue())
					{
						throw new BusinessException(
								true, transactionContext,
								"You have already initiated a request with same criteria.",
								FEBAIncidenceCodes.MAX_POST_SIZE,
								null, CustomEBankingErrorCodes.SAME_CRIT_REQ, null, null);
					}
				}
				else
				{
					if(customReqDetailsVO.getAccountId().equals(criteriaVO.getAccountIDER().getAccountID()) && customReqDetailsVO.getFromDate().isSameAs(criteriaVO.getDateRangeVO().getFromDate()) && customReqDetailsVO.getToDate().isSameAs(criteriaVO.getDateRangeVO().getToDate()))
					{
						throw new BusinessException(
								true, transactionContext,
								"You have already initiated a request with same criteria.",
								FEBAIncidenceCodes.MAX_POST_SIZE,
								null, CustomEBankingErrorCodes.SAME_CRIT_REQ, null, null);
					}	
				}
			}
			return true;
			
		} catch (DALException e) {
			// return true if no records are fetched from the database
			return true;
		} finally {
			// Close fdttRequestCountOperator handle
			cdrtRequestCountOperator.closeHandle(transactionContext);
		}
	}

	/**
	 * 
	 * void
	 * 
	 * @param transactionContext
	 * @param optxnhistoryEnquiryVo
	 * @param arg2
	 * @throws BusinessException
	 * @throws BusinessConfirmation
	 * @throws CriticalException
	 */
	private void insertIntoCDRT(FEBATransactionContext transactionContext,
			IFEBAValueObject txnhistoryEnquiryVo, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		OpTxnHistoryEnquiryVO enquiryVO = (OpTxnHistoryEnquiryVO) txnhistoryEnquiryVo;

		CDRTTAO cdrtTAO = new CDRTTAO(transactionContext);
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String curDate = df.format(cal.getTime());
		// Populating accountVO for branch code details
		AccountId accountId = enquiryVO.getCriteria().getAccountIDER()
				.getAccountID();
		AccountVO accountVO = (AccountVO) FEBAAVOFactory
				.createInstance(TypesCatalogueConstants.AccountVO);
		accountVO.getAccountIDER().setAccountID(accountId);
		accountVO = UserEntityUtility.fetchFilteredAccount(transactionContext,
				accountVO);
		// setting the same in enquiryVO to be available in SCDT insert method
		enquiryVO.getCriteria().getAccountIDER().setBranchCode(
				accountVO.getAccountIDER().getBranchCode());
		enquiryVO.getCriteria().getAccountIDER().setAccountType(
				accountVO.getAccountIDER().getAccountType());
		cdrtTAO.associateBankId(transactionContext.getBankId());
		// associating org_id
		cdrtTAO.associateOrgId(transactionContext.getOrgId());
		cdrtTAO.associateUserId(transactionContext.getUserId());
		cdrtTAO.associateAccountNum(enquiryVO.getCriteria().getAccountIDER().getAccountID());
		cdrtTAO.associateBranchCode(enquiryVO.getCriteria().getAccountIDER().getBranchCode());
		cdrtTAO.associateAccountNickname(enquiryVO.getCriteria().getAccountIDER().getNickName());
		cdrtTAO.associateAccountType(enquiryVO.getCriteria().getAccountIDER().getAccountType());
		cdrtTAO.associateCrnCode(enquiryVO.getCriteria().getAccountIDER().getCurrency());
		FEBAUnboundString requestId = new FEBAUnboundString(transactionContext.getOrgId()+curDate);
		cdrtTAO.associateReqId(requestId);
		if(FEBATypesUtility.isNotBlankLong(enquiryVO.getCriteria().getLastNTransactions()) && enquiryVO.getCriteria().getLastNTransactions().getValue()!=0)
		{
			cdrtTAO.associateNoOfTxns(new FEBAUnboundLong(enquiryVO.getCriteria().getLastNTransactions().getValue()));
		}
		DateRangeVO dateRange = enquiryVO.getCriteria().getDateRangeVO();
		cdrtTAO.associateFromDate(dateRange.getFromDate());
		cdrtTAO.associateToDate(dateRange.getToDate());
		cdrtTAO.associateNoOfDownloads(new FEBAUnboundChar(EBankingConstants.NO_FLAG));
		cdrtTAO.associateReqStatus(new FEBAUnboundString("SUB"));
		cdrtTAO.associateSortOrder(enquiryVO.getCriteria().getSortingOrder());
		// Inserting record in CRDT ,after successful insertion it will try to
		// insert in SCDT for batch to pick up this request and update.
		try {
			cdrtTAO.insert(transactionContext);
		} catch (FEBATableOperatorException e) {
			throw new CriticalException(transactionContext,
					EBIncidenceCodes.RECORD_INSERTION_FAILED,
					"Error in Inserting details in CDRT",
					EBankingErrorCodes.RECORD_INSERTION_FAILED);

		}
		insertIntoSCDT(transactionContext, txnhistoryEnquiryVo, arg2, requestId);

	}
	/**
	 * This method is used to prepare and insert entry into the SCDT table so
	 * that scheduler can pick and process/generate the report. void
	 * 
	 * @param transactionContext
	 * @param optxnhistoryEnquiryVo
	 * @param arg2
	 * @param fileSequenceNum
	 * @throws BusinessException
	 * @throws BusinessConfirmation
	 * @throws CriticalException
	 */
	private void insertIntoSCDT(FEBATransactionContext transactionContext,
			IFEBAValueObject txnhistoryEnquiryVo, IFEBAValueObject arg2,
			FEBAUnboundString requestId) throws BusinessException,
			BusinessConfirmation, CriticalException {

		FEBAStringBuilder scdtTaskData = new FEBAStringBuilder("REQ_ID"
				+ EBankingConstants.EQUALS + requestId
				+ EBankingConstants.URL_SEPERATOR);

		SCDTTAO tao = new SCDTTAO(transactionContext);

		tao.associateBankId(transactionContext.getBankId());
		tao.associateDescription(new TaskDescription("Transaction History Batch"));
		tao.associateScheduleName(new ScheduleName("NCBTH" + requestId));
		tao.associateTaskName(new TaskName("NCBTXNHISTBATCH"));
		tao.associateTaskGroup(new FEBAUnboundChar(FEBABatchConstants.BATCH_TASK_GROUP));
		tao.associateSchTaskData(new TaskData(scdtTaskData.toString()));
		tao.associateSchedulePattern(new SchedulePattern(EBankingBatchConstants.SCHEDULE_PATTERN));
		tao.associateMaxNumberOfOccurrences(new NumberOfOccurrences(EBankingBatchConstants.ARG1));
		tao.associateHolidayOption(new HolidayOption(EBankingBatchConstants.STRING_NA));
		tao.associatePriority(new BatchPriority(EBankingBatchConstants.ARG1));

		Time startTime = new Time(DateUtil.currentDate(transactionContext).getTime());
		tao.associateStartTime(startTime);
		Time nextActivationTime = new Time(startTime.getValue() - 1000L);
		tao.associateNextActivationTime(nextActivationTime);
		tao.associateStatus(new TaskStatus(FEBABatchConstants.TASK_AVAILABLE));
		try {
			tao.insert(transactionContext);
			transactionContext.addBusinessInfo(getSuccessMessage());
		} catch (FEBATableOperatorException e) {
			if (e.getErrorCode() == (ErrorCodes.ANOTHER_RECORD_EXISTS)) {
				throw new BusinessException(transactionContext,
						"Record already exists!",
						FEBAIncidenceCodes.RECORD_ALREADY_EXIST,
						EBankingErrorCodes.DUPLICATE_RECORD_EXISTS);
			} else {
				throw new CriticalException(transactionContext, e.getMessage(),
						e.getErrorCode());
			}

		}

	}

	/**
	 * preparing successful message to be displayed after succesfull insertion
	 * of the record into the table. BusinessInfoVO
	 */
	private BusinessInfoVO getSuccessMessage() {

		final FEBAUnboundInt errorCode = new FEBAUnboundInt(
				CustomEBankingErrorCodes.TRANSACTION_HIS_REQ_SUCCESS);
		final FEBAUnboundString displayMessage = new FEBAUnboundString(
				"The request is posted successfully. Check after some time.");
		final FEBAUnboundString logMessage = new FEBAUnboundString(
				"Log:The request is posted successfully. Check after some time.");
		final BusinessInfoVO infoDetails = (BusinessInfoVO) FEBAAVOFactory
				.createInstance(TypesCatalogueConstants.BusinessInfoVO);
		infoDetails.setCode(errorCode);
		infoDetails.setLogMessage(logMessage);
		infoDetails.setDispMessage(displayMessage);
		infoDetails.setNameSpace("CUST");
		return infoDetails;
	}

}
