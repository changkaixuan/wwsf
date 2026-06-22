﻿define([
		'underScore', 
		'text!application/wws_job_template_layout.html',
		'mloadding',
		'cronjobtemtaskjobinstask-editable2',
		'text!application/wws_cron_expression_modal.html',
		'expression',
		'alertor',
		'select2',
		'css!css/select2.min.css',
		'bootstrap-table', 
		'bootstrap-table-locale', 
		'bootstrap-tagsinput',
		'x-editable', 'mockjax', 'icheck', 'spinner' ], function(_, template, mloadding, ceditable, expressionModal, exp, alertor) {
	var app = function() {
		this.el = $(template);
		this.dragCreatTaskSaved = false;
		this.o_expressionModal = $(expressionModal);
	};
	var job = {};//job对象
	var reg2 = /^[0-9a-zA-Z_]+$/;
	var result = [];
	var resultTasks = [];
	
	_.extend(app.prototype, {
		load : function() {
			
		},
		render : function(container) {
			var self = this;
            var o_container = $(container);
            o_container.empty();
            o_container.append(this.el);
            o_container.append(this.o_expressionModal);
          	$.ajax({
        		async:false,
    			url : 'product/selectProduct',
    			type : 'GET',
    			contentType:'application/json',
    			success: function(data){
    				if(data.success){
    						var products = data.data.products;
    						var prodSel = $("select[name=product]");
    						prodSel.empty();
    						prodSel.append("<option value=''>请选择产品</option>");
    						$.each(products,function(i){
    							prodSel.append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
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
          	
            $('#btn_job_template_add', this.o_container).bind('click', function(event){
        		self.addJob("new");
			});
            $('#btn_job_template_copy_add', this.o_container).bind('click', function(event){
        		self.copyAddJob();
			});
            $('#btn_job_template_query', this.o_container).bind('click', function(event){ 
    			$('#tb_job').bootstrapTable('refresh') 
    		});
            $('#btn_job_template_clear', this.o_container).bind('click', function(event){
            	$("select[name=product]").val("");
    	   		$("input[name=jobId]").val("");
    	   		$("select[name=mode]").val("0");
            });
            $('#btn_job_template_del', this.o_container).bind('click', function(event){ 
        		self.delJob(event); 
        	});
            //导出事件
            $('#btn_job_template_export', this.o_container).bind('click', function(event){
            	var selected = $('#tb_job').bootstrapTable('getSelections');
            	if(selected.length < 1){
            		var qProduct = $("#nav_jtpl_content_list").find("select[name=product]").val();
            		if(null == qProduct || qProduct == ""){
            			bootbox.alert({message:'操作失败，查询条件，产品不能为空',size:'small'}); 
            			return false;
            		}
            	}else{
            		var sameProductArr = [];
            		_.each(selected, function(element, index){
            			if(jQuery.inArray(element.product, sameProductArr) == -1){
            				sameProductArr.push(element.product);
            			}
            		});
            		if(sameProductArr.length > 1){
            			bootbox.alert({message:'操作失败，只能导出同产品任务模板',size:'small'}); 
            			return false;
            		}
            	}
            	self.openExportJobTemplateModal();
            });
            //导入事件
            $('#btn_job_template_import', this.o_container).bind('click', function(event){
                self.openImportJobTemplateModal();
            });
            $('#btn_job_flowChart', this.o_container).bind('click', function(event){
            	var selected = $('#tb_job').bootstrapTable('getSelections');
            	if(selected.length == 1){
            		self.forkTaskGraphicalTabpanel(selected[0]);
            	} else{
            		bootbox.alert({message:'请选中一条任务模板',size:'small'});
            		return;
            	}
            });
            $('#btn_job_edit_tasklist', this.o_container).bind('click', function(event){
            	var selected = $('#tb_job').bootstrapTable('getSelections');
            	if(selected.length == 1){
            		self.forkTaskTabpanel(selected[0]);
            	} else{
            		bootbox.alert({message:'请选中一条任务模板',size:'small'});
            		return;
            	}
            });
            /*$('#btn_job_batchRun', this.o_container).bind('click', function(event){
            	var selected = $('#tb_job').bootstrapTable('getSelections');
            	if(selected.length == 1){
            		product = selected[0].product;
            		jobId = selected[0].jobId;
            	} else{
            		bootbox.alert({message:'请选中一条任务模板',size:'small'});
            		return;
            	}
            	self.batchRun(product,jobId);
            });*/
            self.initJobsTable();
            
            $('#jobModal_template select[name=product]').bind('change', function(event){
            	self.productChanged(event);
            });
            $('#jobModal_template select[name=mode]').bind('change', function(event){
            	self.jobModeChanged(event);
            });
            
            $("#btn_job_batchRun_history").bind('click', function(event) {
            	var selected = $('#tb_job').bootstrapTable('getSelections');
            	if(selected.length == 1){
            		product = selected[0].product;
            		jobId = selected[0].jobId;
            	} else{
            		bootbox.alert({message:'请选中一条任务模板',size:'small'});
            		return;
            	}
            	self.batchRunHistory(product,jobId);
            });
            
            // 历史启动实例 -> 左侧历史列表收缩展开
    		$(".hitory_list_panel").hide();
            $(".left_history_list").mouseover(function() {
            	$(this).stop().animate({
            		width : "50%",
            		backgroundColor : "#F0F0F0"
            	}).mouseout(function() {
            		$(this).stop().animate({
            			width : "10px",
            			backgroundColor : "#d0d0d0"
            		});
            		$(".hitory_list_panel").hide();
            	});
            	$(".hitory_list_panel").show();
            });
		},
		forkTaskGraphicalTabpanel(row){
			var self = this;
			var navPill = $('#nav_jtpl');
			if ($('#nav_jtpl a').length > 6) {
				bootbox.alert({message:'创建选项卡过多',size:'small'});
				return;
			}
			var ckId = "dag_flowchart";
			var navLink = $('#nav_jtpl_dag_flowchart');
			if (navLink.length > 0) {
				bootbox.alert({message:'只能打开一个DAG流程编辑器',size:'small'});
				return;
			} else {
				var item = {'id':ckId,'name':row.jobId+'(DAG)','closable':true};
    			var panelId = self.addPill(navPill, item);
    			require([ "application/wws_job_template_task_graphical" ], function(ModuleClass) {
    				var oModule = new ModuleClass(self, row, panelId);
    				oModule.load();
    				oModule.render('#'+panelId);
    			});
			}
		},
		forkTaskTabpanel(row){
			var self = this;
			var navPill = $('#nav_jtpl');
			if ($('#nav_jtpl a').length > 6) {
				bootbox.alert({message:'创建选项卡过多',size:'small'});
				return;
			}
			var navLink = $('#nav_jtpl_'+row.jobId);
			if (navLink.length > 0) {
				navLink.get(0).click();
				return;
			}
			var item = {'id':row.jobId,'name':row.jobId,'closable':true};
			var panelId = self.addPill(navPill, item);
			require([ "application/wws_job_template_task_list" ], function(ModuleClass) {
				var oModule = new ModuleClass(self, row, panelId);
				oModule.load();
				oModule.render('#'+panelId);
			});
		},
		initJobsTable: function(){
        	var self = this;
        	$('#tb_job').bootstrapTable({
        		theadClasses: 'thead-light',
                url: 'template/jobList',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
                toolbar: '#tab_jtpl_toolbar',                //工具按钮用哪个容器
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
                	   		product: $("select[name='product']").val(),
                	   		jobId: $("input[name='jobId']").val(),
                	   		mode: $("select[name='mode']").val()//,
                	   		//isEnable: $("select[name='isEnable']").val()
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
                	{field: 'product', title: '产品'},
        			{field: 'jobId', title: '任务编号'},
        			{field: 'name', title: '任务名称'},
        			{field: 'mode', title: '调度模型',formatter:function(value, row, index){
        				if(value == 2){
        					return "网维调度";
        				} else {
        					return "网状调度";
        				}
        			}},
        			{field: 'version', title: '版本'},
        			{field: 'creator', title: '创建人'},
        			{field: 'createTime', title: '创建时间', align: 'center'}],
                responseHandler: function (res) {
                	return res.data;
                },
                onDblClickRow: function (row) {//双击
                	self.showJobInfo(row.product,row.jobId,false); 
                	self.initTaskTable(row);
                	$("#addOrEdit").empty();
                	$("#addOrEdit").append("编辑");
                }
            });
        },
        showJobInfo:function(pProduct,jobId,flag){
        	
        	
        	var self = this;
        	var sel_prd = $("#tabs_jtpl_baseinfo").find("select[name=product]");
        	sel_prd.empty();
          	$.ajax({
        		async:false,
    			url : 'product/selectProduct',
    			type : 'GET',
    			contentType:'application/json',
    			success: function(data){
    				if(data.success){
    						var products = data.data.products;
    						sel_prd.append("<option value=''>请选择产品</option>");
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
        	$.ajax({
  				url:'template/getDimensionByProduct',
  				async:false,
  				type:"GET",
  				data:{product:pProduct},
  				success:function (data) {
  					if(data.success){
  						result = [];
  						var dimensions = data.data.dimensions;
      					$.each(dimensions,function(i){
      						result.push({value:dimensions[i].name,text:dimensions[i].name});
      					});
  					} else {
  						var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
	                	alertor.dangerAlert(msgInfo,'middle');
  					}	
  				},
  				error: function (e) {
  					bootbox.alert({message:'获取维度失败',size:'small'});
  	   	         } 
  		    });
        	var f = $('#tabs_jtpl_baseinfo');
        	f.find("select[name=product]")[0].disabled = true;
			f.find("input[name=jobId]")[0].disabled = true;
			if(flag){
				f.find("select[name=mode]")[0].disabled = true;
			}
        	$.ajax({
        		async:false,
    			data : {product:pProduct,jobId:jobId},
    			url : 'template/getJob',
    			type : 'GET',
    			contentType:'application/json',
    			success: function(data){
    				if(data.success){
    					$('#jobModal_template').modal('show');
    					if (data.data.job) {
    						job = data.data.job;
    						mutexObject = job.mutexGroups;
    						product = job.product;
    						$('#saveJobinfo').unbind('click').bind('click', function(event){
    							app.prototype.saveJobinfo();
    						});
    						f.find("select[name=product]").val(job.product);
    			        	f.find("input[name=jobId]").val(job.jobId);
    			        	f.find("select[name=mode]").val(job.mode);
    			        	f.find("input[name=name]").val(job.name);
    			        	f.find("input[name=scheduleRid]").val(job.scheduleRid);
    			        	//if(job.enable){
    			        		//f.find("select[name=isEnable]").val(1);	
    			        	//} else {
    			        		//f.find("select[name=isEnable]").val(0);	
    			        	//}
    			        	$('#job_tags').tagsinput({style:'min-width:80%;'});
    			        	if(job.title != undefined){
    			        		$("#job_tags").tagsinput("removeAll");
    			        		var tags = job.title.split(",");
    			        		$.each(tags,function(i){
        			        		$('#job_tags').tagsinput("add",tags[i]);
        			        	})
    			        	} else {
    			        		$("#job_tags").tagsinput("removeAll");
    			        	}
    			        	f.find("input[name=creator]").val(job.creator);
    			        	f.find("input[name=createTime]").val(job.createTime);
    			        	f.find("input[name=version]").val(job.version);
    			        	self.initializeTableEdit("#tab_job_template_properties",job.jobParamterIsMust,job.properties,"/properties","/updateCellProperties","属性","默认值","是否必填");
          					if(job.mode == 2){
          						var a1 = job.defaultDimensions;
        			        	var a2 = {};
        			        	var a3 = {};
        			        	$.each(a1,function(key1,value1){
        			        		$.each(value1,function(key2,value2){
        			        			$.each(value2,function(i){
        			        				if(value2[i].substring(0,4) == "TAG:"){
        			        					if(a2[key1] == undefined){
            			        					a2[key1] = {};
            			        				}
            			        				if(a2[key1][key2] == undefined){
            			        					a2[key1][key2] = [];
            			        				}
        			        					a2[key1][key2].push(value2[i].substring(4));
        			        				} else {
        			        					if(a3[key1] == undefined){
            			        					a3[key1] = {};
            			        				}
            			        				if(a3[key1][key2] == undefined){
            			        					a3[key1][key2] = [];
            			        				}
        			        					a3[key1][key2].push(value2[i]);
        			        				}
        			        			});
        			        		});
        			        	});
        			        	self.initializeTableSelectModel("#tab_job_dimension_entity",a3,"/jobDimensionEntities","/updateCellDimensionEntity","维度","实体","jobDimensionEntity","template/getDimensionEntityByProductAndDimension","&dimensionName=","entity");
        			        	self.initializeTableSelectModel("#tab_job_dimension_title",a2,"/jobDimensionTitles","/updateCellDimensionTitle","维度","标签","jobDimensionTitle","template/getTitleByProductAndDimension","&dimensionName=","title");
        			        	$("#tr_addDimension").css("display","");
        		        		$("#tr_specialLean").css("display","");
          					}else{
          						$("#tr_addDimension").css("display","none");
          		        		$("#tr_specialLean").css("display","none");
          					}
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
        	//查询到所有tasks
        	$.ajax({
    			async:false,
    			url: 'template/getTaskIdsByProductAndJobId',
    	        data:{product:pProduct,jobId:jobId},
    	    	type : 'GET',
    			contentType:'application/x-www-form-urlencoded',
    	        success:function (data) {
    	        	if(data.success){
    	        		var tasks = data.data.tasks;
    	        		resultTasks = [];
    	        		$.each(tasks, function(i){
    	        			resultTasks.push(tasks[i].taskId);
        		       	})
    	        	} else {
    	        		var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
	                	alertor.dangerAlert(msgInfo,'middle');
    	        	}
    	       },
    	       error: function (e) {
    	    	   bootbox.alert('获取任务失败' + e.status);
    	       }  	
    		})
    		if(job.mode && job.mode == 2){
    			var specialLeans;
            	if(job.specialLeans != undefined){
            		specialLeans = job.specialLeans;
            	} else {
            		specialLeans = {};
            	}
				self.initializeTableSpecialModel("#jobTemplateSpecialView",specialLeans,"/jobSpecialLeans","/updateSpecialLeans","任务项编号","表达式","被依赖任务项编号","表达式");
    		} 
        },
        initTaskTable : function(jrow) {
        	var self = this;
        	var tt_el = $('#tb_tasks_instance');
        	if(!self.isNan(self.tb_tasks_instance)){
        		$('#btn_task_add').unbind('click');
        		$('#btn_task_delete').unbind('click');
        		$('#btn_task_lean_setting').unbind('click');
        		$('#btn_task_lean_manager').unbind('click');
        		$('#btn_task_query').unbind('click');
        		$('#btn_task_clear').unbind('click');
        		tt_el.bootstrapTable("destroy");
        	}
        	var cdTaskIdInput = $("#tabs_jtpl_tasks").find("input[name=taskId]");
        	self.tb_tasks_instance = tt_el.bootstrapTable({
        		theadClasses: 'thead-light',
                url: 'task/list',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
                toolbar: '#tb_tasks_instance_toolbar',                //工具按钮用哪个容器
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
                	   		jobId : jrow == null ? "" : jrow.jobId,
                	   		taskId : cdTaskIdInput.val()
                        };
                    },           //传递参数（*）
                sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                pageNumber:1,                       //初始化加载第一页，默认第一页
                pageSize: 20,                       //每页的记录行数（*）
                pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                minimumCountColumns: 2,             //最少允许的列数
                clickToSelect: true,                //是否启用点击选中行
                uniqueId: "taskId",                     //每一行的唯一标识，一般为主键列
                showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                buttonsClass: 'sm btn-primary',
                cardView: false,                    //是否显示详细视图
                detailView: false,                   //是否显示父子表
                showColumns: true,
                showFullscreen: false,
                showRefresh: true,
                columns: [
	                    	{checkbox: true}, 
	                    	{field: 'taskId', title: '任务项编号'},
	                    	{field: 'name', title: '任务项名称'},
	                    	{field: 'plugin', title: '插件'},
	                    	{field: 'programName', title: '程序'},
	                    	{field: 'workingDay', title: '假日设定'},
	                    	{field: 'errorDelay', title: '错误延迟间隔(S)'},
	                    	{field: 'errorIgnore', title: '忽略错误通过',
	                    		formatter:function(value, row, index){
	                				return value==1? "是":"否";
	                    		}
	                    	},
	                    	{field: 'maxNumOfExeErrors', title: '错误重试次数'},
	                    	{field: 'useJobDimension', title: '维度方案'},
	                    	{field: 'agentScope', title: '可执行节点'},
	                    	{field: 'createTime', title: '创建时间', align: 'center'}
                		],
                responseHandler: function (res) {return res.data;},
                onDblClickRow: function (tRow) {
                	self.taskEdit(jrow ,tRow.taskId, tRow.plugin, true);
                }
            });
        	$('#btn_task_add').bind('click', function(event){ 
        		self.taskEdit(jrow ,null, null, false);
            });
        	$('#btn_task_delete').bind('click', function(event){ 
            	self.deleteTask(jrow, tt_el);
            });
        	$('#btn_task_lean_setting').bind('click', function(event){ 
            	self.showTaskLeanSettingModal(tt_el);
            });
        	$('#btn_task_lean_manager').bind('click', function(event){ 
            	self.showTaskLeanManagerModal(tt_el);
            });
            $('#btn_task_query').bind('click', function(event){ 
            	tt_el.bootstrapTable('refresh');
            });
            $('#btn_task_clear').bind('click', function(event){
            	cdTaskIdInput.val("");
            });
        },
        isNan : function (o) {
        	return typeof(o) == "undefined" || o == null;
        },
        //编辑task信息
        taskEdit: function(jrow, taskId ,plugin, isEditTask, job_jsPlambToolkit){
        	if(jrow == null){
        		bootbox.alert({message:'任务模板基本信息未保存',size:'small'});
    			return;
        	}
        	var self = this;
        	if (isEditTask) {
        		if (self.isNan(taskId) || self.isNan(plugin)) {
        			bootbox.alert({message:'获取Task参数信息失败',size:'small'});
        			return;
        		}
        	}
        	var isCreateNewTaskFromGraphy = (!isEditTask && !self.isNan(taskId) && !self.isNan(plugin));
        	//保存退出标记，后续关闭modal时，删除画图节点
        	self.dragCreatTaskSaved = false;
        	//获取task数据
			var task;
			if (isEditTask || isCreateNewTaskFromGraphy){
				$.ajax({
					async:false,
					url: 'template/getTask',
					type:"GET",
			        contentType:'application/x-www-form-urlencoded',
			        data:{product:jrow.product, jobId:jrow.jobId, taskId:taskId},
			        success:function (data) {
			        	if(data.success){
			        		task = data.data.task;
			        	}else{
			        		var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
	    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
	    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
	    					}
		                	alertor.dangerAlert(msgInfo,'middle');
			        	}
			        },
			        error: function (e) {
			        	bootbox.alert({message:'获取任务项失败',size:'small'});
			        }
				});
				if (self.isNan(task) && isEditTask) {
					bootbox.alert({message:'获取任务项信息为空',size:'small'});
        			return;
				}
				if (!self.isNan(task) && isCreateNewTaskFromGraphy) {
					bootbox.alert({message:'任务项编号重复',size:'small'});
        			return;
				}
			}
			
        	var taskEditModal = $('#task_edit_modal');
        	$('#saveTaskinfo').unbind('click').bind('click', function(event){
            	self.saveTaskinfo();
            });
        	//初始化维度方案下拉
        	var sel_job_dmsn = taskEditModal.find("select[name=useJobDefDimension]");
        	self.initJobDmsnSchemaSelcet(sel_job_dmsn, jrow.product, jrow.jobId);
			//初始化插件下拉
        	ceditable.init("jobTemplateLayoutPluginId","jobTemplateLayoutProgramNameId","tab_task_template_properties", "task_edit_modal");
        	//var sel_plu = taskEditModal.find("select[name=taskPlugin]");
        	//self.initPluginSelcet(sel_plu);
        	//sel_plu.unbind('change').bind('change', function(event){
        		//self.selectOnChangeTaskPlugin(event);
        	//});
        	//获取plugin列表信息
			/*var pluginObject = {};
			var pluginObjectIsMust = {};
			if (isEditTask || isCreateNewTaskFromGraphy) {
				self.selectOnchangePluginName(plugin, pluginObject, pluginObjectIsMust);
			}*/
        	//可执行节点范围
        	var AgentScope_Id_Css = "<i class='fal fa-key fa-fw'></i>&nbsp;";
        	var AgentScope_Tag_Css = "<i class='fal fa-tag fa-fw'></i>&nbsp;";
        	var selectAgentScope = $("#task_edit_modal").find("select[name=agentScope]");
        	selectAgentScope.empty();
        	selectAgentScope.select2({
        		dropdownParent: $("#task_edit_modal"),
        		allowClear: true,
        		placeholder: "请输入可执行节点范围",
        		multiple: true,
             	ajax: {
             		async: false,
            		url: 'server/selectAgentScope?product='+jrow.product,	
            		dataType: 'json',
            		data: function(params){
            			return{
            				inputAgentScope: params.term,
            			};
            		},
    				processResults: function(data, params){
    					var results = [];
           				var selectTwos = data.data.selectTwos;
           				if(selectTwos != undefined){
           					$.each(selectTwos,function(i){
           						var item = {};
    	           				item.id = selectTwos[i].id;
    	           				if(selectTwos[i].type == "id"){
    	           					item.text = AgentScope_Id_Css + selectTwos[i].text;
    	           				}else{
    	           					item.text = AgentScope_Tag_Css + selectTwos[i].text;
    	           				}
    	           				results.push(item);
							})
           				}
               			return{
               				results: results
               			};
    				},
    				cache: false
             	},
             	escapeMarkup:function(markup){ return markup; },
             	minimumInputLength:0,
                templateResult: function formatData(data){ //下拉格式
                	return data.text; //选中后下拉显示值
                }
            });
        	//可执行窗口时间
        	$('#taskModal_period', this.o_container).unbind('click').bind('click', function(event){
        		exp.initExpression(this.id,this.value,exp.displayHDWMYTabArr);
        	});
        	//初始化工作日设定列表
        	//加载产品
        	var selJob = taskEditModal.find("select[name=workingDay]");
        	$.ajax({
        		async:false,
        		url: 'calendar/getCalendarList?product='+jrow.product,
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
    	    	   bootbox.alert({message:'获取工作日下拉框数据出错' + e.status,size:'small'});
    	       }  	
    		});
			var taskDmsn = {};
			var taskTagsEl = taskEditModal.find("input[name=task_tags]");
			taskTagsEl.tagsinput({style:'min-width:80%'});
			taskTagsEl.tagsinput("removeAll");
			taskEditModal.find("label[name=product]")[0].innerHTML = jrow.product;
			taskEditModal.find("label[name=jobId]")[0].innerHTML = jrow.jobId;
			taskEditModal.find("input[name=creator]").val(jrow.creator);
			taskEditModal.find("input[name=jobMode]").val(jrow.mode);
			if(!isEditTask || isCreateNewTaskFromGraphy){
				  ceditable.setJobIdAndSourceId(null,null);
				  if (!self.isNan(taskId)){
					  taskEditModal.find("input[name=taskId]").val(taskId);
					  taskEditModal.find("input[name=taskId]").attr('disabled', true);
				  } else {
					  taskEditModal.find("input[name=taskId]").val('');
					  taskEditModal.find("input[name=taskId]").attr('disabled', false);
				  }
				  if (!self.isNan(plugin)){
					  taskEditModal.find("select[name=taskPlugin]").val(plugin).trigger('change');
					  taskEditModal.find("select[name=taskPlugin]").attr('disabled', true);
					  taskEditModal.find("select[name=programName]").val(plugin).trigger('change');
				  } else {
					  taskEditModal.find("select[name=taskPlugin]").val('').trigger('change');
					  taskEditModal.find("select[name=taskPlugin]").attr('disabled', false);
					  taskEditModal.find("select[name=programName]").val('').trigger('change');
				  }
				  taskEditModal.find("input[name=taskName]").val("");
				  taskEditModal.find("select[name=useJobDefDimension]").val("0");		
				  taskEditModal.find("input[name=errorMaxCount]").val("1");
				  taskEditModal.find("input[name=period]").val("");
				  taskEditModal.find("select[name=workingDay]").val("");
				  taskEditModal.find("select[name=errorIgnore]").val("0");
				  taskEditModal.find("input[name=errorDelay]").val("30");
				  taskEditModal.find("input[name=createTime]").val(self.getSysYmd());
			} else {
				  ceditable.setJobIdAndSourceId(task.jobId,task.taskId);
				  taskDmsn = task.dimensions;
	    		  if(task.title != undefined){
	    			  var tags = task.title.split(",");
		        	  $.each(tags,function(i) {taskTagsEl.tagsinput("add",tags[i]);});
	    		  }
	    		  taskEditModal.find("input[name=editAction]").val('1');
	        	  taskEditModal.find("input[name=taskId]").val(task.taskId);
	        	  taskEditModal.find("input[name=taskId]").attr('disabled', true);
	        	  taskEditModal.find("input[name=taskName]").val(task.name);
	        	  taskEditModal.find("select[name=taskPlugin]").val(task.plugin).trigger('change');
	        	  taskEditModal.find("select[name=taskPlugin]").attr('disabled', true);
	        	  taskEditModal.find("select[name=programName]").val(task.programName).trigger('change');
	        	  taskEditModal.find("select[name=useJobDefDimension]").val(task.useJobDimension);		
	        	  taskEditModal.find("input[name=errorMaxCount]").val(task.maxNumOfExeErrors);
	        	  taskEditModal.find("select[name=errorIgnore]").val(task.errorIgnore);
	        	  taskEditModal.find("input[name=errorDelay]").val(task.errorDelay);
	        	  taskEditModal.find("input[name=creator]").val(task.creator);
	        	  taskEditModal.find("input[name=period]").val(task.period);
	        	  taskEditModal.find("select[name=workingDay]").val(task.workingDay);
	        	  if(task.agentScope != null && task.agentScope != ""){
	        		  var agentScopeArr = task.agentScope.split(",");
	        		  for(var a=0;a<agentScopeArr.length;a++){
	        			  var tAgentScope = agentScopeArr[a];
	        			  var option = null;
	        			  if(tAgentScope.startsWith("#")){//标签
	        				  option = new Option(AgentScope_Tag_Css+tAgentScope.substring(1,tAgentScope.length), tAgentScope);
	        			  }else{//ID
	        				  option = new Option(AgentScope_Id_Css+tAgentScope, tAgentScope);
	        			  }
	        			  taskEditModal.find("select[name=agentScope]").append(option);
	        		  }
	        		  taskEditModal.find("select[name=agentScope]").val(task.agentScope.split(",")).trigger("change");
	        	  }
	        	  taskEditModal.find("input[name=createTime]").val(task.createTime);
			}
			taskEditModal.modal('show');
			taskEditModal.off().on('hidden.bs.modal',function(){
				  //模态框消失，判断如果是第一次task没有保存，删除node
				  if (job_jsPlambToolkit && job_jsPlambToolkit != null) {
				  	if(isCreateNewTaskFromGraphy && !self.dragCreatTaskSaved) {
				  		job_jsPlambToolkit.remove(taskId);
				  	}
				  }
				  //隐藏wwsfInput相关modal
          		  ceditable.hideWwsfInputModal();
			});
			var titleDmsn = {};
			var entityDmsn = {};
			$.each(taskDmsn,function(key,value){
				$.each(value,function(i){
    				if(value[i].substring(0,4) == "TAG:"){
    					if(titleDmsn[key] == undefined){
        					titleDmsn[key] = [];
        				}
    					titleDmsn[key].push(value[i].substring(4));
    				} else {
    					if(entityDmsn[key] == undefined){
    						entityDmsn[key] = [];
        				}
    					entityDmsn[key].push(value[i]);
    				}
    			});
			});
			if(jrow.mode == 2){
				self.initializeTableSelect("#tab_task_dimension_entity",entityDmsn,"/taskDimensionEntities","/updateTaskCellDimensionEntity","维度","实体","taskDimensionEntity","template/getDimensionEntityByProductAndDimension","&dimensionName=","entity");
				self.initializeTableSelect("#tab_task_dimension_title",titleDmsn,"/taskDimensionTitles","/updateTaskCellDimensionTitle","维度","标签","taskDimensionTitle","template/getTitleByProductAndDimension","&dimensionName=","title");
				$("#tr_setTaskDimensionInfo").css("display","");
			} else {
				$("#tab_task_dimension_title").bootstrapTable('destroy');
				$("#tab_task_dimension_entity").bootstrapTable('destroy');
				$("#tr_setTaskDimensionInfo").css("display","none");
			}
		},
		//保存Task信息
        saveTaskinfo: function () {
        	var self = this;
        	var taskEditModal = $('#task_edit_modal');
        	try {
        		mloadding.showLoadding();
	        	var isEditAction = taskEditModal.find("input:hidden[name=editAction]").val() == '1';
	        	var jobMode = taskEditModal.find("input:hidden[name=jobMode]").val();
	        	var taskJson = {};
	        	var useTaskId = taskEditModal.find("input[name=taskId]").val();
	    		if(!/^[0-9a-zA-Z_]+$/.test(useTaskId)){
	    			alertor.dangerAlert('操作失败，任务项编号只能为数字、字母或者下划线','small');
	    			return;
	    		}
	    		if(!validateStringLength(useTaskId,50)){
            		alertor.dangerAlert('操作失败，任务项编号的长度不能超过50','small'); 
            		return false;
            	}
	    		taskJson.taskId = taskEditModal.find("label[name=product]")[0].innerHTML + "#" +taskEditModal.find("input[name=taskId]").val();
	    		taskJson.name = taskEditModal.find("input[name=taskName]").val();
	    		if(null == taskJson.name || taskJson.name == ""){
	    			alertor.dangerAlert('操作失败，任务项名称不能为空','small');
	    			return;
	    		}
	    		if(!validateStringLength(taskJson.name,200)){
            		alertor.dangerAlert('操作失败，任务项名称的长度不能超过200','small'); 
            		return false;
            	}
	    		taskJson.jobId = taskEditModal.find("label[name=jobId]")[0].innerHTML;
	    		taskJson.title = taskEditModal.find("input[name=task_tags]").val();
	    		if(!validateStringLength(taskJson.title,500)){
            		alertor.dangerAlert('操作失败，标签的长度不能超过500','small'); 
            		return false;
            	}
	    		taskJson.useJobDimension = taskEditModal.find("select[name=useJobDefDimension]").val();
	    		taskJson.plugin = taskEditModal.find("select[name=taskPlugin]").val();
	    		if(null == taskJson.plugin || taskJson.plugin == ""){
	    			alertor.dangerAlert('操作失败，插件不能为空','small');
	    			return;
	    		}
	    		taskJson.programName = taskEditModal.find("select[name=programName]").val();
	    		if(null == taskJson.programName || taskJson.programName == ""){
	    			alertor.dangerAlert('操作失败，程序不能为空','small');
	    			return;
	    		}
	    		taskJson.maxNumOfExeErrors = taskEditModal.find("input[name=errorMaxCount]").val();
	    		var agentScopeArr = taskEditModal.find("select[name=agentScope]").val();
	    		if(agentScopeArr.length == 0){
	    			taskJson.agentScope = "";
	    		}else{
	    			var agentScopes = "";
	    			for(var a=0;a<agentScopeArr.length;a++){
	    				if(a == agentScopeArr.length-1){
	    					agentScopes += agentScopeArr[a]
	    				}else{
	    					agentScopes += agentScopeArr[a] + ","
	    				}
	    			}
	    			taskJson.agentScope = agentScopes;
	    		}
	    		if(!validateStringLength(taskJson.agentScope,500)){
            		alertor.dangerAlert('操作失败，可执行节点范围的长度不能超过500','small'); 
            		return false;
            	}
	    		taskJson.period = taskEditModal.find("input[name=period]").val();
	    		if(!validateStringLength(taskJson.period,100)){
            		alertor.dangerAlert('操作失败，可执行窗口时间的长度不能超过100','small'); 
            		return false;
            	}
	    		taskJson.workingDay = taskEditModal.find("select[name=workingDay]").val();
	    		if(isNaN(taskJson.maxNumOfExeErrors)){
	    			alertor.dangerAlert('操作失败，错误最大次数必须为数字类型','small');
	    			return;
	    		}
	    		if(!validateStringLength(taskJson.maxNumOfExeErrors,9)){
            		alertor.dangerAlert('操作失败，错误最大次数的长度不能超过9','small'); 
            		return false;
            	}
	    		taskJson.errorIgnore = taskEditModal.find("select[name=errorIgnore]").val();
	    		taskJson.errorDelay = taskEditModal.find("input[name=errorDelay]").val();
	    		if(isNaN(taskJson.errorDelay)){
	    			alertor.dangerAlert('操作失败，错误延时时间必须为数字类型','small');
	    			return;
	    		}
	    		if(!validateStringLength(taskJson.errorDelay,9)){
            		alertor.dangerAlert('操作失败，错误延时时间的长度不能超过9','small'); 
            		return false;
            	}
	    		taskJson.creator = taskEditModal.find("input[name=creator]").val();
	    		if(!validateStringLength(taskJson.creator,50)){
            		alertor.dangerAlert('操作失败，创建者的长度不能超过50','small'); 
            		return false;
            	}
	    		taskJson.createTime = taskEditModal.find("input[name=createTime]").val();
	    		if(null == taskJson.createTime || taskJson.createTime == ""){
	    			alertor.dangerAlert('操作失败，创建时间不能为空','small');  
	    			return;
	    		}
	    		if(jobMode == 2){
	    			var dmsnEntities = $('#tab_task_dimension_entity').bootstrapTable("getData");
	            	var dimensionsJson = {};//保存维度实体信息
	            	var success = true;
	            	$.each(dmsnEntities,function(i){
	            		var dimension = dmsnEntities[i].key;
	            		if(dimension == ""){
	            			return true;
	            		}
	            		var dimensionEntity;
	            		if(dmsnEntities[i].value.length != 1){
	            			dimensionEntity = dmsnEntities[i].value;
	            		} else {
	            			dimensionEntity = dmsnEntities[i].value[0];
	            		}
	            		if(!(null == dimension || dimension == "")){
	            			if(null == dimensionEntity || dimensionEntity == ""){
	        					alertor.dangerAlert('操作失败，实体添加维度中实体不能为空','small');
	        					success = false;
	        					return false;
	        				}
	    				}
	            		if(dimensionsJson[dimension] == null){
	    					dimensionsJson[dimension] = [];
	    				}
	    				if(dimensionsJson[dimension] == undefined){
	    					dimensionsJson[dimension] = [];
	    				}			
	    				dimensionsJson[dimension].push(dimensionEntity);
	            	});
	            	if (!success) return;
	            	var dmsnTitles = $('#tab_task_dimension_title').bootstrapTable("getData");
	            	$.each(dmsnTitles,function(i){
	            		var dimension = dmsnTitles[i].key;
	            		if(dimension == ""){
	            			return true;
	            		}
	            		if(dmsnTitles[i].value.length != 1){
	            			dimensionTitle = dmsnTitles[i].value;
	            		} else {
	            			dimensionTitle = dmsnTitles[i].value[0];
	            		}
	            		if(!(null == dimension || dimension == "")){
	            			if(null == dimensionTitle || dimensionTitle == ""){
	        					alertor.dangerAlert('操作失败，实体添加维度中标签不能为空','small');
	        					success = false;
	        					return false;
	        				}
	    				}
	            		if(dimensionsJson[dimension] == null){
	            			dimensionsJson[dimension] = [];
	    				}
	    				if(dimensionsJson[dimension] == undefined){
	    					dimensionsJson[dimension] = [];
	    				}			
	    				dimensionsJson[dimension].push("TAG:" + dimensionTitle);
	            	});
	            	if (!success) return;
	            	taskJson.dimensions = dimensionsJson;
	    		}
	        	/*var propertiesObj = $('#tab_task_template_properties').bootstrapTable("getData");
	        	var parametersJson = {};//保存任务参数信息
	        	var isSave = true;
	        	$.each(propertiesObj,function(i){
	        		var taskPropertyKey = propertiesObj[i].key;
	        		if(taskPropertyKey != ""){
	        			var taskPropertyValue;
	            		if(propertiesObj[i].value.length != 1){
	            			taskPropertyValue = propertiesObj[i].value;
	            		} else {
	            			taskPropertyValue = propertiesObj[i].value[0];
	            		}
	        		}
	        		if(taskPropertyValue == undefined || taskPropertyValue == null || taskPropertyValue == "" || taskPropertyValue.trim() == ""){
	        			if(propertiesObj[i].isMust){
	        				alertor.dangerAlert(taskPropertyKey + '是必填参数，不能为空','small');
	        				isSave = false;
	        				return false;
	        			}
	        		}
					parametersJson[taskPropertyKey] = taskPropertyValue;
	        	});
	        	taskJson.parameters = parametersJson;*/
	    		var rowDatasJson = ceditable.getRowDatasJson('tab_task_template_properties');
        		if(rowDatasJson.errorInfo != ""){
        			alertor.dangerAlert(rowDatasJson.errorInfo,'small');
        			return false;
        		}
        		if(rowDatasJson.data != undefined){
        			taskJson.parameters = rowDatasJson.data;
        			taskJson.paramterIsMust = rowDatasJson.paramterIsMust;
        		}
                //if(isSave){
					$.ajax({
						asyn : false,
						url:'template/saveTasks',
			            type:"POST",
			            processData:false,
			            data:JSON.stringify(taskJson),
			            contentType:'application/json',
			            success:function (data) {
			                if (data.success){
			                	self.dragCreatTaskSaved = true;
			                	bootbox.alert({message:'保存任务项信息成功',size:'small'});
			                	$('#tb_tasks_instance').bootstrapTable('refresh');
			                	taskEditModal.modal('hide');
			                }else{
			                	var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
		    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
		    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
		    					}
			                	alertor.dangerAlert(msgInfo,'middle');
			                }
			            },
			            error: function(XMLHttpRequest,textStatus,errorThrown){
			            	bootbox.alert({message:'保存失败'+errorThrown,size:'small'});    
			            }
			        });
                //}
        	} finally {
        		mloadding.hideLoadding();
        	}
        },
        deleteTask : function(jrow, table) {
        	var selected = table.bootstrapTable('getSelections');
        	if (selected.length <= 0) {
        		bootbox.alert({message:'请选择需要删除的任务项',size:'small'});
        		return;
        	}
    		var tasks = [];
    		_.each(selected, function(element, index){
    			tasks.push(element.taskId);
    		});
    		bootbox.confirm({
        		size : 'small',
        		message : '是否确定删除所选任务项吗？',
        		callback : function (result) {
            		if(result){
                		$.ajax({
                			url : 'template/deltasks?product='+jrow.product+'&jobId='+jrow.jobId+'&taskIds='+tasks.toString(),
                			type : 'DELETE',
                			contentType:'application/json',
                			success: function(data){
                				if(data.success){
                					bootbox.alert({message:'删除成功',size:'small'});
                					table.bootstrapTable('refresh');
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
        showTaskLeanSettingModal : function(table) {
        	var self = this;
        	var selected = table.bootstrapTable('getSelections');
        	var selJobId, selTaskId;
        	if(selected.length == 1){
        		selJobId = selected[0].jobId;
        		selTaskId = selected[0].taskId;
        	}else{
        		bootbox.alert({message:'请选择一条任务项',size:'small'});
        		return;
        	}
        	$('#addTaskLean').unbind('click');
        	$('#addTaskLean').bind('click', function(event){ 
            	self.taskLeanOperation(selJobId, selTaskId, "add");
            });
        	$('#removeTaskLean').unbind('click');
            $('#removeTaskLean').bind('click', function(event){ 
            	self.taskLeanOperation(selJobId, selTaskId, "remove");
            });
        	self.initTaskLeanSelesTable('tab_seles_task_leans', selJobId, selTaskId);
        	self.initTaskLeanSeledTable('tab_seled_task_leans', selJobId, selTaskId);
        	$("#taskLeanSettingModal").modal("show");
        },
        taskLeanOperation : function(selJobId, selTaskId, operationType) {
        	var self = this;
        	var selected;
        	var leanTaskIdArr = [];
        	if(operationType == "add"){
        		selected = $('#tab_seles_task_leans').bootstrapTable('getSelections');
        		if(selected.length < 1){
        			bootbox.alert({message:'请至少选择一条任务项',size:'small'});
            		return;
        		}
        		_.each(selected, function(element, index){
        			leanTaskIdArr.push(element.taskId); 
        		});
        	}else{
        		selected = $('#tab_seled_task_leans').bootstrapTable('getSelections');
        		if(selected.length < 1){
        			bootbox.alert({message:'请至少选择一条依赖任务项',size:'small'});
            		return;
        		}
        		_.each(selected, function(element, index){
        			leanTaskIdArr.push(element.leanTaskId);  
        		});
        	}
        	$.ajax({
        		async:false,
    			data : {'jobId':selJobId, 'taskId':selTaskId, 'leanJobId':'', 'leanTaskIds':leanTaskIdArr.join(","), 'operationType':operationType},
    			url : 'template/saveTaskLean',
    			type : 'GET',
    			contentType:'application/json',
    			success: function(data){
    				if(data.success){
    					bootbox.alert({message:'操作成功',size:'small'});
    					$('#tab_seles_task_leans').bootstrapTable('refresh');
    					$('#tab_seled_task_leans').bootstrapTable('refresh');
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
        initTaskLeanSelesTable:function(tabId, selJobId, selTaskId){
        	var self = this;
        	var tb_tleans_el = $('#' + tabId);
        	tb_tleans_el.bootstrapTable('destroy');
        	tb_tleans_el.bootstrapTable({
        		theadClasses: 'thead-light',
                url: 'template/task/selectTaskLeanList',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
                striped: true,                      //是否显示行间隔色
                cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                pagination: true,                   //是否显示分页（*）
                noInfoPagination: true,
                sortable: false,                     //是否启用排序
                sortOrder: "asc",                   //排序方式
                queryParamsType:'undefined',
                queryParams: function (params) {
                   return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                	   		pageNumber: params.pageNumber,   //页面大小
                	   		pageSize: params.pageSize,  //页码
                	   		jobId: selJobId,
                	   		notEqTaskId: selTaskId
                        };
                    },           //传递参数（*）
                sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                pageNumber:1,                       //初始化加载第一页，默认第一页
                pageSize: 20,                       //每页的记录行数（*）
                pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                minimumCountColumns: 2,             //最少允许的列数
                clickToSelect: true,                //是否启用点击选中行
                uniqueId: "taskId",                     //每一行的唯一标识，一般为主键列
                buttonsClass: 'sm btn-primary',
                columns: [
                	{checkbox: true}, 
                	{field: 'taskId', title: '任务项编号'}
                ],
                responseHandler: function (res) {
                	return res.data;
                }
            });
        },
        initTaskLeanSeledTable:function(tabId, selJobId, selTaskId){
        	var self = this;
        	var tb_seled_tleans_el = $('#'+tabId);
        	tb_seled_tleans_el.bootstrapTable('destroy');
        	tb_seled_tleans_el.bootstrapTable({
        		theadClasses: 'thead-light',
                url: 'template/task/beSelectTaskLeanList',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
                striped: true,                      //是否显示行间隔色
                cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                pagination: true,                   //是否显示分页（*）
                noInfoPagination: true,
                sortable: false,                     //是否启用排序
                sortOrder: "asc",                   //排序方式
                queryParamsType:'undefined',
                queryParams: function (params) {
                   return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                	   		pageNumber: params.pageNumber,   //页面大小
                	   		pageSize: params.pageSize,  //页码
                	   		jobId: selJobId,
                	   		taskId: selTaskId
                        };
                    },           //传递参数（*）
                sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                pageNumber:1,                       //初始化加载第一页，默认第一页
                pageSize: 20,                       //每页的记录行数（*）
                pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                minimumCountColumns: 2,             //最少允许的列数
                clickToSelect: true,                //是否启用点击选中行
                uniqueId: "taskId",                     //每一行的唯一标识，一般为主键列
                buttonsClass: 'sm btn-primary',
                columns: [
                	{checkbox: true}, 
                	{field: 'leanTaskId', title: '依赖任务项编号'}
                ],
                responseHandler: function (res) {
                	return res.data;
                }
            });
        },
        //依赖管理
        showTaskLeanManagerModal : function(table) {
        	var self = this;
        	var selected = table.bootstrapTable('getSelections');
        	var selJobId, selTaskId;
        	if(selected.length == 1){
        		selJobId = selected[0].jobId;
        		selTaskId = selected[0].taskId;
        	}else{
        		bootbox.alert({message:'请选择一条任务项',size:'small'});
        		return;
        	}
        	$("#taskLeanManagerModal").find("input[name=hJobId]").val(selJobId);
        	$("#taskLeanManagerModal").find("input[name=hTaskId]").val(selTaskId);
        	$("#taskLeanManagerModal").find("input[name=hLeanTaskId]").val("");
        	self.initTaskLeanTable();
        	self.initTaskInfoTable(selJobId, selTaskId);
        	self.initTaskBeleanTable();
        	self.initTaskSpecialLeanTable();
        	
        	$('#btn_add_task_lean').unbind('click').bind('click', function(event){ 
            	self.showTaskLeanEditModal();
            });
        	$('#btn_del_task_lean').unbind('click').bind('click', function(event){ 
        		self.delTaskLean();
            });
        	
        	$("#taskLeanManagerModal").modal("show");
        },
        initTaskLeanTable:function(){
        	var self = this;
        	var tb_tleans_el = $('#tab_task_leans');
        	tb_tleans_el.bootstrapTable('destroy');
        	tb_tleans_el.bootstrapTable({
        		theadClasses: 'thead-light',
                url: 'template/task/taskLeanList',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
                striped: true,                      //是否显示行间隔色
                cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                pagination: true,                   //是否显示分页（*）
                sortable: false,                    //是否启用排序
                sortOrder: "asc",                   //排序方式
                queryParamsType:'undefined',
                queryParams: function (params) {
                   return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                	   		pageNumber: params.pageNumber,   //页面大小
                	   		pageSize: params.pageSize,  //页码
                	   		jobId: $("#taskLeanManagerModal").find("input[name=hJobId]").val(),
                	   		taskId: $("#taskLeanManagerModal").find("input[name=hTaskId]").val()
                        };
                    },           //传递参数（*）
                sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                pageNumber:1,                       //初始化加载第一页，默认第一页
                pageSize: 20,                       //每页的记录行数（*）
                pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                minimumCountColumns: 2,             //最少允许的列数
                clickToSelect: true,                //是否启用点击选中行
                uniqueId: "leanTaskId",                     //每一行的唯一标识，一般为主键列
                buttonsClass: 'sm btn-primary',
                columns: [
                	{checkbox: true}, 
                	{field: 'leanTaskId', title: '前驱任务项', formatter: function (value, row, index) {
	                		                                                var retValue = value + " ";
	                		                                                if(row.type == 2){
	                		                                                	retValue += "<span class='badge badge-info mr-1'>特</span>";
	                		                                                }
	                		                                                if(row.includeDimension != 0){
	                		                                                	retValue += "<span class='badge badge-success mr-1'>维</span>";
	                		                                                }
	                		                                                return retValue;
                		                                                }
                	}
                ],
                responseHandler: function (res) {
                	return res.data;
                },
                onDblClickRow: function (row) {
                	$("#taskLeanManagerModal").find("input[name=hLeanTaskId]").val(row.leanTaskId);
                	$("#tab_task_special_leans").bootstrapTable("refresh");
                }
            });
        },
        initTaskInfoTable:function(selJobId, selTaskId){
        	$.ajax({
      			async : false,
    			data : {jobId:selJobId, taskId:selTaskId},
    			url : 'template/task/getdmsnName',
    			type : 'GET',
    			contentType:'application/x-www-form-urlencoded',
    			success: function(data){
    				if(data.success){
    					$("#tab_task_display_task_info").empty();
    					$("#tab_task_display_task_info").append(selJobId+" / "+selTaskId+"");
    					var dmsnNameList = data.data.dmsnNameList;
    					if(dmsnNameList != undefined){
    						$("#tab_task_display_task_info").append("【");
    						for(var i=0;i<dmsnNameList.length;i++){
    							if(i == dmsnNameList.length-1){
    								//$("#tab_task_display_task_info").append("<span class='badge badge-success'>"+dmsnNameList[i]+"</span>");
    								$("#tab_task_display_task_info").append(dmsnNameList[i]);
    							}else{
    								//$("#tab_task_display_task_info").append("<span class='badge badge-success mr-1'>"+dmsnNameList[i]+"</span>");
    								$("#tab_task_display_task_info").append(dmsnNameList[i]+", ");
    							}
							}
    						$("#tab_task_display_task_info").append("】");
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
        initTaskBeleanTable:function(){
        	var self = this;
        	var tb_tleans_el = $('#tab_task_beleans');
        	tb_tleans_el.bootstrapTable('destroy');
        	tb_tleans_el.bootstrapTable({
        		theadClasses: 'thead-light',
                url: 'template/task/taskBeleanList',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
                striped: true,                      //是否显示行间隔色
                cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                pagination: true,                   //是否显示分页（*）
                //noInfoPagination: true,
                sortable: false,                     //是否启用排序
                sortOrder: "asc",                   //排序方式
                queryParamsType:'undefined',
                queryParams: function (params) {
                   return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                	   		pageNumber: params.pageNumber,   //页面大小
                	   		pageSize: params.pageSize,  //页码
                	   		jobId: $("#taskLeanManagerModal").find("input[name=hJobId]").val(),
                	   		taskId: $("#taskLeanManagerModal").find("input[name=hTaskId]").val()
                        };
                    },           //传递参数（*）
                sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                pageNumber:1,                       //初始化加载第一页，默认第一页
                pageSize: 20,                       //每页的记录行数（*）
                pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                minimumCountColumns: 2,             //最少允许的列数
                clickToSelect: true,                //是否启用点击选中行
                uniqueId: "taskId",                     //每一行的唯一标识，一般为主键列
                buttonsClass: 'sm btn-primary',
                columns: [
                	{field: 'taskId', title: '后驱任务项', formatter: function (value, row, index) {
	                		                                                var retValue = value + " ";
	                		                                                if(row.type == 2){
	                		                                                	retValue += "<span class='badge badge-info mr-1'>特</span>";
	                		                                                }
	                		                                                if(row.includeDimension != 0){
	                		                                                	retValue += "<span class='badge badge-success mr-1'>维</span>";
	                		                                                }
	                		                                                return retValue;
                		                                                }
                	}
                ],
                responseHandler: function (res) {
                	return res.data;
                }
            });
        },
        initTaskSpecialLeanTable:function(){
        	var self = this;
        	var tb_tleans_el = $('#tab_task_special_leans');
        	tb_tleans_el.bootstrapTable('destroy');
        	tb_tleans_el.bootstrapTable({
        		theadClasses: 'thead-light',
                url: 'template/task/taskSpecialLeanList',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
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
                	   		jobId: $("#taskLeanManagerModal").find("input[name=hJobId]").val(),
                	   		taskId: $("#taskLeanManagerModal").find("input[name=hTaskId]").val(),
                	   		leanTaskId: $("#taskLeanManagerModal").find("input[name=hLeanTaskId]").val()
                        };
                    },           //传递参数（*）
                sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                pageNumber:1,                       //初始化加载第一页，默认第一页
                pageSize: 20,                       //每页的记录行数（*）
                pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                minimumCountColumns: 2,             //最少允许的列数
                clickToSelect: true,                //是否启用点击选中行
                uniqueId: "taskId",                     //每一行的唯一标识，一般为主键列
                buttonsClass: 'sm btn-primary',
                columns: [
                	{field: 'beTaskId', title: '前驱任务项', width:'23%'},
                	{field: 'beExp', title: '表达式', width:'23%'},
                	{field: 'taskId', title: '任务项', width:'23%'},
                	{field: 'exp', title: '表达式', width:'23%'},
                	{title:'操作', edit:false, align:'center', width:'8%',
                    	events:{
                        	'click .class_delete_task_special_lean': function(e, value, row, index) {
                        		self.deleteTaskSpecialLean(row);
                        	}
                        },
                    	formatter:function(value,row,rowIndex){
                        	return '<button type="button" class="btn btn-xs btn-primary px-1 mr-1 class_delete_task_special_lean">删除</button>';
                    	}
                    }
                ],
                responseHandler: function (res) {
                	return res.data;
                }
            });
        },
        deleteTaskSpecialLean:function(rowObj) {
        	var specialLeanTaskIdArr = [];
        	specialLeanTaskIdArr.push(rowObj.beTaskId);
        	bootbox.confirm({
        		size : 'small',
        		message : '是否确定删除这条特殊依赖？',
        		callback : function (result) {
        			if(result){
        				$.ajax({
        					async:false,
                			url : 'template/task/deleteTaskSpecialLean',
                			type : 'DELETE',
                			data:{jobId:rowObj.jobId,taskId:rowObj.taskId,specialLeanTaskIds:specialLeanTaskIdArr.join(",")},
                			beforeSend : function(){
                				mloadding.showLoadding();
                			},
                			complete : function(){
                				mloadding.hideLoadding();
                			},
                			success: function(data){
                				if(data.success){
                					$("#tab_task_special_leans").bootstrapTable("refresh");
                					$('#tab_task_leans').bootstrapTable('refresh');
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
        showTaskLeanEditModal:function(){
        	var selJobId = $("#taskLeanManagerModal").find("input[name=hJobId]").val();
        	var selTaskId = $("#taskLeanManagerModal").find("input[name=hTaskId]").val();
        	var self = this;
        	var taskLeanEditModal = $("#taskLeanEditModal");
        	//初始化依赖类型都不选
        	var leanTypeArr = $("#taskLeanEditModal").find("input:radio[name$=leanType]");
        	for(var i=0;i<leanTypeArr.length;i++){
        		leanTypeArr[i].checked = false;
        	}
        	taskLeanEditModal.find("input:radio[name$=leanType]").change(function(curRadio){
    			var exp = "";
    			if(curRadio.target.value == "1"){
    				$("#display_eidt_task_leans").css("display","");
    				$("#display_eidt_task_special_leans").css("display","none");
    			}else{
    				$("#display_eidt_task_leans").css("display","none");
    				$("#display_eidt_task_special_leans").css("display","");
    			}
        	});
        	//默认完整依赖
        	taskLeanEditModal.find("input:radio[value=1]").click();
        	//初始化完整依赖
        	$("#display_eidt_task_leans").find("input[name=qTaskId]").val("");
        	$("#display_eidt_task_leans").find("button[name=query]").unbind('click').bind('click', function(event){ 
        		$('#tab_select_task_leans').bootstrapTable('refresh');
        	});
        	$("#display_eidt_task_leans").find("button[name=clear]").unbind('click').bind('click', function(event){ 
        		$("#display_eidt_task_leans").find("input[name=qTaskId]").val("");
        		$('#tab_select_task_leans').bootstrapTable('refresh');
        	});
        	self.initSelectTaskLeans();
        	//完整依赖/保存按钮事件
        	$('#btn_save_select_task_leans').unbind('click').bind('click', function(event){ 
            	self.saveSelectTaskLeans(selJobId, selTaskId);
            });
        	//初始化特殊依赖
        	//特殊依赖/前驱任务项 下拉
        	var selLeanTaskObject = $("#display_eidt_task_special_leans").find("select[name=selLeanTaskId]");
        	selLeanTaskObject.empty();
        	selLeanTaskObject.select2({
        		dropdownParent: $("#taskLeanEditModal"),
        		allowClear: true,
        		placeholder: "请输入前驱任务项",
             	ajax: {
             		async: false,
            		url: 'template/selectTaskSpecialLeans?jobId='+selJobId+'&taskId='+selTaskId,	
            		dataType: 'json',
            		data: function(params){
            			return{
            				inputBeleanTaskId: params.term,
            			};
            		},
    				processResults: function(data, params){
    					var results = [];
           				var taskList = data.data.taskList;
           				if(taskList != undefined){
           					$.each(taskList,function(i){
           						var item = {};
    	           				item.id = taskList[i].taskId;
    	           				item.text = taskList[i].taskId;
    	           				results.push(item);
							})
           				}
               			return{
               				results: results
               			};
    				},
    				cache: false
             	},
             	escapeMarkup:function(markup){ return markup; },
             	minimumInputLength:1,
                templateResult: function formatData(data){ //下拉格式
                	return data.text; //选中后下拉显示值
                }
            });
        	selLeanTaskObject.unbind('select2:select').bind('select2:select', function(obj) {
        		self.loadSelLeanExp(selJobId,obj.target.value);
        	});
        	//特殊依赖/前驱任务项表达式 下拉
        	var selLeanExpObject = $("#display_eidt_task_special_leans").find("select[name=selLeanExp]");
        	selLeanExpObject.empty();
        	selLeanExpObject.select2({});
            //特殊依赖/任务项
        	$("#display_eidt_task_special_leans").find("label[name=showSelTaskId]")[0].innerHTML = selTaskId;
        	//特殊依赖/表达式 下拉
        	var selExpObject = $("#display_eidt_task_special_leans").find("select[name=selExp]");
        	selExpObject.empty();
        	selExpObject.select2({
        		dropdownParent: $("#taskLeanEditModal"),
        		allowClear: true,
        		placeholder: "请输入表达式",
             	ajax: {
             		async: false,
            		url: 'template/selectTaskExp?jobId='+selJobId+'&taskId='+selTaskId,	
            		dataType: 'json',
            		data: function(params){
            			return{
            				inputEntityName: params.term,
            			};
            		},
    				processResults: function(data, params){
    					var results = [];
           				var entityNameList = data.data.entityNameList;
           				if(entityNameList != undefined){
           					$.each(entityNameList,function(i){
           						var item = {};
    	           				item.id = entityNameList[i];
    	           				item.text = entityNameList[i];
    	           				results.push(item);
							})
           				}
               			return{
               				results: results
               			};
    				},
    				cache: false
             	},
             	escapeMarkup:function(markup){ return markup; },
             	minimumInputLength:0,
                templateResult: function formatData(data){ //下拉格式
                	return data.text; //选中后下拉显示值
                }
            });
        	//特殊依赖/追加按钮事件
        	$('#btn_append_select_task_spe_leans').unbind('click').bind('click', function(event){ 
            	self.appendTaskLean();
            });
        	//特殊依赖/已追加列表
        	self.initSelectTaskSpecialLeans([]);
        	taskLeanEditModal.modal("show");
        },
        loadSelLeanExp:function(jobId,taskId){
        	var selLeanExpObject = $("#display_eidt_task_special_leans").find("select[name=selLeanExp]");
        	selLeanExpObject.empty();
        	selLeanExpObject.select2({
        		dropdownParent: $("#taskLeanEditModal"),
        		allowClear: true,
        		placeholder: "请输入前驱表达式",
             	ajax: {
             		async: false,
            		url: 'template/selectTaskExp?jobId='+jobId+'&taskId='+taskId,	
            		dataType: 'json',
            		data: function(params){
            			return{
            				inputEntityName: params.term,
            			};
            		},
    				processResults: function(data, params){
    					var results = [];
           				var entityNameList = data.data.entityNameList;
           				if(entityNameList != undefined){
           					$.each(entityNameList,function(i){
           						var item = {};
    	           				item.id = entityNameList[i];
    	           				item.text = entityNameList[i];
    	           				results.push(item);
							})
           				}
               			return{
               				results: results
               			};
    				},
    				cache: false
             	},
             	escapeMarkup:function(markup){ return markup; },
             	minimumInputLength:0,
                templateResult: function formatData(data){ //下拉格式
                	return data.text; //选中后下拉显示值
                }
            });
        },
        appendTaskLean:function(){
        	var self = this;
        	var tDiv = $("#display_eidt_task_special_leans");
        	var selLeanTaskId = tDiv.find("select[name=selLeanTaskId]").val();
        	if(selLeanTaskId == null || selLeanTaskId == ""){
        		bootbox.alert({message:'操作失败，前驱任务项不能为空',size:'small'});
        		return false;
        	}
        	var selLeanExp = tDiv.find("select[name=selLeanExp]").val();
        	if(selLeanExp == null || selLeanExp == ""){
        		bootbox.alert({message:'操作失败，前驱表达式不能为空',size:'small'});
        		return false;
        	}
        	var selExp = tDiv.find("select[name=selExp]").val();
        	if(selExp == null || selExp == ""){
        		bootbox.alert({message:'操作失败，表达式不能为空',size:'small'});
        		return false;
        	}
        	var selJobId = $("#taskLeanManagerModal").find("input[name=hJobId]").val();
        	var selTaskId = $("#taskLeanManagerModal").find("input[name=hTaskId]").val();
        	var rowData = {jobId:selJobId,taskId:selTaskId,exp:selExp,beTaskId:selLeanTaskId,beExp:selLeanExp};
        	var tabRowDataArr = $("#tab_select_task_special_leans").bootstrapTable("getData");
        	var allDataArr = [];
        	var tKey = selJobId+"###"+selTaskId+"###"+selExp+"###"+selLeanTaskId+"###"+selLeanExp;
			for(var i=0;i<tabRowDataArr.length;i++){
				var fKey = tabRowDataArr[i].jobId+"###"+tabRowDataArr[i].taskId+"###"+tabRowDataArr[i].exp+"###"+tabRowDataArr[i].beTaskId+"###"+tabRowDataArr[i].beExp;
				if(tKey == fKey){
					bootbox.alert({message:'操作失败，追加特殊依赖重复',size:'small'});
					return false;
				}
				allDataArr.push(tabRowDataArr[i]);
			}
			allDataArr.push(rowData);
			allDataArr.sort(function(obj1,obj2){
	        	var a = obj1.jobId+"###"+obj1.taskId+"###"+obj1.exp+"###"+obj1.beTaskId+"###"+obj1.beExp;
	        	var b = obj2.jobId+"###"+obj2.taskId+"###"+obj2.exp+"###"+obj2.beTaskId+"###"+obj2.beExp;
	        	if(a > b){
	        		return 1;
	        	}
	        	return -1;
	        });
			this.initSelectTaskSpecialLeans(allDataArr);
        },
        initSelectTaskLeans:function(){
        	var self = this;
        	var tb_tleans_el = $('#tab_select_task_leans');
        	tb_tleans_el.bootstrapTable('destroy');
        	tb_tleans_el.bootstrapTable({
        		theadClasses: 'thead-light',
                url: 'template/task/selectTaskLeans',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
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
                	   		jobId: $("#taskLeanManagerModal").find("input[name=hJobId]").val(),
                	   		notEqTaskId: $("#taskLeanManagerModal").find("input[name=hTaskId]").val(),
                	   		qTaskId: $("#display_eidt_task_leans").find("input[name=qTaskId]").val()
                        };
                },           //传递参数（*）
                sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                pageNumber:1,                       //初始化加载第一页，默认第一页
                pageSize: 20,                       //每页的记录行数（*）
                pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                minimumCountColumns: 2,             //最少允许的列数
                clickToSelect: true,                //是否启用点击选中行
                uniqueId: "taskId",                     //每一行的唯一标识，一般为主键列
                buttonsClass: 'sm btn-primary',
                columns: [
                	{checkbox: true, width:'28px'}, 
                	{field: 'taskId', title: '前驱任务项', width:'260px'},
                	{field: 'name', title: '任务项名称'}
                ],
                responseHandler: function (res) {
                	return res.data;
                }
            });
        },
        saveSelectTaskLeans : function(selJobId, selTaskId) {
        	var self = this;
        	var leanType = $("#taskLeanEditModal").find("input:radio[name$=leanType]:checked").val();
        	if(leanType == "1"){
	        	var selected;
	        	var leanTaskIdArr = [];
	        	var qTaskId = "";
	    		selected = $('#tab_select_task_leans').bootstrapTable('getSelections');
	    		if(selected.length > 0){
	        		_.each(selected, function(element, index){
		    			leanTaskIdArr.push(element.taskId); 
		    		});
	    		}else{//不选根据查询条件设置依赖
	    			qTaskId = $("#display_eidt_task_leans").find("input[name=qTaskId]").val();
	    		}
	        	$.ajax({
	        		async:false,
	    			data : {'jobId':selJobId, 'taskId':selTaskId, 'leanJobId':'', 'leanTaskIds':leanTaskIdArr.join(","), 'qTaskId':qTaskId},
	    			url : 'template/saveSelectTaskLeans',
	    			type : 'PUT',
	    			contentType:'application/x-www-form-urlencoded',
	    			success: function(data){
	    				if(data.success){
	    					bootbox.alert({message:'操作成功',size:'small'});
	    					$('#tab_task_leans').bootstrapTable('refresh');
	    					$('#tab_task_special_leans').bootstrapTable('refresh');
	    					$("#taskLeanEditModal").modal("hide");
	    				}else{
	    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
	    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
	    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
	    					}
		                	alertor.dangerAlert(msgInfo,'middle');
		                	$('#tab_select_task_leans').bootstrapTable('refresh'); //报错，需重新加载数据
	    				}
	    			},
	    			error: function(msg){
	    				alertor.dangerAlert('连接服务器错误','small');
	    			}
	    		});
        	}else{
        		var tabRowDataArr = $("#tab_select_task_special_leans").bootstrapTable("getData");
        		if(tabRowDataArr.length < 1){
        			bootbox.alert({message:'请至少追加一条特殊依赖',size:'small'});
        			return false;
        		}
        		$.ajax({
        			data:JSON.stringify(tabRowDataArr),
        			url : 'template/saveSelectTaskSpecialLeans',
        			type : 'PUT',
        			contentType:'application/json',
        			success: function(data){
        				if(data.success){
        					bootbox.alert({message:'操作成功',size:'small'});
	    					$('#tab_task_leans').bootstrapTable('refresh');
	    					$('#tab_task_special_leans').bootstrapTable('refresh');
	    					$("#taskLeanEditModal").modal("hide");
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
        initSelectTaskSpecialLeans:function(dataArr){
        	var self = this;
        	var useId = "#tab_select_task_special_leans";
        	var useUrl = "/selectTaskSpecialLeans";
        	var useChildUrl = "/updateSelectTaskSpecialLeans";
			$.mockjax.clear(useUrl);
          	$.mockjax({ url: useUrl, logging: 0, responseText: dataArr});
          	$.mockjax.clear(useChildUrl);
           	$.mockjax({
           	    url: useChildUrl,
           	    logging: 0,
           	    response: function(settings) {
           	    	var rowdata = {index:settings.data.pk,row:{}};
           	    	rowdata.row[settings.data.name] = settings.data.value;
           	    	$(useId).bootstrapTable('updateRow', rowdata);
           	    }
           	});
           	$(useId).bootstrapTable('destroy');
    		$(useId).bootstrapTable({
    			theadClasses: 'thead-light',
        		url: useUrl,
                striped: true,  
                clickToSelect: false,  
                pagination: true,
                editable: false,
                columns: [
                	{field:'jobId',title:'jobId',align:'center',visible:false},
                	{field:'beTaskId',title:'前驱任务项',align:'right'},
                    {field:'beExp',title:'表达式',align:'right'},
                	{field:'taskId',title:'任务项',align:'right'},
                    {field:'exp',title:'表达式',align:'right'},  
                    {title:'操作', align:"center", edit:false, width: "65px",
                    	events:{
                        	'click .tab_jip_removerow_style': function(e, value, row, index) {
                        		var allDataArr = [];
                        		var tabRowDataArr = $(useId).bootstrapTable("getData");
                    			for(var i=0;i<tabRowDataArr.length;i++){
                    				if(i != index){
                    					allDataArr.push(tabRowDataArr[i]);
                    				}
                    			}
                    			self.initSelectTaskSpecialLeans(allDataArr);
                        	}
                        },
                    	formatter:function(value,row,rowIndex){  
                    		var btnHtml =  
                            	'<button type="button" class="btn btn-xs btn-outline-danger btn-icon tab_jip_removerow_style">' +
                					'<i class="fal fa-minus" aria-hidden="true"></i>' +
                				'</button>';
                        	return btnHtml;
                    	}}
                ],
                onLoadSuccess: function (aa, bb, cc) {
                	
                }
        	});
        },
        delTaskLean:function(){
        	var selJobId = $("#taskLeanManagerModal").find("input[name=hJobId]").val();
        	var selTaskId = $("#taskLeanManagerModal").find("input[name=hTaskId]").val();
        	var selected = $("#tab_task_leans").bootstrapTable('getSelections');
        	var leanTaskIdArr=[], specialLeanTaskIdArr=[];
        	if(selected.length < 1){
        		bootbox.alert({message:'请至少选择一条前驱任务项',size:'small'});
        		return false;
        	}
        	for(var i=0;i<selected.length;i++){
        		if(selected[i].type == "1"){
        			leanTaskIdArr.push(selected[i].leanTaskId);
        		}else{
        			specialLeanTaskIdArr.push(selected[i].leanTaskId);
        		}
        	}
        	var tConfirm = true;
    		bootbox.confirm({
        		size : 'small',
        		message : '是否确定删除所选前驱任务项？',
        		callback : function (result) {
        			if(result && tConfirm){
        				tConfirm = false;
        				$.ajax({
        					async:false,
                			url : 'template/task/delTaskLean',
                			type : 'DELETE',
                			data:{jobId:selJobId,taskId:selTaskId,leanTaskIds:leanTaskIdArr.join(','),specialLeanTaskIds:specialLeanTaskIdArr.join(',')},
                			success: function(data){
                				if(data.success){
                					bootbox.alert({message:'删除成功',size:'small'});
                					$('#tab_task_leans').bootstrapTable('refresh');
                					$('#tab_task_special_leans').bootstrapTable('refresh');
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
        /*selectOnChangeTaskPlugin : function (event){
        	var self = this;
        	var plugin = $(event.currentTarget).val();
        	var pluginObject = {};
        	var pluginObjectIsMust = {};
        	$.ajax({
        		async:false,
        		url: 'pluginAndAgent/pluginParameters?&pluginName='+plugin,
    			type : 'GET',
    			contentType:'application/x-www-form-urlencoded',       	        	
   	   	        success:function (res) {
   	   	        	var data = JSON.parse(res);
   	   	        	if(data.success){
	   	   	        	var params = data.data.rows;
	   	   	        	$.each(params,function(i){
	   	   	        		pluginObject[params[i].name] = params[i].value;
	   	   	        		if(params[i].required){
	   	   	        			pluginObjectIsMust[params[i].name] = 1;
	   	   	        		} else {
	   	   	        			pluginObjectIsMust[params[i].name] = 0;
	   	   	        		}
	   	   	        	});
   	   	        	}
   	   	        	self.initializeTable("#tab_task_template_properties",pluginObjectIsMust,pluginObject,"/taskProperties","/updateTaskCellProperties","属性","值","是否必填");
   	   	        },
   	   	        error: function (e) {
   	   	        	bootbox.alert({message:'系统错误',size:'small'});
   	   	        }
   	   	    });
   		},*/
		/*selectOnchangePluginName : function (pluginName,pluginObject,pluginObjectIsMust){
        	$.ajax({
        		async:false,
        		data : {pluginName:pluginName},
        		url: 'template/getPlugin',
    			type : 'GET',
    			contentType:'application/x-www-form-urlencoded',       	        	
   	   	        success:function (data) {
   	   	        	if(data.success){
	   	   	        	var params = data.data.params;
	   	   	        	$.each(params,function(i){
	   	   	        		pluginObject[params[i].name] = params[i].value;
	   	   	        		if(params[i].required){
	   	   	        			pluginObjectIsMust[params[i].name] = 1;
	   	   	        		} else {
	   	   	        			pluginObjectIsMust[params[i].name] = 0;
	   	   	        		}
	   	   	        	});
   	   	        	}
   	   	        },
   	   	        error: function (e) {
   	   	        	bootbox.alert({message:'系统错误',size:'small'});
   	   	        }
   	   	    });
   		},*/
        /*batchRun : function(product,jobId){
        	var self = this;
        	var runJobInfo;
        	$.ajax({
        		async:false,
    			data : {product:product,jobId:jobId},
    			url : 'template/getJob',
    			type : 'GET',
    			contentType:'application/json',
    			success: function(data){
    				if(data.success){
    					if (data.data.job) {
    						runJobInfo = data.data.job;
    					}
    				}else{
    					var msgInfo = '获取模板信息失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<a title="'+data.detailInfo+'"><i class="fal fa-exclamation-circle fa-fw ml-1"></i></a>'
    					}
	                	alertor.dangerAlert(msgInfo,'middle');
    				}
    			},
    			error: function(msg){
    				alertor.dangerAlert('连接服务器错误','small');
    			},
    			beforeSend : function(){
    				mloadding.showLoadding();
    			},
    			complete : function(){
    				mloadding.hideLoadding();
    			}
    		});
        	self.initializeTable("#tab_job_template_run_properties",runJobInfo.jobParamterIsMust,runJobInfo.properties,"/runProperties","/updateCellRunProperties","属性","值","是否必填");
        	$('#jobModal_template_run').modal('show');
        	$('#runJobTemplate').unbind('click').bind('click', function(event){
        		var runProperties = $('#tab_job_template_run_properties').bootstrapTable("getData");
        		var flag = true;
            	var properties = {};//保存维度实体信息
            	$.each(runProperties,function(i){
            		var key = runProperties[i].key;
            		var value;
            		if(runProperties[i].value.length != 1){
            			value = runProperties[i].value;
            		} else {
            			value = runProperties[i].value[0];
            		}
            		if(value == undefined || value==null || value == "" || value.trim() == ""){
            			if(runProperties[i].isMust){
            				bootbox.alert({message:key + '是必填参数，不能为空',size:'small'});
            				flag = false;
            				return false;
            			}
            		}
            		properties[key] = value;
            	});	
            	// 保存调试信息
            	properties.debug = $("input:radio[name=debug]:checked").val();
            	var params = JSON.stringify(properties);
            	if(!flag){
            		return;
            	}
        		$.ajax({
					url:'template/runJobTemplate',
		            type:"GET",
		            async:true,
		            data:{product:product,jobId:jobId,params:params},
		            contentType:'application/json',
		            success:function (data) {
		                if (data.success){
		                	bootbox.alert({message:'作业运行中<br>作业实例编号：' + data.data.jobInsId ,size:'small'});    		             	     
		                }else{
		                	var msgInfo = '运行失败,服务器处理出错<br>' + data.info;
	    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
	    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
	    					}
		                	alertor.dangerAlert(msgInfo,'middle');
		                }
		                $('#jobModal_template_run').modal('hide');
		            },
		            error: function(XMLHttpRequest,textStatus,errorThrown){
		            	bootbox.alert({message:'运行失败'+errorThrown,size:'small'});    
		                $('#jobModal_template_run').modal('hide');
		            },
	    			beforeSend : function(){
	    				mloadding.showLoadding();
	    			},
	    			complete : function(){
	    				mloadding.hideLoadding();
	    			}
		        });
			});
        },*/
        
        initializeTable:function(useId,properties,useData,useUrl,useChildUrl,columnKey,columnValue,isMust){
    		var a = useData;
			var response = [];
			if ($.isEmptyObject(a)){
				response.push({key:'',value:'',isMust:0});
			} else {
				$.each(a,function(key,value){
					if(properties == undefined || properties[key] == undefined){
						response.push({'key':key,'value':value,isMust:0});
					} else {
						response.push({'key':key,'value':value,isMust:properties[key]});
					}
				})
			}
			$.mockjax.clear(useUrl);
          	$.mockjax({ url: useUrl, logging: 0, responseText: response});
          	$.mockjax.clear(useChildUrl);
           	$.mockjax({
           	    url: useChildUrl,
           	    logging: 0,
           	    response: function(settings) {
           	    	var rowdata = {index:settings.data.pk,row:{}};
           	    	rowdata.row[settings.data.name] = settings.data.value;
           	    	$(useId).bootstrapTable('updateRow', rowdata);
           	    	$(useId + " a").editable({url:useChildUrl});
           	    }
           	});
           	$(useId).bootstrapTable('destroy');	
          	$(useId).bootstrapTable({  
          		theadClasses: 'thead-light',
        		url: useUrl,
                striped: true,  
                clickToSelect: false,  
                pagination: false,
                editable: true,
                columns: [  
                    {field:'key', title:columnKey, align:'center',                  	  
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\"  class='editable editable-click'  data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                        }},  
                    {field:'value', title:columnValue, align:'center',
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\"  class='editable editable-click'  data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                        }},
                    {field:'isMust', align:'center', title:isMust, width:'65',
                        	events:{
                        		'click .ssssss': function(e, value, row, index) {
                        			if(value == 0){
                        				row.isMust = 1;
                        			} else {
                        				row.isMust = 0;
                        			}
	                        	}
	                        },
                    	formatter: function (value, row, index) {
                    		if(useId == "#tab_task_template_properties"){
                    			if(properties != undefined && properties[row.key] == 1){
                    				return "<input type='checkbox' checked=true disabled=true  name='isMust'></input>"
                    			} else {
                    				return "<input type='checkbox'  disabled=true  name='isMust'></input>";
                    			}
                    		} else {
                    			if(value == 1){
	                    			return "<input type='checkbox' checked=true disabled=true class='ssssss'  name='isMust'></input>"
	                    		} else {
	                    			return "<input type='checkbox'  class='ssssss'  name='isMust'></input>"
	                    		}
                    		}
                          }},
                    {title:'操作', align:"center", edit:false, width:'75',
                    	events:{
                        	'click .tab_jip_removerow_style': function(e, value, row, index) {
                        		if(row.isMust){
                        			bootbox.alert({message:'必填参数不能删除',size:'small'});
                        			return;
                        		}
                           		$(useId).bootstrapTable('removeRow', index);
                           		$(useId + " a").editable({
                           			 url:useChildUrl
                                });
                        	},
                        	'click .tab_jip_appendrow_style': function(e, value, row, index) {
                       		 	$(useId).bootstrapTable('appendRow');
                       		 	$(useId + " a").editable({
                                	url:useChildUrl
                                });
                        	}
                        },
                    	formatter:function(value,row,rowIndex){
                    		var btnHtml =  
                            	'<button type="button" class="btn btn-xs btn-outline-info tab_jip_appendrow_style btn-icon">' +
                    				'<i class="fal fa-plus" aria-hidden="true"></i>' +
                    			'</button>&nbsp;'+
                            	'<button type="button" class="btn btn-xs btn-outline-danger tab_jip_removerow_style btn-icon">' +
                					'<i class="fal fa-minus" aria-hidden="true"></i>' +
                				'</button>';
                        	return btnHtml;
                    	}}
                ],
                onLoadSuccess: function (aa, bb, cc) {
                	  $(useId + " a").editable({ url:useChildUrl});
                }
        	});
    	},
        initializeTableEdit:function(useId,properties,useData,useUrl,useChildUrl,columnKey,columnValue,isMust){
    		var a = useData;
			var response = [];
			if ($.isEmptyObject(a)){
				response.push({key:'',value:'',isMust:0});
			} else {
				$.each(a,function(key,value){
					if(properties == undefined || properties[key] == undefined){
						response.push({'key':key,'value':value,isMust:0});
					} else {
						response.push({'key':key,'value':value,isMust:properties[key]});
					}
				})
			}
			$.mockjax.clear(useUrl);
          	$.mockjax({ url: useUrl, logging: 0, responseText: response});
          	$.mockjax.clear(useChildUrl);
           	$.mockjax({
           	    url: useChildUrl,
           	    logging: 0,
           	    response: function(settings) {
           	    	var rowdata = {index:settings.data.pk,row:{}};
           	    	rowdata.row[settings.data.name] = settings.data.value;
           	    	$(useId).bootstrapTable('updateRow', rowdata);
           	    	$(useId + " a").editable({url:useChildUrl});
           	    }
           	});
           	$(useId).bootstrapTable('destroy');	
          	$(useId).bootstrapTable({  
          		theadClasses: 'thead-light',
          		classes: 'table table-sm table-bordered table-striped',
        		url: useUrl,
                striped: true,  
                clickToSelect: false,  
                pagination: false,
                editable: true,
                columns: [  
                    {field:'key',title:columnKey,align:'right',  width: '200px',             	  
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\"  class='editable editable-click'  data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                        }},  
                    {field:'value',title:columnValue,align:'left',
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\"  class='editable editable-click'  data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                        }},
                    {field:'isMust',align:'center', title:isMust, width: "65px",
                        	events:{
                        		'click .ssssss': function(e, value, row, index) {
                        			if(value == 0){
                        				row.isMust = 1;
                        			} else {
                        				row.isMust = 0;
                        			}
	                        	}
	                        },
                    	formatter: function (value, row, index) {
                    		if(useId == "#tab_task_template_properties"){
                    			if(properties != undefined && properties[row.key] == 1){
                    				return "<input type='checkbox' checked=true name='isMust'></input>"
                    			} else {
                    				return "<input type='checkbox' name='isMust'></input>";
                    			}
                    		} else {
                    			if(value == 1){
	                    			return "<input type='checkbox' checked=true class='ssssss'  name='isMust'></input>"
	                    		} else {
	                    			return "<input type='checkbox'  class='ssssss'  name='isMust'></input>"
	                    		}
                    		}
                          }},
                    {title:'操作', align:"center", edit:false, width: "65px",
                    	events:{
                        	'click .tab_jip_removerow_style': function(e, value, row, index) {
                           		$(useId).bootstrapTable('removeRow', index);
                           		$(useId + " a").editable({
                           			 url:useChildUrl
                                });
                        	},
                        	'click .tab_jip_appendrow_style': function(e, value, row, index) {
                       		 	$(useId).bootstrapTable('appendRow');
                       		 	$(useId + " a").editable({
                                	url:useChildUrl
                                });
                        	}
                        },
                    	formatter:function(value,row,rowIndex){
                    		var btnHtml =  
                            	'<button type="button" class="btn btn-xs btn-outline-info tab_jip_appendrow_style btn-icon">' +
                    				'<i class="fal fa-plus" aria-hidden="true"></i>' +
                    			'</button>&nbsp;'+
                            	'<button type="button" class="btn btn-xs btn-outline-danger tab_jip_removerow_style btn-icon">' +
                					'<i class="fal fa-minus" aria-hidden="true"></i>' +
                				'</button>';
                        	return btnHtml;
                    	}}
                ],
                onLoadSuccess: function (aa, bb, cc) {
                	  $(useId + " a").editable({ url:useChildUrl});
                }
        	});
    	},
    	initializeTableSelect:function(useId,useData,useUrl,useChildUrl,columnKey,columnValue,columnId,columnUrl,columnUrlData,name){
    		var a = useData;
			var response = [];
			if ($.isEmptyObject(a)){
				response.push({key:'',value:''});
			} else {
				$.each(a,function(key,value){
					$.each(value,function(i){
						response.push({'key':key,'value':value[i]});
					});
				})
			}
			$.mockjax.clear(useUrl);
          	$.mockjax({ url: useUrl, logging: 0, responseText: response});
          	$.mockjax.clear(useChildUrl);
           	$.mockjax({
           	    url: useChildUrl,
           	    logging: 0,
           	    response: function(settings) {
           	    	var rowdata = {index:settings.data.pk,row:{}};
           	    	rowdata.row[settings.data.name] = settings.data.value;
           	    	$(useId).bootstrapTable('updateRow', rowdata);
           	    	$(useId + " a.myuseInput").editable({
           	    		url:useChildUrl
                	});
           	    	$(useId + " a.myuse").editable({
           	    		url:useChildUrl,
           	    		type:'select',
                		source:result
                	});
           	    	var entityResult = [];
           	    	var index = rowdata.index;
           	    	var propertiesObj = $(useId).bootstrapTable("getData");
                	var key = propertiesObj[index].key;
                	var columnUrlDataUse = "product="+ product + columnUrlData + key;
                	$("#" + columnId + index).editable({	
                  		url:useChildUrl,
                  		type:'select',
                  		source:function(){
                  			$.ajax({
                				url:columnUrl,
                				async:false,
                				type:"GET",
                				contentType:'application/json',
                				data:columnUrlDataUse,
                				success:function (data) {
                					if(data.success){
                						var entities = data.data.entities;
                    					entityResult = [];
                    					$.each(entities,function(i){
                    						if(name == 'entity'){
                    							entityResult.push({value:entities[i].entity,text:entities[i].entity});
                    						} else if(name == 'title') {
                    							entityResult.push({value:entities[i],text:entities[i]});
                    						}
                    					})
                					}else{
                						var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
                    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                    					}
                	                	alertor.dangerAlert(msgInfo,'middle');
                					}
                				},
                				error: function (e) {
                        			bootbox.alert({message:'查询不到结果',size:'small'});
                	   	         } 
                		  });
                  		  return entityResult;
                  		}
                  	});
           	    }
           	});
           	$(useId).bootstrapTable('destroy');
       		$(useId).bootstrapTable({
       			theadClasses: 'thead-light',
          		classes: 'table table-sm table-bordered table-striped',
        		url: useUrl,
                striped: true,  
                clickToSelect: false,  
                pagination: false,
                editable: true,
                columns: [
                    {field:'key',title:columnKey,align:'right',                 	  
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\" class='myuse editable editable-click'  data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                        }},  
                    {field:'value',title:columnValue,align:'right',
                    	formatter: function (value, row, index) {
                    		var usecolumnId = columnId + index;
                            return "<a href=\"#\" id=" + usecolumnId + " data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                        }},
                    {title:'操作', align:"center", edit:false, width: "75",
                    	events:{
                        	'click .tab_jip_removerow_style': function(e, value, row, index) {
                           		$(useId).bootstrapTable('removeRow', index);
                           		$(useId + " a.myuse").editable({
                           			 url:useChildUrl,
                                	 type:'select',
                                	 source:result
                                });
                        	},
                        	'click .tab_jip_appendrow_style': function(e, value, row, index) {
                       		 	$(useId).bootstrapTable('appendRow');
                       		 	$(useId + " a.myuse").editable({
                                	url:useChildUrl,
                            		type:'select',
                            		source:result
                                });
                        	}
                        },
                    	formatter:function(value,row,rowIndex){  
                    		var btnHtml =  
	                        	'<button type="button" class="btn btn-xs btn-outline-info btn-icon tab_jip_appendrow_style">' +
		            				'<i class="fal fa-plus" aria-hidden="true"></i>' +
		            			'</button>&nbsp;' +
		                    	'<button type="button" class="btn btn-xs btn-outline-danger btn-icon tab_jip_removerow_style">' +
		        					'<i class="fal fa-minus" aria-hidden="true"></i>' +
		        				'</button>';
                        	return btnHtml;
                    	}}
                ],
                onLoadSuccess: function (aa, bb, cc) {
                	  $(useId + " a.myuse").editable({ 
                		  url:useChildUrl,
                		  type:'select',
                		  source:result
                	  });
                }
        	});
    	},
    	initializeTableSelectModel:function(useId,useData,useUrl,useChildUrl,columnKey,columnValue,columnId,columnUrl,columnUrlData,name){
    		var getEntityResult = function(key) {
    			var tResult = [];
    			var params = "product="+ product + columnUrlData + key;
    			$.ajax({
    				url:columnUrl,
    				async:false,
    				type:"GET",
    				contentType:'application/json',
    				data:params,
    				success:function (data) {
    					if(data.success){
    						var entities = data.data.entities;
        					entityResult = [];
        					$.each(entities,function(i){
        						if(name == 'entity'){
        							tResult.push({value:entities[i].entity,text:entities[i].entity});
        						} else if(name == 'title') {
        							tResult.push({value:entities[i],text:entities[i]});
        						}
        					})
    					}else{
    						var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	alertor.dangerAlert(msgInfo,'middle');
    					}
    				},
    				error: function (e) {
            			bootbox.alert({message:'查询不到结果',size:'small'});
    	   	         } 
    		    });
    			return tResult;
    		}
    		var a = useData;
			var response = [];
			if ($.isEmptyObject(a)){
				response.push({key:'',value:'','modelNo':''});
			} else {
				$.each(a,function(key1,value1){
					$.each(value1,function(key2,value2){
						$.each(value2,function(i){
							response.push({'modelNo':key1,'key':key2,'value':value2[i]});
						});
					});
	        	});
			}
			$.mockjax.clear(useUrl);
          	$.mockjax({ url: useUrl, logging: 0, responseText: response});
          	$.mockjax.clear(useChildUrl);
           	$.mockjax({
           		theadClasses: 'thead-light',
           	    url: useChildUrl,
           	    logging: 0,
           	    response: function(settings) {
           	    	var rowdata = {index:settings.data.pk,row:{}};
           	    	rowdata.row[settings.data.name] = settings.data.value;
           	    	$(useId).bootstrapTable('updateRow', rowdata);
           	    	$(useId + " a.myuseInput").editable({
           	    		url:useChildUrl
                	});
           	    	$(useId + " a.myuse").editable({
           	    		url:useChildUrl,
           	    		type:'select',
                		source:result
                	});
           	    	var index = rowdata.index;
           	    	var propertiesObj = $(useId).bootstrapTable("getData");
                	var key = propertiesObj[index].key;
                	$("#" + columnId + index).editable({	
                  		url:useChildUrl,
                  		type:'select',
                  		value:propertiesObj[index].value,
                  		source:getEntityResult(key)
                  	});
           	    }
           	});
           	$(useId).bootstrapTable('destroy');
    		$(useId).bootstrapTable({
    			theadClasses: 'thead-light',
        		url: useUrl,
                striped: true,  
                clickToSelect: false,  
                pagination: false,
                editable: true,
                columns: [
					{field:'modelNo',title:"编号",align:'right',                    	  
					    formatter: function (value, row, index) {
					        return "<a href=\"#\" class='myuseInput editable editable-click'  data-name=\"modelNo\" data-pk=\""+index+"\" data-title='编号'>" + value + "</a>";
					     }},
                    {field:'key',title:columnKey,align:'right',                   	  
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\" class='myuse editable editable-click'  data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                        }},  
                    {field:'value',title:columnValue,align:'right',
                    	formatter: function (value, row, index) {
                    		var usecolumnId = columnId + index;
                            return "<a href=\"#\" class='myvalue editable editable-click' id=" + usecolumnId + " data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                        }},
                    {title:'操作', align:"center", edit:false, width: "75",
                    	events:{
                        	'click .tab_jip_removerow_style': function(e, value, row, index) {
                           		$(useId).bootstrapTable('removeRow', index);
                           		$(useId + " a.myuseInput").editable({
        	           	    		url:useChildUrl
        	                	});
                           		$(useId + " a.myuse").editable({
                           			 url:useChildUrl,
                                	 type:'select',
                                	 source:result
                                });
                           		var rows = $(useId).bootstrapTable("getData");
		                       	for(var i=0,len=rows.length; i<len; i++) {
		                       		if(rows[i].key) {
		                       			$("#" + columnId + i).editable({	
		                       				url:useChildUrl,
		                       				type:'select',
		                       				value:rows[i].value,
		                       				source:getEntityResult(rows[i].key)
		                       			});
		                       		}
		                       	}
                        	},
                        	'click .tab_jip_appendrow_style': function(e, value, row, index) {
                       		 	$(useId).bootstrapTable('appendRow');
                           		$(useId + " a.myuseInput").editable({
        	           	    		url:useChildUrl
        	                	});
                       		 	$(useId + " a.myuse").editable({
                                	url:useChildUrl,
                            		type:'select',
                            		source:result
                                });
	                       		var rows = $(useId).bootstrapTable("getData");
		                       	for(var i=0,len=rows.length; i<len; i++) {
		                       		if(rows[i].key) {
		                       			$("#" + columnId + i).editable({	
		                       				url:useChildUrl,
		                       				type:'select',
		                       				value:rows[i].value,
		                       				source:getEntityResult(rows[i].key)
		                       			});
		                       		}
		                       	}
                        	}
                        },
                    	formatter:function(value,row,rowIndex){  
                    		var btnHtml =  
                            	'<button type="button" class="btn btn-xs btn-outline-info btn-icon tab_jip_appendrow_style">' +
                    				'<i class="fal fa-plus" aria-hidden="true"></i>' +
                    			'</button>&nbsp;' +
                            	'<button type="button" class="btn btn-xs btn-outline-danger btn-icon tab_jip_removerow_style">' +
                					'<i class="fal fa-minus" aria-hidden="true"></i>' +
                				'</button>';
                        	return btnHtml;
                    	}}
                ],
                onLoadSuccess: function (aa, bb, cc) {
                	  $(useId + " a.myuseInput").editable({
           	    		  url:useChildUrl
                	  });
                	  $(useId + " a.myuse").editable({ 
                		  url:useChildUrl,
                		  type:'select',
                		  source:result
                	  });
                	  var rows = $(useId).bootstrapTable("getData");
                	  for(var i=0,len=rows.length; i<len; i++) {
                		  if(rows[i].key) {
                			  $("#" + columnId + i).editable({	
		                    		url:useChildUrl,
		                    		type:'select',
		                    		value:rows[i].value,
		                    		source:getEntityResult(rows[i].key)
		                    	});
                		  }
                	  }
                }
        	});
    	},
    	initializeTableSpecialModel:function(useId,useData,useUrl,useChildUrl,a1,a2,a3,a4){
    		var a = useData;
			var response = [];
			if ($.isEmptyObject(a)){
				response.push({taskId:'',exp:'',beTaskId:'',beExp:''});
			} else {
				$.each(a,function(key,value){
					var keyArray = key.split('|');
					$.each(value,function(i){
						var valueArray = value[i].split('|');
						var exp = "";
						if(keyArray[1] != "null"){
							exp = keyArray[1];
						}
						var beExp = "";
						if(value[i].substring(valueArray[0].length + 1) != "null"){
							beExp = value[i].substring(valueArray[0].length + 1); 
						}
						response.push({"taskId":keyArray[0],"exp":exp,"beTaskId":valueArray[0],"beExp":beExp});
					});
	        	});
			}
			$.mockjax.clear(useUrl);
          	$.mockjax({ url: useUrl, logging: 0, responseText: response});
          	$.mockjax.clear(useChildUrl);
           	$.mockjax({
           		theadClasses: 'thead-light',
           	    url: useChildUrl,
           	    logging: 0,
           	    response: function(settings) {
           	    	var rowdata = {index:settings.data.pk,row:{}};
           	    	rowdata.row[settings.data.name] = settings.data.value;
           	    	$(useId).bootstrapTable('updateRow', rowdata);
           	    	$(useId + " a.myuseInput").editable({
           	    		url:useChildUrl
                	});
               		$(useId + " a.myuse").editable({
               			 url:useChildUrl,
                    	 type:'select',
                    	 source:resultTasks
                    });
           	    }
           	});
           	$(useId).bootstrapTable('destroy');
    		$(useId).bootstrapTable({
    			theadClasses: 'thead-light',
        		url: useUrl,
                striped: true,  
                clickToSelect: false,  
                pagination: false,
                editable: true,
                columns: [
					{field:'taskId',title:a1,align:'right',                    	  
					    formatter: function (value, row, index) {
					        return "<a href=\"#\" class='myuse editable editable-click'  data-name=\"taskId\" data-pk=\""+index+"\" data-title=" + a1 + ">" + value + "</a>";
					     }},
                    {field:'exp',title:a2,align:'right',                   	  
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\" class='myuseInput editable editable-click' data-name=\"exp\" data-pk=\""+index+"\" data-title=" + a2 + ">" + value + "</a>";
                        }},  
                    {field:'beTaskId',title:a3,align:'right',
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\" class='myuse editable editable-click' data-name=\"beTaskId\" data-pk=\""+index+"\" data-title=" + a3 + ">" + value + "</a>";
                        }},
                    {field:'beExp',title:a4,align:'right',
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\" class='myuseInput editable editable-click' data-name=\"beExp\" data-pk=\""+index+"\" data-title=" + a4 + ">" + value + "</a>";
                        }},
                    {title:'操作', align:"center", edit:false, width: "75",
                    	events:{
                        	'click .tab_jip_removerow_style': function(e, value, row, index) {
                           		$(useId).bootstrapTable('removeRow', index);
                           		$(useId + " a.myuseInput").editable({
        	           	    		url:useChildUrl
        	                	});
                           		$(useId + " a.myuse").editable({
                           			 url:useChildUrl,
                                	 type:'select',
                                	 source:resultTasks
                                });
                        	},
                        	'click .tab_jip_appendrow_style': function(e, value, row, index) {
                       		 	$(useId).bootstrapTable('appendRow');
                           		$(useId + " a.myuseInput").editable({
        	           	    		url:useChildUrl
        	                	});
                       		 	$(useId + " a.myuse").editable({
                                	url:useChildUrl,
                            		type:'select',
                            		source:resultTasks
                                });
                        	}
                        },
                    	formatter:function(value,row,rowIndex){  
                    		var btnHtml =  
                            	'<button type="button" class="btn btn-xs btn-outline-info btn-icon tab_jip_appendrow_style">' +
                    				'<i class="fal fa-plus" aria-hidden="true"></i>' +
                    			'</button>&nbsp;' +
                            	'<button type="button" class="btn btn-xs btn-outline-danger btn-icon tab_jip_removerow_style">' +
                					'<i class="fal fa-minus" aria-hidden="true"></i>' +
                				'</button>';
                        	return btnHtml;
                    	}}
                ],
                onLoadSuccess: function (aa, bb, cc) {
                	  $(useId + " a.myuseInput").editable({
           	    		  url:useChildUrl
                	  });
                	  $(useId + " a.myuse").editable({ 
                		  url:useChildUrl,
                		  type:'select',
                		  source:resultTasks
                	  });  
                }
        	});
    	},
    	copyAddJob: function () {
    		var self = this;
    		var selected = $('#tb_job').bootstrapTable('getSelections');
        	if (selected.length == 1) {
        		var modal = $("#copyAddJobModal");
        		// 产品
        		modal.find("select[name=product]").val(selected[0].product);
        		// 任务编号
        		modal.find("input[name=jobId]").val(selected[0].jobId);
        		// 任务名称
        		modal.find("input[name=name]").val(selected[0].name);
        		// 版本
        		modal.find("input[name=version]").val(selected[0].version);
        		// 创建人
        		modal.find("input[name=creator]").val(selected[0].creator);
        		// 创建时间
        		modal.find("input[name=createTime]").val(selected[0].createTime);
        		// 资源编号
        		modal.find("input[name=scheduleRid]").val(selected[0].scheduleRid);
        		// 标签
        		modal.find("input[name=job_tags]").tagsinput({style:'min-width:80%;'});
	        	if(selected[0].title != undefined){
	        		modal.find("input[name=job_tags]").tagsinput("removeAll");
	        		var tags = selected[0].title.split(",");
	        		$.each(tags,function(i){
	        			modal.find("input[name=job_tags]").tagsinput("add",tags[i]);
		        	})
	        	} else {
	        		modal.find("input[name=job_tags]").tagsinput("removeAll");
	        	}
	        	modal.modal("show");
	        	$("#saveCopyAddJob").unbind("click").bind("click", function() {
	        		self.saveCopyAddJob(selected[0]);
	        	});
        	} else {
        		bootbox.alert({message:'请选择一个任务模板',size:'small'});
        	}
    	},
    	addJob: function (container) {
    		var self = this;
        	product = "";
        	$("#addOrEdit").empty();
        	$("#addOrEdit").append("新增");
			$('#jobModal_template').modal('show');
			jobId = "";
			self.initTaskTable(null);
			var f = $('#tabs_jtpl_baseinfo');
			f.find("select[name=product]")[0].disabled = false;
        	f.find("input[name=jobId]")[0].disabled = false;
        	f.find("input[name=jobId]").val("");
        	f.find("select[name=mode]").val("2");
        	f.find("input[name=name]").val("");
        	//f.find("select[name=isEnable]").val("1");
        	$('#job_tags').tagsinput({style:'min-width:90%'});
        	$("#job_tags").tagsinput("removeAll");
        	//f.find("input[name=jobPriority]").val("");
        	f.find("input[name=creator]").val("admin");
        	f.find("input[name=createTime]").val(self.getSysYmd());
        	f.find("input[name=version]").val("");
			f.find("select[name=product]").val("");
			f.find("input[name=scheduleRid]").val("");
			
			$("#jobTemplateSpecialView").bootstrapTable('destroy');
    		$("#tab_job_dimension_entity").bootstrapTable('destroy');
    		$("#tab_job_dimension_title").bootstrapTable('destroy');
        	if(f.find("select[name=mode]").val() == 2){ 
        		self.initializeTableSelectModel("#tab_job_dimension_entity",null,"/jobDimensionEntities","/updateCellDimensionEntity","维度","实体","jobDimensionEntity","template/getDimensionEntityByProductAndDimension","&dimensionName=","entity");
        		self.initializeTableSelectModel("#tab_job_dimension_title",null,"/jobDimensionTitles","/updateCellDimensionTitle","维度","标签","jobDimensionTitle","template/getTitleByProductAndDimension","&dimensionName=","title");
        		self.initializeTableSpecialModel("#jobTemplateSpecialView",{},"/jobSpecialLeans","/updateSpecialLeans","任务项编号","表达式","被依赖任务项编号","表达式");
        		$("#tr_addDimension").css("display","");
        		$("#tr_specialLean").css("display","");
        	}else{
        		$("#tr_addDimension").css("display","none");
        		$("#tr_specialLean").css("display","none");
        	}
        	self.initializeTableEdit("#tab_job_template_properties",null,null,"/properties","/updateCellProperties","属性","默认值","是否必填");
        	$('#saveJobinfo').unbind('click').bind('click', function(event){
				if(container == "new"){
					self.saveJobinfo("new");
				} else {
					self.saveJobinfo();
				}
			});		
        },
        saveJobinfo: function (isAdd) {
        	var self = this;
    		var isSave = true;
    		var modal = $('#jobModal_template');
        	var formJson = {};
        	formJson.product = modal.find("select[name=product]").val();
        	if(null == formJson.product || formJson.product == ""){
        		bootbox.alert({message:'操作失败，请选择产品',size:'small'});                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
    			return;
        	}
    		formJson.jobId = modal.find("input[name=jobId]").val();
    		if(!reg2.test(formJson.jobId)){
    			bootbox.alert({message:'操作失败，任务编号只能为数字、字母或者下划线',size:'small'});                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
    			return;
    		}
    		if(!validateStringLength(formJson.jobId,50)){
        		bootbox.alert({message:'操作失败，任务编号的长度不能超过50',size:'small'}); 
        		return false;
        	}
    		formJson.mode = modal.find("select[name=mode]").val();	
    		formJson.name = modal.find("input[name=name]").val();
    		if(null == formJson.name || formJson.name == ""){
    			bootbox.alert({message:'操作失败，任务名称不能为空',size:'small'});  
    			return;
    		}
    		if(!validateStringLength(formJson.name,200)){
        		bootbox.alert({message:'操作失败，任务名称的长度不能超过200',size:'small'}); 
        		return false;
        	}
    		formJson.title = $("#job_tags").val();
    		if(!validateStringLength(formJson.title,500)){
        		bootbox.alert({message:'操作失败，标签的长度不能超过500',size:'small'}); 
        		return false;
        	}
    		/*if(modal.find("select[name=isEnable]").val() == 1){
    			formJson.enable = true;
    		} else {
    			formJson.enable = false;
    		}*/
    		/*formJson.jobPriority = modal.find("input[name=jobPriority]").val();
    		if(isNaN(formJson.jobPriority)){
    			bootbox.alert({message:'优先级必须为数字类型',size:'small'});
    			return;
    		}*/
    		formJson.scheduleRid = modal.find("input[name=scheduleRid]").val();
    		if(!validateStringLength(formJson.scheduleRid,100)){
        		bootbox.alert({message:'操作失败，资源编号的长度不能超过100',size:'small'}); 
        		return false;
        	}
    		formJson.creator = modal.find("input[name=creator]").val();
    		if(!validateStringLength(formJson.creator,50)){
        		bootbox.alert({message:'操作失败，创建人的长度不能超过50',size:'small'}); 
        		return false;
        	}
    		formJson.createTime = modal.find("input[name=createTime]").val();
    		if(null == formJson.createTime || formJson.createTime == ""){
    			bootbox.alert({message:'操作失败，创建时间不能为空',size:'small'});  
    			return;
    		}
    		formJson.version = modal.find("input[name=version]").val();
    		if(!validateStringLength(formJson.version,30)){
        		bootbox.alert({message:'操作失败，版本的长度不能超过30',size:'small'}); 
        		return false;
        	}
        	if(formJson.mode == 2){
        		var entitiesObj = $('#tab_job_dimension_entity').bootstrapTable("getData");
            	var dimensionsJson = {};//保存维度实体信息
            	$.each(entitiesObj,function(i){
            		var modelNo = entitiesObj[i].modelNo;
            		var dimension = entitiesObj[i].key;
            		if(modelNo == ""){
            			return;
            		}
            		var dimensionEntity = entitiesObj[i].value;
            		if(entitiesObj[i].value.length != 1){
            			dimensionEntity = entitiesObj[i].value;
            		} else {
            			dimensionEntity = entitiesObj[i].value[0];
            		}
            		if(!(null == dimension || dimension == "")){
            			if(null == dimensionEntity || dimensionEntity == ""){
        					bootbox.alert({message:'操作失败，维度有值，实体不能为空',size:'small'});
        					isSave = false;
        				}
    				}
            		if(dimensionsJson[modelNo] == null || dimensionsJson[modelNo] == undefined){
    					dimensionsJson[modelNo] = {};
    				}
            		if(dimensionsJson[modelNo][dimension] == null || dimensionsJson[modelNo][dimension] == undefined){
    					dimensionsJson[modelNo][dimension] = [];
    				}
            		dimensionsJson[modelNo][dimension].push(dimensionEntity);
            	});
            	var titlesObj = $('#tab_job_dimension_title').bootstrapTable("getData");
            	$.each(titlesObj,function(i){
            		var modelNo = titlesObj[i].modelNo;
            		var dimension = titlesObj[i].key;
            		if(modelNo == ""){
            			return;
            		}
            		if(titlesObj[i].value.length != 1){
            			dimensionTitle = titlesObj[i].value;
            		} else {
            			dimensionTitle = titlesObj[i].value[0];
            		}
            		if(!(null == dimension || dimension == "")){
            			if(null == dimensionTitle || dimensionTitle == ""){
        					bootbox.alert({message:'操作失败，维度有值,标签不能为空',size:'small'});
        					isSave = false;
        				}
    				}
            		if(dimensionsJson[modelNo] == null || dimensionsJson[modelNo] == undefined){
            			dimensionsJson[modelNo] = {};
    				}
            		if(dimensionsJson[modelNo][dimension] == null || dimensionsJson[modelNo][dimension] == undefined){
            			dimensionsJson[modelNo][dimension] = [];
    				}			
            		dimensionsJson[modelNo][dimension].push("TAG:" + dimensionTitle);
            	});
            	formJson.defaultDimensions = dimensionsJson;
            	var specialLeans = $('#jobTemplateSpecialView').bootstrapTable("getData");
            	var specialLeansJson = {};//保存任务参数信息
            	var jobTemplateSpecialViewError = false;
            	$.each(specialLeans,function(i){
            		if(specialLeans[i].taskId == ""  && specialLeans[i].beTaskId != ""){
            			jobTemplateSpecialViewError = true;
            			bootbox.alert({message:'操作失败，第'+(i+1)+'行，特殊依赖关系/任务项编号不能为空',size:'small'}); 
            			return false;
            		}
            		if(specialLeans[i].taskId != ""  && specialLeans[i].beTaskId == ""){
            			jobTemplateSpecialViewError = true;
            			bootbox.alert({message:'操作失败，第'+(i+1)+'行，特殊依赖关系/被依赖任务项编号不能为空',size:'small'}); 
            			return false;
            		}
            		if(specialLeans[i].taskId != "" && specialLeans[i].beTaskId != ""){
            			var key = specialLeans[i].taskId + "|" + specialLeans[i].exp;
                		var value = specialLeans[i].beTaskId + "|" + specialLeans[i].beExp;
                		if(specialLeansJson[key] == undefined){
                			specialLeansJson[key] = [];
                		}
                		specialLeansJson[key].push(value);
            		}
            	});
            	if(jobTemplateSpecialViewError){
            		return false;
            	}
            	formJson.specialLeans = specialLeansJson;
        	}
        	var propertiesObj = $('#tab_job_template_properties').bootstrapTable("getData");
        	var jobParamterIsMust = {};
        	var parametersJson = {};//保存任务参数信息
        	var jobParametersErrorFlag = false;
        	$.each(propertiesObj,function(i){
        		var jobPropertyKey = propertiesObj[i].key;
        		if(jobPropertyKey != ""){
        			if(propertiesObj[i].isMust == 1){
        				jobParamterIsMust[jobPropertyKey] = 1;
        			} else {
        				jobParamterIsMust[jobPropertyKey] = 0;
        			}
        			var jobPropertyValue;
            		if(propertiesObj[i].value.length != 1){
            			jobPropertyValue = propertiesObj[i].value;
            		} else {
            			jobPropertyValue = propertiesObj[i].value[0];
            		}
            		/*
            		if(jobPropertyValue == undefined || jobPropertyValue == null || jobPropertyValue == "" || jobPropertyValue.trim() == ""){
            			if(propertiesObj[i].isMust){
            				bootbox.alert({message:jobPropertyKey + '是必填参数，不能为空',size:'small'});
            				isSave = false;
            				return false;//跳出整个循环
            			}
            		}*/
            		if(!validateStringLength(jobPropertyKey,200)){
            			jobParametersErrorFlag = true;
                		bootbox.alert({message:'操作失败，第'+(i+1)+'行，属性的长度不能超过200',size:'small'}); 
                		return false;
                	}
            		/*
            		if(!validateStringLength(jobPropertyValue,2000)){
            			jobParametersErrorFlag = true;
                		bootbox.alert({message:'操作失败，第'+(i+1)+'行，默认值的长度不能超过2000',size:'small'}); 
                		return false;
                	}*/
    				parametersJson[jobPropertyKey] = jobPropertyValue;	
        		}
        	});
        	if(jobParametersErrorFlag){
        		return false;
        	}
        	formJson.jobParamterIsMust = jobParamterIsMust;
        	formJson.properties = parametersJson;
        	var mutexObj = $('#tab_job_template_mutex_edit').bootstrapTable("getData");
        	var mutexObjectSave = {};
        	$.each(mutexObj,function(i){
        		var taskIds = mutexObj[i].taskIds;
        		var group = mutexObj[i].group;
        		if(!(group == undefined || group == null || group == "")){
        			if(mutexObject[group] != undefined){
            			$.each(taskIds,function(j){
            				if(mutexObject[group][taskIds[j]] == undefined){
            					mutexObject[group][taskIds[j]] = {};
            				}
            				if(mutexObjectSave[group] == undefined){
            					mutexObjectSave[group] = {};
            				}
            				mutexObjectSave[group][taskIds[j]] = mutexObject[group][taskIds[j]];
            			})
            		} else {
            			mutexObjectSave[group] = {};
            			$.each(taskIds,function(j){
            				mutexObjectSave[group][taskIds[j]] = {};
            			})
            		}
        		}
        	})
        	formJson.mutexGroups = mutexObjectSave;
        	if(!isSave){
        		return;
        	}
        	var url;
        	if(isAdd == "new"){
        		url = 'template/saveJob';
        	} else {
        		url = 'template/updateJob';
        	}
        	if(!mloadding.showLoadding()){
        		return false;
        	}
			$.ajax({
				url:url,
	            type:"POST",
	            processData:false,
	            data:JSON.stringify(formJson),
	            contentType:'application/json',
	            success:function (data) {
	                if (data.success){
	                	bootbox.alert({message:'保存成功',size:'small'});
	                	$('#tb_job').bootstrapTable('refresh');
	                	$('#jobModal_template').modal('hide');
	                }else{
	                	var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
	                	alertor.dangerAlert(msgInfo,'middle');
	                }
	                mloadding.hideLoadding();
	            },
	            error: function(XMLHttpRequest,textStatus,errorThrown){
	            	bootbox.alert({message:'保存失败'+errorThrown,size:'small'});    
	            	mloadding.hideLoadding();
	            }
	        });
    	},
    	delJob : function(event){
        	var selected = $('#tb_job').bootstrapTable('getSelections');
        	if(selected.length > 0){
        		var jobs = [];
        		_.each(selected, function(element, index){
        			jobs.push(element.jobId + "+" + element.product);
        			return;
        		});
        		var tConfirm = true;
        		bootbox.confirm({
            		size : 'small',
            		message : '是否确定删除所选任务吗？',
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
            				mloadding.showLoadding();
                    		$.ajax({
                    			url : 'template/jobs/' + jobs.join('|'),
                    			type : 'DELETE',
                    			contentType:'application/json',
                    			success: function(data){
                    				if(data.success){
                    					bootbox.alert({message:'删除成功',size:'small'});
                    					$('#tb_job').bootstrapTable('refresh');
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
                		}
                	 }
            	});
        	} else{
        		bootbox.alert({message:'请选择需要删除的任务模板',size:'small'});
        	}
        },
        openExportJobTemplateModal : function(){
        	var self = this;
        	$("#exportJobTemplateModal").find("input[name=exportFileType]").val("SQL");
        	$("#exportJobTemplateModal").find("input[name=exportFileName]").val("jt_productid_"+dateFormat("yyyyMMdd",new Date()));
        	$.each($("#exportFileCatalog").find("button"),function(index,but){//弹框消失样式清空
            	$(but).attr("class","btn btn-sm btn-default mb-1 mr-1");
        	});
        	$.each($("#exportFileCatalog").find("button"),function(index,but){//循环加点击事件
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
        	$("#exportJobTemplateModal").modal("show");
        	$('#expotJobTemplate', this.o_container).bind('click', function(event){ self.expotJobTemplate(); });
        },
        openImportJobTemplateModal : function(){
        	var self = this;
        	//加载查询条件产品下拉数据
            var uProdSelect = $("#jobTemplateImportForm").find("select[name=uProduct]");
          	$.ajax({
	        		async: false,
	    			url: 'product/selectProduct',
	    			type: 'GET',
	    			contentType: 'application/json',
	    			success: function(data){
	    				if(data.success){
							var products = data.data.products;
							uProdSelect.empty();
							uProdSelect.append("<option value=''>请选择产品</option>");
							$.each(products,function(i){
								uProdSelect.append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
							});
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
          	var qProduct = $("#nav_jtpl_content_list").find("select[name=product]").val();
          	$("#jobTemplateImportForm").find("select[name=uProduct]").val(qProduct);
        	$("#importJobTemplateModal").modal("show");
        	$('#importJobTemplate', this.o_container).unbind('click').bind('click', function(event){ self.importJobTemplate(); });
        },
    	productChanged : function(){
    		var self = this;
        	var f = $('#jobModal_template');
        	var newProduct = f.find("select[name=product]").val();
        	if(newProduct != "" && product != newProduct){
        		product = newProduct;
        		$.ajax({
    				url:'template/getDimensionByProduct',
      				async:false,
      				type:"GET",
      				data:{product:product},
      				success:function (data) {
      					if(data.success){
      						result = [];
      						var dimensions = data.data.dimensions;
          					$.each(dimensions,function(i){
          						result.push({value:dimensions[i].name,text:dimensions[i].name});
          					});
          					self.initializeTableEdit("#tab_job_template_properties",null,null,"/properties","/updateCellProperties","属性","默认值","是否必填");
          					self.initializeTableSelectModel("#tab_job_dimension_entity",null,"/jobDimensionEntities","/updateCellDimensionEntity","维度","实体","jobDimensionEntity","template/getDimensionEntityByProductAndDimension","&dimensionName=","entity");
          					self.initializeTableSelectModel("#tab_job_dimension_title",null,"/jobDimensionTitles","/updateCellDimensionTitle","维度","标签","jobDimensionTitle","template/getTitleByProductAndDimension","&dimensionName=","title");
      					} else {
      						var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	alertor.dangerAlert(msgInfo,'middle');
      					}
      				},
      				error: function (e) {
      					bootbox.alert({message:'获取维度失败',size:'small'});
      	   	         } 
      		    });
        	}
        },
        jobModeChanged : function(){
        	var self = this;
			var f = $('#jobModal_template');
        	if(f.find("select[name=mode]").val() == 2){
        		self.initializeTableSpecialModel("#jobTemplateSpecialView",null,"/jobSpecialLeans","/updateSpecialLeans","任务项编号","表达式","被依赖任务项编号","表达式");
        		self.initializeTableSelectModel("#tab_job_dimension_entity",null,"/jobDimensionEntities","/updateCellDimensionEntity","维度","实体","jobDimensionEntity","template/getDimensionEntityByProductAndDimension","&dimensionName=","entity");
        		self.initializeTableSelectModel("#tab_job_dimension_title",null,"/jobDimensionTitles","/updateCellDimensionTitle","维度","标签","jobDimensionTitle","template/getTitleByProductAndDimension","&dimensionName=","title");
	        	$("#tr_addDimension").css("display","");
        		$("#tr_specialLean").css("display","");
        	} else {
        		$("#jobTemplateSpecialView").bootstrapTable('destroy');
        		$("#tab_job_dimension_entity").bootstrapTable('destroy');
        		$("#tab_job_dimension_title").bootstrapTable('destroy');
        		$("#tr_addDimension").css("display","none");
        		$("#tr_specialLean").css("display","none");
        	}	
        },
        initJobDmsnSchemaSelcet:function(sel, product, jobId){
			$.ajax({
				async:false,
				data : {'product':product,'jobId':jobId},
				url : 'template/getJob',
				type : 'GET',
				contentType:'application/json',
				success: function(data){
					if(data.success){
						var defaultDimensions = data.data.job.defaultDimensions;
						sel.empty();
	        			sel.append('<option value="0" selected >0</option>');
						if (data.data.job) {
							var jobDmsnSchemas = data.data.job.defaultDimensions;
							$.each(jobDmsnSchemas,function(key,value){
								sel.append("<option value='" + key + "'>" + key + "</option>");
							})
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
        /*initPluginSelcet:function(sel){
        	$.ajax({
        		async:false,
        		data : {product:"wws"},
        		url: 'template/getPluginByProduct',
    			type : 'GET',
    			contentType:'application/x-www-form-urlencoded',
    	        success:function (data) {
    	        	if(data.success){
    	        		var plugins = data.data.plugins;
    	        		sel.empty();
	        			sel.append('<option value="" selected>请选择</option>');
    	        		if (plugins && plugins.length > 0){
    	        			for(var i=0;i<plugins.length;i++){
        	        			sel.append("<option value='" + plugins[i].name + "'>" + plugins[i].name + "</option>");
        	        		}
    	        		}
    	        	} else {
    	        		bootbox.alert({message:'获取插件失败',size:'small'});
    	        	}
    	       },
    	       error: function (e) {
    	    	   bootbox.alert({message:'获取插件失败' + e.status,size:'small'});
    	       }  	
    		})
        },*/
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
		//关闭tab
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
		},
		getSysYmd:function(){
			var date = new Date();
        	var cTime = date.getFullYear()+'-';
        	var tMonth = date.getMonth()+1;
        	if(tMonth < 10){
        		cTime = cTime + "0"+tMonth+"-";
        	}else{
        		cTime = cTime + tMonth+"-";
        	}
        	var tDate = date.getDate();
        	if(tDate < 10){
        		cTime = cTime + "0"+tDate;
        	}else{
        		cTime = cTime + tDate;
        	}
        	return cTime;
		},
		saveCopyAddJob:function(row) {
			var modal = $("#copyAddJobModal");
			// 保存源任务模板
			var data = [];
			data.push(row);
			// 新任务模板
			var formJson = {};
			// 产品
			formJson.product = modal.find("select[name=product]").val();
			if(!formJson.product){
        		bootbox.alert({message:'请选择产品',size:'small'});                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
    			return;
        	}
			// 任务编号
			formJson.jobId = modal.find("input[name=jobId]").val();
			if(!/^[0-9a-zA-Z_]+$/g.test(formJson.jobId)){
        		bootbox.alert({message:'操作失败，任务编号必须为字母， 数字，下划线组成',size:'small'});                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
    			return;
        	}
			if(!validateStringLength(formJson.jobId,50)){
        		bootbox.alert({message:'操作失败，任务编号的长度不能超过50',size:'small'}); 
        		return false;
        	}
			// 任务名称
			formJson.name = modal.find("input[name=name]").val();
			if(!formJson.name){
        		bootbox.alert({message:'操作失败，任务名称不能为空',size:'small'});                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
    			return;
        	}
			if(!validateStringLength(formJson.name,200)){
        		bootbox.alert({message:'操作失败，任务名称的长度不能超过200',size:'small'}); 
        		return false;
        	}
			// 创建时间
			formJson.createTime = modal.find("input[name=createTime]").val();
			if(!formJson.createTime){
        		bootbox.alert({message:'操作失败，创建时间不能为空',size:'small'});                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
    			return;
        	}
			// 版本
			formJson.version = modal.find("input[name=version]").val();
			if(!validateStringLength(formJson.version,30)){
        		bootbox.alert({message:'操作失败，版本的长度不能超过30',size:'small'}); 
        		return false;
        	}
			// 创建人
			formJson.creator = modal.find("input[name=creator]").val();
			if(!validateStringLength(formJson.creator,50)){
        		bootbox.alert({message:'操作失败，创建人的长度不能超过50',size:'small'}); 
        		return false;
        	}
			// 标签
			formJson.title = modal.find("input[name=job_tags]").val();
			if(!validateStringLength(formJson.title,500)){
        		bootbox.alert({message:'操作失败，标签的长度不能超过500',size:'small'}); 
        		return false;
        	}
			// 调度模型
			formJson.mode = row.mode;
			// 资源编号
			formJson.scheduleRid = modal.find("input[name=scheduleRid]").val();
			if(!validateStringLength(formJson.scheduleRid,100)){
        		bootbox.alert({message:'操作失败，资源编号的长度不能超过100',size:'small'}); 
        		return false;
        	}
			/*	
			// 参数
			formJson.properties = row.properties;
			// 参数是否必填
			formJson.jobParamterIsMus = row.jobParamterIsMus;
			// 维度
			formJson.defaultDimensions = row.defaultDimensions;
			// 特殊依赖
			formJson.specialLeans = row.specialLeans;
			// 互斥组
			formJson.mutexGroups = row.mutexGroups;
			*/
			data.push(formJson);
			$.ajax({
				url:"template/copyAddJob",
	            type:"POST",
	            processData:false,
	            data:JSON.stringify(data),
	            contentType:'application/json',
	            success:function (data) {
	                if (data.success){
	                	bootbox.alert({message:'保存成功',size:'small'});
	                	$('#tb_job').bootstrapTable('refresh');
	                	$('#copyAddJobModal').modal('hide');
	                }else{
	                	var msgInfo = '复制新增失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
	                	alertor.dangerAlert(msgInfo,'middle');
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
		batchRunHistory : function(product, jobId) {
			var self = this;
			
			// 置空过滤框
			$("input[name=unselectTaskId]").val("");
			$("input[name=selectTaskId]").val("");
			
			// 填充标题
			$("#currentJobIdHistory").text(product+" "+jobId);
		
			// 初始化左侧历史记录列表
			self.initJobRunHistoryTable(product, jobId);
			
			// 初始化debug模式
			$("input:radio[name=debug_history]").eq(0).prop("checked", true);
			
			// 初始化右侧参数列表
			$.ajax({
        		async:false,
    			data : {product:product,jobId:jobId},
    			url : 'template/getJob',
    			type : 'GET',
    			contentType:'application/json',
    			success: function(data){
    				if(data.success){
    					if (data.data.job) {
    						var runJobInfo = data.data.job;
    						self.initializeTable("#tab_job_template_run_properties_history",runJobInfo.jobParamterIsMust,runJobInfo.properties,"/runPropertiesHistroy","/updateCellRunPropertiesHistroy","属性","值","是否必填");
    					}
    				}else{
    					var msgInfo = '复制新增失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
	                	alertor.dangerAlert(msgInfo,'middle');
    				}
    			},
    			error: function(msg){
    				alertor.dangerAlert('连接服务器错误','small');
    			},
    			beforeSend : function(){
    				mloadding.showLoadding();
    			},
    			complete : function(){
    				mloadding.hideLoadding();
    			}
    		});
		
			// 新增临时数据
			self.addTempJobHistory("", jobId);
			// 初始化未选择任务项编号
			self.initTaskHistoryTableUnselect("unselected_task_table", "", jobId);
			// 初始化已选择任务项编号
			self.initTaskHistoryTableSelect("selected_task_table", "", jobId);
			
			//整体运行按钮事件
			$('#runJobTemplate').unbind('click').bind('click', function(event){
				if(!mloadding.showLoadding()){
            		return false;
            	}
        		var runProperties = $('#tab_job_template_run_properties_history').bootstrapTable("getData");
        		var flag = true;
            	var properties = {};//保存维度实体信息
            	$.each(runProperties,function(i){
            		var key = runProperties[i].key;
            		var value;
            		if(runProperties[i].value.length != 1){
            			value = runProperties[i].value;
            		} else {
            			value = runProperties[i].value[0];
            		}
            		if(key == undefined || key==null || key == "" || key.trim() == ""){
            			if(value != undefined && value != null && value != "" && value.trim() != ""){
	        				bootbox.alert({message:'第'+(i+1)+'行，属性不能为空',size:'small'});
	        				flag = false;
	        				return false;
            			}
        			}else{
        				if(value == undefined || value==null || value == "" || value.trim() == ""){
            				//if(runProperties[i].isMust){
                				bootbox.alert({message:'第'+(i+1)+'行，值不能为空',size:'small'});
                				flag = false;
                				return false;
                			//}
                		}
        				properties[key] = value;
        			}
            	});	
            	// 保存调试信息
            	var tDebug = $("input:radio[name=debug_history]:checked").val();
            	if(tDebug != ""){
            		properties.debug = tDebug;
            	}
            	var params = JSON.stringify(properties);
            	if(!flag){
            		mloadding.hideLoadding();
            		return;
            	}
            	$.ajax({
					url:'template/runJobTemplate',
		            type:"GET",
		            async:false,
		            data:{product:product,jobId:jobId,params:params},
		            contentType:'application/json',
		            success:function (data) {
		                if (data.success){
		                	bootbox.alert({message:'作业运行中<br>作业实例编号：' + data.data.jobInsId ,size:'small'});    		             	     
		                }else{
		                	var msgInfo = '运行失败,服务器处理出错<br>' + data.info;
	    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
	    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
	    					}
		                	alertor.dangerAlert(msgInfo,'middle');
		                }
		                $('#jobModal_template_run_history').modal('hide');
		                mloadding.hideLoadding();
		            },
		            error: function(XMLHttpRequest,textStatus,errorThrown){
		            	bootbox.alert({message:'运行失败'+errorThrown,size:'small'});  
		            	mloadding.hideLoadding();
		            }
		        });
			});
			
			//部分运行按钮事件
			$("#runJobTemplateHistory").unbind("click").bind("click", function() {
				if(!mloadding.showLoadding()){
            		return false;
            	}
				var runProperties = $('#tab_job_template_run_properties_history').bootstrapTable("getData");
				var flag = true;
				var paramOjb = {};
				$.each(runProperties,function(i){
            		var key = runProperties[i].key;
            		var value;
            		if(runProperties[i].value.length != 1){
            			value = runProperties[i].value;
            		} else {
            			value = runProperties[i].value[0];
            		}
            		if(key == undefined || key==null || key == "" || key.trim() == ""){
            			if(value != undefined && value != null && value != "" && value.trim() != ""){
	        				bootbox.alert({message:'第'+(i+1)+'行，属性不能为空',size:'small'});
	        				flag = false;
	        				return false;
            			}
        			}else{
        				if(value == undefined || value==null || value == "" || value.trim() == ""){
            				//if(runProperties[i].isMust){
                				bootbox.alert({message:'第'+(i+1)+'行，值不能为空',size:'small'});
                				flag = false;
                				return false;
                			//}
                		}
        				paramOjb[key] = value;
        			}
            	});
            	var debugHistoryValue = $("input:radio[name=debug_history]:checked").val();
            	if(debugHistoryValue) {
            		paramOjb.debug = debugHistoryValue;
            	}
            	var runTasks = $('#selected_task_table').bootstrapTable('getData');
            	if(runTasks.length < 1){
            		bootbox.alert({message:'操作失败，没有执行的task',size:'small'});
            		flag = false;
            	} 
            	if(!flag){
            		mloadding.hideLoadding();
            		return;
            	}
            	$.ajax({
					url:'template/runJobTemplateHistory',
		            type:"GET",
		            async:false,
		            data:{infoParameter:JSON.stringify(paramOjb)},
		            contentType:'application/json',
		            success:function (data) {
		                if (data.success){
		                	bootbox.alert({message:'作业运行中<br>作业实例编号：' + data.data.jobInsId ,size:'small'});           	     
		                }else{
		                	var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
	    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
	    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
	    					}
		                	alertor.dangerAlert(msgInfo,'middle');
		                }
		                $('#jobModal_template_run_history').modal('hide');
		                mloadding.hideLoadding();
		            },
		            error: function(XMLHttpRequest,textStatus,errorThrown){
		            	bootbox.alert({message:'运行失败'+errorThrown,size:'small'});    
		                $('#jobModal_template_run_history').modal('hide');
		                mloadding.hideLoadding();
		            }
		        });
			});
			
			// 新增运行task
			$("#addRunTask").unbind('click').bind('click', function() {
				self.addRunTask(product, jobId);
			});
			// 移除运行task
			$("#removeRunTask").unbind('click').bind('click', function() {
				self.removeRunTask();
			});
			// 过滤未选择任务项
			$("#filterUnselectTaskId").unbind('click').bind('click',function() {
				$('#unselected_task_table').bootstrapTable('refresh');
			});
			// 过滤选择任务项
			$("#filterselectTaskId").unbind('click').bind('click',function() {
				$('#selected_task_table').bootstrapTable('refresh');
			});
			
			$("#table_show_hide").hide();
			$("#btn_show_hide").text("展开任务项列表");
			$("#btn_show_hide").unbind('click').bind('click',function() {
				$("#table_show_hide").toggle();
				var display = $("#table_show_hide").css('display');
				if(display=="block") {
					$("#btn_show_hide").text("隐藏任务项列表");
				}else if(display=="none"){
					$("#btn_show_hide").text("展开任务项列表");
				}
			});
			
			$('#jobModal_template_run_history').modal('show');
		},
		initJobRunHistoryTable : function(product, jobId) {
        	var self = this;
        	$("#tab_job_template_run_history").bootstrapTable('destroy');
        	$("#tab_job_template_run_history").bootstrapTable({
        		theadClasses: 'thead-light',
                url: 'template/queryJobExeInfo',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
                striped: true,                      //是否显示行间隔色
                cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                pagination: false,                   //是否显示分页（*）
                noInfoPagination: true,
                sortable: false,                     //是否启用排序
                sortOrder: "asc",                   //排序方式
                queryParamsType:'undefined',
                queryParams: function (params) {
                   return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                	   		jobId: jobId,
                	   		product: product
                        };
                    },           //传递参数（*）
                sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                pageNumber:1,                       //初始化加载第一页，默认第一页
                pageSize: 10,                       //每页的记录行数（*）
                pageList: [10, 30, 50, 100],        //可供选择的每页的行数（*）
                minimumCountColumns: 2,             //最少允许的列数
                clickToSelect: true,                //是否启用点击选中行
                singleSelect: true,
                uniqueId: "infoId",                     //每一行的唯一标识，一般为主键列
                buttonsClass: 'sm btn-primary',
                columns: [
                	{title:'置顶', edit:false, align:'center',field:'top',width:'45px',
        				events:{
        					'click .job_history_top_style': function(e, value, row, index) {
        						if(value==0) {
        							self.topRecord(row.infoId, true);
        						}else if(value==1) {
        							self.topRecord(row.infoId, false);
        						}
        					}
        				},
        				formatter:function(value,row,rowIndex){  
        					if(value==1){
        						return '<button type="button" class="btn btn-xs btn-primary px-1 job_history_top_style">置顶</button>';
        					}else {
        						return '<button type="button" class="btn btn-xs btn-default px-1 job_history_top_style">置顶</button>';
        					}
        				}
        			},
        			{field: 'infoDesc', title: '描述',
        				formatter: function (value, row, index) {
        					var time = row.executeTime.substring(4,6)+"/"+
        							   row.executeTime.substring(6,8)+" "+
        							   row.executeTime.substring(8,10)+":"+
        							   row.executeTime.substring(10,12)+":"+
        							   row.executeTime.substring(12,14) + " ";
                            return time +"<a href=\"#\" data-name=\"historyDesc\" data-pk=\""+index+"\" data-title=\"描述\">" + (value ? value : "") + "</a>";
                        }
        			},
                	/*{filed:'', title:'选择', align: 'center', 
                		events:{
        					'click .selectOneHistory': function(event, value, row, index) {
        						$("input[name=unselectTaskId]").val("");
        						$("input[name=selectTaskId]").val("");
        						var parameterIsMust = {};
        	                	var properties = {};
        	                	$.ajax({
             		        		async:false,
             		    			data : {product:row.product,jobId:row.jobId},
             		    			url : 'template/getJob',
             		    			type : 'GET',
             		    			contentType:'application/json',
             		    			success: function(data){
             		    				if(data.success){
             		    					if (data.data.job) {
             		    						var runJobInfo = data.data.job;
             		    						if(runJobInfo.jobParamterIsMust) {
             		    							parameterIsMust = runJobInfo.jobParamterIsMust;
             		    						}
             		    						if(runJobInfo.properties) {
             		    							properties = runJobInfo.properties;
             		    						}
             		    					}
             		    				}else{
             		    					bootbox.alert('提交失败,服务器出错,' + data.info);
             		    				}
             		    			},
             		    			error: function(msg){
             		    				alertor.dangerAlert('连接服务器错误','small');
             		    			},
             		    			beforeSend : function(){
             		    				mloadding.showLoadding();
             		    			},
             		    			complete : function(){
             		    				mloadding.hideLoadding();
             		    			}
             		    		});
        	                	var selfcbk = $("input[type=checkbox][name=selectOneHistory"+index+"]")[0];
        	                	var checkArr = $("input[type=checkbox][name^=selectOneHistory]");
        	             		$.each(checkArr,function(i){ 
        	             			if(checkArr[i].name != selfcbk.name) {
        	             				$(checkArr[i]).prop("checked", false); 
        	             			}else {
        	             				if($(checkArr[i]).prop("checked")) {
        	             					self.addTempJobHistory(row.infoId, "");
        	             					self.initTaskHistoryTableUnselect("unselected_task_table", row.infoId, row.jobId);
        	             					self.initTaskHistoryTableSelect("selected_task_table", row.infoId, row.jobId);
        	             					var tProperties = JSON.parse(row.infoParameter);
        	             					$.each(tProperties,function(key,value){
        	             						properties[key] = value;
        	             					})
        	             				}else {
        	             					self.addTempJobHistory("", row.jobId);
        	             					self.initTaskHistoryTableUnselect("unselected_task_table", "", row.jobId);
        	             					self.initTaskHistoryTableSelect("selected_task_table", "", row.jobId);
        	             				}
        	             			}
        	             		});
        	        			if(properties && properties.debug) {
        	        				$("input:radio[name=debug_history][value="+properties.debug+"]").prop("checked",true);
        	        				delete properties.debug;
        	        			}else {
        	        				$("input:radio[name=debug_history]").eq(0).prop("checked", true);
        	        			}
        	        			self.initializeTable("#tab_job_template_run_properties_history",parameterIsMust,properties,"/runPropertiesHistroy","/updateCellRunPropertiesHistroy","属性","值","是否必填");
        					}
        				},
                		formatter: function (value, row, index){
                			return "<input type='checkbox'class='selectOneHistory' name='selectOneHistory"+index+"'/>";
                		}
                	}*/
        			{checkbox : true}
                ],
                responseHandler: function (res) {
                	return res.data;
                },
                onLoadSuccess: function (aa, bb, cc) {
                	$("#tab_job_template_run_history a").editable({
                		success : function() {
                			var self = this;
                			var allRows = $('#tab_job_template_run_history').bootstrapTable('getData');
                			var curRow = allRows[self.dataset.pk];
                			setTimeout(function(){
                				$.ajax({
                	        		async:false,
                	    			url : 'template/editJobExeInfo',
                	    			type : 'GET',
                	    			contentType:'application/json',
                	    			data:{infoId:curRow.infoId, desc:self.innerText},
                	    			success: function(data){
                	    				if(data.success){
                	    					
                	    				}else{
                	    					$('#tab_job_template_run_history').bootstrapTable('refresh');
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
                	        },100);
                		}
                	});
                },
            onCheck: function onCheck(row) {
            	self.clickHistoryTableRow(row, "check");
            	return false;
            }, 
            onUncheck: function onUncheck(row) {
            	self.clickHistoryTableRow(row, "uncheck");
            	return false;
            }
            });
        },
        initTaskHistoryTableSelect:function(tabId, infoId, jobId){
        	var self = this;
        	var tb_tleans_el = $('#' + tabId);
        	tb_tleans_el.bootstrapTable('destroy');
        	tb_tleans_el.bootstrapTable({
        		theadClasses: 'thead-light',
                url: 'template/getJobExeInfoSelect',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
                striped: true,                      //是否显示行间隔色
                cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                pagination: true,                   //是否显示分页（*）
                noInfoPagination: true,
                sortable: false,                     //是否启用排序
                sortOrder: "asc",                   //排序方式
                queryParamsType:'undefined',
                queryParams: function (params) {
                   return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                	   		pageNumber: params.pageNumber,   //页面大小
                	   		pageSize: params.pageSize,  //页码
                	   		infoId: infoId,
                	   		jobId : jobId,
                	   		filterTaskId : $("input[name=selectTaskId]").val()
                        };
                    },           //传递参数（*）
                sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                pageNumber:1,                       //初始化加载第一页，默认第一页
                pageSize: 10,                       //每页的记录行数（*）
                pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                minimumCountColumns: 2,             //最少允许的列数
                clickToSelect: true,                //是否启用点击选中行
                uniqueId: "taskId",                     //每一行的唯一标识，一般为主键列
                buttonsClass: 'sm btn-primary',
                columns: [
                	{checkbox: true}, 
                	{field: 'taskId', title: '已选择任务项编号'}
                ],
                responseHandler: function (res) {
                	return res.data;
                }
            });
        },
        initTaskHistoryTableUnselect:function(tabId, infoId, jobId){
        	var self = this;
        	var tb_tleans_el = $('#' + tabId);
        	tb_tleans_el.bootstrapTable('destroy');
        	tb_tleans_el.bootstrapTable({
        		theadClasses: 'thead-light',
                url: 'template/getJobExeInfoUnselect',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
                striped: true,                      //是否显示行间隔色
                cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                pagination: true,                   //是否显示分页（*）
                noInfoPagination: true,
                sortable: false,                     //是否启用排序
                sortOrder: "asc",                   //排序方式
                queryParamsType:'undefined',
                queryParams: function (params) {
                   return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                	   		pageNumber: params.pageNumber,   //页面大小
                	   		pageSize: params.pageSize,  //页码
                	   		infoId: infoId,
                	   		jobId : jobId,
                	   		filterTaskId : $("input[name=unselectTaskId]").val()
                        };
                    },           //传递参数（*）
                sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                pageNumber:1,                       //初始化加载第一页，默认第一页
                pageSize: 10,                       //每页的记录行数（*）
                pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                minimumCountColumns: 2,             //最少允许的列数
                clickToSelect: true,                //是否启用点击选中行
                uniqueId: "taskId",                     //每一行的唯一标识，一般为主键列
                buttonsClass: 'sm btn-primary',
                columns: [
                	{checkbox: true}, 
                	{field: 'taskId', title: '未选择任务项编号'}
                ],
                responseHandler: function (res) {
                	return res.data;
                }
            });
        },
        addTempJobHistory : function(infoId, jobId) {
        	$.ajax({
        		async:false,
    			url : 'template/addTempJobExeInfo',
    			type : 'GET',
    			contentType:'application/json',
    			data:{infoId:infoId},
    			success: function(data){
    				if(data.success){
    				
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
        addRunTask : function(product, jobId) {
        	var self = this;
        	var selected = $('#unselected_task_table').bootstrapTable('getSelections');
        	if(selected.length < 1){
        		bootbox.alert({message:'请选中至少一个任务项编号',size:'small'});
        		return;
        	} 
        	var taskIds = [];
        	for(var i=0, len=selected.length; i<len; i++) {
        		taskIds.push(selected[i].taskId);
        	}
        	$.ajax({
        		async:false,
    			url : 'template/addJobExeDetail',
    			type : 'GET',
    			contentType:'application/json',
    			data:{product:product, jobId:jobId, taskIds:taskIds.join(",")},
    			success: function(data){
    				if(data.success){
    					self.initTaskHistoryTableUnselect("unselected_task_table", data.data.infoId, jobId);
     					self.initTaskHistoryTableSelect("selected_task_table", data.data.infoId, jobId);
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
        removeRunTask : function() {
        	var self = this;
        	var selected = $('#selected_task_table').bootstrapTable('getSelections');
        	if(selected.length < 1){
        		bootbox.alert({message:'请选中至少一个任务项编号',size:'small'});
        		return;
        	}
        	var taskIds = [];
        	for(var i=0, len=selected.length; i<len; i++) {
        		taskIds.push(selected[i].taskId);
        	}
        	$.ajax({
        		async:false,
    			url : 'template/removeJobExeDetail',
    			type : 'GET',
    			contentType:'application/json',
    			data:{taskIds:taskIds.join(",")},
    			success: function(data){
    				if(data.success){
    					self.initTaskHistoryTableUnselect("unselected_task_table", data.data.infoId, jobId);
     					self.initTaskHistoryTableSelect("selected_task_table", data.data.infoId, jobId);
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
        topRecord : function(infoId, flag) {
        	$.ajax({
        		async:false,
    			url : 'template/topRecord',
    			type : 'GET',
    			contentType:'application/json',
    			data:{infoId:infoId, flag:flag},
    			success: function(data){
    				if(data.success){
    					$("#tab_job_template_run_history").bootstrapTable('refresh');
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
        clickHistoryTableRow : function(row, type) {
        	var self = this;
        	$("input[name=unselectTaskId]").val("");
			$("input[name=selectTaskId]").val("");
			var parameterIsMust = {};
        	var properties = {};
        	$.ajax({
        		async:false,
    			data : {product:row.product,jobId:row.jobId},
    			url : 'template/getJob',
    			type : 'GET',
    			contentType:'application/json',
    			success: function(data){
    				if(data.success){
    					if (data.data.job) {
    						var runJobInfo = data.data.job;
    						if(runJobInfo.jobParamterIsMust) {
    							parameterIsMust = runJobInfo.jobParamterIsMust;
    						}
    						if(runJobInfo.properties) {
    							properties = runJobInfo.properties;
    						}
    					}
    				}else{
    					var msgInfo = '复制新增失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
	                	alertor.dangerAlert(msgInfo,'middle');
    				}
    			},
    			error: function(msg){
    				alertor.dangerAlert('连接服务器错误','small');
    			},
    			beforeSend : function(){
    				mloadding.showLoadding();
    			},
    			complete : function(){
    				mloadding.hideLoadding();
    			}
    		});
        	if(type=="check") {
        		self.addTempJobHistory(row.infoId, "");
				self.initTaskHistoryTableUnselect("unselected_task_table", row.infoId, row.jobId);
				self.initTaskHistoryTableSelect("selected_task_table", row.infoId, row.jobId);
				var tProperties = JSON.parse(row.infoParameter);
				$.each(tProperties,function(key,value){
					properties[key] = value;
				});
        	}else {
        		self.addTempJobHistory("", row.jobId);
				self.initTaskHistoryTableUnselect("unselected_task_table", "", row.jobId);
				self.initTaskHistoryTableSelect("selected_task_table", "", row.jobId);
        	}
			if(properties && properties.debug) {
				$("input:radio[name=debug_history][value="+properties.debug+"]").prop("checked",true);
				delete properties.debug;
			}else {
				$("input:radio[name=debug_history]").eq(0).prop("checked", true);
			}
			self.initializeTable("#tab_job_template_run_properties_history",parameterIsMust,properties,"/runPropertiesHistroy","/updateCellRunPropertiesHistroy","属性","值","是否必填");
        },
        expotJobTemplate : function(){
        	if(!mloadding.showLoadding()){
        		return false;
        	}
        	var submitFlag = true;
        	var qProduct = "";
        	var qJobId = "";
        	var qMode = 0;
        	var jobIds = "";
        	var selected = $('#tb_job').bootstrapTable('getSelections');
        	if(selected.length > 0){
        		var jobIdArr = [];
        		_.each(selected, function(element, index){
        			jobIdArr.push(element.jobId);
        		});
        		jobIds = jobIdArr.join(',');
        	}else{
        		qProduct = $("#nav_jtpl_content_list").find("select[name=product]").val();
        		qJobId = $("#nav_jtpl_content_list").find("input[name=jobId]").val();
        		qMode = $("#nav_jtpl_content_list").find("select[name=mode]").val();
        	}
        	var exportFileType = $("#exportJobTemplateModal").find("select[name=exportFileType]").val();
        	var exportFileName = $("#exportJobTemplateModal").find("input[name=exportFileName]").val();
        	if(null == exportFileName || exportFileName == ""){
    			bootbox.alert({message:'操作失败，导出文件名称不能为空',size:'small'}); 
    			submitFlag = false;
    		}
        	if(!submitFlag){
            	mloadding.hideLoadding();
            	return submitFlag;
            }
        	var exportFileCatalogArr = [];
        	$.each($("#exportFileCatalog").find("button[class='btn btn-sm btn-primary mb-1 mr-1']"),function(index,but){
        		exportFileCatalogArr.push($(but).val());
        	});
        	var tConfirm = true;
    		bootbox.confirm({
        		size : 'small',
        		message : '是否确定导出任务模板？',
        		callback : function (result) {
            		if(result && tConfirm){
        				tConfirm = false;
            			window.location.href = "template/expotJobTemplate?exportFileName="+encodeURIComponent(encodeURIComponent(exportFileName))+"&jobIds="+jobIds+"&exportFileType="+exportFileType+"&exportFileCatalog="+exportFileCatalogArr.join(',')+"&qProduct="+qProduct+"&qJobId="+qJobId+"&qMode="+qMode;
            		}
            	 }
        	});
    		mloadding.hideLoadding();
        },
        importJobTemplate : function(){
        	if(!mloadding.showLoadding()){
        		return false;
        	}
        	var submitFlag = true;
        	var tValue = $("#jobTemplateImportForm").find("input[name='uFile']").val();
        	if(null == tValue || tValue == ""){
        		bootbox.alert({message:'操作失败，上传文件不能为空',size:'small'});
        		submitFlag = false;
        	}
        	var typeArr = ["xls", "xlsx", "sql"];
        	tValue = tValue.substring(tValue.lastIndexOf(".")+1);
        	if(submitFlag && $.inArray(tValue.toLowerCase(), typeArr) == -1){
        		bootbox.alert({message:'操作失败<br>上传文件格式错误(支持上传文件格式：xls、xlsx、sql)',size:'small'});
        		submitFlag = false;
        	}
        	var uploadFileMaxSize = -1;
        	var uploadFileMaxSizeMB = "";
        	$.ajax({
      			async : false,
    			data : {},
    			url : 'template/getUploadFileMaxSize',
    			type : 'GET',
    			contentType:'application/x-www-form-urlencoded',
    			success: function(data){
    				if(data.success){
    					uploadFileMaxSize = data.data.uploadFileMaxSize;
    					uploadFileMaxSizeMB = data.data.uploadFileMaxSizeMB;
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
        	if(uploadFileMaxSize == -1){
        		submitFlag = false;
        	}
        	var fileSize = $("#jobTemplateImportForm").find("input[name=uFile]")[0].files[0].size;
        	if(submitFlag && fileSize > uploadFileMaxSize){
        		bootbox.alert({message:'操作失败<br>上传文件大小不能超过'+uploadFileMaxSizeMB,size:'small'});
        		submitFlag = false;
        	}
        	if(!submitFlag){
            	mloadding.hideLoadding();
            	return submitFlag;
            }
        	$.ajax({
        		type:'POST',
        		url:'template/importJobTemplate',
        		cache:false,
        		processData:false,
        		contentType:false,
        		async:true,
        		data:new FormData($("#jobTemplateImportForm")[0]),
        		dataType:'json',
        		success:function(data){
        			if(data.success){
        				$("#importJobTemplateModal").modal("hide");
        				bootbox.alert({message:'上传成功',size:'small'});
        				$('#tb_job').bootstrapTable('refresh');
        			}else{
        				var msgInfo = '上传操作失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
	                	alertor.dangerAlert(msgInfo,'middle');
        			}
        			mloadding.hideLoadding();
        		},
        		error:function(){
        			mloadding.hideLoadding();
        			alertor.dangerAlert('连接服务器错误','small');
        		}
        		
        	});
        }
	});

	return app;
});