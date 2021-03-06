package com.muiboot.shiro.system.service.impl;

import java.util.*;

import com.muiboot.shiro.common.menum.GroupType;
import com.muiboot.core.service.impl.BaseService;
import com.muiboot.shiro.system.dao.SysGroupMapper;
import com.muiboot.shiro.system.entity.SysGroup;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.muiboot.core.entity.LayerTree;
import com.muiboot.core.util.TreeUtils;
import com.muiboot.shiro.system.service.GroupService;
import tk.mybatis.mapper.entity.Example;

@Service("groupService")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class GroupServiceImpl extends BaseService<SysGroup> implements GroupService {

	@Autowired
	private SysGroupMapper groupMapper;

	@Override
	public LayerTree<SysGroup> getGroupTree(String groupName) {
		List<LayerTree<SysGroup>> trees = new ArrayList<>();
		List<SysGroup> groups = this.findAllGroups(new SysGroup());
		if (StringUtils.isNotBlank(groupName)){
			groups=findDicsWithNameLike(groups,groupName);
		}
		for (SysGroup group : groups) {
			LayerTree<SysGroup> tree = new LayerTree<>();
			tree.setId(group.getGroupId().toString());
			tree.setParentId(group.getParentId().toString());
			tree.setName(group.getGroupName());
			if (GroupType.ORGAN.getType().equals(group.getGroupType())){
				tree.setIcon("layui-icon layui-icon-group");
			}else {
				tree.setIcon("layui-icon layui-icon-user");
			}
			tree.setLevel(group.getGroupType());
			trees.add(tree);
		}
		return TreeUtils.build(trees);
	}

	@Override
	public List<SysGroup> findAllGroups(SysGroup group) {
		try {
			Example example = new Example(SysGroup.class);
			Example.Criteria criteria=example.createCriteria();
			if (StringUtils.isNotBlank(group.getGroupName())) {
				criteria.andEqualTo("groupName",group.getGroupName());
			}
			if (null!=group.getParentId()) {
				criteria.andEqualTo("parentId",group.getParentId());
			}
			if (StringUtils.isNotBlank(group.getValid())) {
				criteria.andEqualTo("valid",group.getValid());
			}
			if (StringUtils.isNotBlank(group.getGroupType())) {
				criteria.andEqualTo("groupType",group.getGroupType());
			}
			example.setOrderByClause("group_type desc,group_id");
			return this.selectByExample(example);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	@Override
	public SysGroup findByName(String groupName) {
		Example example = new Example(SysGroup.class);
		example.createCriteria().andEqualTo("groupName",groupName);
		List<SysGroup> list = this.selectByExample(example);
		if (list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}

	@Override
	public SysGroup findById(Long groupId) {
		return this.selectByKey(groupId);
	}

	@Override
	@Transactional
	public void addGroup(SysGroup group) {
		Long parentId = group.getParentId();
		if (parentId == null)
			group.setParentId(0L);
		group.setCreateTime(new Date());
		this.save(group);
	}

	@Override
	@Transactional
	public void updateGroup(SysGroup group) {
		this.updateNotNull(group);
	}

	@Override
	public void deleteGroups(String groupIds) {
		List<String> list = Arrays.asList(groupIds.split(","));
		this.batchDelete(list, "groupId", SysGroup.class);
	}

	@Override
	public Map getGroupDetail(Long groupId) {
		//1.获取部门详情
		SysGroup group=this.selectByKey(groupId);
		//2.获取部门人员列表
		Map res = new HashMap();
		res.put("info",group);
		return res;
	}

	@Override
	public Map getDeptByParent(Long parentId) {
		SysGroup $group = new SysGroup();
		$group.setParentId(parentId);
		$group.setValid("1");
		$group.setGroupType(GroupType.DEPT.getType());
		List<SysGroup> groups=this.findAllGroups($group);
		LinkedHashMap<Long,String> res = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(groups)){
			for (SysGroup group:groups){
				res.put(group.getGroupId(),group.getGroupName());
			}
		}
		return res;
	}

	/**
	 * 模糊匹配，找到搜索节点
	 * @param dics
	 * @param dicName
	 */
	private List<SysGroup> findDicsWithNameLike(List<SysGroup> dics, String dicName) {
		List<SysGroup> res = new ArrayList<>();
		//1找到搜索节点list
		List<SysGroup> owners= findDicsByNameLike(dics,dicName);
		//2.递归查找子节点
		List<SysGroup> children = new ArrayList<>();
		findChildren(dics,owners,children);
		//3.递归查找父节点
		List<SysGroup> parents=new ArrayList<>();
		findParents(dics,owners,parents);
		if(null!=owners)res.addAll(owners);
		if(null!=children)res.addAll(children);
		if(null!=parents)res.addAll(parents);
		return res;
	}

	private List<SysGroup> findParents(List<SysGroup> dics, List<SysGroup> owners, List<SysGroup> parents) {
		if (CollectionUtils.isEmpty(dics)||CollectionUtils.isEmpty(owners))return parents;
		Set<Long> ownerParentsIds=new HashSet<>();
		Iterator<SysGroup> ownerIt = owners.iterator();
		while(ownerIt.hasNext()){
			SysGroup d = ownerIt.next();
			ownerParentsIds.add(d.getParentId());
		}
		Iterator<SysGroup> it = dics.iterator();
		List<SysGroup> ownersNew = new ArrayList<>();
		while(it.hasNext()){
			SysGroup d = it.next();
			if(ownerParentsIds.contains(d.getGroupId())){
				parents.add(d);
				ownersNew.add(d);
				it.remove();
			}
		}
		return findParents(dics,ownersNew,parents);
	}

	private List<SysGroup> findChildren(List<SysGroup> dics, List<SysGroup> owners, List<SysGroup> children) {
		if (CollectionUtils.isEmpty(dics)||CollectionUtils.isEmpty(owners))return children;
		Set<Long> ownerIds=new HashSet<>();
		Iterator<SysGroup> ownerIt = owners.iterator();
		while(ownerIt.hasNext()){
			SysGroup d = ownerIt.next();
			ownerIds.add(d.getGroupId());
		}
		Iterator<SysGroup> it = dics.iterator();
		List<SysGroup> ownersNew = new ArrayList<>();
		while(it.hasNext()){
			SysGroup d = it.next();
			if(ownerIds.contains(d.getParentId())){
				children.add(d);
				ownersNew.add(d);
				it.remove();
			}
		}
		return findChildren(dics,ownersNew,children);
	}

	private List<SysGroup> findDicsByNameLike(List<SysGroup> dics, String dicName) {
		if (CollectionUtils.isEmpty(dics))return null;
		List<SysGroup> owners=new ArrayList<>();
		Iterator<SysGroup> it = dics.iterator();
		while(it.hasNext()){
			SysGroup d = it.next();
			if (d.getGroupName().contains(dicName)){
				owners.add(d);
				it.remove();
			}
		}
		return owners;
	}
}
