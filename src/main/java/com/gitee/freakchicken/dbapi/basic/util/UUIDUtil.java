package com.gitee.freakchicken.dbapi.basic.util;

import org.apache.commons.lang3.RandomStringUtils;

public class UUIDUtil {

    public static String id() {


        return RandomStringUtils.random(8,true,true);
    }

}
