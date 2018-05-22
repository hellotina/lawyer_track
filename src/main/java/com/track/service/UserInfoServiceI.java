package com.track.service;
import java.util.List;

import com.track.entities.UserFavoriteRelation;
import com.track.entities.UserInfoEntity;
import com.track.entities.LawyerInfoEntity;
import com.track.entities.LawyerLocation;

import net.sf.json.JSONObject;

public interface UserInfoServiceI {
//	保存和修改用户信息
	public void save(UserInfoEntity user);
//	根据用户openid查询用户基本信息
	public UserInfoEntity findById(String openid);
//	根据用户openid查询律师基本信息
	public LawyerInfoEntity findLawyerById(String openid);
//	查询还未通过的认证信息
	public LawyerInfoEntity findAuthLawyerById(String openid);
//	根据用户openid查询律师Qrcode
	public String getQrcode(String openid,String status);
//	根据用户openid查询用户角色信息
	public JSONObject findRoleInfo(String openid);
//	保存律师办公地址
	public void saveAddress(JSONObject locInfo,String openid);
//	保存律师信息
	public void saveLawyer(LawyerInfoEntity lawyer);
//	律师认证状态查询
	public int getLawyerStatus(String openid);
//	查询律师主页内容
	public JSONObject getLawyerAllInfo(String openid);
//	查询还未通过认证的律师主页内容
	public JSONObject getAuthLawyerAllInfo(String openid);
//	删除未通过的认证信息
	public void delLawyerInfo(String openid);
//	获取用户收藏列表
	public List<UserFavoriteRelation> getFavoriteList(String openid);
//	查询距离
	public double distance(float lng,float lat,LawyerLocation location);
//	查询律师定位
	public LawyerLocation findLocation(String openid);
//	查看待处理认证数量
	public int getAuthNum();
//	查看待处理认证列表
	public List<LawyerInfoEntity> getAuthList();
//	搜索所有律师
	public List<LawyerInfoEntity> getAllLawyer();
//	按专长查询律师
	public List<LawyerInfoEntity> getLawyerByField(int fieldId);
//	按姓名查询律师
	public List<LawyerInfoEntity> getLawyerByName(String realname);
//	按地区查询律师
	public List<LawyerInfoEntity> getLawyerByRegion(String province,String city,String district);
//	组装律师所有信息
	public  List<JSONObject> getUserTotalInfo(List<LawyerInfoEntity> lawyers,float lat,float lng);
//	添加收藏
	public void saveFavor(UserFavoriteRelation relation);
//	删除收藏
	public void delFavor(String userid,String lawyerid);
//	查询收藏
	public boolean searchFavor(String userid,String lawyerid);
}                                                                         	
