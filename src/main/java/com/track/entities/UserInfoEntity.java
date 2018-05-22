package com.track.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;
import javax.persistence.Id;

@Entity
@Table(name = "user_info")
public class UserInfoEntity {
//	@GeneratedValue
	@Id
	private String openid;
	private String nickname;
	private String avatarUrl;
	private int role;

    
    public String getNickname(){
    	return nickname;
    }
    public void setNickname(String nickname){
    	this.nickname = nickname;
    }
 
    
    public String getOpenid(){
    	return openid;
    }
    public void setOpenid(String openid){
    	this.openid=openid;
    }
    
    public String getAvatarUrl(){
    	return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl){
    	this.avatarUrl=avatarUrl;
    }
  
    public int getRole(){
    	return role;
    }
    public void setRole(int role){
    	this.role=role;
    }
}
