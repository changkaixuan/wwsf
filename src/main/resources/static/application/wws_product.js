define([
	'underScore', 
	'text!application/wws_product.html',
	'app-editable',
	'alertor',
	'css!js/bootstrap/css/bootstrap-table.css', 
	'bootstrap-table',
	'css!js/bootstrap/css/bootstrap-select.css',
	'bootstrap-select',
	'bootstrap-table-locale',
	'css!js/jquery-editable/poshytip-1.2/src/tip-twitter/tip-twitter.css'
	],
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
                $('#btn_product_query', this.o_container).bind('click', function(event){ 
                	$('#tb_product').bootstrapTable('refresh'); }
                );
                //重置按钮加事件
                $('#btn_product_clear', this.o_container).bind('click', function(event){
                	$("input[name='qPId']").val("");
                	$('#tb_product').bootstrapTable('refresh');
                });
                $('#btn_product_add', this.o_container).bind('click', function(event){ self.editProduct(null); });
                $('#btn_product_edit', this.o_container).bind('click', function(event){
                	var result = $('#tb_product').bootstrapTable('getSelections');
                    if(result.length ==1){
                    	self.editProduct(result[0]);
                    }else{
                    	alertor.dangerAlert('请选中一条记录','small');
                    }
                });
                $('#btn_product_del', this.o_container).bind('click', function(event){ self.delProduct(event); });
                $('#btn_product_param_config', this.o_container).bind('click', function(event){
                    var selected = $('#tb_product').bootstrapTable('getSelections');
                    if(selected.length == 1){
                        self.forkParamTabpanel(selected[0]);
                    } else{
                    	alertor.dangerAlert('请选中一条记录','small');
                        return;
                    }
                });


                //初始化用户表格
                self.initProductTable();
            }, 
            initProductTable: function(){
            	var self = this;
            	$('#tb_product').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'product/selectProductByName',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    toolbar: '#product_tab_toolbar',                //工具按钮用哪个容器
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
                    	   	product: $("input[name='qPId']").val()
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
                    	{field: 'pId', title: '产品英文名'}, 
                        {field: 'pName', title: '产品中文名'}, 
                        {field: 'pDesc', title: '产品描述' }
                    ],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    onDblClickRow: function (row) {
                    	self.editProduct(row);
                    }
                });
            },
            editProduct : function(row){
            	var self = this;
            	var m = $('#productEditModal');
            	m.find("input[name=pId]").removeAttr("disabled");
            	m.modal('show');
            	var modalType = "add";
            	if(null == row){
            		m.find("input[type='text']").val(""); //模态框中所有输入框的值初始化为空
            		m.find("textarea[name=pDesc]").val("");
            		m.find("textarea[name=memo]").val("");
            	}else{
            		modalType = "edit";
            		$.ajax({
            			data : {pId:row.pId},
            			url : 'product/get',
            			type : 'GET',
            			contentType:'application/x-www-form-urlencoded',
            			success: function(data){
            				if(data.success){
        						var product = data.data.product;
        						m.find("input[name=pId]").val(product.pId);
        						m.find("input[name=pId]").attr("disabled",true);
        						m.find("input[name=pName]").val(product.pName);
        						m.find("textarea[name=pDesc]").val(product.pDesc);
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
            	$('#saveProduct').unbind('click').bind('click', function(event){ 
            		self.saveProduct(event, modalType); 
            	});
            },
            saveProduct : function(event,modalType){
            	var self = this;
            	var m = $('#productEditModal');
            	var formJson = {};
            	//pId
        		formJson.pId = m.find("input[name=pId]").val();
        		if(null == formJson.pId || formJson.pId == ""){
        			alertor.dangerAlert('操作失败，产品英文名不能为空','small');
        			return false;
                }
        		if(!/^[0-9a-zA-Z_]+$/g.test(formJson.pId)) {
        			alertor.dangerAlert('操作失败，产品英文名必须为字母，数字，下划线组成','small');
            		return;
            	}
        		if(!validateStringLength(formJson.pId,50)){
        			alertor.dangerAlert('操作失败，产品英文名的长度不能超过50','small');
            		return false;
            	}
        		//pName
        		formJson.pName = m.find("input[name=pName]").val();
        		if(!validateStringLength(formJson.pName,500)){
        			alertor.dangerAlert('操作失败，产品中文名的长度不能超过500','small');
            		return false;
            	}
        		//pDesc
        		formJson.pDesc = m.find("textarea[name=pDesc]").val();
        		if(!validateStringLength(formJson.pDesc,500)){
        			alertor.dangerAlert('操作失败，产品描述的长度不能超过500','small');
            		return false;
            	}

        		var url = "product/insertProduct";
        		if(modalType == "edit"){
        			url = "product/updateProduct";
        		}
        		$.ajax({
        			url:url,
                    type:"POST",
                    processData:false,
                    data:JSON.stringify(formJson),
                    contentType:'application/json',
        			success: function(data){
        				if(data.success){
        					$('#productEditModal').modal('hide');
        					alertor.successAlert('保存成功','small');
        					$('#tb_product').bootstrapTable('refresh');
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
            },
            delProduct : function(event){
            	var selected = $('#tb_product').bootstrapTable('getSelections');
            	if(selected.length<1) {
            		alertor.dangerAlert('请选择需要删除的产品','small');
            		return;
            	}
            	var productIdArr = [];
            	$.each(selected,function(i){
            		productIdArr.push(selected[i].pId);
        		})
        		var pids = productIdArr.toString();
            	var tConfirm = true;
        		bootbox.confirm({
            		size : 'small',
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			async:false,
                    			url : 'product/deleteProduct',
                    			type : 'DELETE',
                    			data : {pids:pids},
                    			dataType : 'json',
                    			success: function(data){
                    				if(data.success) {
                    					if(data.info != null && data.info=='confrimDel') {
                    						app.prototype.confirmDelProduct(pids);
                    					}else {
                    						alertor.successAlert('删除产品成功','small');
                    						$('#tb_product').bootstrapTable('refresh');
                    					}
                                	}else {
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
                	message : alertor.warningMessage('是否确定删除所选产品吗？')
            	});
            },
            confirmDelProduct : function(pids) {
            	var tConfirm = true;
        		bootbox.confirm({
            		size : 'small',
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			async:false,
                    			url : 'product/confirmDeleteProduct',
                    			type : 'DELETE',
                    			data : {pids:pids},
                    			dataType : 'json',
                    			success: function(data){
                    				if(data.success) {
                    					alertor.successAlert('删除产品成功','small');
                						$('#tb_product').bootstrapTable('refresh');
                                	}else {
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
                	message : alertor.warningMessage('选择的产品中包含已存在任务模板，维度，计划任务等，是否确认全部删除')
            	});
            },
		    forkParamTabpanel : function(row){
            var self = this;
            var navPill = $('#nav_jtpl');
            if ($('#nav_jtpl a').length > 6) {
                alertor.dangerAlert('创建选项卡过多','small');
                return;
            }
            var navLink = $('#nav_jtpl_'+row.pId);
            if (navLink.length > 0) {
                navLink.get(0).click();
                return;
            }
            var item = {'id':row.pId,'name':row.pName + '(认证信息)','closable':true};
            var panelId = self.addPill(navPill, item);
            require([ "application/wws_product_parameter" ], function(ModuleClass) {
                var oModule = new ModuleClass(self, row, panelId);
                oModule.load();
                oModule.render('#'+panelId);
            });
        },
            addPill:function(el, pillConfig){
                var self = this;
                var nav_id = el[0].id;
                var link_id = nav_id + '_' + pillConfig.id;
                var content_id = nav_id + '_content';
                var panel_id = content_id + '_' + pillConfig.id;

                $(".active", el).removeClass("active");
                $(".show", el).removeClass("show");
                $(".active", $('#'+content_id)).removeClass("active");
                $(".show", $('#'+content_id)).removeClass("show");

                if(!$('#'+link_id)[0]){
                    var nav_link;
                    if(pillConfig.closable){
                        nav_link =
                            '<li class="nav-item mr-1 mb-1" role="presentation">' +
                            '<a class="nav-link pr-2" data-toggle="tab" role="tab" id="'+link_id+'" href="#'+panel_id+'">' +
                            '<i class="fal fa-tasks mr-1"></i>' + pillConfig.name +
                            '<i class="fal fa-window-close ml-3" data-linkid="'+link_id+'"></i>' +
                            '</a>' +
                            '</li>';
                    }else{
                        nav_link =
                            '<li class="nav-item mr-1" role="presentation">' +
                            '<a class="nav-link" data-toggle="tab" role="tab" id="'+link_id+'" href="#'+panel_id+'">' +
                            '<i class="fal fa-clock mr-1"></i>' + pillConfig.name +
                            '</a>' +
                            '</li>';
                    }
                    el.append(nav_link);
                    var tabpanel = '<div class="tab-pane fade" role="tabpanel" id="'+panel_id+'">'+
                        '<div class="progress progress-striped" style="margin-bottom:0;width:20%;height:26px;">'+
                        '<div class="progress-bar" style="width:100%;">'+
                        '<span style="font-weight: bold;"><i class="fal fa-cog fa-spin mr-1"></i>Loading, please wait...</span>'+
                        '</div>'+
                        '</div>'+
                        '</div>';
                    $('#'+content_id).append(tabpanel);
                    if(pillConfig.closable){
                        $('i[data-linkid="'+link_id+'"]').bind("click", function(e){
                            self.closePill(e.currentTarget);
                        });
                    }
                    if (pillConfig.url && pillConfig.url != null){
                        $('#'+panel_id).load(pillConfig.url,function(response,status,xhr){
                            //status的值为success和error，如果error则显示一个错误页面
                            if(status=='error'){
                                $(this).html(response);
                            }
                        });
                    }
                }
                $("#"+link_id)[0].click();
                return panel_id;
            },
            closePill:function(el){
                if (el) {
                    var link_id = $(el).attr('data-linkid');
                    var link = $('#'+link_id);
                    var nav = link.parent();
                    var preNav = nav.prev();
                    var panel_id = link.attr('href').substring(1);
                    nav.remove();
                    $('#'+panel_id).remove();
                    if(link.hasClass('active')) preNav.find("a")[0].click();
                }
            }
        });
        return app;
});