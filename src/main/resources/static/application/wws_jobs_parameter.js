﻿﻿define([
	'underScore', 
	'text!application/wws_jobs_parameter.html',
	'css!js/bootstrap/css/bootstrap-table.css'],
    function (_, template) {
		var app = function (pA,pInput,pSpan,pProduct) {
	        this.o_template = $(template);
	        this.o_a = pA;
	        this.o_input = pInput;
	        this.o_span = pSpan;
	        this.o_product = pProduct;
	        this.o_appendJobIdArr = []; //存已追加jobId
	    }
        _.extend(app.prototype, {
        	initialize:function(){
        		
            },
            load: function () {

            },
            render: function () {
            	var self = this;
            	this.o_template.insertAfter(this.o_a); //将modal追加到<a>后面
            	var tModal = $("#wwsJobsParameterModal");
            	//加工回显值
            	var aInputValue = this.o_input.value;
            	var commonParametersJson = {};
            	var jobsArr = []; //输入框中job在表(ww_job)中
            	if(aInputValue != null && aInputValue != ""){//输入框为空就加载一条空数据
            		var batchJson = JSON.parse(aInputValue);
            		commonParametersJson = batchJson.commonParameters;
            		var inputJobsArr = batchJson.jobs; //输入框中所有的job
            		var pJobIdArr = []; //输入框中的所有的jobId
            		for(var i=0;i<inputJobsArr.length;i++){
            			pJobIdArr.push(inputJobsArr[i].jobId);
            		}
            		$.ajax({
                		async:false,
                		url: 'template/getWwJob?product='+this.o_product+"&includeJobIds="+pJobIdArr.toString(),
            			type : 'GET',
            			contentType:'application/x-www-form-urlencoded',
            	        success:function (data) {
            	        	if(data.success){
            	        		var tableJobsArr = data.data.jobs; 
            	        		if(tableJobsArr != undefined){
            	        			for(var i=0;i<tableJobsArr.length;i++){
            	        				for(var j=0;j<inputJobsArr.length;j++){
            	        					if(tableJobsArr[i].jobId == inputJobsArr[j].jobId){
            	        						jobsArr.push(inputJobsArr[j]);
            	        						break;
            	        					}
            	                		}
                	        		}
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
            	    	   bootbox.alert({message:'获取JOB出错' + e.status,size:'small'});
            	       }  	
            		});
            		for(var i=0;i<jobsArr.length;i++){
            			self.o_appendJobIdArr.push(jobsArr[i].jobId);
            		}
            	}
            	//1.初始化共性属性
            	self.initializePublicTable("#tab_wwsJobsParameter_public",commonParametersJson,"/wwsPublicJobsParameters","/updateWwsPublicJobsParameters","属性","值");
            	//加载产品
            	self.initSelectJob(tModal);
            	//JOBS/追加按钮事件
            	$('#wws_jobs_parameter_tabs_jobs').find("button[name=btnAppend]").unbind('click').bind('click', function(event){
            		self.appendJob();
            	});
            	//2.JOBS  
            	self.initJobsTable("#tab_wwsJobsParameter_jobs",jobsArr,"/tabWwsJobsParameterJobs","/updateTabWwsJobsParameterJobs","任务编号","参数");
            	//保存按钮事件
            	$('#wws_jobs_parameter_btn_save').unbind('click').bind('click', function(event){
            		self.saveJobsParameter();
            	});
            	//关闭按钮事件
            	$('#wws_jobs_parameter_btn_close').unbind('click').bind('click', function(event){
            		self.o_span.find('form[class="form-inline editableform"]').submit();
            		tModal.modal("hide");
            		tModal.empty().remove();
            	});
            	tModal.modal('show');
            },
            initSelectJob: function(tModal){
            	var selJob = tModal.find("select[name=job]");
            	var exclusionJobIds = this.o_appendJobIdArr.join(',');
            	$.ajax({
            		async:false,
            		url: 'template/selectWwJob?product='+this.o_product+"&exclusionJobIds="+exclusionJobIds,
        			type : 'GET',
        			contentType:'application/x-www-form-urlencoded',
        	        success:function (data) {
        	        	if(data.success){
        	        		selJob.empty();
        	        		selJob.append("<option value=''>请选择任务</option>");
        	        		var jobs = data.data.jobs;
        	        		if(jobs != undefined){
        	        			for(var i=0;i<jobs.length;i++){
            	        			selJob.append("<option value='" + jobs[i].jobId + "'>" + jobs[i].name + "</option>");
            	        			//selJob.append("<option value='" + jobs[i].jobId + "'>" + jobs[i].jobId + "|" + jobs[i].name + "</option>");
            	        		}
        	        		}
        	        	} else {
        	        		bootbox.alert({message:'获取任务下拉框数据失败',size:'small'});
        	        	}
        	       },
        	       error: function (e) {
        	    	   bootbox.alert({message:'获取任务下拉框数据出错' + e.status,size:'small'});
        	       }  	
        		});
            },
            initializePublicTable: function(useId,commonParametersJson,useUrl,useChildUrl,columnKey,columnValue){//初始化共性属性表格
               	// 填充数据
               	var fillTableData = function() {
               		$(useId + " a").editable({
                    	url:useChildUrl,
                    	type:'text',
                    	clear:false
                    });
               	}
        		var response = [];
        		for(var key in commonParametersJson){
    				response.push({'key':key,'value':commonParametersJson[key]});
    			}
        		if(response.length == 0){
        			response.push({key:'',value:''});
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
               	    	fillTableData();
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
                        {field:'key',title:columnKey,align:'right',width:'140px',                 	  
                        	formatter: function (value, row, index) {
                                return "<a href=\"#\" data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                            }},  
                        {field:'value',title:columnValue,align:'left',
                        	formatter: function (value, row, index) {
                                return "<a href=\"#\" data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                            }},
                        {title:'操作', align:"center", edit:false,width:'65px',
                        	events:{
                            	'click .tab_jip_removerow_style': function(e, value, row, index) {
                               		$(useId).bootstrapTable('removeRow', index);
                               		fillTableData();
                            	},
                            	'click .tab_jip_appendrow_style': function(e, value, row, index) {
                           		 	$(useId).bootstrapTable('appendRow');
                           		 	fillTableData();
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
                    	fillTableData();
                    }
            	});
        	},
        	appendJob: function(){
        		var self = this;
        		var tModal = $('#wwsJobsParameterModal');
        		var selectJobId = tModal.find("select[name=job]").val();
        		if(selectJobId == null || selectJobId == ""){
        			bootbox.alert({message:'任务不能为空',size:'small'});
        			return false;
        		}
        		var jobsArr = [];
        		$.ajax({
            		async:false,
            		url: 'template/getJobParameter?jobId='+selectJobId,
        			type : 'GET',
        			contentType:'application/x-www-form-urlencoded',
        	        success:function (data) {
        	        	if(data.success){
        	        		var jobParameters = data.data.jobParameters;
        	        		var batchJobJson = {};
	        				batchJobJson["jobId"] = selectJobId;
	        				var parametersJson = {};
        	        		if(jobParameters != undefined){
        	        			for(var i=0;i<jobParameters.length;i++){
        	        				parametersJson[jobParameters[i].name] = jobParameters[i].value;
            	        		}
        	        		}
        	        		batchJobJson["parameters"] = parametersJson;
    	        			jobsArr.push(batchJobJson);
        	        	} else {
        	        		var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	bootbox.alert({message:msgInfo, size:'middle'});
        	        	}
        	       },
        	       error: function (e) {
        	    	   bootbox.alert({message:'获取任务下拉框数据出错' + e.status,size:'small'});
        	       }  	
        		});
        		var jobsRows = $('#tab_wwsJobsParameter_jobs').bootstrapTable("getData");
        		for(var i=0; i<jobsRows.length; i++){
        			if(jobsRows[i].key != null && jobsRows[i].key != ""){
        				var batchJobJson = {};
        				batchJobJson["jobId"] = jobsRows[i].key;
        				var parametersJson = {};
        				if(jobsRows[i].value != null && jobsRows[i].value != ""){
        					parametersJson = JSON.parse(jobsRows[i].value);
        				}
        				batchJobJson["parameters"] = parametersJson;
	        			jobsArr.push(batchJobJson);
        			}
        		}
        		self.initJobsTable("#tab_wwsJobsParameter_jobs",jobsArr,"/tabWwsJobsParameterJobs","/updateTabWwsJobsParameterJobs","任务编号","参数");
        		self.o_appendJobIdArr.push(selectJobId);
        		self.initSelectJob(tModal);
        	},
        	initJobsTable: function(useId,jobsArr,useUrl,useChildUrl,columnKey,columnValue){//初始化共性属性表格
        		var self = this;
               	// 填充数据
               	var fillTableData = function() {
           			/*$(useId + " a[name=value]").editable({
                    	url:useChildUrl,
                    	type:'text',
                    	clear:false
                    });*/
               		var rows = $(useId).bootstrapTable("getData");
                	for(var i=0; i<rows.length; i++) {
                		$(useId + " a[name=value"+i+"]").editable({
	        				url:useChildUrl,
	        				type:"text",
	        				clear:false,
	        				onblur:'ignore',
	        				tpl:"<input type='text' name='wwsfInputEditTable' readonly='true' style='width:100%;'><input type='hidden' name='firstModalId' value='wwsJobsParameterModal'><input type='hidden' name='jobId' value='"+rows[i].key+"'>"
	        			});
                	}
               	}
        		var response = [];
        		if (null == jobsArr || jobsArr.length == 0){
        			//response.push({key:'',value:''});
        		} else {
        			for(var i=0;i<jobsArr.length;i++){
        				var batchJobJson = jobsArr[i];
        				var parametersStr = JSON.stringify(batchJobJson.parameters);
        				if(parametersStr == "{}"){
        					parametersStr = "";
        				}
        				response.push({'key':batchJobJson.jobId,'value':parametersStr});
        			}
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
               	    	fillTableData();
               	    }
               	});
               	$(useId).bootstrapTable('destroy');	
              	$(useId).bootstrapTable({ 
              		theadClasses: 'thead-light',
            		url: useUrl,
                    striped: true,  
                    clickToSelect: false,  
                    pageSize: 10,
              	    pageList: [10, 25, 50, 100],
                    pagination: false,
                    editable: true,
                    columns: [  
                        {field:'key',title:columnKey,align:'right',width:'140px',                 	  
                        	formatter: function (value, row, index) {
                                return "<a href=\"#\" data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                            }},  
                        {field:'value',title:columnValue,align:'left',
                        	formatter: function (value, row, index) {
                                return "<a name='value"+index+"' href=\"#\" data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + (value ? value : '') + "</a>";
                            }},
                        {title:'操作', align:"center", edit:false,width:'65px',
                        	events:{
                            	'click .tab_jip_removerow_style': function(e, value, row, index) {
                            		//debugger;
                            		if($.inArray(row.key,self.o_appendJobIdArr) != -1){
                            			self.o_appendJobIdArr.splice($.inArray(row.key,self.o_appendJobIdArr),1);
                            			self.initSelectJob($("#wwsJobsParameterModal"));
                            		}
                               		$(useId).bootstrapTable('removeRow', index);
                               		var editTableRow = $(useId).bootstrapTable("getData").length;
                               		if(editTableRow == 1){
                               			if($(useId).bootstrapTable("getData")[0].key == null || $(useId).bootstrapTable("getData")[0].key == ""){
                               				$(useId).bootstrapTable('removeAll');
                               			}else{
                               				fillTableData();
                               			}
                               		}else{
                               			fillTableData();
                               		}
                            	}
                            },
                        	formatter:function(value,row,rowIndex){
                        		var btnHtml =  
                                	'<button type="button" class="btn btn-xs btn-outline-danger tab_jip_removerow_style btn-icon">' +
                    					'<i class="fal fa-minus" aria-hidden="true"></i>' +
                    				'</button>';
                            	return btnHtml;
                        	}}
                    ],
                    onLoadSuccess: function (aa, bb, cc) {
                    	fillTableData();
                    }
            	});	
        	},
        	saveJobsParameter: function(){
        		var tModal = $('#wwsJobsParameterModal');
        		var batchJson = {};
        		var commonParameters = {};
        		var publicRows = $('#tab_wwsJobsParameter_public').bootstrapTable("getData");
        		for(var i=0; i<publicRows.length; i++){//保存共性属性
	        		if(publicRows[i].key == null || publicRows[i].key == ""){//属性为空
	        			if(publicRows[i].value != null && publicRows[i].value != ""){//值不为空
	            			bootbox.alert({message:'操作失败，第'+(i+1)+"行,参数不能为空",size:'small'});
	            			return false;
	            		}
	        		}else{//属性不为空
	        			commonParameters[publicRows[i].key] = publicRows[i].value;
	        		}
        		}
        		var jobs = [];
        		var jobsRows = $('#tab_wwsJobsParameter_jobs').bootstrapTable("getData");
        		for(var i=0; i<jobsRows.length; i++){//保存jobs
        			if(jobsRows[i].key == null || jobsRows[i].key == ""){//jobId为空
	        			if(jobsRows[i].value != null && jobsRows[i].value != ""){//参数不为空
	            			bootbox.alert({message:'操作失败，第'+(i+1)+"行,任务编号不能为空",size:'small'});
	            			return false;
	            		}
	        		}else{//jobId不为空
	        			var batchJobJson = {};
	        			batchJobJson["jobId"] = jobsRows[i].key;
	        			var parametersJson = {};
        				if(jobsRows[i].value != null && jobsRows[i].value != ""){
        					parametersJson = JSON.parse(jobsRows[i].value);
        				}
	        			batchJobJson["parameters"] = parametersJson;
	        			jobs.push(batchJobJson);
	        		}
        		}
        		if(jobs.length > 0) {
        			batchJson.commonParameters = commonParameters;
        			batchJson.jobs = jobs;
        			this.o_input.value = JSON.stringify(batchJson);
        		}else{
        			bootbox.alert({message:'任务不能为空',size:'small'});
        			return false;
        		}
        		this.o_span.find('form[class="form-inline editableform"]').submit();
        		tModal.modal("hide");
        		tModal.empty().remove();
        	}
        });
        return app;
});