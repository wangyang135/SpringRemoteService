# SpringRemoteService
远程调用，包括RMI、Hessian、Burlap、HttpInvoker、JAX-WS等方式与Spring进行整合实现远程调用。为分布式应用提供访问的接口与服务。

##博客地址：
[hyperlink syntax](http://blog.csdn.net/wangyang1354/article/details/52048643)


##RMI

 RMI（即Remote Method Invoke 远程方法调用）。在Java中，只要一个类extends了java.rmi.Remote接口，即可成为存在于服务器端的远程对象，供客户端访问并提供一定的服务。JavaDoc描述：Remote 接口用于标识其方法可以从非本地虚拟机上调用的接口。任何远程对象都必须直接或间接实现此接口。只有在“远程接口”（扩展java.rmi.Remote 的接口）中指定的这些方法才可远程使用。
 它将调用的方法绑定到服务端的定义好的接口上，对于防火墙的穿透力明显不够的。
  
RMIServiceExporter将POJO包装到服务适配器中，并将服务适配器绑定到RMI注册表中，从而将POJO转换为RMI服务。
示例：
服务端

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

    public class User implements Serializable {
    
    	private static final long serialVersionUID = 1L;
    
    	private Long userId;
    	
    	private String userName;
    
    	public User() {
    		super();
    	}
    
    	public User(Long userId, String userName) {
    		super();
    		this.userId = userId;
    		this.userName = userName;
    	}
    
    	public Long getUserId() {
    		return userId;
    	}
    
    	public void setUserId(Long userId) {
    		this.userId = userId;
    	}
    
    	public String getUserName() {
    		return userName;
    	}
    
    	public void setUserName(String userName) {
    		this.userName = userName;
    	}
    	
    	
    }

    public class UserServiceImpl implements UserService {
    
    	@Resource
    	private UserDao userDao;
    	
    	@Override
    	public List<User> getUserList() {
    		List<User> userList = userDao.selectUsersList();
    		return userList;
    	}
    	
    }
###服务端
Test，运行Test将POJO绑定到定义的端口上去。
    public class Test {
    	public static void main(String[] args) {
    		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
    	}
    }
ApplicationContext.xml
    <beans xmlns="http://www.springframework.org/schema/beans"
    	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
    	xmlns:context="http://www.springframework.org/schema/context"
    	xmlns:aop="http://www.springframework.org/schema/aop" xmlns:tx="http://www.springframework.org/schema/tx"
    	xsi:schemaLocation="http://www.springframework.org/schema/beans 
    		http://www.springframework.org/schema/beans/spring-beans-3.2.xsd 
    		http://www.springframework.org/schema/mvc 
    		http://www.springframework.org/schema/mvc/spring-mvc-3.2.xsd 
    		http://www.springframework.org/schema/context 
    		http://www.springframework.org/schema/context/spring-context-3.2.xsd 
    		http://www.springframework.org/schema/aop 
    		http://www.springframework.org/schema/aop/spring-aop-3.2.xsd 
    		http://www.springframework.org/schema/tx 
    		http://www.springframework.org/schema/tx/spring-tx-3.2.xsd ">
    		
    		<bean id = "userDao" class = "cn.wy.dao.impl.UserDaoImpl"/>
    		
    		<bean id = "userService" class = "cn.wy.service.impl.UserServiceImpl"></bean>
    		
    		<bean class = "org.springframework.remoting.rmi.RmiServiceExporter">
    			<property name="service" ref="userService"></property>
    			<property name="serviceName" value = "userServiceRemote"></property>
    			<property name="serviceInterface" value="cn.wy.service.UserService"></property>
    			<property name="registryPort" value="1992"></property>
    		</bean>
    		
    		<context:annotation-config></context:annotation-config>
    </beans>
    

###接口调用客户端

 
RMIProxyFactoryBean生成代理对象，该对象代表客户端来负责与远程的RMI服务进行通信，客户端通过服务的接口与代理进行交互，如同远程服务就是一个本地的POJO.
ApplicationContext.xml
    <beans xmlns="http://www.springframework.org/schema/beans"
    	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
    	xmlns:context="http://www.springframework.org/schema/context"
    	xmlns:tx="http://www.springframework.org/schema/tx"
    	xsi:schemaLocation="http://www.springframework.org/schema/beans 
    		http://www.springframework.org/schema/beans/spring-beans-3.2.xsd 
    		http://www.springframework.org/schema/mvc 
    		http://www.springframework.org/schema/mvc/spring-mvc-3.2.xsd 
    		http://www.springframework.org/schema/context 
    		http://www.springframework.org/schema/context/spring-context-3.2.xsd 
    		http://www.springframework.org/schema/tx 
    		http://www.springframework.org/schema/tx/spring-tx-3.2.xsd ">
    		
    		<!-- <bean id = "userService" class="cn.wy.service.impl.UserServiceImpl"></bean> -->
    		
    		<bean name = "userServiceRemote" class = "org.springframework.remoting.rmi.RmiProxyFactoryBean">
    			<property name="serviceUrl" value = "rmi://127.0.0.1:1992/userServiceRemote"></property>
    			<property name="serviceInterface" value = "cn.wy.service.UserService"></property>
    		</bean>
    		
    		<context:annotation-config></context:annotation-config>
    </beans>
测试类
    public class Test {
    
    	public static void main(String[] args) {
    		
    		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
    		UserService userService = (UserService) context.getBean("userServiceRemote");
    		List<User> userList = userService.getUserList();
    		for (User user : userList) {
    			System.out.println(user);
    		}
    	}
    }



##Hessian

Hessian是一个轻量级的remoting onhttp工具，使用简单的方法提供了RMI的功能。 相比WebService，Hessian更简单、快捷。采用的是二进制RPC协议，因为采用的是二进制协议，所以它很适合于发送二进制数据。具有较好的穿墙能力。
在进行基于Hessian的项目开发时，应当注意以下几点：
▲JAVA服务器端必须具备以下几点：
·包含Hessian的jar包
·设计一个接口，用来给客户端调用
·实现该接口的功能
·配置web.xml，配好相应的servlet
·由于使用二进制RPC协议传输数据，对象必须进行序列化，实现Serializable 接口
·对于复杂对象可以使用Map的方法传递
▲客户端必须具备以下几点：
·java客户端包含Hessian.jar的包。
·具有和服务器端结构一样的接口。包括命名空间都最好一样
·利用HessianProxyFactory调用远程接口。
示例：
###服务端

User（set、get方法已经省去，这部分的类和RMI中基本上一致）
    public class User implements Serializable {
    	private static final long serialVersionUID = 1L;
    	private Long userId;
    	private String userName;
    }
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
UserServiceImpl
    public class UserServiceImpl implements UserService {
    
    	@Resource
    	private UserDao userDao;
    	
    	@Override
    	public List<User> getUserList() {
    		List<User> userList = userDao.selectUsersList();
    		return userList;
    	}
    	
    }
ApplicationContext.xml
    <beans xmlns="http://www.springframework.org/schema/beans"
    	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
    	xmlns:context="http://www.springframework.org/schema/context"
    	xmlns:aop="http://www.springframework.org/schema/aop" xmlns:tx="http://www.springframework.org/schema/tx"
    	xsi:schemaLocation="http://www.springframework.org/schema/beans 
    		http://www.springframework.org/schema/beans/spring-beans-3.2.xsd 
    		http://www.springframework.org/schema/mvc 
    		http://www.springframework.org/schema/mvc/spring-mvc-3.2.xsd 
    		http://www.springframework.org/schema/context 
    		http://www.springframework.org/schema/context/spring-context-3.2.xsd 
    		http://www.springframework.org/schema/aop 
    		http://www.springframework.org/schema/aop/spring-aop-3.2.xsd 
    		http://www.springframework.org/schema/tx 
    		http://www.springframework.org/schema/tx/spring-tx-3.2.xsd ">
    		
    		<bean id = "userDao" class = "cn.wy.dao.impl.UserDaoImpl"/>
    		
    		<bean id = "userService" class = "cn.wy.service.impl.UserServiceImpl"></bean>
    		
    		<bean id = "hessianUserService" class = "org.springframework.remoting.caucho.HessianServiceExporter">
    			<property name="service" ref="userService"></property>
    			<property name="serviceInterface" value="cn.wy.service.UserService"></property>
    		</bean>
    		
    		<bean id="urlMapping" class = "org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
    			<property name="mappings">
    				<value>
    				/userService.service=hessianUserService
    				</value>
    			</property>
    		</bean>
    		<context:annotation-config></context:annotation-config>
    </beans>
Web.xml
    <?xml version="1.0" encoding="UTF-8"?>
    <web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://java.sun.com/xml/ns/javaee" xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd" version="3.0">
      <display-name></display-name>
      <welcome-file-list>
    <welcome-file>index.jsp</welcome-file>
      </welcome-file-list>
      <servlet>
    <servlet-name>userService</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:applicationContext.xml</param-value>
    </init-param>
      </servlet>
      <servlet-mapping>
    <servlet-name>userService</servlet-name>
    <url-pattern>*.service</url-pattern>
      </servlet-mapping>
    </web-app>


###调用端

ApplicationContext.xml
    <beans xmlns="http://www.springframework.org/schema/beans"
    	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
    	xmlns:context="http://www.springframework.org/schema/context"
    	xmlns:tx="http://www.springframework.org/schema/tx"
    	xsi:schemaLocation="http://www.springframework.org/schema/beans 
    		http://www.springframework.org/schema/beans/spring-beans-3.2.xsd 
    		http://www.springframework.org/schema/mvc 
    		http://www.springframework.org/schema/mvc/spring-mvc-3.2.xsd 
    		http://www.springframework.org/schema/context 
    		http://www.springframework.org/schema/context/spring-context-3.2.xsd 
    		http://www.springframework.org/schema/tx 
    		http://www.springframework.org/schema/tx/spring-tx-3.2.xsd ">
    		
    		<!-- <bean id = "userService" class="cn.wy.service.impl.UserServiceImpl"></bean> -->
    		
    		<bean name = "userServiceRemote" class = "org.springframework.remoting.caucho.HessianProxyFactoryBean">
    			<!-- 链接根据项目的名称以及自己定义的服务名称进行设置  -->
    			<property name="serviceUrl" value = "http://127.0.0.1:8080/SpringRemote/userService.service"></property>
    			<property name="serviceInterface" value = "cn.wy.service.UserService"></property>
    		</bean>
    		<context:annotation-config></context:annotation-config>
    </beans>

测试类
public class Test {

	public static void main(String[] args) {
		
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		System.out.println("====HessianTest->====");
		UserService userService = (UserService) context.getBean("userServiceRemote");
		List<User> userList = userService.getUserList();
		for (User user : userList) {
			System.out.println(user);
		}
	}
}


##Burlap

Burlap是一种基于XML远程调用技术，但与其他基于XML的远程技术（例如SOAP或者XML-RPC）不同，Burlap的消息结构尽可能的简单，不需要额外的外部定义语言（例如WSDL或IDL）。
Burlap和Hessian很大程度上，它们是一样的，唯一的区别在于Hessian的消息是二进制的，而Burlap的消息是XML。（Burlap和Hessian代码实现上也很相似）.下面的例子和Hessian的例子我将他们放在了同一个项目中去实现，所以类是一致的只是其在Spring中的导出器和Bean工厂有所区别。为了避免混淆，我还是拆开来列举。
###服务端

User（set、get方法已经省去，这部分的类和RMI中基本上一致）
    public class User implements Serializable {
    	private static final long serialVersionUID = 1L;
    	private Long userId;	
    	private String userName;
    }
UserServiceImpl
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
测试类
    public class Test {
    	public static void main(String[] args) {
    		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
    	}
    }
ApplicationContext.xml
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:aop="http://www.springframework.org/schema/aop" xmlns:tx="http://www.springframework.org/schema/tx"
	xsi:schemaLocation="http://www.springframework.org/schema/beans 
		http://www.springframework.org/schema/beans/spring-beans-3.2.xsd 
		http://www.springframework.org/schema/mvc 
		http://www.springframework.org/schema/mvc/spring-mvc-3.2.xsd 
		http://www.springframework.org/schema/context 
		http://www.springframework.org/schema/context/spring-context-3.2.xsd 
		http://www.springframework.org/schema/aop 
		http://www.springframework.org/schema/aop/spring-aop-3.2.xsd 
		http://www.springframework.org/schema/tx 
		http://www.springframework.org/schema/tx/spring-tx-3.2.xsd ">
		
		<bean id = "userDao" class = "cn.wy.dao.impl.UserDaoImpl"/>
		
		<bean id = "userService" class = "cn.wy.service.impl.UserServiceImpl"></bean>
		
		<bean id="urlMapping" class = "org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
			<property name="mappings">
				<value>
				/userServiceBurlap.service=burlapUserService
				</value>
			</property>
		</bean>
		
		<bean id = "burlapUserService" class = "org.springframework.remoting.caucho.BurlapServiceExporter">
			<property name="service" ref="userService"></property>
			<property name="serviceInterface" value="cn.wy.service.UserService"></property>
		</bean>
		
		<context:annotation-config></context:annotation-config>
</beans>

Web.xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://java.sun.com/xml/ns/javaee" xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd" version="3.0">
  <display-name></display-name>
  <welcome-file-list>
<welcome-file>index.jsp</welcome-file>
  </welcome-file-list>
  <servlet>
<servlet-name>userService</servlet-name>
<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
<init-param>
  <param-name>contextConfigLocation</param-name>
  <param-value>classpath:applicationContext.xml</param-value>
</init-param>
  </servlet>
  <servlet-mapping>
<servlet-name>userService</servlet-name>
<url-pattern>*.service</url-pattern>
  </servlet-mapping>
</web-app>


###客户端

ApplicationContext.xml
    <beans xmlns="http://www.springframework.org/schema/beans"
    	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
    	xmlns:context="http://www.springframework.org/schema/context"
    	xmlns:tx="http://www.springframework.org/schema/tx"
    	xsi:schemaLocation="http://www.springframework.org/schema/beans 
    		http://www.springframework.org/schema/beans/spring-beans-3.2.xsd 
    		http://www.springframework.org/schema/mvc 
    		http://www.springframework.org/schema/mvc/spring-mvc-3.2.xsd 
    		http://www.springframework.org/schema/context 
    		http://www.springframework.org/schema/context/spring-context-3.2.xsd 
    		http://www.springframework.org/schema/tx 
    		http://www.springframework.org/schema/tx/spring-tx-3.2.xsd ">
    		
    		<!-- <bean id = "userService" class="cn.wy.service.impl.UserServiceImpl"></bean> -->
    		
    		<bean name = "userServiceBurlap" class = "org.springframework.remoting.caucho.BurlapProxyFactoryBean">
    			<property name="serviceUrl" value = "http://127.0.0.1:8080/SpringRemote/userServiceBurlap.service"></property>
    			<property name="serviceInterface" value = "cn.wy.service.UserService"></property>
    		</bean>
    		
    		<context:annotation-config></context:annotation-config>
    </beans>
测试类
    public class Test {
    
    	public static void main(String[] args) {
    		
    		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
    		System.out.println("====BurlapTest->====");
    		userService = (UserService) context.getBean("userServiceBurlap");
    		userList = userService.getUserList();
    		for (User user : userList) {
    			System.out.println(user);
    		}
    	}
    }


###Hessian与Burlap

 
HessianProxyFactoryBean 和BurlapProxyFactoryBean生成代理对象负责通过HTTP与远程对象通信。其中Hessian为二进制、而Burlap为xml文件形式。


##HttpInvoker

Spring HTTP invoker 是 spring 框架中的一个远程调用模型，执行基于 HTTP 的远程调用（意味着可以通过防火墙），并使用 java 的序列化机制在网络间传递对象。客户端可以很轻松的像调用本地对象一样调用远程服务器上的对象，这有点类似于 webservice ，但又不同于 webservice ，区别如下：
webservice
HTTP invoker
跨平台，跨语言
只支持 java 语言
支持 SOAP ，提供 wsdl
不支持
结构庞大，依赖特定的 webservice 实现，如 xfire等
结构简单，只依赖于 spring 框架本身


示例：
###服务端

User：
    public class User implements Serializable {
    	private static final long serialVersionUID = 1L;
    	private Long userId;
    	private String userName;
    }
UserDaoImpl
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
UserServiceImpl
    public class UserServiceImpl implements UserService {
    
    	@Resource
    	private UserDao userDao;
    	
    	@Override
    	public List<User> getUserList() {
    		List<User> userList = userDao.selectUsersList();
    		return userList;
    	}
    	
    }

ApplicationContext.xml
    <beans xmlns="http://www.springframework.org/schema/beans"
    	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
    	xmlns:context="http://www.springframework.org/schema/context"
    	xmlns:aop="http://www.springframework.org/schema/aop" xmlns:tx="http://www.springframework.org/schema/tx"
    	xsi:schemaLocation="http://www.springframework.org/schema/beans 
    		http://www.springframework.org/schema/beans/spring-beans-3.2.xsd 
    		http://www.springframework.org/schema/mvc 
    		http://www.springframework.org/schema/mvc/spring-mvc-3.2.xsd 
    		http://www.springframework.org/schema/context 
    		http://www.springframework.org/schema/context/spring-context-3.2.xsd 
    		http://www.springframework.org/schema/aop 
    		http://www.springframework.org/schema/aop/spring-aop-3.2.xsd 
    		http://www.springframework.org/schema/tx 
    		http://www.springframework.org/schema/tx/spring-tx-3.2.xsd ">
    		
    		<bean id = "userDao" class = "cn.wy.dao.impl.UserDaoImpl"/>
    		
    		<bean id = "userService" class = "cn.wy.service.impl.UserServiceImpl"></bean>
    		
    		<bean id = "httpInvokerService" class = "org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter">
    			<property name="service" ref="userService"></property>
    			<property name="serviceInterface" value="cn.wy.service.UserService"></property>
    		</bean>
    		
    		<bean id = "urlMapping" class = "org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
    			<property name="mappings">
    				<value>
    					/userService.service=httpInvokerService
    				</value>
    			</property>
    		</bean>
    		
    		<context:annotation-config></context:annotation-config>
    </beans>

Web.xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://java.sun.com/xml/ns/javaee" xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd" version="3.0">
  <display-name></display-name>
  <welcome-file-list>
<welcome-file>index.jsp</welcome-file>
  </welcome-file-list>
  <servlet>
<servlet-name>userService</servlet-name>
<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
<init-param>
  <param-name>contextConfigLocation</param-name>
  <param-value>classpath:applicationContext.xml</param-value>
</init-param>
  </servlet>
  <servlet-mapping>
<servlet-name>userService</servlet-name>
<url-pattern>*.service</url-pattern>
  </servlet-mapping>
  
</web-app>


###客户端

ApplicationContext.xml
    <beans xmlns="http://www.springframework.org/schema/beans"
    	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
    	xmlns:context="http://www.springframework.org/schema/context"
    	xmlns:tx="http://www.springframework.org/schema/tx"
    	xsi:schemaLocation="http://www.springframework.org/schema/beans 
    		http://www.springframework.org/schema/beans/spring-beans-3.2.xsd 
    		http://www.springframework.org/schema/mvc 
    		http://www.springframework.org/schema/mvc/spring-mvc-3.2.xsd 
    		http://www.springframework.org/schema/context 
    		http://www.springframework.org/schema/context/spring-context-3.2.xsd 
    		http://www.springframework.org/schema/tx 
    		http://www.springframework.org/schema/tx/spring-tx-3.2.xsd ">
    		
    		<!-- <bean id = "userService" class="cn.wy.service.impl.UserServiceImpl"></bean> -->
    		
    		<bean name = "userServiceRemote" class = "org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean">
    			<property name="serviceUrl" value = "http://127.0.0.1:8080/SpringRemote/userService.service"></property>
    			<property name="serviceInterface" value = "cn.wy.service.UserService"></property>
    		</bean>
    		
    		<context:annotation-config></context:annotation-config>
    </beans>

Test测试类
    public class Test {
    
    	public static void main(String[] args) {
    		
    		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
    		UserService userService = (UserService) context.getBean("userServiceRemote");
    		List<User> userList = userService.getUserList();
    		for (User user : userList) {
    			System.out.println(user);
    		}
    	}
    }
 


##Jax-WS

JAX-WS规范是一组XML web services的JAVA API，JAX-WS允许开发者可以选择RPC-oriented或者message-oriented 来实现自己的web services。
在 JAX-WS中，一个远程调用可以转换为一个基于XML的协议例如SOAP，在使用JAX-WS过程中，开发者不需要编写任何生成和处理SOAP消息的代码。JAX-WS的运行时实现会将这些API的调用转换成为对应的SOAP消息。
在服务器端，用户只需要通过Java语言定义远程调用所需要实现的接口SEI（service endpoint interface），并提供相关的实现，通过调用JAX-WS的服务发布接口就可以将其发布为WebService接口。
在客户端，用户可以通过JAX-WS的API创建一个代理（用本地对象来替代远程的服务）来实现对于远程服务器端的调用。
当然 JAX-WS 也提供了一组针对底层消息进行操作的API调用，你可以通过Dispatch 直接使用SOAP消息或XML消息发送请求或者使用Provider处理SOAP或XML消息。
通过web service所提供的互操作环境，我们可以用JAX-WS轻松实现JAVA平台与其他编程环境（.net等）的互操作。
由于经验的原因，想搭建原生的JAX-WS程序都是无法得到最终的结果，不过已经到了可以访问出来WEB服务的页面了，但是WEB服务的WSDL文件无法获得到。查看网上的诸多教程，推荐借助CXF实现Jax-WS。

示例：
###服务端


User
    public class User {
    	private Long userId;
    	private String userName;
    	private String sex;
    }
UserService
    @WebService
    @SOAPBinding(style = Style.RPC)
    public interface UserService {
    
    	public User getUserByName(@WebParam(name ="name")String name);
    
    	public void setUser(User user);
    }

UserServiceImpl
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

ApplicationContext.xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:jaxws="http://cxf.apache.org/jaxws"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context-3.1.xsd
	http://cxf.apache.org/jaxws   
	http://cxf.apache.org/schemas/jaxws.xsd">
	<import resource="classpath*:META-INF/cxf/cxf.xml"/>
	<import resource="classpath*:META-INF/cxf/cxf-extension-soap.xml"/>
	<import resource="classpath*:META-INF/cxf/cxf-servlet.xml"/>
	
	<bean id="userServiceBean" class="cn.wy.service.impl.UserServiceImpl"/> 
	
	<bean id="outLoggingInterceptor" class="org.apache.cxf.interceptor.LoggingOutInterceptor"/>
	<!-- 注意下面的address，这里的address的名称就是访问的WebService的name -->
	<jaxws:server id="userService" serviceClass="cn.wy.service.UserService" address="/Users">
		<jaxws:serviceBean>
			<!-- 要暴露的 bean 的引用 -->
			<ref bean="userServiceBean"/>
		</jaxws:serviceBean>
		<jaxws:outInterceptors>
			<ref bean="outLoggingInterceptor"/>
		</jaxws:outInterceptors>
	</jaxws:server>
	
	<jaxws:client id="userWsClient" serviceClass="cn.wy.service.UserService" address="http://localhost:8080/SpringRemoteService/Users"/>
</beans>

Web.xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="3.0" 
	xmlns="http://java.sun.com/xml/ns/javaee" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee 
	http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd">
  <display-name></display-name>	
  <welcome-file-list>
<welcome-file>index.jsp</welcome-file>
  </welcome-file-list>
  
  <!-- 加载Spring容器配置 -->
	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener><!-- 设置Spring容器加载配置文件路径 -->
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>classpath*:applicationContext.xml</param-value>
	</context-param>
	
	<listener>
		<listener-class>org.springframework.web.util.IntrospectorCleanupListener</listener-class>
	</listener> 
	<servlet>
		<servlet-name>CXFService</servlet-name>
		<servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
	</servlet> 
	<servlet-mapping>
		<servlet-name>CXFService</servlet-name>
		<url-pattern>/*</url-pattern>
	</servlet-mapping>
	
</web-app>

###客户端


User
    public class User {
    	private Long userId;
    	private String userName;	
    	private String sex;
    }

UserService
    @WebService
    @SOAPBinding(style = Style.RPC)
    public interface UserService {
    
    	public User getUserByName(@WebParam(name ="name")String name);
    
    	public void setUser(User user);
    }
测试类
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
ApplicationContext.xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:jaxws="http://cxf.apache.org/jaxws"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context-3.1.xsd
	http://cxf.apache.org/jaxws   
	http://cxf.apache.org/schemas/jaxws.xsd">
	<import resource="classpath*:META-INF/cxf/cxf.xml"/>
	<import resource="classpath*:META-INF/cxf/cxf-extension-soap.xml"/>
	<import resource="classpath*:META-INF/cxf/cxf-servlet.xml"/>
	
	<jaxws:client id="userWsClient" 
		serviceClass="cn.wy.service.UserService" 
		address="http://localhost:8080/SpringRemoteService/Users"/>
</beans>

注意：上面的ApplicationContext.xml中Meta-INF/cxf/*.xml这几个xml文件，我这里没有添加，程序是可以正常运行的。