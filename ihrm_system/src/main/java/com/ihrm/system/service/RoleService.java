package com.ihrm.system.service;

import com.ihrm.common.service.BaseService;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.common.utils.PermissionConstants;
import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.Role;
import com.ihrm.system.dao.PermissionDao;
import com.ihrm.system.dao.RoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleService extends BaseService {

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PermissionDao permissionDao;

    @Autowired
    private IdWorker idWorker;

    /**
     * 添加角色
     */
    public void save(Role role) {
        //填充其他参数
        role.setId(idWorker.nextId() + "");
        roleDao.save(role);
    }

    /**
     * 更新角色
     */
    public void update(Role role) {
        Role targer = roleDao.getOne(role.getId());
        targer.setDescription(role.getDescription());
        targer.setName(role.getName());
        roleDao.save(targer);
    }

    /**
     * 根据ID查询角色
     * 北京市昌平区建材城西路金燕龙办公楼一层 电话：400-618-9090
     */
    public Role findById(String id) {
        return roleDao.findById(id).get();
    }

    /**
     * 删除角色
     */
    public void delete(String id) {
        roleDao.deleteById(id);
    }

    public Page<Role> findSearch(String companyId, int page, int size) {
        Specification<Role> specification = new Specification<Role>() {
            @Override
            public Predicate toPredicate(Root<Role> root, CriteriaQuery<?> query,
                                         CriteriaBuilder cb) {
                return cb.equal(root.get("companyId").as(String.class), companyId);
            }
        };
        return roleDao.findAll(specification, PageRequest.of(page - 1, size));
    }

    /**
     * 分配权限
     * @param roleId
     * @param permIds
     */
    public void assignPerm(String roleId, List<String> permIds) {
        //1。获取分配权限的角色对象
        Role role = roleDao.findById(roleId).get();
        //2.构建角色的权限集合
        Set<Permission> perms = new HashSet<>();
        for (String permId : permIds) {
            Permission permission = permissionDao.findById(permId).get();
            //需要根据父id和类型查询API权限列表
            List<Permission> apiList = permissionDao.findByTypeAndPid(PermissionConstants.PERMISSION_API, permission.getId());
            //自定义赋予API权限
            perms.addAll(apiList);
            //当前菜单或按钮的权限
            perms.add(permission);
        }
        System.out.println(perms.size());
        //3.设置角色和权限的关系
        role.setPermissions(perms);
        //4.更新角色
        roleDao.save(role);
    }

    public List<Role> findAll(String companyId) {
        return roleDao.findAll(getSpec(companyId));
    }
}
