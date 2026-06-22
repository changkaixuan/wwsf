define(['jqueryUI',
	'daterangepicker',
	'select2',
	'css!css/select2.min.css'],function () {
	var ret = {};
	
	ret.jobId = null;
	ret.sourceId = null;
	ret.fal_agent_kid = "<i class='fal fa-key fa-fw' style='margin-right:2px'></i>";
	ret.fal_agent_tag = "<i class='fal fa-tag fa-fw' style='margin-right:2px'></i>";
	
	ret.setJobIdAndSourceId = function(jobId,sourceId){
		ret.jobId = jobId;
		ret.sourceId = sourceId;
	}
	
	ret.init = function(pluginId,programNameId,tabId,modelId){
		//加载插件下拉框数据
		ret.initPluginSelcet(pluginId);
		//插件下拉框改变事件
        $("#"+pluginId).unbind('change').bind('change', function(event){
        	//加载程序名称下拉框数据
        	ret.initProgramNameSelcet(programNameId,this.value);
        	$("#"+programNameId).val("").change();
        });
        //程序名称输入框失去焦点事件
        $("#"+programNameId).unbind('change').bind('change', function(event){
        	var pluginArr = [];
        	ret.selectOnchangePluginName($("#"+pluginId).val(),this.value,pluginArr);
        	ret.initializeTable("#"+tabId,pluginArr,"/taskProperties","/updateTaskCellProperties","属性","值","是否必填",modelId);
		});
        // 产品下拉框事件
        $("#"+modelId).find("select[name=product]").unbind("change").bind("change", function() {
        	//触发节点改变
        	var product = this.value;
        	var pselQuery = $("#"+modelId).find("select[name=agent]");
			pselQuery.empty();
			pselQuery.select2({
        		dropdownParent: $("#"+modelId),
        		allowClear: true,
        		multiple: true,
             	ajax: {
             		async: true,
            		url: 'server/selectAgentScope?product='+product,	
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
    	           					item.text = ret.fal_agent_kid + selectTwos[i].text;
    	           				}else{
    	           					item.text = ret.fal_agent_tag + selectTwos[i].text;
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
			
        	//插件设置为空
        	$("#"+pluginId).val("").change();
        });
	}
	
	ret.initPluginSelcet = function(pluginId){
    	$.ajax({
    		async:false,
    		url: 'template/getPluginByProduct',
			type : 'GET',
			contentType:'application/x-www-form-urlencoded',
	        success:function (data) {
	        	if(data.success){
	        		var sel_plugin = $("#"+pluginId);
	        		sel_plugin.empty();
	        		sel_plugin.append("<option  value='' selected='selected'>请选择</option>");
	        		var plugins = data.data.plugins;
	        		for(var i=0;i<plugins.length;i++){
	        			sel_plugin.append("<option value='" + plugins[i].name + "'>" + plugins[i].name + "</option>");
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
		});
    }
	
	ret.initProgramNameSelcet = function(programNameId,pPlugin){
    	$.ajax({
    		async:false,
    		data : {plugin:pPlugin},
    		url: 'template/getProgramNameByPlugin',
			type : 'GET',
			contentType:'application/x-www-form-urlencoded',
	        success:function (data) {
	        	if(data.success){
	        		var sel_programName = $("#"+programNameId);
	        		sel_programName.empty();
	        		sel_programName.append("<option  value='' selected='selected'>请选择</option>");
	        		var programNames = data.data.programNames;
	        		for(var i=0;i<programNames.length;i++){
	        			sel_programName.append("<option value='" + programNames[i] + "'>" + programNames[i] + "</option>");
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
	    	   bootbox.alert({message:'获取程序名称失败' + e.status,size:'small'});
	       }  	
		});
    }
	
	ret.selectOnchangePluginName = function (pluginName,programName,pluginArr){
		$.ajax({
			async:false,
			data : {jobId:ret.jobId,sourceId:ret.sourceId,pluginName:pluginName,programName:programName},
			url: 'template/getPluginParameter',
			type : 'GET',
			contentType:'application/x-www-form-urlencoded',       	        	
        	success:function (data) {
        		if(data.success){
        			var params = data.data.params;
        			$.each(params,function(i){
        				var pluginJosn = {};
        				pluginJosn.name = params[i].name;
        				pluginJosn.value = params[i].value;
        				if(params[i].required){
        					pluginJosn.isMust = 1;
        				} else {
        					pluginJosn.isMust = 0;
        				}
        				pluginJosn.tempParamType = params[i].tempParamType;
        				pluginJosn.paramType = params[i].paramType;
        				pluginJosn.dataRange = params[i].dataRange;
        				pluginArr.push(pluginJosn);
        			});
        		}else{
        			bootbox.alert({message:data.info,size:'small'});
        		} 
        	},
        	error: function (e) {
        		bootbox.alert({message:'系统错误',size:'small'});
        	}
   	    });
	}
	
	ret.initializeTable = function(useId,pluginArr,useUrl,useChildUrl,columnKey,columnValue,isMust,modelId){
		var tProduct = "";
		var modal = $("#"+modelId);
		if(modal.find("label[name=product]").text()) {
			tProduct = modal.find("label[name=product]").text();
		}else {
			tProduct = modal.find("select[name=product]").val();
		}
		/*var tAgent = modal.find("select[name=agent]").val();
		if(tAgent == undefined){
			tAgent = "";
		}*/
		//获取数据源
       	var getDataSource = function() {
			var tResult = [];
			$.ajax({
				//url:"dataSource/dataSourceNameList?product="+tProduct+"&agent="+tAgent,
				url:"dataSource/dataSourceNameList?product="+tProduct,
				async:false,
				type:"GET",
				contentType:'application/json',
				success:function (data) {
					if(data.success){
						var dataSourceList = data.data.dataSourceList;
						if(dataSourceList != undefined){
							$.each(dataSourceList,function(i){
								tResult.push({value:dataSourceList[i].name,text:dataSourceList[i].name});
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
				error: function (e) {
        			bootbox.alert({message:'获取数据源下拉框数据出错',size:'small'});
	   	        } 
		    });
			return tResult;
		}
		//获取产品认证信息编号
       	var getProductCertification = function() {
			var tResult = [];
			$.ajax({
				url:"product/getProductCertification?product="+tProduct,
				async:false,
				type:"GET",
				contentType:'application/json',
				success:function (data) {
					if(data.success){
						var productCertificationList = data.data.productCertificationList;
						if(productCertificationList != undefined){
							$.each(productCertificationList,function(i){
								tResult.push({value:productCertificationList[i].authId,text:productCertificationList[i].authId});
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
				error: function (e) {
        			bootbox.alert({message:'获取产品认证信息编号下拉框数据出错',size:'small'});
	   	        } 
		    });
			return tResult;
		}
       	//获取服务器
       	var getServer = function() {
			var tResult = [];
			$.ajax({
				url:"server/selectServer?product="+tProduct,
				async:false,
				type:"GET",
				contentType:'application/json',
				success:function (data) {
					if(data.success){
						var servers = data.data.servers;
						if(servers != undefined){
							$.each(servers,function(i){
								tResult.push({value:servers[i].serverId,text:servers[i].serverId});
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
				error: function (e) {
        			bootbox.alert({message:'获取服务器下拉框数据出错',size:'small'});
	   	        } 
		    });
			return tResult;
		}
       	// 填充数据
       	var fillTableData = function() {
       		//加载key
       		$(useId + " a[name=editableAddEditLink]").editable({
            	url:useChildUrl
            });
       	   //加载可编辑value 
       		var rows = $(useId).bootstrapTable("getData");
        	for(var i=0; i<rows.length; i++) {
        		var tValueId = rows[i].paramType+i;
        		if($(useId).find("a[name="+tValueId+"]").length > 0){//可以编辑参数值 
	        		if(rows[i].paramType == "STRING") {
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	        				url:useChildUrl,
	        				type:"text"
	        			});
	        		}else if(rows[i].paramType == "DATASOURCE"){
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	                		url:useChildUrl,
	              		    type:'select',
	              		    tpl:'<select style="width:100%;"></select>',
	              		    source:getDataSource()
	              	    });
	        		}else if(rows[i].paramType == "CERTIFICATION"){
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	                		url:useChildUrl,
	              		    type:'select',
	              		    tpl:'<select style="width:100%;"></select>',
	              		    source:getProductCertification()
	              	    });
	        		}else if(rows[i].paramType == "SERVER"){
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	                		url:useChildUrl,
	              		    type:'select',
	              		    tpl:'<select style="width:100%;"></select>',
	              		    source:getServer()
	              	    });
	        		}else if(rows[i].paramType == "BOOLEAN") {
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	        				url:useChildUrl,
	        				type:"text",
	        				clear:false,
	        				tpl:"<input type='checkbox' value='"+rows[i].value+"' name='wwsfInputCheckbox'>"
	        			});
	        		}else if(rows[i].paramType == "DATE") {
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	        				url:useChildUrl,
	        				type:"text",
	        				tpl:"<input type='text' name='wwsfInputDate' style='width:100%;'>"
	        			});
	        		}else if(rows[i].paramType == "DATETIME") {
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	        				url:useChildUrl,
	        				type:"text",
	        				tpl:"<input type='text' name='wwsfInputDateTime' style='width:100%;'>"
	        			});
	        		}else if(rows[i].paramType == "JOB") {
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	        				url:useChildUrl,
	        				type:"text",
	        				clear:false,
	        				onblur:'ignore',
	        				tpl:"<input type='text' name='wwsfInputJob' readonly='true' style='width:100%;'><input type='hidden' name='product' value='"+tProduct+"'>"
	        			});
	        		}else if(rows[i].paramType == "JOBS") {
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	        				url:useChildUrl,
	        				type:"text",
	        				clear:false,
	        				onblur:'ignore',
	        				tpl:"<input type='text' name='wwsfInputJobs' readonly='true' style='width:100%;'><input type='hidden' name='product' value='"+tProduct+"'>"
	        			});
	        		}else if(rows[i].paramType == "RANGE"){
	        			var dataRangArr = [];
	        			if(rows[i].dataRange != null && rows[i].dataRange != ""){
	        				var tArr = JSON.parse(rows[i].dataRange);
		        		    for(var x=0;x<tArr.length;x++){
		        		    	dataRangArr.push({text:tArr[x].value,value:tArr[x].key});
		        		    }
		        		    dataRangArr.sort(function(nextRow, curRow) {
		        		    	if(nextRow.value > curRow.value) {
		        		    		return 1;
		        		    	} 
		        		    	return -1;
		        		    });
	        			}
	        			var dataRangeValue = rows[i].value;
	        			if(dataRangeValue == null){
	        				dataRangeValue = "";
	        			}
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	                		url:useChildUrl,
	                		value:dataRangeValue,
	              		    type:'select',
	              		    tpl:'<select style="width:100%;"></select>',
	              		    source:dataRangArr
	              	    });
	        		}else if(rows[i].paramType == "LONG") {
	        			var tIndex = i;
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	        				url:useChildUrl,
	        				type:"text",
	        				validate: function(pInputValue){
	        					var rFlag = isNumberWwsf(pInputValue);
	        					if(!rFlag){
	        						//bootbox.alert({message:'第'+(tIndex+1)+'行值，必须是LONG',size:'small'});
	        						return '第'+(tIndex+1)+'行值，必须是LONG';
	        					}
	        				}
	        			});
	        		}else if(rows[i].paramType == "INT") {
	        			var tIndex = i;
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	        				url:useChildUrl,
	        				type:"text",
	        				validate: function(pInputValue){
	        					var rFlag = isNumberWwsf(pInputValue);
	        					if(!rFlag){
	        						//bootbox.alert({message:'第'+(tIndex+1)+'行值，必须是INT',size:'small'});
	        						return '第'+(tIndex+1)+'行值，必须是INT';
	        					}
	        				}
	        			});
	        		}else{//默认字符串类型
	        			$(useId + " a[name="+tValueId+"]").editable({ 
	        				url:useChildUrl,
	        				type:"text"
	        			});
	        		}
	        	}
        	}
       	}
		var response = [];
		if (pluginArr.length == 0){
			response.push({key:'',value:'',isMust:0,tempParamType:3,paramType:'STRING'});
		} else {
			$.each(pluginArr,function(fIndex,pluginJson){
				response.push({'key':pluginJson.name,'value':pluginJson.value,isMust:pluginJson.isMust,tempParamType:pluginJson.tempParamType,paramType:pluginJson.paramType,dataRange:pluginJson.dataRange});
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
            	{field:'paramType',title:'paramType',align:'center',visible:false},
            	{field:'dataRange',title:'dataRange',align:'center',visible:false},
                {field:'key',title:columnKey,align:'right',width:'25%',                	  
                	formatter: function (value, row, index) {
                		if(row.tempParamType == 1 || row.tempParamType == 2){//1：插件参数 2：程序参数（不可改变参数）
                			return "<a href=\"#\" data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                		}
                        return "<a name='editableAddEditLink' href=\"#\" data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                    }},  
                {field:'value',title:columnValue,align:'left',width:'55%',
                	formatter: function (value, row, index) {
                		if(row.tempParamType == 2){//2：程序参数（不可改变参数）
                			return "<a href=\"#\" data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                		}
                		var tParamType = row.paramType;
                		var tValueId = tParamType+index;
                        return "<a name="+tValueId+" href=\"#\" data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                    }},
                {field:'isMust',align:'center',title:isMust,width:'10%',
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
                {title:'操作', align:"center", edit:false,width:'10%',
                	events:{
                    	'click .tab_jip_removerow_style': function(e, value, row, index) {
                    		if(row.isMust || row.tempParamType == 2){
                    			bootbox.alert({message:'必填参数和程序参数不能删除',size:'small'});
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
	}
	
	ret.hideWwsfInputModal = function(){
		//隐藏jobs参数/job参数modal
		var wwsEditTableModal = $("#wwsEditTableModal");
		if(wwsEditTableModal.length > 0){
			wwsEditTableModal.modal("hide");
		}
		//隐藏jobs参数modal
		var wwsJobsParameterModal = $("#wwsJobsParameterModal");
		if(wwsJobsParameterModal.length > 0){
			wwsJobsParameterModal.modal("hide");
		}
		//隐藏job参数modal
		var wwsJobParameterModal = $("#wwsJobParameterModal");
		if(wwsJobParameterModal.length > 0){
			wwsJobParameterModal.modal("hide");
		}
	}
	
	ret.getRowDatasJson = function(tabId){
    	var retJson = {"errorInfo":""};
    	var obj = $('#'+tabId).bootstrapTable("getData");
    	if(obj.length > 0){
    		var dataJson = {};
    		var paramterIsMustJson = {};
    		for(var i=0;i<obj.length;i++){
    			if(obj[i].tempParamType != 2){//第2种类型的参数不可以修改,不需存为cron或job参数（不存入表ww_parameter）
    				if(obj[i].isMust == '1'){
    					if(obj[i].key == null || obj[i].key == ""){
        					retJson.errorInfo = "操作失败，第"+(i+1)+"行，属性不能为空";
        					break;
        				}
    					if(!validateStringLength(obj[i].key,200)){
            				retJson.errorInfo = "操作失败，第"+(i+1)+"行，属性的长度不能超过200"; 
                    		break;
                    	}
    					if(obj[i].value == null || obj[i].value == ""){
        					retJson.errorInfo = "操作失败，第"+(i+1)+"行，值不能为空";
        					break;
        				}
    					/*
    					if(!validateStringLength(obj[i].value,2000)){
        					retJson.errorInfo = "操作失败，第"+(i+1)+"行，值的长度不能超过2000"; 
                    		break;
                    	}*/
    					dataJson[obj[i].key] = obj[i].value;
    					paramterIsMustJson[obj[i].key] = obj[i].isMust==1?1:0;
    				}else{
    					if(obj[i].key == null || obj[i].key == ""){
    						if(obj[i].value != null && obj[i].value != ""){
    							retJson.errorInfo = "操作失败，第"+(i+1)+"行，属性不能为空";
            					break;
    						}
        				}else{
        					if(!validateStringLength(obj[i].key,200)){
                				retJson.errorInfo = "操作失败，第"+(i+1)+"行，属性的长度不能超过200"; 
                        		break;
                        	}
        					if(obj[i].value != null && obj[i].value != ""){
        						/*
        						if(!validateStringLength(obj[i].value,2000)){
                					retJson.errorInfo = "操作失败，第"+(i+1)+"行，值的长度不能超过2000"; 
                            		break;
                            	}*/
        						dataJson[obj[i].key] = obj[i].value;
            					paramterIsMustJson[obj[i].key] = obj[i].isMust==1?1:0;
    						}
        				}
    				}
    			}
    		}
    		if(retJson.errorInfo == ""){
    			retJson.data = dataJson;
    			retJson.paramterIsMust = paramterIsMustJson;
    		}
    	}
    	return retJson;
    }
	
	return ret;
});