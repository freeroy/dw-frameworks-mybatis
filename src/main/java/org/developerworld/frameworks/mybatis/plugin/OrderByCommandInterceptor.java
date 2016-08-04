package org.developerworld.frameworks.mybatis.plugin;

import java.util.List;

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
import org.developerworld.frameworks.mybatis.mapping.SqlSourceWrapper;

/**
 * 动态排序插件
 * 
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
		OrderByCommand orderByCommand = null;
		List<OrderByCommand> orderByCommands=getArgObjects(invocation, OrderByCommand.class);
		// 无排序对象，传递至下一个执行链
		if (orderByCommands==null || orderByCommands.size()==0)
			return invocation.proceed();
		orderByCommand=orderByCommands.get(orderByCommands.size()-1);
		// 获取sql对象
		BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
		// 获取原生sql信息
		String sql = boundSql.getSql();
		// 重新构建带排序的sql
		String orderBySql = buildOrderBySql(invocation, sql, orderByCommand);
		// 若构建不了，传递至下一个执行链
		if (orderBySql == null)
			return invocation.proceed();
		Object rst = null;
		SqlSource sqlSource = mappedStatement.getSqlSource();
		MetaObject mappedStatementMetaObject = SystemMetaObject.forObject(mappedStatement);
		SqlSourceWrapper sqlSourceWarpper = new SqlSourceWrapper(sqlSource, orderBySql);
		try {
			mappedStatementMetaObject.setValue("sqlSource", sqlSourceWarpper);
			args[0] = mappedStatement;
			rst = invocation.proceed();
		} finally {
			// 删除当前线程对象
			sqlSourceWarpper.clearLocalSql();
			// 还原对象
			mappedStatementMetaObject.setValue("sqlSource", sqlSource);
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
			sql += " " + orderByCommand.buildSql();
		return sql;
	}

}
