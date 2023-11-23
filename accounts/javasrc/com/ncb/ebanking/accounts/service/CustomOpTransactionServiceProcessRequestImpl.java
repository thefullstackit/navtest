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

import com.infosys.ebanking.common.EBIncidenceCodes;
import com.infosys.ebanking.common.EBankingBatchConstants;
import com.infosys.ebanking.common.EBankingConstants;
import com.infosys.ebanking.common.EBankingErrorCodes;
import com.infosys.ebanking.hif.EBRequestConstants;
import com.infosys.ebanking.types.TypesCatalogueConstants;
import com.infosys.ebanking.types.valueobjects.TxnHistoryDetailsVO;
import com.infosys.ebanking.types.valueobjects.TxnHistoryEnquiryVO;
import com.infosys.feba.framework.batch.FEBABatchConstants;
import com.infosys.feba.framework.common.ErrorCodes;
import com.infosys.feba.framework.common.FEBAIncidenceCodes;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.common.exception.FEBAException;
import com.infosys.feba.framework.common.logging.LogManager;
import com.infosys.feba.framework.common.util.DateUtil;
import com.infosys.feba.framework.common.util.resource.PropertyUtil;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.hif.EBHostInvoker;
import com.infosys.feba.framework.service.LocalServiceUtil;
import com.infosys.feba.framework.tao.FEBATableOperatorException;
import com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran;
import com.infosys.feba.framework.transaction.pattern.AbstractHostUpdateTran;
import com.infosys.feba.framework.types.FEBAStringBuilder;
import com.infosys.feba.framework.types.lists.FEBAArrayList;
import com.infosys.feba.framework.types.primitives.BatchPriority;
import com.infosys.feba.framework.types.primitives.FEBAUnboundChar;
import com.infosys.feba.framework.types.primitives.FEBAUnboundInt;
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
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.infosys.fentbase.tao.SCDTTAO;
import com.ncb.ebanking.tao.CDRTTAO;
import com.ncb.ebanking.tao.info.CDRTInfo;
import com.ncb.ebanking.types.valueobjects.CustomTransHistoryReqDetailsVO;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

/**
 * This class will fetch and pass the transaction history of user given date
 * range for longer duration.
 * 
 * @author Abhishek_Rana01
 * @version 1.0
 * @since FEBA 2.0
 */

public class CustomOpTransactionServiceProcessRequestImpl extends
		AbstractHostUpdateTran {

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0,
			IFEBAValueObject arg1, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		/*
		 * No validation needed as all the validations are done at the frontend
		 * insertion
		 */
		return new FEBAValItem[]{};
	}

	/**
	 * Method to invoke the host call and fetch the transaction history for the
	 * requested date range.
	 */
	@Override
	protected void processHostData(FEBATransactionContext objTxnContext,
			IFEBAValueObject objInputOutput, IFEBAValueObject objTxnWM)
			throws BusinessException, BusinessConfirmation, CriticalException {
		TxnHistoryEnquiryVO txnHistoryEnqVO = (TxnHistoryEnquiryVO) objInputOutput;
		int CUSTOM_MAX_RETRIES_THIS_ALLOWED = Integer.valueOf(PropertyUtil
				.getProperty("CUSTOM_MAX_RETRIES_THIS_ALLOWED",
						objTxnContext));
		CustomTransHistoryReqDetailsVO detailsVO = (CustomTransHistoryReqDetailsVO)txnHistoryEnqVO.getExtensionVO();
		FEBAArrayList<TxnHistoryDetailsVO> newResultList = new FEBAArrayList<TxnHistoryDetailsVO>();
		try {
			EBHostInvoker.processRequest(objTxnContext,"CustomTransactionHistoryServiceFetchRequest", txnHistoryEnqVO);
			if ( txnHistoryEnqVO.getResultList() == null || txnHistoryEnqVO.getResultList().size() == 0) {
				throw new BusinessException(
						objTxnContext,
						EBIncidenceCodes.NO_TRANSACTIONS_WERE_FOUND_FOR_THIS_ACCOUNT,
						"No Accounts Found",
						EBankingErrorCodes.NO_TRANSACTIONS_FOUND);
			}
			//&& txnHistoryEnqVO.getCriteria().getLastNTransactions().getValue() % 20 != 0
			if(txnHistoryEnqVO.getCriteria().getLastNTransactions().getValue() != 0 
					&& txnHistoryEnqVO.getCriteria().getLastNTransactions().getValue() < txnHistoryEnqVO.getResultList().size()) 
			{
				for(int i=0; i<txnHistoryEnqVO.getCriteria().getLastNTransactions().getValue(); i++)
				{
					TxnHistoryDetailsVO txnHistoryDetailsVO = (TxnHistoryDetailsVO)txnHistoryEnqVO.getResultList().get(i);
					newResultList.add(txnHistoryDetailsVO);
				}
				txnHistoryEnqVO.setResultList(newResultList);
			}

		} catch (BusinessException ce) {
			CDRTTAO cdrtTAO = new CDRTTAO(objTxnContext);
			CDRTInfo cdrtInfo = new CDRTInfo();
			try {
					cdrtInfo = CDRTTAO.select(objTxnContext, detailsVO.getRequestId());
			} catch (FEBATableOperatorException e1) {
				LogManager.log(objTxnContext, e1.getMessage(), LogManager.MESSAGE);
			}
			FEBAUnboundInt noOfFailures = cdrtInfo.getNoOfFailures();
			cdrtTAO.associateReqId(detailsVO.getRequestId());
			cdrtTAO.associateCookie(cdrtInfo.getCookie());
	
			if(cdrtInfo.getNoOfFailures().getValue() < CUSTOM_MAX_RETRIES_THIS_ALLOWED)
			{
				cdrtTAO.associateReqStatus(new FEBAUnboundString("PRO"));
				int noOfFailuresUpdated = noOfFailures.getValue()+1;
				cdrtTAO.associateNoOfFailures(new FEBAUnboundInt(noOfFailuresUpdated));
				cdrtTAO.associateFailureReason(new FEBAUnboundString("Table Insertion Failed"));

				FEBAStringBuilder scdtTaskData = new FEBAStringBuilder("REQ_ID"
						+ EBankingConstants.EQUALS + detailsVO.getRequestId()
						+ EBankingConstants.URL_SEPERATOR);

				SCDTTAO tao = new SCDTTAO(objTxnContext);

				tao.associateBankId(objTxnContext.getBankId());
				tao.associateDescription(new TaskDescription("Transaction History Download" + detailsVO.getRequestId()));
				tao.associateScheduleName(new ScheduleName("NCBTH" + detailsVO.getRequestId()+String.valueOf(noOfFailuresUpdated)));
				tao.associateTaskName(new TaskName("NCBTXNHISTBATCH"));
				tao.associateTaskGroup(new FEBAUnboundChar(FEBABatchConstants.BATCH_TASK_GROUP));
				tao.associateSchTaskData(new TaskData(scdtTaskData.toString()));
				tao.associateSchedulePattern(new SchedulePattern(EBankingBatchConstants.SCHEDULE_PATTERN));
				tao.associateMaxNumberOfOccurrences(new NumberOfOccurrences(EBankingBatchConstants.ARG1));
				tao.associateHolidayOption(new HolidayOption(EBankingBatchConstants.STRING_NA));
				tao.associatePriority(new BatchPriority(EBankingBatchConstants.ARG1));

				Time startTime = new Time(DateUtil.currentDate(objTxnContext).getTime());
				tao.associateStartTime(startTime);
				Time nextActivationTime = new Time(startTime.getValue() - 1000L);
				tao.associateNextActivationTime(nextActivationTime);
				tao.associateStatus(new TaskStatus(FEBABatchConstants.TASK_AVAILABLE));
				try {
					tao.insert(objTxnContext);
				} catch (FEBATableOperatorException e2) {
					if (e2.getErrorCode() == (ErrorCodes.ANOTHER_RECORD_EXISTS)) {
						throw new BusinessException(objTxnContext,
								"Record already exists!",
								FEBAIncidenceCodes.RECORD_ALREADY_EXIST,
								EBankingErrorCodes.DUPLICATE_RECORD_EXISTS);
					} else {
						throw new CriticalException(objTxnContext, e2.getMessage(),
								e2.getErrorCode());
					}

				}
			}
			else
			{
				cdrtTAO.associateReqStatus(new FEBAUnboundString("CNP"));
				cdrtTAO.associateFailureReason(new FEBAUnboundString("Table Insertion Failed"));
			}
			try {
				cdrtTAO.update(objTxnContext);
			} catch (FEBATableOperatorException e1) {
				LogManager.log(objTxnContext, e1.getMessage(), LogManager.MESSAGE);
			}
			objTxnContext.getConnection().commit();

			final BusinessInfoVO infoDetails = (BusinessInfoVO) FEBAAVOFactory

			.createInstance(TypesCatalogueConstants.BusinessInfoVO);

			if (ce.getErrorCode() == EBankingErrorCodes.NO_TRANSACTIONS_FOUND) {
				throw ce;
			}

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
							objTxnContext,

							EBIncidenceCodes.WE_ARE_UNABLE_TO_PROCESS_YOUR_REQUEST_RIGHT_NOW_PLEASE_TRY_AFTER_SOMETIME,

							EBankingErrorCodes.UNABLE_TO_PROCESS_REQUEST, se);

				}

				if (errorCode.getValue() == EBankingErrorCodes.NO_RECORDS_FOUND) {
					
					throw new BusinessException(

							objTxnContext,

							EBIncidenceCodes.NO_TRANSACTIONS_WERE_FOUND_FOR_THIS_ACCOUNT,

							"No Accounts Found",

							EBankingErrorCodes.NO_TRANSACTIONS_FOUND, ce);

				}

				else {

					/* if any othet business exception */

					throw new BusinessException(

							objTxnContext,

							EBIncidenceCodes.WE_ARE_UNABLE_TO_PROCESS_YOUR_REQUEST_RIGHT_NOW_PLEASE_TRY_AFTER_SOMETIME,

							"Unable to process request",

							errorCode.getValue(), ce);

				}
			} else {
				throw ce;
			}

			/* Incase the Offline Handler is throwing business exception */

		}

		catch (Exception e) {
			CDRTTAO cdrtTAO = new CDRTTAO(objTxnContext);
			CDRTInfo cdrtInfo = new CDRTInfo();
			try {
					cdrtInfo = CDRTTAO.select(objTxnContext, detailsVO.getRequestId());
			} catch (FEBATableOperatorException e1) {
				LogManager.log(objTxnContext, e1.getMessage(), LogManager.MESSAGE);
			}
			FEBAUnboundInt noOfFailures = cdrtInfo.getNoOfFailures();
			cdrtTAO.associateReqId(detailsVO.getRequestId());
			cdrtTAO.associateCookie(cdrtInfo.getCookie());
	
			if(cdrtInfo.getNoOfFailures().getValue() < CUSTOM_MAX_RETRIES_THIS_ALLOWED)
			{
				cdrtTAO.associateReqStatus(new FEBAUnboundString("PRO"));
				int noOfFailuresUpdated = noOfFailures.getValue()+1;
				cdrtTAO.associateNoOfFailures(new FEBAUnboundInt(noOfFailuresUpdated));
				cdrtTAO.associateFailureReason(new FEBAUnboundString("Table Insertion Failed"));

				FEBAStringBuilder scdtTaskData = new FEBAStringBuilder("REQ_ID"
						+ EBankingConstants.EQUALS + detailsVO.getRequestId()
						+ EBankingConstants.URL_SEPERATOR);

				SCDTTAO tao = new SCDTTAO(objTxnContext);

				tao.associateBankId(objTxnContext.getBankId());
				tao.associateDescription(new TaskDescription("Transaction History Download" + detailsVO.getRequestId()));
				tao.associateScheduleName(new ScheduleName("NCBTH" + detailsVO.getRequestId()+String.valueOf(noOfFailuresUpdated)));
				tao.associateTaskName(new TaskName("NCBTXNHISTBATCH"));
				tao.associateTaskGroup(new FEBAUnboundChar(FEBABatchConstants.BATCH_TASK_GROUP));
				tao.associateSchTaskData(new TaskData(scdtTaskData.toString()));
				tao.associateSchedulePattern(new SchedulePattern(EBankingBatchConstants.SCHEDULE_PATTERN));
				tao.associateMaxNumberOfOccurrences(new NumberOfOccurrences(EBankingBatchConstants.ARG1));
				tao.associateHolidayOption(new HolidayOption(EBankingBatchConstants.STRING_NA));
				tao.associatePriority(new BatchPriority(EBankingBatchConstants.ARG1));

				Time startTime = new Time(DateUtil.currentDate(objTxnContext).getTime());
				tao.associateStartTime(startTime);
				Time nextActivationTime = new Time(startTime.getValue() - 1000L);
				tao.associateNextActivationTime(nextActivationTime);
				tao.associateStatus(new TaskStatus(FEBABatchConstants.TASK_AVAILABLE));
				try {
					tao.insert(objTxnContext);
				} catch (FEBATableOperatorException e2) {
					if (e2.getErrorCode() == (ErrorCodes.ANOTHER_RECORD_EXISTS)) {
						throw new BusinessException(objTxnContext,
								"Record already exists!",
								FEBAIncidenceCodes.RECORD_ALREADY_EXIST,
								EBankingErrorCodes.DUPLICATE_RECORD_EXISTS);
					} else {
						throw new CriticalException(objTxnContext, e2.getMessage(),
								e2.getErrorCode());
					}

				}
			}
			else
			{
				cdrtTAO.associateReqStatus(new FEBAUnboundString("CNP"));
				cdrtTAO.associateFailureReason(new FEBAUnboundString("Table Insertion Failed"));
			}
			try {
				cdrtTAO.update(objTxnContext);
			} catch (FEBATableOperatorException e1) {
				LogManager.log(objTxnContext, e1.getMessage(), LogManager.MESSAGE);
			}
			objTxnContext.getConnection().commit();
			throw new BusinessException(
					objTxnContext,
					EBIncidenceCodes.UNABLE_TO_PROCESS_REQUEST_PLEASE_TRY_AFTER_SOME_TIME,

					EBankingErrorCodes.UNABLE_TO_PROCESS_REQUEST, e);

		}

	}

	/**
	 * This method is used to fetch and process local data before making a host
	 * call to fetch the host data. This function checks whether the user has
	 * inquiry access to the account detail and throws error accordingly.
	 * 
	 * @author Abhishek_Rana01
	 * @param objContext
	 * @param objInputOutput
	 * @param objTxnWM
	 * @since FEBA 2.0
	 * @see com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran#processLocalData(com.infosys.feba.framework.context.FEBATransactionContext,
	 *      com.infosys.feba.framework.types.valueobjects.IFEBAValueObject,
	 *      com.infosys.feba.framework.types.valueobjects.IFEBAValueObject)
	 * @throws CriticalException
	 * @throws BusinessConfirmation
	 * @throws BusinessException
	 */
	@Override
	protected void processLocalData(FEBATransactionContext objContext,
			IFEBAValueObject objInputOutput, IFEBAValueObject objTxnWM)
			throws BusinessException, BusinessConfirmation, CriticalException {
	}
}
