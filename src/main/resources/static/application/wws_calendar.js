define(['underScore', 
	'text!application/wws_calendar.html',
	'text!application/wws_cron_expression_modal.html', 
	'expression',
	'moment',
	'daterangepicker',
	'mloadding',
	'bootstrap-table',
	'x-editable',
	'mockjax'],
    function (_, template, expressionModal, exp, moment, daterangepicker, mloadding) {
        var app = function () {
            this.o_template = $(template);
            this.o_expressionModal = $(expressionModal);
            this.saveList = [];
            this.cancelList = [];
        }
        _.extend(app.prototype, {
        	initialize:function(){
        		
            },
            load: function () {
            	
            },
            render:function(container){
            	var self = this;
                var o_container = $(container);
                o_container.empty();
                o_container.append(this.o_template);
                o_container.append(this.o_expressionModal);
                // 初始化假期编排表格
                self.initCalendarTable();
                // 加载产品下拉列表
                var proSelect1 = $("#calendar_products");
                var proSelect2 = $("#tab_calendar_info").find("select[name=product]");
                var proSelArr = [proSelect1, proSelect2];
                self.loadProducts(proSelArr);
                // 新增假期编排按钮绑定事件
                $('#btn_add_calendar', this.o_container).bind('click', function(event){ 
                	self.showCalendarPlanModal(null); 
                });
                // 修改假期编排按钮绑定事件
                $('#btn_edit_calendar', this.o_container).bind('click', function(event){ 
                	var result = $('#calendar_tab').bootstrapTable('getSelections');
                    if(result.length ==1){
                    	self.showCalendarPlanModal(result[0]);
                    }else{
                    	bootbox.alert({message:'请选中一条记录',size:'small'});
                    }
                });
                // 查询假期编排按钮绑定事件
                $('#tb_calendar_search', this.o_container).bind('click', function(event){
                	self.calendarQuery();
                });
                //重置查询条件
                $('#tb_calendar_clear', this.o_container).bind('click', function(event){
                	$("#calendarSearchForm").find("select[name=qProduct]").val("");
                	$("#calendarSearchForm").find("input[name=qCalName]").val("");
                });
                // 删除假期计划
                $('#btn_delete_calendar', this.o_container).bind('click', function(event){
                	self.deleteCalPlan();
                });
            },
            calendarQuery:function(){
            	$('#calendar_tab').bootstrapTable('refresh');
            },
            loadProducts:function(proSelArr){
            	$.ajax({
            		async:false,
        			url : 'product/selectProduct',
        			type : 'GET',
        			contentType:'application/json',
        			success: function(data){
        				if(data.success){
        						var products = data.data.products;
        						for(var inx=0,len=proSelArr.length; inx<len; inx++) {
        							proSelArr[inx].empty();
        							proSelArr[inx].append("<option value=''>请选择产品</option>");
        							$.each(products,function(i){
        								proSelArr[inx].append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
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
            },
            initCalendarTable: function(){
            	var self = this;
            	$('#calendar_tab').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'calendar/calendarPlanListPage',         
                    method: 'get',                     
                    toolbar: '#tab_calendar_toolbar',               
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
                    	   		product: $("select[name='qProduct']").val(),
                    	   		calName: $("input[name='qCalName']").val()
                            };
                        },           
                    sidePagination: "server",           
                    pageNumber:1,                      
                    pageSize: 20,                    
                    pageList: [20, 30, 50, 100],       
                    minimumCountColumns: 2,             
                    clickToSelect: true,                
                    uniqueId: "id",                    
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
                    	{field: 'calName', title: '名称'}, 
                    	{field: 'calDescription', title: '描述'},
                    	{field: 'includeValue', title: '工作日',
                    		formatter: function (value, row, index){
                    			var daysStr = '';
                        		if(value && value.length > 0) {
                        			var coupDays = value.split("|");
                        			coupDays.some((item, index) => {
                        				if (index > 3) {
                        					daysStr = daysStr + item + "(" + coupDays.length + ")......";
                        					return true;
                        				} else {
                        					daysStr = daysStr + item + "<br>";
                        				}
                        			});
                        			
                        		}
                        		return daysStr;
                        	}
                    	},
                    	{field: 'exclusionValue', title: '非工作日(优先排除)',
                    		formatter: function (value, row, index){
                    			var daysStr = '';
                        		if(value && value.length > 0) {
                        			var coupDays = value.split("|");
                        			coupDays.some((item, index) => {
                        				if (index > 3) {
                        					daysStr = daysStr + item + "(" + coupDays.length + ")......";
                        					return true;
                        				} else {
                        					daysStr = daysStr + item + "<br>";
                        				}
                        			});
                        		}
                        		return daysStr;
                        	}
                    	}
                    ],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    onDblClickRow: function (row) {
                    	self.showCalendarPlanModal(row); 
                    }
                });
            },
            showCalendarPlanModal : function(row) {
            	var self = this;
            	var includeDataArr = [];
            	var exclusionDataArr = [];
            	$("#tab_calendar_info").find("select[name=product]").removeAttr("disabled");
        		$("#tab_calendar_info").find("input[name=calName]").removeAttr("disabled");
            	if(row != null){
            		$("#tab_calendar_info").find("select[name=product]").val(row.product);
            		$("#tab_calendar_info").find("select[name=product]").attr("disabled",true);
            		$("#tab_calendar_info").find("input[name=calName]").val(row.calName);
            		$("#tab_calendar_info").find("input[name=calName]").attr("disabled",true);
            		$("#tab_calendar_info").find("textarea[name=calDescription]").val(row.calDescription);
            		$.ajax({
            			async:false,
            			data : {product:row.product,calName:row.calName},
            			url : 'calendar/queryCalendar',
            			type : 'GET',
            			contentType:'application/x-www-form-urlencoded',
            			success: function(data){
            				data = JSON.parse(data);
            				if(data.success){
            					if (data.data.calendar) {
            						var calendar = data.data.calendar;
            						if(calendar.includeValue) {
            							var tIncludeDataArr = calendar.includeValue.split("|");
            							for(var i=0;i<tIncludeDataArr.length;i++){
            								if(tIncludeDataArr[i].split(":")[0] == "d"){
            									tIncludeDataArr[i].split(":")[1]
            									if(tIncludeDataArr[i].split(":")[1].length == 8){//tIncludeDataArr[i]值：    d:20190101  改为  d:20190101 - 20190101
            										includeDataArr[i] = tIncludeDataArr[i] + "-" + tIncludeDataArr[i].split(":")[1];
            									}else{
            										includeDataArr[i] = tIncludeDataArr[i];
            									}
            								    continue;
            								}
            								includeDataArr[i] = tIncludeDataArr[i];
            							}
            						}
            						if(calendar.exclusionValue) {
            							var tExclusionDataArr = calendar.exclusionValue.split("|");
            							for(var i=0;i<tExclusionDataArr.length;i++){
            								if(tExclusionDataArr[i].split(":")[0] == "d"){
            									tExclusionDataArr[i].split(":")[1]
            									if(tExclusionDataArr[i].split(":")[1].length == 8){//tExclusionDataArr[i]值：    d:20190101  改为  d:20190101 - 20190101
            										exclusionDataArr[i] = tExclusionDataArr[i] + "-" + tExclusionDataArr[i].split(":")[1];
            									}else{
            										exclusionDataArr[i] = tExclusionDataArr[i];
            									}
            								    continue;
            								}
            								exclusionDataArr[i] = tExclusionDataArr[i];
            							}
            						}
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
            	}else {
            		$("#tab_calendar_info").find("select[name=product]").val("");
            		$("#tab_calendar_info").find("input[name=calName]").val("");
            		$("#tab_calendar_info").find("textarea[name=calDescription]").val("");
            	}
            	//包含
            	self.initIncOrExcTable("tab_calendar_include_edit_table",includeDataArr,"/tabCalendarIncludeEditTable","/updateCellTabCalendarIncludeEditTable");
            	//排除
            	self.initIncOrExcTable("tab_calendar_exclusion_edit_table",exclusionDataArr,"/tabCalendarExclusionEditTable","/updateCellTabCalendarExclusionEditTable");
            	
            	$('#modal_calendar_edit').modal('show');
            	$('#save_cal_plan').unbind('click').bind('click', function(event){ 
            		self.saveCalPlan(row); 
                });
            },
            initIncOrExcTable : function(tableId,tabDataArr,useUrl,useChildUrl){
            	var self = this;
				var response = [];
				if ($.isEmptyObject(tabDataArr)){
					response.push({type:'1',value:''});
				} else {
					$.each(tabDataArr,function(index,value){
						var tType = "1";
						if(value.split(":")[0] == "c"){
							tType = "2";
						}
						response.push({type:tType,value:value.split(":")[1]});
					});
				}
				$.mockjax.clear(useUrl);
	          	$.mockjax({ url: useUrl, logging: 0, responseText: response});
	          	$.mockjax.clear(useChildUrl);
	           	$("#"+tableId).bootstrapTable('destroy');	
	          	$("#"+tableId).bootstrapTable({ 
	          		theadClasses: 'thead-light',
	        		url: useUrl,
	                striped: true,  
	                clickToSelect: false,  
	                pagination: false,
	                editable: true,
	                columns: [  
	                	{field:'type',title:'类型',align:'center',width:'15%',                    	  
	                    	formatter: function (value, row, index) {
	                    		//return "<select id='"+tableId+"_s_"+index+"' style='width:100%;height:23px;'><option value='1'>日期</option><option value='2'>表达式</option></select>";
	                    		return "<input type='radio' name='"+tableId+"_t_"+index+"' value='1'>日期<input type='radio' name='"+tableId+"_t_"+index+"' value='2'>表达式";
	                        }
	                    },
	                    {field:'value',title:'值',align:'center',width:'75%',                    	  
	                    	formatter: function (value, row, index) {
	                    		return "<input type='text' style='width:100%;' id='"+tableId+"_di_"+index+"' value='"+value+"'><input type='hidden' style='width:100%;' id='"+tableId+"_ci_"+index+"' value='"+value+"'>";
	                        }
	                    },  
	                    {title:'操作', align:"center", edit:false,width:'10%',
	                    	events:{
	                        	'click .tab_jip_removerow_style': function(e, value, row, index) {
	                           		self.loadInputEvent(tableId,"removeRow",index,null);
	                        	},
	                        	'click .tab_jip_appendrow_style': function(e, value, row, index) {
	                       		 	self.loadInputEvent(tableId,"appendRow",null,null);
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
	                onLoadSuccess: function (dataArr) {
	                	self.loadInputEvent(tableId,"init",null,dataArr);
	                }
	        	});
            },
            loadInputEvent : function(tabId,type,delIndex,dataArr){//delIndex：删除用参数      dataArr：初始化用参数
            	if(type == "init"){
       			    var tabRowArr = $("#"+tabId).bootstrapTable('getData');
        			var tLength = dataArr.length - tabRowArr.length;
        			for(var i=0;i<tLength;i++){
        				$("#"+tabId).bootstrapTable('appendRow'); //追加行后，会自动清理数据
        			}
        			for(var i=0;i<dataArr.length;i++){
        				$("#"+tabId).find("input[name="+tabId+"_t_"+i+"]").change(function(curRadio){
        					var curRadioName = curRadio.target.name;
        					var index = curRadioName.substring(curRadioName.lastIndexOf("_")+1);
            				if(curRadio.target.value == "1"){//日期
            					$("#"+tabId+"_di_"+index).attr("type","text");
            					$("#"+tabId+"_ci_"+index).attr("type","hidden");
            				}else{
            					$("#"+tabId+"_di_"+index).attr("type","hidden");
            					$("#"+tabId+"_ci_"+index).attr("type","text");
            				}
                        });
        				//日期输入框值
        				$("#"+tabId+"_di_"+i).daterangepicker({
        	    			singleDatePicker : false,
        	    			locale : {
        	    				format : 'YYYYMMDD',
        	    				separator: '-'
        	    			}
        	    		});
                		//表达式输入框值
                		$("#"+tabId+"_ci_"+i).unbind('click').bind('click', function(event){ 
            				exp.initExpression(this.id,this.value,exp.displayDWMYTabArr);
            			});
                		//赋值
                		if(dataArr[i].type == "1"){//选中日期
                			$("#"+tabId+"_di_"+i).val(dataArr[i].value);
            				$("#"+tabId+"_ci_"+i).val("");
            				$("#"+tabId).find("input[name="+tabId+"_t_"+i+"]").eq(0).click(); //选中日期
                		}else{
                			$("#"+tabId+"_di_"+i).val("");
            				$("#"+tabId+"_ci_"+i).val(dataArr[i].value);
            				$("#"+tabId).find("input[name="+tabId+"_t_"+i+"]").eq(1).click(); //选中表达式
                		}
        			}
            	}else if(type == "appendRow"){
            		//保存源表格数据
	            	var rowArr = [];
	            	var tabRowArr = $("#"+tabId).bootstrapTable('getData');
	            	for(var i=0;i<tabRowArr.length;i++){
	            		var tType = $("#"+tabId).find("input[name="+tabId+"_t_"+i+"]:checked").val();
	            		var tDi = $("#"+tabId+"_di_"+i).val();
	            		var tCi = $("#"+tabId+"_ci_"+i).val();
	            		rowArr[i] = {type:tType, di:tDi, ci:tCi};
	            	}
	            	var tLength = tabRowArr.length + 1;
	            	$("#"+tabId).bootstrapTable('appendRow'); //追加行后，会自动清理数据
	            	//表格重新赋值
	            	for(var i=0;i<tLength;i++){
	            		$("#"+tabId).find("input[name="+tabId+"_t_"+i+"]").change(function(curRadio){
        					var curRadioName = curRadio.target.name;
        					var index = curRadioName.substring(curRadioName.lastIndexOf("_")+1);
            				if(curRadio.target.value == "1"){//日期
            					$("#"+tabId+"_di_"+index).attr("type","text");
            					$("#"+tabId+"_ci_"+index).attr("type","hidden");
            				}else{
            					$("#"+tabId+"_di_"+index).attr("type","hidden");
            					$("#"+tabId+"_ci_"+index).attr("type","text");
            				}
                        });
        				//日期输入框值
        				$("#"+tabId+"_di_"+i).daterangepicker({
        	    			singleDatePicker : false,
        	    			locale : {
        	    				format : 'YYYYMMDD',
        	    				separator: '-'
        	    			}
        	    		});
                		//表达式输入框值
                		$("#"+tabId+"_ci_"+i).unbind('click').bind('click', function(event){ 
            				exp.initExpression(this.id,this.value,exp.displayDWMYTabArr);
            			});
                		//赋值
                		if(i != tLength-1){//非最后一行
	                		$("#"+tabId+"_di_"+i).val(rowArr[i].di);
	        				$("#"+tabId+"_ci_"+i).val(rowArr[i].ci);
	        				if(rowArr[i].type == 1){
	        					$("#"+tabId).find("input[name="+tabId+"_t_"+i+"]").eq(0).click(); //选中日期
	        				}else{
	        					$("#"+tabId).find("input[name="+tabId+"_t_"+i+"]").eq(1).click(); //选中表达式
	        				}
                		}else{//最后一行
                			$("#"+tabId+"_di_"+i).val("");
	        				$("#"+tabId+"_ci_"+i).val("");
	        				$("#"+tabId).find("input[name="+tabId+"_t_"+i+"]").eq(0).click(); //选中日期
                		}
	            	}
            	}else if(type == "removeRow"){
            		//保存源表格数据
	            	var rowArr = [];
	            	var tabRowArr = $("#"+tabId).bootstrapTable('getData');
	            	var rowIndex = 0;
	            	for(var i=0;i<tabRowArr.length;i++){
	            		if(delIndex != i){
		            		var tType = $("#"+tabId).find("input[name="+tabId+"_t_"+i+"]:checked").val();
		            		var tDi = $("#"+tabId+"_di_"+i).val();
		            		var tCi = $("#"+tabId+"_ci_"+i).val();
		            		rowArr[rowIndex] = {type:tType, di:tDi, ci:tCi};
		            		rowIndex++;
	            		}
	            	}
	            	var tLength = tabRowArr.length - 1;
	            	$("#"+tabId).bootstrapTable('removeRow',delIndex); //删除行后，会自动清理数据
	            	//表格重新赋值
	            	if(tLength == 0){//删除最后一行
	            		$("#"+tabId).find("input[name="+tabId+"_t_0]").change(function(curRadio){
        					var curRadioName = curRadio.target.name;
        					var index = curRadioName.substring(curRadioName.lastIndexOf("_")+1);
            				if(curRadio.target.value == "1"){//日期
            					$("#"+tabId+"_di_"+index).attr("type","text");
            					$("#"+tabId+"_ci_"+index).attr("type","hidden");
            				}else{
            					$("#"+tabId+"_di_"+index).attr("type","hidden");
            					$("#"+tabId+"_ci_"+index).attr("type","text");
            				}
                        });
        				//日期输入框值
        				$("#"+tabId+"_di_0").daterangepicker({
        	    			singleDatePicker : false,
        	    			locale : {
        	    				format : 'YYYYMMDD',
        	    				separator: '-'
        	    			}
        	    		});
                		//表达式输入框值
                		$("#"+tabId+"_ci_0").unbind('click').bind('click', function(event){ 
            				exp.initExpression(this.id,this.value,exp.displayDWMYTabArr);
            			});
                		//赋值
                		$("#"+tabId+"_di_0").val("");
        				$("#"+tabId+"_ci_0").val("");
        				$("#"+tabId).find("input[name="+tabId+"_t_0]").eq(0).click(); //选中日期
	            	}
	            	for(var i=0;i<tLength;i++){
	            		$("#"+tabId).find("input[name="+tabId+"_t_"+i+"]").change(function(curRadio){
        					var curRadioName = curRadio.target.name;
        					var index = curRadioName.substring(curRadioName.lastIndexOf("_")+1);
            				if(curRadio.target.value == "1"){//日期
            					$("#"+tabId+"_di_"+index).attr("type","text");
            					$("#"+tabId+"_ci_"+index).attr("type","hidden");
            				}else{
            					$("#"+tabId+"_di_"+index).attr("type","hidden");
            					$("#"+tabId+"_ci_"+index).attr("type","text");
            				}
                        });
        				//日期输入框值
        				$("#"+tabId+"_di_"+i).daterangepicker({
        	    			singleDatePicker : false,
        	    			locale : {
        	    				format : 'YYYYMMDD',
        	    				separator: '-'
        	    			}
        	    		});
                		//表达式输入框值
                		$("#"+tabId+"_ci_"+i).unbind('click').bind('click', function(event){ 
            				exp.initExpression(this.id,this.value,exp.displayDWMYTabArr);
            			});
                		//赋值
                		$("#"+tabId+"_di_"+i).val(rowArr[i].di);
        				$("#"+tabId+"_ci_"+i).val(rowArr[i].ci);
        				if(rowArr[i].type == 1){
        					$("#"+tabId).find("input[name="+tabId+"_t_"+i+"]").eq(0).click(); //选中日期
        				}else{
        					$("#"+tabId).find("input[name="+tabId+"_t_"+i+"]").eq(1).click(); //选中表达式
        				}
	            	}
            	}
            },
            saveCalPlan : function(row) {
            	var self = this;
            	var calendar = {};
            	var modal = $('#modal_calendar_edit');
            	// 获取产品
            	var product = modal.find("select[name=product]").val();
            	if(!product) {
            		bootbox.alert({message:'操作失败，产品不能为空',size:'small'});
            		return;
            	}
            	calendar.product = product;
            	// 获取名称
            	var calName = modal.find("input[name=calName]").val();
            	if(!calName) {
            		bootbox.alert({message:'操作失败，名称不能为空',size:'small'});
            		return;
            	}
            	if(!validateStringLength(calName,50)){
            		bootbox.alert({message:'操作失败，名称的长度不能超过50',size:'small'}); 
            		return false;
            	}
            	calendar.calName = calName;
            	// 获取描述
            	calendar.calDescription = modal.find("textarea[name=calDescription]").val();
            	if(!validateStringLength(calendar.calDescription,200)){
            		bootbox.alert({message:'操作失败，描述的长度不能超过200',size:'small'}); 
            		return false;
            	}
            	// 获取包含值
            	calendar.includeValue = self.collectExpList("tab_calendar_include_edit_table");
            	// 获取排除值
            	calendar.exclusionValue = self.collectExpList("tab_calendar_exclusion_edit_table");
            	if(!calendar.includeValue && !calendar.exclusionValue) {
            		bootbox.alert({message:'操作失败，包含值和排除值不能同时为空',size:'small'});
            		return;
            	}
            	if(!mloadding.showLoadding()){
            		return false;
            	}
            	var url = row==null ? "calendar/calendarPlanAdd" : "calendar/calendarPlanEdit";
            	$.ajax({
            		async:false,
        			url:url,
                    type:"POST",
                    processData:false,
                    data:JSON.stringify(calendar),
                    contentType:'application/json',
        			success: function(data){
        				if(data.success){
        					bootbox.alert({message:'保存成功',size:'small'});
        					$('#calendar_tab').bootstrapTable('refresh');
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        	        		if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	bootbox.alert({message:msgInfo, size:'middle'});
        				}
        				mloadding.hideLoadding();
        			},
        			error: function(msg){
        				bootbox.alert({message:'连接服务器错误',size:'small'});
        				mloadding.hideLoadding();
        			}
        		});
            	modal.modal('hide');
            },
            collectExpList : function(tableId) {
            	var arr = $("#"+tableId+"").find("tr");
            	var expArr = [];
            	for(var i=0,len=arr.length; i<len; i++) {
            		var tr = $(arr[i]);
            		var radio = tr.find("input:radio:checked");
            		if(!radio) {
            			continue;
            		}
            		switch($(radio).val()) {
	            		case "1":// 日期
	            			var dateVal = tr.find("input[id^="+tableId+"_di_]").val();
	            			if(dateVal) {
	            				if(/^(\d{8})-\1$/g.test(dateVal)) {
	            					dateVal = dateVal.split("-")[0];
	            				}
	            				dateVal = "d:"+dateVal;
	            				if(expArr.indexOf(dateVal) == -1) {
	            					expArr.push(dateVal);
	            				}
	            			}
	            			break;
	            		case "2":// 表达式
	            			var cronVal = tr.find("input[id^="+tableId+"_ci_]").val();
	            			if(cronVal) {
	            				expArr.push("c:"+cronVal);
	            			}
	            			break;
            		} 
            	}
            	return expArr.join("|");
            },
            deleteCalPlan : function() {
            	var selected = $('#calendar_tab').bootstrapTable('getSelections');
            	if(selected.length<1) {
            		bootbox.alert({message:'请选择需要删除的假期计划',size:'small'});
            		return;
            	}
            	var proArr = [];
            	var calNameArr = [];
        		_.each(selected, function(element, index){
        			proArr.push(selected[index].product);
        			calNameArr.push(selected[index].calName);
        		});
            	var tConfirm = true;
        		bootbox.confirm({
            		size : 'small',
            		message : '是否确定删除所选假期计划？',
            		callback : function (result) {
            			if(result && tConfirm){
            				tConfirm = false;
                    		$.ajax({
                    			url : 'calendar/calendarPlanDel',
                    			type : 'DELETE',
                    			dataType : 'json',
                    			data:{proArr:proArr.toString(), calNameArr:calNameArr.toString()},
                    			success: function(data){
                    				if(data.success){
                    					bootbox.alert({message:'删除成功',size:'small'});
                    					$('#calendar_tab').bootstrapTable('refresh');
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
            }
        });
        
        return app;
});