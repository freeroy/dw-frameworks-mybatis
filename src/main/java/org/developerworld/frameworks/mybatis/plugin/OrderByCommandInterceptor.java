package org.developerworld.frameworks.mybatis.plugin;

import java.util.Set;

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
		Set<OrderByCommand> orderByCommands = getArgObjects(invocation, OrderByCommand.class);
		// 无排序对象，传递至下一个执行链
		if (orderByCommands == null || orderByCommands.size() == 0)
			return invocation.proceed();
		else if (orderByCommands.size() > 1)
			throw new IllegalArgumentException("OrderByCommand args can only set one!");
		orderByCommand = orderByCommands.iterator().next();
		// 是否有排序信息
		if (!orderByCommand.hasOrder())
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
		// 注意！这里假设原语句中，不存在分页信息
		// 判断是否有for update
		int bi = sql.toLowerCase().lastIndexOf(" for update");
		bi = bi == -1 ? sql.length() : bi;
		if (sql.substring(0, bi).toLowerCase().indexOf(" order by ") != -1)
			return sql.substring(0, bi) + "," + orderByCommand.buildSqlWithOutOrderBy()
					+ sql.substring(bi, sql.length());
		else
			return sql.substring(0, bi) + " " + orderByCommand.buildSql() + sql.substring(bi, sql.length());
	}

}
