define([
	'underScore', 
	'text!application/wws_datasource.html',
	'app-editable',
	'alertor',
	'bootstrap-table',
	'css!js/jquery-editable/poshytip-1.2/src/tip-twitter/tip-twitter.css',
	'select2',
	'css!css/select2.min.css'],
    function (_, template, app_editable, alertor) {
        var app = function () {
            this.o_template = $(template);
            this.fal_agent_kid = "<i class='fal fa-key fa-fw' style='margin-right:2px'></i>";
            this.fal_agent_tag = "<i class='fal fa-tag fa-fw' style='margin-right:2px'></i>";
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
                //产品下拉
                var sel_prd = $("#query_product");
                var sel_prd_mdal = $("#dataSourceEditModal").find("select[name=product]");
	          	$.ajax({
  	        		async: false,
  	    			url: 'product/selectProduct',
  	    			type: 'GET',
  	    			contentType: 'application/json',
  	    			success: function(data){
  	    				if(data.success){
    						var products = data.data.products;
    						sel_prd.empty();
			                sel_prd_mdal.empty();
			                sel_prd.append("<option value=''>请选择产品</option>");
			                sel_prd_mdal.append("<option value=''>请选择产品</option>");
    						$.each(products,function(i){
    							sel_prd_mdal.append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
    							sel_prd.append("<option value='" + products[i].pId + "'>" + products[i].pName + "</option>");
    						});
  	    				}else{
  	    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
        					alertor.dangerAlert(msgInfo, 'middle');
  	    				}
  	    			},
  	    			error: function(msg){
  	    				alertor.dangerAlert('连接服务器错误', 'small');
  	    			}
	          	});
	          	self.init_agent_select2();
	          	sel_prd_mdal.bind('change', function(event){ self.flush_agent_select2(event); });
	          	//加载节点下拉框值
	          	var sel_agent = $('#query_agent');
	          	sel_agent.empty();
	          	sel_agent.append("<option value=''>请选择节点</option>");
	          	$.ajax({
	        		async:false,
	    			url : 'zoo/selectAgent',
	    			type : 'GET',
	    			contentType:'application/json',
	    			success: function(data){
	    				if(data.success){
	    					var agents = data.data.agents;
	    					$.each(agents,function(i){
	    						sel_agent.append("<option value='" + agents[i] + "'>" + agents[i] + "</option>");
	    					})
	    				}else{
	    					alertor.dangerAlert('获取节点信息失败' + (data.info ? '<br>' + data.info : ''), 'middle');
	    				}
	    			},
	    			error: function(msg){
	    				alertor.dangerAlert('连接服务器错误', 'small');
	    			}
	          	});
	          	//加载驱动类下拉框值
	          	var sel_driverName = $("#sel_driverName");
	          	$.ajax({
  	        		async: false,
  	    			url: 'dataSource/dsFactories',
  	    			type: 'GET',
  	    			contentType: 'application/json',
  	    			success: function(data){
  	    				if(data.success){
    						var dsFactories = data.data.dsFactories;
    						sel_driverName.empty();
    						for (var key in dsFactories) {
    							sel_driverName.append("<option value='" + key + "'>" + key + ' | ' + dsFactories[key] + "</option>");
    						}
  	    				}else{
  	    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        	        		if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	alertor.dangerAlert(msgInfo,'middle');
  	    				}
  	    			},
  	    			error: function(msg){
  	    				alertor.dangerAlert('连接服务器错误', 'small');
  	    			}
	          	});
	          	
                $('#btn_datasource_query', this.o_container).bind('click', function(event){ $('#tb_datasource').bootstrapTable('refresh'); });
                $('#btn_datasource_clear', this.o_container).bind('click', function(event){
                	$("#query_product").val("");
        	   		$("#query_agent").val("");
        	   		$("input[name='q_name']").val("")
                	$('#tb_datasource').bootstrapTable('refresh');
                });
                $('#btn_datasource_add', this.o_container).bind('click', function(event){self.edit(null);});
                $('#btn_datasource_edit', this.o_container).bind('click', function(event){
                	var result = $('#tb_datasource').bootstrapTable('getSelections');
                    if(result.length ==1){
                    	self.edit(result[0]);
                    }else{
                    	alertor.dangerAlert('请选中一条未在线的记录','small');
                    }
                });
                $('#btn_datasource_del', this.o_container).bind('click', function(event){ self.del(event); });
                $('#btn_datasource_create', this.o_container).bind('click', function(event){ self.create(event); });
                $('#btn_datasource_destroy', this.o_container).bind('click', function(event){ self.destroy(event); });
                self.initDataSourceTable();
            }, 
            flush_agent_select2: function(event){
            	var self = this;
            	var product_name = event.target.value;
            	var select_agent = $("#dataSourceEditModal").find("select[name=agent]");
            	if (product_name != null && product_name != '') {
                	self.destroy_agent_select2();
                	select_agent.select2({
                		dropdownParent: $("#dataSourceEditModal"),
                		allowClear: true,
                		multiple: true,
                     	ajax: {
                     		async: true,
                    		url: 'server/selectAgentScope?product='+product_name,	
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
            	           					item.text = self.fal_agent_kid + selectTwos[i].text;
            	           				}else{
            	           					item.text = self.fal_agent_tag + selectTwos[i].text;
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
            	} else {
            		self.destroy_agent_select2();
            		self.init_agent_select2();
            	}
            },
            init_agent_select2: function() {
            	var select_agent = $("#dataSourceEditModal").find("select[name=agent]");
            	select_agent.select2({});
            },
            destroy_agent_select2: function() {
            	var select_agent = $("#dataSourceEditModal").find("select[name=agent]");
        		select_agent.empty();
        		select_agent.select2("destroy");
            },
            initDataSourceTable: function(){
            	var self = this;
            	$('#tb_datasource').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'dataSource/list',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    toolbar: '#ds_tab_toolbar',                //工具按钮用哪个容器
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
                    	   		product: $("#query_product").val(),
                    	   		agent: $("#query_agent").val(),
                    	   		name: $("input[name='q_name']").val()
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
                    	{field: 'product', title: '产品', width: '100'}, 
                        {field: 'agent', title: '节点',
                    		formatter: function (value, row, index) {
	                    		if (value != null && value != '') {
	                    			var text = '';
	                    			var options = value.split(",");
		    		        		$.each(options,function(i){
		    		        			if (options[i] != null && options[i] != '') {
		    		        				if (options[i].startsWith("#")) {
		    		        					text = text + "<span class='badge badge-secondary'>" + self.fal_agent_tag + options[i].substring(1,options[i].length) + "</span><br/>";
		    			        			} else {
		    			        				text = text + "<span class='badge badge-secondary'>" + self.fal_agent_kid + options[i] + "</span><br/>";
		    			        			}
		    		        			}
		    						})
		    						return text;
	                    		}
					        } 
                        }, 
                        {field: 'name', title: '数据源名称' },
                        {field: 'onLineDesc', title: '是否在线', align: 'center',
                        	formatter: function (value, row, index) {
	                    		if(value == "Y1"){
	                    			return "<span class='badge badge-success'>是</span>";
	                    		}else if(value == "Y2"){
	                    			return "<span class='badge badge-warning'>是(*)</span>";
	                    		}else{
	                    			return "<span class='badge badge-danger'>否</span>";
	                    		}
					        } 
                        },
                        {field: 'runningon', title: '运行节点', 
                        	formatter: function (value, row, index) {
					    		var text = '';
	                    		if (value && value.length > 0) {
	                    			for (var i=0; i<value.length; i++){
	                    				text = text + "<span class='badge badge-success'>" + value[i] + "</span><br/>";
	                    			}
	                    		}
	                    		return text;
                        	}
                        },
                        {field: 'driverName', title: '驱动名称' },
                        {field: 'databaseType', title: '数据源类型' , width: '180'},
                        {field: 'jdbcUrl', title: 'URL'},
                        {field: 'jdbcUser', title: '用户名' },
                        {field: 'autoStart', title: '自动启动', align: 'center',
                        	formatter: function (value, row, index) {
	                    		if(value == 1){
	                    			return "是";
	                    		}else{
	                    			return "否";
	                    		}
					        }
                        }
                        /*,
                        {field: 'pool', title: '连接池', align: 'center' },
                        {field: 'xa', title: 'xa', align: 'center', 
                        	formatter: function (value, row, index) {
	                    		if(value == "true"){
	                    			return "是";
	                    		}else{
	                    			return "否";
	                    		}
					        }
                        }*/],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    onDblClickRow: function (row) {
                    	self.edit(row);
                    }
                });
            },
            edit : function(row){
            	var self = this;
            	var f = $('#dataSourceEditModal');
            	f.find("input[name=name]").removeAttr("disabled");
            	f.modal('show');
            	var modalType = "add";
            	if(null == row){
            		f.find("select[name=product]").val("").trigger('change');
            		f.find("input[type='text']").val("");
            		f.find("select[name=driverName]").val("");
            		f.find("select[name=databaseType]").val("DataSource");
                	f.find("input[name=jdbcPassword]").val("");
                	f.find("select[name=pool]").val("dbcp2");
                	f.find("input[name=autoStart]")[0].checked = true;
                	f.find("input[name=xa]")[0].checked = false;
            		app_editable.initEditTable("etab_ds_pool_properties",{},"/dsemTabPoolProperties","/updateCellDsemTabPoolProperties");
            	}else{
            		modalType = "edit";
            		$.ajax({
            			data : {product:row.product,agent:row.agent,name:row.name},
            			url : 'dataSource',
            			type : 'GET',
            			contentType:'application/x-www-form-urlencoded',
            			success: function(data){
            				if(data.success){
            					if (data.data.dataSourceConfig) {
            						var dsc = data.data.dataSourceConfig;
            			        	f.find("select[name=product]").val(dsc.product).trigger("change");
            			        	f.find("input[name=name]").val(dsc.name);
            			        	f.find("input[name=name]").attr("disabled",true);
            			        	var ds_sel_agent = f.find("select[name=agent]");
            			        	if(dsc.agent != null && dsc.agent != ""){
            			        		var avAgents = [];
            			        		var options = dsc.agent.split(",");
            			        		$.each(options,function(i){
            			        			if (options[i] != null && options[i] != '') {
            			        				if (options[i].startsWith("#")) {
                			        				ds_sel_agent.append(new Option(self.fal_agent_tag + options[i].substring(1,options[i].length), options[i]));
                			        			} else {
                			        				ds_sel_agent.append(new Option(self.fal_agent_kid + options[i], options[i]));
                			        			}
            			        				avAgents.push(options[i]);
            			        			}
            							})
	    			        		    ds_sel_agent.val(avAgents).trigger("change");
            			        	} else {
            			        		ds_sel_agent.val(null).trigger("change");
            			        	}
            			        	if(dsc.autoStart == 1){
            			        		f.find("input[name=autoStart]")[0].checked = true;
            				    	}else{
            				    		f.find("input[name=autoStart]")[0].checked = false;
            				    	}
            			        	f.find("select[name=driverName]").val(dsc.driverName);
            			        	f.find("select[name=databaseType]").val(dsc.databaseType);
            				    	f.find("input[name=jdbcUrl]").val(dsc.jdbcUrl);
            				    	f.find("input[name=jdbcUser]").val(dsc.jdbcUser);
            				    	f.find("input[name=jdbcPassword]").val(dsc.jdbcPassword);
            				    	f.find("select[name=pool]").val(dsc.pool);
            				    	if(dsc.xa == "true"){
            				    		f.find("input[name=xa]")[0].checked = true;
            				    	}else{
            				    		f.find("input[name=xa]")[0].checked = false;
            				    	}
            				    	if(row.poolProperties != undefined && row.poolProperties != "" && row.poolProperties != null){
            				    		app_editable.initEditTable("etab_ds_pool_properties",JSON.parse(row.poolProperties),"/dsemTabPoolProperties","/updateCellDsemTabPoolProperties");
            				    	}else{
            				    		app_editable.initEditTable("etab_ds_pool_properties",row.poolProperties,"/dsemTabPoolProperties","/updateCellDsemTabPoolProperties");
            				    	}
            					}
            				}else{
            					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
	        	        		if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
            						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
            					}
        	                	alertor.dangerAlert(msgInfo,'middle');
            				}
            			},
            			error: function(msg){
            				alertor.dangerAlert('连接服务器错误', 'small');
            			}
            		});
            	}
            	$('#dsem_btn_save').unbind('click').bind('click', function(event){ self.saveDataSource(event,modalType); });
            },
            saveDataSource : function(event,modalType){
            	var self = this;
            	var f = $('#dataSourceEditModal');
            	var formJson = {};
        		//formJson.nodeType = nType;
            	//product
        		formJson.product = f.find("select[name=product]").val();
        		/*if(null == formJson.product || formJson.product == ""){
                	alertor.dangerAlert('操作失败，产品不能为空','small');
        			return false;
                }*/
        		//agent
        		var agentArr = f.find("select[name=agent]").val();
        		/*if(null == formJson.agent || formJson.agent == ""){
                	alertor.dangerAlert('操作失败，节点不能为空','small');
        			return false;
                }*/
        		if(agentArr.length == 0){
        			formJson.agent = "";
	    		}else{
	    			var agents = "";
	    			for(var a = 0; a < agentArr.length; a++) { agents += "," + agentArr[a]}
	    			formJson.agent = agents + ",";
	    		}
        		
        		if(!formJson.product && formJson.agent) {
        			alertor.dangerAlert('操作失败，有节点时，产品不能为空', 'small');
        			return false;
        		}
        		if(!validateStringLength(formJson.agent,50)){
            		alertor.dangerAlert('操作失败，节点的长度不能超过50', 'small'); 
            		return false;
            	}
        		//name
        		formJson.name = f.find("input[name=name]").val();
        		if(null == formJson.name || formJson.name == ""){
                	alertor.dangerAlert('操作失败，数据源名称不能为空', 'small');
        			return false;
                }
        		if(!validateStringLength(formJson.name,50)){
            		alertor.dangerAlert('操作失败，数据源名称的长度不能超过50', 'small'); 
            		return false;
            	}
        		//autoStart
        		formJson.autoStart = 0;
        		if(f.find("input[name=autoStart]")[0].checked){
        			formJson.autoStart = 1;
        		}
        		//driverName
        		formJson.driverName = f.find("select[name=driverName]").val();
        		if(null == formJson.driverName || formJson.driverName == ""){
                	alertor.dangerAlert('操作失败，驱动不能为空', 'small');
        			return false;
                }
        		//databaseType
        		formJson.databaseType = f.find("select[name=databaseType]").val();
        		//jdbcUrl
        		formJson.jdbcUrl = f.find("input[name=jdbcUrl]").val();
        		if(null == formJson.jdbcUrl || formJson.jdbcUrl == ""){
                	alertor.dangerAlert('操作失败，URL不能为空', 'small');
        			return false;
                }
        		if(!validateStringLength(formJson.jdbcUrl,500)){
            		alertor.dangerAlert('操作失败，URL的长度不能超过500', 'small'); 
            		return false;
            	}
        		//jdbcUser
        		formJson.jdbcUser = f.find("input[name=jdbcUser]").val();
        		if(null == formJson.jdbcUser || formJson.jdbcUser == ""){
                	alertor.dangerAlert('操作失败，用户名不能为空', 'small');
        			return false;
                }
        		if(!validateStringLength(formJson.jdbcUser,50)){
            		alertor.dangerAlert('操作失败，用户名的长度不能超过50', 'small'); 
            		return false;
            	}
        		//jdbcPassword
        		formJson.jdbcPassword = f.find("input[name=jdbcPassword]").val();
        		if(null == formJson.jdbcPassword || formJson.jdbcPassword == ""){
                	alertor.dangerAlert('操作失败，密码不能为空', 'small');
        			return false;
                }
        		if(!validateStringLength(formJson.jdbcPassword,50)){
            		alertor.dangerAlert('操作失败，密码的长度不能超过50', 'small'); 
            		return false;
            	}
        		//pool
        		formJson.pool = f.find("select[name=pool]").val();
        		if(null == formJson.pool || formJson.pool == ""){
                	alertor.dangerAlert('操作失败，连接池类型不能为空', 'small');
        			return false;
                }
        		//xa
        		formJson.xa = "false";
        		if(f.find("input[name=xa]")[0].checked){
        			formJson.xa = "true";
        		}
        		//poolProperties
        		var poolPropertiesJson = {};
            	var propertiesObj = $('#etab_ds_pool_properties').bootstrapTable("getData");
            	if(propertiesObj.length > 0){
            		var kAndVs = self.getKeyAndValues(propertiesObj);
            		if(kAndVs.errorInfo != ""){
            			alertor.dangerAlert(kAndVs.errorInfo, 'small');
            			return false;
            		}
            		if(!jQuery.isEmptyObject(kAndVs.data)){
            			poolPropertiesJson = kAndVs.data;
        			}
            		formJson.poolProperties = JSON.stringify(poolPropertiesJson);
            	}
        		var url = "dataSource/save";
        		if(modalType == "edit"){
        			url = "dataSource/update";
        		}
        		$.ajax({
        			url:url,
                    type:"POST",
                    processData:false,
                    data:JSON.stringify(formJson),
                    contentType:'application/json',
        			success: function(data){
        				if(data.success){
        					$('#dataSourceEditModal').modal('hide');
        					alertor.successAlert('保存成功','small');
        					$('#tb_datasource').bootstrapTable('refresh');
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        	        		if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	alertor.dangerAlert(msgInfo,'middle');
        				}
        			},
        			error: function(msg){
        				alertor.dangerAlert('连接服务器错误', 'small');
        			}
        		});
            },
            del : function(event){
            	var selected = $('#tb_datasource').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var rows = [];
            		_.each(selected, function(element, index){
            			rows.push(element);
            		});
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : alertor.warningMessage('是否确定删除所选数据源？'),
                		callback : function (result) {
                			if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			url : 'dataSource',
                        			type : 'DELETE',
                        			contentType:'application/json',
                        			data:JSON.stringify(rows),
                        			success: function(data){
                        				if(data.success){
                        					alertor.successAlert('删除成功','small');
                        					$('#tb_datasource').bootstrapTable('refresh');
                        				}else{
                        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
            	        	        		if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                        					}
                    	                	alertor.dangerAlert(msgInfo,'middle');
                        				}
                        			},
                        			error: function(msg){
                        				alertor.dangerAlert('连接服务器错误', 'small');
                        			}
                        		});
                    		}
                    	 }
                	});
            	}else{
            		alertor.dangerAlert('请选择需要删除的数据源','small');
            	}
            },
            create : function(event){
            	var selected = $('#tb_datasource').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var rows = [];
            		_.each(selected, function(element, index){
            			if (element.onLineDesc != "Y1"){
            				rows.push(element);
            			}
            		});
            		if(rows.length == 0){
            			alertor.dangerAlert('请至少选中一条未在线的记录','small');
                		return false;
                	}
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : alertor.warningMessage('是否确定创建所选数据源？'),
                		callback : function (result) {
                			if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			data:JSON.stringify(rows),
                        			url : 'dataSource/create',
                        			type : 'PUT',
                        			contentType:'application/json',
                        			success: function(data){
                        				if(data.success){
                        					alertor.successAlert('请求已提交，请在数据源界面查看状态','small');
                        					$('#tb_datasource').bootstrapTable('refresh');
                        				}else{
                        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
            	        	        		if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                        					}
                    	                	alertor.dangerAlert(msgInfo,'middle');
                        				}
                        			},
                        			error: function(msg){
                        				alertor.dangerAlert('连接服务器错误', 'small');
                        			}
                        		});
                    		}
                    	 }
                	});
            	}else{
            		alertor.dangerAlert('请至少选中一条未在线的记录', 'small');
            	}
            },
            destroy : function(event){
            	var selected = $('#tb_datasource').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var rows = [];
            		_.each(selected, function(element, index){
            			if (element.onLineDesc == "Y1"){
            				rows.push({'product':element.product,'agent':element.agent,'name':element.name});
            			}
            		});
            		if(rows.length == 0){
                		alertor.dangerAlert('请至少选中一条在线的记录', 'small');
                		return false;
                	}
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : alertor.warningMessage('是否确定销毁所选数据源？'),
                		callback : function (result) {
                			if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			data:JSON.stringify(rows),
                        			url : 'dataSource/destroy',
                        			type : 'PUT',
                        			contentType:'application/json',
                        			success: function(data){
                        				if(data.success){
                        					alertor.successAlert('请求已提交，请在数据源界面查看状态','small');
                        					$('#tb_datasource').bootstrapTable('refresh');
                        				}else{
                        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
            	        	        		if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                        					}
            	        	        		alertor.dangerAlert(msgInfo, 'middle');
                        				}
                        			},
                        			error: function(msg){
                        				alertor.dangerAlert('连接服务器错误', 'small');
                        			}
                        		});
                    		}
                    	 }
                	});
            	}else{
            		alertor.dangerAlert('请至少选中一条在线的记录', 'small');
            	}
            },
            getKeyAndValues: function(obj){
            	var retJson = {"errorInfo":"","data":""};
            	var dataJson = {};
        		for(var i=0;i<obj.length;i++){
        			if(obj[i].key == "" && obj[i].value != ""){
    					retJson.errorInfo = "第"+(i+1)+"行,属性不能为空";
    					break;
    				}
    				if(obj[i].key != "" && obj[i].value == ""){
    					retJson.errorInfo = "第"+(i+1)+"行,值不能为空";
    					break;
    				}
    				if(obj[i].key != "" && obj[i].value != ""){
    					dataJson[obj[i].key] = obj[i].value;
    				}
        		}
        		if(retJson.errorInfo == ""){
        			retJson.data = dataJson;
        		}
            	return retJson;
            }
        });
        return app;
});