define([
    'underScore',
    'text!application/wws_product_parameter.html',
    'mloadding',
    'ko',
    'alertor',
    'bootstrap-table',
    'bootstrap-switch',
    'bootstrap-typeahead',
    'bootstrap-tagsinput',
    'x-editable', 'mockjax', 'icheck', 'spinner' ], function(_, template, mloadding, ko, alertor) {
    var app = function(jmodule, jrow, id) {
        this.jmodule = jmodule;
        this.jrow = jrow;
        this.elId = id;
        this.el = $(template);
    };
    var result = [];
    _.extend(app.prototype, {
        load : function() {

        },
        render : function(container) {
            var self = this;
            var o_container = $(container);
            o_container.empty();
            o_container.append(self.el);
            var vm_tab = {
                elid : self.elId
            }
            ko.applyBindings(vm_tab, o_container.get(0));

            self.initTable(self.jrow);

            var product_param_Table = $('#' + self.elId + '_tb_product_param_table');

            $('#' + self.elId + '_btn_product_param_add').unbind('click');
            $('#' + self.elId + '_btn_product_param_add').bind('click',function () {
                self.edit_product_parameter(self, self.elId, null);
            });

            var btn_del = $('#' + self.elId + '_btn_product_param_delete');
            btn_del.unbind('click');
            btn_del.bind('click',function () {
                var selected = product_param_Table.bootstrapTable('getSelections');
                if(selected.length<1) {
                	alertor.dangerAlert('请选择需要删除的参数','small');
                    return;
                }
                var productparamsArr = [];
                $.each(selected,function(i){
                    productparamsArr.push(selected[i]);
                });
                var tConfirm = true;
                bootbox.confirm({
                    size : 'small',
                    callback : function (result) {
                        if(result && tConfirm){
                            tConfirm = false;
                            $.ajax({
                                data :JSON.stringify(productparamsArr),
                                url : 'product/deletecertification',
                                type : 'POST',
                                contentType:'application/json',
                                success: function(data){
                                    if(data.success){
                                    	alertor.successAlert('删除成功','small');
                                        product_param_Table.bootstrapTable('refresh');
                                    }else{
                                    	var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
                    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                    					}
                	                	alertor.dangerAlert(msgInfo,'middle');
                                    }
                                },
                                error: function(msg){
                                    alertor.dangerAlert('连接服务器错误','small');
                                }
                            });
                        }
                    },
                    message : alertor.warningMessage('是否确定删除所选产品认证信息吗？')
                });

            });

            var btn_edit = $('#' + self.elId + '_btn_product_param_edit');
            btn_edit.unbind('click');
            btn_edit.bind('click',function () {
                var result = product_param_Table.bootstrapTable('getSelections');
                if(result.length ==1){
                    self.edit_product_parameter(self,self.elId,result[0]);
                }else{
                    alertor.dangerAlert('请选中一条记录','small');
                }
            });

            var btn_query = $('#' + self.elId + '_btn_product_param_query');
            btn_query.unbind('click');
            btn_query.bind('click',
        		function () { 
            		$('#' + self.elId + '_tb_product_param_table').bootstrapTable('refresh'); 
            	});

            var btn_clear = $('#' + self.elId + '_btn_product_param_clear');
        	btn_clear.unbind('click');
        	btn_clear.bind('click',
        		function () {
	            	$('#' + self.elId + '_input_authid_query').val("");
	                $('#' + self.elId + '_tb_product_param_table').bootstrapTable('refresh')
            	});
        },
        initTable : function(row) {
            var self = this;
            var product_param_Table = $('#' + self.elId + '_tb_product_param_table');

            //销毁已存在
            product_param_Table.bootstrapTable("destroy");
            product_param_Table.bootstrapTable({
                theadClasses: 'thead-light',
                url: 'product/certification',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
                toolbar: '#' + self.elId + '_tb_product_param_template_toolbar',                //工具按钮用哪个容器
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
                        product: row.pId,
                        authId: $('#' + self.elId + '_input_authid_query').val()
                    };
                },           //传递参数（*）
                sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                pageNumber:1,                       //初始化加载第一页，默认第一页
                pageSize: 20,                       //每页的记录行数（*）
                pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                minimumCountColumns: 2,             //最少允许的列数
                clickToSelect: true,                //是否启用点击选中行
                uniqueId: "authId",                     //每一行的唯一标识，一般为主键列
                showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                buttonsClass: 'sm btn-primary',
                cardView: false,                    //是否显示详细视图
                detailView: false,                   //是否显示父子表
                showColumns: false,
                showFullscreen: false,
                showRefresh: false,
                columns: [
                    {checkbox: true},
                    {field: 'pId',       title: '产品',      width: '120'},
                    {field: 'authId',    title: '认证信息编号', width: '120'},
                    {field: 'protocal',  title: '协议' ,     width: '120'},
                    {field: 'userId',    title: '用户名' ,    width: '120'},
                    {field: 'publicKey', title: '密码(密钥)' }
                ],
                responseHandler: function (res) {
                    return res.data;
                },
                onDblClickRow: function (row) {
                	self.edit_product_parameter(self, self.elId, row);
                }
            });

        },
        edit_product_parameter: function (self, elId, row) {
        	var param_table = $('#' + self.elId + '_tb_product_param_table');
        	var btn_save_param = $('#saveNewProductParm');
        	var save_url = null;
        	if (row && row != null){
        		save_url = 'product/editcertification';
                $('#pId').val(row.pId);
                $('#pId').attr("disabled",true);
                $('#authId').val(row.authId);
                $('#authId').attr("disabled",true);
                $('#userId').val(row.userId);
                $('#protocal').val(row.protocal);
                $('#publicKey').val(row.publicKey);
        	} else {
        		save_url = 'product/addcertification';
                $('#pId').val(self.jrow.pId);
                $('#pId').attr("disabled",true);
                $('#authId').removeAttr("disabled");
                $('#authId').val('');
                $('#userId').val('');
                $('#protocal').val('');
                $('#publicKey').val('');
        	}
        	btn_save_param.unbind('click');
        	btn_save_param.bind('click',function () {
        		var tValue = $('#authId').val();
        		if(null == tValue || tValue == ""){
        			alertor.dangerAlert('操作失败，认证信息编号不能为空','small');
        			return false;
            	}
        		if(!validateStringLength(tValue,50)){
            		alertor.dangerAlert('操作失败，认证信息编号的长度不能超过50','small'); 
            		return false;
            	}
        		tValue = $('#userId').val();
        		if(null == tValue || tValue == ""){
            		alertor.dangerAlert('操作失败，用户名不能为空','small');
        			return false;
            	}
        		if(!validateStringLength(tValue,20)){
            		alertor.dangerAlert('操作失败，用户名的长度不能超过20','small'); 
            		return false;
            	}
        		tValue = $('#protocal').val();
        		if(null == tValue || tValue == ""){
            		alertor.dangerAlert('操作失败，请选择协议','small');
        			return false;
            	}
        		tValue = $('#publicKey').val();
        		if(!validateStringLength(tValue,2000)){
            		alertor.dangerAlert('操作失败，密码(密钥)的长度不能超过2000','small'); 
            		return false;
            	}
                var formdata= {
                    pId: $('#pId').val(), authId: $('#authId').val(),
                    userId: $('#userId').val(), protocal: $('#protocal').val(),
                    publicKey: $('#publicKey').val()
                };
                $.ajax({
                    data :JSON.stringify(formdata),
                    url : save_url,
                    type : 'POST',
                    contentType:'application/json',
                    success: function(data){
                        if(data.success){
                            alertor.successAlert('保存成功','small');
                            param_table.bootstrapTable('refresh');
                            $('#productParamEditModal').modal("hide");
                        }else{
                        	var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	alertor.dangerAlert(msgInfo,'middle');
                        }
                    },
                    error: function(msg){
                        alertor.dangerAlert('连接服务器错误','small');
                    }
                });
            });
        	$('#productParamEditModal').modal("show");
        }
    });

    return app;
});