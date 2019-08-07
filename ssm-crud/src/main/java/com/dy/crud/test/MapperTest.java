package com.dy.crud.test;

import java.util.UUID;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dy.crud.bean.Department;
import com.dy.crud.bean.Employee;
import com.dy.crud.dao.DepartmentMapper;
import com.dy.crud.dao.EmployeeMapper;

/**
 * 测试dao层的工作
 * 推荐Spring的项目就可以使用Spring的单元测试，可以字段注入我们需要的组件
 * 1.导入SpringTest模块
 * 2.使用@ContextConfiguration注解指定Spring配置文件的位置
 * 3.直接autowired要使用的组件即可
 * @author dy
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class MapperTest {

	@Autowired
	DepartmentMapper departmentMapper;
	
	@Autowired
	EmployeeMapper employeeMapper;
	
	@Autowired
	SqlSession sqlSession;
	
	/**
	 * 测试DepartmentMapper
	 */
	@Test
	public void testCRUD() {
		/*
		 * //1.创建Spring的IOC容器 ApplicationContext ioc = new
		 * ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		 * //2.从容器中获取mapper ioc.getBean(Department.class);
		 */
		//System.out.println(departmentMapper);
		
		//1.插入几个部门
		//departmentMapper.insertSelective(new Department(null, "开发部"));
		//departmentMapper.insertSelective(new Department(null, "测试部"));
		//2.生成员工数据，测试员工插入
		//employeeMapper.insertSelective(new Employee(null, "Jerry", "M", "Jerry@qq.com", 1));
		//3.批量插入多个员工，使用可以执行批量操作的sqlSession
		EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);
		for (int i = 0; i < 1000; i++) {
			String uid = UUID.randomUUID().toString().substring(0, 5)+i;
			mapper.insertSelective(new Employee(null, uid, "M",uid+"@dy.com", 1));
		}
		System.out.println("执行完成");
	}
	
	
}
