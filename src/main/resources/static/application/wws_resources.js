﻿define([
	'underScore', 
	'text!application/wws_resources.html', 
	'alertor',
	'mloadding', 
	'css!js/fancytree/skin-win8/ui.fancytree.css', 
	'fancytree',
	'jqueryUI',
	'css!css/pagination.css', 'css!css/jquery.json-viewer.css',
	'bootstrap-select', 'bootstrap-table',
	'jquery-jsoneditor', 'jquery-jsonviewer'],
    function (_, template, alertor, mloadding) {
        var app = function (config) {
            this.o_template = $(template);
            this.displayModal = 1;
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
                $('#dirTree').fancytree({
                	checkbox : false,
                	selectMode : 1,
                	'click' : function(event, data){
                		$("#prod_menu").find("input").val(data.node.key);
                		if (self.displayModal == 1) {
                			self.graphMainFolderTable(data.node.data.product, data.node.key);
                		} else {
                			self.graphMainFolder(data.node.data.product, data.node.key);
                		}
                	}});
                $('#btn_show_file_imgs').bind('click', 
            		function(){ 
                		self.displayModal = 0;
                		var tree = $.ui.fancytree.getTree("#dirTree");
                		var selNodes = tree.getActiveNode();
                		if (selNodes && selNodes != null) $(selNodes.span).trigger("click"); 
            		});
                $('#btn_show_file_list').bind('click', 
            		function(){ 
                		self.displayModal = 1;
                		var tree = $.ui.fancytree.getTree("#dirTree");
                		var selNodes = tree.getActiveNode();
                		if (selNodes && selNodes != null) $(selNodes.span).trigger("click"); 
                	});
                $('#btn_to_create_folder_modal').bind('click', function(event){
            		self.toCreateFolderModal();
                });
                $('#btn_to_upload_file_modal').bind('click', function(event){
            		self.toUploadFilesModal();
                }); 
                $('#btn_new_folder_save').bind('click', function(event){
            		var fname = $("#createFolderModal").find("input[name=folderName]").val();
            		if(null == fname || fname == ""){
                		alertor.dangerAlert('操作失败，请输入文件夹名称','small');
            			return false;
                	} 
            		var tree = $.ui.fancytree.getTree("#dirTree");
            		var selNodes = tree.getActiveNode();
            		var product = selNodes.data.product;
            		var params = {product:selNodes.data.product, parentFolder:selNodes.key, folderName:fname};
            		$.ajax({ 
            			data : params,
            			url : 'resource/makeFolder',
            			type : 'POST',
            			contentType:'application/x-www-form-urlencoded',
            			success: function(data){
            				if(data.success){ 
            					$("#createFolderModal").modal("hide");
            					self.graphDirTree(product);
            				}else{
            					alertor.dangerAlert('创建目录失败','small');
            				}
            				mloadding.hideLoadding();
            			},
            			error: function(msg){
            				alertor.dangerAlert('连接服务器错误','small');
            				mloadding.hideLoadding();
            			}
            		}); 
                });
                
                $.ajax({
            		async:false,
        			url : 'product/selectProduct',
        			type : 'GET',
        			contentType:'application/json',
        			success: function(data){
        				if (data.success) {
        						var products = data.data.products;
        						var prodSel = $("#prod_menu").find(".dropdown-menu");
        						var urlInput = $("#prod_menu").find("input");
        						prodSel.empty();
        						$.each(products,function(i){
        							var item = $('<a class="dropdown-item" href="#" >' + products[i].pName + '</a>');
        							item.bind("click", function(){
        								  urlInput.val("/" + products[i].pId);
        								  self.graphDirTree(products[i].pId);
        							});
        							prodSel.append(item);
        						})
        						if (products.length > 0) {
        							prodSel.children(":first").trigger("click");
        						}
        				} else {
        					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	alertor.dangerAlert(msgInfo,'middle');
        				}
        			},
        			error: function(msg){
        				alertor.dangerAlert('连接服务器错误','small');
        			}
              	});
            }, 
            graphDirTree : function(product) {
            	var self = this;
            	$.ajax({
            		async:false,
        			url : '/resource/dirTree',
        			type : 'GET',
        			dataType : 'json',
        			data:{'product':product},
        			success: function(data){
        				if(data.success){
        					var treeData = [];
        					var dirTreeData = data.data.dirTreeData;
        					if (dirTreeData && dirTreeData != null){
        						treeData.push(dirTreeData);
        						var tree = $.ui.fancytree.getTree("#dirTree");
        						tree.reload(treeData);
        						$('#dirTree').children(":first").children(":first").trigger("click");
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
            graphMainFolder : function(product, folder){
            	var self = this;
            	var folder_main_container = $("#folder_main_container");
            	folder_main_container.empty();
            	folder_main_container.addClass("d-flex").addClass("flex-wrap");
            	$.ajax({
            		async:false,
        			url : '/resource/folder',
        			type : 'GET',
        			dataType : 'json',
        			data:{'product':product, 'folder': folder},
        			success: function(data){
        				if(data.success){
        					var folderList = data.data.folder;
        					if (folderList && folderList != null){
    							$.each(folderList.children ,function(i,fs){
        							folder_main_container.append(self.getFileTempl(fs));
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
            getFileTempl : function(fs){
            	return '<div id="'+ fs.key +'" class="card m-1 noborder-noshadow-nobackground" style="width:75px;height:85px" title="'+ fs.title + '&#10;' + fs.lastModified + '&#10;' + fs.length +'">' +
						   '<div class="card-body p-1 text-center">' + 
							   '<div class="fal '+ (fs.folder=='true' ? 'fa-folder' : 'fa-file') +' fa-4x"></div>' + 
							   '<p class="card-text file-name-clipping">'+ fs.title +'</p>' + 
						   '</div>' +
		               '</div>';
            },
            graphMainFolderTable : function(product, folder){
            	var self = this;
            	var folder_main_container = $("#folder_main_container");
            	folder_main_container.empty();
            	folder_main_container.removeClass("d-flex").removeClass("flex-wrap");
            	folder_main_container.append('<table data-toggle="table" class="table table-sm table-striped"></table>');
            	var tab = folder_main_container.children(":first");
            	tab.bootstrapTable({
            		theadClasses: 'thead-light',
                    url: 'resource/folder',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    striped: true,                      //是否显示行间隔色
                    cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                    sortable: false,                     //是否启用排序
                    queryParamsType:'undefined',
                    queryParams: function (params) {
                    	return { product: product, folder: folder };
                    },           //传递参数（*）
                    uniqueId: "key",               //每一行的唯一标识，一般为主键列
                    showToggle:false,                    //是否显示详细视图和列表视图的切换按钮
                    buttonsClass: 'sm btn-primary',
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                   //是否显示父子表
                    showColumns: false,
                    columns: [
                    	{field: 'title', title: '文件名'}, 
                        {field: 'lastModified', title: '修改日期'}, 
                        {field: 'serverType', title: '类型'},
                        {field: 'length', title: '大小'}
                    ],
                    responseHandler: function (res) {
                    	return res.data.folder.children;
                    } 
                });
            },
            toCreateFolderModal : function(){
            	var self = this;
            	var createFolderModal = $("#createFolderModal");
            	createFolderModal.find("input[name=folderName]").val('');
            	createFolderModal.modal('show');
            },
            toUploadFilesModal : function(){
            	var self = this;
            	var uploadFilesModal = $("#uploadFilesModal");
            	uploadFilesModal.modal('show');
            } 
        }); 
        return app;
});