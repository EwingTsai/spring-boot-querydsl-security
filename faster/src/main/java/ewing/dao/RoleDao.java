package ewing.dao;

import com.querydsl.core.types.Predicate;
import ewing.query.BasicDao;
import ewing.query.Page;
import ewing.query.Pager;
import ewing.dao.entity.Role;
import ewing.security.SecurityBeans;
import ewing.security.vo.RoleWithAuthority;

import java.math.BigInteger;
import java.util.List;

/**
 * 角色数据访问接口。
 */
public interface RoleDao extends BasicDao<Role>, SecurityBeans {

    Page<RoleWithAuthority> findRoleWithAuthority(Pager pager, Predicate predicate);

    List<Role> getRolesByUser(BigInteger userId);

}