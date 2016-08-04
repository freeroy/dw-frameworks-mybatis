package org.developerworld.frameworks.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.developerworld.commons.dbutils.sql.command.OrderByCommand;
import org.developerworld.commons.dbutils.sql.command.RowBoundCommand;
import org.developerworld.frameworks.mybatis.model.TestTable;

public interface TestTableMapper {

	public int insert(TestTable testTable);

	public int update(TestTable testTable);

	public int delete(int id);

	public TestTable selectByPK(int id);

	public List<TestTable> selectList(OrderByCommand orderByCommand, RowBoundCommand rowBoundCommand);

	public long selectCount();

	public List<TestTable> selectListByName(@Param("name")String name, OrderByCommand orderByCommand,
			RowBoundCommand rowBoundCommand);

	public long selectCountByName(@Param("name")String name);

	public List<TestTable> selectListByNames(@Param("names")String[] names, OrderByCommand orderByCommand,
			RowBoundCommand rowBoundCommand);

	public long selectCountByNames(@Param("names")String[] names);
}
