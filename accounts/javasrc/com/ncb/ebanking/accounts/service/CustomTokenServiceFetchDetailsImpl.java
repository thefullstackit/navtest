/**
 * 
 */
package com.ncb.ebanking.accounts.service;

import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;
import com.infosys.feba.framework.commontran.context.FEBATransactionContext;
import com.infosys.feba.framework.tao.FEBATableOperatorException;
import com.infosys.feba.framework.transaction.pattern.AbstractLocalInquiryTran;
import com.infosys.feba.framework.types.valueobjects.IFEBAValueObject;
import com.infosys.feba.framework.valengine.FEBAValItem;
import com.ncb.ebanking.tao.info.A_LGINInfo;
import com.ncb.ebanking.tao.A_LGINTAO;
import com.ncb.ebanking.types.valueobjects.CustomTokenDetailsVO;


/**
 * @author Rama_Ujjina
 *
 */
public class CustomTokenServiceFetchDetailsImpl extends AbstractLocalInquiryTran{

	@Override
	public FEBAValItem[] prepareValidationsList(FEBATransactionContext arg0, IFEBAValueObject arg1,
			IFEBAValueObject arg2) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void process(FEBATransactionContext context, IFEBAValueObject inOutVO, IFEBAValueObject arg2)
			throws BusinessException, BusinessConfirmation, CriticalException {

		 A_LGINTAO lginTAO = new A_LGINTAO();
		 CustomTokenDetailsVO detailsVO = (CustomTokenDetailsVO)  inOutVO;
		 try {
			A_LGINInfo lginInfo = lginTAO.select(context,context.getBankId(),context.getRecordUserId());
			
			detailsVO.setToken(lginInfo.getToken());
			
			
		} catch (FEBATableOperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
