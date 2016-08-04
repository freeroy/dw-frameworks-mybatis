package org.developerworld.frameworks.mybatis.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.developerworld.commons.dbutils.sql.command.OrderByCommand;
import org.developerworld.commons.dbutils.sql.command.RowBoundCommand;
import org.developerworld.frameworks.mybatis.model.TestTable;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTableMapperTest {
	
	private static SqlSession session;
	private static TestTableMapper testTableMapper;
	
	@BeforeClass
	public static void beforeClass() throws IOException{
		String resource="mybatis.xml";
		InputStream inputStream = Resources.getResourceAsStream(resource);
		try{
			SqlSessionFactoryBuilder builder=new SqlSessionFactoryBuilder();
			SqlSessionFactory factory=builder.build(inputStream);
			session=factory.openSession();
			testTableMapper=session.getMapper(TestTableMapper.class);
		}
		finally{
			inputStream.close();
		}
	}
	
	@AfterClass
	public static void afterClass(){
		if(session!=null)
			session.close();
	}

	@Test
	public void testInsert() {
		TestTable testTable =new TestTable();
		testTable.setBirthDate(new Date());
		testTable.setDescription(RandomStringUtils.randomAlphabetic(500));
		testTable.setLoginTimes(RandomUtils.nextInt(999));
		testTable.setName(RandomStringUtils.randomAlphabetic(20));
		testTable.setSex((byte) RandomUtils.nextInt(1));
		int row=testTableMapper.insert(testTable);
		session.commit();
		Assert.assertTrue(row>0);
		Assert.assertTrue(testTable.getId()!=null);
	}

	@Test
	public void testUpdate() {
		testInsert();
		TestTable testTable=testTableMapper.selectList(new OrderByCommand("ID desc"), new RowBoundCommand(0,1)).get(0);
		String name=testTable.getName();
		testTable.setName(RandomStringUtils.randomAlphabetic(21));
		testTableMapper.update(testTable);
		session.commit();
		testTable=testTableMapper.selectByPK(testTable.getId());
		Assert.assertTrue(!testTable.getName().equals(name));
	}

	@Test
	public void testDelete() {
		testInsert();
		TestTable testTable=testTableMapper.selectList(new OrderByCommand("ID desc"), new RowBoundCommand(0,1)).get(0);
		int row=testTableMapper.delete(testTable.getId());
		session.commit();
		Assert.assertTrue(row>0);
	}

	@Test
	public void testSelectByPK() {
		TestTable testTable=testTableMapper.selectList(new OrderByCommand("ID desc"), new RowBoundCommand(0,1)).get(0);
		TestTable testTable2=testTableMapper.selectByPK(testTable.getId());
		Assert.assertTrue(testTable.equals(testTable2));
	}

	@Test
	public void testSelectList() {
		List<TestTable> datas=testTableMapper.selectList(null, null);
		Assert.assertTrue(datas.size()>0);
		datas=testTableMapper.selectList(null, new RowBoundCommand(1, 2));
		Assert.assertTrue(datas.size()==2);
		datas=testTableMapper.selectList(null, new RowBoundCommand(1, 2));
		List<TestTable> datas2=testTableMapper.selectList(new OrderByCommand("ID desc"), new RowBoundCommand(1, 2));
		Assert.assertTrue(!datas.equals(datas2));
	}

	@Test
	public void testSelectCount() {
		long count=testTableMapper.selectCount();
		Assert.assertTrue(count>0);
	}

	@Test
	public void testSelectListByName() {
		List<TestTable> datas=testTableMapper.selectListByName("%a%",null, null);
		Assert.assertTrue(datas.size()>0);
		datas=testTableMapper.selectListByName("%a%",null, new RowBoundCommand(1, 2));
		Assert.assertTrue(datas.size()==2);
		datas=testTableMapper.selectListByName("%a%",null, new RowBoundCommand(1, 2));
		List<TestTable> datas2=testTableMapper.selectListByName("%a%",new OrderByCommand("ID desc"), new RowBoundCommand(1, 2));
		Assert.assertTrue(!datas.equals(datas2));
	}

	@Test
	public void testSelectCountByName() {
		long count=testTableMapper.selectCountByName("%a%");
		Assert.assertTrue(count>0);
	}

	@Test
	public void testSelectListByNames() {
		List<TestTable> datas=testTableMapper.selectListByNames(new String[]{"%a%","%b%"},null, null);
		Assert.assertTrue(datas.size()>0);
		datas=testTableMapper.selectListByNames(new String[]{"%a%","%b%"},null, new RowBoundCommand(1, 2));
		Assert.assertTrue(datas.size()==2);
		datas=testTableMapper.selectListByNames(new String[]{"%a%","%b%"},null, new RowBoundCommand(1, 2));
		List<TestTable> datas2=testTableMapper.selectListByNames(new String[]{"%a%","%b%"},new OrderByCommand("ID desc"), new RowBoundCommand(1, 2));
		Assert.assertTrue(!datas.equals(datas2));
	}

	@Test
	public void testSelectCountByNames() {
		long count=testTableMapper.selectCountByNames(new String[]{"%a%","%b%"});
		Assert.assertTrue(count>0);
	}

}
