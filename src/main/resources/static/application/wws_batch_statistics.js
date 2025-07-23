define(['ko', 'underScore', 
	'text!application/wws_batch_statistics.html', 
	'mloadding',
	'css!css/pagination.css', 
	'bootstrap-table',
	'skillbar',
	'css!css/an-skill-bar.css'],
    function (ko, _, template, mloadding) {
        var app = function (config) {
            this.o_template = $(template);
            this.pageSize = 20;
            this.pageNumber = 1;
            this.vm_batchs = null;
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
                
                var sel_prd = $('#collapse_productsQ');
                sel_prd.empty();
                sel_prd.append("<option value=''>请选择产品</option>");
	          	$.ajax({
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
                
                self.vm_batchs = {
            		batchs: ko.observableArray(),
            		//展示多彩的进度条，以batch的hashcode%size选取class
            		skillcsss: [
            			'html','css','js', 'php', 'jquery', 'sass', 'less', 'react', 'nodejs', 'angular', 
            			'vue', 'meteor', 'backbone', 'ember', 'mysql', 'django', 'agile', 'photoshop', 'illustrator',
            			'indesign', 'laravel'],
            		refreshBatch: function(item, event){
            			var index = self.vm_batchs.batchs.indexOf(this);
            			$.ajax({ 
                     		url : 'batch/info?product=' + item.product + '&batch=' + item.batch,
                     		type : 'GET',
                     		contentType:'application/json',
                     		beforeSend : function(){
                				mloadding.showLoadding();
                			},
                			complete : function(){
                				mloadding.hideLoadding();
                			},
                     		success: function(data){
                     			var rs = $.parseJSON(data);
                				if(rs.success){
                					self.vm_batchs.batchs.replace(item, rs.data.batch_info);
                        			$('.skillbar').skillbar({speed : 500});
                        			var batch_a = $("#a_batch_"+index);
                                	$(batch_a.attr("href")).addClass("show");
                                	setTimeout(function(){
                                		$('html,body').animate({scrollTop:batch_a.parent().offset().top - 70}, 500);
                                	},300);
                				}else{
                					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
                					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
                						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
                					}
            	                	bootbox.alert({message:msgInfo, size:'middle'});
                				}
                     		},
                     		error: function(msg){
                     			bootbox.alert({message:'connect server error',size:'small'});
                     		}
                     	});
            		},
            		clearBatch: function(item){
            			/*
            			if (item.state != 10){
            				bootbox.alert({message:'Batch must be Quit!',size:'small'});
            				return;
            			}*/
            			var tConfirm = true;
            			bootbox.confirm({
                    		size : 'small',
                    		message : 'Are you sure clean data of batch？' + ((item.state!=10) ? 'Even Batch is unfinished' : ''),
                    		callback : function (result) {
                    			if(result && tConfirm){
                    				tConfirm = false;
                    				$.ajax({ 
                                 		url : 'batch/info?product=' + item.product + '&batch=' + item.batch,
                                 		type : 'DELETE',
                                 		contentType:'application/json',
                                 		beforeSend : function(){
                            				mloadding.showLoadding();
                            			},
                            			complete : function(){
                            				mloadding.hideLoadding();
                            			},
                                 		success: function(data){
                                 			var rs = $.parseJSON(data);
                            				if(rs.success){
                            					self.loadPageData(self.pageNumber);
                            					bootbox.alert({message:'successful', size:'small'});
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
                };
                ko.applyBindings(self.vm_batchs, $('#batchs_skill').get(0));
                //self.loadPageData(1);
             	$("#btn_redis_batchStaticQuery").bind("click",function(){
             		if ($("#collapse_productsQ").val() == "") {
             			bootbox.alert({message:'请选择产品',size:'small'});
             			return;
             		}
             		self.loadPageData(1); 
             	});
             	$('#btn_redis_clear', this.o_container).bind('click', function(event){
                	$("#collapse_productsQ").val("");
        	   		$("#collapse_batchNo").val("");
        	   		self.loadPageData(1);
                });
            },
    		cloneSource : function (source) {
			    var obj = {};
			    for (var p in source)
			      obj[p] = source[p];
			    return obj;
			},
            loadPageData:function(pageNumber){
            	var self = this;
        		var product = $("#collapse_productsQ").val();
        		var	batchKey =$("#collapse_batchNo").val();
        		$.ajax({ 
        			url :'batch/list',
        			type : 'GET',
        			data: {'product': product, 'pageNumber': pageNumber, 'pageSize': self.pageSize, 'batchKey': batchKey},
        			contentType:'application/json',
        			beforeSend : function(){
        				mloadding.showLoadding();
        			},
        			complete : function(){
        				mloadding.hideLoadding();
        				var checkbox_all = $("input[class=_selectAllBatch]");
        				checkbox_all.prop("checked", false);
        				// 绑定全部选中 和 取消全部选中
        				checkbox_all.unbind('click').bind('click', function() {
                     		var me = this;
                     		var checkedme = $(me).prop("checked");
                     		var checkbox_single = $("input[class='_cbox_single_batch']");
                     		$.each(checkbox_single,function(i){ $(checkbox_single[i]).prop("checked", checkedme); });
                     	});
        				// 绑定删除按钮
        				$("#btn_batch_del").unbind('click').bind('click', function() {
                     		var checkboxArr = $("input[class='_cbox_single_batch']:checked");
                     		if(checkboxArr.length<1) {
                        		bootbox.alert({message:'请选择需要删除的批量任务',size:'small'});
                        		return;
                        	}
                     		var productArr = [];
                     		var batchArr = [];
                     		for(var i=0,len=checkboxArr.length; i<len; i++) {
                     			productArr.push($(checkboxArr[i]).parent().find('input[name=product]').val());
                     			batchArr.push($(checkboxArr[i]).parent().find('input[name=batch]').val());
                     		}
                     		var tConfirm = true;
                    		bootbox.confirm({
                        		size : 'small',
                        		message : '是否确定删除所选'+batchArr.length+'个批量任务数据？',
                        		callback : function (result) {
                        			if(result && tConfirm){
                        				tConfirm = false;
                                		$.ajax({
                                			url : 'batch/infos',
                                			type : 'DELETE',
                                			dataType : 'json',
                                			data:{productArr:productArr.toString(), batchArr:batchArr.toString()},
                                			success: function(data){
                                				if(data.success){
                                					bootbox.alert({message:'删除成功',size:'small'});
                                					self.loadPageData(self.pageNumber);
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
                                			},
                                			beforeSend : function(){
                                				mloadding.showLoadding();
                                			},
                                			complete : function(){
                                				mloadding.hideLoadding();
                                			}
                                		});
                            		}
                            	 }
                        	});
                     	});
        			},
        			success: function(data){
        				var rs = $.parseJSON(data);
        				if(rs.success){
        					self.pageNumber = rs.data.pageNumber;
        					self.vm_batchs.batchs(rs.data.rows);
        					self.createPagination(rs.data.total, rs.data.pageNumber);
        				}else{
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	bootbox.alert({message:msgInfo, size:'middle'});
        				}
        				$('.skillbar').skillbar({speed : 1000});
        			},
        			error: function(msg){
        				bootbox.alert({message:'连接服务器错误',size:'small'});
        				$('.skillbar').skillbar({speed : 1000});
        			}
        		});
            },
            createPagination:function(total, currentPage){
            	var self = this;
            	var batchs_page = $("#batchs_pagination");
            	var pagination_el = $('#collapse_pagination');
            	if (pagination_el) pagination_el.remove();
            	batchs_page.append('<div id="collapse_pagination" class="right flickr"></div>');
            	$("#collapse_pagination").pagination(total,{
            		callback:function(pageNumber, obj){
        				self.loadPageData(pageNumber+1);
        				return obj;
        			},
        			prev_text:"<i style='margin:5px 10px;'></i>上一页",
        			next_text:"下一页",
            		items_per_page:self.pageSize,//每页的数据个数
            		num_display_entries:5,//两侧首尾分页条目数
            		current_page:currentPage-1,//当前页
            		num_edge_entries:3 //连续分页主体部分分页条目数
            	});
            }
        });
        return app;
});