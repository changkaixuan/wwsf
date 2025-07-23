define([
	'underScore', 
	'text!application/wws_sys_role.html',
	'mloadding',
	'css!js/bootstrap/css/bootstrap-table.css', 
	'css!js/fancytree/skin-win8/ui.fancytree.css', 
	'fancytree',
	'jqueryUI',
	'bootstrap-select',
	'bootstrap-table-locale',
	'bootstrap-table'],
    function (_, template, mloadding) {
        var app = function () {
            this.o_template = $(template);
        }
        _.extend(app.prototype, {
        	initialize:function(){
        		
            },
            load: function () {

            },
            render: function (container) {
                var self = this;
                var o_container = $(container);
                o_container.empty();
                o_container.append(this.o_template);
	          	
                //查询按钮
                $('#btn_sysrole_query', this.o_container).bind('click', function(event){ 
                	$('#tb_sysRole').bootstrapTable('refresh'); 
                });
                //重置按钮
                $('#btn_sysrole_clear', this.o_container).bind('click', function(event){
                	$("input[name=qRoleId]").val("");
                	$("input[name=qRoleName]").val("");
                });
                //初始化用户表格
               self.initSysRoleTable();
               // 新增角色按钮
               $('#btn_sysrole_add', this.o_container).bind('click', function(event){ 
            	   self.showSysroleEditModal(null);
               });
               // 修改角色按钮
               $('#btn_sysrole_edit', this.o_container).bind('click', function(event){ 
            	   var result = $('#tb_sysRole').bootstrapTable('getSelections');
                   if(result.length ==1){
                	   self.showSysroleEditModal(result[0]);
                   }else{
                	   bootbox.alert({message:'请选中一个角色',size:'small'});
                   }
               });
               // 删除角色按钮
               $('#btn_sysrole_del', this.o_container).bind('click', function(event){
            	   self.deleteSysRole();
               });
            }, 
            initSysRoleTable: function(){
            	var self = this;
            	$('#tb_sysRole').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'sysRole/pageList',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    toolbar: '#sysrole_tab_toolbar',                //工具按钮用哪个容器
                    striped: true,                      //是否显示行间隔色
                    cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                    pagination: true,                   //是否显示分页（*）
                    sortable: false,                     //是否启用排序
                    sortOrder: "asc",                   //排序方式
                    queryParamsType:'undefined',
                    queryParams: function (params) {
                    	return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                    		pageNumber: params.pageNumber,   //页面大小
                    	   	pageSize: params.pageSize,  //页码
                    	   	roleName: $("input[name=qRoleName]").val(),
                    	   	roleId: $("input[name=qRoleId]").val()
                    	};
                    },           //传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "roleId",                     //每一行的唯一标识，一般为主键列
                    showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                   //是否显示父子表
                    showColumns: true,
                    showFullscreen: true,
                    showRefresh: true,
                    columns: [
                    	{checkbox: true}, 
                    	{field: 'roleId', title: '编号'}, 
                        {field: 'roleName', title: '名称'}, 
                        {field: 'roleDesc', title: '描述' }
                    ],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    onDblClickRow: function (row) {
                    	self.showSysroleEditModal(row);
                    }
                });
            },
            showSysroleEditModal : function(row) {
            	var self = this;
            	var tab = $("#tab_sysrole_info");
            	tab.find("input[name=roleId]").removeAttr("disabled");
            	if(row) {
            		tab.find("input[name=roleId]").val(row.roleId).attr("disabled", true);
            		tab.find("input[name=roleName]").val(row.roleName);
            		tab.find("textarea[name=roleDesc]").val(row.roleDesc);
            	}else {
            		tab.find("input[name=roleId]").val("");
            		tab.find("input[name=roleName]").val("");
            		tab.find("textarea[name=roleDesc]").val("");
            	}
            	self.initAuthTree(row);
            	$("#modal_sysrole_edit").modal("show");
            	$('#save_sysrole', this.o_container).unbind('click').bind('click', function(event){ 
            		self.saveSysRole(row);
                });
            },
            saveSysRole : function(row) {
            	var sysrole = {};
            	var tab = $("#tab_sysrole_info");
            	// roleId
            	sysrole.roleId = tab.find("input[name=roleId]").val();
            	if(!sysrole.roleId) {
            		bootbox.alert({message:'操作失败，编号不能为空',size:'small'});
            		return;
            	}
            	if(!/^[0-9a-zA-Z_]+$/g.test(sysrole.roleId)) {
            		bootbox.alert({message:'操作失败，编号必须为字母，数字，下划线组成',size:'small'});
            		return;
            	}
            	if(!validateStringLength(sysrole.roleId,32)){
            		bootbox.alert({message:'操作失败，编号的长度不能超过32',size:'small'}); 
            		return false;
            	}
            	// roleName
            	sysrole.roleName = tab.find("input[name=roleName]").val();
            	if(!validateStringLength(sysrole.roleName,100)){
            		bootbox.alert({message:'操作失败，名称的长度不能超过100',size:'small'}); 
            		return false;
            	}
            	// roleDesc
            	sysrole.roleDesc = tab.find("textarea[name=roleDesc]").val();
            	if(!validateStringLength(sysrole.roleDesc,500)){
            		bootbox.alert({message:'操作失败，描述的长度不能超过500',size:'small'}); 
            		return false;
            	}
            	// delFlag
            	sysrole.delFlag = "1";
            	// roleLevel
            	sysrole.roleLevel = "1";
            	// rolePid
            	sysrole.rolePid = "ADMIN";
            	// roleType
            	sysrole.roleType = "";
            	var selNodeArr = $('#authTree').fancytree("getTree").getSelectedNodes();
            	if(selNodeArr.length > 0){
            		var selKeyArr = [];
            		for(var i=0;i<selNodeArr.length;i++){
            			selKeyArr.push(selNodeArr[i].key);
            		}
            		sysrole.roleType = selKeyArr.join(",");
            	}
            	if(!mloadding.showLoadding()){
            		return false;
            	}
            	var url = row==null ? "sysRole/insert" : "sysRole/update";
            	$.ajax({
            		async:false,
        			url:url,
                    type:"POST",
                    processData:false,
                    data:JSON.stringify(sysrole),
                    contentType:'application/json',
        			success: function(data){
        				if(data.success){
        					bootbox.alert({message:'保存成功',size:'small'});
        					$('#tb_sysRole').bootstrapTable('refresh');
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	bootbox.alert({message:msgInfo, size:'middle'});
        				}
        				mloadding.hideLoadding();
        			},
        			error: function(msg){
        				bootbox.alert({message:'连接服务器错误',size:'small'});
        				mloadding.hideLoadding();
        			}
        		});
            	$("#modal_sysrole_edit").modal('hide');
            },
            deleteSysRole : function() {
            	var selected = $('#tb_sysRole').bootstrapTable('getSelections');
            	if(selected.length<1) {
            		bootbox.alert({message:'请选择需要删除的角色',size:'small'});
            		return;
            	}
            	var roleIds = [];
        		_.each(selected, function(element, index){
        			roleIds.push(selected[index].roleId);
        		});
            	var tConfirm = true;
        		bootbox.confirm({
            		size : 'small',
            		message : '是否确定删除所选角色？',
            		callback : function (result) {
            			if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			url : 'sysRole/delete',
                    			type : 'DELETE',
                    			dataType : 'json',
                    			data:{roleIds:roleIds.toString()},
                    			success: function(data){
                    				if(data.success){
                    					bootbox.alert({message:'删除成功',size:'small'});
                    					$('#tb_sysRole').bootstrapTable('refresh');
                    				}else{
                    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
                    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                    					}
                	                	bootbox.alert({message:msgInfo, size:'middle'});
                    				}
                    			},
                    			error: function(msg){
                    				bootbox.alert({message:'连接服务器错误',size:'small'});
                    			}
                    		});
                		}
                	 }
            	});
            },
            initAuthTree : function(row) {
            	var self = this;
            	/*var treeData = [
            		{title:'系统菜单', key:'nav.wws.root', selected:false, expanded:true, folder:true,
            			children:[
            				{title:'任务管理', key:'nav.wwsjob.mgr', selected:false, expanded:true, folder:true,
                    			children:[
                    				{title:'任务模板', key:'nav.wwsjob_template', selected:false, expanded:false},
                    				{title:'任务实例', key:'nav.wwsjob_instance', selected:false, expanded:false},
                    				{title:'计划任务', key:'nav.wwsjob_cron', selected:false, expanded:false},
                    				{title:'任务维度', key:'nav.wwsjob_dimension', selected:false, expanded:false},
                    				{title:'产品插件', key:'nav.wwsjob_agent_plus', selected:false, expanded:false},
                    				{title:'假日编排', key:'nav.wwsjob_workday', selected:false, expanded:false}
                    			]
                    		},
                    		{title:'数据源', key:'nav.wwsds', selected:false, expanded:true, folder:true,
                    			children:[
                    				{title:'Zookeeper', key:'nav.wws_ds_zookeeper', selected:false, expanded:false},
                    				{title:'DataSource', key:'nav.wws_ds_jdbc_ds', selected:false, expanded:false}
                    			]
                    		},
                    		{title:'任务批量', key:'nav.notifications', selected:false, expanded:true, folder:true,
                    			children:[
                    				{title:'批量任务', key:'nav.wws_batch_info', selected:false, expanded:false},
                    				{title:'批量进度', key:'nav.wws_batch_progress', selected:false, expanded:false},
                    				{title:'批量单一视图', key:'nav.wws_batch_sview', selected:false, expanded:false}
                    			]
                    		},
                    		{title:'系统管理', key:'nav.wwssys.mgr', selected:false, expanded:true, folder:true,
                    			children:[
                    				{title:'用户管理', key:'nav.wws_sys_user', selected:false, expanded:false},
                    				{title:'产品管理', key:'nav.wws_product', selected:false, expanded:false},
                    				{title:'角色管理', key:'nav.wws_sys_role', selected:false, expanded:false}
                    			]
                    		}
            			]
            		}
            	];*/
            	var treeData = [];
            	var tRoleId = "";
            	if(null != row){
            		tRoleId = row.roleId;
            	}
            	$.ajax({
            		async:false,
        			url : 'sysRole/get',
        			type : 'GET',
        			dataType : 'json',
        			data:{'roleId':tRoleId},
        			success: function(data){
        				if(data.success){
        					treeData = data.data.sysAuthList;
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	bootbox.alert({message:msgInfo, size:'middle'});
        				}
        			},
        			error: function(msg){
        				bootbox.alert({message:'连接服务器错误',size:'small'});
        			}
        		});
            	$('#authTree').fancytree({
                    checkbox:true,
                    selectMode:3,
                    source: treeData
    			});
            }
        });
        
        return app;
});