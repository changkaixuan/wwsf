define(['underScore', 
	'text!application/wws_job_instance.html', 
	'text!application/wws_cron_expression_modal.html',
	'expression',
	'app-editable',
	'mloadding',
	'alertor',
	'bootstrap-table', 
	'bootstrap-table-locale', 
	'x-editable', 
	'icheck','spinner'],
    function ( _, template, expressionModal, exp, app_editable, mloadding, alertor) {
        var app = function () {
            this.o_template = $(template);
            this.o_expressionModal = $(expressionModal);
        }
      //页面所有的页签名称
        var itemList = ["second", "min", "hour", "day", "month", "week"];

        //初始化各个页签自主选择的最大值
        var max_second = 59;
        var max_min = 59;
        var max_hour = 23;
        var max_day = 31;
        var max_month = 12;
        var max_quarter = 12;
        var max_week = 7;
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
                o_container.append(this.o_expressionModal);
                var sel_prd = $("#query_product");
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
        	                	alertor.dangerAlert(msgInfo,'middle');
	  	    				}
	  	    			},
	  	    			error: function(msg){
	  	    				alertor.dangerAlert('连接服务器错误','small');
	  	    			}
	          	});
                $('#btn_job_ins_query', this.o_container).bind('click', function(event){ $('#tb_job_ins').bootstrapTable('refresh') });
                $('#btn_jobins_rerunfailed', this.o_container).bind('click', function(event){ self.jobRerunfailed(event); });
                $('#btn_jobins_rerunUnpassed', this.o_container).bind('click', function(event){ self.jobRerunUnpassed(event); });
                $('#btn_jobins_rerun', this.o_container).bind('click', function(event){ self.jobRerun(event); });
                $('#btn_jobins_disabled', this.o_container).bind('click', function(event){ self.jobDisabled(event); });
                $('#btn_jobins_driver', this.o_container).bind('click', function(event){ self.driver(event); });
                $('#btn_jobins_pause', this.o_container).bind('click', function(event){ self.jobPause(event); });
                $('#btn_jobins_continue', this.o_container).bind('click', function(event){ self.jobContinue(event); });
                $('#btn_jobins_del', this.o_container).bind('click', function(event){ self.del(event); });
                $('#btn_jobins_flowerChart', this.o_container).bind('click', function(event){
                	var selected = $('#tb_job_ins').bootstrapTable('getSelections');
                	var exportObj = [];
                	if(selected.length == 1){
                		$('#jobInsIdUseInFlowChart').val(selected[0].jobInsId);
                		$('#productUseInFlowChart').val(selected[0].product);
                	} else{
                		alertor.dangerAlert('请选中一条记录','small');
                		return;
                	}
                	$('#jobInsFlowChart').click();
                });
                self.initJobInsTable();
                //self.inition();
                
                $('#btn_job_ins_clear', this.o_container).bind('click', function(event){
                	$("select[name=product]").val("");
        	   		$("input[name=jobId]").val("");
        	   		$("input[name=jobInsId]").val("");
        	   		$("input[name=batch]").val("");
        	   		$("select[name=state]").val("");
                });
                
                $('#btn_task_ins_clear', this.o_container).bind('click', function(event){
        	   		$("input[name=taskInsId]").val("");
        	   		$("input[name=taskId]").val("");
        	   		$("select[name=taskState]").val("");
                });
                
                window.__proto__.isUndefined = function(o) {
                	return o ? o : "";
                };
            }, 
            initJobInsTable: function(){
            	var self = this;
            	$('#tb_job_ins').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'jobIns/list',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    toolbar: '#j_ins_tab_toolbar',                //工具按钮用哪个容器
                    striped: true,                      //是否显示行间隔色
                    cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                    pagination: true,                   //是否显示分页（*）
                    sortable: false,                     //是否启用排序
                    sortOrder: "asc",                   //排序方式
                    locale: "zh-CN",
                    queryParamsType:'undefined',
                    queryParams: function (params) {
                       return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                    	   		pageNumber: params.pageNumber,   //页面大小
                    	   		pageSize: params.pageSize,  //页码
                    	   		product: $("#query_product").val(),
                    	   		jobId: $("input[name='jobId']").val(),
                    	   		jobInsId: $("input[name='jobInsId']").val(),
                    	   		batch: $("input[name='batch']").val(),
                    	   		state: $("select[name='state']").val()
                            };
                        },           //传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "jobInsId",                     //每一行的唯一标识，一般为主键列
                    showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                   //是否显示父子表
                    showColumns: true,
                    showFullscreen: true,
                    showRefresh: true,
                    columns: [
                    	{checkbox: true}, 
                    	{field: 'jobId', title: '任务编号'}, 
                    	{field: 'jobInsId', title: '任务实例编号'}, 
                        {field: 'name', title: '任务名称' },
                        {field: 'mode', title: '调度模型', 
                        	formatter:function(value,row,index){
                        		return value==1?"网状调度":(value==2?"网维调度":"未知模式");
                            }
                        },
                        {field: 'batch', title: '批量' },
                        {field: 'state', title: '状态',
                        	formatter:function(value,row,index){
                        		return self.getStateDesc(value,row);
                            }
                        },
                        {field: 'pause', title: '是否暂停',
                        	formatter:function(value,row,index){
                        		if(value == 1){
	                    			return "<span class='badge badge-danger'>是</span>";
	                    		}else{
	                    			return "<span class='badge badge-info'>否</span>";
	                    		}
                            }
                        },
                        {field: 'creator', title: '创建人' },
                        {field: 'version', title: '版本' },
                        {field: 'initTime', title: '初始化时间' },
                        {field: 'beginTime', title: '开始时间' },
                        {field: 'endTime', title: '结束时间' }],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    onDblClickRow: function (row) {
                    	$.ajax({
                			data : {product:row.product,jobInsId:row.jobInsId},
                			url : 'jobIns',
                			type : 'GET',
                			contentType:'application/x-www-form-urlencoded',
                			success: function(data){
                				if(data.success){
                					$('#jobInsModal').modal();
                					if (data.data.job) {
                						var job = data.data.job;
                						var f = $('#tab_j_ins_baseinfo');
                						$('#btn_jobins_savejobins').unbind('click').bind('click', function(event){ self.saveJobIns(event); });
                			        	f.find("label[data-name=modeDesc]")[0].innerHTML = job.mode==1?"网状调度":(job.mode==2?"网维调度":"未知模式");
                			        	f.find("label[data-name=jobId]")[0].innerHTML = job.jobId;
                			        	f.find("label[data-name=name]")[0].innerHTML = job.name;
                			        	f.find("label[data-name=jobInsId]")[0].innerHTML = job.jobInsId;
                			        	f.find("label[data-name=batch]")[0].innerHTML = job.batch;
                			        	f.find("label[data-name=product]")[0].innerHTML = job.product;
                			        	f.find("select[name=state]").val(job.state);
                			        	f.find("label[data-name=scheduleRid]")[0].innerHTML = isUndefined(job.scheduleRid);
                			        	f.find("label[data-name=accutDate]")[0].innerHTML = isUndefined(job.accutDate);
                			        	f.find("input[name=pause]")[0].checked = job.pause==0?false:true;
                			        	f.find("label[data-name=initTime]")[0].innerHTML = job.initTime;
                			        	f.find("label[data-name=creator]")[0].innerHTML = job.creator;
                			        	f.find("label[data-name=createTime]")[0].innerHTML = self.undefinedNull(job.createTime);
                			        	f.find("label[data-name=beginTime]")[0].innerHTML = self.undefinedNull(job.beginTime);
                			        	f.find("label[data-name=endTime]")[0].innerHTML = self.undefinedNull(job.endTime);
                			        	f.find("label[data-name=version]")[0].innerHTML = job.version;
                			        	app_editable.initEditTable("tab_job_ins_properties",job.properties,"/properties","/updateCellProperties");
                			        	app_editable.initEditTable("tab_job_ins_parameters",job.parameters,"/parameters","/updateCellParameters");
                			        	
                			        	$('#btn_task_ins_query', this.o_container).unbind('click').bind('click', function(event){ $('#tb_tasks_instance').bootstrapTable('refresh'); });
                			        	$('#btn_taskins_rerun', this.o_container).unbind('click').bind('click', function(event){ self.taskRerun(event,job.product,job.jobInsId); });
                			        	$('#btn_taskins_kill', this.o_container).unbind('click').bind('click', function(event){ self.taskKill(event,job.product,job.jobInsId); });
                			        	$('#btn_taskins_recursion_rerun', this.o_container).unbind('click').bind('click', function(event){ self.taskRecursionRerun(event,job.product,job.jobInsId); });
                			        	$('#btn_taskins_disabled', this.o_container).unbind('click').bind('click', function(event){ self.taskDisabled(event,job.product,job.jobInsId); });
                			        	self.initTaskInsTable(job);
                					}
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
                    ajaxOptions: {
						beforeSend : function(){
	        				mloadding.showLoadding();
	        			},
	        			complete : function(){
	        				mloadding.hideLoadding();
	        			}
                    }
                });
            },
            getStateDesc:function(stateValue,row){
            	if(stateValue == "0"){
        			return "<span class='badge badge-dark'>暂停</span>";
        		}else if(stateValue == "1"){
        			return "<span class='badge badge-secondary'>初始化</span>";
        		}else if(stateValue == "2"){
        			if(row.countFailTask > 0){
        				return "<span class='badge badge-danger'>正在运行</span>";
        			}
        			return "<span class='badge badge-info'>正在运行</span>";
        		}else if(stateValue == "9"){
        			return "<span class='badge badge-success'>强制执行结束</span>";
        		}else if(stateValue == "10"){
        			return "<span class='badge badge-success'>执行结束</span>";
        		}else{
        			return "<span class='badge badge-dark'>未知</span>";
        		}
            },
            initTaskInsTable: function(row){
            	var self = this;
            	if(self.tb_tasks_instance){
            		$('#tb_tasks_instance').bootstrapTable("destroy");
            	}
            	self.tb_tasks_instance = $('#tb_tasks_instance').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'taskIns/list',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    toolbar: '#tb_tasks_instance_toolbar',                //工具按钮用哪个容器
                    striped: true,                      //是否显示行间隔色
                    cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                    pagination: true,                   //是否显示分页（*）
                    sortable: false,                     //是否启用排序
                    sortOrder: "asc",                 //排序方式
                    queryParamsType:'undefined',
                    queryParams: function (params) {
                       return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                    	   		pageNumber: params.pageNumber,   //页面大小
                    	   		pageSize: params.pageSize,  //页码
                    	   		product: row.product,
                    	   		jobInsId: row.jobInsId,
                    	   		taskId: $("input[name='taskId']").val(),
                    	   		taskInsId: $("input[name='taskInsId']").val(),
                    	   		state: $("select[name='taskState']").val()
                            };
                        },           //传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "taskInsId",                     //每一行的唯一标识，一般为主键列
                    showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                   //是否显示父子表
                    showColumns: true,
                    showFullscreen: false,
                    showRefresh: true,
                    columns: [
                    	{checkbox: true}, 
                    	{field: 'taskInsId', title: '编号'}, 
                    	/*{field: 'taskId', title: 'Task ID'},*/
                        {field: 'state', title: '状态',
                    		formatter:function(value,row,index){
                        		return self.getTaskStateDesc(value);
                            }	
                        },
                        {title:'日志', edit:false, align:'center',
                        	events:{
                            	'click .tab_taskInstance_style': function(e, value, row, index) {
                            		self.showTaskInsInfoWindow(row.jobInsId, row.taskInsId);
                            	}
                            },
                        	formatter:function(value,row,rowIndex){  
                            	return '<button type="button" class="btn btn-xs btn-default px-1 tab_taskInstance_style">查看</button>';
                        	}
                        },
                        {field: 'plugin', title: '插件' },
                        {field: 'maxNumOfExeErrors', title: '错误重试次数' },
                        {field: 'currentErrorExeCount', title: '当前错误次数' },
                        {field: 'errorDelay', title: '错误延迟时间' },
                        {field: 'errorIgnore', title: '错误忽略通过' },
                        {field: 'agent', title: '执行节点' },
                        {field: 'beginTime', title: '开始时间' },
                        {field: 'endTime', title: '结束时间' }],
                    responseHandler: function (res) {return res.data;},
                    onDblClickRow: function (taskRow) {
                    	$.ajax({
                			data : {product:row.product,jobInsId:row.jobInsId,taskInsId:taskRow.taskInsId},
                			url : 'taskIns',
                			type : 'GET',
                			contentType:'application/x-www-form-urlencoded',
                			success: function(data){
                				if(data.success){
                					$('#taskInsModal').modal();
                					var f = $('#taskInsModal');
                					//初始化工作日设定列表
                	            	var selJob = f.find("select[name=workingDay]");
                	            	$.ajax({
                	            		async:false,
                	            		url: 'calendar/getCalendarList?product='+row.product,
                	        			type : 'GET',
                	        			contentType:'application/x-www-form-urlencoded',
                	        	        success:function (data) {
                	        	        	if(data.success){
                	        	        		selJob.empty();
                	        	        		selJob.append("<option value=''>请选择工作日</option>");
                	        	        		var calendars = data.data.calendars;
                	        	        		if(calendars != undefined){
                	        	        			for(var i=0;i<calendars.length;i++){
                	        	        				if(calendars[i].calDescription != null && calendars[i].calDescription != ""){
                	        	        					selJob.append("<option value='" + calendars[i].calName + "'>" + calendars[i].calDescription + "</option>");
                	        	        				}else{
                	        	        					selJob.append("<option value='" + calendars[i].calName + "'>" + calendars[i].calName + "</option>");
                	        	        				}
                	            	        		}
                	        	        		}
                	        	        	} else {
                	        	        		var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
                	        	        		if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                            						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                            					}
                        	                	alertor.dangerAlert(msgInfo,'middle');
                	        	        	}
                	        	       },
                	        	       error: function (e) {
                	        	    	   alertor.dangerAlert('获取工作日下拉框数据出错' + e.status,'small');
                	        	       }  	
                	        		});
                					f.find("label[data-name=product]")[0].innerHTML = row.product;
                		        	f.find("label[data-name=jobInsId]")[0].innerHTML = data.data.task.jobInsId;
                		        	f.find("label[data-name=taskId]")[0].innerHTML = data.data.task.taskId;
                		        	f.find("label[data-name=name]")[0].innerHTML = self.undefinedNull(data.data.task.name);
                		        	f.find("label[data-name=taskInsId]")[0].innerHTML = data.data.task.taskInsId;
                		        	//f.find("select[name=state]").val(data.data.task.state);
                		        	f.find("label[data-name=stateDesc]")[0].innerHTML = self.getTaskStateDesc(data.data.task.state);
                		        	f.find("label[data-name=plugin]")[0].innerHTML = data.data.task.plugin;
                		        	f.find("input[name=errorDelay]").val(data.data.task.errorDelay);
                		        	f.find("input[name=maxNumOfExeErrors]").val(data.data.task.maxNumOfExeErrors);
                		        	f.find("input[name=currentErrorExeCount]").val(data.data.task.currentErrorExeCount);
                		        	f.find("label[data-name=errorIgnore]")[0].innerHTML = data.data.task.errorIgnore==1?'是':'否';
                		        	f.find("label[data-name=result]")[0].innerHTML = data.data.task.result;
                		        	/*if(data.data.task.useDefaultJobDimension){
                		        		f.find("label[data-name=useDefaultJobDimensionDesc]")[0].innerHTML = "是["+data.data.task.dimesionSchemeNo+"]";
                		        	}else{
                		        		f.find("label[data-name=useDefaultJobDimensionDesc]")[0].innerHTML = "否["+data.data.task.dimesionSchemeNo+"]";
                		        	}*/
                		        	f.find("label[data-name=useDefaultJobDimensionDesc]")[0].innerHTML = data.data.task.useJobDimension;
                		        	f.find("label[data-name=agent]")[0].innerHTML = self.undefinedNull(data.data.task.agent);
                		        	f.find("input[name=agentScope]").val(data.data.task.agentScope);
                		        	f.find("label[data-name=programName]")[0].innerHTML = data.data.task.programName;
                		        	if(data.data.task.enable == 1){
                		        		f.find("label[data-name=enableDesc]")[0].innerHTML = "是";
                		        	}else{
                		        		f.find("label[data-name=enableDesc]")[0].innerHTML = "否";
                		        	}
                		        	var beginTime = data.data.task.beginTime;
                		        	f.find("label[data-name=beginTime]")[0].innerHTML = self.undefinedNull(beginTime);
                		        	f.find("label[data-name=endTime]")[0].innerHTML = self.undefinedNull(data.data.task.endTime);
                		        	f.find("label[data-name=creator]")[0].innerHTML = data.data.task.creator;
                		        	f.find("label[data-name=createTime]")[0].innerHTML = data.data.task.createTime;
                		        	f.find("input[name=nextExecuteTime]").val(data.data.task.nextExecuteTime);
            			        	f.find("input[name=period]").val(data.data.task.period);
            			        	f.find("select[name=workingDay]").val(data.data.task.workingDay);
                		        	var tLeansRuntime = JSON.stringify(data.data.task.leansRuntime);
                		        	if(tLeansRuntime != "{}" && tLeansRuntime != "" && tLeansRuntime != undefined){
                		        		app_editable.initEditTableView("tab_task_ins_edit_leans",data.data.task.leansRuntime,"/tabTaskInsEditLeans","/updateCellTabTaskInsEditLeans");
                		        	}else{
                		        		$('#tab_task_ins_edit_leans').bootstrapTable('destroy');
                		        	}
                		        	if(data.data.task.beleans != undefined && data.data.task.beleans.length > 0){
                		        		app_editable.initEditTableView2("tab_task_ins_edit_beleans",data.data.task.beleans,"/tabTaskInsEditBeleans","/updateCellTabTaskInsEditBeleans","任务");
                		        	}else{
                		        		$('#tab_task_ins_edit_beleans').bootstrapTable('destroy');
                		        	}
            			        	app_editable.initEditTable("tab_task_ins_edit_parameters",data.data.task.parameters,"/taskInsEditParameters","/updateCellTabTaskInsEditParameters");
            			        	
                		        	$('#btn_taskins_savetaskins', this.o_container).unbind('click').bind('click', function(event){ self.saveTaskIns(row.product, row.jobInsId, taskRow.taskInsId); });
                		        	
                		        	$('#taskInsModal_period', this.o_container).unbind('click').bind('click', function(event){
                		        		exp.initExpression(this.id,this.value,exp.displayHDWMYTabArr);
            		                 	/*$('#cronExpSetModal').modal('show');
            		                 	var cronExpression = $('#cronExpSetModal').find("input[name=cronExpression]").val();
            		                 	if(cronExpression!=null && cronExpression!=""){
            		                 		$("#cron").val(cronExpression);
            		                 		$('#explain').trigger('click');
            		                 	}*/
                		        	});
	                		        /*$('#save_explain', this.o_container).bind('click', function(event){
            		                 	var cronExp = $("#cron").val();
            		                 	$('#taskInsModal').find("input[name=period]").val(cronExp);
            		                 	$('#cronExpSetModal').modal('hide');
            		                });*/
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
                });
            },
            undefinedNull : function(text){
            	return text ? text : '';
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
        			return "<span class='badge badge-info'>结果返回</span>";
        		}else if(stateValue == "16"){
        			return "<span class='badge badge-danger'>失败</span>";
        		}else if(stateValue == "17"){
        			return "<span class='badge badge-info'>待人干预</span>";
        		}else if(stateValue == "18"){
        			return "<span class='badge badge-info'>延后执行</span>";
        		}else if(stateValue == "20"){
        			return "<span class='badge badge-success'>成功通过</span>";
        		}else if(stateValue == "21"){
        			return "<span class='badge badge-success'>忽略通过</span>";
        		}else if(stateValue == "22"){
        			return "<span class='badge badge-success'>周期不满足</span>";
        		}else if(stateValue == "23"){
        			return "<span class='badge badge-success'>置无效通过</span>";
        		}else if(stateValue == "24"){
        			return "<span class='badge badge-success'>置失败通过</span>";
        		}else{
        			return "<span class='badge badge-dark'>未知状态</span>";
        		}
            },
            jobRerunfailed : function(event){
            	var selected = $('#tb_job_ins').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var hasUnpassJob = false;
            		var unpassjobs = [];
            		var jobs = [];
            		var products = [];
            		_.each(selected, function(element, index){
            			jobs.push(element.jobInsId);
            			products.push(element.product);
            			if (element.state != 2){//正在运行   Running
            				hasUnpassJob = true;
            				unpassjobs.push(element.jobInsId);
            			}
            		});
            		if(hasUnpassJob){
            			alertor.dangerAlert('<span class="badge badge-danger">任务已结束</span>,无法进行失败重运行<br>' + unpassjobs.join(';'),'middle');
            			return;
            		}
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : alertor.warningMessage('是否确定重新执行所有失败的任务项吗？'),
                		callback : function (result) {
                    		if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			data : {products:products.join('|'),jobInsIds:jobs.join('|')},
                        			url : 'jobIns/jobRerunfailed',
                        			type : 'PUT',
                        			contentType:'application/x-www-form-urlencoded',
                        			success: function(data){
                        				if(data.success){
                        					alertor.successAlert('请求已提交，请在任务项界面查看状态','small');
                        					$('#tb_job_ins').bootstrapTable('refresh');
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
                        			},beforeSend : function(){
                        				mloadding.showLoadding();
                        			},
                        			complete : function(){
                        				mloadding.hideLoadding();
                        			}
                        		});
                    		}
                    	 }
                	});
            	} else{
            		alertor.dangerAlert('请选中一个任务实例','small');
            	}
            },
            jobRerunUnpassed : function(event){
            	var selected = $('#tb_job_ins').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var hasUnpassJob = false;
            		var unpassjobs = [];
            		var jobs = [];
            		var products = [];
            		_.each(selected, function(element, index){
            			jobs.push(element.jobInsId);
            			products.push(element.product);
            			if (element.state != 2){//正在运行   Running
            				hasUnpassJob = true;
            				unpassjobs.push(element.jobInsId);
            			}
            		});
            		if(hasUnpassJob){
            			bootbox.alert({message:'只有正在运行的任务才能被执行，'+unpassjobs.join(';'),size:'small'});
            			return;
            		}
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : alertor.warningMessage('是否确定重新执行所有未通过的任务项吗？'),
                		callback : function (result) {
                    		if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			data : {products:products.join('|'),jobInsIds:jobs.join('|')},
                        			url : 'jobIns/jobRerunUnpassed',
                        			type : 'PUT',
                        			contentType:'application/x-www-form-urlencoded',
                        			success: function(data){
                        				if(data.success){
                        					alertor.successAlert('请求已提交，请在任务项界面查看状态','small');
                        					$('#tb_job_ins').bootstrapTable('refresh');
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
                        			},beforeSend : function(){
                        				mloadding.showLoadding();
                        			},
                        			complete : function(){
                        				mloadding.hideLoadding();
                        			}
                        		});
                    		}
                    	 }
                	});
            	} else{
            		alertor.dangerAlert('请选中一个任务实例','small');
            	}
            },
            jobRerun : function(event){
            	var selected = $('#tb_job_ins').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var jobs = [];
            		var products = [];
            		_.each(selected, function(element, index){
            			jobs.push(element.jobInsId);
            			products.push(element.product);
            		});
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : alertor.warningMessage('是否确定重新执行所有任务项吗？'),
                		callback : function (result) {
                    		if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			data : {products:products.join('|'),jobInsIds:jobs.join('|')},
                        			url : 'jobIns/jobRerun',
                        			type : 'PUT',
                        			contentType:'application/x-www-form-urlencoded',
                        			success: function(data){
                        				if(data.success){
                        					alertor.successAlert('请求已提交，请在任务项界面查看状态','small');
                        					$('#tb_job_ins').bootstrapTable('refresh');
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
                        			},beforeSend : function(){
                        				mloadding.showLoadding();
                        			},
                        			complete : function(){
                        				mloadding.hideLoadding();
                        			}
                        		});
                    		}
                    	 }
                	});
            	} else{
            		alertor.dangerAlert('请选中一个任务实例','small');
            	}
            },
            jobDisabled : function(event){
            	var selected = $('#tb_job_ins').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var hasUnpassJob = false;
            		var unpassjobs = [];
            		var jobs = [];
            		var products = [];
            		_.each(selected, function(element, index){
            			jobs.push(element.jobInsId);
            			products.push(element.product);
            			if (element.state >= 9){//未结束的任务
            				hasUnpassJob = true;
            				unpassjobs.push(element.jobInsId);
            			}
            		});
            		if(hasUnpassJob){
            			alertor.dangerAlert('只有未结束的任务才能被设置为通过，'+unpassjobs.join(';'),'small');
            			return;
            		}
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : alertor.warningMessage('是否确定将所有未通过的任务设置为通过吗？'),
                		callback : function (result) {
                    		if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			data : {products:products.join('|'),jobInsIds:jobs.join('|')},
                        			url : 'jobIns/jobDisabled',
                        			type : 'PUT',
                        			contentType:'application/x-www-form-urlencoded',
                        			success: function(data){
                        				if(data.success){
                        					alertor.successAlert('请求已提交，请在任务项界面查看状态','small');
                        					$('#tb_job_ins').bootstrapTable('refresh');
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
                        			},beforeSend : function(){
                        				mloadding.showLoadding();
                        			},
                        			complete : function(){
                        				mloadding.hideLoadding();
                        			}
                        		});
                    		}
                    	 }
                	});
            	} else{
            		alertor.dangerAlert('请选中一个任务实例','small');
            	}
            },
            driver : function(event){
            	var selected = $('#tb_job_ins').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var jobs = [];
            		var products = [];
            		_.each(selected, function(element, index){
            			var job = {};
            			job.jobInsId = element.jobInsId;
            			job.product = element.product;
            			jobs.push(job);
            		});
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : alertor.warningMessage('是否确定驱动任务？'),
                		callback : function (result) {
                    		if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			data : JSON.stringify(jobs),
                        			url : 'jobIns/jobDriver',
                        			type : 'POST',
                        			contentType:'application/json',
                        			success: function(data){
                        				var response = JSON.parse(data);
                        				if(response.success){
                        					alertor.successAlert('请求已提交，请在任务项界面查看状态','small');
                        					$('#tb_job_ins').bootstrapTable('refresh');
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
                        			},beforeSend : function(){
                        				mloadding.showLoadding();
                        			},
                        			complete : function(){
                        				mloadding.hideLoadding();
                        			}
                        		});
                    		}
                    	 }
                	});
            	} else{
            		alertor.dangerAlert('请选中一个任务实例','small');
            	}
            },
            jobPause : function(event){
            	var selected = $('#tb_job_ins').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var hasUnpassJob = false;
            		var unpassjobs = [];
            		var jobs = [];
            		var products = [];
            		_.each(selected, function(element, index){
            			jobs.push(element.jobInsId);
            			products.push(element.product);
            			if (element.state != 2 || element.pause == 1){//正在运行   Running
            				hasUnpassJob = true;
            				unpassjobs.push(element.jobInsId);
            			}
            		});
            		if(hasUnpassJob){
            			alertor.dangerAlert('只有正在运行的任务才能被暂停<br>'+unpassjobs.join(';'),'small');
            			return;
            		}
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : alertor.warningMessage('是否确定暂停所有未运行任务吗？'),
                		callback : function (result) {
                    		if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			data : {products:products.join('|'),jobInsIds:jobs.join('|')},
                        			url : 'jobIns/jobPause',
                        			type : 'PUT',
                        			contentType:'application/x-www-form-urlencoded',
                        			success: function(data){
                        				if(data.success){
                        					alertor.successAlert('请求已提交，请在任务项界面查看状态','small');
                        					$('#tb_job_ins').bootstrapTable('refresh');
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
                        			},beforeSend : function(){
                        				mloadding.showLoadding();
                        			},
                        			complete : function(){
                        				mloadding.hideLoadding();
                        			}
                        		});
                    		}
                    	 }
                	});
            	} else{
            		alertor.dangerAlert('请选中一个任务实例','small');
            	}
            },
            jobContinue : function(event){
            	var selected = $('#tb_job_ins').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var hasUnpassJob = false;
            		var unpassjobs = [];
            		var jobs = [];
            		var products = [];
            		_.each(selected, function(element, index){
            			jobs.push(element.jobInsId);
            			products.push(element.product);
            			if (!selected[0].pause){//暂停  PAUSE
            				hasUnpassJob = true;
            				unpassjobs.push(element.jobInsId);
            			}
            		});
            		if(hasUnpassJob){
            			alertor.dangerAlert('只有暂停的任务才能继续运行，'+unpassjobs.join(';'),'small');
            			return;
            		}
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : alertor.warningMessage('是否确定继续运行所有暂停的任务吗？'),
                		callback : function (result) {
                    		if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			data : {products:products.join('|'),jobInsIds:jobs.join('|')},
                        			url : 'jobIns/jobContinue',
                        			type : 'PUT',
                        			contentType:'application/x-www-form-urlencoded',
                        			success: function(data){
                        				if(data.success){
                        					alertor.successAlert('请求已提交，请在任务项界面查看状态','small');
                        					$('#tb_job_ins').bootstrapTable('refresh');
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
                        			},beforeSend : function(){
                        				mloadding.showLoadding();
                        			},
                        			complete : function(){
                        				mloadding.hideLoadding();
                        			}
                        		});
                    		}
                    	 }
                	});
            	} else{
            		alertor.dangerAlert('请选中一个任务实例','small');
            	}
            },
            del : function(event){
            	var selected = $('#tb_job_ins').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var hasUnpassJob = false;
            		var unpassjobs = [];
            		var jobs = [];
            		var products = [];
            		_.each(selected, function(element, index){
            			jobs.push(element.jobInsId);
            			products.push(element.product);
            			if (element.state == 2){
            				hasUnpassJob = true;
            				unpassjobs.push(element.jobInsId);
            			}
            		});
            		if(hasUnpassJob){
            			alertor.dangerAlert('操作失败,只能被删除非运行中的任务实例，'+unpassjobs.join(';'),'small');
            			return;
            		}
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : alertor.warningMessage('是否确定删除所选任务实例吗？'),
                		callback : function (result) {
                    		if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			url : 'jobIns/' + products.join('|') + '/' + jobs.join('|'),
                        			type : 'DELETE',
                        			contentType:'application/json',
                        			success: function(data){
                        				if(data.success){
                        					alertor.successAlert('操作成功','small');
                        					$('#tb_job_ins').bootstrapTable('refresh');
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
                        			},beforeSend : function(){
                        				mloadding.showLoadding();
                        			},
                        			complete : function(){
                        				mloadding.hideLoadding();
                        			}
                        		});
                    		}
                    	 }
                	});
            	} else{
            		alertor.dangerAlert('请选择需要删除的任务实例','small');
            	}
            },
            saveJobIns : function(event){
            	var self = this;
            	var f = $('#tab_j_ins_baseinfo');
            	//product
            	var product = f.find("label[data-name=product]")[0].innerHTML
            	//jobInsId
            	var jobInsId = f.find("label[data-name=jobInsId]")[0].innerHTML
            	// state
            	var state = f.find("select[name=state]").val();
            	// pause
            	var pause = f.find("input[name=pause]")[0].checked?1:0;
            	//properties
            	var properties = "";
            	var propertiesObj = $('#tab_job_ins_properties').bootstrapTable("getData");
            	if(propertiesObj.length > 0){
            		var kAndVs = self.getKeyAndValues(propertiesObj);
            		if(kAndVs.errorInfo != ""){
            			bootbox.alert({message:kAndVs.errorInfo,size:'small'});
            			return false;
            		}
            		if(!jQuery.isEmptyObject(kAndVs.data)){
        				properties = JSON.stringify(kAndVs.data);
        			}
            	}
            	//parameters
            	var parameters = "";
            	var parametersObj = $('#tab_job_ins_parameters').bootstrapTable("getData");
            	if(parametersObj.length > 0){
            		var kAndVs = self.getKeyAndValues(parametersObj);
            		if(kAndVs.errorInfo != ""){
            			bootbox.alert({message:kAndVs.errorInfo,size:'small'});
            			return false;
            		}
            		if(!jQuery.isEmptyObject(kAndVs.data)){
            			parameters = JSON.stringify(kAndVs.data);
        			}
            	}
            	if(!mloadding.showLoadding()){
            		return false;
            	}
            	$.ajax({
        			//data : {product:product,jobInsId:jobInsId,jobPriority:jobPriority,properties:properties,parameters:parameters},
        			data : {product:product,jobInsId:jobInsId,properties:properties,parameters:parameters,state:state, pause:pause},
        			url : 'jobIns/saveJobIns',
        			type : 'PUT',
        			contentType:'application/x-www-form-urlencoded',
        			success: function(data){
        				if(data.success){
        					alertor.successAlert('保存成功','small');
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	alertor.dangerAlert(msgInfo,'middle');
        				}
        				mloadding.hideLoadding();
        			},
        			error: function(msg){
        				alertor.dangerAlert('连接服务器错误','small');
        				mloadding.hideLoadding();
        			}
        		});
            },
            getKeyAndValues: function(obj){
            	var retJson = {"errorInfo":"","data":""};
            	var dataJson = {};
        		for(var i=0;i<obj.length;i++){
        			if(obj[i].key == "" && obj[i].value != ""){
    					retJson.errorInfo = "操作失败，第"+(i+1)+"行,属性不能为空";
    					break;
    				}
        			if(!validateStringLength(obj[i].key,200)){
                		bootbox.alert({message:"操作失败，第"+(i+1)+"行,属性的长度不能超过200",size:'small'}); 
                		return false;
                	}
    				if(obj[i].key != "" && obj[i].value == ""){
    					retJson.errorInfo = "操作失败，第"+(i+1)+"行,值不能为空";
    					break;
    				}
    				if(obj[i].key != "" && obj[i].value != ""){
    					dataJson[obj[i].key] = obj[i].value;
    				}
        		}
        		if(retJson.errorInfo == ""){
        			retJson.data = dataJson;
        		}
            	return retJson;
            },
            taskKill : function(event,product,jobInsId){
            	var selected = $('#tb_tasks_instance').bootstrapTable('getSelections');
            	var taskInsId = "";
            	if(selected.length == 1){
            		taskInsId = selected[0].taskInsId;
            	} else {
            		alertor.dangerAlert('请选择一条任务项','small');
            		return;
            	}
        		var tConfirm = true;
        		bootbox.confirm({
            		size : 'small',
            		message : alertor.warningMessage('确定终止任务项吗？'),
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			data : {product:product,jobInsId:jobInsId,taskInsId:taskInsId},
                    			url : 'taskIns/kill',
                    			type : 'PUT',
                    			contentType:'application/x-www-form-urlencoded',
                    			success: function(data){
                    				if(data.success){
                    					alertor.successAlert('请求已提交，请在任务项界面查看状态','small');
                    					$('#tb_tasks_instance').bootstrapTable('refresh');
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
                	 }
            	});
            },
            taskRerun : function(event,product,jobInsId){
            	var selected = $('#tb_tasks_instance').bootstrapTable('getSelections');
            	if(selected.length < 1){
            		alertor.dangerAlert('请至少选中一条任务项','small');
            		return;
            	}
            	var taskInsIds = "";
        		var tasks = [];
        		_.each(selected, function(element, index){
        			tasks.push(element.taskInsId);
        		});
        		taskInsIds = tasks.join('|');
        		var tConfirm = true;
            	bootbox.confirm({
            		size : 'small',
            		message : alertor.warningMessage('是否确定重新执行任务项吗？'),
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			data : {product:product,jobInsId:jobInsId,taskInsIds:taskInsIds},
                    			url : 'taskIns/taskRerun',
                    			type : 'PUT',
                    			contentType:'application/x-www-form-urlencoded',
                    			success: function(data){
                    				if(data.success){
                    					alertor.successAlert('请求已提交，请在任务项界面查看状态','small');
                    					$('#tb_tasks_instance').bootstrapTable('refresh');
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
                	 }
            	});
            },
            taskRecursionRerun : function(event,product,jobInsId){
            	var selected = $('#tb_tasks_instance').bootstrapTable('getSelections');
            	if(selected.length < 1){
            		alertor.dangerAlert('请至少选中一条任务项','small');
            		return;
            	}
            	var taskInsIds = "";
        		var tasks = [];
        		_.each(selected, function(element, index){
        			tasks.push(element.taskInsId);
        		});
        		taskInsIds = tasks.join('|');
        		var tConfirm = true;
            	bootbox.confirm({
            		size : 'small',
            		message : alertor.warningMessage('是否确定递归重新执行任务项吗？'),
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			data : {product:product,jobInsId:jobInsId,taskInsIds:taskInsIds},
                    			url : 'taskIns/taskRecursionRerun',
                    			type : 'PUT',
                    			contentType:'application/x-www-form-urlencoded',
                    			success: function(data){
                    				if(data.success){
                    					alertor.successAlert('请求已提交，请在任务项界面查看状态','small');
                    					$('#tb_tasks_instance').bootstrapTable('refresh');
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
                	 }
            	});
            },
            taskDisabled : function(event,product,jobInsId){
            	var selected = $('#tb_tasks_instance').bootstrapTable('getSelections');
            	if(selected.length < 1){
            		alertor.dangerAlert('请至少选中一条任务项','small');
            		return;
            	}
            	var taskInsIds = "";
            	var hasUnpassTask = false;
        		var unpasstasks = [];
        		var tasks = [];
        		_.each(selected, function(element, index){
        			tasks.push(element.taskInsId);
        			if (element.state >= 20){//未结束的任务
        				hasUnpassTask = true;
        				unpasstasks.push(element.taskInsId);
        			}
        		});
        		if(hasUnpassTask){
        			bootbox.alert({message:'只有未结束的任务才能被设置为通过，'+unpasstasks.join(';'),size:'small'});
        			return;
        		}
        		taskInsIds = tasks.join('|');
        		var tConfirm = true;
            	bootbox.confirm({
            		size : 'small',
            		message : alertor.warningMessage('是否确定将未通过的任务设置为通过吗？'),
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			data : {product:product,jobInsId:jobInsId,taskInsIds:taskInsIds},
                    			url : 'taskIns/taskDisabled',
                    			type : 'PUT',
                    			contentType:'application/x-www-form-urlencoded',
                    			success: function(data){
                    				if(data.success){
                    					alertor.successAlert('请求已提交，请在任务项界面查看状态','small');
                    					$('#tb_tasks_instance').bootstrapTable('refresh');
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
                	 }
            	});
            },
            saveTaskIns : function(product,jobInsId,taskInsId){
            	var self = this;
            	var f = $('#taskInsModal');
            	var formJson = {};
        		formJson.product = product;
        		formJson.jobInsId = jobInsId+"";
        		formJson.taskInsId = taskInsId;
        		//formJson.state = $(viewDivId).find("select[name=state]").val();
        		//errorDelay
        		formJson.errorDelay = f.find("input[name=errorDelay]").val();
        		if(null == formJson.errorDelay || formJson.errorDelay == ""){
                	alertor.dangerAlert('操作失败，错误延迟时间不能为空','small');
        			return false;
                }
        		if(isNaN(formJson.errorDelay)){
        			alertor.dangerAlert('操作失败，错误延迟时间的数据类型应为数字型','small');
        			return false;
        		}
        		if(!validateStringLength(formJson.errorDelay,9)){
            		alertor.dangerAlert('操作失败，错误延迟时间的长度不能超过9','small'); 
            		return false;
            	}
        		//maxNumOfExeErrors
        		formJson.maxNumOfExeErrors = f.find("input[name=maxNumOfExeErrors]").val();
        		if(null == formJson.maxNumOfExeErrors || formJson.maxNumOfExeErrors == ""){
                	alertor.dangerAlert('操作失败，最大错误重试次数不能为空','small');
        			return false;
                }
        		if(isNaN(formJson.maxNumOfExeErrors)){
        			alertor.dangerAlert('操作失败，最大错误重试次数的数据类型应为数字型','small');
        			return false;
        		}
        		if(!validateStringLength(formJson.maxNumOfExeErrors,9)){
            		alertor.dangerAlert('操作失败，最大错误重试次数的长度不能超过9','small'); 
            		return false;
            	}
        		//currentErrorExeCount
        		formJson.currentErrorExeCount = f.find("input[name=currentErrorExeCount]").val();
        		if(null == formJson.currentErrorExeCount || formJson.currentErrorExeCount == ""){
                	alertor.dangerAlert('操作失败，当前错误次数不能为空','small');
        			return false;
                }
        		if(isNaN(formJson.currentErrorExeCount)){
        			alertor.dangerAlert('操作失败，当前错误次数的数据类型应为数字型','small');
        			return false;
        		}
        		if(!validateStringLength(formJson.currentErrorExeCount,9)){
            		alertor.dangerAlert('操作失败，当前错误次数的长度不能超过9','small'); 
            		return false;
            	}
        		//agentScope
        		formJson.agentScope = f.find("input[name=agentScope]").val();
        		if(!validateStringLength(formJson.agentScope,500)){
            		alertor.dangerAlert('操作失败，执行节点范围的长度不能超过500','small'); 
            		return false;
            	}
        		//period
        		formJson.period = f.find("input[name=period]").val();
        		//nextExecuteTime
        		formJson.nextExecuteTime = f.find("input[name=nextExecuteTime]").val();
        		if(!validateStringLength(formJson.nextExecuteTime,30)){
            		alertor.dangerAlert('操作失败，下次执行时间的长度不能超过30','small'); 
            		return false;
            	}
        		//workingDay
        		formJson.workingDay = f.find("select[name=workingDay]").val();
        		//parameters
        		var isSaveFlag = true;
        		var parametersJson = {};
            	var parametersObj = $('#tab_task_ins_edit_parameters').bootstrapTable("getData");
            	if(parametersObj.length > 0){
            		var kAndVs = self.getKeyAndValues(parametersObj);
            		if(kAndVs.errorInfo != ""){
            			alertor.dangerAlert(kAndVs.errorInfo,'small');
            			return false;
            		}
            		parametersJson = kAndVs.data;
            	}
        		formJson.parameters = parametersJson;
        		if(!mloadding.showLoadding()){
            		return false;
            	}
            	$.ajax({
        			data : {formJson:JSON.stringify(formJson)},
        			url : 'taskIns/saveTaskIns',
        			type : 'PUT',
        			contentType:'application/x-www-form-urlencoded',
        			success: function(data){
        				if(data.success){
        					alertor.successAlert('保存成功','small');
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	alertor.dangerAlert(msgInfo,'middle');
        				}
        				mloadding.hideLoadding();
        			},
        			error: function(msg){
        				alertor.dangerAlert('连接服务器错误','small');
        				mloadding.hideLoadding();
        			}
        		});
            },
            showTaskInsInfoWindow: function(jobInsId,taskInsId){
                $.ajax({
        			data : {jobInsId:jobInsId,taskInsId:taskInsId},
        			url : 'getTaskInsInfo',
        			type : 'GET',
        			contentType:'application/x-www-form-urlencoded',
        			success: function(data){
        				var responseData = data.data;
        				if(data.success){
        					if (responseData && responseData != null) {
        						var tinfo = responseData.information;
            					if (tinfo && tinfo != null && tinfo != '') {
            						$("#log_info_area").html('<pre class=\"shellLog\">' + $.trim(tinfo) + '</pre>');
        	                    	$("#taskInsInfoWindowModal").modal('show');
            					} else {
            						alertor.dangerAlert('没有执行日志','small');
            					}
        					} else {
        						alertor.dangerAlert('没有执行日志','small');
        					}
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
        });
        return app;
});