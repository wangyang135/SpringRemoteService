package cn.wy.service.impl;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import cn.wy.domain.User;
import cn.wy.service.UserService;

@WebService
@SOAPBinding(style = Style.RPC)
public class UserServiceImpl implements UserService {

	@Override
	public User getUserByName(@WebParam(name = "name")String name) {
		
		User user = new User();
		user.setUserId(System.currentTimeMillis());
		user.setSex("M");
		user.setUserName(name);
		return user;
	}

	@Override
	public void setUser(User user) {
		System.out.println("############Server setUser###########");
		System.out.println("setUser:" + user);
	}

	
}
