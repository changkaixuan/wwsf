﻿﻿define([
	'underScore', 
	'text!application/wws_job_parameter.html',
	'css!js/bootstrap/css/bootstrap-table.css'],
    function (_, template) {
		var app = function (pA,pInput,pSpan,pProduct) {
	        this.o_template = $(template);
	        this.o_a = pA;
	        this.o_input = pInput;
	        this.o_span = pSpan;
	        this.o_product = pProduct;
	    }
        _.extend(app.prototype, {
        	initialize:function(){
        		
            },
            load: function () {

            },
            render: function () {
            	var self = this;
            	this.o_template.insertAfter(this.o_a); //将modal追加到<a>后面
            	var tModal = $("#wwsJobParameterModal");
            	//加载产品
            	var selJob = tModal.find("select[name=job]");
            	$.ajax({
            		async:false,
            		url: 'template/selectWwJob?product='+this.o_product,
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
        	    	   bootbox.alert({message:'获取任务下拉框数据出错' + e.status,size:'small'});
        	       }  	
        		});
            	selJob.unbind("change").bind("change", function() {
        			self.jobChange(this);
        		});
            	var tJobId = "";
            	var aInputValue = this.o_input.value;
				if(aInputValue != null && aInputValue != ""){
					var aInputValueJosn = JSON.parse(aInputValue);
					tJobId = aInputValueJosn.jobId;
				}
            	if(tJobId == ""){//没有选择产品时加载一行空表格
            		self.initializeTable("#tab_wwsJobParameter",null,"/wwsJobParameters","/updateWwsJobParameters","属性","值","是否必填");
            	}else{
            		selJob.val(tJobId).trigger("change");
            	}
            	$('#wws_job_parameter_btn_save').unbind('click').bind('click', function(event){self.saveJobParameter();});
            	$('#wws_job_parameter_btn_close').unbind('click').bind('click', function(event){
            		self.o_span.find('form[class="form-inline editableform"]').submit();
            		tModal.modal("hide");
            		tModal.empty().remove();
            	});
            	tModal.modal('show');
            },
            initializeTable: function(useId,pluginArr,useUrl,useChildUrl,columnKey,columnValue,isMust){
               	// 填充数据
               	var fillTableData = function() {
               		//加载key
               		$(useId + " a[name=key]").editable({
                    	url:useChildUrl,
                    	type:'text',
                    	clear:false
                    });
               		//加载value
               		$(useId + " a[name=value]").editable({
                    	url:useChildUrl,
                    	type:'text',
                    	clear:false
                    });
               	}
        		var response = [];
        		if (null == pluginArr || pluginArr.length == 0){
        			response.push({key:'',value:'',isMust:0,tempParamType:0});
        		} else {
        			$.each(pluginArr,function(fIndex,pluginJson){
        				response.push({'key':pluginJson.name,'value':pluginJson.value,isMust:pluginJson.isMust,tempParamType:pluginJson.tempParamType});
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
                    	{field:'tempParamType',title:'type',align:'center',visible:false}, 
                        {field:'key',title:columnKey,align:'right',width:'140px',                 	  
                        	formatter: function (value, row, index) {
                        		if(row.tempParamType == 1){//1：JOB参数(key不可编辑)
                        			return "<a href=\"#\" data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                        		}
                                return "<a name='key' href=\"#\" data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                            }},  
                        {field:'value',title:columnValue,align:'left',
                        	formatter: function (value, row, index) {
                                return "<a name='value' href=\"#\" data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                            }},
                        {field:'isMust',align:'center',title:isMust,width:'65px',
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
                        	    	if(value == 1){//选中 必输参数
                    					return "<input type='checkbox' disabled=true name='isMust' class='ssssss' checked=true></input>";
                    				}
                    				return "<input type='checkbox' disabled=true name='isMust' class='ssssss'></input>";
                                }
                        },
                        {title:'操作', align:"center", edit:false,width:'65px',
                        	events:{
                            	'click .tab_jip_removerow_style': function(e, value, row, index) {
                            		if(row.isMust || row.tempParamType == 2){
                            			bootbox.alert({message:'必填参数不能删除',size:'small'});
                            			return;
                            		}
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
        	jobChange: function(jobSelect) {
        		var self = this;
        		var selectJobId = jobSelect.value;
        		$.ajax({
            		async:false,
        			data : {jobId:selectJobId},
        			url : 'template/getJobParameter',
        			type : 'GET',
        			contentType:'application/json',
        			success: function(data){
        				if(data.success){
        					var jobParameterArr = []; //需要显示的表格数据
        					var aJobParameterArr = []; //链接的值/parameters
        					var aKeyArr = []; //链接的值/parameters/key
        					var aValueArr = []; //链接的值/parameters/value
        					var aInputValue = self.o_input.value; //链接的值(包含：jobId、parameters)
        					if(aInputValue != null && aInputValue != ""){
        						var aInputValueJosn = JSON.parse(aInputValue);
        						if(selectJobId == aInputValueJosn.jobId){//下拉框jobId=连接的值/jobId
	            					var parametersJson = aInputValueJosn.parameters;
	            					for(var key in parametersJson){
	            						aKeyArr.push(key);
            							aValueArr.push(parametersJson[key]);
            							aJobParameterArr.push({name:key,value:parametersJson[key],isMust:0,tempParamType:0});
	            					}
        						}
        					}
        					var jobParameters = data.data.jobParameters;
        					var jobKeyArr = [];
        					if(jobParameters != undefined){//JOB有参数
        						$.each(jobParameters,function(i){
        							var index = $.inArray(jobParameters[i].name, aKeyArr);
        							if(index == -1){//JOB参数不在"链接的值/parameters"中，所有属性值以JOB参数为准
        								jobParameterArr.push({name:jobParameters[i].name,value:jobParameters[i].value,isMust:jobParameters[i].required,tempParamType:1});
        							}else{//JOB参数在"链接的值/parameters"中，值以"链接的值/parameters/value"为准
        								jobParameterArr.push({name:jobParameters[i].name,value:aValueArr[index],isMust:jobParameters[i].required,tempParamType:1});
        							}
        							jobKeyArr.push(jobParameters[i].name);
        						});
        						//将新增的参数追加进来
        						for(var i=0;i<aJobParameterArr.length;i++){
        							var index =  $.inArray(aJobParameterArr[i].name, jobKeyArr);
        							if(index == -1){//链接的值/parameters/key,不在JOB参数中，认为是新增的参数
        								jobParameterArr.push(aJobParameterArr[i]);
        							}
        						}
        					}else{//JOB没有参数，以链接的值/parameters为准
        						jobParameterArr = aJobParameterArr;
        					}
        					self.initializeTable("#tab_wwsJobParameter",jobParameterArr,"/wwsJobParameters","/updateWwsJobParameters","属性","值","是否必填");
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	bootbox.alert({message:msgInfo, size:'middle'});
        				}
        			},
        			error: function(msg){
        				bootbox.alert({message:'获取任务参数出错',size:'small'});
        			}
        		});
        	},
        	saveJobParameter: function(){
        		var myModal = $('#wwsJobParameterModal');
        		var formJson = {};
        		formJson.jobId = myModal.find("select[name=job]").val();
        		if(formJson.jobId == null || formJson.jobId == ""){
        			bootbox.alert({message:'操作失败，任务不能为空',size:'small'});
        			return false;
        		}
        		var parameters = {};
        		var rows = $('#tab_wwsJobParameter').bootstrapTable("getData");
        		for(var i=0; i<rows.length; i++){
        			if(rows[i].isMust == 1){//必填参数
        				if(rows[i].key == null || rows[i].key == ""){
        					bootbox.alert({message:'操作失败，第'+(i+1)+"行，属性不能为空",size:'small'});
        					return false;
        				}
        				if(rows[i].value == null || rows[i].value == ""){
        					bootbox.alert({message:'操作失败，第'+(i+1)+"行，值不能为空",size:'small'});
        					return false;
        				}
        				parameters[rows[i].key] = rows[i].value;
        			}else{
        				if(rows[i].key == null || rows[i].key == ""){//属性为空
        					if(rows[i].value != null && rows[i].value != ""){//值不为空
            					bootbox.alert({message:'操作失败，第'+(i+1)+"行,属性不能为空",size:'small'});
            					return false;
            				}
        				}else{//属性不为空
            				parameters[rows[i].key] = rows[i].value;
        				}
        			}
        		}
        		formJson.parameters = parameters;
        		if(formJson.jobId != null && formJson.jobId != ""){
        			this.o_input.value = JSON.stringify(formJson);
        		}else{
        			this.o_input.value = "";
        		}
        		this.o_span.find('form[class="form-inline editableform"]').submit();
        		myModal.modal("hide");
        		myModal.empty().remove();
        	}
        });
        return app;
});