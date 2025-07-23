/*define(['lib/jquery', 'lib/underscore', 'r/text!/application/wws_server.html', 'lib/bootbox', 
	'lib/bootstrap-select', 'lib/bootstrap-table', 'lib/x-editable', 'lib/typeahead', 'r/css!lib/bootstrap/css/bootstrap-tagsinput.css', 'lib/tagsinput'],
    function ($, _, template, bootbox,bs,bt,lx) {*/
define([
	'underScore', 
	'text!application/wws_server.html',
	'app-editable',
	'mloadding', 
	'xterm',
	'css!js/bootstrap/css/bootstrap-table.css', 
	'bootstrap-table',
	'bootstrap-table-locale',
	'bootstrap-tagsinput',
	'css!js/jquery-editable/poshytip-1.2/src/tip-twitter/tip-twitter.css'],
    function (_, template, app_editable, mloadding) {
        var app = function () {
            this.o_template = $(template);
            this.fal_agent_tag = "<i class='fal fa-tag fa-fw' style='margin-right:2px'></i>";
            this.webSocketClient = null;
            this.term = null;
            this.fitAddon = null;
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
                //加载查询条件产品下拉数据
                var qProdSelect = $("#serverQueryForm").find("select[name=qProduct]");
	          	$.ajax({
  	        		async: false,
  	    			url: 'product/selectProduct',
  	    			type: 'GET',
  	    			contentType: 'application/json',
  	    			success: function(data){
  	    				if(data.success){
    						var products = data.data.products;
    						qProdSelect.empty();
    						qProdSelect.append("<option value=''>请选择产品</option>");
    						$.each(products,function(i){
    							qProdSelect.append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
    						});
  	    				}else{
  	    					var msgInfo = '获取产品信息失败<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
        					self.errorAlert(msgInfo,'middle');
  	    				}
  	    			},
  	    			error: function(msg){
  	    				self.errorAlert('连接服务器错误','small');
  	    			}
	          	});
	          	//查询按钮事件
                $('#btn_server_query', this.o_container).bind('click', function(event){ $('#tb_server').bootstrapTable('refresh') });
                //重置按钮事件
	          	$('#btn_server_clear', this.o_container).bind('click', function(event){ 
	          		$("#serverQueryForm").find("select[name=qProduct]").val("");
	          		$("#serverQueryForm").find("input[name=qServerId]").val("");
	          		$("#serverQueryForm").find("input[name=qTags]").val("");
	          	});
	          	//新增事件
                $('#btn_server_add', this.o_container).bind('click', function(event){ self.edit(null); });
                //修改事件
                $('#btn_server_edit', this.o_container).bind('click', function(event){
                	var result = $('#tb_server').bootstrapTable('getSelections');
                    if(result.length ==1){
                    	self.edit(result[0]);
                    }else{
                    	self.errorAlert('请选择其中一台服务器','small');
                    }
                });
                //删除事件
                $('#btn_server_del', this.o_container).bind('click', function(event){ self.del(); });
                //安装事件
                $('#btn_server_install', this.o_container).bind('click', function(event){
                	var selected = $('#tb_server').bootstrapTable('getSelections');
                    if(selected.length ==1){
                		if(selected[0].status == "2" || selected[0].status == "3"){
                    		self.errorAlert('请勿重复安装,您可在卸载后重新安装','small');
                    	}else{
                    		self.toInstallModal(selected[0]);
                    	}
                    }else{
                    	self.errorAlert('请选择其中一台服务器','small');
                    }
                });
                //卸载事件
                $('#btn_server_uninstall', this.o_container).bind('click', function(event){
                	var selected = $('#tb_server').bootstrapTable('getSelections');
                    if(selected.length == 1){
                		if(selected[0].status != "2"){
                			self.errorAlert('无法卸载,服务器必须处于脱机状态','small');
                    	}else{
                    		self.uninstall(selected[0]);
                    	}
                    }else{
                    	self.errorAlert('请选择其中一台服务器','small');
                    }
                });
                //开始事件
                $('#btn_server_start', this.o_container).bind('click', function(event){ self.start(); });
                //停止事件
                $('#btn_server_stop', this.o_container).bind('click', function(event){ self.stop(); });
                //加载服务分页数据
                self.initServerTable();
                // 查看插件信息
                $('#btn_server_look', this.o_container).bind('click', function(event){ self.look(); });
                // 连karaf
                $('#btn_server_ssh_karaf', this.o_container).bind('click', function(event){
                	var selected = $('#tb_server').bootstrapTable('getSelections');
                    if(selected.length == 1){
                		if(selected[0].status != "3"){
                			self.errorAlert('操作失败,服务器必须处于联机状态','small');
                    	}else{
                    		self.sshShell(selected[0].product,selected[0].serverId,"karaf"); 
                    	}
                    }else{
                    	self.errorAlert('请选择其中一台服务器','small');
                    }
                });
                // 连shell
                $('#btn_server_ssh_shell', this.o_container).bind('click', function(event){
                	var selected = $('#tb_server').bootstrapTable('getSelections');
                    if(selected.length == 1){
                		if(selected[0].status != "2" && selected[0].status != "3"){
                    		self.errorAlert('操作失败,服务器必须处于脱机或者联机状态','small');
                    	}else{
                    		self.sshShell(selected[0].product,selected[0].serverId,"shell"); 
                    	}
                    }else{
                    	self.errorAlert('请选择其中一台服务器','small');
                    }
                });
            }, 
            initServerTable: function(){
            	var self = this;
            	$('#tb_server').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'server/list',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    toolbar: '#server_tab_toolbar',                //工具按钮用哪个容器
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
                    	   	product: $("#serverQueryForm").find("select[name=qProduct]").val(),
                	   		serverId: $("#serverQueryForm").find("input[name='qServerId']").val(),
                	   		tags: $("#serverQueryForm").find("input[name='qTags']").val()
                    	};
                    },           //传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "serverId",               //每一行的唯一标识，一般为主键列
                    showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                   //是否显示父子表
                    showColumns: true,
                    showFullscreen: true,
                    showRefresh: true,
                    columns: [
                    	{checkbox: true}, 
                    	{field: 'product', title: '产品'}, 
                        {field: 'serverId', title: '节点编号',
                    		formatter: function (value, row, index) {
                    			var retValue = value;
	                    		if(row.cfgVersion != row.installCfgVersion){
	                    			retValue += "&nbsp;<font color='red'><b>(*)</b></font>";
	                    		}
	                    		return retValue;
					        }
                    	},
                        {field: 'serverType', title: '节点类型', align: 'center',
                        	formatter: function (value, row, index) {
	                    		if(value == "slave"){
	                    			return "slave";
	                    		}else{
	                    			return "master";
	                    		}
					        }
                        },
                        {field: 'status', title: '状态', align: 'center',
                        	formatter: function (value, row, index) {
                        		if(value == "0"){
	                    			return "<span class='badge badge-secondary'>未安装</span>";
	                    		}else if(value == "1"){
	                    			return "<span class='badge badge-danger'>安装失败</span>";
	                    		}else if(value == "2"){
	                    			return "<span class='badge badge-dark'>脱机</span>";
	                    		}else if(value == "3"){
	                    			return "<span class='badge badge-success'>联机</span>";
	                    		}else{
	                    			return value;
	                    		}
					        } 
                        },
                        {title:'安装日志', edit:false, align:'center',
                        	events:{
                            	'click .tab_serverInstallLog_style': function(e, value, row, index) {
                            		self.showServerInstallLogInfoWindow(row.product, row.serverId);
                            	}
                            },
                        	formatter:function(value,row,rowIndex){  
                            	return '<button type="button" class="btn btn-xs btn-default px-1 tab_serverInstallLog_style">查看</button>';
                        	}
                        },
                        {field: 'ipAddr', title: '地址' },
                        {field: 'sshPort', title: '客户端接口' },
                        {field: 'rmiRegistryPort', title: 'RMI服务注册端口' },
                        {field: 'rmiServerPort', title: 'RMI服务器端口' },
                        {field: 'tags', title: '标签',
                        	formatter: function (value, row, index) {
	                    		if (value != null && value != '') {
	                    			var text = '';
	                    			var options = value.split(",");
		    		        		$.each(options,function(i){
		    		        			if (options[i] != null && options[i] != '') {
	    		        					text = text + "<span class='badge badge-success'>" + self.fal_agent_tag + options[i] + "</span><br/>";
		    		        			}
		    						})
		    						return text;
	                    		}
					        }},
                        {field: 'homeDir', title: '安装目录' },
                        {field: 'latestOnlineTime', title: '最近上线时间' },
                        {field: 'versionNo', title: '部署版本号', align:'center' },
                        {field: 'cfgVersion', title: '配置版本号', align:'center', visible:false },
                        {field: 'installCfgVersion', title: '安装配置版本号', align:'center', visible:false }
                        //{field: 'osSshUser', title: '系统SSH登陆用户名' },
                        //{field: 'osSshPswd', title: '系统SSH登陆密码' },
                        //{field: 'zooConnectStr', title: 'ZOO连接字符串' },
                        //{field: 'osSshPort', title: '系统SSH登陆端口' }
                    ],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    onDblClickRow: function (row) {
                    	self.edit(row);
                    }
                });
            },
            initIgnorePlugins :function(){
            	var self = this;
            	$("#ignorePlugins").empty();
            	$.ajax({//获取所有插件
            		async:false,
        			url : 'pluginAndAgent/selectPlugin',
        			type : 'GET',
        			contentType:'application/x-www-form-urlencoded',
        			success: function(data){
        				if(data.success){
        					var plugins = data.data.plugins;
        					if(plugins != undefined){
    							for(var i=0;i<plugins.length;i++){
    								$("#ignorePlugins").append("<button type='button' class='btn btn-sm btn-default mb-1 mr-1' value='"+plugins[i].name+"'>"+plugins[i].name+"</button>");
    							}
    						}
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	self.errorAlert(msgInfo,'middle');
        				}
        			},
        			error: function(msg){
        				self.errorAlert('连接服务器错误','small');
        			}
        		});
            	$.each($("#ignorePlugins").find("button"),function(index,but){//循环加点击事件
                	$(but).unbind('click').bind('click', function(event){
                		//切换选中或未选中样式
                		var timeButClass = $(but).attr("class");
                		var regexp = RegExp("btn btn-sm btn-default mb-1 mr-1");
                		if(regexp.test(timeButClass)){
                			$(but).attr("class","btn btn-sm btn-primary mb-1 mr-1");
                		}else{
                			$(but).attr("class","btn btn-sm btn-default mb-1 mr-1");
                		}
                	});
            	});
            },
            edit: function(row){
            	var self = this;
            	self.initIgnorePlugins();
            	var serverEditModal = $("#serverEditModal");
            	serverEditModal.find("select[name=product]").removeAttr("disabled");
            	serverEditModal.find("input[name=serverId]").removeAttr("disabled");
            	serverEditModal.modal('show');
            	//加载服务编辑Modal中产品下拉数据
                var eProdSelect = serverEditModal.find("select[name=product]");
	          	$.ajax({
  	        		async: false,
  	    			url: 'product/selectProduct',
  	    			type: 'GET',
  	    			contentType: 'application/json',
  	    			success: function(data){
  	    				if(data.success){
    						var products = data.data.products;
    						eProdSelect.empty();
    						eProdSelect.append("<option value=''>请选择产品</option>");
    						$.each(products,function(i){
    							eProdSelect.append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
    						});
  	    				}else{
  	    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	self.errorAlert(msgInfo,'middle');
  	    				}
  	    			},
  	    			error: function(msg){
  	    				self.errorAlert('连接服务器错误','small');
  	    			}
	          	});
	          	$('#server_edit_tags').tagsinput({style:'min-width:75%'});
          		$("#server_edit_tags").tagsinput("removeAll");
	          	if(null == row){
	          		serverEditModal.find("input").val("");
	          		serverEditModal.find("select").val("");
	          		serverEditModal.find("textarea").val("");
	          		$.ajax({
	          			async : false,
            			data : {},
            			url : 'server/getDefaultValue',
            			type : 'GET',
            			contentType:'application/x-www-form-urlencoded',
            			success: function(data){
            				if(data.success){
            					if (data.data.server) {
            						var server = data.data.server;
            						serverEditModal.find("input[name=sshPort]").val(server.sshPort);
            						serverEditModal.find("input[name=rmiRegistryPort]").val(server.rmiRegistryPort);
            						serverEditModal.find("input[name=rmiServerPort]").val(server.rmiServerPort);
            						serverEditModal.find("input[name=zooNamespace]").val(server.zooNamespace);
            						serverEditModal.find("input[name=zooConnectStr]").val(server.zooConnectStr);
            						serverEditModal.find("input[name=osSshPort]").val(server.osSshPort);
            					}
            				}else{
            					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
            					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
            						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
            					}
        	                	self.errorAlert(msgInfo,'middle');
            				}
            			},
            			error: function(msg){
            				self.errorAlert('连接服务器错误','small');
            			}
            		});	
	          	}else{
	          		$.ajax({
	          			async : false,
            			data : {product:row.product, serverId:row.serverId},
            			url : 'server/get',
            			type : 'GET',
            			contentType:'application/x-www-form-urlencoded',
            			success: function(data){
            				if(data.success){
            					//$("#server_edit_tags").tagsinput("removeAll");
            					var server = data.data.server;
            					serverEditModal.find("input[name=cfgVersion]").val(server.cfgVersion);
            					serverEditModal.find("select[name=product]").val(server.product);
            					serverEditModal.find("select[name=product]").attr("disabled",true);
            					serverEditModal.find("input[name=serverId]").val(server.serverId);
            					serverEditModal.find("input[name=serverId]").attr("disabled",true);
            					serverEditModal.find("textarea[name=serverDesc]").val(server.serverDesc);
            					serverEditModal.find("input[name=ipAddr]").val(server.ipAddr);
            					serverEditModal.find("input[name=sshPort]").val(server.sshPort);
            					serverEditModal.find("input[name=rmiRegistryPort]").val(server.rmiRegistryPort);
            					serverEditModal.find("input[name=rmiServerPort]").val(server.rmiServerPort);
            					serverEditModal.find("select[name=serverType]").val(server.serverType);
            					//标签
            					if(server.tags != undefined){
        			        		var tags = server.tags.split(",");
        			        		$.each(tags,function(i){
            			        		$('#server_edit_tags').tagsinput("add",tags[i]);
            			        	});
        			        	}
            					$('#server_edit_tags').tagsinput({style:'min-width:75%'});
            					serverEditModal.find("input[name=osSshUser]").val(server.osSshUser);
            					serverEditModal.find("input[name=osSshPswd]").val(server.osSshPswd);
            					serverEditModal.find("input[name=zooNamespace]").val(server.zooNamespace);
            					serverEditModal.find("input[name=zooConnectStr]").val(server.zooConnectStr);
            					serverEditModal.find("input[name=homeDir]").val(server.homeDir);
            					serverEditModal.find("input[name=osSshPort]").val(server.osSshPort);
            					//插件
            					if(server.ignorePlugins != undefined && server.ignorePlugins != ""){
            						var pluginArr = server.ignorePlugins.split(",");
            						for(var i=0;i<pluginArr.length;i++){
        								$("#ignorePlugins").find("button[value="+pluginArr[i]+"]").click();
        							}
            					}
            				}else{
            					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
            					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
            						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
            					}
        	                	self.errorAlert(msgInfo,'middle');
            				}
            			},
            			error: function(msg){
            				self.errorAlert('连接服务器错误','small');
            			}
            		});
	          	}
	          	$('#btn_server_save').unbind('click').bind('click', function(event){ self.save(row); });
            },
            save: function(row){
            	var self = this;
            	var f = $("#serverEditModal");
            	var formJson = {};
            	formJson.product = f.find("select[name=product]").val();
            	if(null == formJson.product || formJson.product == ""){
            		bootbox.alert({
            			message:'<div class="alert alert-danger mb-0 py-2">请选择产品</div>',
            			size:'small',
            			closeButton: false,
            			callback: function(result){
            				if (!result){
            					f.find("select[name=product]").focus();
            				}
            			}
            		});
        			return false;
            	}
            	formJson.serverId = f.find("input[name=serverId]").val();
            	if(null == formJson.serverId || formJson.serverId == ""){
            		bootbox.alert({
            			message:'<div class="alert alert-danger mb-0 py-2">节点编号不能为空</div>',
            			size:'small',
            			closeButton: false,
            			callback: function(result){
            				if (!result){
            					f.find("input[name=serverId]").focus();
            				}
            			}
            		});
        			return false;
            	}
            	var reg2 = /^[0-9a-zA-Z_]+$/;
            	if(!reg2.test(formJson.serverId)){
            		self.errorAlert('操作失败，节点编号只能为数字、字母或者下划线','small');
        			return;
        		}
            	if(!validateStringLength(formJson.serverId,50)){
            		self.errorAlert('操作失败，节点编号长度不能超过50','small');
            		return false;
            	}
            	formJson.serverDesc = f.find("textarea[name=serverDesc]").val();
            	if(!validateStringLength(formJson.serverDesc,500)){
            		self.errorAlert('操作失败，描述的长度不能超过500','small');
            		return false;
            	}
            	formJson.ipAddr = f.find("input[name=ipAddr]").val();
            	if(null == formJson.ipAddr || formJson.ipAddr == ""){
            		self.errorAlert('操作失败，地址不能为空','small');
        			return false;
            	}
            	if(!validateStringLength(formJson.ipAddr,100)){
            		self.errorAlert('操作失败，地址的长度不能超过100','small');
            		return false;
            	}
            	formJson.sshPort = f.find("input[name=sshPort]").val();
            	if(null == formJson.sshPort || formJson.sshPort == ""){
            		self.errorAlert('操作失败，客户端接口不能为空','small');
        			return false;
            	}
            	if(!self.isNumber(formJson.sshPort)){
            		self.errorAlert('操作失败，客户端接口只能输入数字','small');
        			return false;
            	}
            	if(!validateStringLength(formJson.sshPort,9)){
            		self.errorAlert('操作失败，客户端接口的长度不能超过9','small');
            		return false;
            	}
            	formJson.rmiRegistryPort = f.find("input[name=rmiRegistryPort]").val();
            	if(null == formJson.rmiRegistryPort || formJson.rmiRegistryPort == ""){
            		self.errorAlert('操作失败，jmx注册端口不能为空','small');
        			return false;
            	}
            	if(!self.isNumber(formJson.rmiRegistryPort)){
            		self.errorAlert('操作失败，jmx注册端口只能输入数字','small');
        			return false;
            	}
            	if(!validateStringLength(formJson.rmiRegistryPort,9)){
            		self.errorAlert('操作失败，jmx注册端口的长度不能超过9','small');
            		return false;
            	}
            	formJson.rmiServerPort = f.find("input[name=rmiServerPort]").val();
            	if(null == formJson.rmiServerPort || formJson.rmiServerPort == ""){
            		self.errorAlert('操作失败，rmi服务端口不能为空','small');
        			return false;
            	}
            	if(!self.isNumber(formJson.rmiServerPort)){
            		self.errorAlert('操作失败，rmi服务端口只能输入数字','small');
        			return false;
            	}
            	if(!validateStringLength(formJson.rmiServerPort,9)){
            		self.errorAlert('操作失败，rmi服务端口的长度不能超过9','small');
            		return false;
            	}
            	formJson.serverType = f.find("select[name=serverType]").val();
            	if(null == formJson.serverType || formJson.serverType == ""){
            		self.errorAlert('操作失败，请选择类型','small');
        			return false;
            	}
            	formJson.tags = f.find("input[name=tags]").val();
            	if(!validateStringLength(formJson.tags,500)){
            		self.errorAlert('操作失败，标签的长度不能超过500','small');
            		return false;
            	}
            	formJson.osSshUser = f.find("input[name=osSshUser]").val();
            	if(null == formJson.osSshUser || formJson.osSshUser == ""){
            		self.errorAlert('操作失败，系统SSH登陆用户名不能为空','small');
        			return false;
            	}
            	if(!validateStringLength(formJson.osSshUser,100)){
            		self.errorAlert('操作失败，系统SSH登陆用户名的长度不能超过100','small');
            		return false;
            	}
            	formJson.osSshPswd = f.find("input[name=osSshPswd]").val();
            	if(null == formJson.osSshPswd || formJson.osSshPswd == ""){
            		self.errorAlert('操作失败，系统SSH登陆密码不能为空','small');
        			return false;
            	}
            	if(!validateStringLength(formJson.osSshPswd,100)){
            		self.errorAlert('操作失败，系统SSH登陆密码的长度不能超过100','small');
            		return false;
            	}
            	formJson.zooNamespace = f.find("input[name=zooNamespace]").val();
            	if(null == formJson.zooNamespace || formJson.zooNamespace == ""){
            		self.errorAlert('操作失败，ZOO命名空间不能为空','small');
        			return false;
            	}
            	if(!validateStringLength(formJson.zooNamespace,200)){
            		self.errorAlert('操作失败，ZOO命名空间的长度不能超过200','small');
            		return false;
            	}
            	formJson.zooConnectStr = f.find("input[name=zooConnectStr]").val();
            	if(null == formJson.zooConnectStr || formJson.zooConnectStr == ""){
            		self.errorAlert('操作失败，ZOO连接字符串不能为空','small');
        			return false;
            	}
            	if(!validateStringLength(formJson.zooConnectStr,200)){
            		self.errorAlert('操作失败，ZOO连接字符串的长度不能超过200','small');
            		return false;
            	}
            	formJson.homeDir = f.find("input[name=homeDir]").val();
            	if(null == formJson.homeDir || formJson.homeDir == ""){
            		self.errorAlert('操作失败，安装目录不能为空','small');
        			return false;
            	}
            	if(!validateStringLength(formJson.homeDir,200)){
            		self.errorAlert('操作失败，安装目录的长度不能超过200','small');
            		return false;
            	}
            	formJson.osSshPort = f.find("input[name=osSshPort]").val();
            	if(null == formJson.osSshPort || formJson.osSshPort == ""){
            		self.errorAlert('操作失败，系统SSH登陆端口不能为空','small');
        			return false;
            	}
            	if(!validateStringLength(formJson.osSshPort,9)){
            		self.errorAlert('操作失败，系统SSH登陆端口的长度不能超过9','small');
            		return false;
            	}
            	var ignorePluginArr = [];
            	$.each($("#ignorePlugins").find("button[class='btn btn-sm btn-primary mb-1 mr-1']"),function(index,but){
            		ignorePluginArr.push($(but).val());
            	});
            	formJson.ignorePlugins = ignorePluginArr.toString();
            	if(!validateStringLength(formJson.ignorePlugins,500)){
            		self.errorAlert('操作失败，忽略插件的长度不能超过500','small');
            		return false;
            	}
            	var url = "server/save";
        		if(null != row){
        			formJson.cfgVersion = f.find("input[name=cfgVersion]").val();
        			url = "server/update";
        		}
        		$.ajax({
        			url:url,
                    type:"POST",
                    processData:false,
                    data:JSON.stringify(formJson),
                    contentType:'application/json',
        			success: function(data){
        				if(data.success){
        					$('#serverEditModal').modal('hide');
        					self.successAlert('保存成功','small');
        					$('#tb_server').bootstrapTable('refresh');
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	self.errorAlert(msgInfo,'middle');
        				}
        			},
        			error: function(msg){
        				self.errorAlert('连接服务器错误','small');
        			}
        		});
            },
            del: function(){
            	var self = this;
            	var selected = $('#tb_server').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var rows = [];
            		var errorInfo = "";
            		_.each(selected, function(element, index){
            			if(element.status == 3){
            				errorInfo = '操作失败，联机状态服务器不能被删除'+element.product+"/"+element.serverId;
            			}
            			rows.push(element);
            		});
            		if(errorInfo != ""){
            			self.errorAlert(errorInfo,'small');
            			return false;
            		}
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message:'<div class="alert alert-warning mb-0 py-2">是否确定删除所选服务器?<div>',
                		callback : function (result) {
                			if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			url : 'server/delete',
                        			type : 'DELETE',
                        			contentType:'application/json',
                        			data:JSON.stringify(rows),
                        			success: function(data){
                        				if(data.success){
                        					self.successAlert('删除成功','small');
                        					$('#tb_server').bootstrapTable('refresh');
                        				}else{
                        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
                        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                        					}
                    	                	self.errorAlert(msgInfo,'middle');
                        				}
                        			},
                        			error: function(msg){
                        				self.errorAlert('连接服务器错误','small');
                        			}
                        		});
                    		}
                    	 }
                	});
            	}else{
            		self.errorAlert('请选择非联机状态下的服务器','small');
            	}
            },
            toInstallModal : function(row){
            	var self = this;
            	var serverInstallModal = $("#serverInstallModal");
            	serverInstallModal.find("input[name=product]").val(row.product);
            	serverInstallModal.find("input[name=serverId]").val(row.serverId);
            	serverInstallModal.find("input[name=installCfgVersion]").val(row.installCfgVersion);
            	serverInstallModal.modal('show');
            	//加载服务器安装Modal中安装文件下拉数据
                var ifnSelect = serverInstallModal.find("select[name=installFileName]");
	          	$.ajax({
  	        		async: false,
  	    			url: 'server/getInstallFileName',
  	    			type: 'GET',
  	    			contentType: 'application/json',
  	    			success: function(data){
  	    				if(data.success){
    						var ifns = data.data.ifnList;
    						ifnSelect.empty();
    						ifnSelect.append("<option value=''>请选择安装文件</option>");
    						$.each(ifns,function(i){
    							ifnSelect.append("<option value='" + ifns[i] + "'>" + ifns[i] + "</option>");
    						});
    						if(data.data.newInstallFileName != undefined){
    							ifnSelect.val(data.data.newInstallFileName);
    						}
  	    				}else{
  	    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	self.errorAlert(msgInfo,'middle');
  	    				}
  	    			},
  	    			error: function(msg){
  	    				self.errorAlert('连接服务器错误','small');
  	    			}
	          	});
            	$('#btn_server_install_save').unbind('click').bind('click', function(event){ self.install(); });
            },
            install : function(){
            	var self = this;
            	var serverInstallModal = $("#serverInstallModal");
            	var installFileName = serverInstallModal.find("select[name=installFileName]").val();
            	if(null == installFileName || installFileName == ""){
            		self.errorAlert('操作失败，请选择安装文件','small');
        			return false;
            	}
            	var product = serverInstallModal.find("input[name=product]").val();
            	var serverId = serverInstallModal.find("input[name=serverId]").val();
            	var installCfgVersion = serverInstallModal.find("input[name=installCfgVersion]").val();
            	var tConfirm = true;
            	bootbox.confirm({
            		size : 'small',
            		message:'<div class="alert alert-warning mb-0 py-2">确定安装?<div>',
            		callback : function (result) {
            			if(result && tConfirm){
            				tConfirm = false;
            				mloadding.showLoadding();
                    		$.ajax({
                    			data : {product:product, serverId:serverId, installFileName:installFileName, installCfgVersion:installCfgVersion},
                    			url : 'server/install',
                    			type : 'get',
                    			contentType:'application/x-www-form-urlencoded',
                    			success: function(data){
                    				if(data.success){
                    					self.successAlert('安装成功','small');
                    					$("#serverInstallModal").modal("hide");
                    					$('#tb_server').bootstrapTable('refresh');
                    				}else{
                    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
                    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                    					}
                    					self.errorAlert(msgInfo,'middle');
                    				}
                    				mloadding.hideLoadding();
                    			},
                    			error: function(msg){
                    				self.errorAlert('连接服务器错误','small');
                    				mloadding.hideLoadding();
                    			}
                    		});
                		}
                	 }
            	});
            },
            uninstall : function(pRowData){
            	var self = this;
            	var parameterJson = {product:pRowData.product, serverId:pRowData.serverId};
            	var tConfirm = true;
            	bootbox.confirm({
            		size : 'small',
            		message : '<div class="alert alert-warning mb-0 py-2">确定卸载?<div>',
            		callback : function (result) {
            			if(result && tConfirm){
            				tConfirm = false;
            				mloadding.showLoadding();
                    		$.ajax({
                    			data : parameterJson,
                    			url : 'server/uninstall',
                    			type : 'get',
                    			contentType:'application/x-www-form-urlencoded',
                    			success: function(data){
                    				if(data.success){
                    					self.successAlert('卸载成功','small');
                    					$("#serverInstallModal").modal("hide");
                    					$('#tb_server').bootstrapTable('refresh');
                    				}else{
                    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
                    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                    					}
                	                	self.errorAlert(msgInfo,'middle');
                    				}
                    				mloadding.hideLoadding();
                    			},
                    			error: function(msg){
                    				self.errorAlert('连接服务器错误','small');
                    				mloadding.hideLoadding();
                    			}
                    		});
                		}
                	 }
            	});
            },
            start : function(){
            	var self = this;
            	var selected = $('#tb_server').bootstrapTable('getSelections');
            	if(selected.length == 1){
            		if(selected[0].status != "2"){
            			self.errorAlert('必须选择一台处于脱机状态的服务器','small');
                		return false;
                	}
            		var tConfirm = true;
                	bootbox.confirm({
                		size : 'small',
                		message:'<div class="alert alert-warning mb-0 py-2">确定联机?<div>',
                		callback : function (result) {
                			if(result && tConfirm){
                				tConfirm = false;
                				mloadding.showLoadding();
                        		$.ajax({
                        			data : {product:selected[0].product, serverId:selected[0].serverId},
                        			url : 'server/start',
                        			type : 'get',
                        			contentType:'application/x-www-form-urlencoded',
                        			success: function(data){
                        				if(data.success){
                        					$('#tb_server').bootstrapTable('refresh');
                        					self.successAlert('启动请求已提交<br>请刷新列表，查看节点最新状态','small');
                        				}else{
                        					var msgInfo = '提交请求失败,服务器处理出错<br>' + data.info;
                        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                        					}
                        					self.errorAlert(msgInfo,'middle');
                        				}
                        				mloadding.hideLoadding();
                        			},
                        			error: function(msg){
                        				self.errorAlert('连接服务器错误','small');
                        				mloadding.hideLoadding();
                        			}
                        		});
                    		}
                    	 }
                	});
            	}else{
            		self.errorAlert('必须选择一台处于脱机状态的服务器','small');
            	}
            },
            stop : function(){
            	var self = this;
            	var selected = $('#tb_server').bootstrapTable('getSelections');
            	if(selected.length == 1){
            		if(selected[0].status != "3"){
                		self.errorAlert('请选择一台处于联机状态的服务器','small');
                		return false;
                	}
            		var tConfirm = true;
                	bootbox.confirm({
                		size : 'small',
                		message : '<div class="alert alert-warning mb-0 py-2">确定脱机?<div>',
                		callback : function (result) {
                			if(result && tConfirm){
                				tConfirm = false;
                				mloadding.showLoadding();
                        		$.ajax({
                        			data : {product:selected[0].product, serverId:selected[0].serverId},
                        			url : 'server/stop',
                        			type : 'get',
                        			contentType:'application/x-www-form-urlencoded',
                        			success: function(data){
                        				if(data.success){
                        					$('#tb_server').bootstrapTable('refresh');
                        					self.successAlert('停止请求已提交<br>请刷新列表，查看节点最新状态','small');
                        				}else{
                        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
                        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                        					}
                    	                	self.errorAlert(msgInfo,'middle');
                        				}
                        				mloadding.hideLoadding();
                        			},
                        			error: function(msg){
                        				self.errorAlert('连接服务器错误','small');
                        				mloadding.hideLoadding();
                        			}
                        		});
                    		}
                    	 }
                	});
            	}else{
            		self.errorAlert('请选择一台处于联机状态的服务器','small');
            	}
            },
            showServerInstallLogInfoWindow: function(product,serverId){
            	var self = this;
                $.ajax({
                	async : false,
        			data : {product:product, serverId:serverId},
        			url : 'server/getServerInstallLogInfo',
        			type : 'GET',
        			contentType:'application/x-www-form-urlencoded',
        			success: function(data){
        				if(data.success){
        					if (data.data.installLog) {
        						var trinfo = data.data.installLog;
            					if (trinfo && trinfo != null && trinfo != '') {
            						$("#log_info_area").html($.trim(trinfo));
        	                    	$("#serverInstallLogInfoWindowModal").modal('show');
            					} else {
            						self.successAlert('没有安装日志','small');
            					}
        					} else {
        						self.successAlert('没有安装日志','small');
        					}
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	self.errorAlert(msgInfo,'middle');
        				}
                    },
                    error: function(msg){
                    	self.errorAlert('连接服务器错误','small');
        			}
                }); 
            },
            isNumber : function(value){//大于等于0
    			if(null != value && value != ""){
    				if(value == 0){
    					return true;
    				}
    				var exp = "^[0-9]+$"; 
    		    	var regExp = new RegExp(exp);
    		    	if(value.search(regExp) != -1){
    					var first = value.substring(0,1);
    					if(first < 1){
    						return false;
    					}
    		    	    return true; 
    		    	}
    			}
    	    	return false;
    	    },
    	    look : function() {
    	    	var self = this;
    	    	var selected = $('#tb_server').bootstrapTable('getSelections');
                if(selected.length == 1){
            		if(selected[0].status != "3"){
                		self.errorAlert('请选择一台处于联机状态的服务器','small');
                		return;
                	}
                }else{
                	self.errorAlert('请选择一台服务器','small');
                	return;
                }
                // 插件信息
                var pluginData = self.getPluginQueueInfo(selected[0]);
          		app_editable.initServerPluginInfoEditTable("table_serverPluginInfo",pluginData,"/tableServerPluginInfo","/updateCellTableServerPluginInfo");
          		selected[0].pluginData = pluginData;
	          	// 资源分配信息
	          	var	srInfo = self.getScheduleResourceInfo();
	          	app_editable.initSRInfoTable("table_srInfo",srInfo,"/tableServerSRInfo","/updateCellTableServerSRInfo");
	        	var siqrInfo = self.getScheduleInitQueueResourceInfo();
	        	app_editable.initSIQRInfoTable("table_siqrInfo",siqrInfo,"/tableServerSIQRInfo","/updateCellTableServerSIQRInfo");
	          	
	          	// 保存按钮
	          	$("#btn_save_pluginqueue").unbind("click").bind("click", function() {
	          		self.updatePluginQueueInfo(selected[0]);
	          	});
	          	// 刷新按钮
	          	$(".btn_refresh_pluginqueue").unbind("click").bind("click", function() {
          			pluginData = self.getPluginQueueInfo(selected[0]);
	          		app_editable.initServerPluginInfoEditTable("table_serverPluginInfo",pluginData,"/tableServerPluginInfo","/updateCellTableServerPluginInfo");
	          		selected[0].pluginData = pluginData;
		          	srInfo = self.getScheduleResourceInfo();
		          	app_editable.initSRInfoTable("table_srInfo",srInfo,"/tableServerSRInfo","/updateCellTableServerSRInfo");
		          	siqrInfo = self.getScheduleInitQueueResourceInfo();
		        	app_editable.initSIQRInfoTable("table_siqrInfo",siqrInfo,"/tableServerSIQRInfo","/updateCellTableServerSIQRInfo");
	          	});
	          	$("#serverPluginQueueModal").modal("show");
    	    },
    	    sshShell : function(product,serverId,connType) {
    	    	var self = this;
    	    	var tModal = $("#sshShellModal");
    	    	var Terminal = require('xterm').Terminal; //异步
    	    	var FitAddon = require('xterm-addon-fit').FitAddon;
    	    	var websocketAddr = "";
    	    	var sshConnInfo = null;
    	    	$.ajax({
          			async : false,
        			data : {product:product, serverId:serverId, connType:connType},
        			url : 'server/getWebsocketAddr',
        			type : 'GET',
        			contentType:'application/x-www-form-urlencoded',
        			success: function(data){
        				if(data.success){
        					websocketAddr = data.data.websocketAddr;
        					sshConnInfo = data.data.sshConnInfo;
        					$("#displayIpAddr")[0].innerHTML = connType+" | "+sshConnInfo.ipAddr;
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	self.errorAlert(msgInfo,'middle');
        				}
        			},
        			error: function(msg){
        				self.errorAlert('连接服务器错误','small');
        			}
        		});
    	    	if(sshConnInfo != null){
	    	    	//延迟1秒执行self.createWebSocketClient();
	    	    	setTimeout(function(){
	    	    		self.createWebSocketClient(Terminal,FitAddon,websocketAddr,sshConnInfo,connType);
	    	    	}, 500); 
	    	    	//显示窗口
	    	    	tModal.modal({
	    	    		backdrop: "static",
	    	    		show: true
	    	    	});
	    	    	//添加model隐藏事件（model隐藏，关闭springboot/session）
	    	    	tModal.off().on("hidden.bs.modal",function(){
	    	    		if(webSocketClient.readyState == webSocketClient.OPEN){
	    	    			webSocketClient.send(JSON.stringify({"type":"close","command":"close session"}));
	    	    			webSocketClient = null;
	    	    			if(term){
	    	    				term.dispose();
	    	    			}
	    	    		}
	            	});
	    	    	//添加重新连接事件
	    	    	$('#displayReconnect').unbind('click').bind('click', function(event){
	    	    		if(webSocketClient.readyState == webSocketClient.OPEN){
		    	    		webSocketClient.send(JSON.stringify({"type":"close","command":"close session"}));
			    			webSocketClient = null;
	    	    		}
		    	    	self.sshShell(product,serverId,connType);
	    	    	});
	    	    	//添加全屏事件
	    	    	var displaySelectClass = "<i class='fal fa-check-square fa-fw'></i>";
	    	    	var displayNoeSelectClass = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	    	    	$('#displayFullscreen').unbind('click').bind('click', function(event){
	    	    		var labelFullscreen = $("#sshShellModal").find("label[class="+this.id+"]")[0];
	    	    		var isFullScreen = document.fullScreen || document.mozFullScreen || document.webkitIsFullScreen;
    	    			if(!isFullScreen){
    	    				var el = $("#sshShellModalContent")[0];
    	    				if(el.requestFullscreen){
    	    					el.requestFullscreen();
	    	    			}else if(de.mozRequestFullscreen){
	    	    				el.mozRequestFullscreen();
	    	    			}else if(de.webkitRequestFullscreen){
	    	    				el.webkitRequestFullscreen();
	    	    			}else if(de.msRequestFullscreen){
	    	    				el.msRequestFullscreen();
	    	    			}else{
	    	    				self.errorAlert('当前浏览器不支持全屏','small');
	    	    			}
    	    			}else{
    	    				if(document.exitFullscreen){
	    	    				document.exitFullscreen();
	    	    			}else if(document.mozCancelFullscreen){
	    	    				document.mozCancelFullscreen();
	    	    			}else if(document.webkitExitFullscreen){
	    	    				document.webkitExitFullscreen();
	    	    			}else{
	    	    				self.errorAlert('退出全屏失败','small');
	    	    			}
    	    			}
	    	    	});
	    	    	//监听屏幕变化（按键盘ESC键 和 点击"modal/View/Fullscreen"，都可以退出全屏）
	    	    	window.onresize = function(){
	    	    		var labelFullscreen = $("#sshShellModal").find("label[class=displayFullscreen]")[0];
	    	    		var isFullScreen = document.fullScreen || document.mozFullScreen || document.webkitIsFullScreen;
    	    			if(!isFullScreen){//全屏变非全屏
    	    				labelFullscreen.innerHTML = displayNoeSelectClass;
    	    				$("#shellCommandWindow").attr("style","height:432px;");
    	    				setTimeout(function(){
        	    				fitAddon.fit();
        	    				term.focus();
        	    	    	}, 100);
    	    			}else{
    	    				labelFullscreen.innerHTML = displaySelectClass;
    	    				$("#shellCommandWindow").attr("style","height:"+(window.screen.availHeight-24)+"px;"); 
    	    				setTimeout(function(){
        	    				fitAddon.fit();
        	    				term.focus();
        	    	    	}, 100);
    	    			}
	    	    	}
	    	    	//默认不全屏
	    	    	$("#sshShellModal").find("label[class=displayFullscreen]")[0].innerHTML = displayNoeSelectClass;
	    	    	//添加字符编码事件
	    	    	var displayEncoding = "displayEncoding"; //displayEncoding打头
	    	    	var btnEncodingArr = $("#sshShellModal").find("a[id^="+displayEncoding+"]");
	    	    	for(var a=0;a<btnEncodingArr.length;a++){
	    	    		$('#'+btnEncodingArr[a].id).unbind('click').bind('click', function(event){
	    	    			if(webSocketClient.readyState == webSocketClient.OPEN){
		        	    		var btnId = this.id;
		        	    		var encoding = btnId.replace(displayEncoding,"");
		        	    		var labelEncodingArr = $("#sshShellModal").find("label[class^="+displayEncoding+"]");
		        	    		for(var i=0;i<labelEncodingArr.length;i++){
		        	    			if(btnId == labelEncodingArr[i].className){
		        	    				labelEncodingArr[i].innerHTML = displaySelectClass;
		        	    			}else{
		        	    				labelEncodingArr[i].innerHTML = displayNoeSelectClass;
		        	    			}
		        	    		}
		        	    		webSocketClient.send(JSON.stringify({"type":"encoding","command":encoding}));
		        	    		term.focus();
	        	    		}
	                    });
	    	    	}
	    	    	//选中默认编码
    	    		var labelEncodingArr = $("#sshShellModal").find("label[class^="+displayEncoding+"]");
    	    		for(var i=0;i<labelEncodingArr.length;i++){
    	    			if((displayEncoding + sshConnInfo.charsetName) == labelEncodingArr[i].className){
    	    				labelEncodingArr[i].innerHTML = displaySelectClass;
    	    			}else{
    	    				labelEncodingArr[i].innerHTML = displayNoeSelectClass;
    	    			}
    	    		}
    	    	}
    	    },
    	    createWebSocketClient : function(Terminal,FitAddon,websocketAddr,sshConnInfo,connType){
    	    	var self = this;
    	    	var shellDiv = $("#shellCommandWindow");
    	        if (typeof (WebSocket) === "undefined") {
    	            self.errorAlert('您的浏览器不支持建立通道','small');
    	        } else {
    	            //指定要连接的服务器地址与端口建立连接
    	            //注意ws、wss使用不同的端口。我使用自签名的证书测试，
    	            //无法使用wss，浏览器打开WebSocket时报错
    	            //ws对应http、wss对应https。
    	        	webSocketClient = new WebSocket("ws://"+websocketAddr+encodeURIComponent(encodeURIComponent(JSON.stringify(sshConnInfo))));
    	            //连接打开事件
    	            webSocketClient.onopen = function() {
    	            	term = new Terminal({
    	                	//cols: Math.floor(shellDiv.width() / 9.25),
    	                    //rows: Math.floor(shellDiv.height() / 17.85),
    	                    cursorStyle: 'block', //block | underline | bar
    	                    rendererType: 'canvas', //'dom' | 'canvas'
    	                    screenKeys: false,
    	                    useStyle: true,
    	                    cursorBlink: true,
    	                    convertEol: true,
    	                    theme:{
    	                    	foreground: 'white', //字体
    	                    	background: '#060101' //背景色
    	                    },
    	                    fontSize: 15,
    	                    fontFamily: "courier-new, courier, monospace",
    	                    lineHeight: 1.06
    	                });
    	                term.open(shellDiv.empty()[0]);
    	                fitAddon = new FitAddon();
    	            	term.loadAddon(fitAddon);
    	            	fitAddon.fit();
    	    	        var copyText = "";
    	    	        term.onKey(e => { //e: 指方法onKey的一个json参数; 值格式：{key:'',domEvent:KeyboardEvent}
    	    	        	if(webSocketClient.readyState === webSocketClient.OPEN){ //防止连接关闭，页面报js错误
    		    	            if(e.domEvent.ctrlKey && e.domEvent.code == 'KeyC') {//ctrl+c
    		    	            	copyText = term.getSelection();
    		    	            	if(copyText == null || copyText == ""){
    		    	            		webSocketClient.send(JSON.stringify({"type":"command","command":e.key}));
    		    	            	}
    		    	            }else if(e.domEvent.ctrlKey && e.domEvent.code == 'KeyV') {//ctrl+v
    		    	            	if(copyText != null && copyText != ""){
    		    	            		webSocketClient.send(JSON.stringify({"type":"command","command":copyText}));
    		    	            	}
    		    	            }else if(e.domEvent.code.startsWith("F")){
    		    	            	if(e.domEvent.altKey){
    		    	            		webSocketClient.send(JSON.stringify({"type":"command","command":e.key}));
    		    	            	}
    		    	            }else{
    		    	            	webSocketClient.send(JSON.stringify({"type":"command","command":e.key}));
    		    	            }
    	    	        	}
    	    	        });
    	    			term.focus();
    	            };
    	            //收到消息事件
    	            webSocketClient.onmessage = function(evn) {
    	            	term.write(evn.data);
    	            };
    	            //连接关闭事件
    	            webSocketClient.onclose = function() {
    	            	term.write("connect fail、 command close connect or session timeout causes connect closed.");
    	            	term.reset();
    	            };
    	            //发生了错误事件
    	            webSocketClient.onerror = function() {
    	            	self.errorAlert('连接服务器出错','small');
    	            };
    	        }
    	    },
    	    getPluginQueueInfo : function(row) {
    	    	var self = this;
    	    	var arr = [];
    	    	 $.ajax({
                 	async : false,
         			data : {nodeId:row.serverId, product:row.product},
         			url : 'server/getPluginQueueInfo',
         			type : 'GET',
         			contentType:'application/x-www-form-urlencoded',
         			success: function(data){
         				if(data.success){
         					arr = data.data.rows;
         				}else{
         					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	self.errorAlert(msgInfo,'middle');
         				}
                     },
                     error: function(msg){
                    	 self.errorAlert('连接服务器错误','small');
         			 }
                 }); 
    	    	return arr;
    	    },
    	    updatePluginQueueInfo : function(row) {
    	    	var self = this;
    	    	if(row.pluginData.length==0) {
    	    		$("#serverPluginQueueModal").modal("hide");
    	    		return true;
    	    	}
    	    	var nameArr = [];
    	    	var coreSizeArr = [];
    			var data = $('#table_serverPluginInfo').bootstrapTable("getData");
    			for(var i=0, len=data.length; i<len; i++) {
    				if(!/^((\d)|([1-9]\d+))$/g.test(data[i].coreSize)) {
    					self.errorAlert('插件 ['+data[i].name+'] 的线程数量应为数字，且不以0开头','small');
    					return false;
    				}
    				if(data[i].name==row.pluginData[i].name && (data[i].coreSize!=row.pluginData[i].coreSize)) {
    					nameArr.push(data[i].name);
    					coreSizeArr.push(data[i].coreSize);
    				}
    			}
    			if(nameArr.length>0) {
    				 $.ajax({
	                 	async : false,
	         			data : {product:row.product, nodeId:row.serverId, names:nameArr.join(","), coreSizes:coreSizeArr.join(",")},
	         			url : 'server/updatePluginPoolSize',
	         			type : 'GET',
	         			contentType:'application/x-www-form-urlencoded',
	         			success: function(data){
	         				if(data.success){
	         					self.successAlert('保存成功','small');
	         				}else{
	         					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
	        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
	        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
	        					}
	    	                	self.errorAlert(msgInfo,'middle');
	         					return false;
	         				}
	                     },
	                     error: function(msg){
	                    	self.errorAlert('连接服务器错误','small');
	         				return false;
	         			}
    				 }); 
    			}
    			$(".btn_refresh_pluginqueue").click();
    			return true;
    	    },
    	    getScheduleResourceInfo : function() {
    	    	var self = this;
    	    	var arr = [];
	   	    	 $.ajax({
	                	async : false,
	        			url : 'server/getScheduleResourceInfo',
	        			type : 'GET',
	        			contentType:'application/x-www-form-urlencoded',
	        			success: function(data){
	        				if(data.success){
	        					arr = data.data.rows;
	        				}else{
	        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
	        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
	        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
	        					}
	    	                	self.errorAlert(msgInfo,'middle');
	        				}
	                    },
	                    error: function(msg){
	                    	self.errorAlert('连接服务器错误','small');
	        			}
	                }); 
	   	    	 return arr;
    	    },
    	    getScheduleInitQueueResourceInfo : function() {
    	    	var self = this;
    	    	var arr = [];
	   	    	 $.ajax({
	                	async : false,
	        			url : 'server/getScheduleInitQueueResourceInfo',
	        			type : 'GET',
	        			contentType:'application/x-www-form-urlencoded',
	        			success: function(data){
	        				if(data.success){
	        					arr = data.data.rows;
	        				}else{
	        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
	        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
	        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
	        					}
	    	                	self.errorAlert(msgInfo,'middle');
	        				}
	                    },
	                    error: function(msg){
	                    	self.errorAlert('连接服务器错误','small');
	        			}
	                }); 
	   	    	 return arr;
    	    },
    	    errorAlert : function(msg, size) {
    	    	bootbox.alert({
					message:'<div class="alert alert-danger mb-0 py-2">'+msg+'<div>', 
					closeButton: false,
					size:size
				});
    	    },
    	    successAlert : function(msg, size) {
    	    	bootbox.alert({
					message:'<div class="alert alert-success mb-0 py-2">'+msg+'<div>', 
					closeButton: false,
					size:size
				});
    	    }
        });
        return app;
});