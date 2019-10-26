package com.ihrm.system.controller;

import com.alibaba.fastjson.JSON;
import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;

import com.ihrm.common.exception.CommonException;
import com.ihrm.common.utils.JwtUtils;
import com.ihrm.common.utils.PermissionConstants;
import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.Role;
import com.ihrm.domain.system.User;
import com.ihrm.domain.system.response.ProfileResult;
import com.ihrm.domain.system.response.UserResult;
import com.ihrm.system.service.PermissionService;
import com.ihrm.system.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.HashAttributeSet;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//1.解决跨域
@CrossOrigin
//2.声明restContoller
@RestController
//3.设置父路径
@RequestMapping(value="/sys")

@Slf4j
public class UserController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 分配角色
     * @param map
     * @return
     */
    @RequestMapping(value = "/user/assignRoles",method = RequestMethod.PUT)
    public Result save(@RequestBody Map<String,Object> map){
        //1.获取被分配的用户id
        String userId = (String) map.get("id");
        //2.获取到角色的id列表
        List<String> roleIds = (List<String>) map.get("roleIds");
        //3.调用service完成角色分配
        userService.assignRoles(userId,roleIds);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 保存
     */
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public Result save(@RequestBody User user) {
        //1.设置保存的企业id
        user.setCompanyId(companyId);
        user.setCompanyName(companyName);
        //2.调用service完成保存企业
        userService.save(user);
        logger.info("保存成功");
        //3.构造返回结果
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 查询企业的部门列表
     * 指定企业id
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public Result findAll(int page, int size, @RequestParam Map map) {
        //1.获取当前的企业id
        map.put("companyId",companyId);
        //2.完成查询
        Page<User> pageUser = userService.findAll(map,page,size);
        //3.构造返回结果
        PageResult pageResult = new PageResult(pageUser.getTotalElements(),pageUser.getContent());
        logger.info("获得的结果是：{}", JSON.toJSONString(pageResult));
        return new Result(ResultCode.SUCCESS, pageResult);
    }

    /**
     * 根据ID查询user
     */
    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    public Result findById(@PathVariable(value = "id") String id) {
        User user = userService.findById(id);
        UserResult userResult = new UserResult(user);
        return new Result(ResultCode.SUCCESS, userResult);
    }

    /**
     * 修改User
     */
    @RequestMapping(value = "/user/{id}", method = RequestMethod.PUT)
    public Result update(@PathVariable(value = "id") String id, @RequestBody User user) {
        //1.设置修改的部门id
        user.setId(id);
        //2.调用service更新
        userService.update(user);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 根据id删除
     */
    @RequiresPermissions(value = "API-USER-DELETE")
    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE,name = "API-USER-DELETE")
    public Result delete(@PathVariable(value = "id") String id) {
        userService.deleteById(id);
        return new Result(ResultCode.SUCCESS);
    }
    /**
     * 用户登陆
     * 1，通过比较service根据mobile查询用户
     * 2，比较password
     * 3，生成jwt信息
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result login(@RequestBody Map<String,String> loginMap) {
        String mobile = loginMap.get("mobile");
        String password = loginMap.get("password");

        try {
            //1.构造登陆令牌 UsernamePasswordToken
            //加密密码
            password = new Md5Hash(password,mobile,3).toString();
            UsernamePasswordToken upToken = new UsernamePasswordToken(mobile, password);
            //2。获取subject
            Subject subject = SecurityUtils.getSubject();
            //3.调用login方法，进入realm完成认证
            subject.login(upToken);
            //4。获取sessionId
            String sessionId = (String) subject.getSession().getId();
            //5.构造返回结果
            return new Result(ResultCode.SUCCESS,sessionId);
        }catch (Exception e){
            return new Result(ResultCode.MOBILEORPASSWORDERROR);
        }

//        User user = userService.findByMobile(mobile);
//        //登陆失败
//        if (null == user || !user.getPassword().equals(password)){
//            return new Result(ResultCode.MOBILEORPASSWORDERROR);
//        }else {
//            //登陆成功
//            //api权限字符串
//            StringBuilder sb = new StringBuilder();
//            //获取到所有的可访问API权限
//            for (Role role : user.getRoles()) {
//                for (Permission perm : role.getPermissions()) {
//                    if (perm.getType() == PermissionConstants.PERMISSION_API){
//                        sb.append(perm.getCode()).append(",");
//                    }
//                }
//            }
//            Map<String,Object> map = new HashMap<>();
//            map.put("apis",sb.toString());
//            map.put("companyId",user.getCompanyId());
//            map.put("companyName",user.getCompanyName());
//            String token = jwtUtils.createJwt(user.getId(), user.getUsername(), map);
//            return new Result(ResultCode.SUCCESS,token);
//        }
    }

    /**
     * 用户登陆成功后，获取用户信息
     *  1，获取用户ID
     *  2，根据用户ID查询用户
     *  3，构建返回值对象
     *  4，响应
     */
    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public Result profile(HttpServletRequest request) throws Exception {
        //获取session中的安全数据
        Subject subject = SecurityUtils.getSubject();
        //2.subject获取所有的安全数据集合
        PrincipalCollection principals = subject.getPrincipals();
        //3.获取安全数据
        ProfileResult result = (ProfileResult) principals.getPrimaryPrincipal();

//        String userId = claims.getId();
//        User user = userService.findById(userId);
//
//        //根据不同的用户级别获取用户权限
//
//        ProfileResult result = null;
//        if ("user".equals(user.getLevel())){
//            //普通用户
//            result = new ProfileResult(user);
//        }else {
//            Map map = new HashMap();
//            if ("coAdmin".equals(user.getLevel())) {
//                //企业管理员
//                map.put("enVisible","1");
//            }
//            List<Permission> list = permissionService.findAll(map);
//            result = new ProfileResult(user, list);
//
//        }
        return new Result(ResultCode.SUCCESS,result);
    }

    public static void main(String[] args) {
        String password = new Md5Hash("123456","13800000003",3).toString();
        System.out.println(password);
    }
}
