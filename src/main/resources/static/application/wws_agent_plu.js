define(['underScore', 
		'text!application/wws_agent_plu.html', 
		'mloadding', 
		'alertor',
		'bootstrap-table',
		'bootstrap-typeahead',
		'bootstrap-table-locale',
		'mockjax'],
    function (_, template, mloadding, alertor) {
        var app = function () {
            this.o_template = $(template);
        }
        _.extend(app.prototype, {
        	initialize:function(){
        		
            },
            load: function () {

            },
            
            redisPluginQuery:function(){
            	$('#tb_plugin').bootstrapTable('refresh');
            },
            render: function (container) {
                var self = this;
                var o_container = $(container);
                o_container.empty();
                o_container.append(this.o_template);
                //plugin信息
                self.initPluginTable();
                
                $('#btn_plugin_info_search', this.o_container).bind('click', function(event){
                	self.redisPluginQuery();
                });
    			
			    $('#btn_plugin_info_reset', this.o_container).bind('click', function(event){
     	   		    $("input[name=pluginName]").val("");
                });
			    
                $('#btn_plugin_info_flush', this.o_container).bind('click', function(event){
                	self.flushPluginInfo();
                });
            }, 
            flushPluginInfo:function(){
            	$.ajax({
  	    			url : 'plu/flushPluginInfo',
  	    			type : 'GET',
  	    			contentType:'application/json',
  	    			success: function(data){
  	    				var ctext = '';
  	    				var dataj = JSON.parse(data);
  	    				if(dataj.success){
  	    					if (dataj.data && dataj.data != null) {
  	    						var rs = JSON.parse(dataj.data);
  	    						for (var p in rs) {
  	    							var pname = p;
  	    							var pfr = rs[p];
  	    							if ('ok' == pfr) {
  	    								ctext = ctext + '<div class="alert alert-success mb-0 py-1 mt-1">[√] ' + pname + '</div>';
  	    							} else {
  	    								ctext = ctext + '<div class="alert alert-danger mb-0 py-1 mt-1">[×] ' + pname + '</div>';
  	    							}
  	    						}
  	    					}
  	    					if ('' == ctext) {
  	    						alertor.alert('<pre>操作成功，但未同步任何插件信息</pre>','small');
  	    					} else {
  	    						alertor.alert('<pre>' + ctext + '</pre>','large');
  	    					}
  	    				}else{
  	    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	alertor.alert(msgInfo,'large');
  	    				}
  	    			},
  	    			error: function(msg){
  	    				alertor.alert('连接服务器错误','small');
  	    			}
          	});
            },
            appendParamTable:function(name){
            	$('#tb_plugin_param_defini').bootstrapTable("destroy");
            	$('#tb_plugin_param_defini').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'plu/parameters',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    striped: true,                      //是否显示行间隔色
                    cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                    pagination: false,                   //是否显示分页（*）
                    sortable: false,                     //是否启用排序
                    sortOrder: "asc",                   //排序方式
                    queryParamsType:'undefined',
                    queryParams: function (params) { return { pluginName:name }; },
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "name",                     //每一行的唯一标识，一般为主键列
                    showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-primary',
                    columns: [
                    	{field: 'name', title: '参数名称' },
                    	{field: 'dataType', title: '参数类型' },
                    	{field: 'value', title: '默认值' },
                    	{field: 'required', title: '是否必填',
                    		formatter:function(value,row,rowIndex){ 
                    			return value 
                    				? "<span class='badge badge-danger'>是</span>" 
                    					: "<span class='badge badge-success'>否</span>"; 
                    		}
                    	},
                    	{field: 'desc', title: '参数描述',
                    		formatter:function(value,row,rowIndex){
                    			var ctext = value;
                    			if (row.dataRange) {
                    				var obj = $.parseJSON(row.dataRange);
                    				for (var i=0; i<obj.length; i++) {
                    					ctext = ctext + '<br>[ ' + obj[i].key + ' ]>>>>>>' + obj[i].value;
                    				}
                    			}
                    			return ctext; 
                    		}}
                        ],
                    responseHandler: function (res) {
                    	return res.data;
                    }
                });
            },
            initPluginTable: function(){
            	var self = this;
            	$('#tb_plugin').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'pluginAndAgent/pluginList',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    toolbar: '#tb_plugin_toolbar',                //工具按钮用哪个容器
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
                    	   		product: self.product,
                    	   		pluginName:$("input[name='pluginName']").val()//,
                            };
                        },           //传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "name",                     //每一行的唯一标识，一般为主键列
                    showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                   //是否显示父子表
                    showColumns: true,
                    showFullscreen: true,
                    showRefresh: true,
                    columns: [
                    	{checkbox: true}, 
                    	{field: 'name', title: '插件名称' },
                    	 {title:'插件参数', align:"center", edit:false, width:'65',
                        	events:{
                            	'click .tab_pluginParm_style': function(e, value, row, index) {
                            		$("#modal-redisPlugin-param").modal('show');
                            		$("#redisPluginTitleParam")[0].innerHTML = row.name;
                            		self.appendParamTable(row.name);
                            	}
                            },
                        	formatter:function(value,row,rowIndex){  
                            	return '<button type="button" class="btn btn-xs btn-outline-info tab_pluginParm_style">查看</button>';
                        	}
                        },
                    	{field: 'desc', title: '插件名称' },
                    	{field: 'feature', title: 'Feature名称' },
                    	{field: 'version', title: '版本号', align:"center" }
                        ],
                    responseHandler: function (res) {
                    	return res.data;
                    }
                });
            }
            
        });
        return app;
});