define(['underScore', 
	'text!application/wws_batch.html', 
	'mloadding', 
	'bootstrap-table'],
    function ( _, template, mloadding) {
        var app = function () {
            this.o_template = $(template);
        }
        _.extend(app.prototype, {
        	initialize:function(){
            },
            load: function () {
            	
            },
            initRedisBatchTable: function(){
            	var self = this;
            	$('#tb_wws_batch').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'jobInsAndTaskInsPage',         //请求后台的URL（*）
                    ajaxOptions: {
						beforeSend : function(){
	        				mloadding.showLoadding();
	        			},
	        			complete : function(){
	        				mloadding.hideLoadding();
	        			}
                    },
                    method: 'get',                      //请求方式（*）
                    toolbar: '#tb_wws_batch_toolbar',                //工具按钮用哪个容器
                    cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                    pagination: true,                   //是否显示分页（*）
                    sortable: false,                     //是否启用排序
                    sortOrder: "asc",                   //排序方式
                    queryParamsType:'undefined',
                    queryParams: function (params) {
                       return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                    	   		pageNumber: params.pageNumber,   //页面大小
                    	   		pageSize: params.pageSize,  //页码
                    	   		product: $("select[name='cond_product']").val(),
                    	   		batchNo: $("select[name='batchNo']").val(),
                    	   		jobId: $("input[name='jobId']").val(),
                    	   		jobInsId: $("input[name='jobInsId']").val(),
                    	   		taskId: $("input[name='taskId']").val(),
                    	   		taskInsId: $("input[name='taskInsId']").val(),
                    	   		score: $("select[name='score']").val(),
                    	   		taskTitle: $("input[name='taskTitle']").val()
                            };
                        },           //传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "",                     //每一行的唯一标识，一般为主键列
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
                        {field: 'batch', title: '批量'}, 
                        {field: 'job_id', title: '任务编号' },
                        {field: 'job_ins_id', title: '任务实例编号' },
                        {field: 'task_id', title: '任务项编号' },
                        {field: 'task_ins_id', title: '任务项实例编号' },
                        {field: 'task_state', title: '状态',
                        	formatter:function(value,row,index){
                        		return self.getTaskStateDesc(value);
                            }},
                        {field: 'task_title', title: '标签' }
                        ],
                    responseHandler: function (res) {
                    	return res.data;
                    }
                });
            },
            render: function (container) {
                var self = this;
                var o_container = $(container);
                o_container.empty();
                o_container.append(this.o_template);
                var sel_prd = $("select[name='cond_product']");
                sel_prd.empty();
                sel_prd.append("<option value=''>请选择产品</option>");
	          	$.ajax({
	  	        		async:false,
	  	    			url : 'product/selectProduct',
	  	    			type : 'GET',
	  	    			contentType:'application/json',
	  	    			success: function(data){
	  	    				if(data.success){
  	    						var products = data.data.products;
  	    						$.each(products,function(i){
  	    							sel_prd.append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
  	    						})
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
	          	sel_prd.bind('change', function(event){ 
	          		self.productChange(); 
	          	});
                $('#btn_redis_batchQuery', this.o_container).bind('click', function(event){ $('#tb_wws_batch').bootstrapTable('refresh') });
                self.initRedisBatchTable();
                
                $('#btn_taskins_rerun_select', this.o_container).bind('click', function(event){ self.taskRerunSelect(); });
                $('#btn_taskins_rerun_query', this.o_container).bind('click', function(event){ self.taskRerunQuery(); });
                $('#btn_taskins_disabled_select', this.o_container).bind('click', function(event){ self.taskDisabledSelect(); });
                $('#btn_taskins_disabled_query', this.o_container).bind('click', function(event){ self.taskDisabledQuery(); });
                
                $('#btn_wwsBatchList_clear', this.o_container).bind('click', function(event){
                	$("select[name=cond_product]").val("");
        	   		$("select[name='batchNo']").val("");
        	   		$("input[name='jobId']").val("");
        	   		$("input[name='jobInsId']").val("");
        	   		$("input[name='taskId']").val("");
        	   		$("input[name='taskInsId']").val("");
        	   		$("select[name=score]").val("-1");
        	   		$("input[name='taskTitle']").val("");
                });
                
                var sel_batch = $("select[name='batchNo']");
            	sel_batch.empty();
            	sel_batch.append("<option value=''>请选择批量</option>");
            },
            getTaskStateDesc:function(stateValue){
            	if(stateValue == "11"){
        			return "<span class='badge badge-secondary'>初始化</span>";
        		}else if(stateValue == "12"){
        			return "<span class='badge badge-secondary'>准备执行</span>";
        		}else if(stateValue == "13"){
        			return "<span class='badge badge-info'>正在执行</span>";
        		}else if(stateValue == "14"){
        			return "<span class='badge badge-danger'>出错延迟</span>";
        		}else if(stateValue == "15"){
        			return "<span class='badge badge-info'>执行结果返回中</span>";
        		}else if(stateValue == "16"){
        			return "<span class='badge badge-danger'>失败</span>";
        		}else if(stateValue == "17"){
        			return "<span class='badge badge-info'>需人工干预</span>";
        		}else if(stateValue == "18"){
        			return "<span class='badge badge-info'>执行延迟中</span>";
        		}else if(stateValue == "20"){
        			return "<span class='badge badge-success'>成功通过</span>";
        		}else if(stateValue == "21"){
        			return "<span class='badge badge-success'>忽略通过</span>";
        		}else if(stateValue == "22"){
        			return "<span class='badge badge-success'>周期不满足通过</span>";
        		}else if(stateValue == "23"){
        			return "<span class='badge badge-success'>人工置无效通过</span>";
        		}else if(stateValue == "24"){
        			return "<span class='badge badge-success'>人工置为失败通过</span>";
        		}else{
        			return "<span class='badge badge-dark'>未知</span>";
        		}
            },
            taskRerunSelect : function(){
            	var selected = $('#tb_wws_batch').bootstrapTable('getSelections');
            	if(selected.length < 1){
            		bootbox.alert({message:'操作失败，请至少选中一条任务项实例',size:'small'});
            		return;
            	}
        		var pjts = "";
        		var pjtArr = [];
        		_.each(selected, function(element, index){
        			pjtArr.push(element.product+";"+element.job_ins_id+";"+element.task_ins_id);
        		});
        		pjts = pjtArr.join('|');
        		var tConfirm = true;
            	bootbox.confirm({
            		size : 'small',
            		message : '确定重跑选中的任务项实例？',
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			data : {pjts:pjts},
                    			url : 'taskIns/taskRerunBatchSelect',
                    			type : 'PUT',
                    			contentType:'application/x-www-form-urlencoded',
                    			success: function(data){
                    				if(data.success){
                    					bootbox.alert({message:'请求已提交，请在批量查询界面查看状态',size:'small'});
                    					$('#tb_wws_batch').bootstrapTable('refresh');
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
            taskRerunQuery : function(){
            	var product = $("select[name='cond_product']").val();
            	if(null == product || product == ""){
            		bootbox.alert({message:'操作失败，查询条件（产品）不能为空',size:'small'});
            		return;
            	}
            	var batchNo = $("select[name='batchNo']").val();
    	   		if(null == batchNo || batchNo == ""){
            		bootbox.alert({message:'操作失败，查询条件（批量）不能为空',size:'small'});
            		return;
            	}
            	var jobId = $("input[name='jobId']").val();
            	var jobInsId = $("input[name='jobInsId']").val();
            	var taskId = $("input[name='taskId']").val();
            	var taskInsId = $("input[name='taskInsId']").val();
            	var score = $("select[name='score']").val();
            	var taskTitle = $("input[name='taskTitle']").val();
        		var tConfirm = true;
            	bootbox.confirm({
            		size : 'small',
            		message : '确定按条件重跑任务项实例？',
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
            				if(!mloadding.showLoadding()){
                        		return false;
                        	}
                    		$.ajax({
                    			data : {"product":product,"batchNo":batchNo,"jobId":jobId,"jobInsId":jobInsId,"taskId":taskId,"taskInsId":taskInsId,"score":score,"taskTitle":taskTitle},
                    			url : 'taskIns/taskRerunBatchQuery',
                    			type : 'PUT',
                    			contentType:'application/x-www-form-urlencoded',
                    			success: function(data){
                    				if(data.success){
                    					bootbox.alert({message:'请求已提交，请在批量查询界面查看状态',size:'small'});
                    					$('#tb_wws_batch').bootstrapTable('refresh');
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
                		}
                	 }
            	});
            },
            taskDisabledSelect : function(){
            	var selected = $('#tb_wws_batch').bootstrapTable('getSelections');
            	if(selected.length < 1){
            		bootbox.alert({message:'操作失败，请至少选中一条未结束的任务项实例',size:'small'});
            		return;
            	}
            	//var hasUnpassTask = false;
        		var pjts = "";
        		var pjtArr = [];
        		_.each(selected, function(element, index){
        			if (element.TASK_STATE < 20){//未结束的任务
        				pjtArr.push(element.product+";"+element.job_ins_id+";"+element.task_ins_id);
        			}
        			/*if (element.TASK_STATE > 19){//未结束的任务
        				hasUnpassTask = true;
        			}*/
        		});
        		/*if(hasUnpassTask){
        			bootbox.alert({message:'操作失败，只有未结束的Task实例才能被设为通过',size:'small'});
        			return;
        		}*/
        		if(pjtArr.length < 1){
        			bootbox.alert({message:'操作失败，请至少选中一条未结束的任务项实例',size:'small'});
        			return;
        		}
        		pjts = pjtArr.join('|');
        		var tConfirm = true;
            	bootbox.confirm({
            		size : 'small',
            		message : '确定选中的任务项实例设为通过？',
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			data : {pjts:pjts},
                    			url : 'taskIns/taskDisabledBatchSelect',
                    			type : 'PUT',
                    			contentType:'application/x-www-form-urlencoded',
                    			success: function(data){
                    				if(data.success){
                    					bootbox.alert({message:'请求已提交，请在批量查询界面查看状态',size:'small'});
                    					$('#tb_tasks_instance').bootstrapTable('refresh');
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
            taskDisabledQuery : function(){
            	var product = $("select[name='cond_product']").val();
            	if(null == product || product == ""){
            		bootbox.alert({message:'操作失败，查询条件（产品）不能为空',size:'small'});
            		return;
            	}
            	var batchNo = $("select[name='batchNo']").val();
    	   		if(null == batchNo || batchNo == ""){
            		bootbox.alert({message:'操作失败，查询条件（批量）不能为空',size:'small'});
            		return;
            	}
            	var jobId = $("input[name='jobId']").val();
            	var jobInsId = $("input[name='jobInsId']").val();
            	var taskId = $("input[name='taskId']").val();
            	var taskInsId = $("input[name='taskInsId']").val();
            	var taskTitle = $("input[name='taskTitle']").val();
        		var tConfirm = true;
            	bootbox.confirm({
            		size : 'small',
            		message : '确定按条件将任务项实例设为通过？',
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			data : {"product":product,"batchNo":batchNo,"jobId":jobId,"jobInsId":jobInsId,"taskId":taskId,"taskInsId":taskInsId,"taskTitle":taskTitle},
                    			url : 'taskIns/taskDisabledBatchQuery',
                    			type : 'PUT',
                    			contentType:'application/x-www-form-urlencoded',
                    			success: function(data){
                    				if(data.success){
                    					bootbox.alert({message:'请求已提交，请在批量查询界面查看状态',size:'small'});
                    					$('#tb_wws_batch').bootstrapTable('refresh');
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
            productChange : function() {
            	var sel_batch = $("select[name='batchNo']");
            	sel_batch.empty();
            	sel_batch.append("<option value=''>请选择批量</option>");
              	$.ajax({
            		async:false,
        			url : 'batch/select?product='+$("select[name='cond_product']").val(),
        			type : 'GET',
        			contentType:'application/json',
        			success: function(data){
        				if(data.success){
        					var batchs = data.data.batchs;
        					$.each(batchs,function(i){
        						sel_batch.append("<option value='" + batchs[i].BATCH + "'>" + batchs[i].BATCH + "</option>");
        					})
        				}else{
        					bootbox.alert({message:'获取批量信息失败' + (data.info ? '<br>' + data.info : ''),size:'middle'});
        				}
        			},
        			error: function(msg){
        				bootbox.alert({message:'连接服务器错误',size:'small'});
        			}
              	});
            }
        });

        return app;
});