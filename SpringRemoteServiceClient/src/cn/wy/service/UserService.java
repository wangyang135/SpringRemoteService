package cn.wy.service;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import cn.wy.domain.User;

@WebService
@SOAPBinding(style = Style.RPC)
public interface UserService {

	public User getUserByName(@WebParam(name ="name")String name);

	public void setUser(User user);
}
