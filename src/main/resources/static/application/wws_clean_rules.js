define([
		'underScore',
		'text!application/wws_clean_rules.html', 
		'mloadding', 
		'select2',
		'css!css/select2.min.css', 
		'bootstrap-table',
		'bootstrap-table-locale'],
    function (_, template, mloadding) {
        var app = function (config) {
            this.o_template = $(template);
            this.chartTaskState = null;
            this.chartTaskLabelState = null;
        }
        _.extend(app.prototype, {
        	initialize:function(){
            },
            load: function () {
            },
            render:function(container){
            	var self = this;
                var o_container = $(container);
                o_container.empty();
                o_container.append(self.o_template);
                // 初始化产品下拉框
                var sel_prd = $('#qProduct');
                sel_prd.select2({minimumResultsForSearch : -1,"width":"240","placeholder":"请选择产品"});
                self.loadProductSelect(sel_prd);
                sel_prd.bind('change', function() {
                	$("#job_ins_clean_rule_table").bootstrapTable("refresh");
	          		$("#cron_log_clean_rule_table").bootstrapTable("refresh");
                });
	          
	          	// 初始化Job实例清理策略表
	          	self.initJobInstanceTable("job_ins_clean_rule_table","JOB");
	          	
	          	// 初始化Cron日志清理策略表
	          	self.initJobInstanceTable("cron_log_clean_rule_table","CRON");
	          	
	          	// 新增job清理策略按钮
	          	$("#btn_add_job_rule").bind('click', function() {
	          		self.editCleanRule("JOB",null);
	          	});

	          	// 新增cron清理策略按钮
	          	$("#btn_add_cron_rule").bind('click', function() {
	          		self.editCleanRule("CRON",null);
	          	});
	          	
	          	// 加载按钮权限
	          	self.loadBtnAuth();
	          	
	          	// 预警参数保存按钮
	          	$("#save_ewarn_setting").bind('click', function() {
	          		self.saveEwarnSetting();
	          	});
	          	// 清理参数保存按钮
	          	$("#save_clean_setting").bind('click', function() {
	          		self.saveCleanSetting();
	          	});
	            // 执行策略按钮
	          	$("#clean_rules_execute").unbind('click').bind('click', function() {
	          		var product = $("#wws_clean").find("select[name=cond_product]").val();
	          		self.execuCleanRules(product,null,null);
	          	});
	          	// 初始化参数
	          	self.initSetting();
            },
            initJobInstanceTable: function(tableId, type){
            	var self = this;
            	$('#'+tableId).bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'cleanRules/list',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                  //  toolbar: '#btn_add_rule',                //工具按钮用哪个容器
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
                	   		product: $("#qProduct").val(),
                	   		type:type
                       };
                    },           //传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "jobInsId",               //每一行的唯一标识，一般为主键列
                    showToggle:false,                   //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-default',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                  //是否显示父子表
                    showColumns: false,
                    showFullscreen: false,
                    showRefresh: false,
                    columns: [
//                    	{checkbox : true},
                    	{field: 'id', title: '策略编号', width:'240px'}, 
                    	{field: 'jobNames', title: '任务名称',
                    		 formatter: function (value, row, index) {
                    			var text = "";
                    			var jobIdArr = row.jobIds.split(",");
                    			var tempJobIdArr = [];
                    			var tempJobNameArr = [];
                    			if(value != null){
                    				var jobNameArr = value.split("%%");
		                    		if (jobNameArr && jobNameArr.length > 0) {
		                    			for (var i=0; i<jobNameArr.length; i++){
		                    				tempJobIdArr.push(jobNameArr[i].split("^^")[0]);
		                    				tempJobNameArr.push(jobNameArr[i].split("^^")[1]);
		                    			}
		                    		}
                    			}
                    			for(var i=0;i<jobIdArr.length;i++){
                    				var tempIndex = $.inArray(jobIdArr[i],tempJobIdArr);
                    				if(tempIndex != -1){
                    					text = text + "<span class='badge badge-danger mr-1 mb-1 p-1' title='"+jobIdArr[i]+"'>";
	                    				text = text + tempJobNameArr[tempIndex];
	                    				text = text + "</span>";
                    				}else{
                    					text = text + "<span class='badge badge-secondary mr-1 mb-1 p-1' title='"+jobIdArr[i]+"'>";
	                    				text = text + jobIdArr[i];
	                    				text = text + "</span>";
                    				}
                    			}
						        return text;
						     }
                    	},
                        {field: 'keepTime', title: '保留时间', align:'center', width:'160px',
                    		formatter:function(value,row,index){
                    			return value.split(":")[0] + "天" + value.split(":")[1] + "月" + value.split(":")[2] + "年";
                            }
                        },
                        {field: 'clearRunningJob', title: '是否清理正在执行的任务', width:'160px', align:'center',
                        	formatter:function(value,row,index){
                        		return value==1?"是":"否";
                            }
                        },
                        {title:'操作', edit:false, align:'center', width:'160px', align:'center',
                        	events:{
                            	'click .delete_current_row': function(e, value, row, index) {
                            		self.deleteCleanRule(row.id, type);
                            	},
                            	'click .execu_clean_rules': function(e, value, row, index) {
                            		self.execuCleanRules(row.product,row.type,row.id);
                            	}
                            },
                        	formatter:function(value,row,rowIndex){
                        		var str = '<button type="button" class="btn btn-xs btn-primary px-1 execu_clean_rules">执行策略</button>';
                            	return '<button type="button" class="btn btn-xs btn-primary px-1 mr-1 delete_current_row">删除</button>'+str;
                        	}
                        }
                    ],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    onDblClickRow: function (row) {
                    	self.editCleanRule(type, row);
                    }
                });
            },
            editCleanRule : function(type, row) {
            	var self = this;
            	var modal = $("#edit_celan_rule_modal");
            	modal.find("input[name=type]").val(type);
            	var sel_prd = modal.find("select[name=product]");
            	self.loadProductSelect(sel_prd);
            	sel_prd.unbind('change').bind('change', function(obj) {
            		self.loadJobIdByProduct(obj.target.value, type);
            	});
            	$("#jobIds").empty();
            	modal.find("input[name=id]").removeAttr("disabled");
            	if(row == null){
            		modal.find("input[name=id]").val("");
            		sel_prd.val($("#qProduct").val());
            		sel_prd.change();
            		modal.find("input[name=keepTimeDay]").val("0");
            		modal.find("input[name=keepTimeMonth]").val("0");
            		modal.find("input[name=keepTimeYear]").val("0");
            		modal.find("input[name=clearRunningJob]").prop("checked",false);
            		$("#btn_save_clean_rule").unbind('click').bind('click', function() {
                		self.saveCleanRule(type,"add");
                	});
            	}else{
            		$.ajax({
      	        		async: false,
      	    			url: 'cleanRules/get',
      	    			type: 'GET',
      	    			contentType: 'application/json',
      	    			data: {id:row.id},
      	    			success: function(data){
      	    				if(data.success){
        						var cr = data.data.cleanRules;
        						modal.find("input[name=id]").val(cr.id).attr("disabled",true);
        						sel_prd.val(cr.product);
        						sel_prd.change();
        						modal.find("input[name=keepTimeDay]").val(cr.keepTime.split(":")[0]);
        	            		modal.find("input[name=keepTimeMonth]").val(cr.keepTime.split(":")[1]);
        	            		modal.find("input[name=keepTimeYear]").val(cr.keepTime.split(":")[2]);
        	            		modal.find("input[name=clearRunningJob]").prop("checked",cr.clearRunningJob==1);
        	            		var jobIdArr = cr.jobIds.split(",");
        						if(jobIdArr){
        							for(var i=0;i<jobIdArr.length;i++){
        								$.each($("#jobIds").find("span"),function(index,but){
            			                	if($(but).attr('value')==jobIdArr[i]) {
            			                		$(but).click();
            			                	}
            			            	});
        							}
        						}
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
            		$("#btn_save_clean_rule").unbind('click').bind('click', function() {
                		self.saveCleanRule(type,"edit");
                	});
            	}
            	if(type=="JOB") {
            		$("#tr_clean_running_job").show();
            	}else {
            		$("#tr_clean_running_job").hide();
            	}
            	modal.modal("show");
            },
            loadProductSelect : function(sel_prd){
	          	$.ajax({
  	        		async: false,
  	    			url: 'product/selectProduct',
  	    			type: 'GET',
  	    			contentType: 'application/json',
  	    			success: function(data){
  	    				if(data.success){
    						var products = data.data.products;
    						sel_prd.empty();
//    						sel_prd.append("<option value=''>请选择产品</option>");
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
            },
            loadJobIdByProduct : function(product, type) {
            	$("#jobIds").empty();
            	if(!product) {
            		return;
            	}
            	var url = type=="JOB" ? "template/getAllJob" : "cron/getAllCron";
            	$.ajax({
            		async:false,
        			url : url,
        			type : 'GET',
        			contentType:'application/json',
        			data:{product:product},
        			success: function(data){
        				if(data.success){
        					var jobIds = data.data.jobIds;
        					if(jobIds != undefined){
    							for(var i=0;i<jobIds.length;i++){
    								var name = type=="JOB" ? jobIds[i].name : jobIds[i].jobName;
    								$("#jobIds").append("<span style='cursor:pointer;' title='"+jobIds[i].jobId+"' class='btn btn-sm btn-default mb-1 mr-1' value='"+jobIds[i].jobId+"'>"+name+"</span>");
    							}
    							$.each($("#jobIds").find("span"),function(index,but){
    			                	$(but).unbind('click').bind('click', function(event){
    			                		var timeButClass = $(but).attr("class");
    			                		var regexp = RegExp("btn btn-sm btn-default mb-1 mr-1");
    			                		if(regexp.test(timeButClass)){
    			                			$(but).attr("class","btn btn-sm btn-danger mb-1 mr-1");
    			                		}else{
    			                			$(but).attr("class","btn btn-sm btn-default mb-1 mr-1");
    			                		}
    			                	});
    			            	});
    						}
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
            },
            saveCleanRule : function(type, op) {
            	var modal = $("#edit_celan_rule_modal");
            	var cleanRules = {};
            	// id
            	cleanRules.id = modal.find("input[name=id]").val();
            	if(!/^[0-9a-zA-Z_]+$/g.test(cleanRules.id)) {
            		bootbox.alert({message:'操作失败，策略编号只能为数字、字母或者下划线',size:'small'}); 
            		return;
            	}
            	if(!validateStringLength(cleanRules.id,32)){
            		bootbox.alert({message:'操作失败，策略编号的长度不能超过32',size:'small'}); 
            		return false;
            	}
            	var idRepeat = false;
            	$.ajax({
            		async:false,
        			url : 'cleanRules/checkCRId',
        			type : 'GET',
        			contentType:'application/json',
        			data:{id:cleanRules.id},
        			success: function(data){
        				idRepeat = data.success;
        			},
        			error: function(msg){
        				idRepeat = true;
        				bootbox.alert({message:'连接服务器错误',size:'small'});
        			}
              	});
            	if(op=="add" && idRepeat) {
            		bootbox.alert({message:'策略编号已存在',size:'small'});
            		return;
            	}
            	// product
            	cleanRules.product = modal.find("select[name=product]").val();
            	if(!cleanRules.product) {
            		bootbox.alert({message:'产品不能为空',size:'small'});
            		return;
            	}
            	// jobIds
            	var arr = [];
            	$.each($("#jobIds").find("span[class='btn btn-sm btn-danger mb-1 mr-1']"),function(index,but){
            		arr.push($(but).attr('value'));
            	});
            	if(arr.length<1) {
            		bootbox.alert({message:'至少选择一个任务',size:'small'});
            		return;
            	}
            	cleanRules.jobIds = arr.toString();
            	// type
            	cleanRules.type = type;
            	// keepTime
            	var keepTimeDay = modal.find("input[name=keepTimeDay]").val();
            	if(keepTimeDay && !/^(([0-9])|([12][0-9])|(3[01]))$/g.test(keepTimeDay)) {
            		bootbox.alert({message:'保留天数应为0-31天之间',size:'small'});
            		return;
            	}
            	var keepTimeMonth = modal.find("input[name=keepTimeMonth]").val();
            	if(keepTimeMonth && !/^(([0-9])|(1[0-2]))$/g.test(keepTimeMonth)) {
            		bootbox.alert({message:'保留月份应为0-12个月之间',size:'small'});
            		return;
            	}
            	var keepTimeYear = modal.find("input[name=keepTimeYear]").val();
            	if(keepTimeYear && !/^[0-5]$/g.test(keepTimeYear)) {
            		bootbox.alert({message:'保留年建议应为0-5年之间',size:'small'});
            		return;
            	}
            	if(!keepTimeDay && !keepTimeMonth && !keepTimeYear) {
            		bootbox.alert({message:'请输入保留时间',size:'small'});
            		return;
            	}
            	cleanRules.keepTime = (keepTimeDay?keepTimeDay:0) + ":" 
            						+ (keepTimeMonth?keepTimeMonth:0) + ":"
            						+ (keepTimeYear?keepTimeYear:0);
            	// clearRunningJob
            	cleanRules.clearRunningJob = modal.find("input[name=clearRunningJob]").prop("checked")?1:0;
            	// 保存清理策略
            	var url = op=="add" ? "cleanRules/insert" : "cleanRules/update";
            	$.ajax({
            		async:false,
        			url : url,
        			type : 'POST',
        			contentType:'application/json',
        			data:JSON.stringify(cleanRules),
        			beforeSend : function(){
        				mloadding.showLoadding();
        			},
        			complete : function(){
        				mloadding.hideLoadding();
        			},
        			success: function(data){
        				if(data.success){
        					bootbox.alert({message:'保存成功',size:'small'});
        					modal.modal("hide");
        					if(type=="JOB") {
        						$("#job_ins_clean_rule_table").bootstrapTable("refresh");
        					}else {
        						$("#cron_log_clean_rule_table").bootstrapTable("refresh");
        					}
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
            },
            deleteCleanRule : function(id ,type) {
            	bootbox.confirm({
            		size : 'small',
            		message : '是否确定删除这条清理策略？',
            		callback : function (result) {
            			if(result){
            				$.ajax({
            					async:false,
                    			url : 'cleanRules/delete',
                    			type : 'DELETE',
                    			data:{ids:id},
                    			beforeSend : function(){
                    				mloadding.showLoadding();
                    			},
                    			complete : function(){
                    				mloadding.hideLoadding();
                    			},
                    			success: function(data){
                    				if(data.success){
                    					if(type=="JOB") {
                    						$("#job_ins_clean_rule_table").bootstrapTable("refresh");
                    					}else {
                    						$("#cron_log_clean_rule_table").bootstrapTable("refresh");
                    					}
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
            saveEwarnSetting : function() {
            	var arr = [];
            	var setting = {};
            	// 任务执行超过多少秒
            	setting.name = "EWARN_TASK_RUNNING_TIMEOUT";
            	setting.modal = "EWARN";
            	setting.open = $("input[name=cbk_ewarn_task_running_timeout]").prop("checked") ? 1 : 0;
            	var second = $("input[name=ewarn_task_running_timeout]").val();
            	if(setting.open==1 && !/^((\d)|([1-9]\d+))$/g.test(second)) {
            		bootbox.alert({message:'任务执行超过秒数不能为空，且为整数',size:'small'});
            		return;
            	}else if(setting.open==0 && !second) {
            		second = 0;
            	}
            	setting.value = second;
            	arr.push(setting);
            	// 需人工干预
            	setting = {};
            	setting.name = "EWARN_TASK_MANUAL";
            	setting.modal = "EWARN";
            	setting.open = $("input[name=cbk_ewarn_task_manual]").prop("checked") ? 1 : 0;
            	arr.push(setting);
            	// 节点故障
            	setting = {};
            	setting.name = "EWARN_NODE_BREAKDOWN";
            	setting.modal = "EWARN";
            	setting.open = $("input[name=cbk_ewarn_node_breakdown]").prop("checked") ? 1 : 0;
            	arr.push(setting);
            	// 任务失败
            	setting = {};
            	setting.name = "EWARN_TASK_FAIL";
            	setting.modal = "EWARN";
            	setting.open = $("input[name=cbk_ewarn_task_fail]").prop("checked") ? 1 : 0;
            	arr.push(setting);
            	// 任务失败重试次数
            	setting = {};
            	setting.name = "EWARN_TASK_FAIL_RETRY_TOO_MANY";
            	setting.modal = "EWARN";
            	setting.open = $("input[name=cbk_ewarn_task_fail_retry_too_many]").prop("checked") ? 1 : 0;
            	var time = $("input[name=ewarn_task_fail_retry_too_many]").val();
            	if(setting.open==1 && !/^((\d)|([1-9]\d+))$/g.test(time)) {
            		bootbox.alert({message:'任务失败重试次数不能为空，且为整数',size:'small'});
            		return;
            	}else if(setting.open==0 && !time) {
            		time = 0;
            	}
            	setting.value = time;
            	arr.push(setting);
            	$.ajax({
    				url:"setting/updateSettingList",
    	            type:"POST",
    	            processData:false,
    	            data:JSON.stringify(arr),
    	            contentType:'application/json',
    	            success:function (data) {
    	                if (data.success){
    	                	bootbox.alert({message:'保存成功',size:'small'});
    	                }else{
    	                	var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	bootbox.alert({message:msgInfo, size:'middle'});
    	                }
    	            },
    	            error: function(XMLHttpRequest,textStatus,errorThrown){
    	            	bootbox.alert({message:'保存失败'+errorThrown,size:'small'});    
    	            },
    	            beforeSend : function(){
        				mloadding.showLoadding();
        			},
        			complete : function(){
        				mloadding.hideLoadding();
        			}
    	        });
            },
            initSetting : function() {
            	$.ajax({
  	        		async: false,
  	    			url: 'setting/querySettingList',
  	    			type: 'GET',
  	    			contentType: 'application/json',
  	    			success: function(data){
  	    				if(data.success){
    						var settingList = data.data.settingList;
    						for(var i=0,len=settingList.length; i<len; i++) {
    							switch(settingList[i].name) {
    							case "EWARN_TASK_RUNNING_TIMEOUT":
    								$("input[name=cbk_ewarn_task_running_timeout]").prop("checked", settingList[i].open==1);
    								$("input[name=ewarn_task_running_timeout]").val(settingList[i].value);
    								break;
    							case "EWARN_TASK_MANUAL":
    								$("input[name=cbk_ewarn_task_manual]").prop("checked", settingList[i].open==1);
    								break;
    							case "EWARN_NODE_BREAKDOWN":
    								$("input[name=cbk_ewarn_node_breakdown]").prop("checked", settingList[i].open==1);
    								break;
    							case "EWARN_TASK_FAIL":
    								$("input[name=cbk_ewarn_task_fail]").prop("checked", settingList[i].open==1);
    								break;
    							case "EWARN_TASK_FAIL_RETRY_TOO_MANY":
    								$("input[name=cbk_ewarn_task_fail_retry_too_many]").prop("checked", settingList[i].open==1);
    								$("input[name=ewarn_task_fail_retry_too_many]").val(settingList[i].value);
    								break;
    							case "CLEAN_JOB_EXE_INFO_TVALUE":
    								$("input[name=cbk_clean_job_exe_info_tvalue]").prop("checked", settingList[i].open==1);
    								$("input[name=clean_job_exe_info_tvalue]").val(settingList[i].value);
    								break;
    							}
    						}
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
            },
            saveCleanSetting : function() {
            	var setting = {};
            	setting.name = "CLEAN_JOB_EXE_INFO_TVALUE";
            	setting.modal = "CLEAN";
            	setting.open = $("input[name=cbk_clean_job_exe_info_tvalue]").prop("checked") ? 1 : 0;
            	var num = $("input[name=clean_job_exe_info_tvalue]").val();
            	if(setting.open==1 && !/^((\d)|([1-9]\d+))$/g.test(num)) {
            		bootbox.alert({message:'保留数目不能为空，且为整数',size:'small'});
            		return;
            	}else if(setting.open==0 && !num) {
            		num = 0;
            	}
            	setting.value = num;
            	$.ajax({
            		async:false,
        			url : 'setting/updateSetting',
        			type : 'POST',
        			contentType:'application/json',
        			data:JSON.stringify(setting),
        			success: function(data){
        				if (data.success){
    	                	bootbox.alert({message:'保存成功',size:'small'});
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
        			},
        			beforeSend : function(){
        				mloadding.showLoadding();
        			},
        			complete : function(){
        				mloadding.hideLoadding();
        			}
              	});
            },
            loadBtnAuth : function() {
            	$("#save_ewarn_setting").hide();
            	$("#save_clean_setting").hide();
            	$.ajax({
            		async:false,
        			url : 'cleanRules/getBtnAuth',
        			type : 'GET',
        			contentType:'application/json',
        			success: function(data){
        				if (data.success){
    	                	if(data.data.sysAuthList) {
    	                		var btnArr = data.data.sysAuthList;
    	                		for(var p=0,len=btnArr.length; p<len; p++) {
    	                			if(btnArr[p].requiresPermissions == "button:cleanRules-save-ewarn") {
    	                				$("#save_ewarn_setting").show();
    	                				continue;
    	                			}
    	                			if(btnArr[p].requiresPermissions == "button:cleanRules-save-clean") {
    	                				$("#save_clean_setting").show();
    	                				continue;
    	                			}
    	                		}
    	                	}
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
            },
            execuCleanRules	:function(product,type,ruleId){
            	if(null == product || product == ""){
            		bootbox.alert({message:'操作失败，产品不能为空',size:'small'});
        			return false;
            	}
            	$.ajax({
            		async:false,
        			url : 'cleanRules/execuRule',
        			data : {product:product,type:type,ruleId:ruleId},
        			type : 'GET',
        			contentType:'application/json',
        			success: function(data){
        				var msgInfo = '操作失败';
        				if (data.success){
        					msgInfo = "操作成功";
    	                }
        				if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
        				bootbox.alert({message:msgInfo, size:'middle'});
        			},
        			error: function(msg){
        				bootbox.alert({message:'连接服务器错误',size:'small'});
        			}
              	});
            }  
        });
        return app;
});