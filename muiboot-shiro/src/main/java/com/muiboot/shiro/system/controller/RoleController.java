package com.muiboot.shiro.system.controller;

import java.util.List;
import java.util.Map;

import com.muiboot.core.annotation.Log;
import com.muiboot.core.entity.QueryRequest;
import com.muiboot.core.entity.ResponseBo;
import com.muiboot.core.util.FileUtils;
import com.muiboot.shiro.system.entity.Role;
import com.muiboot.shiro.system.service.RoleService;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import com.muiboot.shiro.common.controller.ShiroBaseController;

@Controller
public class RoleController extends ShiroBaseController {

	@Autowired
	private RoleService roleService;

	@RequestMapping("role/list")
	@ResponseBody
	public Map<String, Object> roleList(QueryRequest request, Role role) {
		PageHelper.startPage(request.getPage(), request.getLimit());
		List<Role> list = this.roleService.findAllRole(role);
		PageInfo<Role> pageInfo = new PageInfo<>(list);
		return getDataTable(pageInfo);
	}
	@RequestMapping("role/grantUser")
	@RequiresPermissions("role:add")
	@ResponseBody
	public ResponseBo grantUser(Long roleId,Long userId) {
		roleService.grantUser(roleId,userId);
		return ResponseBo.ok("授权成功！");
	}
	@RequestMapping("role/revokeUser")
	@RequiresPermissions("role:add")
	@ResponseBody
	public ResponseBo revokeUser(Long roleId,Long userId) {
		roleService.revokeUser(roleId,userId);
		return ResponseBo.ok("权限回收成功！");
	}
	@RequestMapping("role/excel")
	@ResponseBody
	public ResponseBo roleExcel(Role role) {
		try {
			List<Role> list = this.roleService.findAllRole(role);
			return FileUtils.createExcelByPOIKit("角色表", list, Role.class);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseBo.error("导出Excel失败，请联系网站管理员！");
		}
	}

	@RequestMapping("role/csv")
	@ResponseBody
	public ResponseBo roleCsv(Role role){
		try {
			List<Role> list = this.roleService.findAllRole(role);
			return FileUtils.createCsv("角色表", list, Role.class);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseBo.error("导出Csv失败，请联系网站管理员！");
		}
	}
	
	@RequestMapping("role/getRole")
	@ResponseBody
	public ResponseBo getRole(Long roleId) {
		try {
			Role role = this.roleService.selectByKey(roleId);
			return ResponseBo.ok(role);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseBo.error("获取角色信息失败，请联系网站管理员！");
		}
	}

	@RequestMapping("role/checkRoleName")
	@ResponseBody
	public boolean checkRoleName(String roleName, String oldRoleName) {
		if (StringUtils.isNotBlank(oldRoleName) && roleName.equalsIgnoreCase(oldRoleName)) {
			return true;
		}
		Role result = this.roleService.findByName(roleName);
		return result == null;
	}

	@Log("新增角色")
	@RequiresPermissions("role:add")
	@RequestMapping("role/add")
	@ResponseBody
	public ResponseBo addRole(Role role, Long[] menuId) {
		this.roleService.addRole(role, menuId);
		return ResponseBo.ok("新增角色成功！");
	}

	@Log("删除角色")
	@RequiresPermissions("role:delete")
	@RequestMapping("role/delete")
	@ResponseBody
	public ResponseBo deleteRoles(String ids) {
		this.roleService.deleteRoles(ids);
		return ResponseBo.ok("删除角色成功！");
	}

	@Log("修改角色")
	@RequiresPermissions("role:update")
	@RequestMapping("role/update")
	@ResponseBody
	public ResponseBo updateRole(Role role, Long[] menuId) {
		this.roleService.updateRole(role, menuId);
		return ResponseBo.ok("修改角色成功！");
	}

	@Log("用户授权")
	@RequestMapping("role/grant")
	@ResponseBody
	public ResponseBo grant(Long[] userIds, Long[] roleIds) {
		this.roleService.grant(userIds, roleIds);
		return ResponseBo.ok("用户授权成功！");
	}
}
