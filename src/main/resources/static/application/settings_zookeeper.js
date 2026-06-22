﻿define([
	'underScore', 
	'text!application/settings_zookeeper.html', 
	'alertor',
	'css!css/pagination.css', 'css!css/jquery.json-viewer.css',
	'bootstrap-select', 'bootstrap-table',
	'jquery-jsoneditor', 'jquery-jsonviewer'],
    function (_, template, alertor) {
        var app = function (config) {
            this.o_template = $(template);
            this.pageSize = 30;
            this.jsonEditor = null;
        };
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
            	self.loadTreeNodes({'path' : '/', 'pageSize' : self.pageSize, 'pageNumber' : 1});
            	$('#zk_edit_btnSave').bind('click',function(){
            		self.saveNode(self);
            	});
            	$('#btn_zoo_jsonformat').bind('click',function(){self.showJsonFormat();});
            	
            	$('#btn_zoo_data_tran').bind('click',{'app':self},function(event){
            		if(event.data.app.jsonEditor != null){
            			var jsonStr = null;
        				try {
        					jsonStr = JSON.stringify(event.data.app.jsonEditor.get());
        				} catch (error) {
        					alertor.dangerAlert('非JSON格式数据','small');
                			return;
        				}
            			$("#zkNodeData").val(jsonStr);
            			$('#modal-zoo-data-edit').modal('hide');
            		}
            	});
            	
            },
            showJsonFormat: function(){
            	var self = this;
            	var data = $("#zkNodeData").val();
            	var dataJson = null;
            	if(data != null && data != ''){
            		data = data.replace(/\r/g, '').replace(/\\r/g, '');
            		try {
            			dataJson = $.parseJSON(data);
            		} catch (error) {
            			alertor.dangerAlert('非JSON格式数据无法展示','small');
            			return;
            		}
            		self.jsonEditor = new JsonEditor('#json-display', dataJson, {withQuotes : true});
                	$('#modal-zoo-data-edit').modal();
            	}
            },
            loadTreeNodes : function(params) {
            	var self = this;
            	if(params.path.indexOf("##")>0){
            		params.path = params.path.replace("##","_|_").replace("]","$");
            	}
            	$.ajax({ 
            		url : 'zoo/children',
            		type : 'GET',
            		data : params,
            	    contentType:'application/json',
            		success: function(data){
            			var rs = $.parseJSON(data);
            			self.createPagination(rs.data.total, rs.data.pageNumber);
            			self.treeDataLoad(self, $.parseJSON(data));
            		},
            		error: function(msg){
            			alertor.dangerAlert('系统错误，'+msg,'small');
            			//nothing to do 
            		}
            	});
            },
            treeDataLoad : function(that, treedata){
				var nodes = treedata.data.rows;
				$("#zkNodeShow").empty();
				var treeNodes = '';
				var rootNode = "<div id='zkRoot' style='cursor:pointer;'><i class='fal fa-circle' title='根节点'></i><i class='fal fa-home' style='margin-left:10px'></i><a style='cursor:pointer;margin-left:10px' title='返回根节点'>/</a></div>";
				var prevNode = "<div id='zkReturn' style='cursor:pointer;'><i class='fal fa-circle' title='上一层节点'></i><i class='fal fa-reply' style='margin-left:10px'></i><a style='cursor:pointer;margin-left:10px;font-weight:bold' title='返回上一层节点'>.&nbsp;.&nbsp;</a></div>";
				treeNodes += rootNode;
				treeNodes += prevNode;
				if(nodes){
					for (var i=0,len=nodes.length; i<len; i++){
    					var treeNode = '<div class="bind_v_clz" title="双击进入">';
    					if(nodes[i].mode == "PERSISTENT"){
    						treeNode += "<i class='fal fa-circle' title='持久节点'></i>";
    					}else{
    						treeNode += "<i class='fal fa-dot-circle' title='临时节点'></i>";
    					}
    					treeNodes += treeNode + "<a data-event='node_del_handler' title='删除节点' style='margin-left:10px;cursor:pointer' class='fal fa-trash'></a><a data-event='zoo_node_link' style='margin-left:10px;cursor:pointer'>"+nodes[i].name+"</a><a data-event='zoo_node_detail_link' title='节点详细信息' style='margin:5px 10px 0 0;cursor:pointer;float:right;' class='fal fa-list'></a><span title='子节点个数' style='padding-left:8px'>"+nodes[i].numOfChildren+"</span></div>";
    				}
    				$("#zkNodeShow").append(treeNodes);
    				
    				$("a[data-event='node_del_handler']").bind('click', function(event){
    					that.removeNode(event);
    				});
    				$("a[data-event='zoo_node_detail_link']").bind('click', function(event){
    					that.showNodeDetail(event);
    				});
    				$("div.bind_v_clz").bind('dblclick', function(event){
    					that.showNodeChildren(event);
    					$("#zkNode").focus();
    				});
    			} else {
    				$("#zkNodeShow").append(treeNodes);
    			}
            	$('#zkReturn').bind('click',function(){
            		that.previousBack(that);
            	});
            	$('#zkRoot').bind('click',function(){
            		that.backRoot(that);
            	});
            },
            createPagination : function(total, currentPage){
            	var self = this;
            	var mypagin = $("#mypagin");
            	var pagination_el = $('#pagination');
            	if (pagination_el) pagination_el.remove();
            	mypagin.append("<div id='pagination' class='right flickr'></div>");
            	$("#pagination").pagination(total,{
            		prev_link: "<i class='fal fa-dot-circle'></i><i class='fal fa-folder-open' style='margin-left:10px;margin-right:10px'></i>",
            		callback:function(pageNumber, obj){
        				self.loadTreeNodes({path:$("#zkNodeNow").val(),pageSize:self.pageSize,pageNumber:pageNumber+1});
        				return obj;
        			},
            		items_per_page:self.pageSize,//每页的数据个数
            		num_display_entries:2,//两侧首尾分页条目数
            		current_page: currentPage-1,//当前页
            		num_edge_entries:2 //连续分页主体部分分页条目数
            	});
            },
            clearDetailInfo: function(){
            	$("input[name='CreateTime']").val('');
				$("input[name='ACLVersion']").val('');
				$("input[name='ChildrenVersion']").val('');
				$("input[name='CreateID']").val('');
				$("input[name='ModifiedID']").val('');
				$("input[name='DataLength']").val('');
				$("input[name='EphemeralOwner']").val('');
				$("input[name='LastModifiedTime']").val('');
				$("input[name='DataVersion']").val('');
				$("input[name='NumberOfChildren']").val('');
				$("input[name='NodeID']").val('');
				$("#zkNodeData").val('');
            },
            showNodeChildren : function(event){
            	var self = this;
            	self.clearDetailInfo();
    			var el_input_node = $("#zkNode"); 
				var el_current_node = $("#zkNodeNow");
				var el_zooTree = $("#zkNodeShow");
        		var v_current_node = el_current_node.val();
        		var el_a_text = $("a[data-event='zoo_node_link']",$(event.currentTarget)).text();
        		var path = (v_current_node == "/" ? (v_current_node + el_a_text) : (v_current_node + "/" + el_a_text));
            	//当前目录显示
        		el_current_node.val(path);
            	//节点显示
        		el_input_node.val(path);
        		self.loadTreeNodes(
        				{'path' : path, 'pageSize' : self.pageSize, 'pageNumber' : 1},
        				function(that, data){
        					that.createPagination(data.data.total);
        				});
            },
            removeNode : function(event){
            	var self = this;
            	bootbox.confirm({
            		size : 'small',
            		message : alertor.warningMessage('确定删除选中吗？'),
            		callback : function (result) {
                		if(result){
                			var el_input_node = $("#zkNode"); 
        					var el_current_node = $("#zkNodeNow");
        					var el_zooTree = $("#zkNodeShow");
                    		var v_current_node = el_current_node.val();
                    		var el_a_text = $(event.currentTarget).next().text();
                    		var path = (v_current_node == "/" ? (v_current_node + el_a_text) : (v_current_node + "/" + el_a_text));
                    		$.ajax({ 
                    			url : 'zoo/nodeDel?path='+path+'&delChildren=true',
                    			type : 'DELETE',
                    			contentType:'application/json',
                    			success: function(data){
                    				var dataJson = $.parseJSON(data);
                    				if(dataJson.success){
                    					alertor.successAlert('删除成功','small');
                    					//将该节点的所属节点移除
                    					$(event.currentTarget).parent().remove();
                    					self.clearDetailInfo();
                    					el_input_node.val(el_current_node.val());
                    					self.loadTreeNodes({'path' : v_current_node, 'pageSize' : self.pageSize, 'pageNumber' : 1}, self.treeDataLoad);
                    				}else{
                    					alertor.dangerAlert('删除失败,服务器处理错误,'+data.info,'small');
                    				}
                    			},
                    			error: function(msg){
                    				alertor.dangerAlert('访问服务器错误','small');
                    			}
                    		});
                		}
                	 }
            	});
            },
            saveNode : function(){
				var path = $("#zkNode").val();
				var data = $("#zkNodeData").val();
            	if(path == null || path == ''){
            		alertor.dangerAlert('节点不能为空','small');
            		return;
            	}
            	var param = {};
            	if(data) data = data.trim();
            	param.nodeData = data;
            	param.name = path;
            	$.ajax({ 
            		url : 'zoo/nodeModify',
            		type:"PUT",
                    data:JSON.stringify(param),
                    contentType:'application/json',
            		success: function(data){
            			var jsonData = JSON.parse(data);
            			if (jsonData.success){
            				alertor.successAlert('保存成功','small');
                         }else{
                        	alertor.dangerAlert('保存失败,服务器处理错误,'+jsonData.info,'small');
                         }
            		},
            		error: function(msg){
            			alertor.dangerAlert('访问服务器错误','small');
            		}
            	});
            },
            showNodeDetail : function(event){
            	var el_input_node = $("#zkNode"); 
				var el_current_node = $("#zkNodeNow");
				var el_zooTree = $("#zkNodeShow");
        		var v_current_node = el_current_node.val();
        		var el_a_text = $(event.currentTarget).prev().text();
        		var path = (v_current_node == "/" ? (v_current_node + el_a_text) : (v_current_node + "/" + el_a_text));
        		el_input_node.val(path);
            	if(path.indexOf("##")>0) path = path.replace("##","_|_").replace("]","$");
            	$.ajax({ 
            		url : 'zoo/nodeInfo',
            		type : 'GET',
            		data : {path : path},
            		dataType : 'json',
            		success: function(data){
            			var jsonNode = data.data.node;
                    	$("input[name='CreateTime']").val(jsonNode.createTime);
        				$("input[name='ACLVersion']").val(jsonNode.aversion);
        				$("input[name='ChildrenVersion']").val(jsonNode.cversion);
        				$("input[name='CreateID']").val(jsonNode.czxid);
        				$("input[name='ModifiedID']").val(jsonNode.mzxid);
        				$("input[name='DataLength']").val(jsonNode.dataLength);
        				$("input[name='EphemeralOwner']").val(jsonNode.ephemeralOwner);
        				$("input[name='LastModifiedTime']").val(jsonNode.lastModifiedTime);
        				$("input[name='DataVersion']").val(jsonNode.version);
        				$("input[name='NumberOfChildren']").val(jsonNode.numOfChildren);
        				$("input[name='NodeID']").val(jsonNode.znodeId);
            			if(jsonNode.nodeData){
            				$("#zkNodeData").val(jsonNode.nodeData);
            			}else{
            				$("#zkNodeData").val("");
            			}
            		},
            		error: function(msg){
            			//nothing to do 
            		}
            	});
            },
            previousBack : function(that){
            	that.clearDetailInfo();
            	$('#zkNodeData').val('');
            	$("#zkNode").val('');
            	var currentNode = $('#zkNodeNow').val();
            	if(currentNode == "/"){
            		return;
            	}
            	var parentNode;
            	if(currentNode.lastIndexOf("/")==0){
            		parentNode = currentNode.substring(0,1);
            	}else{
            		parentNode = currentNode.substring(0,currentNode.lastIndexOf("/"));
            	}
            	//清除所有节点
            	$('#zkNodeShow').empty();
            	//当前目录显示
            	$('#zkNodeNow').val(parentNode);
            	//节点显示
            	$("#zkNode").val(parentNode);
            	
            	that.loadTreeNodes(
						{ path : parentNode, pageSize : self.pageSize, pageNumber : 1}, 
						function(that, data){
        					that.createPagination(data.data.total);
        				});
            },
            backRoot : function(that){
            	that.clearDetailInfo();
            	$('#zkNodeData').val('');
            	$("#zkNode").val('');
            	var currentNode = $('#zkNodeNow').val();
            	if(currentNode == "/") return;
            	//清除所有节点
            	$('#zkNodeShow').empty();
            	//当前目录显示
            	$('#zkNodeNow').val('/');
            	//节点显示
            	$("#zkNode").val('/');
            	
        		that.loadTreeNodes(
        				{'path' : '/', 'pageSize' : this.pageSize, 'pageNumber' : 1},
        				function(that, data){
        					that.createPagination(data.data.total);
        				});
            }
        });
        return app;
});