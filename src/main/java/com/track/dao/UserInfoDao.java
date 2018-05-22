package com.track.dao;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import com.track.entities.UserInfoEntity;
import com.track.entities.LawyerInfoEntity;
import com.track.entities.LawyerLocation;
import com.track.entities.UserFavoriteRelation;

@Repository
public class UserInfoDao extends HibernateDaoSupport {
	
	@Autowired
    private SessionFactory sessionFactory;
	
//	保存和修改用户信息
	public void save(UserInfoEntity user){
		sessionFactory.getCurrentSession().saveOrUpdate(user);		
	}
	
//	根据用户openid查询用户基本信息
	public UserInfoEntity findById(String openid){
		String hql = "from UserInfoEntity where openid=:openid";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("openid", openid);
		UserInfoEntity user = (UserInfoEntity) query.uniqueResult();
		return user;
	}
	
//	根据用户openid查询律师基本信息
	public LawyerInfoEntity findLawyerById(String openid){
		String hql = "from LawyerInfoEntity where openid=:openid and status=1";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("openid", openid);
		return (LawyerInfoEntity) query.uniqueResult();
	}
//	根据用户openid查询律师Qrcode
	public String getQrcode(String openid,String status){
		String hql = "select a.Qrcode from LawyerInfoEntity as a where openid=:openid and status=1";
		if(status.equals("0")){
			hql= "select a.Qrcode from LawyerInfoEntity as a where openid=:openid and status<>1";
			
		}
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("openid", openid);
		return (String) query.uniqueResult();
	}
//	保存律师办公地址
	public void saveAddress(LawyerLocation location){
		sessionFactory.getCurrentSession().saveOrUpdate(location);
	}
//	保存律师信息
	public void saveLawyer(LawyerInfoEntity lawyer){
		sessionFactory.getCurrentSession().saveOrUpdate(lawyer);
	}
//	获取用户收藏列表
	@SuppressWarnings("unchecked")
	public List<UserFavoriteRelation> getFavoriteList(String openid){
		String hql = "from UserFavoriteRelation as a where userid=:openid";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("openid", openid);
		return query.list();
	}
	
//	律师认证状态查询
	public int getLawyerStatus(String openid){
		String hql = "select a.status from LawyerInfoEntity as a where openid=:openid and status<>1";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("openid", openid);
		if(query.list().size()<=0){
			return -1;
		}
		else{
			return (Integer)query.uniqueResult();
		}
	}
//	查询还未通过的认证信息
	public LawyerInfoEntity findAuthLawyerById(String openid){
		String hql = "from LawyerInfoEntity where openid=:openid and status<>1";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("openid", openid);
		return (LawyerInfoEntity) query.uniqueResult();
	}
	
//	查询律师定位
	public LawyerLocation findLocation(String openid){
		String hql = "from LawyerLocation where lawyerid=:openid";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("openid", openid);
		return (LawyerLocation)query.uniqueResult();
		
	}
//	删除未通过的认证信息
	public void delLawyerInfo(String openid){
		String hql="delete from LawyerInfoEntity where openid=:openid and status<>1";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("openid", openid);
		query.executeUpdate();
	}
//	查看待处理认证数量
	public int getAuthNum(){
		String hql = "from LawyerInfoEntity where status=2";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		return query.list().size();	
	}
	
//	查看待处理认证列表
	@SuppressWarnings("unchecked")
	public List<LawyerInfoEntity> getAuthList(){
		String hql = "from LawyerInfoEntity where status=2";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		return query.list();	
	}
	
//	查询所有律师
	@SuppressWarnings("unchecked")
	public List<LawyerInfoEntity> getAllLawyer(){
		String hql="from LawyerInfoEntity where status=1";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		return query.list();
	}
//	按专长查询律师
	@SuppressWarnings("unchecked")
	public List<LawyerInfoEntity> getLawyerByField(int fieldId){
		String hql="select a from LawyerInfoEntity as a,LawyerFieldRelation as b where a.openid = b.lawyerid and b.fieldid=:fieldid and a.status=1";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("fieldid", fieldId);
		return query.list();
	}
	
//	按地区查询律师
	@SuppressWarnings("unchecked")
	public List<LawyerInfoEntity> getLawyerByRegion(String province,String city,String district){
		String hql="select a from LawyerInfoEntity as a,LawyerLocation as b where a.openid = b.lawyerid and b.province=:province and b.city=:city and b.district=:district and a.status=1";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("province", province);
		query.setParameter("city", city);
		query.setParameter("district", district);
		return query.list();
	}
//	按姓名查询律师
	@SuppressWarnings("unchecked")
	public List<LawyerInfoEntity> getLawyerByName(String realname){
		String hql="from LawyerInfoEntity where realname=:realname and status=1";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("realname", realname);
		return query.list();
	}
	
//	添加收藏
	public void saveFavor(UserFavoriteRelation relation){
		sessionFactory.getCurrentSession().saveOrUpdate(relation);		
	}
//	删除收藏
	public void delFavor(String userid,String lawyerid){
		String hql = "delete from UserFavoriteRelation where userid=:userid and lawyerid=:lawyerid";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("userid",userid);
		query.setParameter("lawyerid", lawyerid);
		query.executeUpdate();
	}

//	查询收藏
	public int searchFavor(String userid,String lawyerid){
		String hql = "from UserFavoriteRelation where userid=:userid and lawyerid=:lawyerid";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("userid",userid);
		query.setParameter("lawyerid", lawyerid);
		return query.list().size();
	}
}

