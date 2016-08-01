package org.developerworld.frameworks.mybatis.plugin;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;

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
import org.developerworld.commons.dbutils.sql.command.RowBoundCommand;
import org.developerworld.commons.dbutils.sql.dialect.rowbound.RowBoundDialect;
import org.developerworld.commons.dbutils.sql.dialect.rowbound.RowBoundDialectFactory;
import org.developerworld.frameworks.mybatis.mapping.SqlSourceWrapper;

/**
 * 动态分页插件
 * 
 * @author Roy Huang
 *
 */
@Intercepts({ @Signature(args = { MappedStatement.class, Object.class, RowBounds.class,
		ResultHandler.class }, method = "query", type = Executor.class) })
public class RowBoundCommandInterceptor extends AbstractInterceptorSupport {

	/* 分页方言对象 */
	private RowBoundDialect rowBoundDialect;
	/* 分页方言类 */
	private Class<RowBoundDialect> rowBoundDialectClass;
	/* 是否隐藏参数 */
	private boolean hideArg = true;

	public void setRowBoundDialect(RowBoundDialect rowBoundDialect) {
		this.rowBoundDialect = rowBoundDialect;
	}

	public void setRowBoundDialectClass(Class<RowBoundDialect> rowBoundDialectClass) {
		this.rowBoundDialectClass = rowBoundDialectClass;
	}

	public boolean isHideArg() {
		return hideArg;
	}

	public void setHideArg(boolean hideArg) {
		this.hideArg = hideArg;
	}

	@Override
	public void setProperties(Properties properties) {
		try {
			if (properties.containsKey("hideArg"))
				hideArg = Boolean.valueOf(properties.getProperty("hideArg"));
			if (properties.containsKey("rowBoundDialectClass"))
				rowBoundDialectClass = (Class<RowBoundDialect>) Class
						.forName(properties.getProperty("rowBoundDialectClass"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private RowBoundDialect getRowBoundDialect(Invocation invocation) {
		if (rowBoundDialect == null) {
			if (rowBoundDialectClass != null) {
				try {
					rowBoundDialect = rowBoundDialectClass.newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (rowBoundDialect == null) {
				try {
					Connection connection = ((MappedStatement) invocation.getArgs()[0]).getConfiguration()
							.getEnvironment().getDataSource().getConnection();
					try {
						rowBoundDialect = RowBoundDialectFactory.buildRowBoundDialect(connection);
					} finally {
						connection.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return rowBoundDialect;
	}

	public Object intercept(Invocation invocation) throws Throwable {
		// 提取核心参数
		Object[] args = invocation.getArgs();
		MappedStatement mappedStatement = (MappedStatement) args[0];
		// 不是查询，传递至下一个执行链
		if (!mappedStatement.getSqlCommandType().equals(SqlCommandType.SELECT))
			return invocation.proceed();
		// 完全无请求参数，传递至下一个执行链
		if (args.length > 1 && args[1] == null)
			return invocation.proceed();
		Object parameterObject = args[1];
		RowBoundCommand rowBoundCommand = null;
		List<RowBoundCommand> rowBoundCommands = getArgObjects(invocation, RowBoundCommand.class);
		// 无分页对象，代表无需分页，传递至下一个执行链
		if (rowBoundCommands == null || rowBoundCommands.size() == 0)
			return invocation.proceed();
		rowBoundCommand = rowBoundCommands.get(rowBoundCommands.size() - 1);
		// 判断是否隐藏参数
		if (isHideArg())
			removeArgs(invocation, RowBoundCommand.class, true);
		Object rst = null;
		// 获取sql对象
		BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
		// 获取原生sql信息
		String sql = boundSql.getSql();
		// 重新构建带分页的sql
		String pageSql = buildPageSql(invocation, sql, rowBoundCommand);
		// 若构建不了，代表无合适的分页语法，使用默认原生操作
		if (pageSql == null) {
			RowBounds oldRowBounds = null;
			if (args.length > 2 && args[2] != null)
				oldRowBounds = (RowBounds) args[2];
			try {
				RowBounds newBounds = null;
				if (rowBoundCommand.getOffset() != null && rowBoundCommand.getLimit() != null)
					newBounds = new RowBounds(rowBoundCommand.getOffset(), rowBoundCommand.getLimit());
				else if (rowBoundCommand.getOffset() == null && rowBoundCommand.getLimit() == null)
					newBounds = new RowBounds();
				else if (rowBoundCommand.getOffset() != null)
					newBounds = new RowBounds(rowBoundCommand.getOffset(), Integer.MAX_VALUE);
				else if (rowBoundCommand.getLimit() != null)
					newBounds = new RowBounds(0, rowBoundCommand.getLimit());
				args[2] = newBounds;
				rst = invocation.proceed();
			} finally {
				// 还原rowBounds
				args[2] = oldRowBounds;
			}
		} else {
			RowBounds oldRowBounds = null;
			if (args.length > 2 && args[2] != null)
				oldRowBounds = (RowBounds) args[2];
			SqlSource sqlSource = mappedStatement.getSqlSource();
			MetaObject mappedStatementMetaObject = SystemMetaObject.forObject(mappedStatement);
			SqlSourceWrapper sqlSourceWarpper = new SqlSourceWrapper(sqlSource, pageSql);
			try {
				// 若分页插件不支持指定开始位置，需要利用原生rowBound进行跳过
				RowBounds newRowBounds = null;
				if (rowBoundCommand.getOffset() != null && rowBoundCommand.getOffset() >= 0
						&& !isSupportOffset(invocation)) {
					newRowBounds = new RowBounds(rowBoundCommand.getOffset(), rowBoundCommand.getLimit());
					args[2] = newRowBounds;
				}
				mappedStatementMetaObject.setValue("sqlSource", sqlSourceWarpper);
				args[0] = mappedStatement;
				rst = invocation.proceed();
			} finally {
				// 删除当前线程对象
				sqlSourceWarpper.clearLocalSql();
				// 还原sqlSource
				mappedStatementMetaObject.setValue("sqlSource", sqlSource);
				args[2] = oldRowBounds;
			}
		}
		return rst;
	}

	/**
	 * 构建分页sql语句
	 * 
	 * @param invocation
	 * @param sql
	 * @return
	 */
	private String buildPageSql(Invocation invocation, String sql, RowBoundCommand rowBoundCommand) {
		RowBoundDialect rowBoundDialect = getRowBoundDialect(invocation);
		if (rowBoundDialect != null && rowBoundCommand != null)
			return rowBoundDialect.buildRowBoundSql(sql, rowBoundCommand.getLimit(), rowBoundCommand.getOffset());
		return null;
	}

	/**
	 * 是否支持指定开始位置
	 * 
	 * @return
	 */
	private boolean isSupportOffset(Invocation invocation) {
		RowBoundDialect rowBoundDialect = getRowBoundDialect(invocation);
		return rowBoundDialect != null && rowBoundDialect.supportOffset();
	}

}
