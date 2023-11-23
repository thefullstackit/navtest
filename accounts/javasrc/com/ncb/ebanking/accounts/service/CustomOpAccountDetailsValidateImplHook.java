package com.ncb.ebanking.accounts.service;

import com.infosys.ebanking.common.EBTransactionContext;
import com.infosys.ebanking.types.valueobjects.IOpAccountDetailsVO;
import com.infosys.feba.framework.common.IContext;
import com.infosys.feba.framework.common.ICustomHook;
import com.infosys.feba.framework.common.exception.BusinessConfirmation;
import com.infosys.feba.framework.common.exception.BusinessException;
import com.infosys.feba.framework.common.exception.CriticalException;

public class CustomOpAccountDetailsValidateImplHook implements ICustomHook {

	@Override
	public void execute(IContext objContext, Object objInputOutput) throws BusinessException, BusinessConfirmation, CriticalException {
		// TODO Auto-generated method stubs test3
		IOpAccountDetailsVO opAccountDetailsVO = (IOpAccountDetailsVO) objInputOutput;
		System.out.println("In custom validate hook.."+ opAccountDetailsVO);
		if(opAccountDetailsVO!=null && opAccountDetailsVO.getCommonAccountDetails()!=null){
			System.out.println("In custom validate hook 1..");
			if(opAccountDetailsVO.getCommonAccountDetails().getAccountTypeER()!=null && opAccountDetailsVO.getCommonAccountDetails().getAccountIDER()!=null){
				System.out.println("In custom validate hook 2..");
				opAccountDetailsVO.getCommonAccountDetails().getAccountTypeER().setAccountType(opAccountDetailsVO.getCommonAccountDetails().getAccountIDER().getAccountType());
				System.out.println("In custom validate hook 3.."+ opAccountDetailsVO);
			}
		}
	}

}
