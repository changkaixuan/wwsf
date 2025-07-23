define([
	'underScore', 
	'text!application/wws_user_log.html',
	'bootstrap-table',
	'bootstrap-table-locale'],
    function (_, template) {
        var app = function () {
            this.o_template = $(template);
        }
        _.extend(app.prototype, {
        	initialize:function(){
        		
            },
            load: function () {

            },
            render: function (container) {
                var self = this;
                var o_container = $(container);
                o_container.empty();
                o_container.append(this.o_template);
              
                //查询按钮
                $('#btn_userlog_query', this.o_container).bind('click', function(event){ 
                	$('#tb_userLog').bootstrapTable('refresh'); 
                });
                //重置按钮
                $('#btn_userlog_clear', this.o_container).bind('click', function(event){ 
                	$("input[name=modName]").val("");
                	$("input[name=optDateStart]").val("");
                	$("input[name=optDateEnd]").val("");
                	$("input[name=optName]").val("");
                	$("input[name=userId]").val("");
                	$("select[name=optRst]").val("");
                });
	          	
                // 初始化 用户日志表格
                self.initUserLogTable();
            }, 
            initUserLogTable: function(){
            	var self = this;
            	$('#tb_userLog').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'sysLog/pageList',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
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
                    	   	modName: $("input[name=modName]").val(),
                    	   	optDateStart: self.formateDate($("input[name=optDateStart]").val()),
                    	   	optDateEnd: self.formateDate($("input[name=optDateEnd]").val()),
                    	   	optName: $("input[name=optName]").val(),
                    	   	userId: $("input[name=userId]").val(),
                    	   	optRst: $("select[name=optRst]").val()
                    	};
                    },           //传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "id",                     //每一行的唯一标识，一般为主键列
                    showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                   //是否显示父子表
                    showColumns: true,
                    showFullscreen: true,
                    showRefresh: true,
                    columns: [
                    	// {checkbox: true}, 
                    //	{field: 'id', title: '日志编号'}, 
                   //   {field: 'logType', title: '日志类型'}, 
                        {field: 'modName', title: '模块名称', width:'135px;'},
                        {field: 'optName', title: '操作名称', width:'135px;'},
                        {field: 'userId', title: '操作人', align:'center', width:'90px;'}, 
                        {field: 'userUip', title: '操作IP', align:'center', width:'100px;'},
                        {field: 'optDate', title: '操作日期', align:'center', width:'135px;',
                        	formatter: function (value, row, index){
                        		if(/^\d{14}$/g.test(value)) {
                        			return value.substring(0,4)+"-"
                        					+value.substring(4,6)+"-"
                        					+value.substring(6,8)+" "
                        					+value.substring(8,10)+":"
                        					+value.substring(10,12)+":"
                        					+value.substring(12,14);
                        		}
                        		return value;
                        	}
                        }, 
                        {field: 'optRst', title: '操作结果', align:'center', width:'70px;'}, 
                        {field: 'logDesc', title: '日志描述'}
                    ],
                    responseHandler: function (res) {
                    	return res.data;
                    }
                });
            },
            formateDate : function(date) {
            	if(date && /^\d{4}-\d{2}-\d{2}$/g.test(date)) {
            		date = date.replace(/-/g,"");
            	}
            	return date;
            }
        });
        return app;
});