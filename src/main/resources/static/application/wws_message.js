define([
	'underScore', 
	'text!application/wws_message.html',
	'mloadding',
	'css!js/bootstrap/css/bootstrap-table.css', 
	'bootstrap-table',
	'css!js/jquery-editable/poshytip-1.2/src/tip-twitter/tip-twitter.css'],
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
                var messageModal = $("#messageModal");
                if(messageModal.length==0){
                	o_container.append(this.o_template);
                	messageModal = $("#messageModal");
                	//初始化用户表格
                    self.initMessageTable();
                }else{
                	$('#tb_message').bootstrapTable('refresh');
                }
                //产品下拉
                var sel_prd = $("#message_q_product");
	          	$.ajax({
  	        		async: false,
  	    			url: 'product/selectProduct',
  	    			type: 'GET',
  	    			contentType: 'application/json',
  	    			success: function(data){
  	    				if(data.success){
    						var products = data.data.products;
    						sel_prd.empty();
			                sel_prd.append("<option value=''>请选择产品</option>");
    						$.each(products,function(i){
    							sel_prd.append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
    						});
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
                //查询按钮加事件
                $('#btn_message_query', this.o_container).unbind("click").bind('click', function(event){ 
                	$('#tb_message').bootstrapTable('refresh'); }
                );
                //重置按钮加事件
                $('#btn_message_clear', this.o_container).unbind("click").bind('click', function(event){
                	$("#message_q_product").val("");
                	$("#message_q_mLevel").val("");
                	$("#message_q_mRead").val("");
                	$('#tb_message').bootstrapTable('refresh');
                });
                $('#btn_message_read', this.o_container).unbind("click").bind('click', function(event){ self.messageRead(null,null); });
                messageModal.modal("show");
            },
            initMessageTable: function(){
            	var self = this;
            	$('#tb_message').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'message/list',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    toolbar: '#message_tab_toolbar',                //工具按钮用哪个容器
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
                    	   	product: $("#message_q_product").val(),
                    	   	mLevel: $("#message_q_mLevel").val(),
                    	   	mRead: $("#message_q_mRead").val()
                    	};
                    },           //传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "id",              //每一行的唯一标识，一般为主键列
                    showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                   //是否显示父子表
                    showColumns: true,
                    showFullscreen: true,
                    showRefresh: true,
                    columns: [
                    	{checkbox: true}, 
                    	{field: 'product', title: '产品', width:'100px'}, 
                        {field: 'createTime', title: '创建时间', align: 'center', width:'140px'}, 
                        {field: 'mLevel', title: '消息级别', align: 'center', width:'100px' },
                        {field: 'mRead', title: '已阅', align: 'center', width:'70px',
                        	formatter: function (value, row, index) {
	                    		if(value == "1"){
	                    			return "<span class='badge badge-secondary'>是</span>";
	                    		}else{
	                    			return "<span class='badge badge-info'>否</span>";
	                    		}
					        } 
                        },
                        {field: 'mInfo', title: '消息内容',
                        	formatter: function (value, row, index) {
	                    		if(value.length < 80){
	                    			return value;
	                    		}else{
	                    			return value.substr(1,80)+"...";
	                    		}
					        } 
                        }
                    ],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    onDblClickRow: function (row) {
                    	self.messageRead(row.id,row.mRead);
                    }
                });
            },
            messageRead : function(id,mRead){
            	if(id != null){
            		if(mRead == "0"){
	            		$.ajax({
	            			async : false,
	            			url : 'message/read/'+id,
	            			type : 'DELETE',
	            			contentType:'application/json',
	            			success: function(data){
	            				if(data.success){
	            					//bootbox.alert({message:'已阅成功',size:'small'});
	            					$('#tb_message').bootstrapTable('refresh');
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
	            		loadMessage();
            	    }
            		$.ajax({
            			async : false,
            			url : 'message/get?id='+id,
            			type : 'get',
            			contentType:'application/json',
            			success: function(data){
            				if(data.success){
            					var messageViewModal = $("#messageViewModal");
            					messageViewModal.find("label[data-name=product]")[0].innerHTML = data.data.message.product;
            					messageViewModal.find("label[data-name=createTime]")[0].innerHTML = data.data.message.createTime;
            					messageViewModal.find("label[data-name=mLevel]")[0].innerHTML = data.data.message.mLevel;
            					messageViewModal.find("label[data-name=mRead]")[0].innerHTML = data.data.message.mRead == "0" ? "<span class='badge badge-info'>否</span>" : "<span class='badge badge-secondary'>是</span>";
            					messageViewModal.find("label[data-name=mInfo]")[0].innerHTML = data.data.message.mInfo;
            					messageViewModal.modal('show');
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
            	}else{
	            	var selected = $('#tb_message').bootstrapTable('getSelections');
	            	if(selected.length > 0){
	            		var idArr = [];
	            		_.each(selected, function(element, index){
	            			if(element.mRead == "0"){
	            				idArr.push(element.id);
	            			}
	            		});
	            		if(idArr.length < 1){
	            			bootbox.alert({message:'请至少选中一条未阅读消息',size:'small'});
	            			return false;
	            		}
	            		var tConfirm = true;
	            		bootbox.confirm({
	                		size : 'small',
	                		message : '确定已阅？',
	                		callback : function (result) {
	                			if(result && tConfirm){
	                				tConfirm = false;
	                        		$.ajax({
	                        			async : false,
	                        			url : 'message/read/'+idArr.join(','),
	                        			type : 'DELETE',
	                        			contentType:'application/json',
	                        			success: function(data){
	                        				if(data.success){
	                        					bootbox.alert({message:'已阅成功',size:'small'});
	                        					$('#tb_message').bootstrapTable('refresh');
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
	                        		loadMessage();
	                    		}
	                    	 }
	                	});
	            	}else{
	            		bootbox.alert({message:'请至少选中一条未阅读消息',size:'small'});
	            	}
            	}
            }
        });
        return app;
});