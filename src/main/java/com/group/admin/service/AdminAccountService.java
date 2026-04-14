package com.group.admin.service;

import com.group.admin.req.admin.AccountFilterCondition;
import com.group.admin.req.admin.CreateAdminAccountReq;
import com.group.admin.req.admin.UpdateAccountRoleReq;
import com.group.admin.req.admin.UpdateAccountStatusReq;
import com.group.admin.res.admin.AdminAccountRes;
import java.util.Map;

public interface AdminAccountService {
    AdminAccountRes createAccount(CreateAdminAccountReq req, String adminUserId);
    Map<String, Object> listAccounts(AccountFilterCondition filters, int page, int size);
    AdminAccountRes updateStatus(String id, UpdateAccountStatusReq req, String adminUserId);
    AdminAccountRes updateRole(String id, UpdateAccountRoleReq req, String adminUserId);
}
