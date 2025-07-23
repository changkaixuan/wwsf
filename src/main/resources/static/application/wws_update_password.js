define([
	'underScore', 
	'text!application/wws_update_password.html',
	'css!js/bootstrap/css/bootstrap-table.css', 
	'bootstrap-table'],
    function (_, template) {
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
                var updatePwdModal = $("#updatePasswordModal");
                if(updatePwdModal.length==0){
                	o_container.append(this.o_template);
                	updatePwdModal = $("#updatePasswordModal");
                	updatePwdModal.on('shown.bs.modal', function(e){$('input[name=oldPwd]').focus();});
                }
                $('#btn_updatepassword_save').unbind('click').bind('click', function(event){
                	var sysUser = {};
                	sysUser.oldUserPwd = $("#updatePasswordModal").find("input[name=oldPwd]").val();
                	if(null == sysUser.oldUserPwd || sysUser.oldUserPwd == ""){
                		bootbox.alert({message:'旧密码不能为空',size:'small'});
            			return false;
                	}
                	sysUser.userPwd = $("#updatePasswordModal").find("input[name=newPwd]").val();
                	if(null == sysUser.userPwd || sysUser.userPwd == ""){
                		bootbox.alert({message:'新密码不能为空',size:'small'});
            			return false;
                	}
                	var affirmNewPwd = $("#updatePasswordModal").find("input[name=affirmNewPwd]").val();
                	if(null == affirmNewPwd || affirmNewPwd == ""){
                		bootbox.alert({message:'确认密码不能为空',size:'small'});
            			return false;
                	}
                	if(sysUser.userPwd != affirmNewPwd){
                		bootbox.alert({message:'新密码与确认密码不一致',size:'small'});
            			return false;
                	}
                	$.ajax({
                		async: false,
                        url:'sysUser/updatePassword',
                        type:"POST",
                        processData:false,
                        data:JSON.stringify(sysUser),
                        contentType:'application/json',
                        success:function (data) {
                            if (data.success){
                            	bootbox.alert({message:'保存成功.',size:'small'});
                            	updatePwdModal.modal("hide");
                            }else{
                            	var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
            					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
            						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
            					}
        	                	bootbox.alert({message:msgInfo, size:'middle'});
                            }
                        },
                        error:function(XMLHttpRequest,textStatus,errorThrown){
                        	bootbox.alert({message:'服务器出错',size:'small'});
                        }
                    });
                });
                $('#btn_updatepassword_reset').unbind('click').bind('click', function(event){
                	$("#updatePasswordModal").find("input[name=oldPwd]").val("");
                	$("#updatePasswordModal").find("input[name=newPwd]").val("");
                	$("#updatePasswordModal").find("input[name=affirmNewPwd]").val("");
                });
                $('#btn_updatepassword_reset').click();
                updatePwdModal.modal("show");
            }
        });
        return app;
});