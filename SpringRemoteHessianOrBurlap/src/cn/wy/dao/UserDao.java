package cn.wy.dao;

import java.util.List;

import cn.wy.domain.User;

public interface UserDao {

	public List<User> selectUsersList();
}
