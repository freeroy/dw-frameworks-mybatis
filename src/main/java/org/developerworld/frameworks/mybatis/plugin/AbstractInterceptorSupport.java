package org.developerworld.frameworks.mybatis.plugin;

import java.util.Properties;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Plugin;

public abstract class AbstractInterceptorSupport implements Interceptor {
	
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties properties) {
		
	}
	
	protected class SqlSourceWrap implements SqlSource {

		private BoundSql boundSql;

		SqlSourceWrap(BoundSql boundSql) {
			this.boundSql = boundSql;
		}

		@Override
		public BoundSql getBoundSql(Object parameterObject) {
			return boundSql;
		}

	}
}
