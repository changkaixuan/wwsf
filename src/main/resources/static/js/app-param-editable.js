define(['bootstrap-table', 
		'x-editable', 
		'mockjax'],function () {	
	var app_editable = {};

	app_editable.initEditTable = function(tabId,tabData,tabLoadUrl,cellUpdateUrl){
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
       	    	$("#"+tabId+" a").editable({url:cellUpdateUrl});
       	    }
       	});
       	$('#'+tabId).bootstrapTable('destroy');
    	$('#'+tabId).bootstrapTable({
    		theadClasses: 'thead-light',
    		classes: 'table table-sm table-bordered table-striped',
    		url: tabLoadUrl,
            editable: true,
            columns: [  
                {field:'key',title:'属性',align:'right', width:'160px',
                	formatter: function (value, row, index) {
                        return "<a href=\"#\" data-name=\"key\" data-pk=\""+index+"\" data-title=\"属性\">" + value + "</a>";
                    }},  
                {field:'value',title:'值',align:'left',
                	formatter: function (value, row, index) {
                        return "<a href=\"#\" data-name=\"value\" data-pk=\""+index+"\" data-title=\"值\">" + value + "</a>";
                    }},
                {title:'操作', align:"center", edit:false, width:'75px',
                	events:{
                    	'click .tab_jip_removerow_style': function(e, value, row, index) {
                    		$('#'+tabId).bootstrapTable('removeRow', index);
                       		$("#"+tabId+" a").editable({url:cellUpdateUrl});
                    	},
                    	'click .tab_jip_appendrow_style': function(e, value, row, index) {
                   		 	$('#'+tabId).bootstrapTable('appendRow');
                   		 	$("#"+tabId+" a").editable({url:cellUpdateUrl});
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
            	$("#"+tabId+" a").editable({url:cellUpdateUrl});
            }
    	});
	};
	
	app_editable.initEditTableView = function(tabId,tabData,tabLoadUrl,cellUpdateUrl){
		var response = [];
    	if ($.isEmptyObject(tabData)){
    		response.push({key:'',value:''});
    	} else {
    		$.each(tabData,function(key,value){
			var vdesc = (value == "1" ? "是" : "否");
    			response.push({'key':key,'value':vdesc});
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
       	    	$("#"+tabId+" a").editable({url:cellUpdateUrl});
       	    }
       	});
       	$('#'+tabId).bootstrapTable('destroy');
    	$('#'+tabId).bootstrapTable({
    		theadClasses: 'thead-light',
    		url: tabLoadUrl,
    		classes: 'table table-bordered table-striped text-wrap',
            pagination: true,
            noInfoPagination: true,
            pageSize: 5,
            editable: true,
            columns: [  
                {field:'key',title:'任务',align:'left',
                	formatter: function (value, row, index) {
                        return value;
                    }},  
                {field:'value',title:'完成',align:'center', width:'75',
                	formatter: function (value, row, index) {
                        return value;
                    }}
            ],
            onLoadSuccess: function (aa, bb, cc) {
            	$("#"+tabId+" a").editable({url:cellUpdateUrl});
            }
    	});
	}
	
	app_editable.initEditTableView2 = function(tabId,tabData,tabLoadUrl,cellUpdateUrl,tabHead){
		var response = [];
    	if ($.isEmptyObject(tabData)){
    		response.push({value:''});
    	} else {
    		if (tabData instanceof Array){//数组
        		for(var i=0;i<tabData.length;i++){
        			response.push({'value':tabData[i]});
        		}
    		}else{//map键值对
    			$.each(tabData,function(key,value){
        			response.push({'value':key});
        		});
    		}
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
       	    	$("#"+tabId+" a").editable({url:cellUpdateUrl});
       	    }
       	});
       	$('#'+tabId).bootstrapTable('destroy');
    	$('#'+tabId).bootstrapTable({
    		theadClasses: 'thead-light',
    		url: tabLoadUrl,
    		classes: 'table table-bordered table-striped text-wrap',
            pagination: true,
            noInfoPagination: true,
            pageSize: 5, 
            editable: true,
            cache:false,
            columns: [  
                {field:'value',title:tabHead,align:'left'}
            ],
            onLoadSuccess: function (aa, bb, cc) {
            	$("#"+tabId+" a").editable({url:cellUpdateUrl});
            }
    	});
	}
	
	app_editable.initServerPluginInfoEditTable = function(tabId,tabData,tabLoadUrl,cellUpdateUrl){
		var response = [];
    	if ($.isEmptyObject(tabData)){
    		response.push({name:'',queueSize:'', active:'', coreSize:'', shutdown:''});
    	} else {
			for(var i=0;i<tabData.length;i++){
    			response.push({'name':tabData[i].name,'queueSize':tabData[i].queueSize,'active':tabData[i].active,'coreSize':tabData[i].coreSize,'shutdown':tabData[i].shutdown});
    		}
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
       	    	$("#"+tabId+" a").editable({url:cellUpdateUrl});
       	    }
       	});
       	$('#'+tabId).bootstrapTable('destroy');
    	$('#'+tabId).bootstrapTable({
    		theadClasses: 'thead-light',
    		url: tabLoadUrl,
            editable: true,
            classes: 'table table-bordered table-striped',
            columns: [  
                {field:'name',title:'插件' },  
                {field:'queueSize',title:'队列长度',align:'center'},
                {field:'active',title:'活动线程',align:'center'},
                {field:'coreSize',title:'核心线程',align:'center',
                	formatter: function (value, row, index) {
                		return "<a href=\"#\" data-name=\"coreSize\" data-pk=\""+index+"\" data-title=\"核心线程\">" + value + "</a>";
                	}
                },
                {field:'shutdown',title:'状态',align:'center',
                	formatter: function (value, row, index) {
                		if (value == false) {
                			return "<span class='badge badge-success'>正常</span>";
                		} else {
                			return "<span class='badge badge-danger'>异常</span>";
                		}
                	}}
            ],
            onLoadSuccess: function (aa, bb, cc) {
            	$("#"+tabId+" a").editable({url:cellUpdateUrl});
            }
    	});
	};
	
	app_editable.initSRInfoTable = function(tabId,tabData,tabLoadUrl,cellUpdateUrl){
		var response = [];
    	if ($.isEmptyObject(tabData)){
    		//response.push({name:'',rcv_size:'', drv_size:'', drv_parallel:'',  res_size:'', res_parallel:'', rcv_parallel:''});
    	} else {
			for(var i=0;i<tabData.length;i++){
    			response.push({'name':tabData[i].name,
    							'rcv_size':tabData[i].rcv_size,
    							'rcv_parallel':tabData[i].rcv_active + "/" + tabData[i].rcv_parallel,
    							'drv_size':tabData[i].drv_size,
    							'drv_parallel':tabData[i].drv_active + "/" + tabData[i].drv_parallel,
    							'res_size':tabData[i].res_size,
    							'res_parallel':tabData[i].res_active + "/" + tabData[i].res_parallel
    							});
    		}
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
       	    	$("#"+tabId+" a").editable({url:cellUpdateUrl});
       	    }
       	});
       	$('#'+tabId).bootstrapTable('destroy');
    	$('#'+tabId).bootstrapTable({
    		theadClasses: 'thead-light',
    		url: tabLoadUrl,
            editable: true,
            classes: 'table table-bordered table-striped',
            columns: [  
                {field:'name',title:'资源编号',align:'center'},  
                {field:'rcv_size',title:'rcv_size',align:'center'},
                {field:'rcv_parallel',title:'rcv_parallel',align:'center'},
                {field:'drv_size',title:'drv_size',align:'center'},
                {field:'drv_parallel',title:'drv_parallel',align:'center'},
                {field:'res_size',title:'res_size',align:'center'},
                {field:'res_parallel',title:'res_parallel',align:'center'},
                {title:'操作',align:'center',
                	events:{
                    	'click .tab_server_table_srInfo_style': function(e, value, row, index) {
                    		$.ajax({
        	        			async : false,
                    			data : {resourceId:row.name},
                    			url : 'server/destroyScheduleResource',
                    			type : 'GET',
                    			contentType:'application/x-www-form-urlencoded',
        	        			success: function(data){
        	        				if(data.success){
        	        					bootbox.alert({message:'操作成功',size:'small'});
        	        					$(".btn_refresh_pluginqueue").click();
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
                    },
                	formatter:function(value,row,rowIndex){  
                    	return "<button type='button' class='btn btn-xs btn-default px-1 tab_server_table_srInfo_style'>释放资源</button>";
                	}
                }
            ],
            onLoadSuccess: function (aa, bb, cc) {
            	$("#"+tabId+" a").editable({url:cellUpdateUrl});
            }
    	});
	};
	
	app_editable.initSIQRInfoTable = function(tabId,tabData,tabLoadUrl,cellUpdateUrl){
		var response = [];
    	if ($.isEmptyObject(tabData)){
    		//response.push({name:'',init_size:'', init_parallel:'', init_active:'',  ended_size:'', ended_parallel:'', ended_active:''});
    	} else {
			for(var i=0;i<tabData.length;i++){
    			response.push({'name':tabData[i].name,
    							'init_size':tabData[i].init_size,
    							'init_parallel':tabData[i].init_active + "/" + tabData[i].init_parallel,
    							'init_active':tabData[i].init_active,
    							'ended_size':tabData[i].ended_size,
    							'ended_parallel':tabData[i].ended_active + "/" + tabData[i].ended_parallel,
    							'ended_active':tabData[i].ended_active
    							});
    		}
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
       	    	$("#"+tabId+" a").editable({url:cellUpdateUrl});
       	    }
       	});
       	$('#'+tabId).bootstrapTable('destroy');
    	$('#'+tabId).bootstrapTable({
    		theadClasses: 'thead-light',
    		url: tabLoadUrl,
            editable: true,
            classes: 'table table-bordered table-striped',
            columns: [  
                {field:'name',title:'产品名称',align:'center'},  
                {field:'init_size',title:'init_size',align:'center'},
                {field:'init_parallel',title:'init_parallel',align:'center'},
                {field:'init_active',title:'init_active',align:'center'},
                {field:'ended_size',title:'ended_size',align:'center'},
                {field:'ended_parallel',title:'ended_parallel',align:'center'},
                {field:'ended_active',title:'ended_active',align:'center'}
            ],
            onLoadSuccess: function (aa, bb, cc) {
            	$("#"+tabId+" a").editable({url:cellUpdateUrl});
            }
    	});
	};
	
	return app_editable;
});