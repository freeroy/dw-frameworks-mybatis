package org.developerworld.frameworks.mybatis.plugin;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.developerworld.commons.dbutils.sql.command.OrderByCommand;

/**
 * 动态排序插件
 * @author Roy Huang
 *
 */
@Intercepts({ @Signature(args = { MappedStatement.class, Object.class, RowBounds.class,
		ResultHandler.class }, method = "query", type = Executor.class) })
public class OrderByCommandInterceptor extends AbstractInterceptorSupport {

	public Object intercept(Invocation invocation) throws Throwable {
		// 提取核心参数
		Object[] args = invocation.getArgs();
		MappedStatement mappedStatement = (MappedStatement) args[0];
		if (!mappedStatement.getSqlCommandType().equals(SqlCommandType.SELECT))
			return invocation.proceed();
		// 完全无请求参数，传递至下一个执行链
		if (args.length > 1 && args[1] == null)
			return invocation.proceed();
		Object parameterObject = args[1];
		OrderByCommand orderByCommand = getOrderByCommand(parameterObject);
		// 无排序对象，传递至下一个执行链
		if (orderByCommand == null)
			return invocation.proceed();
		// 获取sql对象
		BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
		// 获取原生sql信息
		String sql = boundSql.getSql();
		// 重新构建带排序的sql
		String orderBySql = buildOrderBySql(invocation, sql, orderByCommand);
		// 若构建不了，传递至下一个执行链
		if (orderBySql == null)
			return invocation.proceed();
		Object rst=null;
		SqlSource _sqlSource=mappedStatement.getSqlSource();
		MetaObject mappedStatementMetaObject = SystemMetaObject.forObject(mappedStatement);
		try{
			MetaObject boundSqlMetaObject = SystemMetaObject.forObject(boundSql);
			boundSqlMetaObject.setValue("sql", orderBySql);
			mappedStatementMetaObject.setValue("sqlSource", new SqlSourceWrap(boundSql));
			args[0] = mappedStatement;
			rst=invocation.proceed();
		}
		finally{
			//还原对象
			mappedStatementMetaObject.setValue("sqlSource", _sqlSource);
		}
		return rst;
	}

	/**
	 * 构建排序sql
	 * 
	 * @param invocation
	 * @param sql
	 * @param orderByCommand
	 * @return
	 */
	private String buildOrderBySql(Invocation invocation, String sql, OrderByCommand orderByCommand) {
		if (orderByCommand.hasOrder())
			sql += " " + orderByCommand.getWithOrderBySql();
		return sql;
	}

	/**
	 * 获取排序参数对象
	 * 
	 * @param parameterObject
	 * @return
	 */
	private OrderByCommand getOrderByCommand(Object parameterObject) {
		if (parameterObject instanceof OrderByCommand)
			return (OrderByCommand) parameterObject;
		else if (parameterObject instanceof Map) {
			Map parameterMap = (Map) parameterObject;
			Iterator<Entry> iterator = parameterMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry entry = iterator.next();
				if (entry.getValue() instanceof OrderByCommand)
					return (OrderByCommand) entry.getValue();
			}
		}
		return null;
	}

}
