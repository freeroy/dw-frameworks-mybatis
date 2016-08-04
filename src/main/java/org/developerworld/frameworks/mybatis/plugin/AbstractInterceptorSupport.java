package org.developerworld.frameworks.mybatis.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

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
	protected <T> List<T> getArgObjects(Invocation invocation, Class<T> argObjectClass) {
		List<T> rst = new ArrayList<T>();
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
					if (argObjectClass.isInstance(entry.getValue()))
						rst.add((T) entry.getValue());
				}
			}
		}
		return rst;
	}
//
//	/**
//	 * 删除参数
//	 * 
//	 * @param invocation
//	 * @param argClass
//	 * @param fixArgs
//	 */
//	protected void removeArgs(Invocation invocation, Class argClass, boolean fixArgs) {
//		Object[] args = invocation.getArgs();
//		if (args != null && args.length > 1 && args[1] != null) {
//			Object parameterObject = args[1];
//			if (argClass.isInstance(parameterObject))
//				args[1] = null;
//			else if (parameterObject instanceof Map) {
//				Map parameterMap = (Map) parameterObject;
//				Iterator<Entry> iterator = parameterMap.entrySet().iterator();
//				while (iterator.hasNext()) {
//					Entry entry = iterator.next();
//					if (argClass.isInstance(entry.getValue()))
//						iterator.remove();
//				}
//				fixArgs(fixArgs, args, parameterMap);
//			}
//		}
//	}
//
//	/**
//	 * 删除参数
//	 * 
//	 * @param invocation
//	 * @param arg
//	 * @param fixArgs
//	 */
//	protected void removeArgs(Invocation invocation, Object arg, boolean fixArgs) {
//		Object[] args = invocation.getArgs();
//		if (args != null && args.length > 1 && args[1] != null) {
//			Object parameterObject = args[1];
//			if (parameterObject.equals(arg))
//				args[1] = null;
//			else if (parameterObject instanceof Map) {
//				Map parameterMap = (Map) parameterObject;
//				Iterator<Entry> iterator = parameterMap.entrySet().iterator();
//				while (iterator.hasNext()) {
//					Entry entry = iterator.next();
//					if (entry.getValue().equals(arg)) {
//						iterator.remove();
//					}
//				}
//				fixArgs(fixArgs, args, parameterMap);
//			}
//		}
//	}
//
//	/**
//	 * 修正参数
//	 * 
//	 * @param fixArgs
//	 * @param args
//	 * @param parameterMap
//	 */
//	private void fixArgs(boolean fixArgs, Object[] args, Map parameterMap) {
//		// TODO 极端情况下，该逻辑会出错！！！（如设定变量注解值为数字，且数字值等于参数位置值）
//		if (fixArgs && parameterMap.size() == 2) {
//			// 需要判断是否制定了别名(若通过注解设定变量名，原来用数字代表的key会变成变量名)
//			Iterator<String> keyIterator = parameterMap.keySet().iterator();
//			String key1 = keyIterator.next();
//			String key2 = keyIterator.next();
//			if(
//				(NumberUtils.isNumber(key1) && key2.startsWith("param") && key2.equals("param"+(Integer.parseInt(key1)+1)))
//				||
//				(NumberUtils.isNumber(key2) && key1.startsWith("param") && key1.equals("param"+(Integer.parseInt(key2)+1)))
//			)
//				// 替换参数
//				args[1] = parameterMap.values().iterator().next();
//			else
//				// 理论上这操作是多余的
//				args[1] = parameterMap;
//		} else
//			// 理论上这操作是多余的
//			args[1] = parameterMap;
//	}
}
