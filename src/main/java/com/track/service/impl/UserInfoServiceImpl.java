package com.track.service.impl;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.track.dao.ArticleInfoDao;
import com.track.dao.FieldDao;
import com.track.dao.UserInfoDao;
import com.track.entities.FieldInfoEntity;
import com.track.entities.LawyerFieldRelation;
import com.track.entities.LawyerInfoEntity;
import com.track.entities.LawyerLocation;
import com.track.entities.UserFavoriteRelation;
import com.track.entities.UserInfoEntity;
import com.track.service.UserInfoServiceI;
import com.track.util.HttpRequestUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("userInfoService")
@Transactional
public class UserInfoServiceImpl implements UserInfoServiceI{
	@Autowired
	private UserInfoDao userInfoDao;
	@Autowired
	private FieldDao fieldDao;
	@Autowired
	private ArticleInfoDao articleInfoDao;
	
//	保存和修改用户信息
	public void save(UserInfoEntity user){
		userInfoDao.save(user);
	}
//	根据用户openid查询用户基本信息
	public UserInfoEntity findById(String openid){
		return userInfoDao.findById(openid);
	}
//	根据用户openid查询律师基本信息
	public LawyerInfoEntity findLawyerById(String openid){
		return userInfoDao.findLawyerById(openid);
	}
//	查询还未通过的认证信息
	public LawyerInfoEntity findAuthLawyerById(String openid){
		return userInfoDao.findAuthLawyerById(openid);
	}
//	根据用户openid查询律师Qrcode
	public String getQrcode(String openid,String status){
		return userInfoDao.getQrcode(openid,status);
	}
//	根据用户openid查询用户角色信息
	public JSONObject findRoleInfo(String openid){
		UserInfoEntity user = userInfoDao.findById(openid);
		JSONObject result=new JSONObject();
		if(user.getRole() == 2){
			result.put("lawyer", userInfoDao.findLawyerById(openid));
		}
		result.put("user", user);
		result.put("role", user.getRole());
		return result;
	}
	
//	保存律师办公地址
	public void saveAddress(JSONObject locInfo,String openid){
		LawyerLocation location = new LawyerLocation();
		location.setCity(locInfo.getString("city"));
		location.setDistrict(locInfo.getString("district"));
		location.setProvince(locInfo.getString("province"));
		location.setLat((float)locInfo.getDouble("lat"));
		location.setLng((float)locInfo.getDouble("lng"));
		location.setLawyerId(openid);
		userInfoDao.saveAddress(location);
	}
	
//	保存律师信息
	public void saveLawyer(LawyerInfoEntity lawyer){
		userInfoDao.saveLawyer(lawyer);
	}
	
//	律师认证状态查询
	public int getLawyerStatus(String openid){
		return userInfoDao.getLawyerStatus(openid);
	}
//	查询律师主页内容
	public JSONObject getLawyerAllInfo(String openid){
		JSONObject result = new JSONObject();
		result.put("lawyer", userInfoDao.findLawyerById(openid));
		result.put("field", fieldDao.findLawyerField(openid));
		result.put("article", articleInfoDao.findLawyerArticle(openid));
		return result;
	}
//	查询还未通过认证的律师主页内容
	public JSONObject getAuthLawyerAllInfo(String openid){
		JSONObject result = new JSONObject();
		result.put("lawyer", userInfoDao.findAuthLawyerById(openid));
		result.put("field", fieldDao.findLawyerField(openid));
		result.put("article", articleInfoDao.findLawyerArticle(openid));
		return result;
	}
//	删除未通过的认证信息
	public void delLawyerInfo(String openid){
		userInfoDao.delLawyerInfo(openid);
	}
//	获取用户收藏列表
	public List<UserFavoriteRelation> getFavoriteList(String openid){
		return userInfoDao.getFavoriteList(openid);
	}
//	查询距离
	public double distance(float lng,float lat,LawyerLocation location){
		  String GetDistanceUrl = "http://apis.map.qq.com/ws/distance/v1/";
		  String distancePara = "mode=walking&key=MKMBZ-EPIKK-GZ3JA-ALXD6-BZ5VZ-BOBBG&";
		  String fromStr = "from="+lat+","+lng;
		  String toStr = "to="+location.getLat()+","+location.getLng();
		  distancePara +=fromStr+"&"+toStr;
		  String disResult=HttpRequestUtil.sendGet(GetDistanceUrl,distancePara);  
		  System.out.println(GetDistanceUrl+"?"+distancePara);
		  JSONObject disResponseJson = JSONObject.fromObject(disResult);
		  double value = disResponseJson.getJSONObject("result").getJSONArray("elements").getJSONObject(0).getDouble("distance")/1000;
		  //保留两位小数
		  BigDecimal bg = new BigDecimal(value).setScale(2, RoundingMode.UP);
		  return bg.doubleValue();
	}
//	查询律师定位
	public LawyerLocation findLocation(String openid){
		return userInfoDao.findLocation(openid);
	}
//	查看待处理认证数量
	public int getAuthNum(){
		return userInfoDao.getAuthNum();
	}	
	
//	查看待处理认证列表
	public List<LawyerInfoEntity> getAuthList(){
		return userInfoDao.getAuthList();
	}
	
//	搜索所有律师
	public List<LawyerInfoEntity> getAllLawyer(){
		return userInfoDao.getAllLawyer();
	}
	
//	按专长查询律师
	public List<LawyerInfoEntity> getLawyerByField(int fieldId){
		return userInfoDao.getLawyerByField(fieldId);
	}
	
//	按姓名查询律师
	public List<LawyerInfoEntity> getLawyerByName(String realname){
		return userInfoDao.getLawyerByName(realname);
	}
	
//	按地区查询律师
	public List<LawyerInfoEntity> getLawyerByRegion(String province,String city,String district){
		return userInfoDao.getLawyerByRegion(province, city, district);
	}
	
//	组装律师所有信息
	public  List<JSONObject> getUserTotalInfo(List<LawyerInfoEntity> lawyers,float lat,float lng){
		//计算距离请求地址
		String GetDistanceUrl = "http://apis.map.qq.com/ws/distance/v1/";
		String distancePara = "mode=walking&key=MKMBZ-EPIKK-GZ3JA-ALXD6-BZ5VZ-BOBBG&";
		//起点参数
		String fromStr = "from="+lat+","+lng;
		//终点参数
		String toStr="to=";
		for(LawyerInfoEntity item : lawyers){
			String lawyerid = item.getOpenid();
			LawyerLocation location = findLocation(lawyerid);
			String temp = location.getLat()+","+location.getLng();
			toStr += temp+";";
		}
		//组装参数
		distancePara +=fromStr+"&"+toStr;
		//去掉参数最后一个分号
		distancePara = distancePara.substring(0,distancePara.length() - 1);
		//请求计算距离
		String disResult=HttpRequestUtil.sendGet(GetDistanceUrl,distancePara);
		//获取请求结果
		JSONObject disResponseJson = JSONObject.fromObject(disResult);
		JSONArray data = disResponseJson.getJSONObject("result").getJSONArray("elements");
		
		//组装最后的结果
		 List<JSONObject> result =new ArrayList<JSONObject>();
		 if(data.size()>0){  
			 for(int i=0;i<data.size();i++){  
				 //距离精确到小数点后两位
				  JSONObject job = data.getJSONObject(i);
				  double distance = job.getDouble("distance")/1000;
				  BigDecimal bg = new BigDecimal(distance).setScale(2, RoundingMode.UP);
				  //获取专长列表
				  List<FieldInfoEntity> fieldList = fieldDao.findLawyerField(lawyers.get(i).getOpenid());
				  //获取律师定位
				  LawyerLocation location = findLocation(lawyers.get(i).getOpenid());
				  //组合数据
				  JSONObject temp = new JSONObject();
				  temp.put("lawyer",lawyers.get(i));
				  temp.put("field",fieldList);
				  temp.put("location", location);
				  temp.put("distance",bg.doubleValue());
				  result.add(temp);
			  }  
		  }
		 return result;
	}
	
//	添加收藏
	public void saveFavor(UserFavoriteRelation relation){
		userInfoDao.saveFavor(relation);
	}
//	删除收藏
	public void delFavor(String userid,String lawyerid){
		userInfoDao.delFavor(userid, lawyerid);
	}
//	查询收藏
	public boolean searchFavor(String userid,String lawyerid){
		int i=userInfoDao.searchFavor(userid, lawyerid);
		if(i==1){
			return true;
		}
		else{
			return false;
		}
	}
}
