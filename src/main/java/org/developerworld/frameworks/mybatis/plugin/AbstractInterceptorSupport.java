package org.developerworld.frameworks.mybatis.plugin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;

public abstract class AbstractInterceptorSupport implements Interceptor {

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties properties) {

	}

	/**
	 * 从参数中获取分页对象
	 * 
	 * @param parameterObject
	 * @return
	 */
	protected <T> Set<T> getArgObjects(Invocation invocation, Class<T> argObjectClass) {
		Set<T> rst = new HashSet<T>();
		Object[] args = invocation.getArgs();
		if (args != null && args.length > 1 && args[1] != null) {
			Object parameterObject = args[1];
			if (argObjectClass.isInstance(parameterObject))
				rst.add((T) parameterObject);
			else if (parameterObject instanceof Map) {
				Map parameterMap = (Map) parameterObject;
				Iterator<Entry> iterator = parameterMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry entry = iterator.next();
					if (entry.getValue()!=null && argObjectClass.isInstance(entry.getValue()))
						rst.add((T) entry.getValue());
				}
			}
		}
		return rst;
	}

}
