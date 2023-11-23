package com.ncb.ebanking.accounts.service;

import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.hif.EBHostInvoker;
import com.infosys.feba.framework.transaction.pattern.AbstractHostInquiryTran;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.ncb.ebanking.common.CustomEBRequestConstants;
import com.ncb.ebanking.types.valueobjects.CustomChartEnquiryVO;

public class CustomChartServiceFetchCCDetailsImpl extends AbstractHostInquiryTran {

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0, IFEBAValueObject arg1,
			IFEBAValueObject arg2) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		return new FEBAValItem[]{};
	}

	protected void processHostData(FEBATransactionContext objTxnContext, IFEBAValueObject objInputOutput,
			IFEBAValueObject objTxnWM) throws BusinessException, BusinessConfirmation, CriticalException {

		CustomChartEnquiryVO customChartEnquiryVO = (CustomChartEnquiryVO) objInputOutput;
        EBHostInvoker.processRequest(objTxnContext, CustomEBRequestConstants.CUSTOM_CHART_CC_EXPENSE_REQUEST, customChartEnquiryVO);


	}
	@Override
	protected void processLocalData(FEBATransactionContext arg0, IFEBAValueObject arg1, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub

	}

}
