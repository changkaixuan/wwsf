requirejs.config({
			baseUrl : require.toUrl(""),
			paths : {
				'jquery'     : 'js/jquery-decorate',
				'mockjax'    : 'js/jquery.mockjax',
				'css'        : 'js/require.css-0.1.2',
				'text'       : 'js/require.text-2.0.12',
				'domReady'   : 'js/domReady-2.0.1',
				'underScore' : 'js/underscore-1.9.1',
				'jquery-jsoneditor' : 'js/jquery.json-editor',
				'jquery-jsonviewer' : 'js/jquery.json-viewer',
				'bootstrap-select'  : 'js/bootstrap/js/bootstrap-select',
				'bootstrap-table'   : 'js/bootstrap/js/bootstrap-table',
				'bootstrap-table-locale' : 'js/bootstrap/js/bootstrap-table-locale-all',
				'bootstrap-typeahead' : 'js/bootstrap/js/bootstrap-typeahead',
				'bootstrap-tagsinput' : 'js/bootstrap/js/bootstrap-tagsinput',
				'bootstrap-switch'    : 'js/bootstrap/js/bootstrap-switch',
				'mloadding'    : 'js/bootstrap/js/bootstrap-mloadding',
				'poshytip'     : 'js/jquery-editable/poshytip-1.2/src/jquery.poshytip',
				'x-editable'   : 'js/jquery-editable/js/jquery-editable-poshytip',
				'pubsub'       : 'js/pubsub',
				'ko'           : 'js/knockout-3.4.2',
				'skillbar'     : 'js/an-skill-bar',
				'app-editable' : 'js/app-param-editable',
				'dimension-editable' : 'js/app-dimension-editable',
				'icheck'    : 'js/icheck.min',
				'spinner'   : 'js/jquery.spinner.min',
				'moment'          : 'js/daterangepicker/moment',
				'daterangepicker' : 'js/daterangepicker/daterangepicker',
				'jsPlumb' : 'js/jsplumb/jsplumb',
				'jsPlumbToolkit' : 'js/jsplumb/jsplumbtoolkit',
				'jsPlumbToolkitUndoRedo' : 'js/jsplumb/jsplumbtoolkit-undo-redo',
				'jsPlumbToolkitHighlighter' : 'js/jsplumb/jsplumbtoolkit-syntax-highlighter',
				'expression' : 'js/expression',
				'echarts' : 'js/echarts-all',
				'fancytree' : 'js/fancytree/jquery.fancytree',
				'jqueryUI' : 'js/jquery-ui',
				'vis' : 'js/vis/vis',
				'select2' : 'js/select2.min',
				'cronjobtemtaskjobinstask-editable2' : 'js/app-cronjobtemtaskjobinstask-editable2',
				'xterm' : 'js/xterm/4.8.0/xterm',
				'xterm-addon-fit' : 'js/xterm/addon/xterm-addon-fit',
				'alertor' : 'js/alertor'
			},
			shim : {
				'underScore'     : { exports : '_' },
				'x-editable'     : { deps : [ 'poshytip' ] },
				'jsPlumb'        : { exports : 'jsPlumb' },
				'jsPlumbToolkit' : { exports : 'jsPlumbToolkit' },
				'xterm'          : { deps : ['css!css/xterm/4.8.0/xterm.css','xterm-addon-fit'] },
				'bootstrap-table-locale' : { deps : [ 'bootstrap-table' ] },
				'fancytree'      : { deps : ['jquery', 'jqueryUI'] }
			},
			waitSeconds : 30
		});

require(['domReady'], function(domReady) {
	domReady(function() {
		
		$.ajax({
    		async: false,
            url:'ssoCloseLogin',
            type:"get",
            processData:false,
            contentType:'application/json',
            success:function (data) {
                if (data.success){
                	$("#index_updateSysUserPwd_click").remove();
            		$("#index_loginOrLogout_click").remove();
            		$(".loginOrlogout").remove();
                }
            }
		});
		var navLinks = $("#js-primary-nav [data-filter-tags*='navLink']");
		navLinks.bind("click", 
			function(event) {
				var el = $(event.currentTarget);
				var data_module = el.attr("data-module");
				var data_target = el.attr("data-target");
				//点击执行逻辑
				if (data_module && data_target) {
					require([ "application/" + data_module ], function(ModuleClass) {
						var oModule = new ModuleClass();
						oModule.load();
						oModule.render(data_target);
						initApp.dateFill();
					});
				} else {
					console.log(el);
				}
			});
		
		$.ajax({
    		async: false,
            url:'getSessionMenu',
            type:"get",
            processData:false,
            contentType:'application/json',
            success:function (data) {
                if (data.success){
                	var userInfo = data.data.sysUser;
                	var isSSOLogin = data.data.isSSOLogin;
                	if (isSSOLogin == "1") {
                		$("#index_updateSysUserPwd_click").remove();
                		$("#index_loginOrLogout_click").remove();
                		$(".loginOrlogout").remove();
                	} else {
                		$("#index_loginOrLogout").html("退出登录");
                    	$(".loginOrlogout").attr("data-original-title", "退出登录");
                    	$(".loginOrlogout i").attr("class", "fal fa-sign-out");
                    	$("#index_updateSysUserPwd_click").show();
                    	$("#index_sysuser_cur_loginname").html(userInfo.loginName);
                	}
                	//左侧及右上登陆信息
                	$("#index_sysuser_cur_username_left").html(userInfo.userName);
            		$("#index_sysuser_cur_username").html(userInfo.userName);
            		$("#index_sysuser_cur_useremail").html(userInfo.userEmail);
            		$("#index_sysuser_cur_useremail_title").attr("title", (userInfo.userEmail == null ? "" : userInfo.userEmail));
            		
                	var sysAuthArr = data.data.sysAuthList;
                	if(sysAuthArr != undefined){
                		var menuArr = genMenuString(sysAuthArr,data.data.SysAuth_Root);
                		var menuString = "";
                		for(var i=0;i<menuArr.length;i++){
                			menuString += menuArr[i];
                		}
                		$("#js-nav-menu li:first").after(menuString);
                		//先销毁菜单样式，再创建
                		initApp.destroyNavigation("#js-nav-menu");
                		initApp.buildNavigation("#js-nav-menu");
                		//菜单加点击事件
                		var navLinks = $("#js-primary-nav [data-filter-tags*='navLink']");
                		navLinks.unbind("click").bind("click",function(event) {
                			var el = $(event.currentTarget);
                			var data_module = el.attr("data-module");
                			var data_target = el.attr("data-target");
                			//点击执行逻辑
                			if (data_module && data_target) {
                				require([ "application/" + data_module ], function(ModuleClass) {
                					var oModule = new ModuleClass();
                					oModule.load();
                					oModule.render(data_target);
                					initApp.dateFill();
                				});
                			}else{
                				console.log(el);
                			}
                		});
                	}
                	loadMessage();
                	$("#index_message").css("display","");
                	$("#sysLoginModal").modal("hide");
                }else{
                	$("#index_loginOrLogout").html("登录");
                	$(".loginOrlogout").attr("data-original-title", "登录");
                	$(".loginOrlogout i").attr("class", "fal fa-sign-in");
                	$("#index_updateSysUserPwd_click").hide();
                	if(data.info != undefined){
                		bootbox.alert({message:'登录失败.'+data.info,size:'small'});
                	}
                	$("#index_message").css("display","none");
                }
            },
            error:function(XMLHttpRequest,textStatus,errorThrown){
            	bootbox.alert({message:'服务器出错',size:'small'});
            }
        });
		
		$("#js-primary-nav [data-filter-tags*='navLink wws home']").click();

	});
	//登陆或登出事件
	$("#index_loginOrLogout_click").unbind("click").bind("click", function() {
		if($("#index_loginOrLogout").html() == "登录"){
			require([ "application/wws_sys_login" ], function(ModuleClass) {
				var oModule = new ModuleClass();
				oModule.load();
				oModule.render('#js-page-content');
			});
		}else{
			bootbox.confirm({
        		size : 'small',
        		message : '确定退出？',
        		callback : function (result) {
        			if(result){
        				$.ajax({
        	        		async: false,
        	                url:'logout',
        	                type:"get",
        	                processData:false,
        	                contentType:'application/json',
        	                success:function (data) {
        	                	window.location.reload(true);
        	                	$("#index_loginOrLogout").html("登录");
        	                	$(".loginOrlogout").attr("data-original-title", "登录");
        	                	$(".loginOrlogout i").attr("class", "fal fa-sign-in");
        	                	$("#index_updateSysUserPwd_click").hide();
        	                },
        	                error:function(XMLHttpRequest,textStatus,errorThrown){
        	                	bootbox.alert({message:'服务器出错',size:'small'});
        	                }
        	            });
        			}
           	    }
       	    });
		}
	});
	// 登录/退出登录图标
	$(".loginOrlogout").unbind("click").bind("click", function() {
		$("#index_loginOrLogout_click").click();
	});
	
	//修改密码
	$("#index_updateSysUserPwd_click").unbind("click").bind("click", function() {
		require([ "application/wws_update_password" ], function(ModuleClass) {
			var oModule = new ModuleClass();
			oModule.load();
			oModule.render('#js-page-content');
		});
	});
	
	//查看所有消息
	$("#index_all_message_click").unbind("click").bind("click", function() {
		require([ "application/wws_message" ], function(ModuleClass) {
			var oModule = new ModuleClass();
			oModule.load();
			oModule.render('#js-page-content');
		});
	});
	
	//session超时
	(function($){
		var _ajax = $.ajax; 
		$.ajax=function(opt){
			var fn={
					error:function(XMLHttpRequest,textStatus,errorThrown){},
					success:function(data,textStatus){}
			}
			if(opt.error){
				fn.error=opt.error;
			}
			if(opt.success){
				fn.success = opt.success;
			}
			//扩展增强处理
			var _opt = $.extend(opt,{
				error:function(XMLHttpRequest,textStatus,errorThrown){
					if(XMLHttpRequest.responseText=='sessionOut') {
						top.location.href = "index.html";
					}
					fn.error(XMLHttpRequest,textStatus,errorThrown);
				},
				success:function(data,textStatus,xhr){
					//debugger;
					if(data !='sessionOut'){
						fn.success(data,textStatus);
						return false;
					}else{
						top.location.href = "index.html";
					}
				}
			});
			return _ajax(opt);
		};
	})(jQuery);
	
});