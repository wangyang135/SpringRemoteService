package cn.wy.test;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wy.domain.User;
import cn.wy.service.UserService;


public class Test {

	public static void main(String[] args) {
		
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		System.out.println("====HessianTest->====");
		UserService userService = (UserService) context.getBean("userServiceRemote");
		List<User> userList = userService.getUserList();
		for (User user : userList) {
			System.out.println(user);
		}
		System.out.println("====BurlapTest->====");
		userService = (UserService) context.getBean("userServiceBurlap");
		userList = userService.getUserList();
		for (User user : userList) {
			System.out.println(user);
		}
	}
}
