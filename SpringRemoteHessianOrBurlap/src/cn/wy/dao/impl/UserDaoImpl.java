package cn.wy.dao.impl;

import java.util.ArrayList;
import java.util.List;

import cn.wy.dao.UserDao;
import cn.wy.domain.User;


public class UserDaoImpl implements UserDao {

	public static List<User> userList = new ArrayList<User>();
	
	// 构造一部分数据，在实际的项目中数据是从数据库中获取的
	static{
		for (long i = 0; i < 10; i++) {
			User user = new User(i, "wy" + i);
			userList.add(user);
		}
	}
	
	@Override
	public List<User> selectUsersList() {
		return userList;
	}
}
