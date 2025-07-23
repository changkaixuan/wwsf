define(['underScore', 
		'text!application/wws_dimension.html', 
		'mloadding', 
		'dimension-editable', 
		'bootstrap-table',
		'bootstrap-typeahead', 
		'bootstrap-tagsinput',
		'css!js/bootstrap/css/bootstrap-tagsinput.css'],
    function (_, template, mloadding, dmsn) {
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
                var sel_prd = $('#query_product');
                sel_prd.empty();
                sel_prd.append("<option value=''>请选择产品</option>");
	          	$.ajax({
	  	        		async:false,
	  	    			url : 'product/selectProduct',
	  	    			type : 'GET',
	  	    			contentType:'application/json',
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
                //维度主界面事件
                $('#btn_dmsn_query', this.container).bind('click', function(event){ self.dmsnQuery(); })
                $('#btn_dmsn_add', this.o_container).bind('click', function(event){ self.dmsnAdd(); });
                $('#btn_dmsn_remove', this.o_container).bind('click', function(event){ self.dmsnDel(); });
                $('#btn_dmsn_import', this.o_container).bind('click', function(event){ self.dmsnImport(); });
                $('#btn_dmsn_save_import', this.o_container).bind('click', function(event){ self.dmsnSaveImport(); });
                $('#btn_dmsn_export', this.o_container).bind('click', function(event){ self.dmsnExport(); });
                
                //维度实体界面事件
            	$('#btn_entities_query').bind('click', function(event){ self.entitiesQuery(); });
            	$('#tb_dmsn_entities_add').bind('click', function(event){ self.entitiesAdd("query"); }); //从维度实体查询界面进入添加维度实体
            	$('#tb_dmsn_entities_del').bind('click', function(event){ self.entitiesDel(); });
            	$('#tb_dmsn_entities_tags_add').bind('click', function(event){ self.batchTagsAddOrDel("add"); }); 
            	$('#tb_dmsn_entities_tags_del').bind('click', function(event){ self.batchTagsAddOrDel("del"); });
            	$('#dmsnModal').bind('hidden.bs.modal', function(event){ self.dmsnModalClearAndUpdate(); });
            	$('#input_tag').bind('change', function(event){ self.entitiesQuery(); });
            	
            	//增加维度界面事件
            	$('#btn_entity_add').bind('click', function(event){ self.entitiesAdd("add"); }); //从维度添加界面进入添加维度实体
            	$('#btn_dmsn_save').bind('click', function(event){ self.dmsnSave(); });

				//增加维度实体界面事件
            	$('#btn_entity_save').bind('click', function(event){ self.entitiesSave(); });
                self.initDmsnTable();
                
                /*$("#tagAddModal").find("input[name=tags]").tagsinput({
    				typeahead : {
    					selectOnBlur: false,
    					changeInputOnSelect: false,
    					autoSelect: false,
    					source : function(query){
    						return [];
    					}
    				}
    			});*/
                
                $('#btn_dmsn_clear', this.o_container).bind('click', function(event){
                	$("select[name=product]").val("");
        	   		$("input[name=dimension]").val("");
                });
                
                $('#btn_entities_clear', this.o_container).bind('click', function(event){
                	$("select[name=input_tag]").val("");
        	   		$("input[name=input_entity]").val("");
                });
            }, 
            
            //刷新tag标签Select选项列表
            updateTagSelect: function(param){
				$.ajax({
					data: param,
					url: 'dmsn/tags',
					type: 'GET',
					contentType: 'application/json',
					success: function(data){
						if(data.success){
	                         $('#input_tag').empty();
	                         $('#input_tag').append("<option value=''>请选择标签</option>");
		                     var ddl = $("#input_tag");
		                     //转成Json对象
		                     var result = eval(data.data.tags);
		                     //循环遍历 下拉框绑定
		                     $.each(result, function (index, tag) {
		                         $('#input_tag').append("<option value='"+tag+"'>"+tag+"</option>")
		                     });

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
            
            //维度列表
            initDmsnTable: function(){
            	var self = this;
            	$('#tb_dimension').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'dmsn/list',             //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    toolbar: '#tab_dimension_toolbar',                //工具按钮用哪个容器
                    striped: true,                      //是否显示行间隔色
                    cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                    pagination: true,                   //是否显示分页（*）
                    sortable: false,                    //是否启用排序
                    sortOrder: "asc",                   //排序方式
                    queryParamsType:'undefined',
                    queryParams: function (params) {
                       return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                    	   		pageNumber: params.pageNumber,   //页面大小
                    	   		pageSize: params.pageSize,       //页码
                    	   		product: $('#query_product').val(),
                    	   		dmsn: $("input[name='dimension']").val()
                            };
                        },           					//传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "dimension",              //每一行的唯一标识，一般为主键列
                    showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                  //是否显示父子表
                    showColumns: true,
                    showFullscreen: true,
                    showRefresh: true,
                    columns: [
                    	{checkbox: true}, 
                    	{field: 'product', title: '产品'}, 
                        {field: 'name', title: '维度'},
                        {field: 'description', title: '描述'}],
                    responseHandler: function (res) {
                    	return res.data;
                    },
                    onDblClickRow: function (row) {
                    	$('#dmsnModal').modal();
                    	$("span[name=e_product]").text(row.product);
        				$("span[name=e_dimension]").text(row.name);
                    	self.initDmsnEntityTable(row);

                    	//设置tag标签输入选项
                    	var param={"product":row.product,"dimension":row.name};
                    	self.updateTagSelect(param);
                    }
                });
            },
            
            //维度实体列表
            initDmsnEntityTable: function(row){
            	var self = this;
            	if(self.tb_dmsn_entities){
            		$('#tb_dmsn_entities').bootstrapTable("destroy");
            	}
            	self.tb_dmsn_entities = $('#tb_dmsn_entities').bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'dmsn/entity',            //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    toolbar: '#tb_dmsn_entities_bar',   //工具按钮用哪个容器
                    striped: true,                      //是否显示行间隔色
                    cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                    pagination: true,                   //是否显示分页（*）
                    sortable: true,                    //是否启用排序
                    sortOrder: "asc",                   //排序方式
                    queryParamsType:'undefined',
                    queryParams: function (params) {
                       return {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                    	   		pageNumber: params.pageNumber,   //页面大小
                    	   		pageSize: params.pageSize,       //页码
                    	   		product: row.product,			 //product
                    	   		dmsn: row.name,		             //dimension
                    	   		p_entity: $("input[name='input_entity']").val(),
                    	   		p_tag: $("#input_tag option:selected").val()
                            };
                        },          			 		//传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    pageNumber:1,                       //初始化加载第一页，默认第一页
                    pageSize: 20,                       //每页的记录行数（*）
                    pageList: [20, 30, 50, 100],        //可供选择的每页的行数（*）
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    uniqueId: "entity",                 //每一行的唯一标识，一般为主键列
                    showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                  //是否显示父子表
                    showColumns: true,
                    showFullscreen: false,
                    showRefresh: true,
                    columns: [
                    	{checkbox: true}, 
                        {field: 'entity', title: '维度实体'},
                        {field: 'tags', title: '标签' }],
                    responseHandler: function (res) {return res.data;},
                });
            },
            
            //增加维度
            dmsnAdd: function(){
            	$('#dmsnAddModal').modal();
            	dmsn.initEditTable("tb_dmsn_add","dmsn","","/properties","/updateCellProperties");
            },
            
            //保存维度
            dmsnSave: function(){
            	var allDmsnData = $('#tb_dmsn_add').bootstrapTable('getData');
    	   		if(allDmsnData.length > 0){
    	   			var dmsns = [];
    	   			var nullDmsn = false; //存在维度栏位为空
    	   			var nullLine = null;
    	   			var duplicateDmsn = false; //存在重复维度名
    	   			var valPassFlag = true;
            		_.each(allDmsnData, function(element, index){
            			if(typeof element.product == "undifined" || element.product == null || element.product == ""
            				|| typeof element.dimension == "undifined" || element.dimension == null || element.dimension == ""){
            				nullDmsn = true;
            				nullLine = index + 1;
            			}else{
                    		_.each(dmsns, function(curr, index){ //判断实体名是否重复
                    			if(curr.product == element.product && curr.dimension == element.dimension){
                    				duplicateDmsn = true;
                    			}
                    		})
                    		if(!validateStringLength(element.product,50)){
                        		bootbox.alert({message:'操作失败，第'+(index+1)+'行，产品的长度不能超过50',size:'small'}); 
                        		valPassFlag = false;
                        		return false;
                        	}
                    		if(!validateStringLength(element.dimension,50)){
                        		bootbox.alert({message:'操作失败，第'+(index+1)+'行，维度的长度不能超过50',size:'small'}); 
                        		valPassFlag = false;
                        		return false;
                        	}
                    		if(!validateStringLength(element.description,200)){
                        		bootbox.alert({message:'操作失败，第'+(index+1)+'行，描述的长度不能超过200',size:'small'}); 
                        		valPassFlag = false;
                        		return false;
                        	}
                    		if(duplicateDmsn == false){
                    			dmsns.push({"product":element.product, "dimension": element.dimension, "description": element.description});
                    		}else{
                    			return false; //相当于break；
                    		}
            			}
            		});
            		if(valPassFlag){
	            		if(dmsns.length > 0){
	            			if(nullDmsn == false){
	            				if(duplicateDmsn == false){
	            					if(!mloadding.showLoadding()){
	            	            		return false;
	            	            	}
			    					$.ajax({
			    						data: JSON.stringify(dmsns),
			    						url: 'dmsn/save',
			    						type: 'POST',
			    						contentType: 'application/json',
			    					    traditional: true,
			    						success: function(data){
			    							if(data.success){
			    								bootbox.alert({message:'保存维度成功', size: 'small'});
			                					$('#tb_dimension').bootstrapTable('refresh');
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
	            				}else{
	            					bootbox.alert({message:'存在重复维度', size:'small'});
	            				}
		            		}else{
		            			bootbox.alert({message:'存在维度为空, 所在行：' + nullLine, size:'small'});
		            		}
	            		}else{
	            			bootbox.alert({message:'没有可保存的维度!',size:'small'});
	            		}
            		}
    	   		}else{
        			bootbox.alert({message:'请填写维度信息!',size:'small'});
    	   		}
            },
            
            //清理模态框输入数据
            dmsnModalClearAndUpdate: function(){
            	//清理方案： 按元素逐个清空
            	$("[name='input_entity']").val("");
            	$("[name='input_tag']").find("option").remove();
            	$("#tb_dimension").bootstrapTable("refresh");
            },
            
            //查询维度实体
            entitiesQuery: function(){
            	$('#tb_dmsn_entities').bootstrapTable("refresh");
            },
            
            //增加维度实体
            entitiesAdd: function(cmdSource){
            	if(cmdSource == 'add'){ //添加维度界面，添加实体
            		var tabId = 'tb_dmsn_add';
                	var selected = $('#'+tabId).bootstrapTable('getSelections');
                	if(selected.length == 1){
                		var product = selected[0].product;
                		var dimension = selected[0].dimension;
                		if(typeof(product) == "undefined" || product == null || product == ""
                			||typeof(dimension) == "undefined" || dimension == null || dimension == ""){
                			bootbox.alert({message:'请填写完整维度信息后再添加维度实体！',size:'small'});
                		}else{
                			if(!mloadding.showLoadding()){
                        		return false;
                        	}
                			$.ajax({
                    			data : {product:product,dmsn:dimension},
                    			url : 'dmsn/existsDmsn',
                    			type : 'GET',
                    			contentType:'application/x-www-form-urlencoded',
                    			success: function(data){
                    				if(data.success){
                    					if (data.data.isExistsDmsn == "Y") {
                    						var prod_dmsn={"product":product,"dimension":dimension}
                                        	$('#entityAddModal').modal();
                                        	$("span[name=ea_product]").text(product);
                            				$("span[name=ea_dimension]").text(dimension);
                                        	dmsn.initEditTable("tb_entity_add","entity",prod_dmsn,"/properties","/updateCellProperties");
                    					}else{
                    						bootbox.alert({message:'产品['+product+']维度['+dimension+']不存在',size:'small'});
                    					}
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
                		}
                	}else if(selected < 1){
                		bootbox.alert({message:'请选择维度',size:'small'});
                	}else{
                		bootbox.alert({message:'请选择一个维度',size:'small'});
                	}
            	}else{//维度详细信息界面，添加实体
        	   		var product = $("span[name='e_product']").html().trim();
        	   		var dimension =$("span[name='e_dimension']").html().trim();
            		var prod_dmsn={"product":product,"dimension":dimension};
                	$('#entityAddModal').modal();
                	$("span[name=ea_product]").text(product);
    				$("span[name=ea_dimension]").text(dimension);
                	dmsn.initEditTable("tb_entity_add","entity",prod_dmsn,"/properties","/updateCellProperties");
            	}
            },
            
            //保存维度实体
            entitiesSave: function(){
            	var self = this;
    	   		var product = $("span[name='ea_product']").html().trim();
    	   		var dimension =$("span[name='ea_dimension']").html().trim();
    	   		
    	   		var allEntityData = $('#tb_entity_add').bootstrapTable('getData');
    	   		if(allEntityData.length > 0){
    	   			var entities = [];
    	   			var nullEntity = false; //存在实体名为空
    	   			var nullLine = null;
    	   			var duplicateEntity = false; //存在重复实体名
    	   			var valPassFlag = true;
            		_.each(allEntityData, function(element, index){
            			if(typeof element.entity == "undifined" || element.entity == null || element.entity == ""){
            				nullEntity = true;
            				nullLine = index + 1;
            			}else{
                    		_.each(entities, function(curr, index){ //判断实体名是否重复
                    			if(curr.entity == element.entity){
                    				duplicateEntity = true;
                    			}
                    		});
                    		if(!duplicateEntity){
                    			var reg1 = new RegExp("[\\\\/:*?\"< >|#\\\[\\\]]");   //不能包含字符（\ / : * ? " < > | # [ ]）
                    			var reg2 = /^[0-9a-zA-Z_]+$/;
                        		if(reg1.test(element.entity)){
                        			bootbox.alert({message:'操作失败，维度实体不能包含字符（\\ / : * ? " < > | # [ ]）',size:'small'});
                        			valPassFlag = false;
                        			return false;
                        		}
                        		if(!validateStringLength(element.entity,50)){
                            		bootbox.alert({message:'操作失败，维度实体的长度不能超过50',size:'small'}); 
                            		valPassFlag = false;
                            		return false;
                            	}
                        		if(null != element.tags && element.tags != "" && !reg2.test(element.tags)){
                        			bootbox.alert({message:'操作失败，标签只能为数字字母或者下划线',size:'small'});
                        			valPassFlag = false;
                        			return false;
                        		}
                        		if(!validateStringLength(element.tags,200)){
                            		bootbox.alert({message:'操作失败，标签的长度不能超过50',size:'small'}); 
                            		valPassFlag = false;
                            		return false;
                            	}
                				entities.push({"product":product, "dimension": dimension, "entity":element.entity, "tags":element.tags});
                    		}else{
                    			return false; //相当于break；
                    		}
            			}
            		});
            		if(valPassFlag && entities.length > 0){
            			if(nullEntity == false){
            				if(duplicateEntity == false){
            					if(!mloadding.showLoadding()){
            	            		return false;
            	            	}
		    					$.ajax({
		    						data: JSON.stringify(entities),
		    						url: 'dmsn/entity/save',
		    						type: 'POST',
		    						contentType: 'application/json',
		    						success: function(data){
		    							if(data.success){
		    								bootbox.alert({message:'保存维度实体成功', size: 'small'});
		                					$('#tb_dmsn_entities').bootstrapTable('refresh');
		                                	//设置tag标签输入选项
		                					var param={"product":product, "dimension":dimension};
		                                	self.updateTagSelect(param);
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
            				}else{
            					bootbox.alert({message:'存在维度实体名重复', size:'small'});
            				}
	            		}else{
	            			bootbox.alert({message:'存在维度实体为空, 所在行：' + nullLine, size:'small'});
	            		}
            		}else{
            			if(valPassFlag){
            				bootbox.alert({message:'没有可保存的实体!',size:'small'});
            			}
            		}
    	   		}else{
        			bootbox.alert({message:'请填写实体信息!',size:'small'});
    	   		}
            },
            
            //删除维度实体
            entitiesDel: function(){
            	var self = this;
    	   		var product = $("span[name='e_product']").text().trim();
    	   		var dimension =$("span[name='e_dimension']").text().trim();
            	
            	var selected = $('#tb_dmsn_entities').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var entities = [];
            		_.each(selected, function(element, index){
            			entities.push(product + ":" + dimension + ":" + element.entity + ":" + element.tags)
            		});
            		var tConfirm = true;
            		bootbox.confirm({
            			size: 'small',
            			message: '是否确定删除所选维度实体？', 
            			callback: function(result) {
            				if(result && tConfirm){
                				tConfirm = false;
            					$.ajax({
            						url: 'dmsn/delEntity/' + entities.join('|'),
            						type: 'DELETE',
            						contentType: 'application/json',
            						success: function(data){
            							if(data.success){
            								bootbox.alert({message:'删除维度实体成功', size: 'small'});
                        					$('#tb_dmsn_entities').bootstrapTable('refresh');
		                                	//设置tag标签输入选项
		                					var param={"product":product, "dimension":dimension};
		                                	self.updateTagSelect(param);
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
            	}else{
            		bootbox.alert({message:'请选择需要删除的维度实体!',size:'small'});
            	}
            },

            //删除维度
            dmsnDel : function(){
            	var selected = $('#tb_dimension').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var dmsns = [];
            		_.each(selected, function(element, index){
            			dmsns.push(element.product + ':' + element.name);
            		});
            		var tConfirm = true;
            		bootbox.confirm({
                		size : 'small',
                		message : '是否确定删除所选维度吗？',
                		callback : function (result) {
                    		if(result && tConfirm){
                				tConfirm = false;
                        		$.ajax({
                        			url : 'dmsn/' + dmsns.join('|'),
                        			type : 'DELETE',
                        			contentType:'application/json',
                        			success: function(data){
                        				if(data.success){
                        					bootbox.alert({message:'删除成功',size:'small'});
                        					$('#tb_dimension').bootstrapTable('refresh');
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
            	     //
            	} else{
            		bootbox.alert({message:'请选择需要删除的维度',size:'small'});
            	}
            },
            
          //显示批量增加或删除维度实体标签
            batchTagsAddOrDel: function(cmdSource){
            	var self = this;
            	var product = $('#entity_query_form').find("span[name=e_product]")[0].innerHTML;
        		var dimension = $('#entity_query_form').find("span[name=e_dimension]")[0].innerHTML;
        		var selected = $('#tb_dmsn_entities').bootstrapTable('getSelections');
        		var selectEntity = "";
        		if(selected.length > 0){
        			var dmsns = [];
            		_.each(selected, function(element, index){
            			dmsns.push(element.entity);
            		});
            		selectEntity = dmsns.join(',');
        		}
        		var tEntity = $('#entity_query_form').find("input[name=input_entity]").val();
        		var tTag = $('#entity_query_form').find("select[name=input_tag]").val();
            	$.ajax({
            		async:false,
        			data : {product:product,dimension:dimension,entity:tEntity,tag:tTag,selectEntity:selectEntity},
        			url : 'dmsn/entity/getDmsnEntity',
        			type : 'GET',
        			contentType:'application/x-www-form-urlencoded',
        			success: function(data){
        				if(data.success){
        					if (data.data.dmsnEntitys != "") {
        						if(cmdSource == "add"){
        		        	    	$('#tagAddModal').find("label[name=tagsTitle]")[0].innerHTML = "批量新增标签";
        		            	}else{
        		            		$('#tagAddModal').find("label[name=tagsTitle]")[0].innerHTML = "批量删除标签";
        		            	}
        		        		$('#tagAddModal').find("input[name=product]").removeAttr("disabled");
        		        		$('#tagAddModal').find("input[name=dimension]").removeAttr("disabled");
        		        		$('#tagAddModal').find("input[name=entitys]").removeAttr("disabled");
        		        		$('#tagAddModal').find("input[name=type]").val(cmdSource);
        		        		$('#tagAddModal').find("input[name=product]").val(product);
        		        		$('#tagAddModal').find("input[name=dimension]").val(dimension);
        		        		$('#tagAddModal').find("input[name=entitys]").val(data.data.dmsnEntitys);
        		        		$('#tagAddModal').find("input[name=product]").attr("disabled",true);
        		        		$('#tagAddModal').find("input[name=dimension]").attr("disabled",true);
        		        		$('#tagAddModal').find("input[name=entitys]").attr("disabled",true);
        		        		$('#tagAddModal').find("input[name=tags]").val("");
        		        		$('#tagAddModal_saveTags').unbind('click').bind('click', function(event){ self.batchSaveTags(); });
        		        		$('#tagAddModal').find("input[name=tags]").tagsinput({style:'min-width:90%'});
        		        		$('#tagAddModal').find("input[name=tags]").tagsinput("removeAll");
        		        		$('#tagAddModal').modal();
        					}else{
        						bootbox.alert({message:'操作失败，没有符合条件的产品['+product+']维度['+dimension+']实体',size:'small'});
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
            
            //批量保存维度实体标签
            batchSaveTags: function(cmdSource){
            	var self = this;
            	var modelObj = $('#tagAddModal');
            	var tags = modelObj.find("input[name=tags]").val();
            	if(tags == null || tags == ""){
            		bootbox.alert({message:'操作失败，标签不能为空',size:'small'});
            		return false;
            	}
            	var reg2 = /^[0-9a-zA-Z_,]+$/;
        		if(!reg2.test(tags)){
        			bootbox.alert({message:'操作失败，标签只能为数字字母或者下划线',size:'small'});
        			return false;
        		}
        		if(!validateStringLength(tags,200)){
            		bootbox.alert({message:'操作失败，标签的长度不能超过200',size:'small'}); 
            		return false;
            	}
        		var type = modelObj.find("input[name=type]").val();
            	var product = modelObj.find("input[name=product]").val();
            	var dimension = modelObj.find("input[name=dimension]").val();
            	var entitys = modelObj.find("input[name=entitys]").val();
            	if(entitys == null || entitys == ""){
            		entitys = null;
            	}
            	var titleDesc = "批量删除实体标签";
            	if(type == "add"){
            		titleDesc = "批量添加实体标签";
            	}
            	var tConfirm = true;
            	bootbox.confirm({
            		size : 'small',
            		message : '是否确定'+titleDesc+'？',
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                			$.ajax({
                    			url : 'dmsn/entity/tags/'+type+'/'+product+'/'+dimension+'/'+entitys+'/'+tags,
                    			type : 'DELETE',
                    			contentType:'application/json',
                    			success: function(data){
                    				if(data.success){
                    					if(type == "add"){
                    						bootbox.alert({message:titleDesc+'成功',size:'small'});
                    					}else{
                    						bootbox.alert({message:titleDesc+'成功',size:'small'});
                    					}
                    					$('#tb_dmsn_entities').bootstrapTable('refresh');
                    					var param={"product":product,"dimension":dimension};
                                    	self.updateTagSelect(param);
                    					$('#tagAddModal').modal('hide');
                    				}else{
                    					if(type == "add"){
                    						var msgInfo = '批量添加实体标签操作失败,服务器处理出错<br>' + data.info;
            	        	        		if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                        					}
                    	                	bootbox.alert({message:msgInfo, size:'middle'});
                    					}else{
                    						var msgInfo = '批量删除实体标签操作失败,服务器处理出错<br>' + data.info;
            	        	        		if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                        					}
                    	                	bootbox.alert({message:msgInfo, size:'middle'});
                    					}
                    				}
                    			},
                    			error: function(msg){
                    				bootbox.alert({message:'连接服务器错误',size:'small'});
                    			}
                    		});
                		}
                	 }
            	});
            },
            
            dmsnImport : function(){
            	$("#dimensionImportForm").find("input[name='uFile']").val("");
            	$("#dimensionImportModal").modal("show");
            },
            
            dmsnSaveImport : function(){
            	var tValue = $("#dimensionImportForm").find("input[name='uFile']").val();
            	if(null == tValue || tValue == ""){
            		bootbox.alert({message:'操作失败，上传文件不能为空',size:'small'});
            		return false;
            	}
            	tValue = tValue.substring(tValue.lastIndexOf(".")+1);
            	if(tValue.toLowerCase() != 'xls' && tValue.toLowerCase() != 'xlsx'){
            		bootbox.alert({message:'操作失败<br>上传文件格式错误(支持上传文件格式：xls、xlsx)',size:'small'});
            		return false;
            	}
            	$.ajax({
            		type:'POST',
            		url:'dmsn/dmsnSaveImport',
            		cache:false,
            		processData:false,
            		contentType:false,
            		data:new FormData($("#dimensionImportForm")[0]),
            		dataType:'json',
            		success:function(data){
            			if(data.success){
            				$("#dimensionImportModal").modal("hide");
            				bootbox.alert({message:'上传成功',size:'small'});
            				$('#tb_dimension').bootstrapTable('refresh');
            			}else{
            				bootbox.alert('上传操作失败,服务器出错<br>' + data.info);
            			}
            		},
            		error:function(){
            			bootbox.alert({message:'连接服务器错误',size:'small'});
            		}
            		
            	});
            },
            
            dmsnExport : function(){
            	var qProduct = "";
            	var qDimension = "";
            	var pAndDs = "";
            	var selected = $('#tb_dimension').bootstrapTable('getSelections');
            	if(selected.length > 0){
            		var dmsns = [];
            		_.each(selected, function(element, index){
            			dmsns.push(element.product + ':' + element.name);
            		});
            		pAndDs = dmsns.join('|');
            	}else{
            		qProduct = $("#wrap_container_from").find("input[name='product']").val();
            		qDimension = $("#wrap_container_from").find("input[name='dimension']").val();
            	}
            	var tConfirm = true;
        		bootbox.confirm({
            		size : 'small',
            		message : '是否确定导出维度？',
            		callback : function (result) {
                		if(result && tConfirm){
            				tConfirm = false;
                			window.location.href = "dmsn/dmsnExport?qProduct="+encodeURIComponent(encodeURIComponent(qProduct))+"&qDimension="+encodeURIComponent(encodeURIComponent(qDimension))+"&pAndDs="+encodeURIComponent(encodeURIComponent(pAndDs));
                		}
                	 }
            	});
            },
            
            dmsnQuery: function(){
            	$('#tb_dimension').bootstrapTable('refresh');
            }
        });
        return app;
});