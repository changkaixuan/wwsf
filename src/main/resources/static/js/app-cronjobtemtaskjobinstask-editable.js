define(['bootstrap-table', 'x-editable', 'mockjax'],function () {	
	var ret = {};
	
	ret.jobId = null;
	ret.sourceId = null;
	
	ret.setJobIdAndSourceId = function(jobId,sourceId){
		ret.jobId = jobId;
		ret.sourceId = sourceId;
	}
	
	ret.init = function(pluginId,programNameId,tabId){
		//加载插件下拉框数据
		ret.initPluginSelcet(pluginId);
		//插件下拉框改变事件
        $("#"+pluginId).bind('change', function(event){
        	//加载程序名称下拉框数据
        	ret.initProgramNameSelcet(programNameId,this.value);
        	var pluginArr = [];
        	ret.selectOnchangePluginName(this.value,$("#"+programNameId).val(),pluginArr);
        	ret.initializeTable("#"+tabId,pluginArr,"/taskProperties","/updateTaskCellProperties","属性","值","是否必填");
        });
        //程序名称输入框失去焦点事件
        $("#"+programNameId).bind('change', function(event){
        	var pluginArr = [];
        	ret.selectOnchangePluginName($("#"+pluginId).val(),this.value,pluginArr);
        	ret.initializeTable("#"+tabId,pluginArr,"/taskProperties","/updateTaskCellProperties","属性","值","是否必填");
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
	        		bootbox.alert({message:'获取插件失败',size:'small'});
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
        				pluginArr.push(pluginJosn);
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
	}
	
	ret.initializeTable = function(useId,pluginArr,useUrl,useChildUrl,columnKey,columnValue,isMust){
		var response = [];
		if (pluginArr.length == 0){
			response.push({key:'',value:'',isMust:0,tempParamType:3});
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
       	    	$(useId + " a[name=editableAddEditLink]").editable({url:useChildUrl});
       	    }
       	});
       	$(useId).bootstrapTable('destroy');	
      	$(useId).bootstrapTable({ 
      		theadClasses: 'thead-light',
      		classes: 'table table-bordered table-striped text-wrap',
    		url: useUrl,
            striped: true,  
            clickToSelect: false,  
            pagination: false,
            editable: true,
            columns: [  
            	{field:'tempParamType',title:'type',align:'center',visible:false}, 
                {field:'key',title:columnKey,align:'right',                 	  
                	formatter: function (value, row, index) {
                		if(row.tempParamType == 1 || row.tempParamType == 2){//1：插件参数 2：程序参数（不可改变参数）
                			return "<a href=\"#\" data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                		}
                        return "<a name='editableAddEditLink' href=\"#\" data-name=\"key\" data-pk=\""+index+"\" data-title=" + columnKey + ">" + value + "</a>";
                    }},  
                {field:'value',title:columnValue,align:'left',
                	formatter: function (value, row, index) {
                		if(row.tempParamType == 2){//2：程序参数（不可改变参数）
                			return "<a href=\"#\" data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                		}
                        return "<a name='editableAddEditLink' href=\"#\" data-name=\"value\" data-pk=\""+index+"\" data-title=" + columnValue + ">" + value + "</a>";
                    }},
                {field:'isMust',align:'center',title:isMust,width:'65',
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
                			if(row.tempParamType == 1 || row.tempParamType == 2){//1：插件参数 2：程序参数（不可改变参数） 3：计划任务或task参数（可随意编辑）
                				if(value == 1){//选中 必输参数
                					return "<input type='checkbox' disabled=true name='isMust' class='ssssss' checked=true></input>";
                				}
                				return "<input type='checkbox' disabled=true name='isMust' class='ssssss'></input>";
                    		} else {
                    			if(value == 1){
                    				return "<input type='checkbox' name='isMust' class='ssssss' checked=true></input>";
                    			}
                    			return "<input type='checkbox' name='isMust' class='ssssss'></input>";
                    		}
                        }
                },
                {title:'操作', align:"center", edit:false,width:'65',
                	events:{
                    	'click .tab_jip_removerow_style': function(e, value, row, index) {
                    		if(row.isMust || row.tempParamType == 2){
                    			bootbox.alert({message:'必填参数和程序参数不能删除',size:'small'});
                    			return;
                    		}
                       		$(useId).bootstrapTable('removeRow', index);
                       		$(useId + " a[name=editableAddEditLink]").editable({
                       			 url:useChildUrl
                            });
                    	},
                    	'click .tab_jip_appendrow_style': function(e, value, row, index) {
                   		 	$(useId).bootstrapTable('appendRow');
                   		 	$(useId + " a[name=editableAddEditLink]").editable({
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
            	  $(useId + " a[name=editableAddEditLink]").editable({ url:useChildUrl,type:"text"});
            }
    	});
	}
	
	ret.getRowDatasJson = function(tabId){
    	var retJson = {"errorInfo":""};
    	var obj = $('#'+tabId).bootstrapTable("getData");
    	if(obj.length > 0){
    		var dataJson = {};
    		var paramterIsMustJson = {};
    		for(var i=0;i<obj.length;i++){
    			if(obj[i].tempParamType != 2){
        			if(obj[i].key == "" && obj[i].value != "" && obj[i].isMust=='1'){
    					retJson.errorInfo = "第"+(i+1)+"行,属性不能为空";
    					break;
    				}
    				if(obj[i].key != "" && obj[i].value == "" && obj[i].isMust=='1'){
    					retJson.errorInfo = "第"+(i+1)+"行,值不能为空";
    					break;
    				}
    				if(obj[i].key != "" && obj[i].value != ""){
    					dataJson[obj[i].key] = obj[i].value;
    					paramterIsMustJson[obj[i].key] = obj[i].isMust==1?1:0;
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