define([
	'underScore', 
	'text!application/wws_sys_user.html',
	'app-editable',
	'alertor',
	'css!js/bootstrap/css/bootstrap-table.css', 
	'bootstrap-table',
	'bootstrap-table-locale',
	'css!js/jquery-editable/poshytip-1.2/src/tip-twitter/tip-twitter.css'],
    function (_, template, app_editable, alertor) {
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
	          	//查询按钮加事件
                $('#btn_sysuser_query', this.o_container).bind('click', function(event){ 
                	$('#tb_sysUser').bootstrapTable('refresh'); }
                );
                //重置按钮加事件
                $('#btn_sysuser_clear', this.o_container).bind('click', function(event){
                	$("input[name='qLoginName']").val("");
                	$("input[name='qUserName']").val("");
                	//$("#qDelFlag").val("");
                	$("#qLockFlag").val("");
                	$('#tb_sysUser').bootstrapTable('refresh');
                });
                $('#btn_sysuser_add', this.o_container).bind('click', function(event){ self.editSysUser(null); });
                $('#btn_sysuser_edit', this.o_container).bind('click', function(event){
                	var result = $('#tb_sysUser').bootstrapTable('getSelections');
                    if(result.length ==1){
                    	self.editSysUser(result[0]);
                    }else{
                    	alertor.dangerAlert('请选择一个需要修改的用户', 'small');
                    }
                });
                $('#btn_sysuser_del', this.o_container).bind('click', function(event){ self.delSysUser(event); });
                //初始化用户表格
                self.initSysUserTable();
            }, 
            initSysUserTable: function(){
            	var self = this;
            	$('#tb_sysUser').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'sysUser/list',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    toolbar: '#sysuser_tab_toolbar',                //工具按钮用哪个容器
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
                    	   	loginName: $("input[name='qLoginName']").val(),
                    	   	userName: $("input[name='qUserName']").val(),
                    	   	//userEmail: $("input[name='qUserEmail']").val(),
                    	   	//delFlag: $("#qDelFlag").val(),
                    	   	lockFlag: $("#qLockFlag").val()
                    	};
                    },           //传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "loginName",                     //每一行的唯一标识，一般为主键列
                    showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                   //是否显示父子表
                    showColumns: true,
                    showFullscreen: true,
                    showRefresh: true,
                    columns: [
                    	{checkbox: true}, 
                    	{field: 'loginName', title: '登录名'}, 
                        {field: 'userName', title: '姓名'}, 
                        {field: 'userMobile', title: '手机号码' },
                        {field: 'userEmail', title: '电子邮件' },
                        /*{field: 'delFlag', title: '账号状态', align: 'center',
                        	formatter: function (value, row, index) {
	                    		if(value == "0"){
	                    			return "删除";
	                    		}else{
	                    			return "正常";
	                    		}
					        }  
                        },*/
                        {field: 'lckFlg', title: '是否锁定', align: 'center',
                        	formatter: function (value, row, index) {
	                    		if(value == "0"){
	                    			return "<span class='badge badge-danger'>已锁定</span>";
	                    		}else{
	                    			return "<span class='badge badge-info'>未锁定</span>";
	                    		}
					        } 
                        },
                        {field: 'lockTime', title: '锁定时间', align: 'center' },
                        {field: 'validDate', title: '生效日期', align: 'center' },
                        {field: 'invalidDate', title: '失效日期', align: 'center' }
                    ],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    onDblClickRow: function (row) {
                    	self.editSysUser(row);
                    }
                });
            },
            editSysUser : function(row){
            	var self = this;
            	//初始化产品、角色
            	self.initSysUserRoleAndProduct();
            	var m = $('#sysUserEditModal');
            	m.find("input[name=loginName]").removeAttr("disabled");
            	m.modal('show');
            	var modalType = "add";
            	if(null == row){
            		m.find("input[type='text']").val(""); //模态框中所有输入框的值初始化为空
            	}else{
            		modalType = "edit";
            		$.ajax({
            			async:false,
            			data : {loginName:row.loginName},
            			url : 'sysUser/get',
            			type : 'GET',
            			contentType:'application/x-www-form-urlencoded',
            			success: function(data){
            				if(data.success){
        						var sysUser = data.data.sysUser;
        						m.find("input[name=loginName]").val(sysUser.loginName);
        						m.find("input[name=loginName]").attr("disabled",true);
        						m.find("input[name=userName]").val(sysUser.userName);
        						m.find("input[name=userMobile]").val(sysUser.userMobile);
        						m.find("input[name=userEmail]").val(sysUser.userEmail);
        						m.find("input[name=validDate]").val(sysUser.validDate);
        						m.find("input[name=invalidDate]").val(sysUser.invalidDate);
        						//角色
        						var sysRoleArr = data.data.sysRoleList;
        						if(sysRoleArr != undefined){
        							for(var i=0;i<sysRoleArr.length;i++){
        								$("#sysUserRoleRef").find("button[value="+sysRoleArr[i].roleId+"]").click();
        							}
        						}
        						//产品
        						var productArr = data.data.productList;
        						if(productArr != undefined){
        							for(var i=0;i<productArr.length;i++){
        								$("#sysUserProductRef").find("button[value="+productArr[i].pId+"]").click();
        							}
        						}
            				}else{
            					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
            					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
            						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
            					}
            					alertor.dangerAlert(msgInfo, 'middle');
            				}
            			},
            			error: function(msg){
            				alertor.dangerAlert('连接服务器错误', 'small');
            			}
            		});
            	}
            	$('#saveSysUser').unbind('click').bind('click', function(event){ 
            		self.saveSysUser(event, modalType); 
            	});
            },
            initSysUserRoleAndProduct : function(){
            	//1 清空 2 加载数据 3 按钮加事件
            	$("#sysUserRoleRef").empty();
            	$("#sysUserProductRef").empty();
            	$.ajax({
            		async:false,
        			url : 'sysUser/getRoleAndProduct',
        			type : 'GET',
        			contentType:'application/x-www-form-urlencoded',
        			success: function(data){
        				if(data.success){
        					var sysRoleArr = data.data.sysRoleList;
        					if(sysRoleArr != undefined){
    							for(var i=0;i<sysRoleArr.length;i++){
    								$("#sysUserRoleRef").append("<button type='button' style='line-height:1.2' class='btn btn-sm btn-default mb-1 mr-1 px-2' value='"+sysRoleArr[i].roleId+"'>"+sysRoleArr[i].roleName+"</button>");
    							}
    						}
    						var productArr = data.data.productList;
    						if(productArr != undefined){
    							for(var i=0;i<productArr.length;i++){
    								$("#sysUserProductRef").append("<button type='button' style='line-height:1.2' class='btn btn-sm btn-default mb-1 mr-1 px-2' value='"+productArr[i].pId+"'>"+productArr[i].pName+"</button>");
    							}
    						}
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	alertor.dangerAlert(msgInfo, 'middle');
        				}
        			},
        			error: function(msg){
        				alertor.dangerAlert('连接服务器错误', 'small');
        			}
        		});
            	//角色
            	$.each($("#sysUserRoleRef").find("button"),function(index,but){//循环加点击事件
                	$(but).unbind('click').bind('click', function(event){
                		//切换选中或未选中样式
                		var timeButClass = $(but).attr("class");
                		var regexp = RegExp("btn btn-sm btn-default mb-1 mr-1 px-2");
                		if(regexp.test(timeButClass)){
                			$(but).attr("class","btn btn-sm btn-danger mb-1 mr-1 px-2");
                		}else{
                			$(but).attr("class","btn btn-sm btn-default mb-1 mr-1 px-2");
                		}
                	});
            	});
            	//产品
            	$.each($("#sysUserProductRef").find("button"),function(index,but){//循环加点击事件
                	$(but).unbind('click').bind('click', function(event){
                		//切换选中或未选中样式
                		var timeButClass = $(but).attr("class");
                		var regexp = RegExp("btn btn-sm btn-default mb-1 mr-1 px-2");
                		if(regexp.test(timeButClass)){
                			$(but).attr("class","btn btn-sm btn-danger mb-1 mr-1 px-2");
                		}else{
                			$(but).attr("class","btn btn-sm btn-default mb-1 mr-1 px-2");
                		}
                	});
            	});
            },
            saveSysUser : function(event,modalType){
            	var self = this;
            	var m = $('#sysUserEditModal');
            	var formJson = {};
            	//loginName
        		formJson.loginName = m.find("input[name=loginName]").val();
        		if(null == formJson.loginName || formJson.loginName == ""){
                	alertor.dangerAlert('操作失败，登录名不能为空', 'small');
        			return false;
                }
        		if(!/^[0-9a-zA-Z_]+$/g.test(formJson.loginName)) {
            		alertor.dangerAlert('操作失败，登录名必须为字母，数字，下划线组成', 'small');
            		return;
            	}
        		if(!validateStringLength(formJson.loginName,100)){
            		alertor.dangerAlert('操作失败，登录名的长度不能超过100', 'small'); 
            		return false;
            	}
        		//userName
        		formJson.userName = m.find("input[name=userName]").val();
        		if(null == formJson.userName || formJson.userName == ""){
                	alertor.dangerAlert('操作失败，姓名不能为空', 'small');
        			return false;
                }
        		if(!validateStringLength(formJson.userName,100)){
            		alertor.dangerAlert('操作失败，姓名的长度不能超过100', 'small'); 
            		return false;
            	}
        		//userMobile
        		formJson.userMobile = m.find("input[name=userMobile]").val();
        		if(!isNumberWwsf(formJson.userMobile)){
            		alertor.dangerAlert('操作失败，手机只能输入数字', 'small');
        			return false;
            	}
        		if(!validateStringLength(formJson.userMobile,20)){
            		alertor.dangerAlert('操作失败，手机的长度不能超过20', 'small'); 
            		return false;
            	}
        		//userEmail
        		formJson.userEmail = m.find("input[name=userEmail]").val();
        		if(formJson.userEmail != null && formJson.userEmail != ""){
        			var emailReg = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/; 
        			if( !emailReg.test(formJson.userEmail) ){ 
        				alertor.dangerAlert('操作失败，邮箱格式错误', 'small'); 
        				return false; 
        			}
        		}
        		if(!validateStringLength(formJson.userEmail,50)){
            		alertor.dangerAlert('操作失败，邮箱的长度不能超过50', 'small'); 
            		return false;
            	}
        		//validDate
        		formJson.validDate = m.find("input[name=validDate]").val();
        		//invalidDate
        		formJson.invalidDate = m.find("input[name=invalidDate]").val();
        		//角色
        		formJson.sysRoleList = [];
            	$.each($("#sysUserRoleRef").find("button[class='btn btn-sm btn-danger mb-1 mr-1 px-2']"),function(index,but){
            		formJson.sysRoleList.push($(but).val());
            	});
            	//产品
        		formJson.productList = [];
            	$.each($("#sysUserProductRef").find("button[class='btn btn-sm btn-danger mb-1 mr-1 px-2']"),function(index,but){
            		formJson.productList.push($(but).val());
            	});
        		var url = "sysUser/insert";
        		if(modalType == "edit"){
        			url = "sysUser/update";
        		}
        		$.ajax({
        			url:url,
                    type:"POST",
                    processData:false,
                    data:JSON.stringify(formJson),
                    contentType:'application/json',
        			success: function(data){
        				if(data.success){
        					$('#sysUserEditModal').modal('hide');
        					alertor.successAlert('保存成功', 'small');
        					$('#tb_sysUser').bootstrapTable('refresh');
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	alertor.dangerAlert(msgInfo, 'middle');
        				}
        			},
        			error: function(msg){
        				alertor.dangerAlert('连接服务器错误', 'small');
        			}
        		});
            },
            delSysUser : function(event){
            	var selected = $('#tb_sysUser').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var loginNameArr = [];
            		_.each(selected, function(element, index){
            			loginNameArr.push(element.loginName);
            		});
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message :alertor.warningMessage('确定删除所选用户？'),
                		callback : function (result) {
                			if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			url : 'sysUser/delete/'+loginNameArr.join(','),
                        			type : 'DELETE',
                        			contentType:'application/json',
                        			success: function(data){
                        				if(data.success){
                        					alertor.successAlert('删除成功', 'small');
                        					$('#tb_sysUser').bootstrapTable('refresh');
                        				}else{
                        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
                        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                        					}
                    	                	alertor.dangerAlert(msgInfo, 'middle');
                        				}
                        			},
                        			error: function(msg){
                        				alertor.dangerAlert('连接服务器错误', 'small');
                        			}
                        		});
                    		}
                    	 }
                	});
            	}else{
            		alertor.dangerAlert('请选择需要删除的用户', 'small');
            	}
            }
        });
        return app;
});