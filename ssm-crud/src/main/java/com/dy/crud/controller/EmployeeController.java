package com.dy.crud.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.crud.bean.Employee;
import com.dy.crud.bean.Msg;
import com.dy.crud.service.EmployeeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 处理员工的CRUD请求
 * 
 * @author dy
 *
 */
@Controller
public class EmployeeController {

	@Autowired
	EmployeeService employeeService;
	
	/**
	 * 单个批量二合一
	 * 批量删除用-隔开：1-2-3
	 * 单个删除：1
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/emp/{ids}",method = RequestMethod.DELETE)
	public Msg deleteEmpById(@PathVariable("ids")String ids) {
		if(ids.contains("-")) {//批量删除
			List<Integer> del_ids = new ArrayList<Integer>();
			String[] str_ids = ids.split("-");
			//组装id的集合
			for (String string : str_ids) {
				del_ids.add(Integer.parseInt(string));
			}
			employeeService.deleteBatch(del_ids);
		}else {//单个删除
			Integer id = Integer.parseInt(ids);
			employeeService.deleteEmp(id);
		}
		return Msg.success();
	}
	
	
	/**
	 * 如果直接发送ajax=PUT形式的请求
	 * 出问题：
	 * 请求体中有数据
	 * 但Employee对象封装不上
	 * 
	 * 原因：tomcat服务器
	 * 	会将请求体中的数据，封装为一个map
	 * 	request.getParameter("empName")就会从这个map中取值
	 * 	SpringMVC封装POJO对象的时候会把POJO中每个属性的值 ，request.getParameter("email");
	 * 
	 * ajax发送PUT请求引发的血案
	 * 	PUT请求：请求体中的数据request.getParameter("empName")拿不到
	 * 	Tomcat一看是PUT就不会封装请求体中的数据，只有POST形式的请求菜封装为map
	 * 	
	 * 	解决方案：
	 * 
	 * 我们要能支持直接发送PUT之类的请求还要封装请求体中的数据
	 * 配置上HttpPutFormContentFilter
	 * 作用：将请求体中的数据解析包装成一个map，request被重新包装，
	 * request.getParameter()方法被重写，就会从自己封装的map中取数据
	 * 
	 * 员工更新方法
	 * @param employee
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/emp/{empId}",method = RequestMethod.PUT)
	public Msg saveEmp(Employee employee) {
		employeeService.updateEmp(employee);
		return Msg.success();
	}
	
	/**
	 * 根据id查询员工
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/emp/{id}",method = RequestMethod.GET)
	@ResponseBody
	public Msg getEmp(@PathVariable("id")Integer id) {
		Employee employee = employeeService.getEmp(id);
		return Msg.success().add("emp", employee);
	}

	/**
	 * 导入jackson包
	 */
	@RequestMapping("/emps")
	@ResponseBody
	public Msg getEmpsWithJson(@RequestParam(value = "pn", defaultValue = "1") Integer pn) {
		// 这不是一个分页查询
		// 为了方便引入PageHelper分页插件
		// 在查询之前，只需要调用下面的方法,传入页码，以及每页的大小
		PageHelper.startPage(pn, 5);
		// startPage后面紧跟的这个查询就是一个分页查询
		List<Employee> emps = employeeService.getAll();
		// 使用PageInfo包装查询后的结果，只需要将PageInfo交给页面就行了
		// 其中封装了详细的分页信息，包括有我们查询出来的信息,传入连续显示的页数
		PageInfo page = new PageInfo(emps, 5);
		return Msg.success().add("pageInfo", page);
	}

	/**
	 * 查询员工数据（分页查询）
	 * 
	 * @return
	 */
	// @RequestMapping("/emps")
	public String getEmps(@RequestParam(value = "pn", defaultValue = "1") Integer pn, Model model) {
		// 这不是一个分页查询
		// 为了方便引入PageHelper分页插件
		// 在查询之前，只需要调用下面的方法,传入页码，以及每页的大小
		PageHelper.startPage(pn, 5);
		// startPage后面紧跟的这个查询就是一个分页查询
		List<Employee> emps = employeeService.getAll();
		// 使用PageInfo包装查询后的结果，只需要将PageInfo交给页面就行了
		// 其中封装了详细的分页信息，包括有我们查询出来的信息,传入连续显示的页数
		PageInfo page = new PageInfo(emps, 5);
		model.addAttribute("pageInfo", page);

		return "index";
	}

	/**
	 * 员工保存
	 * 1.支持JSR303校验
	 * 2.导入Hibernate-validator包
	 * @param employee
	 * @return
	 */
	@RequestMapping(value = "/emp", method = RequestMethod.POST)
	@ResponseBody
	public Msg saveEmp(@Valid Employee employee,BindingResult result) {
		if(result.hasErrors()) {
			//校验失败,应该返回失败,在模态框中显示失败的错误信息
			Map<String, Object> map = new HashMap<String, Object>();
			List<FieldError> errors = result.getFieldErrors();
			for (FieldError fieldError : errors) {
				System.out.println("错误的字段名:"+fieldError.getField());
				System.out.println("错误信息："+fieldError.getDefaultMessage());
				map.put(fieldError.getField(), fieldError.getDefaultMessage());
			}
			return Msg.fail().add("errorField", map);
		}else {
			employeeService.saveEmp(employee);
			return Msg.success();
		}
		
	}

	/**
	 * 检查用户名是否可用
	 * 
	 * @param empName
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/checkuser")
	public Msg checkuser(@RequestParam("empName")String empName) {
		// 先判断用户名是否是合法的
		String regx = "(^[a-zA-Z0-9_-]{6,16}$)|(^[\u2E80-\u9FFF]{2,5})";//(^[a-zA-Z0-9_-]{6,16}$)|(^[\u2E80-\u9FFF]{2,5})
		if (!empName.matches(regx)) {
			return Msg.fail().add("va_msg", "用户名需要是6-16位的英文数字组合或者2-5位的中文");
		}

		// 数据库用户名重复校验
		boolean b = employeeService.checkUser(empName);
		if (b) {
			return Msg.success();
		} else {
			return Msg.fail().add("va_msg", "用户名不可用");
		}
	}

}
