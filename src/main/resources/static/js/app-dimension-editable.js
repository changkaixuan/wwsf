define(['bootstrap-table', 'x-editable', 'mockjax'],function () {	
	var wws = {};
	
	wws.initEditTable = function(tabId,type,tabData,tabLoadUrl,cellUpdateUrl){
		var response = [];
		var key;
		var value;

		if(type == "dmsn"){
			response.push({product:'',dimension:'',description:''});
		}else{
			response.push({entity:'',tags:''});
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
       	if(type == "dmsn"){
	    	$('#'+tabId).bootstrapTable({
	    		theadClasses: 'thead-light',
	    		url: tabLoadUrl,
	            striped: true,  
	            clickToSelect: true, 
	            singleSelect: false,
	            pagination: false,
	            editable: true,
	            columns: [
	            	{checkbox: true}, 
	                {field:'product',title:'产品',align:'left',
	                	formatter: function (value, row, index) {
	                        return "<a href=\"#\" data-name=\"product\" data-pk=\""+index+"\" data-title=\"Product\">" + value + "</a>";
	                    }},  
	                {field:'dimension',title:'维度',align:'left',
	                	formatter: function (value, row, index) {
	                        return "<a href=\"#\" data-name=\"dimension\" data-pk=\""+index+"\" data-title=\"Dimension\">" + value + "</a>";
	                    }},
	                {field:'description',title:'描述',align:'left',
	                	formatter: function (value, row, index) {
	                        return "<a href=\"#\" data-name=\"description\" data-pk=\""+index+"\" data-title=\"Description\">" + value + "</a>";
	                    }},
	                {title:'操作', align:"center", edit:false,width:'75',
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
       	}else{
	    	$('#'+tabId).bootstrapTable({
	    		theadClasses: 'thead-light',
	    		url: tabLoadUrl,
	            striped: true,  
	            clickToSelect: true, 
	            singleSelect: false,
	            pagination: false,
	            editable: true,
	            columns: [
	            	//{checkbox: true}, 
	                {field:'entity',title:'维度实体',align:'right',
	                	formatter: function (value, row, index) {
	                        return "<a href=\"#\" data-name=\"entity\" data-pk=\""+index+"\" data-title=\"entity\">" + value + "</a>";
	                    }},  
	                {field:'tags',title:'标签',align:'left',
	                	formatter: function (value, row, index) {
	                        return "<a href=\"#\" data-name=\"tags\" data-pk=\""+index+"\" data-title=\"tags\">" + value + "</a>";
	                    }},
	                {title:'操作', align:"center", edit:false,width:'75',
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
       	}
	}
	/*wws.initEditTableView = function(tabId,tabData,tabLoadUrl,cellUpdateUrl){
		var response = [];
    	if ($.isEmptyObject(tabData)){
    		response.push({product:'',dimension:''});
    	} else {
    		$.each(tabData,function(product,dimension){
    			response.push({'prouct':product,'dimension':dimension});
    		})
    	}
    	$.mockjax({ url: tabLoadUrl, logging: 0, responseText: response });
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
    	$('#'+tabId).bootstrapTable({  
    		url: tabLoadUrl,
            striped: true,  
            clickToSelect: false,  
            pagination: false,
            editable: true,
            columns: [  
                {field:'product',title:'Product',align:'right',width:'20%',
                	formatter: function (value, row, index) {
                        return value;
                    }},  
                {field:'dimension',title:'Dimension',align:'left',width:'70%',
                	formatter: function (value, row, index) {
                        return value;
                    }}
            ],
            onLoadSuccess: function (aa, bb, cc) {
            	$("#"+tabId+" a").editable({url:cellUpdateUrl});
            }
    	});
	}*/
	return wws;
});