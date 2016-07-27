package cn.wy.service.impl;

import java.util.List;

import javax.annotation.Resource;

import cn.wy.dao.UserDao;
import cn.wy.domain.User;
import cn.wy.service.UserService;

public class UserServiceImpl implements UserService {

	@Resource
	private UserDao userDao;
	
	@Override
	public List<User> getUserList() {
		List<User> userList = userDao.selectUsersList();
		return userList;
	}
	
}
