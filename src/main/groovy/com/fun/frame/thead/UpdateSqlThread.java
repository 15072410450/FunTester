package com.fun.frame.thead;

import com.fun.base.constaint.ThreadBase;
import com.fun.base.interfaces.IMySqlBasic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库多线程类,update方法类，区别于querythread
 */
public class UpdateSqlThread extends ThreadBase {

    private static Logger logger = LoggerFactory.getLogger(UpdateSqlThread.class);

    String sql;

    IMySqlBasic base;

    public UpdateSqlThread(IMySqlBasic base, String sql, int times) {
        this.times = times;
        this.sql = sql;
        this.base = base;
    }

    @Override
    protected void before() {
        base.getConnection();
    }

    @Override
    protected void doing() {
        base.excuteUpdateSql(sql);
    }

    @Override
    protected void after() {
        base.mySqlOver();
    }
}
