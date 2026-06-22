define([
	'underScore', 
	'text!application/wws_sys_login.html',
	'mloadding',
	'css!js/bootstrap/css/bootstrap-table.css', 
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
                var loginModal = $("#sysLoginModal");
                if(loginModal.length==0){
                	o_container.append(this.o_template);
                	loginModal = $("#sysLoginModal");
                	loginModal.on('shown.bs.modal', function(e){$("#sysLoginModal").find("input[name=userName]").focus();});
                }
                $('#btn_syslogin_login').unbind('click').bind('click', function(event){
                	if(!mloadding.showLoadding()){
                		return false;
                	}
                	var submitFlag = true;
                	var login = {};
                	login.userName = $("#sysLoginModal").find("input[name=userName]").val();
                	if(null == login.userName || login.userName == ""){
                		bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">用户名不能为空</div>',
                			size:'small',
                			closeButton: false,
                			callback: function(result){
                				if (!result){
                					$("#sysLoginModal").find("input[name=userName]").focus();
                				}
                			}
                		});
                		submitFlag = false;
                	}
                	login.password = $("#sysLoginModal").find("input[name=password]").val();
                	if(submitFlag && (null == login.password || login.password == "")){
                		bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">密码不能为空</div>',
                			size:'small',
                			closeButton: false,
                			callback: function(result){
                				if (!result){
                					$("#sysLoginModal").find("input[name=password]").focus();
                				}
                			}
                		});
                		submitFlag = false;
                	}
                	if(!submitFlag){
                    	mloadding.hideLoadding();
                    	return submitFlag;
                    }
                	$.ajax({
                		async: false,
                        url:'login',
                        type:"POST",
                        processData:false,
                        data:JSON.stringify(login),
                        contentType:'application/json',
                        success:function (data) {
                            if (data.success){
                            	$("#index_loginOrLogout").html("退出登录");
                            	$(".loginOrlogout").attr("data-original-title", "退出登录");
                            	$(".loginOrlogout i").attr("class", "fal fa-sign-out");
                            	$("#index_updateSysUserPwd_click").show();

                            	var userInfo = data.data.sysUser;
                            	$("#index_sysuser_cur_loginname").html(userInfo.loginName);
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
	                        			}
	                        		});
	                        	}
	                        	loadMessage();
	                        	$("#index_message").css("display","");
	                        	$("#sysLoginModal").modal("hide");
	                        	
	                        	
                            }else{
                        		bootbox.alert({
                        			message:'<div class="alert alert-danger mb-0 py-2">登录失败.' + (data.info ? data.info : '') +'</div>',
                        			size:'small',
                        			closeButton: false,
                        			callback: function(result){
                        				if (!result){
                        					$("#sysLoginModal").find("input[name=userName]").focus();
                        				}
                        			}
                        		});
                            	$("#index_message").css("display","none");
                            }
                            mloadding.hideLoadding();
                        },
                        error:function(XMLHttpRequest,textStatus,errorThrown){
                        	bootbox.alert({message:'服务器出错',size:'small'});
                        	mloadding.hideLoadding();
                        }
                    });
                });
                $('#btn_syslogin_reset').unbind('click').bind('click', function(event){
                	$("#sysLoginModal").find("input[name=userName]").val("");
                	$("#sysLoginModal").find("input[name=password]").val("");
                });
                loginModal.modal("show");
                //回车自动点击登录
                $("#sysLoginModal").on("keydown",function(e){
                	if (e.keyCode == 13){
                		$("#btn_syslogin_login").click();
                	}
                });
            }
        });
        return app;
});