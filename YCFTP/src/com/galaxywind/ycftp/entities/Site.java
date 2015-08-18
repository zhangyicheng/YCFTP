package com.galaxywind.ycftp.entities;

import java.io.Serializable;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

//建议加上注解， 混淆后表名不受影响
@Table(name = "site")
public class Site extends EntityBase implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(column = "name")
	public String name;
	
	@Column(column = "host")
	public String host;
	
	@Column(column = "port")
	public String port;
	
	@Column(column = "user")
	public String user;
	
	@Column(column = "passWord")
	public String passWord;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(name!=null){
			this.name = name;
		}else{
			this.name = "null";
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		if(host!=null){
			this.host = host;
		}else{
			this.host = "null";
		}
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		if(port!=null){
			this.port = port;
		}else{
			this.port = "null";
		}
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		if(user!=null){
			this.user = user;
		}else{
			this.user = "null";
		}
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		if(passWord!=null){
			this.passWord = passWord;
		}else{
			this.passWord = "null";
		}
	}

	@Override
	public String toString() {
		return "Site [name=" + name + ", host=" + host + ", port=" + port
				+ ", user=" + user + ", passWord=" + passWord + "]";
	}
}
