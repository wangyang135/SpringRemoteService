package cn.wy.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wy.domain.User;
import cn.wy.service.UserService;

public class MainTest {

	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"applicationContext.xml");
		UserService service = ctx.getBean("userWsClient", UserService.class);
		System.out.println(">>>>>>><<<<<<<<<<");
		User user = service.getUserByName("wy");
		System.out.println(user);
		
		user.setSex("test");
		service.setUser(user);
		System.out.println("========================="+user.getUserName());
	}
}
