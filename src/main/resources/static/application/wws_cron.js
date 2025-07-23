define([
	'underScore', 
	'text!application/wws_cron.html', 
	'text!application/wws_cron_expression_modal.html', 
	'expression',
	'mloadding',
	'cronjobtemtaskjobinstask-editable2',
	'bootstrap-table', 
	'bootstrap-table-locale',
	'mockjax',
	'x-editable', 
	'icheck',
	'spinner',
	'css!css/jquery.json-viewer.css',
	'jquery-jsoneditor', 
	'jquery-jsonviewer'],
    function (_, template, expressionModal, exp, mloadding, ceditable) {
        var app = function () {
            this.o_template = $(template);
            this.o_expressionModal = $(expressionModal);
        }
        var fal_agent_kid = "<i class='fal fa-key fa-fw' style='margin-right:2px'></i>";
        var fal_agent_tag = "<i class='fal fa-tag fa-fw' style='margin-right:2px'></i>";
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
            initCronTable: function(){
            	$('#tb_cron_keys').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'cron/cronspage',         
                    method: 'get',                     
                    toolbar: '#cj_tab_toolbar',               
                    striped: true,                      
                    cache: false,                      
                    pagination: true,                  
                    sortable: false,                    
                    sortOrder: "asc",               
                    queryParamsType:'undefined',
                    queryParams: function (params) {
                       return {   
                    	   		pageNumber: params.pageNumber,   
                    	   		pageSize: params.pageSize,  
                    	   		product: $("#cj_query_form").find("select[name=product]").val(),
//                    	   		agent: $("input[name='agent']").val(),
                    	   		agent: $("#cj_query_form").find("select[name=agent]").val(),
                    	   		jobId: $("input[name='jobId']").val()
                            };
                        },           
                    sidePagination: "server",           
                    pageNumber:1,                      
                    pageSize: 20,                    
                    pageList: [20, 30, 50, 100],       
                    minimumCountColumns: 2,             
                    clickToSelect: true,                
                    uniqueId: "jobId",                    
                    showToggle:false,                    
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                   
                    detailView: false,                  
                    showColumns: true,
                    showFullscreen: true,
                    showRefresh: true,
                    columns: [
                    	{checkbox: true}, 
                    	{field: 'product', title: '产品'},
                    	{field: 'jobId', title: '任务编号'},
                    	{field: 'jobName', title: '任务名称'},
                    	{field: 'agent', title: '节点',
                    		formatter: function (value, row, index) {
	                    		if (value != null && value != '') {
	                    			var text = '';
	                    			var options = value.split(",");
		    		        		$.each(options,function(i){
		    		        			if (options[i] != null && options[i] != '') {
		    		        				if (options[i].startsWith("#")) {
		    		        					text = text + "<span class='badge badge-secondary'>" + fal_agent_tag + options[i].substring(1,options[i].length) + "</span><br/>";
		    			        			} else {
		    			        				text = text + "<span class='badge badge-secondary'>" + fal_agent_kid + options[i] + "</span><br/>";
		    			        			}
		    		        			}
		    						})
		    						return text;
	                    		}
					        } 
                    	},
                    	{field: 'status', title: '状态',formatter: function (value, row, index) {
                    		if (value == 'STARTED'){
                    			value = "<span class='badge badge-success'>运行中</span>";
                    		} else if (value =='STARTED_SHOULDNOT'){
                    			value = "<span class='badge badge-warning'>运行中(*)</span>";
                    		} else if (value == 'ERROR'){
                    			value = "<span class='badge badge-danger'>错误</span>";
                    		} else if (value == 'NOTSTARTED'){
                    			value = "<span class='badge badge-secondary'>未运行</span>";
                    		} else if (value == 'UNKNOWN'){
                    			value = "<span class='badge badge-secondary'>未知</span>";
                    		}else{
                    			value = "<span class='badge badge-secondary'>未运行</span>";
                    		}
					        return "<a>" + value + "</a>";
					     }},
					    {field: 'runningon', title: '运行节点',
					    	 formatter: function (value, row, index) {
					    		var text = '';
	                    		if (value && value.length > 0) {
	                    			for (var i=0; i<value.length; i++){
	                    				text = text + "<span class='badge badge-success'>" + value[i] + "</span><br/>";
	                    			}
	                    		}
	                    		return text;
                        	}
				    	},
                    	{field: 'cronExpression', title: '计划任务执行表达式'},
                    	{field: 'plugin', title: '插件'},
                    	{field: 'parameter', title: 'parameter',visible:false},
                    	{field: 'shared', title: 'shared',visible:false}
                    	],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    onDblClickRow: function (row) {
                          app.prototype.operateCron(row);
                    }
                });
            },
            render: function (container) {
                var self = this;
                var o_container = $(container);
                o_container.empty();
                o_container.append(this.o_template);
                o_container.append(this.o_expressionModal);
                self.initProduct();
                self.initAgent();
                self.initCronTable();
                $('#btn_cron_query', this.o_container).bind('click', function(event){ $('#tb_cron_keys').bootstrapTable('refresh') });
                $('#btn_cron_clear', this.o_container).bind('click', function(event){
                	var cronQueryForm = $("#cj_query_form");
                	cronQueryForm.find("select[name=product]").val("");
                	cronQueryForm.find("select[name=agent]").val("");
                	cronQueryForm.find("input[name='jobId']").val('');
                	$('#tb_cron_keys').bootstrapTable('refresh');
                });
                $('#btn_add_cron', this.o_container).unbind('click').bind('click', function(event){self.operateCron(null)});
                $('#btn_edit_cron', this.o_container).unbind('click').bind('click', function(event){
                	var selected = $('#tb_cron_keys').bootstrapTable('getSelections');
                	if(selected.length != 1){
                		bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">请选择一个计划任务</div>',
                			size:'small',
                			closeButton: false
                		});
                		return false;
                	}
                	self.operateCron(selected[0]);
                });
                $('#btn_log_cron', this.o_container).unbind('click').bind('click', function(event){
                	var result = $('#tb_cron_keys').bootstrapTable('getSelections');
                    if(result.length == 1){
                    	self.showCronLogsInfoWindow(result[0]);
                    }else{
                    	bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">请选择一个计划任务</div>',
                			size:'small',
                			closeButton: false
                		});
                    }	
                	
                });
                $('#btn_delete_cron', this.o_container).bind('click', function(event){self.del(event)});
                $('#btn_start_cron', this.o_container).bind('click', function(event){self.start(event)});
                $('#btn_stop_cron', this.o_container).bind('click', function(event){self.stop(event)});
                $('#btn_restart_cron', this.o_container).bind('click', function(event){self.restart(event)});
                $('#btn_reset_cron', this.o_container).bind('click', function(event){
                  	$("#cj_query_form")[0].reset();
                });
                //self.initionCron();
                $('#expSet').unbind('click').bind('click', function(event){
                	exp.initExpression(this.id, this.value, exp.displayAllTabArr);
                });
               
                // 初始化model下拉框
                $("#model").append("<option value='parallel'>parallel</option>");
                $("#model").append("<option value='serial'>serial</option>");
                $("#model").append("<option value='single'>single</option>");
                
        		$('#btn_manage_cron', this.o_container).bind('click', function(event){self.showCronSharedModal()});
        		$('#btn_cronShared_query', this.o_container).bind('click', function(event){ $('#tb_cron_Shared').bootstrapTable('refresh') });
        		$('#btn_cronShared_clear', this.o_container).bind('click', function(event){
        		    var cronSharedModal = $("#cronSharedModal");
        		    cronSharedModal.find("input[name=cronSharedProduct]").val(cronSharedModal.find("input[name=paramProduct]").val());
        		    cronSharedModal.find("input[name=cronSharedAgent]").val(cronSharedModal.find("input[name=paramAgent]").val());
                });
        		$('#btn_delete_cronShared', this.o_container).bind('click', function(event){
        			self.delCronShared();
                });
        		$("#btn_jsonformat_kv").bind('click',function(event){
        			self.showJsonFormat();
        		});
        		$("#btn_json_save").bind('click',function(event){
        			self.saveJsonFormat();
        		});
            },
            operateCron : function(row){
            	var self = this;
            	ceditable.init("pluginName","cronProgramName","cron_param_tab_poolProperties", "cronEditModal");
            	var mymodal = $('#cronEditModal');
            
            	$("#addOrEdit").empty();
            	$("#addOrEdit").append(row==null?"新增":"修改");
            	
            	mymodal.modal('show');
            	var modalType = "add";
            	if(null == row){
            		ceditable.setJobIdAndSourceId(null,null);
            		mymodal.find("select[name=product]").val("");
            		mymodal.find("select[name=agent]").val("").trigger("change");
            		mymodal.find("input[name=jobId]").val('').attr("disabled", false);
            		mymodal.find("input[name=jobName]").val('');
            		mymodal.find("select[name=plugin]").val('').trigger('change');
            		mymodal.find("input[name=cronExpression]").val('');
            		mymodal.find("select[name=programName]").val('').trigger('change');
            		mymodal.find("select[name=model]").val("");
            		mymodal.find("select[name=plugin]").val("");
            		// 新增时默认自动开始
            		mymodal.find("input[name=autoStart]")[0].checked = true;
            		mymodal.find("input[name=creator]").val('');
            	}else{
            		ceditable.setJobIdAndSourceId(row.jobId,row.jobId);
            		modalType = "edit";
            		if(row.product) {
            			mymodal.find("select[name=product]").val(row.product).change();
            		}else {
            			mymodal.find("select[name=product]").val("").change();
            		}
            		
            		var cron_edit_agent = mymodal.find("select[name=agent]");
		        	if(row.agent != null && row.agent != ""){
		        		var avAgents = [];
		        		var options = row.agent.split(",");
		        		$.each(options,function(i){
		        			if (options[i] && options[i] != null && options[i].length > 0) {
		        				if (options[i].startsWith("#")) {
		        					cron_edit_agent.append(new Option(fal_agent_tag + options[i].substring(1,options[i].length), options[i]));
			        			} else {
			        				cron_edit_agent.append(new Option(fal_agent_kid + options[i], options[i]));
			        			}
		        				avAgents.push(options[i]);
		        			}
						})
	        		    cron_edit_agent.val(avAgents).trigger("change");
		        	} else {
		        		cron_edit_agent.val(null).trigger("change");
		        	}
		        	
            		mymodal.find("input[name=jobId]").val(row.jobId).attr("disabled", true);
            		mymodal.find("input[name=jobName]").val(row.jobName);
            		mymodal.find("input[name=cronExpression]").val(row.cronExpression);
            		mymodal.find("select[name=plugin]").val(row.plugin).trigger('change');
            		mymodal.find("select[name=programName]").val(row.programName).trigger('change');
            		mymodal.find("select[name=model]").val(row.model);
            		mymodal.find("input[name=autoStart]")[0].checked = row.autoStart==0?false:true;
            		mymodal.find("input[name=creator]").val(row.creator);
            	}
            	$('#cron_btn_save', this.o_container).unbind('click').bind('click', function(event){app.prototype.saveCron(modalType); });
            	mymodal.off().on("hidden.bs.modal",function(){
            		//隐藏wwsfInput相关modal
            		ceditable.hideWwsfInputModal();
            	});
            },
            del:function(event){
            	var selected = $('#tb_cron_keys').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var delFlag = false;
            		var crons = [];
            		_.each(selected, function(element, index){
            			if(element.status==null || element.status=="" || element.status=="STOPED") {
            				delFlag = true;
            			}
            			crons.push(element.jobId);
            		});
            		if(!delFlag) {
            			bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">只能删除未运行状态的计划任务</div>',
                			size:'small',
                			closeButton: false
                		});
            			return;
            		}
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : '确定删除所选计划任务？',
                		callback : function (result) {
                    		if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			url : 'cron/' + crons.join('|'),
                        			type : 'DELETE',
                        			contentType:'application/json',
                        			success: function(data){
                        				var datajson = JSON.parse(data);
                        				if(datajson.success){
                        					var info = datajson.info;
                        					bootbox.alert({
                                    			message:'<div class="alert alert-success mb-0 py-2">删除成功!'+info+'</div>',
                                    			size:'small',
                                    			closeButton: false
                                    		});
                        					$('#tb_cron_keys').bootstrapTable('refresh');
                        				}else{
                        					var msgInfo = '操作失败,服务器处理出错<br>' + datajson.info;
            	        	        		if (datajson.detailInfo && datajson.detailInfo != null && datajson.detailInfo != '') {
                        						msgInfo = msgInfo + '<br><a title="'+datajson.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                        					}
            	        	        		bootbox.alert({
            	                    			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
            	                    			size:'middle',
            	                    			closeButton: false
            	                    		});
                        				}
                        			},
                        			error: function(msg){
                        				bootbox.alert({
                                			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
                                			size:'small',
                                			closeButton: false
                                		});
                        			}
                        		});
                    		}
                    	 }
                	});
            	} else{
            		bootbox.alert({
            			message:'<div class="alert alert-danger mb-0 py-2">请选择需要删除的计划任务</div>',
            			size:'small',
            			closeButton: false
            		});
            	}
            },
            start:function(event){
            	var selected = $('#tb_cron_keys').bootstrapTable('getSelections');
            	if(selected.length >0){
            		var crons = [];
            		var cronNames=[];
            		_.each(selected, function(element, index){
            			crons.push(element.jobId);
            			cronNames.push("【"+element.jobName+"】");
            		});
            	var tSuccess = true;
            	var tConfirm = true;
            	bootbox.dialog({
            		size : 'small',
            		message : "启动"+cronNames.join(","),
            		buttons:{
            			success:{
            				label:'清除临时数据启动',
            				className:"btn btn-sm btn-primary",
            				callback : function (result) {
            					if(result && tSuccess){
            						tSuccess = false;
            						$.ajax({
            							data : {clearShared:"true",jobsId:crons.join('|')},
                            			url : 'cron/start',
                            			type : 'PUT',
                            			contentType:'application/x-www-form-urlencoded',		
                            			success: function(data){
                            				var datajson = JSON.parse(data);
	                        				if(datajson.success){
	                        					bootbox.alert({
	                                    			message:'<div class="alert alert-success mb-0 py-2">请求已提交，请刷新列表查看最新状态</div>',
	                                    			size:'small',
	                                    			closeButton: false
	                                    		});
	                        					$('#tb_cron_keys').bootstrapTable('refresh');
	                        				}else{
	                        					var msgInfo = '操作失败,服务器处理出错<br>' + datajson.info;
                	        	        		if (datajson.detailInfo && datajson.detailInfo != null && datajson.detailInfo != '') {
                            						msgInfo = msgInfo + '<br><a title="'+datajson.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                            					}
                	        	        		bootbox.alert({
                	                    			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
                	                    			size:'middle',
                	                    			closeButton: false
                	                    		});
	                        				}
                        			},
                        			error: function(msg){
                        				bootbox.alert({
                                			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
                                			size:'small',
                                			closeButton: false
                                		});
                        			}
                        		});
                			  }
            				}},
            				confirm:{
            				label:'启动',
            				className:"btn btn-sm btn-primary",
            				callback : function (result) {
                				if(result && tConfirm){
                					tConfirm = false;
            						$.ajax({
            							data : {clearShared:"false",jobsId:crons.join('|')},
                            			url : 'cron/start',
                            			type : 'PUT',
                            			contentType:'application/x-www-form-urlencoded',
                            			success: function(data){
                            				var datajson = JSON.parse(data);
	                        				if(datajson.success){
	                        					bootbox.alert({
	                                    			message:'<div class="alert alert-success mb-0 py-2">请求已提交，请刷新列表查看最新状态</div>',
	                                    			size:'small',
	                                    			closeButton: false
	                                    		});
	                        					$('#tb_cron_keys').bootstrapTable('refresh');
	                        				}else{
	                        					var msgInfo = '操作失败,服务器处理出错<br>' + datajson.info;
                	        	        		if (datajson.detailInfo && datajson.detailInfo != null && datajson.detailInfo != '') {
                            						msgInfo = msgInfo + '<br><a title="'+datajson.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                            					}
                	        	        		bootbox.alert({
                	                    			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
                	                    			size:'middle',
                	                    			closeButton: false
                	                    		});
	                        				}
                        			},
                        			error: function(msg){
                        				bootbox.alert({
                                			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
                                			size:'small',
                                			closeButton: false
                                		});
                        			}
                        		});
                			  }
                    	 	}
            			},
            			cancel:{
            				label:'取消',
            				className:"btn btn-sm btn-default"
            			}
            		}});
            	}else{
            		bootbox.alert({message:'请选择计划任务',size:'small'});
            	}
            },
            stop:function(event){
            	var selected = $('#tb_cron_keys').bootstrapTable('getSelections');
            	if(selected.length >0){
            		var crons = [];
            		var cronNames=[];
            		_.each(selected, function(element, index){
            			crons.push(element.jobId);
            			cronNames.push("【"+element.jobName+"】");
            		});
            	var tConfirm = true;
            	bootbox.confirm({
            		size : 'small',
            		message : '停止'+cronNames.join(","),
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			url : 'cron/stop',
                    			type : 'PUT',
                    			data : {jobsId:crons.join('|')},
                    			contentType:'application/x-www-form-urlencoded',
                    			success: function(data){
                    				var datajson = JSON.parse(data);
                    				if(datajson.success){
                    					bootbox.alert({
                                			message:'<div class="alert alert-success mb-0 py-2">请求已提交，请刷新列表查看最新状态</div>',
                                			size:'small',
                                			closeButton: false
                                		});
                    				 	$('#tb_cron_keys').bootstrapTable('refresh');
                    				}else{
                    					var msgInfo = '操作失败,服务器处理出错<br>' + datajson.info;
        	        	        		if (datajson.detailInfo && datajson.detailInfo != null && datajson.detailInfo != '') {
                    						msgInfo = msgInfo + '<br><a title="'+datajson.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                    					}
        	        	        		bootbox.alert({
        	                    			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
        	                    			size:'middle',
        	                    			closeButton: false
        	                    		});
                    				}
                    			},
                    			error: function(msg){
                    				bootbox.alert({
                            			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
                            			size:'small',
                            			closeButton: false
                            		});
                    			}
                    		});
                		}
                	  }
            	  });
            	}else{
            		bootbox.alert({message:'请选择计划任务',size:'small'});
            	}
            },
            restart:function(event){
            	var selected = $('#tb_cron_keys').bootstrapTable('getSelections');
            	if(selected.length >0){
            		var crons = [];
            		var cronNames=[];
            		_.each(selected, function(element, index){
            			crons.push(element.jobId);
            			cronNames.push("【"+element.jobName+"】");
            		});
            	var tSuccess = true;
                var tConfirm = true;
        		bootbox.dialog({
            		size : 'small',
            		message : "重启"+cronNames.join(","),
		    		buttons:{
		    			success:{
		    				label:'清除临时数据后重启',
		    				className:"btn btn-primary",
		    				callback : function (result) {
		        				if(result && tSuccess){
            						tSuccess = false;
		                    		$.ajax({
		                    			url : 'cron/restart',
		                    			type : 'PUT',
		                    			data : {clearShared:"true",jobsId:crons.join('|')},
		                    			contentType:'application/x-www-form-urlencoded',
		                    			success: function(data){
		                    				var datajson = JSON.parse(data);
		                    				if(datajson.success){
		                    					bootbox.alert({message:'重启成功',size:'small'});
		                    					$('#tb_cron_keys').bootstrapTable('refresh');
		                    				}else{
		                    					bootbox.alert({message:'重启操作失败,服务器出错<br>' + datajson.info,size:'small'});
		                    				}
		                    			},
		                    			error: function(msg){
		                    				bootbox.alert({
	                                			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
	                                			size:'small',
	                                			closeButton: false
	                                		});
		                    			}
		                    		});
		                		}
		            	 	}},
		    				confirm:{
		    				label:'确定',
		    				className:"btn btn-primary",
		    				callback : function (result) {
		        				if(result && tConfirm){
                					tConfirm = false;
		                    		$.ajax({
		                    			url : 'cron/restart',
		                    			type : 'PUT',
		                    			data : {clearShared:"false",jobsId:crons.join('|')},
		                    			contentType:'application/x-www-form-urlencoded',
		                    			success: function(data){
		                    				var datajson = JSON.parse(data);
		                    				if(datajson.success){
		                    					bootbox.alert({message:'重启成功',size:'small'});
		                    					$('#tb_cron_keys').bootstrapTable('refresh');
		                    				}else{
		                    					bootbox.alert({message:'重启操作失败,服务器出错<br>' + datajson.info,size:'small'});
		                    				}
		                    			},
		                    			error: function(msg){
		                    				bootbox.alert({
	                                			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
	                                			size:'small',
	                                			closeButton: false
	                                		});
		                    			}
		                    		});
		                		}
		            	 	}
		    			},
		    			cancel:{
		    				label:'取消',
		    				className:"btn btn-default"
		    			}
		    		}});
            	}else{
            		bootbox.alert({message:'请选择计划任务',size:'small'});
            	}
            },
            saveCron:function(modalType){
            	var self = this;
            	var myModal = $('#cronEditModal');
            	var formJson = {};
            	// autoStart
            	formJson.autoStart = myModal.find("input[name=autoStart]")[0].checked?1:0;
            	//product
        		formJson.product = myModal.find("select[name=product]").val();
        		//agent
        		var agentArr = myModal.find("select[name=agent]").val();
        		if(agentArr.length == 0){
        			formJson.agent = "";
	    		}else{
	    			var agents = "";
	    			for(var a = 0; a < agentArr.length; a++) { agents += "," + agentArr[a]}
	    			formJson.agent = agents + ",";
	    		}
        		//jobId
        		formJson.jobId = myModal.find("input[name=jobId]").val();
        		if(null == formJson.jobId || formJson.jobId == ""){
        			bootbox.alert({message:'操作失败，任务编号不能为空',size:'small'});
        			return false;
        	    }
        		var reg2 = /^[0-9a-zA-Z_]+$/;
        		if(!reg2.test(formJson.jobId)){
        			bootbox.alert({message:'操作失败，任务编号只能为数字、字母或者下划线',size:'small'});                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
        			return;
        		}
        		if(!validateStringLength(formJson.jobId,50)){
            		bootbox.alert({message:'操作失败，任务编号的长度不能超过50',size:'small'}); 
            		return false;
            	}
        		//jobName
        		formJson.jobName = myModal.find("input[name=jobName]").val();
        		if(null == formJson.jobName || formJson.jobName == ""){
        	    	bootbox.alert({message:'操作失败，任务名称不能为空',size:'small'});
        			return false;
        	    }
        		if(!validateStringLength(formJson.jobName,200)){
            		bootbox.alert({message:'操作失败，任务名称的长度不能超过200',size:'small'}); 
            		return false;
            	}
        		//cronExpression
        		formJson.cronExpression = myModal.find("input[name=cronExpression]").val();
        		if(null == formJson.cronExpression || formJson.cronExpression == ""){
        			bootbox.alert({message:'操作失败，任务执行表达式不能为空',size:'small'});
        			return false;
        	    }
        		//plugin
        		formJson.plugin = myModal.find("select[name=plugin]").val();
        		if(null == formJson.plugin || formJson.plugin == ""){
        			bootbox.alert({message:'操作失败，插件不能为空',size:'small'});
        			return false;
        	    }
        		//programName
        		formJson.programName = myModal.find("select[name=programName]").val();
        		if(null == formJson.programName || formJson.programName == ""){
        	    	bootbox.alert({message:'操作失败，程序名称不能为空',size:'small'});
        			return false;
        	    }
        		// model
            	formJson.model = myModal.find("select[name=model]").val();
            	if(null == formJson.model || formJson.model == ""){
        			bootbox.alert({message:'操作失败，模式不能为空',size:'small'});
        			return false;
        	    }
            	//creator
            	formJson.creator = myModal.find("input[name=creator]").val();
            	if(!validateStringLength(formJson.creator,50)){
            		bootbox.alert({message:'操作失败，创建人的长度不能超过50',size:'small'}); 
            		return false;
            	}
        		//cron_param_tab_poolProperties
        		var rowDatasJson = ceditable.getRowDatasJson('cron_param_tab_poolProperties');
        		if(rowDatasJson.errorInfo != ""){
        			bootbox.alert({message:rowDatasJson.errorInfo,size:'small'});
        			return false;
        		}
        		if(rowDatasJson.data != undefined){
        			formJson.parameter = rowDatasJson.data;
        			formJson.paramterIsMust = rowDatasJson.paramterIsMust;
        		}
        		//cron_shared_tab_poolProperties
        		/*var sharedPropertiesJson = {};
            	var propertiesObj = $('#cron_shared_tab_poolProperties').bootstrapTable("getData");
            	if(propertiesObj.length > 0){
            		var kAndVs = self.getKeyAndValues(propertiesObj);
            		if(kAndVs.errorInfo != ""){
            			bootbox.alert({message:kAndVs.errorInfo,size:'small'});
            			return false;
            		}
            		if(!jQuery.isEmptyObject(kAndVs.data)){
            			sharedPropertiesJson = kAndVs.data;
        			}
            	}
        		formJson.shared = sharedPropertiesJson;*/
        		var url = "cron/add";
        		if(modalType == "edit"){
        			url = "cron/update";
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
        			success: function(data){
        				var tData = JSON.parse(data);
        				if(tData.success){
        					bootbox.alert({message:'保存成功',size:'small'});
        					$('#cronEditModal').modal('hide');
        					$('#tb_cron_keys').bootstrapTable('refresh');
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + tData.info;
        	        		if (tData.detailInfo && tData.detailInfo != null && tData.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+tData.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
        	        		bootbox.alert({
                    			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
                    			size:'middle',
                    			closeButton: false
                    		});
        				}
        				mloadding.hideLoadding();
        			},
        			error: function(msg){
        				bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
                			size:'small',
                			closeButton: false
                		});
        				mloadding.hideLoadding();
        			}
        		});
            },
            initProduct:function(){
            	var self = this;
            	var pselEdit = $("#cronEditModal").find("select[name=product]");
				var pselQuery = $("#cj_query_form").find("select[name=product]");
				//var cronSharedQuery = $("#cronSharedModal").find("select[name=cronSharedProduct]");
				//var cronSharedAdd = $("#cronSharedAddOrEditModal").find("select[name=cronSharedAddOrEditProduct]");
            	$.ajax({
  	        		async: false,
  	    			url: 'product/selectProduct',
  	    			type: 'GET',
  	    			contentType: 'application/json',
  	    			success: function(data){
  	    				if(data.success){
  	    					pselEdit.empty();
  	    					pselQuery.empty();
  	    					pselEdit.append("<option value=''>请选择产品</option>");
  	    					pselQuery.append("<option value=''>请选择产品</option>");
    						var products = data.data.products;
    						$.each(products,function(i){
    							pselEdit.append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
    							pselQuery.append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
    						});
  	    				}else{
  	    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
        					bootbox.alert({
                    			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
                    			size:'middle',
                    			closeButton: false
                    		});
  	    				}
  	    			},
  	    			error: function(msg){
  	    				bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
                			size:'small',
                			closeButton: false
                		});
  	    			}
	          	});
            },
            initAgent : function() {
            	var self = this;
				var pselQuery = $("#cj_query_form").find("select[name=agent]");
				pselQuery.empty();
				pselQuery.append("<option value=''>请选择节点</option>");
            	$.ajax({
  	        		async: false,
  	    			url: 'zoo/selectAgent',
  	    			type: 'GET',
  	    			contentType: 'application/json',
  	    			success: function(data){
  	    				if(data.success){
	    					var agents = data.data.agents;
	    					$.each(agents,function(i){
	    						pselQuery.append("<option value='" + agents[i] + "'>" + agents[i] + "</option>");
	    					})
  	    				}else{
  	    					bootbox.alert({message:'获取节点失败<br>' + data.info ,size:'small'});
  	    				}
  	    			},
  	    			error: function(msg){
  	    				bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
                			size:'small',
                			closeButton: false
                		});
  	    			}
	          	});
            },
        	initEditTable :function(tabId,tabData,tabLoadUrl,cellUpdateUrl){
        		var response = [];
            	if ($.isEmptyObject(tabData)){
            		response.push({key:'',value:''});
            	} else {
            		$.each(tabData,function(key,value){
            			response.push({'key':key,'value':value});
            		})
            	}
            	$.mockjax.clear(tabLoadUrl);
            	$.mockjax({ url: tabLoadUrl, logging: 0, responseText: response });
            	$.mockjax.clear(cellUpdateUrl);
               	$.mockjax({
               	    url: cellUpdateUrl,
               	    logging: 0,
               	    response: function(settings) {
               	    	var rowdata = {index:settings.data.pk,row:{}};
               	    	rowdata.row[settings.data.name] = settings.data.value;
               	    	$('#'+tabId).bootstrapTable('updateRow', rowdata);
               	    	$("#"+tabId+" a").editable({url:cellUpdateUrl,type:"textarea"});
               	    }
               	});
               	$('#'+tabId).bootstrapTable('destroy');
            	$('#'+tabId).bootstrapTable({  
            		theadClasses: 'thead-light',
            		url: tabLoadUrl,
                    striped: true,  
                    clickToSelect: false,  
                    pagination: false,
                    editable: true,
                    columns: [  
                        {field:'key',title:'属性',align:'center',halign:'center',width:'30%',class:'tabStyle',
                        	formatter: function (value, row, index) {
                                return "<a href=\"#\" data-name=\"key\" data-pk=\""+index+"\" data-title=\"属性\">" + value + "</a>";
                            }},  
                        {field:'value',title:'值',align:'center',halign:'center',width:'55%',class:'tabStyle',
                        	formatter: function (value, row, index) {
                                return "<a href=\"#\" data-name=\"value\" data-pk=\""+index+"\" data-title=\"值\">" + value + "</a>";
                            }},
                        {title:'操作', align:"center", edit:false,width:'15%',
                        	events:{
                            	'click .tab_jip_removerow_style': function(e, value, row, index) {
                            		$('#'+tabId).bootstrapTable('removeRow', index);
                               		$("#"+tabId+" a").editable({url:cellUpdateUrl,type:"textarea"});
                            	},
                            	'click .tab_jip_appendrow_style': function(e, value, row, index) {
                           		 	$('#'+tabId).bootstrapTable('appendRow');
                           		 	$("#"+tabId+" a").editable({url:cellUpdateUrl,type:"textarea"});
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
                    	$("#"+tabId+" a").editable({url:cellUpdateUrl,type:"textarea"});
                    }
            	});
        	},
        	showCronLogsInfoWindow : function(row){
        		var self = this;
        		$("#cronLogJobId").empty();
        		$("#cronLogJobId").append(row.jobId);
        		
        		$("#cronLogsInfoWindowModal").modal('show');
        		$("#tb_cron_logs").bootstrapTable('destroy');
        		$('#tb_cron_logs').bootstrapTable({
        			theadClasses: 'thead-light',
                    url: 'cron/cronLog',
                    method: 'get',                     
                    cache: false,                      
                    pagination: true,                  
                    sortable: false,                    
                    sortOrder: "asc",               
                    queryParamsType:'undefined',
                    queryParams: function (params) {
                    	return {   
                    	   		pageNumber: params.pageNumber,   
                    	   		pageSize: params.pageSize,
                    	   		product:$("#cronLogsInfoWindowModal").find("input[name=cronLogProduct]").val(),
                    	   		agent:$("#cronLogsInfoWindowModal").find("input[name=cronLogAgent]").val(),
                    	   		jobId: row.jobId,
                    	   		startTime: $("#cronLogsInfoWindowModal").find("input[name=qStartTime]").val(),
                    	   		endTime: $("#cronLogsInfoWindowModal").find("input[name=qEndTime]").val(),
                    	   		result: $("#cronLogsInfoWindowModal").find("select[name=cronLogResult]").val()
                    	};
                    }, 
                    ajaxOptions: {
						beforeSend : function(){
	        				mloadding.showLoadding();
	        			},
	        			complete : function(){
	        				mloadding.hideLoadding();
	        			}
                    },
                    sidePagination: "server",           
                    pageNumber:1,                      
                    pageSize: 10,                    
                    pageList: [10,20, 30, 50, 100],       
                    minimumCountColumns: 2,             
                    uniqueId: "logId",                    
                    showToggle:false,                    
                    buttonsClass: 'sm btn-default',
                    cardView: false,                   
                    detailView: false,                  
                    showColumns: false,
                    showFullscreen: false,
                    showRefresh: false,
                    columns: [
                    	{field: 'product', title: '产品'}, 
                    	{field: 'agent', title: '节点', align:'center'},
                    	{field: 'result', title: '运行结果', align:'center', formatter:function(value,row,index){
                    			if(value){
                    				return "成功";
                    			}else{
                    				return "失败";
                    			}
                    		}
                    	},
                    	{field: 'beginTime', title: '开始时间'}, 
                        {field: 'endTime', title: '结束时间'}, 
                        {field: 'spendTime', title: '运行时间', align:'center'},
                        {field: 'information',align:"center", title: '详细日志',  edit:false,
                        	events:{
                            	'click .tab_cronLogDetail': function(e, value, row, index) {
                            		$.ajax({
                	          			async : false,
                            			data : {logId:row.logId},
                            			url : 'cron/getCronLogInformation',
                            			type : 'GET',
                            			contentType:'application/x-www-form-urlencoded',
                            			success: function(data){
                            				if(data.success){
                            					var cronLog = data.data.cronLog;
                            					if(cronLog.information == null || cronLog.information == ""){
                            						bootbox.alert({message:'没有执行日志',size:'small'});
                            					}else{
                            						$("#log_info_area").html($.trim(cronLog.information));
                                        			$("#cronLogInformationModal").modal("show");
                            					}
                            				}else{
                            					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
                            					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                            						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                            					}
                            					bootbox.alert({
                	                    			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
                	                    			size:'middle',
                	                    			closeButton: false
                	                    		});
                            				}
                            			},
                            			error: function(msg){
                            				bootbox.alert({
                                    			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
                                    			size:'small',
                                    			closeButton: false
                                    		});
                            			}
                            		});
                            	}
                            },
                        	formatter:function(value,row,index){
	                        	var btnHtml =  
	                            	'<button type="button" class="btn btn-xs btn-outline-info tab_cronLogDetail">' +
	                    				'<a aria-hidden="true">查看</a>' +
	                    			'</button>&nbsp;';
	                        	return btnHtml;
                        	}
                        }
                     ],
                    responseHandler: function (res) {
                    	return res.data;
                    }
                });
        		$('#btn_cronlog_query', this.o_container).unbind('click').bind('click', function(event){ $('#tb_cron_logs').bootstrapTable('refresh') });
                $('#btn_cronlog_clear', this.o_container).unbind('click').bind('click', function(event){
                	$("#cronLogsInfoWindowModal").find("input[name=qStartTime]").val('');
                	$("#cronLogsInfoWindowModal").find("input[name=qEndTime]").val('');
                	$("#cronLogsInfoWindowModal").find("select[name=cronLogResult]").val("-1");
                	if(row.product) {
                		$("#cronLogsInfoWindowModal").find("select[name=cronLogProduct]").val(row.product);
                	}else {
                		$("#cronLogsInfoWindowModal").find("select[name=cronLogProduct]").val("");
                	}
                	$("#cronLogsInfoWindowModal").find("input[name=cronLogAgent]").val(row.agent);
                });
            },
            showCronSharedModal:function() {
            	var self = this;
            	var selectedCronJob = $('#tb_cron_keys').bootstrapTable('getSelections');
            	if(selectedCronJob.length != 1) {
            		bootbox.alert({message:'请选择一条计划任务',size:'small'});
            		return;
            	}
            	var jrow = selectedCronJob[0];
    			var cronSharedModal = $("#cronSharedModal");
            	$('#btn_add_cronShared')
	            	.unbind('click')
	            	.bind('click', 
            			function(event){
            				self.showCronSharedAddOrEditModal(jrow, null);
						});
            	
            	cronSharedModal.modal('show');
            	
        		$("#tb_cron_Shared").bootstrapTable('destroy');
	    		$('#tb_cron_Shared').bootstrapTable({
	    			theadClasses: 'thead-light',
                    url: 'cron/getCronShared',         
                    method: 'get',                     
                    toolbar: '#toolbar_cronShared',               
                    striped: true,                      
                    cache: false,                      
                    pagination: true,                  
                    sortable: false,                    
                    sortOrder: "asc",               
                    queryParamsType:'undefined',
                    queryParams: function (params) {
                    	return {   
                    		pageNumber: params.pageNumber,   
                    	   	pageSize: params.pageSize,  
                    	   	product: $("#cronSharedModal").find("input[name=cronSharedProduct]").val(),
                    	   	agent: $("#cronSharedModal").find("input[name=cronSharedAgent]").val(),
                    	   	jobId: jrow.jobId
                    	};
                    },           
                    sidePagination: "server",           
                    pageNumber:1,                      
                    pageSize: 20,                    
                    pageList: [20, 30, 50, 100],       
                    minimumCountColumns: 2,             
                    clickToSelect: true,                
                    uniqueId: "jobId",                    
                    showToggle:false,                    
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                   
                    detailView: false,                  
                    showColumns: true,
                    showFullscreen: false,
                    showRefresh: true,
                    columns: [
                    	{checkbox: true}, 
                    	{field: 'jobId', title: '任务编号' },
                    	{field: 'product', title: '产品' },
                    	{field: 'agent', title: '节点', algin:'center' },
                    	{field: 'sharedJson', title: '运行时数据',formatter: function (value, row, index){
                    		if(null != value && value.length>80) {
                    			return value.substring(0,80)+"......";
                    		}
                    		return value;
                    	}},
                    	{field: 'occuTime', title: '变更时间' ,algin:'center',formatter: function (value, row, index){
                    		return value? value:"";
                    	}}
                    ],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    onDblClickRow: function (row) {
                    	self.showCronSharedAddOrEditModal(jrow, row);
                    }
	    		});
            },
            showCronSharedAddOrEditModal:function(jrow, row) {
            	var self = this;
            	var modal = $("#cronSharedAddOrEditModal");
            	var editJobIdEl = modal.find("input[name=cronSharedAddOrEditJobId]");
            	editJobIdEl.attr('disabled',true);
            	editJobIdEl.val(jrow.jobId);
            	$("#cronSharedAddOrEdit").empty();
            	
            	if(row == null) {
            		$("#cronSharedAddOrEdit").text("新增");
            		modal.find("input[name=cronSharedAddOrEditTime]").val("");
            		modal.find("textarea[name=sharedJson]").val("");
            		modal.find("input[name=cronSharedAddOrEditProduct]").attr('disabled',false);
            		modal.find("input[name=cronSharedAddOrEditAgent]").attr('disabled',false);
            	}else{
            		$("#cronSharedAddOrEdit").text("修改");
            		modal.find("input[name=cronSharedAddOrEditProduct]").attr('disabled',true).val(row.product);
            		modal.find("input[name=cronSharedAddOrEditAgent]").attr('disabled',true).val(row.agent);
            		modal.find("input[name=cronSharedAddOrEditTime]").val(row.occuTime);
            		modal.find("textarea[name=sharedJson]").val(row.sharedJson);
            	}
            	modal.modal('show');
            	
            	$('#cronSharedAddOrEditConfirm').unbind('click').bind('click', function(event){
            		self.addOreditCronShared(row == null?'add':'update');
				});
            },
            addOreditCronShared:function(op) {
            	var modal = $("#cronSharedAddOrEditModal");
            	var cronShared = {};
            	cronShared.jobId      = modal.find("input[name=cronSharedAddOrEditJobId]").val();
            	cronShared.product    = modal.find("input[name=cronSharedAddOrEditProduct]").val();
            	cronShared.agent      = modal.find("input[name=cronSharedAddOrEditAgent]").val();
            	cronShared.occuTime   = modal.find("input[name=cronSharedAddOrEditTime]").val();
            	cronShared.sharedJson = modal.find("textarea[name=sharedJson]").val();
            	if(!cronShared.product) {
            		bootbox.alert({message:'产品不能为空',size:'small'});
            		return;
            	}
            	if(!cronShared.agent) {
            		bootbox.alert({message:'节点不能为空',size:'small'});
            		return;
            	}
            	if(!validateStringLength(cronShared.occuTime,30)){
            		bootbox.alert({message:'变更时间的长度不能超过30',size:'small'}); 
            		return false;
            	}
            	if(!validateStringLength(cronShared.sharedJson,2000)){
            		bootbox.alert({message:'运行时数据的长度不能超过2000',size:'small'}); 
            		return false;
            	}
            	var url = op=="update"? "cron/editCronShared":"cron/addCronShared";
            	
            	var msg = "保存";
            	if(!mloadding.showLoadding()){
            		return false;
            	}
            	$.ajax({
            		async:false,
        			url:url,
                    type:"POST",
                    processData:false,
                    data:cronShared,
                    dataType : 'json',
        			success: function(data){
        				if(data.success){
        					bootbox.alert({message:msg+"成功",size:'small'});
        					$('#cronSharedAddOrEditModal').modal('hide');
                    		$('#tb_cron_Shared').bootstrapTable('refresh');
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        	        		if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="' + data.detailInfo + '"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
        	        		bootbox.alert({
                    			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
                    			size:'middle',
                    			closeButton: false
                    		});
        				}
        				mloadding.hideLoadding();
        			},
        			error: function(msg){
        				bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
                			size:'small',
                			closeButton: false
                		});
        				mloadding.hideLoadding();
        			}
        		});
            },
            delCronShared:function() {
            	var selected = $('#tb_cron_Shared').bootstrapTable('getSelections');
            	if(selected.length<1) {
            		bootbox.alert({
            			message:'<div class="alert alert-danger mb-0 py-2">请选择需要删除的运行时数据</div>',
            			size:'small',
            			closeButton: false
            		});
            		return;
            	}
            	var productArr = [];
            	var agentArr = [];
            	var jobId = selected[0].jobId;
            	for(var i=0, len=selected.length; i<len; i++) {
            		productArr.push(selected[i].product);
            		agentArr.push(selected[i].agent);
            	}
            	var tConfirm = true;
        		bootbox.confirm({
            		size : 'small',
            		message : '确定删除所选运行时数据？',
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			url : 'cron/deleteCronShared',
                    			type : 'DELETE',
                    			data : {jobId:jobId, products:productArr.join('|'), agents:agentArr.join('|')},
                    			dataType : 'json',
                    			success: function(data){
                    				if(data.success){
                    					bootbox.alert({message:'删除成功',size:'small'});
                    					$('#tb_cron_Shared').bootstrapTable('refresh');
                    				}else{
                    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        	        	        		if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                    					}
        	        	        		bootbox.alert({
        	                    			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
        	                    			size:'middle',
        	                    			closeButton: false
        	                    		});
                    				}
                    			},
                    			error: function(msg){
                    				bootbox.alert({
                            			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
                            			size:'small',
                            			closeButton: false
                            		});
                    			}
                    		});
                		}
                	 }
            	});
            },
            showJsonFormat:function() {
            	var data = $("#cronSharedAddOrEditModal").find("textarea[name=sharedJson]").val();
            	var dataJson = null;
            	if(data != null && data != ''){
            		data = data.replace(/\r/g, '').replace(/\\r/g, '');
            		try {
            			dataJson = $.parseJSON(data);
            		} catch (error) {
            			bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">非json格式数据，无法格式化</div>',
                			size:'small',
                			closeButton: false
                		});
            			return;
            		}
            		jsonEditor = new JsonEditor('#json-display', dataJson, {withQuotes : true});
                	$('#json_format_modal').modal("show");
            	}else{
            		bootbox.alert({
            			message:'<div class="alert alert-danger mb-0 py-2">value值为空</div>',
            			size:'small',
            			closeButton: false
            		});
            	}
            },
            saveJsonFormat:function() {
            	if(jsonEditor != null) {
            		var jsonStr = "";
            		try {
            			jsonStr = JSON.stringify(jsonEditor.get());
    				} catch (error) {
    					bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">json格式错误</div>',
                			size:'small',
                			closeButton: false
                		});
            			return;
    				}
    				$("#cronSharedAddOrEditModal").find("textarea[name=sharedJson]").val(jsonStr);
    				$('#json_format_modal').modal("hide");
            	}
            },
            loadCronSharedProduct:function(pProduct,pSelectName) {
            	var selObj = $("select[name="+pSelectName+"]");
            	selObj.empty();
            	if(null == pProduct || pProduct == ""){
            		$.ajax({
	  	        		async: false,
	  	    			url: 'product/selectProduct',
	  	    			type: 'GET',
	  	    			contentType: 'application/json',
	  	    			success: function(data){
	  	    				if(data.success){
	  	    					selObj.append("<option value=''>请选择产品</option>");
	    						var products = data.data.products;
	    						$.each(products,function(i){
	    							selObj.append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
	    						});
	  	    				}else{
	  	    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
	        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
	        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
	        					}
	    	                	bootbox.alert({
	                    			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
	                    			size:'middle',
	                    			closeButton: false
	                    		});
	  	    				}
	  	    			},
	  	    			error: function(msg){
	  	    				bootbox.alert({
                    			message:'<div class="alert alert-danger mb-0 py-2">连接服务器错误</div>',
                    			size:'small',
                    			closeButton: false
                    		});
	  	    			}
		          	});
            	}else{
            		selObj.append("<option value='" + pProduct + "'>" + pProduct + "</option>");
            	}
            }
        });
        return app;
});
