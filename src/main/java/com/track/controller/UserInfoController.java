package com.track.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.track.common.ApiResult;
import com.track.entities.ArticleInfoEntity;
import com.track.entities.LawyerFieldRelation;
import com.track.entities.LawyerInfoEntity;
import com.track.entities.LawyerLocation;
import com.track.entities.UserFavoriteRelation;
import com.track.entities.UserInfoEntity;
import com.track.service.ArticleInfoServiceI;
import com.track.service.FieldInfoServiceI;
import com.track.service.UserInfoServiceI;
import com.track.util.EncodingTool;
import com.track.util.HttpRequestUtil;
import com.track.util.uploadAction;

import net.sf.json.JSONObject;


@Controller
@RequestMapping("/userInfoController")
public class UserInfoController {

	@Autowired
	private UserInfoServiceI userInfoService;
	@Autowired
	private FieldInfoServiceI fieldInfoService;
	@Autowired
	private ArticleInfoServiceI articleInfoService;

//	用户登录&自动注册
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public ApiResult Login(@RequestBody String params){
		String app_id = "wx596e7ba76a1ac0da";
		String app_secret = "de171a9f4113e740578d3c771871a239";
	    String GetOpenIdUrl = "https://api.weixin.qq.com/sns/jscode2session";
	    //获取参数code
	    JSONObject paramsJson = JSONObject.fromObject(params);
	    String para ="appid="+app_id+"&secret="+app_secret+"&js_code="+paramsJson.getString("code")+"&grant_type=authorization_code";
	    String openIdResult=HttpRequestUtil.sendGet(GetOpenIdUrl,para);  
	    JSONObject openIdJson = JSONObject.fromObject(openIdResult);
	    if(openIdJson.containsKey("openid")){
	    	String openid=openIdJson.getString("openid");
	    	JSONObject userinfo = paramsJson.getJSONObject("userInfo");
	    	UserInfoEntity uInfo = userInfoService.findById(openid);
	    	if(uInfo==null){
	    		UserInfoEntity user=new UserInfoEntity();
	    		user.setRole(0);
	    		user.setOpenid(openid);
	    		user.setNickname(userinfo.getString("nickName"));
		    	userInfoService.save(user);
	    	}
	    	else{
	    		uInfo.setNickname(userinfo.getString("nickName"));
		    	uInfo.setAvatarUrl(userinfo.getString("avatarUrl"));
		    	userInfoService.save(uInfo);
	    	}
	    	
	    	return ApiResult.success(userInfoService.findRoleInfo(openid));
	    }
	    else{
	    	String errorMsg = openIdJson.getString("errmsg");
	    	int errcode=0;
	    	try {
	    		errcode = Integer.parseInt(openIdJson.getString("errcode"));
	    	   
	    	} catch (NumberFormatException e) {
	    	    e.printStackTrace();
	    	}
	       return ApiResult.failure(errorMsg, errcode);
	    }
	}

//	根据openid获取用户信息
	@RequestMapping(value = "/getUserInfo/{openid}", method = RequestMethod.GET)
	@ResponseBody
	public ApiResult getUserInfo(@PathVariable("openid") String openid){
		return ApiResult.success(userInfoService.findRoleInfo(openid));
	}
	
//	根据openid获取律师信息
	@RequestMapping(value = "/getLawyerInfo/{openid}", method = RequestMethod.GET)
	@ResponseBody
	public ApiResult getLawyerInfo(@PathVariable("openid") String openid){
		return ApiResult.success(userInfoService.findLawyerById(openid));
	}
//	修改律师基本信息
	@RequestMapping(value = "/modifyInfo", method = RequestMethod.POST)
	@ResponseBody
	public ApiResult modifyInfo(@RequestBody JSONObject param){
		String openid = param.getString("id");
		String field = param.getString("field");
		if(field.equals("mobile")){
			LawyerInfoEntity l = userInfoService.findLawyerById(openid);
			LawyerInfoEntity lAuth = userInfoService.findAuthLawyerById(openid);
			String mobile = param.getString("mobile");
			l.setMobile(mobile);
			lAuth.setMobile(mobile);
			userInfoService.saveLawyer(l);
			userInfoService.saveLawyer(lAuth);
		}
		else if(field.equals("address")){
			LawyerInfoEntity l = userInfoService.findLawyerById(openid);
			LawyerInfoEntity lAuth = userInfoService.findAuthLawyerById(openid);
			String address=param.getString("address");
			l.setAddress(address);
			lAuth.setAddress(address);
			userInfoService.saveLawyer(l);
			userInfoService.saveLawyer(lAuth);
			userInfoService.saveAddress(param, openid);
		}
		else if(field.equals("field")){
			String[] id=param.getString("fieldIds").split(",");
			fieldInfoService.delLawyerFieldRelation(openid);
			for(int i=0;i<id.length;i++){
				LawyerFieldRelation relation = new LawyerFieldRelation();
				relation.setId(UUID.randomUUID().toString().replaceAll("-", ""));
				relation.setFieldId(Integer.parseInt(id[i]));
				relation.setLawyerId(openid);
				fieldInfoService.saveLawyerFieldRelation(relation);
			}
		}
		else if(field.equals("qrcode")){
			LawyerInfoEntity l = userInfoService.findLawyerById(openid);
			LawyerInfoEntity lAuth = userInfoService.findAuthLawyerById(openid);
			String qrcode = param.getString("qrcode");
			l.setQrcode(qrcode);
			lAuth.setQrcode(qrcode);
			userInfoService.saveLawyer(l);
			userInfoService.saveLawyer(lAuth);
		}
		return ApiResult.success("");
		
	}
	
//	根据openid获取律师Qrcode信息
	@RequestMapping(value = "/getQrcode/{openid}", method = RequestMethod.GET)
	@ResponseBody
	public ApiResult getQrcode(@PathVariable("openid") String openid,@RequestParam("status") String status){
		String Qrcode = userInfoService.getQrcode(openid,status);
		if(Qrcode == null || Qrcode.length() == 0){
			return ApiResult.failure("该律师暂未上传二维码", Qrcode);
		}
		else{
			return ApiResult.success(Qrcode);
		}
	
	}
	
//	查询普通用户认证状态
	@RequestMapping(value="/getLawyerStatus/{openid}",method=RequestMethod.GET)
	@ResponseBody
	public ApiResult getLawyerStatus(@PathVariable("openid") String openid){
		return ApiResult.success(userInfoService.getLawyerStatus(openid));
	}
	
//	保存认证信息
	@RequestMapping(value = "/saveLawyerInfo", method = RequestMethod.POST)
	@ResponseBody
	public ApiResult saveLawyerInfo(@RequestBody JSONObject params){
		LawyerInfoEntity lawyer=new LawyerInfoEntity();
		String openid=params.getString("openid");
		LawyerInfoEntity l = userInfoService.findAuthLawyerById(openid);
		if(l != null){
			lawyer.setId(l.getId());
		}
		else{
			String uuid=UUID.randomUUID().toString().replaceAll("-", "");
			lawyer.setId(uuid);
		}
		lawyer.setOpenId(openid);
		lawyer.setLawfirm(params.getString("lawfirm"));
		lawyer.setAvatarUrl(params.getString("avatarUrl"));
		lawyer.setLicenseNum(params.getString("licenseNum"));
		lawyer.setMobile(params.getString("mobile"));
		lawyer.setQrcode(params.getString("Qrcode"));
		lawyer.setRealname(params.getString("realname"));
		lawyer.setStatus(params.getInt("status"));
		JSONObject location = params.getJSONObject("address");
		lawyer.setAddress(location.getString("title"));
		userInfoService.saveLawyer(lawyer);
//		保存办公地点
		userInfoService.saveAddress(location, openid);
	
//		保存专长关系
		String[] id=params.getString("fieldIds").split(",");
		for(int i=0;i<id.length;i++){
			LawyerFieldRelation relation = new LawyerFieldRelation();
			relation.setId(UUID.randomUUID().toString().replaceAll("-", ""));
			relation.setFieldId(Integer.parseInt(id[i]));
			relation.setLawyerId(openid);
			fieldInfoService.saveLawyerFieldRelation(relation);
		}
		
		
		return ApiResult.success("");
	}
	
//	修改认证信息
	@RequestMapping(value = "/updateLawyerInfo", method = RequestMethod.POST)
	@ResponseBody
	public ApiResult updateLawyerInfo(@RequestBody JSONObject params){
		String openid=params.getString("openid");
		LawyerInfoEntity l=userInfoService.findLawyerById(openid);
		LawyerInfoEntity lAuth = userInfoService.findAuthLawyerById(openid);
		LawyerInfoEntity lawyer = new LawyerInfoEntity();
		if(lAuth==null){
			lawyer.setId(UUID.randomUUID().toString().replaceAll("-", ""));
		}
		else{
			lawyer.setId(lAuth.getId());
		}
		
		lawyer.setLawfirm(params.getString("lawfirm"));
		lawyer.setAvatarUrl(params.getString("avatarUrl"));
		lawyer.setLicenseNum(params.getString("licenseNum"));
		lawyer.setRealname(params.getString("realname"));
		lawyer.setStatus(2);
		lawyer.setAddress(l.getAddress());
		lawyer.setMobile(l.getMobile());
		lawyer.setOpenId(openid);
		lawyer.setQrcode(l.getQrcode());
		
		userInfoService.saveLawyer(lawyer);
		
		return ApiResult.success("");
	}
	
	
//	上传文件
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	public ApiResult upload(MultipartFile file,HttpServletRequest request) throws IOException{
	    if(file.isEmpty()){   
	   		return ApiResult.failure("文件为空", "error");
	    }else{   
	    	if(file.getContentType().indexOf("image")>-1){
	    		String realPath = request.getSession().getServletContext().getRealPath("/upload");   
		    	String fileName = uploadAction.upload(file,realPath);	
		
			   return ApiResult.success("upload/"+fileName);
	    	}
	    	else
	    		return ApiResult.failure("上传文件格式错误", "content-type must be image");
	    
	    }   
	}
	
//	搜索
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	@ResponseBody
	public ApiResult search(@RequestParam("range") String range,
			@RequestParam(value="realname",required=false) String realname,
			@RequestParam(value="fieldId",required=false) Integer fieldId,
			@RequestParam(value="province",required=false) String province,
			@RequestParam(value="city",required=false) String city,
			@RequestParam(value="district",required=false) String district,
			@RequestParam("lng") float lng,
			@RequestParam("lat") float lat){

		List<LawyerInfoEntity> userlist = new ArrayList<LawyerInfoEntity>();
		if(range.equals("all")){
			if(realname == null || realname.length() <= 0){
				userlist=userInfoService.getAllLawyer();
			}	
			else{
				userlist = userInfoService.getLawyerByName(realname);
			}
		}
		else if(range.equals("field")){
			userlist = userInfoService.getLawyerByField(fieldId);
		}
		else if(range.equals("region")){
			province=EncodingTool.encodeStr(province);
			city=EncodingTool.encodeStr(city);
			district=EncodingTool.encodeStr(district);
			userlist=userInfoService.getLawyerByRegion(province, city, district);
		}
		if(userlist.size()>0){
			List<JSONObject> userTotalInfoList = userInfoService.getUserTotalInfo(userlist, lat,lng);
			return ApiResult.success(userTotalInfoList);
		}
		else{
			return ApiResult.success("[]");
		}
		
	}

	
//	删除文件
	@RequestMapping(value="/delPic",method=RequestMethod.POST)
	@ResponseBody
	public ApiResult delPic(@RequestBody JSONObject param){
		String fileName=param.getString("fileName");
		String realpath = System.getProperty("user.dir")+"/src/main/webapp/"+fileName;
		return ApiResult.success(new File(realpath).delete());
	}
//	计算与律师的距离
	@RequestMapping(value="/getDistance",method=RequestMethod.GET)
	@ResponseBody
	public ApiResult getLawyerAllInfo(@RequestParam("lawyerid") String lawyerid,
			@RequestParam("lng") float lng,@RequestParam("lat") float lat){
		LawyerLocation location = userInfoService.findLocation(lawyerid);
		return ApiResult.success(userInfoService.distance(lng, lat, location));
	}
//	查询律师主页内容
	@RequestMapping(value="/getLawyerAllInfo/{openid}",method=RequestMethod.GET)
	@ResponseBody
	public ApiResult getLawyerAllInfo(@PathVariable("openid") String openid){
		return ApiResult.success(userInfoService.getLawyerAllInfo(openid));
	}
//	查询还未通过认证的律师主页内容
	@RequestMapping(value="/getAuthLawyerAllInfo/{openid}",method=RequestMethod.GET)
	@ResponseBody
	public ApiResult getAuthLawyerAllInfo(@PathVariable("openid") String openid){
		return ApiResult.success(userInfoService.getAuthLawyerAllInfo(openid));
	}
	
//	查看待处理认证数量
	@RequestMapping(value="/getAuthNum",method=RequestMethod.GET)
	@ResponseBody
	public ApiResult getAuthNum(){
		return ApiResult.success(userInfoService.getAuthNum());
	}
	
//	查看待处理认证列表
	@RequestMapping(value="/getAuthList",method=RequestMethod.GET)
	@ResponseBody
	public ApiResult getAuthList(){
		return ApiResult.success(userInfoService.getAuthList());
	}
	
//	处理律师认证
	@RequestMapping(value="/handleLawyer",method=RequestMethod.POST)
	@ResponseBody
	public ApiResult handleLawyer(@RequestBody JSONObject param){
		String openid = param.getString("openid");
		int status = param.getInt("status");
		
		if(status==1){
			//改认证状态
			LawyerInfoEntity lawyer = userInfoService.findLawyerById(openid);
			LawyerInfoEntity l = userInfoService.findAuthLawyerById(openid);
			if(lawyer != null){//是律师
				lawyer.setStatus(status);
				lawyer.setAvatarUrl(l.getAvatarUrl());
				lawyer.setRealname(l.getRealname());
				lawyer.setLicenseNum(l.getLicenseNum());
				lawyer.setLawfirm(l.getLawfirm());
				userInfoService.saveLawyer(lawyer);
			}
			else{//不是律师
				l.setStatus(status);
				userInfoService.saveLawyer(l);
			}
			
			//改用户角色
			UserInfoEntity user = userInfoService.findById(openid);
			user.setRole(2);
			userInfoService.save(user);
			//删除认证中信息
			userInfoService.delLawyerInfo(openid);
		}
		else{
			//改认证状态
			LawyerInfoEntity lawyer = userInfoService.findAuthLawyerById(openid);
			lawyer.setStatus(status);
			userInfoService.saveLawyer(lawyer);
		}
		return ApiResult.success("");
	}
	
//	根据用户openid获取其收藏律师信息列表
	@RequestMapping(value = "/getFavoriteList", method = RequestMethod.POST)
	@ResponseBody
	public ApiResult getFavoriteList(@RequestBody JSONObject param){
		String id = param.getString("openid");
		float lng = (float)param.getDouble("lng");
		float lat = (float)param.getDouble("lat");
		List<UserFavoriteRelation> relationList = userInfoService.getFavoriteList(id);
		List<Object> result=new ArrayList<Object>();
		JSONObject value = new JSONObject();
		for(UserFavoriteRelation item : relationList){
			String userid = item.getLawyerId();
			value.put("lawyer", userInfoService.findLawyerById(userid));
			value.put("distance", userInfoService.distance(lng,lat, userInfoService.findLocation(userid)));
			result.add(value);
		}
		return ApiResult.success(result);
	}
	
//	律师发表案例
	@RequestMapping(value = "/addArticle", method = RequestMethod.POST)
	@ResponseBody
	public ApiResult addArticle(@RequestBody JSONObject params){
		ArticleInfoEntity article = new ArticleInfoEntity();
		article.setId(UUID.randomUUID().toString().replaceAll("-", ""));
		article.setCaseContent(params.getString("casecontent"));
		article.setJudgment(params.getString("judgment"));
		article.setLawyerId(params.getString("lawyerid"));
		article.setViewpoint(params.getString("viewpoint"));
		article.setTitle(params.getString("title"));
		article.setTime(new Date());
		return ApiResult.success(articleInfoService.saveArticle(article));
	}
//	查看案例
	@RequestMapping(value = "/getArticle/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ApiResult getArticle(@PathVariable("id") String id){
		ArticleInfoEntity article = articleInfoService.findArticleById(id);
		String openid = article.getLawyerId();
		LawyerInfoEntity lawyer = userInfoService.findLawyerById(openid);
		JSONObject result = new JSONObject();
		result.put("article", article);
		result.put("lawyer", lawyer);
		return ApiResult.success(result);
	}
//	查看案例列表
	@RequestMapping(value = "/getArticleList/{openid}", method = RequestMethod.GET)
	@ResponseBody
	public ApiResult getArticleList(@PathVariable("openid") String openid){
		return ApiResult.success(articleInfoService.findLawyerArticle(openid));
	}
	
//	添加收藏
	@RequestMapping(value = "/addUserRelation", method = RequestMethod.POST)
	@ResponseBody
	public ApiResult addUserRelation(@RequestBody JSONObject params){
		if(!userInfoService.searchFavor(params.getString("userid"), params.getString("lawyerid"))){
			UserFavoriteRelation relation = new UserFavoriteRelation();
			relation.setId(UUID.randomUUID().toString().replaceAll("-", ""));
			relation.setLawyerId(params.getString("lawyerid"));
			relation.setUserId(params.getString("userid"));
			userInfoService.saveFavor(relation);
		}
		return ApiResult.success("");
	}
//	删除收藏
	@RequestMapping(value = "/delUserRelation", method = RequestMethod.DELETE)
	@ResponseBody
	public ApiResult delUserRelation(@RequestParam("userid") String userid,@RequestParam("lawyerid") String lawyerid){
		userInfoService.delFavor(userid, lawyerid);
		return ApiResult.success("");
	}
	
//	查询收藏
	@RequestMapping(value = "/searchUserRelation", method = RequestMethod.GET)
	@ResponseBody
	public ApiResult searchUserRelation(@RequestParam("userid") String userid,@RequestParam("lawyerid") String lawyerid){
		return ApiResult.success(userInfoService.searchFavor(userid, lawyerid));
	}
	
}



