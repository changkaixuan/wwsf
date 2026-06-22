define([
		'underScore',
		'echarts', 
		'text!application/wws_batch_sview.html', 
		'mloadding', 
		'select2',
		'css!css/select2.min.css', 
		'bootstrap-table',
		'bootstrap-table-locale'],
    function (_, echartsAll, template, mloadding) {
        var app = function (config) {
            this.o_template = $(template);
            this.chartTaskState = null;
            this.chartTaskLabelState = null;
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
                o_container.append(self.o_template);
                //加查询条件：产品下拉数据
                $('#qBatchNo').select2({"width":"260","placeholder":"请选择批量"});
                var sel_prd = $('#qProduct');
                sel_prd.select2({minimumResultsForSearch : -1,"width":"240","placeholder":"请选择产品"});
	          	$.ajax({
	  	    			url : 'product/selectProduct',
	  	    			type : 'GET',
	  	    			contentType:'application/json',
	  	    			beforeSend : function(){
	        				mloadding.showLoadding();
	        			},
	        			complete : function(){
	        				mloadding.hideLoadding();
	        			},
	  	    			success: function(data){
	  	    				if(data.success){
  	    						var products = data.data.products;
  	    						$.each(products,function(i){
  	    							sel_prd.append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
  	    						})
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
	          	sel_prd.bind('change', function(event){ 
	          		self.productChange(); 
	          	});
                //查询按钮绑定click事件
             	$("#btn_sbs_query").bind("click",function(){ 
             		var product = $('#qProduct').val();
        	   		var batchNo = $('#qBatchNo').val();
        	   		var queryFlag = true;
        	   		if(null == product || product == ""){
        	   			bootbox.alert({message:'操作失败，产品不能为空',size:'small'});
        	   			return;
        	   		}
        	   		if(queryFlag && (null == batchNo || batchNo == "")){
        	   			bootbox.alert({message:'操作失败，批量不能为空',size:'small'});
        	   			return;
        	   		}
    	   			//Task实例状态统计
    	   			self.chartTaskState.setSeries(self.getSeriesTaskState(product, batchNo));
    	   			//Task实例标签状态统计
    	   			self.chartTaskLabelState.dispose();
    	   			self.chartTaskLabelState = echarts.init(document.getElementById("chartTaskLabelState"));
    	   			self.chartTaskLabelState.setOption(self.getOptionTaskLabelState(product,batchNo),true);
    	   			
    	   			//Job实例
    	   			$('#tb_jobInstanceTable').bootstrapTable('refresh'); 
    	   			//产品在线节点列表
    	   			$('#tb_nodeTable').bootstrapTable('refresh');
             	});
             	//重置按钮绑定click事件
                $('#btn_sbs_clear').bind('click', function(event){
                	if (sel_prd.val() != "") {
	                	sel_prd.val("");
                		sel_prd.change();
                	}
                });
                
                //从Job实例表中，取最新Job实例对应的产品和批次
                $.ajax({
  	    			url: 'batch/infowithproduct',
  	    			type: 'GET',
  	    			contentType: 'application/json',
  	    			beforeSend : function(){
        				mloadding.showLoadding();
        			},
        			complete : function(){
        				mloadding.hideLoadding();
        			},
  	    			success: function(data){
  	    				if(data.success){
    						$('#qProduct').val(data.data.product);
    						$('#qProduct').change();
    		                $('#qBatchNo').val(data.data.batch);
                            //初始化Task实例状态图
			                self.chartDrawing($('#qProduct').val(),$('#qBatchNo').val());
			                //初始化Job实例表格
			                self.initJobInstanceTable();
			                // 初始化节点信息表格
			                self.initNodeTable();
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
                
                //图形等比例缩放
		        $(window).resize(function() {
		        	if (self.chartTaskState != null) self.chartTaskState.resize();
		        	if (self.chartTaskLabelState != null) self.chartTaskLabelState.resize();
		        });
            },
            chartDrawing : function(product,batch){
            	var self = this;
            	//Task实例状态统计
            	self.chartTaskState = echarts.init(document.getElementById("chartTaskState"));
            	self.chartTaskState.setOption(self.getOptionTaskState(product,batch),true);
                //Task实例标签状态统计
            	self.chartTaskLabelState = echarts.init(document.getElementById("chartTaskLabelState"));
            	self.chartTaskLabelState.setOption(self.getOptionTaskLabelState(product,batch),true);
            },
            getOptionTaskState: function(product,batch){
            	var self = this;
            	return {
            		tooltip : {
        		        //show: false, //关闭悬浮提示
        		        trigger: 'item',
        		        formatter:"{a} <br/>{b}:{c} ({d}%)" //浮动窗口显示内容
        		    },
        		    color:['#868e96','#505050','#2196F3','pink','#666ab5','#fd3995','blueviolet','#886ab5','#1dc9b7'],
        	        legend: { //条目名称：垂直布局，做对齐，居中
        	        	orient:'vertical',
        	        	x:'left',
        	        	y:'center',
	        	        data:['初始化','准备执行','正在执行','出错延迟中','执行结果返回','失败','需人工干预','执行延迟中','成功']
	        	    },
        		    calculable : true,
        		    series: self.getSeriesTaskState(product,batch)
        		}
            },
            getSeriesTaskState: function(product,batch){
            	var tData = [];
            	$.ajax({
  	        		async: false,
  	    			url: 'batch/chart/series?product='+product+'&batch='+batch,
  	    			type: 'GET',
  	    			contentType: 'application/json',
  	    			beforeSend : function(){
        				mloadding.showLoadding();
        			},
        			complete : function(){
        				mloadding.hideLoadding();
        			},
  	    			success: function(data){
  	    				if(data.success){
  	    					tData = data.data.chartNameValueList;
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
            	return [{
            		name:'Task实例状态',
		            type:'pie',
		            radius:['50%','70%'], //控制圆内径和半径
		            center:["60%","50%"], //控制圆位置
		            itemStyle:{
		            	normal:{
		            		label:{
		            			show:false
		            		},
		            		labelLine:{
		            			show:false
		            		}
		            	},
		            	emphasis:{
		            		label:{
		            			show:true,
		            			position:'center',
		            			textStyle:{
		            				fontSize:'24', //控制圆中间字体大小
		            				fontWeight:'bold' //控制圆中间字体
		            			}
		            	        /*
		            	        formatter：鼠标移动到圆中条目时，圆中间显示内容；  默认值： {a}，{a}指 条目名称
		            			formatter:function(selObj){
		            				return selObj.seriesName+"\r\n "+selObj.name+":"+selObj.value+" ("+selObj.percent+"%)";
		            			}
		            			formatter:'{c}'
		            			*/
		            		}
		            	}
		            },
		            data:tData
            	}]
            },
            //Task实例标签状态统计
            getOptionTaskLabelState : function(product, batch) {
            	var option = {
            		tooltip : {
         		        trigger: 'axis'
         		    },
         		    color:['#ffc241','#1dc9b7','#fd3995'],
         		    // 图例
         		    legend: {
        	          x:'center',
        	          padding:30,
         			  data:['总计','成功','失败']
	        	    },
        		    calculable : true,
        		    grid : {y:70, y2:30, x2:20},
        		    xAxis: [{
        		    	type : 'category',
        		    	data : []
        		    },{
        		    	type : 'category',
        		    	axisLine : {show:false},// 坐标轴箭头
        		    	axisTick : {show:false},
        		    	axisLabel : {show:false},
        		    	splitArea : {show:false},
        		    	splitLine : {show:false},// y轴分割线
        		    	data : []
        		    }],
        		    yAxis : [{
        		    	type : 'value'
        		    }],
        		    series : [{
        		    	name : '总计',
        		    	type : 'bar',
        		    	itemStyle : {
        		    		normal : {
        		    			label : {show:true}
        		    		}
        		    	},
        		    	data : []
        		    }, {
        		    	name : '成功',
        		    	type : 'bar',
        		    	itemStyle : {
        		    		normal : {
        		    			label : {
        		    				show : true
        		    			}
        		    		}
        		    	},
        		    	data : []
        		    }, {
        		    	name : '失败',
        		    	type : 'bar',
        		    	itemStyle : {
        		    		normal : {
        		    			label : {
        		    				show : true
        		    			}
        		    		}
        		    	},
        		    	data : []
        		    }]
            	};
            	
            	var titleArr = [];
            	var totalArr = [];
            	var successArr = [];
            	var failArr = [];
            	$.ajax({
  	        		async:false,
  	    			url : 'taskIns/getTaskByBatchAndProduct',
  	    			data: {product:product, batch:batch},
  	    			type : 'GET',
  	    			contentType:'application/json',
  	    			beforeSend : function(){
        				mloadding.showLoadding();
        			},
        			complete : function(){
        				mloadding.hideLoadding();
        			},
  	    			success: function(data){
  	    				if(data.success){
  	    					// x轴横坐标
  	    					var titleArr = data.data.titleList;
  	    					option.xAxis[0].data = titleArr;
  	    					option.xAxis[1].data = titleArr;
  	    					//console.log(titleArr);
  	    					// 基于标签的Task状态统计数据
  	    					for (var i=0; i<titleArr.length; i++) {
                				var titlei = titleArr[i];
  	    						totalArr.push(data.data[titlei][0]);
  	    						successArr.push(data.data[titlei][1]);
  	    						failArr.push(data.data[titlei][2]);
  	    					}
  	    					option.series[0].data = totalArr; // 总计
  	    	            	option.series[1].data = successArr; // 成功
  	    	            	option.series[2].data = failArr; // 失败
  	    					
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
            	
             return option;
            },
            initJobInstanceTable: function(){
            	var self = this;
            	$('#tb_jobInstanceTable').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'batch/chart/jobs',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    //toolbar: '#toolbar',                //工具按钮用哪个容器
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
                	   		product: $("#qProduct").val(),
                	   		batch: $("#qBatchNo").val() 
                       };
                    },           //传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "jobInsId",               //每一行的唯一标识，一般为主键列
                    showToggle:false,                   //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-default',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                  //是否显示父子表
                    showColumns: false,
                    showFullscreen: false,
                    showRefresh: false,
                    columns: [
                    	{field: 'jobId', title: 'Job编号'}, 
                    	{field: 'name', title: 'Job名称'},
                        {field: 'jobInsId', title: 'Job实例编号'},
                        {field: 'mode', title: '调度模型', 
                        	formatter:function(value,row,index){
                        		return value==1?"网状调度":(value==2?"网维调度":"未知模式");
                            }
                        },
                        {field: 'state', title: '状态',
                        	formatter:function(value,row,index){
                        		return self.getStateDesc(value);
                            }
                        },
                        {field: 'pause', title: '是否暂停',
                        	formatter:function(value,row,index){
                        		return value==1?"是":"否";
                            }
                        },
                        {field: 'creator', title: '创建人' },
                        {field: 'version', title: '版本' },
                        {field: 'initTime', title: '初始化时间' },
                        {field: 'beginTime', title: '开始时间' },
                        {field: 'endTime', title: '结束时间' }
                    ],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    ajaxOptions: {
						beforeSend : function(){
	        				mloadding.showLoadding();
	        			},
	        			complete : function(){
	        				mloadding.hideLoadding();
	        			}
                    }
                });
            },
            getStateDesc:function(stateValue){
            	if(stateValue == "0"){
        			return "暂停";
        		}else if(stateValue == "1"){
        			return "初始化";
        		}else if(stateValue == "2"){
        			return "正在运行";
        		}else if(stateValue == "9"){
        			return "强制执行结束";
        		}else if(stateValue == "10"){
        			return "执行结束";
        		}else{
        			return "未知";
        		}
            },
            initNodeTable:function() {
            	$('#tb_nodeTable').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'zoo/getNodeInfo',         
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
                	   		product: $('#qProduct').val()
                       };
                    },          
                    sidePagination: "server",           
                    pageNumber:1,                       
                    pageSize: 20,                      
                    pageList: [20, 30, 50, 100],       
                    minimumCountColumns: 2,           
                    clickToSelect: true,              
                    uniqueId: "nodeId",             
                    showToggle:false,                  
                    buttonsClass: 'sm btn-default',
                    cardView: false,                    
                    detailView: false,                 
                    showColumns: false,
                    showFullscreen: false,
                    showRefresh: false,
                    columns: [
                    	{field: 'product', title: '产品'}, 
                    	{field: 'nodeId', title: '节点'}, 
                    	{field: 'type', title: '节点类型', align:'center'}, 
                    	{field: 'online', title: '主节点', align:'center',
                    		formatter: function (value, row, index) {
                    			if(value == "-1"){ 
                    				return "";
                    			}
                    			return value;
                    		}
                    	},
                    	{field: 'ip', title: 'IP地址'}, 
                    	{field: 'sshdPort', title: 'SSHD端口', align:'center'}, 
                    	{field: 'rmiRegistryPort', title: 'RMI服务注册端口', align:'center'},
                    	{field: 'rmiServerPort', title: 'RMI服务器端口', align:'center'}
                    ],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    ajaxOptions: {
						beforeSend : function(){
	        				mloadding.showLoadding();
	        			},
	        			complete : function(){
	        				mloadding.hideLoadding();
	        			}
                    }
                });
            },
            productChange : function(){
            	var sel_batch = $('#qBatchNo');
            	sel_batch.empty();
            	sel_batch.append("<option value=''>请选择批量</option>");
              	$.ajax({
            		async:false,
        			url : 'batch/select?product='+$('#qProduct').val(),
        			type : 'GET',
        			contentType:'application/json',
        			beforeSend : function(){
        				mloadding.showLoadding();
        			},
        			complete : function(){
        				mloadding.hideLoadding();
        			},
        			success: function(data){
        				if(data.success){
        					var batchs = data.data.batchs;
        					$.each(batchs,function(i){
        						sel_batch.append("<option value='" + batchs[i].BATCH + "'>" + batchs[i].BATCH + "</option>");
        					})
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
              	//$('#tb_jobInstanceTable').bootstrapTable('refresh');
              	//$('#tb_nodeTable').bootstrapTable('refresh');
            }
        });
        return app;
});