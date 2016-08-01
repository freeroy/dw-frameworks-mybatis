package org.developerworld.frameworks.mybatis.mapping;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

/**
 * sql源包装类 
 * 
 * 由于难保证sqlSource在mybatis机制上，会否会被多次重用，因此使用threadlocal方式实现，保证线程安全
 * 
 * @author Roy Huang
 *
 */
public class SqlSourceWrapper implements SqlSource {

	private static final ThreadLocal<String> localSql = new ThreadLocal<String>();

	private SqlSource sqlSource;

	public SqlSourceWrapper(SqlSource sqlSource, String sql) {
		this.sqlSource = sqlSource;
		localSql.set(sql);
	}

	public BoundSql getBoundSql(Object parameterObject) {
		BoundSql rst=sqlSource.getBoundSql(parameterObject);
		String sql=localSql.get();
		if(sql!=null){
			MetaObject boundSqlMetaObject = SystemMetaObject.forObject(rst);
			boundSqlMetaObject.setValue("sql", sql);
		}
		return rst;
	}

	public void clearLocalSql() {
		localSql.set(null);
		localSql.remove();
	}
}
