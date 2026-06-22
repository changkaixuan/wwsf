define([
		'underScore', 
		'text!application/wws_job_template_task_graphical.html',
		'mloadding', 
		'ko',
		'jsPlumb',
		'jsPlumbToolkit',
		'bootstrap-table', 
		'bootstrap-switch',
		'bootstrap-typeahead',
		'bootstrap-tagsinput',
		'x-editable', 'mockjax', 'icheck', 'spinner' ], function(_, template, mloadding, ko, jsPlumb, jsPlumbToolkit) {
	
	var app = function(jmodule, jrow) {
		this.jmodule = jmodule;
		this.jrow = jrow;
		this.el = $(template);
		this.jobJsPlumbToolkit = null;
	};
	
	_.extend(app.prototype, {
		load : function() {
			
		},
		render : function(container) {
			var self = this;
            var o_container = $(container);
            o_container.empty();
            o_container.append(self.el);
           
            $('#btn_job_running').bind('click', function(event){
            	self.jmodule.batchRunHistory(self.jrow.product, self.jrow.jobId);
            });
            $('#btn_job_edit').bind('click', function(event){
            	self.jmodule.showJobInfo(self.jrow.product, self.jrow.jobId, true);
            	self.jmodule.initTaskTable(self.jrow);
            });
            $('#btn_full_screen_graphy').bind('click', function(event){
            	var fullScreen =  $('#full_screen_div');              	
            	$.proxy(fullScreen.toggleFullscreen, fullScreen);
            	fullScreen.toggleClass('fullscreen');
            });
            $('#btn_save_graphy').bind('click', function(event){	
            	self.saveJobGraphy(true);
            });
            $('#btn_format_graphy').bind('click', function(event){
            	
            	window.renderer.setLayout({
            		type: "Spring",
	                parameters: {
	                    padding: [100, 100]
	                }
            	});
            	var nodes = self.jobJsPlumbToolkit.getNodes();
            	var edges = self.jobJsPlumbToolkit.getAllEdges();
            	
            	var conciseGraphData = {vertexs:[],edges:[]};
            	$.each(nodes,function(i,n){
        			conciseGraphData.vertexs.push({name: n.id});
        		});
            	$.each(edges,function(i,n){
        			conciseGraphData.edges.push({
        				source: n.source.getNode().id, 
        				target: n.target.getNode().id,
        				lineType: n.data.lineType,
        				type: n.data.lineType == 1 ? '' : ''
        				});
        		});
            	
            	$.ajax({
            		async:false,
            		url: 'template/graphJob',
        			type : 'POST',
		            data:JSON.stringify(conciseGraphData),
		            contentType:'application/json',
        	        success:function (data) {
        	        	if(data.success){
        	        		if (data.data.graph){
        	        			$.each(data.data.graph.vertexs,function(i,n){
            	            		var nodeDiv = $("div[data-jtk-node-id="+n.name+"]");
            	            		nodeDiv.css("left",n.x);
            	            		nodeDiv.css("top",n.y);
            	        		});
        	                	//self.jobJsPlumbToolkit.clear();
        	                	//self.jobJsPlumbToolkit.load({ data: self.getTasksData() });
        	        		}
        	        	} else {
        	        		var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
        					bootbox.alert({
                    			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
                    			size:'middle',
                    			closeButton: false
                    		});
        	        	}
        	       },
        	       error: function (e) {
        	    	   bootbox.alert({message:'<div class="alert alert-danger mb-0 py-2">访问服务器错误:'+e.status+'<div>', size:'small'});
        	       }
        		});
		        window.renderer.zoomToFit();
            });
            $('#btn_layout_vertical').bind('click', function(event){
            	window.renderer.setLayout({
            		type:"Hierarchical",
                	parameters:{
                		orientation:"vertical",
                		padding:[80,60]
                	}
            	});
            	window.renderer.zoomToFit();
            });
            $('#btn_layout_horizontal').bind('click', function(event){
            	var newLayout = {
                		type:"Hierarchical",
    	            	parameters:{
    	            		orientation:"horizontal",
    	            		padding:[80,60]
    	            	}
            		};
            	window.renderer.setLayout(newLayout);
            	window.renderer.zoomToFit();
            });
            $('#saveTaskinfo').unbind('click').bind('click', function(event){
            	self.saveJobPanel(false);
            	self.saveTaskinfo();
            });
            self.initJobFlowChart();
		},
		initJobFlowChart : function(){
			var self = this;
			var usetree = [];
        	var tagTreeData = {};
        	var usetreeTag = [];
        	var allPluginNames = {};
        	//查找插件的树结构图
        	$.ajax({
        		async:false,
        		data : {product:self.jrow.product},
        		url: 'template/getPluginByProduct',
    			type : 'GET',
    			contentType:'application/x-www-form-urlencoded',
    	        success:function (data) {
    	        	if(data.success){
    	        		var plugins = data.data.plugins;
    	        		var wwsPlugins = data.data.wws;
    	        		$.each(plugins,function(i){
    	        			var name = plugins[i].name;
    	        			allPluginNames[name] = true;
    	        		});
    	        		if(tagTreeData["wws"] == undefined){
    	        			tagTreeData["wws"] = [];
    	        		}
    	        		$.each(wwsPlugins,function(i){
    	        			allPluginNames[wwsPlugins[i].name] = true;
    	        			tagTreeData["wws"].push({"text":wwsPlugins[i].name});
    	        		});
    	        		$.each(tagTreeData,function(key,value){
    	        			usetreeTag.push({"text":key,"nodes":value});
    	        		});
    	        		usetree.push({"text":"","nodes":usetreeTag});
    	        	} else {
    	        		var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
    					bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
                			size:'middle',
                			closeButton: false
                		});
    	        	}
    	       },
    	       error: function (e) {
    	    	   bootbox.alert({message:'<div class="alert alert-danger mb-0 py-2">访问服务器错误:'+e.status+'<div>', size:'small'});
    	       }  	
    		});
        	var treeView = "";
        	var parentNodes = usetree[0].nodes;
        	$.each(parentNodes,function(i){
        		treeView = treeView 
		        		+ '<div class="panel">'
		        		+ '<div class="panel-hdr" style="min-height:2rem;">'
		        		+ '<h2 class="m-0" style="line-height:2rem;">' + parentNodes[i].text + '</h2>'
		        		+ '<div class="panel-toolbar">'
		        		+ '<button class="btn btn-panel bg-transparent fs-xl w-auto h-auto rounded-0 waves-effect waves-themed" data-action="panel-collapse" data-toggle="tooltip" data-offset="0,10" data-original-title="Collapse"><i class="fal fa-bars"></i></button>'
		        		+ '</div>'
		        		+ '</div>'
		        		+ '<div class="panel-container collapse show">'
		        		+ '<div class="panel-content p-1">';
        			
        		var childNodes = parentNodes[i].nodes;
        		$.each(childNodes,function(j){
        			treeView = treeView + '<div  title="drag to add new node" data-node-type="table" class="sidebar-item p-1 m-1">'
        			                    + childNodes[j].text
        			                    + '</div>';
        		})
        		treeView = treeView + '</div></div></div>';
        	});
        	$('#jtpl-acpg').append(treeView);
    		jsPlumbToolkit.ready(function () {
		        var idFunction = function (n) {
		            return n.id;
		        };
		        var typeFunction = function (n) {
		            return n.type;
		        };
		        var mainElement = document.querySelector('#jtk-demo-dbase'),
		            canvasElement = mainElement.querySelector(".jtk-demo-canvas"),
		            miniviewElement = mainElement.querySelector(".miniview"),
		            nodePalette = mainElement.querySelector(".node-palette"),
		            controls = mainElement.querySelector(".controls");

		        self.jobJsPlumbToolkit = jsPlumbToolkit.newInstance({
		            idFunction: idFunction,
		            typeFunction: typeFunction,
		            nodeFactory: function (type, data, callback, event) {
		            	var pluginName = $(event.drag.el).html();
		            	if(allPluginNames[pluginName] == undefined){
		            		bootbox.alert({
	                			message:'<div class="alert alert-danger mb-0 py-2">插件不存在</div>',
	                			size:'small',
	                			closeButton: false
	                		});
    						return;
    					}
		                jsPlumbToolkit.Dialogs.show({
		                    id: "dlgName",
		                    title: "请输入任务项编号:",
		                    onOK: function (d) {
	                        	if(!/^[0-9a-zA-Z_]+$/.test(d.name)){
	                        		bootbox.alert({
	    	                			message:'<div class="alert alert-danger mb-0 py-2">任务项编号只能为数字字母或者下划线</div>',
	    	                			size:'small',
	    	                			closeButton: false
	    	                		});
	                    		} else {
	                    			$.extend(data ,{
	                                	name : d.name, 
	                                	id : d.name, 
	                                	columns : [{id:pluginName}], 
	                                	column : pluginName
	                                });
	                                callback(data);
	                                self.jmodule.taskEdit(self.jrow, d.name, pluginName, false, self.jobJsPlumbToolkit);
	                    		}                      
		                    }
		                });
		            },
		            edgeFactory: function (params, data, callback) {
		                callback(data);
		            },
		            portDataProperty:"columns",
		            beforeStartConnect:function(a, b) {
		            	return {connectCount : 1};
		            },
		            beforeConnect:function(source, target, edgeData) {
		            	if (edgeData) {
			            	if(source == target || source.getNode().id == target.getNode().id){//点源节点，不指向其它节点，不连线
			            		return false;
			            	}
			            	var edges = self.jobJsPlumbToolkit.getAllEdges();
			            	if(edges.length < 1){
			            		return true;
			            	}
			            	var jobPanelInfo = {};
			            	jobPanelInfo.jobId = self.jrow.jobId;
			        		jobPanelInfo.leansRelationShip = {};
			        		var indexArray = -1;
			        		var tEdge = null; //过滤重复线,重复线不建立连接//，只改变线（虚线改为横虚线）
			            	$.each(edges,function(i){
			            		var source1 = edges[i].source.getNode().id;//taskId
			            		//var source2 = edges[i].source.data.id;//pluginName
			            		var target1 = edges[i].target.getNode().id;
			            		//var target2 = edges[i].target.data.id;
			            		if(source1 == source.getNode().id && target1 == target.getNode().id){
			            			tEdge = edges[i];
			            		}else{
				            		if(jobPanelInfo.leansRelationShip[target1] == undefined){
				            			jobPanelInfo.leansRelationShip[target1] = [];
				            		}
				            		indexArray = jobPanelInfo.leansRelationShip[target1].indexOf(source1);
				         	        if(indexArray < 0){
				         	        	jobPanelInfo.leansRelationShip[target1].push(source1);
				         	        }
			            		}
			            	});
			            	if(null != tEdge){ //已存在线，不可建连接
			            		return false;
			            	}
			            	//新的连线
			            	if(jobPanelInfo.leansRelationShip[target.getNode().id] == undefined){
		            			jobPanelInfo.leansRelationShip[target.getNode().id] = [];
		            		}
		            		indexArray = jobPanelInfo.leansRelationShip[target.getNode().id].indexOf(source.getNode().id);
		         	        if(indexArray < 0){
		         	        	jobPanelInfo.leansRelationShip[target.getNode().id].push(source.getNode().id);
		         	        }
			            	var retValue = true;
			            	//新增线条则判断是否闭环
			            	if(edgeData && edgeData.connectCount && edgeData.connectCount == 1) {
			            		$.ajax({
					        		async:false,
									url:'template/isContainLoop',
						            type:"POST",
						            processData:false,
						            data:JSON.stringify(jobPanelInfo),
						            contentType:'application/json',
						            success:function (data) {
						            	if(!data.success){
						                	retValue = false;
						                	var msgInfo = '';
				        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
				        						msgInfo = '闭环验证失败,服务器处理出错<br>';
				        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
				        					}else{
				        						msgInfo = '闭环验证失败<br>' + data.info;
				        						msgInfo = "<p style='scroll:no;word-break:break-all;'>"+msgInfo+"</p>";
				        					}
				    	                	bootbox.alert({
				                    			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
				                    			size:'middle',
				                    			closeButton: false
				                    		});
						                }
						            },
						            error: function(XMLHttpRequest,textStatus,errorThrown){
						            	retValue = false;
						            	bootbox.alert({message:'<div class="alert alert-danger mb-0 py-2">访问服务器错误:'+e.status+'<div>', size:'small'});
						            }
						        });
			            	}
			            	return retValue;
		            	} else {
		            		return true;
		            	}
		            }
		     
		        });
		        jsPlumbToolkit.Dialogs.initialize({ selector: ".dlg" });

		        var renderer = window.renderer = self.jobJsPlumbToolkit.render({
		            container: canvasElement,
		            view: {
		                nodes: {
		                    "table": {
		                        template: "tmplTable"
		                    }
		                },
		                edges: {
		                    "common": {
		                        anchor: ["Right", "Left", "Top", "Bottom"],
		                        connector: "Straight",
		                        cssClass:"common-edge",
		                        events: {
		                            "dbltap": function (params) {
		                                _editEdge(params.edge);
		                            }
		                        },
		                        overlays: [
		                            [ "Label", {
		                                cssClass: "delete-relationship",
		                                label: "<i class='fal fa-times fa-fw'></i>",
		                                events: {
		                                    "tap": function (params) {
		                                    	//lineType=2虚线不能删除  
		                                    	if(params.edge.data.lineType == 1){
		                                    		bootbox.confirm({
		                                    			message:'<div class="alert alert-danger mb-0 py-2">确定删除依赖?</div>',
		                                    			size:'small',
		                                    			closeButton: false,
		                                    			callback: function(result){
		                                    				if (result){
		                                    					self.jobJsPlumbToolkit.removeEdge(params.edge);
		                                    				}
		                                    			}
		                                    		});
		                                    	} else {
		                                    		bootbox.alert({
		                                    			message:'<div class="alert alert-danger mb-0 py-2">无法删除特殊依赖</div>',
		                                    			size:'small',
		                                    			closeButton: false
		                                    		});
		                                    	}
		                                    }
		                                }
		                            } ]
		                        ]
		                    },
		                    "s:e": {
		                        parent: "common",
		                        auchor:"Continuous",
				            	endpoint:"Blank",
				            	paintStyle:{strokeWidth:1, stroke:"red"},
				            	overlays:[
				            		[ "Arrow", { width : 10, length : 10, location : 1 }]
				            	]
		                    }
		                },
		                ports: { //定义连接线生成
		                    "default": {
		                        template: "tmplColumn",
		                        edgeType: "s:e", 
		                        maxConnections: -1, 
		                        dropOptions: { 
		                            hoverClass: "drop-hover"
		                        },
		                        events: {
		                            "dblclick": function () {
		                            },
		                            "click": function () {
		                            	console.log(111);
		                            }
		                        }
		                    }
		                }
		            },
		            layout: {
		                type: "Spring",
		                parameters: {
		                    padding: [100, 100]
		                }
		            },
		            events: {
		                portAdded: function (params) {
		                    params.nodeEl.querySelectorAll("ul")[0].appendChild(params.portEl);
		                },
		                edgeAdded: function (params) {
		                    if (params.addedByMouse) {
		                        _editEdge(params.edge, true);
		                    }
		                },
		                canvasClick: function (e) {
		                    self.jobJsPlumbToolkit.clearSelection();
		                }
		            },
		            dragOptions: { //不可拖拽位置
		                filter: ".testswitch,.testswitch-checkbox,.testswitch-label,.testswitch-inner,.testswitch-switch,i, .view .buttons, .table .buttons, .table-column *, .view-edit, .edit-name"
		            },
		            consumeRightClick: false,
		            zoomToFit:true
		        });
		        
		        renderer.bind("modeChanged", function (mode) {
		            jsPlumb.removeClass(controls.querySelectorAll("[mode]"), "selected-mode");
		            jsPlumb.addClass(controls.querySelectorAll("[mode='" + mode + "']"), "selected-mode");
		        });
		        
		        var _editEdge = function (edge, isNew) {
		        	var data = edge.data;
		        	if(isNew){
		        		data = {'type':'s:e','lineType':'1'};
		        	}
		        	self.jobJsPlumbToolkit.updateEdge(edge, data);
		        };

		        //删除Task
		        jsPlumb.on(canvasElement, "tap", ".delete i, .view-delete i", function (e) {
		            var info = renderer.getObjectInfo(this);
		            jsPlumbToolkit.Dialogs.show({
		                id: "dlgConfirm",
		                data: {
		                    msg: "删除任务项，编号 [" + info.obj.getNode().id + "]"
		                },
		                onOK: function (data) {
		                	//删除task信息
	            	        $.ajax({
	                			async:false,
	                			url: 'template/deltask',
	                	        type:"GET",
	                	        data:{product:self.jrow.product,jobId:self.jrow.jobId,taskId:info.obj.getNode().id},
	                	        contentType:'application/x-www-form-urlencoded',
	                	        success:function (data) {
	                	        	if(data.success){
	                	        		self.jobJsPlumbToolkit.removeNode(info.obj.getNode().id);
	                	        		bootbox.alert({
		    	                			message:'<div class="alert alert-success mb-0 py-2">删除任务项成功</div>',
		    	                			size:'small',
		    	                			closeButton: false
		    	                		});
	                	        	} else {
	                	        		var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
	                					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
	                						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
	                					}
	            	                	bootbox.alert({
	                            			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
	                            			size:'middle',
	                            			closeButton: false
	                            		});
	                	        	}
	                	       },
	                	       error: function (e) {
	                	    	   bootbox.alert({message:'<div class="alert alert-danger mb-0 py-2">访问服务器错误:'+e.status+'<div>', size:'small'});
	                	       }  	
	                		})
		                }
		            });
		        });
		        
		        //编辑Task
		        jsPlumb.on(canvasElement, "tap", ".edit-name", function (e) {
		        	var info = renderer.getObjectInfo(this);
		        	self.jmodule.taskEdit(self.jrow,info.obj.getNode().id,info.id, true);
		        });
		        
		        // pan mode/select mode
		        jsPlumb.on(controls, "tap", "[mode]", function () {
		            renderer.setMode(this.getAttribute("mode"));
		        });

		        // on home button click, zoom content to fit.
		        jsPlumb.on(controls, "tap", "[reset]", function () {
		            self.jobJsPlumbToolkit.clearSelection();
		            renderer.zoomToFit();
		        });
		        //拖拽视图
		        renderer.registerDroppableNodes({
		            droppables: nodePalette.querySelectorAll("[data-node-type]"),
		            dragOptions: {
		                zIndex: 50000,
		                cursor: "move",
		                clone: true
		            },
		            typeExtractor: function (el, eventInfo, isNativeDrag, eventLocation) {
		                return el.getAttribute("data-node-type");
		            },
		            dataGenerator: function (type, draggedElement, eventInfo, eventLocation) {
		                return { name: type };
		            }
		        });
		        //加载初始化数据
		        self.jobJsPlumbToolkit.load({ data: self.getTasksData() });
		        
		        //特殊依赖
		        var dottedLineConnArr = renderer.getJsPlumb().getAllConnections();
		        for(var i=0;i<dottedLineConnArr.length;i++){
		        	//lineType(1:task依赖 实线; 2:特殊依赖关系 点虚线)
		        	if(dottedLineConnArr[i].edge.data.lineType == "2"){
		        		dottedLineConnArr[i].setPaintStyle({strokeWidth:1,stroke:"red",dashstyle:"2 2"}); //stroke线的颜色  #89bcde
		        	}
		        }
		    });
        },
        getTasksData : function(){
        	var self = this;
        	var graphData = { nodes:[] };
        	//回显Tasks
        	$.ajax({
    			async:false,
    			url: 'template/getTasks',
    	        data:{ product: self.jrow.product, jobId: self.jrow.jobId},
    	    	type : 'GET',
    			contentType:'application/x-www-form-urlencoded',
    	        success:function (data) {
    	        	if(data.success){
    	        		var tasks = data.data.tasks;
    	        		var jobPanelInfo = data.data.jobPanelInfo;
    	        		console.log(jobPanelInfo);
    	        		$.each(tasks,function(i, t){
    	        			var node = {
    	        					name: t.taskId, 
    	        					id: t.taskId, 
    	        					type: "table", 
    	        					columns: [{nodeId: t.taskId, id: t.plugin}],
    	        					column: t.plugin
    	        				};
    	        			if(jobPanelInfo && jobPanelInfo.location && jobPanelInfo.location[t.taskId]){
    	        				node.left = jobPanelInfo.location[t.taskId].pointX;
    	        				node.top = jobPanelInfo.location[t.taskId].pointY;
        	        		}
    	        			graphData.nodes.push(node);
    	        		});
    	        		if(jobPanelInfo && jobPanelInfo.edges) { graphData.edges = jobPanelInfo.edges; }
    	        	} else {
    	        		var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
    					bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
                			size:'middle',
                			closeButton: false
                		});
    	        	}
    	       },
    	       error: function (e) {
    	    	   bootbox.alert({message:'<div class="alert alert-danger mb-0 py-2">访问服务器错误:'+e.status+'<div>', size:'small'});
    	       }  	
    		});
        	return graphData;
        },
        saveJobGraphy : function(arg){
        	var self = this;
        	var jobPanelInfo = {};
        	var nodes = self.jobJsPlumbToolkit.getNodes();
    		jobPanelInfo.product = self.jrow.product;
        	jobPanelInfo.jobId = self.jrow.jobId;
    		jobPanelInfo.leansRelationShip = {};
        	jobPanelInfo.beLeansRelationShip = {}; 
        	jobPanelInfo.edges = [];
        	jobPanelInfo.location = {};	
        	var edges = self.jobJsPlumbToolkit.getAllEdges();
        	var sourcesOnly = [];
        	$.each(edges,function(i){
        		if(edges[i].data.lineType != "2"){
	        		var source1 = edges[i].source.getNode().id;
	        		var source2 = edges[i].source.data.id;
	        		var target1 = edges[i].target.getNode().id;
	        		var target2 = edges[i].target.data.id;
	        		var indexArray = sourcesOnly.indexOf(source1+source2+target1+target2);
	     	        if(indexArray < 0){
	     	        	sourcesOnly.push(source1+source2+target1+target2);
	     	        	var edge = {"source":source1+'.'+source2,"target":target1+'.'+target2};
	     	        	jobPanelInfo.edges.push(edge);
	     	        };
	        		if(jobPanelInfo.beLeansRelationShip[source1] == undefined){
	        			jobPanelInfo.beLeansRelationShip[source1] = [];
	        		}
	        		indexArray = jobPanelInfo.beLeansRelationShip[source1].indexOf(target1);
	     	        if(indexArray < 0){
	     	        	jobPanelInfo.beLeansRelationShip[source1].push(target1);
	     	        };
	        		if(jobPanelInfo.leansRelationShip[target1] == undefined){
	        			jobPanelInfo.leansRelationShip[target1] = [];
	        		}
	        		indexArray = jobPanelInfo.leansRelationShip[target1].indexOf(source1);
	     	        if(indexArray < 0){
	     	        	jobPanelInfo.leansRelationShip[target1].push(source1);
	     	        };
        		}
        	})
        	$.each(nodes,function(i){
        		jobPanelInfo.location[nodes[i].data.id] = {};
        		var nodeId = nodes[i].data.id;
        		var nodeDiv = $("div[data-jtk-node-id="+nodeId+"]");
            	jobPanelInfo.location[nodes[i].data.id].pointX = parseFloat(nodeDiv.css("left"));
            	jobPanelInfo.location[nodes[i].data.id].pointY = parseFloat(nodeDiv.css("top"));
        	})
        	$.ajax({
        		async:false,
				url:'template/saveJobPanelInfo',
	            type:"POST",
	            processData:false,
	            data:JSON.stringify(jobPanelInfo),
	            contentType:'application/json',
	            beforeSend:function(){
	            	this.disabled = true;
	            },
	            success:function (data) {
	                if (data.success){
	                	if(arg){
	                		bootbox.alert({
	                			message:'<div class="alert alert-success mb-0 py-2">保存任务信息成功</div>',
	                			size:'small',
	                			closeButton: false
	                		});
	                	}
	                }else{
	                	var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
    					bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
                			size:'middle',
                			closeButton: false
                		});
	                }
	                this.disabled = false;
	            },
	            error: function(XMLHttpRequest,textStatus,errorThrown){
	            	bootbox.alert({message:'<div class="alert alert-danger mb-0 py-2">访问服务器错误:'+errorThrown+'<div>', size:'small'});
	                this.disabled = false;
	            }
	        });
        },
        formatJobPanel : function(){
        	var self = this;
        	$.ajax({
        		async:false,
				url:'template/jobGraphFormat',
	            type:"GET",
	            data:{product:self.jrow.product,jobId:self.jrow.jobId},
	            success:function (data) {
	                if (!data.success){
	                	var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
    					}
    					bootbox.alert({
                			message:'<div class="alert alert-danger mb-0 py-2">'+msgInfo+'</div>',
                			size:'middle',
                			closeButton: false
                		});
	                }
	            },
	            error: function(XMLHttpRequest,textStatus,errorThrown){
	            	bootbox.alert({message:'<div class="alert alert-danger mb-0 py-2">访问服务器错误:'+errorThrown+'<div>', size:'small'});   
	            }
	        });
        }
	});

	return app;
});