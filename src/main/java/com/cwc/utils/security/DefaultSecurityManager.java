package com.cwc.utils.security;

import java.security.Permission;

public class DefaultSecurityManager extends SecurityManager {

    @Override
    public void checkPermission(Permission perm) {
        // 默认不进行权限检查
        System.out.println("默认不进行任何限制");
    }
}
