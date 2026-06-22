define([
		'underScore', 
		'text!application/wws_job_template_task_list.html',
		'mloadding', 
		'ko',
		'bootstrap-table', 
		'bootstrap-switch',
		'bootstrap-typeahead',
		'bootstrap-tagsinput',
		'x-editable', 'mockjax', 'icheck', 'spinner' ], function(_, template, mloadding, ko) {
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
            
            self.initTaskTable(self.jrow);
            self.initPluginSelcet();
		},
        initTaskTable : function(row) {
        	var self = this;
        	var taskTableEl = $('#' + self.elId + '_tb_task_template');
        	var queryCodTaskIdEl = $('#' + self.elId + ' input[name=taskId]');
        	//重新绑定事件
        	$('#' + self.elId + '_btn_task_add').unbind('click');
    		$('#' + self.elId + '_btn_task_delete').unbind('click');
    		$('#' + self.elId + '_btn_task_query').unbind('click');
    		$('#' + self.elId + '_btn_task_clear').unbind('click');
    		$('#' + self.elId + '_btn_task_lean_setting').unbind('click');
    		//销毁已存在
    		taskTableEl.bootstrapTable("destroy");
        	taskTableEl.bootstrapTable({
        		theadClasses: 'thead-light',
                url: 'task/list',         //请求后台的URL（*）
                method: 'get',                      //请求方式（*）
                toolbar: '#' + self.elId + '_tb_task_template_toolbar',                //工具按钮用哪个容器
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
            	   		jobId : row.jobId,
            	   		taskId : queryCodTaskIdEl.val()
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
                showFullscreen: true,
                showRefresh: true,
                columns: [
                	{checkbox: true}, 
                	{field: 'taskId', title: '任务项编号'},
                	{field: 'name', title: '任务项名称'},
                	{field: 'title', title: '标签'},
                	{field: 'plugin', title: '插件'},
                	{field: 'programName', title: '程序'},
                	{field: 'period', title: '可执行窗口时间'},
                	{field: 'workingDay', title: '工作日设定'},
                	{field: 'errorDelay', title: '错误延迟时间'},
                	{field: 'errorIgnore', title: '是否忽略错误通过',
                		formatter:function(value, row, index){
            				return value==1? "是":"否";
                		}
                	},
                	{field: 'maxNumOfExeErrors', title: '最大错误执行次数'},
                	{field: 'useJobDimension', title: '维度方案模板编号'},
                	{field: 'agentScope', title: '可执行节点范围'},
                	{field: 'createTime', title: '创建时间'},
                	{field: 'creator', title: '创建者'}
        		],
                responseHandler: function (res) {return res.data;},
                onDblClickRow: function (taskRow) {
                	self.jmodule.taskEdit(self.jrow, taskRow.taskId, taskRow.plugin, true);
                }
            });
        	
        	$('#' + self.elId + '_btn_task_add').bind('click', function(event){ 
        		self.jmodule.taskEdit(self.jrow ,null, null, false);
            });
        	$('#' + self.elId + '_btn_task_delete').bind('click', function(event){ 
            	self.jmodule.deleteTask(self.jrow, taskTableEl);
            });
        	$('#' + self.elId + '_btn_task_lean_setting').bind('click', function(event){ 
            	self.jmodule.showTaskLeanSettingModal(taskTableEl);
            });
            $('#' + self.elId + '_btn_task_query').bind('click', function(event){ 
            	taskTableEl.bootstrapTable('refresh');
            });
            $('#' + self.elId + '_btn_task_clear').bind('click', function(event){
            	queryCodTaskIdEl.val("");
            });
        },
        initPluginSelcet:function(){
        	var self = this;
        	$.ajax({
        		async:false,
        		data : {product:'wws'},
        		url: 'template/getPluginByProduct',
    			type : 'GET',
    			contentType:'application/x-www-form-urlencoded',
    	        success:function (data) {
    	        	if(data.success){
    	        		var plugins = data.data.plugins;
    	        		var selPlu = $('#' + self.elId + '_taskAddModal select[name=plugin]');
    	        		selPlu.empty();
    	        		for(var i=0;i<plugins.length;i++){
    	        			selPlu.append("<option value='" + plugins[i].name + "'>" + plugins[i].name + "</option>");
    	        		}
    	        	} else {
    	        		var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
	                	bootbox.alert({message:msgInfo, size:'middle'});
    	        	}
    	       },
    	       error: function (e) {
    	    	   bootbox.alert({message:'获取插件失败' + e.status,size:'small'});
    	       }  	
    		})
        },
        taskEdit : function(product, taskId, plugin, addnew){
        	if(!plugin || plugin == null || plugin == ''){
      		  return;
      	  	}
        	//编辑task信息
        	var self = this;
        	var modalEl = $('#'+self.elId+'_taskEditModal');
        	// init select useJobDefDimension
        	var sel_ujdds = modalEl.find("select[name=useJobDefDimension]");
        	sel_ujdds.empty();
        	sel_ujdds.append("<option value=0>0</option>");
        	$.ajax({
				async:false,
				data : {product:product,jobId:self.jrow.jobId},
				url : 'template/getJob',
				type : 'GET',
				contentType:'application/json',
				success: function(data){
					if(data.success){
						if (data.data.job) {
							var defaultDimensions = data.data.job.defaultDimensions;
							$.each(defaultDimensions,function(key,value){
								sel_ujdds.append("<option value='" + key + "'>" + key + "</option>");
							})
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
        	var taskObj;
        	var pluginObject = {};
        	var pluginObjectIsMust = {};
			$.ajax({
				async:false,
				url: 'template/getTask',
				type:"GET",
				contentType:'application/x-www-form-urlencoded',
				data:{product:product,jobId:self.jrow.jobId,taskId:taskId},
				success:function (data) {
					if(data.success){
						taskObj = data.data.task;
					}else{
						var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
	                	bootbox.alert({message:msgInfo, size:'middle'});
					}
				},
				error: function (e) {
					bootbox.alert({message:'获取任务项失败',size:'small'});
		        }
			});
			var dmsns = {};
			self.selectOnchangePluginName(plugin,pluginObject,pluginObjectIsMust);
			if(addnew && taskObj != undefined){
				bootbox.alert({message:'任务项编号命名重复',size:'small'});
				return;
			}
			var task_tagsEl = modalEl.find('input[name=task_tags]');
			task_tagsEl.tagsinput({style:'min-width:90%'});
			if(taskObj == undefined || taskObj == ""){
				task_tagsEl.tagsinput("removeAll");
				modalEl.find("label[name=product]")[0].innerHTML = product;
				modalEl.find("label[name=taskId]")[0].innerHTML = taskId;
				modalEl.find("label[name=taskPlugin]")[0].innerHTML = plugin;
				modalEl.find("input[name=programName]").val(plugin);
				modalEl.find("label[name=jobId]")[0].innerHTML = jobId;
				modalEl.find("input[name=taskName]").val("");
				//modalEl.find("input[name=nodeType]").val("1");
				//modalEl.find("select[name=isEnable]").val("1");
				//modalEl.find("input[name=cycle]").val("0");
				modalEl.find("select[name=useJobDefDimension]").val("0");		
				modalEl.find("input[name=errorMaxCount]").val("1");
				modalEl.find("input[name=period]").val("");
				modalEl.find("input[name=workingDay]").val("");
				modalEl.find("input[name=agentScope]").val("");
				modalEl.find("select[name=errorIgnore]").val("0");
				modalEl.find("input[name=errorDelay]").val("0");
				modalEl.find("input[name=creator]").val(job.creator);
				modalEl.find("input[name=createTime]").val('');
				self.initializeTable('#'+self.elId+'_tab_tt_properties',pluginObjectIsMust,pluginObject,"/"+self.elId+"_taskProperties","/"+self.elId+"_updateTaskCellProperties","属性","值","是否必填");
			} else {
				dmsns = taskObj.dimensions;
				if(taskObj.title != undefined){
					var tags = taskObj.title.split(",");
					task_tagsEl.tagsinput("removeAll");
					$.each(tags,function(i){ task_tagsEl.tagsinput("add",tags[i]); }) 
				} else {
					task_tagsEl.tagsinput("removeAll");
				}
				modalEl.find("label[name=jobId]")[0].innerHTML = taskObj.jobId;
				modalEl.find("label[name=product]")[0].innerHTML = product;
				modalEl.find("label[name=taskId]")[0].innerHTML = taskObj.taskId;
				modalEl.find("input[name=taskName]").val(taskObj.name);
				//modalEl.find("input[name=nodeType]").val(taskObj.nodeType);
				/*if(taskObj.enabled){
					modalEl.find("select[name=isEnable]").val(1);
				} else {
					modalEl.find("select[name=isEnable]").val(0);
				}*/
				//modalEl.find("input[name=cycle]").val(taskObj.cycle);
				modalEl.find("label[name=taskPlugin]")[0].innerHTML = taskObj.plugin;
				modalEl.find("input[name=programName]").val(taskObj.programName);
				modalEl.find("select[name=useJobDefDimension]").val(taskObj.useJobDimension);		
				modalEl.find("input[name=errorMaxCount]").val(taskObj.maxNumOfExeErrors);
				modalEl.find("select[name=errorIgnore]").val(taskObj.errorIgnore);
				modalEl.find("input[name=errorDelay]").val(taskObj.errorDelay);
				modalEl.find("input[name=creator]").val(taskObj.creator);
				modalEl.find("input[name=period]").val(taskObj.period);
				modalEl.find("input[name=workingDay]").val(taskObj.workingDay);
				modalEl.find("input[name=agentScope]").val(taskObj.agentScope);
				modalEl.find("input[name=createTime]").val(taskObj.createTime);
				self.initializeTable('#'+self.elId+'_tab_tt_properties',pluginObjectIsMust,taskObj.parameters,"/"+self.elId+"_taskProperties","/"+self.elId+"_updateTaskCellProperties","属性","值","是否必填");
			}
			modalEl.modal('show');
			var a2 = {};
			var a3 = {};
			$.each(dmsns,function(key,value){
					$.each(value,function(i){
						if(value[i].substring(0,4) == "TAG:"){
							if(a2[key] == undefined){
								a2[key] = [];
							}
							a2[key].push(value[i].substring(4));
						} else {
							if(a3[key] == undefined){
								a3[key] = [];
							}
							a3[key].push(value[i]);
						}
					});
			});
			if(self.jrow.mode == 2){
				self.initializeTableSelect('#'+self.elId+'_tab_tdmsn_title',a2,"/"+self.elId+"_taskDimensionTitles","/"+self.elId+"_updateTaskCellDimensionTitle","维度","标签","taskDimensionTitle","template/getTitleByProductAndDimension","&dimensionName=","title");
				self.initializeTableSelect('#'+self.elId+'_tab_tdmsn_entity',a3,"/"+self.elId+"_taskDimensionEntities","/"+self.elId+"_updateTaskCellDimensionEntity","维度","实体","taskDimensionEntity","template/getDimensionEntityByProductAndDimension","&dimensionName=","entity");
				$('#'+self.elId+'_tr_dmsn_info').css("display","");
			} else {
				$('#'+self.elId+'_tab_tdmsn_title').bootstrapTable('destroy');
				$('#'+self.elId+'_tab_tdmsn_entity').bootstrapTable('destroy');
				$('#'+self.elId+'_tr_dmsn_info').css("display","none");
			}
		},
		selectOnchangePluginName : function (pluginName,pluginObject,pluginObjectIsMust){
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
   	   	        	}else{
	   	   	        	var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
						if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
							msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
						}
	                	bootbox.alert({message:msgInfo, size:'middle'});
   	   	        	}
   	   	        },
   	   	        error: function (e) {
   	   	        	bootbox.alert({message:'系统错误',size:'small'});
   	   	        }
   	   	    });
   		},
		//保存Task信息
        saveTaskinfo: function () {
        	var self = this;
        	var modalEl = $('#'+self.elId+'_taskEditModal');
        	var isSave = true;
        	var formJson = {};
        	var useTaskId = modalEl.find("label[name=taskId]")[0].innerHTML;
        	taskId = modalEl.find("label[name=taskId]")[0].innerHTML;
        	var reg2 = /^[0-9a-zA-Z_]+$/;
    		if(!reg2.test(taskId)){
    			bootbox.alert({message:'任务项编号只能为数字、字母或者下划线',size:'small'});
    			return;
    		}
    		formJson.taskId = modalEl.find("label[name=product]")[0].innerHTML + "#" +modalEl.find("label[name=taskId]")[0].innerHTML;
    		formJson.name = modalEl.find("input[name=taskName]").val();
    		if(null == formJson.name || formJson.name == ""){
    			bootbox.alert({message:'任务项名称不能为空',size:'small'});
    			return;
    		}
    		formJson.jobId = modalEl.find("label[name=jobId]")[0].innerHTML;
    		/*formJson.nodeType = modalEl.find("input[name=nodeType]").val();
    		if(isNaN(formJson.nodeType)){
    			bootbox.alert({message:'节点类型必须为数字类型',size:'small'});
    			return;
    		}*/
    		formJson.title = modalEl.find("input[name=task_tags]").val();
    		//formJson.enabled = modalEl.find("select[name=isEnable]").val();
    		/*formJson.cycle = modalEl.find("input[name=cycle]").val();
    		if(isNaN(formJson.cycle)){
    			bootbox.alert({message:'循环次数必须为数字类型',size:'small'});
    			return;
    		}*/
    		formJson.useJobDimension = modalEl.find("select[name=useJobDefDimension]").val();
    		if(isNaN(formJson.useJobDimension)){
    			bootbox.alert({message:'任务默认模板必须为数字类型',size:'small'});
    			return;
    		}
    		formJson.plugin = modalEl.find("label[name=taskPlugin]")[0].innerHTML;
    		formJson.programName = modalEl.find("input[name=programName]").val();
    		/*if(modalEl.find("select[name=useJobDefDimension]").val() == 0){
    			formJson.useDefaultJobDimension = true;	
    		} else {
    			formJson.useDefaultJobDimension = false;	
    		}*/
    		formJson.maxNumOfExeErrors = modalEl.find("input[name=errorMaxCount]").val();
    		formJson.agentScope = modalEl.find("input[name=agentScope]").val();
    		formJson.period = modalEl.find("input[name=period]").val();
    		formJson.workingDay = modalEl.find("input[name=workingDay]").val();
    		if(isNaN(formJson.maxNumOfExeErrors)){
    			bootbox.alert({message:'错误最大次数必须为数字类型',size:'small'});
    			return;
    		}
    		formJson.errorIgnore = modalEl.find("select[name=errorIgnore]").val();
    		formJson.errorDelay = modalEl.find("input[name=errorDelay]").val();
    		if(isNaN(formJson.errorDelay)){
    			bootbox.alert({message:'错误延时时间必须为数字类型',size:'small'});
    			return;
    		}
    		formJson.creator = modalEl.find("input[name=creator]").val();
    		if(self.jrow.mode == 2){
    			var entitiesObj = $('#'+self.elId+'_tab_tdmsn_entity').bootstrapTable("getData");
            	var dimensionsJson = {};//保存维度实体信息
            	$.each(entitiesObj,function(i){
            		var dimension = entitiesObj[i].key;
            		if(dimension == ""){
            			return;
            		}
            		var dimensionEntity;
            		if(entitiesObj[i].value.length != 1){
            			dimensionEntity = entitiesObj[i].value;
            		} else {
            			dimensionEntity = entitiesObj[i].value[0];
            		}
            		if(!(null == dimension || dimension == "")){
            			if(null == dimensionEntity || dimensionEntity == ""){
        					bootbox.alert({message:'实体添加维度中实体不能为空',size:'small'});
        					isSave = false;
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
            	var titlesObj = $('#'+self.elId+'_tab_tdmsn_title').bootstrapTable("getData");
            	$.each(titlesObj,function(i){
            		var dimension = titlesObj[i].key;
            		if(dimension == ""){
            			return;
            		}
            		if(titlesObj[i].value.length != 1){
            			dimensionTitle = titlesObj[i].value;
            		} else {
            			dimensionTitle = titlesObj[i].value[0];
            		}
            		if(!(null == dimension || dimension == "")){
            			if(null == dimensionTitle || dimensionTitle == ""){
        					bootbox.alert({message:'实体添加维度中标签不能为空',size:'small'});
        					isSave = false;
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
            	formJson.dimensions = dimensionsJson;
    		}
        	var propertiesObj = $('#'+self.elId+'_tab_tt_properties').bootstrapTable("getData");
        	var parametersJson = {};//保存任务参数信息
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
        				bootbox.alert({message:taskPropertyKey + '是必填参数，不能为空',size:'small'});
        				isSave = false;
        				return false;
        			}
        		}
				parametersJson[taskPropertyKey] = taskPropertyValue;
        	});
        	formJson.parameters = parametersJson;
        	if(!isSave){
        		return;
        	}
        	if(!mloadding.showLoadding()){
        		return false;
        	}
			$.ajax({
				url:'template/saveTasks',
	            type:"POST",
	            processData:false,
	            data:JSON.stringify(formJson),
	            contentType:'application/json',
	            success:function (data) {
	                if (data.success){
	                	bootbox.alert({message:'保存任务项信息成功',size:'small'});
	                	firstAddTask = false;
	                	modalEl.modal('hide');
	                	$("#btn_task_query").click();
	                }else{
	                	var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
	                	bootbox.alert({message:msgInfo, size:'middle'});
	                }
	                mloadding.hideLoadding();
	            },
	            error: function(XMLHttpRequest,textStatus,errorThrown){
	            	bootbox.alert({message:'保存失败'+errorThrown,size:'small'});    
	            	mloadding.hideLoadding();
	            }
	        });
        }, 
        showAddTaskModal : function() {
        	var self = this;
        	var modalEl = $('#' + self.elId + '_taskAddModal');
        	modalEl.find("input[name=taskId]").val("");
        	modalEl.find("select[name=plugin]").val("");
        	modalEl.modal("show");
        },
        addTask : function() {
        	var self = this;
        	var modalEl = $('#' + self.elId + '_taskAddModal');
        	
        	var plugin = modalEl.find("select[name=plugin]").val();
        	if(plugin==null || plugin=="") {
    			bootbox.alert({message:'请选择插件',size:'small'});
    			return;
    		}
        	var taskId = modalEl.find("input[name=taskId]").val();
        	var regx = /^[0-9a-zA-Z_]+$/;
    		if(!regx.test(taskId)){
    			bootbox.alert({message:'任务项编号只能为数字、字母或者下划线',size:'small'});
    			return;
    		}
    		
    		modalEl.modal("hide");
        	self.taskEdit(self.jrow.product, taskId, plugin);
        	
        	$('#saveTaskinfo').unbind('click').bind('click', function(event){
            	self.saveTaskinfo();
            });
        },
        delTask : function(jobId) {
        	var selected = $('#tb_tasks_instance').bootstrapTable('getSelections');
        	if(selected.length > 0){
        		var tasks = [];
        		_.each(selected, function(element, index){
        			tasks.push(element.taskId);
        		});
        		
        		var tConfirm = true;
        		bootbox.confirm({
            		size : 'small',
            		message : '是否确定删除所选任务项吗？',
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			url : 'template/deltasks?product='+product+'&jobId='+jobId+'&taskIds='+tasks.toString(),
                    			type : 'DELETE',
                    			contentType:'application/json',
                    			success: function(data){
                    				if(data.success){
                    					bootbox.alert({message:'删除成功',size:'small'});
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
        	} else{
        		bootbox.alert({message:'请选择需要删除的任务项',size:'small'});
        	}
        },
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
        		url: useUrl,
                striped: true,  
                clickToSelect: false,  
                pagination: false,
                editable: true,
                columns: [  
                    {field:'key',title:columnKey,align:'center',width:'20%',                    	  
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\"  class='editable editable-click'  data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                        }},  
                    {field:'value',title:columnValue,align:'center',width:'30%',
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\"  class='editable editable-click'  data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                        }},
                    {field:'isMust',align:'center',width:'20%',title:isMust,
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
                    {title:'操作', align:"center", edit:false,width:'30%',
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
        		url: useUrl,
                striped: true,  
                clickToSelect: false,  
                pagination: false,
                editable: true,
                columns: [  
                    {field:'key',title:columnKey,align:'center',width:'20%',                    	  
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\"  class='editable editable-click'  data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                        }},  
                    {field:'value',title:columnValue,align:'center',width:'30%',
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\"  class='editable editable-click'  data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                        }},
                    {field:'isMust',align:'center',width:'20%',title:isMust,
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
                    {title:'操作', align:"center", edit:false,width:'30%',
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
                	                	bootbox.alert({message:msgInfo, size:'middle'});
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
        		url: useUrl,
                striped: true,  
                clickToSelect: false,  
                pagination: false,
                editable: true,
                columns: [
                    {field:'key',title:columnKey,align:'right',width:'30%',                    	  
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\" onclick='searchEntityOrTitle(" + index + ",\"" + useId + "\",\"" + columnId  +"\",\"" + useChildUrl + "\",\"" + "&dimensionName=" + "\",\"" + columnUrl + "\",\"" +name + "\")' class='myuse editable editable-click'  data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                        }},  
                    {field:'value',title:columnValue,align:'right',width:'40%',
                    	formatter: function (value, row, index) {
                    		var usecolumnId = columnId + index;
                            return "<a href=\"#\" id=" + usecolumnId + " data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                        }},
                    {title:'操作', align:"center", edit:false,width:'30%',
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
    		console.log(11);
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
                					var entities = data.data.entities;
                					entityResult = [];
                					$.each(entities,function(i){
                						if(name == 'entity'){
                							entityResult.push({value:entities[i].entity,text:entities[i].entity});
                						} else if(name == 'title') {
                							entityResult.push({value:entities[i],text:entities[i]});
                						}
                					})	
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
        		url: useUrl,
                striped: true,  
                clickToSelect: false,  
                pagination: false,
                editable: true,
                columns: [
					{field:'modelNo',title:"编号",align:'right',width:'30%',                    	  
					    formatter: function (value, row, index) {
					        return "<a href=\"#\" class='myuseInput editable editable-click'  data-name=\"modelNo\" data-pk=\""+index+"\" data-title='编号'>" + value + "</a>";
					     }},
                    {field:'key',title:columnKey,align:'right',width:'30%',                    	  
                    	formatter: function (value, row, index) {
                            return "<a href=\"#\" class='myuse editable editable-click'  data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                        }},  
                    {field:'value',title:columnValue,align:'right',width:'40%',
                    	formatter: function (value, row, index) {
                    		var usecolumnId = columnId + index;
                            return "<a href=\"#\" id=" + usecolumnId + " data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                        }},
                    {title:'操作', align:"center", edit:false,width:'30%',
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
                }
        	});
    	}
	});

	return app;
});