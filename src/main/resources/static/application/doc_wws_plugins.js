define([
	'underScore', 
	'text!application/doc_wws_plugins.html',	
	'css!js/bootstrap/css/bootstrap-table.css', 
	'bootstrap-table'], function(_,template) {
	var app = function() {
		this.el = $(template);
	}

	_.extend(app.prototype, {
		load : function() {
		},
		render : function(container) {
			var self = this;
			var jcontainer = $(container);
			jcontainer.empty();
			jcontainer.append(this.el);
			
			$.ajax({ 
        		url : '/wwsapi/pluginInfo',
        		type : 'GET',
        	    contentType:'application/json',
        		success: function(data){
        			if (data.success) {
        				if (data.data.plugins && data.data.plugins.length > 0) {
        					for (var i = 0; i < data.data.plugins.length; i++) {
        						$("#plu_badges").append('<a href="#" data-liftaction="#' + data.data.plugins[i].name + '"><span class="badge badge-primary mr-1">' + data.data.plugins[i].name + '</span></a>');
        						$("#plu-detail").append(
        						'<div class="card mb-g" id="' + data.data.plugins[i].name + '">' + 
        							'<div class="card-body">' + 
        								'<h2 class="fw-700 mb-g">' + 
        									data.data.plugins[i].name + 
        									'<small>' + data.data.plugins[i].desc + '</small>' + 
        								'</h2>' + 
        								'<div class="table-responsive">' +
        									'<table id="tab_plu_param_' + data.data.plugins[i].name + '" class="table table-sm table-bordered table-hover"></table>' +
        								'</div>' +
    								'</div>' + 
        						'</div>');
        						$('#tab_plu_param_' + data.data.plugins[i].name).bootstrapTable({
            	            		theadClasses: 'thead-light',
            	            		data: data.data.plugins[i].pluginParameters,
            	                    striped: true, 
            	                    columns: [
            	                    	{field: 'paramName', title: '参数名', width: '160px'}, 
            	                    	{field: 'dataType', title: '参数类型', width: '120px'}, 
            	                    	{field: 'isRequired', title: '必填', width: '80px', 
            	                    		formatter: function (value, row, index) {
	            	                    		if (value == 0){
	            	                    			return "<span class='badge badge-success'>否</span>";
	            	                    		} else {
	            	                    			return "<span class='badge badge-danger'>是</span>";
	            	                    		}
	            						     }
            	                    	}, 
            	                    	{field: 'paramDefValue', title: '默认值', width: '80px',
            	                    		formatter: function (value, row, index) {
            	                    			return value == '' ? '无' : value;
	            						    }
            	                    	},
            	                    	{field: 'paramDesc', title: '插件描述',
            	                    		formatter: function (value, row, index) {
            	                    			if (row.dataType == 'RANGE') {
            	                    				if (row.dataRange && row.dataRange != null) {
            	                    					value = value + "<br>取值范围：<br>";
            	                    					var dataRangeArray = JSON.parse(row.dataRange);
            	                    					value = value + 
        	                    						'<table class="table table-sm">' + 
	            	        								'<tbody>';
		            	                    					for (var r = 0; r < dataRangeArray.length; r++) {
		            	                    						value = value +
		            	                    						'<tr><td width="100px">' + dataRangeArray[r].key + '</td><td nowrap>' + dataRangeArray[r].value + '</td></tr>';
		            	                    					}
            	                    					value = value + 
            	                    						'</tbody>' +
	            	        							'</table>';
            	                    					
            	                    				}
            	                    			}
            	                    			return value;
	            						    }
            	                    	}]
            	                });
        					}
        					$('#tab_plu_overall').bootstrapTable({
        	            		theadClasses: 'thead-light',
        	            		data: data.data.plugins,
        	                    striped: true, 
        	                    columns: [{field: 'name', width: '120px', title: '插件名称'}, {field: 'desc', title: '插件描述' }]
        	                });
        					
        					$("a[data-liftaction]", jcontainer).bind('click',function(){
        						var floor = $(this).attr('data-liftaction');
        						self.lift($(floor));
        					});
        				}
        			}
        		},
        		error: function(msg){
        			
        		}
        	});
		},
		lift : function(el){
			setTimeout(function(){
        		$('html,body').animate({scrollTop:el.offset().top - 70}, 800);
        	},300);
		},
		padRight : function(str, l) {
			var sl = str.length;
			if (sl < l) {
				for (i = 0; i < l-sl; i ++) {
					str = str + '&nbsp;';
				}
			}
			return str
		}
	});

	return app;
});