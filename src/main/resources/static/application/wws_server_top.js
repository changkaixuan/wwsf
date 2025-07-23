define(['underscore',
		'text!application/wws_server_top.html', 
		'vis',
		'mloadding', 
		'css!css/pagination.css', 
		'bootstrap-select', 
		'bootstrap-table'],
    function ( _, template, vis, mloadding) {
        var app = function (config) {
            this.o_template = $(template);
            this.direct = 'LR';
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
                self.draw();
                $("#btn_vis_topo_up_down").bind('click', function(){
                	self.direct = 'UD';
                	self.draw();
                	self.fillNodeDetailInfo();
                });
                $("#btn_vis_topo_refresh").bind('click', function(){
                	self.draw();
                	self.fillNodeDetailInfo();
                });
                $("#btn_vis_topo_left_right").bind('click', function(){
                	self.direct = 'LR';
                	self.draw();
                	self.fillNodeDetailInfo();
                });
            },
            draw : function(){
            	var self = this;
                $.ajax({
        			url : 'server/getNodeInfos',
        			type : 'GET',
        			dataType : 'json',
        			success: function(data){
        				if (data.success) {
        					self.drawNetwork(data.data);
        				} else {
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	bootbox.alert({message:msgInfo, size:'middle'});
    	                	return;
        				}
        			},
        			error: function(msg){
        				bootbox.alert({message:'连接服务器错误',size:'small'});
        				return;
        			},
        			beforeSend : function(){
        				mloadding.showLoadding();
        			},
        			complete : function(){
        				mloadding.hideLoadding();
        			}
        		});
            },
            drawNetwork : function(graphData){
            	var self = this;
            	if(graphData.visNodes && graphData.visEdges){
            		
            	} else {
            		$("#mygraph").empty();
            		return;
            	}
            	var nodes = graphData.visNodes;
                var edges = graphData.visEdges;
                
                var container = document.getElementById('mygraph');

                var options = {
                	configure: {
                		showButton: false,
                		enabled: false
                	},
                    nodes: {
                    	borderWidth: 1,
                    	borderWidthSelected: 1,
	                    scaling: {
	                        min: 16,
	                        max: 32
	                    },
	                    shadow: true,
	                    font: {
	                    	color: '#000',
	                    	size: 12,
	                    	face: 'tahoma'
	                    },
	                    color: {
                        	border: '#886ab5',
                        	background: '#fff'
                        },
                        shapeProperties: {
                        	useBorderWithImage: true
                        }
                    },
                    layout:{
                    	hierarchical: {
                    		direction: self.direct,
                    		sortMethod: 'directed'
                    	}
                    	//randomSeed:1
                    },
                    edges: {
                      arrows: {
                    	  to: {
                    		  enabled: true,
                    		  type: 'arrow',
                    		  scaleFactor: 0.5
                    	  }
                      },
                	  arrowStrikethrough: false,
                      color: {
                    	  color: '#886ab5',
                    	  highlight:'#2b7ce9'
                      },
                      smooth: true
                    },
                    physics: false,
                    groups:{
                    	'master': {
                            shape: 'circularImage',
                            image: 'js/vis/master-server.png',
                            size: 26
                        },
	                    'slave': {
	                        shape: 'circularImage',
	                        image: 'js/vis/slave-server.png',
	                        size: 26
	                    },
	                    'product' : {
	                    	shape: 'box',
	                    	margin: {
	                    		top: 10, bottom: 10, left: 20, right: 20
	                    	}
	                    }
                    }
                };
                  
                var network = new vis.Network(container, { nodes: nodes, edges: edges }, options);
                
                network.on("click",function(params){
                	if(!params.nodes[0] || typeof(params.nodes[0]) != 'string') {
                		return;
                	}
                	if(params.nodes.length > 0) {
                		if (params.nodes[0].indexOf("|") > 0){
                			var idarray = params.nodes[0].split('|');
                			$.ajax({
                    			url : 'server/getNodeInfo',
                    			type : 'GET',
                    			data : {'product': idarray[0], 'nodeId': idarray[2]},
                    			dataType : 'json',
                    			success: function(data){
                    				if (data.data.server) {
                    					self.fillNodeDetailInfo(data.data.server);
                    				} else {
                    					self.fillNodeDetailInfo();
                    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
                    					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                    						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                    					}
                	                	bootbox.alert({message:msgInfo, size:'middle'});
                    				}
                    			},
                    			error: function(msg){
                    				self.fillNodeDetailInfo();
                    				bootbox.alert({message:'连接服务器错误',size:'small'});
                    			},
                    			beforeSend : function(){
                    				mloadding.showLoadding();
                    			},
                    			complete : function(){
                    				mloadding.hideLoadding();
                    			}
                    		});
                    	} else {
                    		self.fillNodeDetailInfo();
                    	}
                	} 
                });
            },
            fillNodeDetailInfo : function(row) {
            	if (row) {
            		$("td[class='_product']").text(row.product ? row.product : "");
                	$("td[class='_type']").text(row.type ? row.type : "");
                	$("td[class='_nodeIp']").text(row.ip ? row.ip : "");
                	$("td[class='_nodeId']").text(row.nodeId ? row.nodeId : "");
                	$("td[class='_rmiRegistryPort']").text(row.rmiRegistryPort ? row.rmiRegistryPort : "");
                	$("td[class='_rmiServerPort']").text(row.rmiServerPort ? row.rmiServerPort : "");
                	$("td[class='_sshPort']").text(row.sshdPort ? row.sshdPort : "");
                	$("td[class='_registTime']").text(row.registTime ? row.registTime.replace(/,\d+/g,"") : "");
                	$("td[class*='_available']").html(
                			row.available != undefined 
                			? (row.available ? "<span class='badge badge-success float-left'>是</span>" : "<span class='badge badge-warning float-left'>否</span>") 
                			: "");
            	} else {
            		$("td[class='_product']").text("");
                	$("td[class='_type']").text("");
                	$("td[class='_nodeIp']").text("");
                	$("td[class='_nodeId']").text("");
                	$("td[class='_rmiRegistryPort']").text("");
                	$("td[class='_rmiServerPort']").text("");
                	$("td[class='_sshPort']").text("");
                	$("td[class='_registTime']").text("");
                	$("td[class*='_available']").html("");
            	}
            }
        });
        return app;
});