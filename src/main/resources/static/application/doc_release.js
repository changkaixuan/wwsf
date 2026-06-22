define(['underScore','ko', 'text!application/doc_release.html'],
    function (_, ko, template) {
        var app = function () {
            this.o_template = $(template);
            this.releaseInfo = null;
            this.vm_restfulList = null;
        }
        _.extend(app.prototype, {
        	initialize:function(){
                
            },
            load: function () {
            	var self = this;
            	$.ajax({
                    type: "GET",//请求方式
                    async: false,
                    url: "release/info",//地址
                    contentType:'application/json',
            		success: function(data){
	    				if(data.success){
	    					self.releaseInfo = data.data.releases;
	    				}else{
	    					var msgInfo = '操作失败,服务器处理出错<br>' + data.info;
        					if (data.detailInfo && data.detailInfo != null && data.detailInfo != '') {
        						msgInfo = msgInfo + '<br><a title="'+data.detailInfo+'"  style="color:red"><i class="fal fa-exclamation-circle fa-fw"></i>查看详细信息</a>'
        					}
    	                	bootbox.alert({message:msgInfo, size:'middle'});
	    					return;
	    				}
	    			},
	    			error: function(msg){
	    				console.log(msg);
	    				bootbox.alert({message:'连接服务器错误,'+msg,size:'small'});
	    				return;
	    			}
                });
            },
            render: function (container) {
                var self = this;
                var o_container = $(container);
                o_container.empty();
                o_container.append(self.o_template);
                self.vm_restfulList = {
            		apis: ko.observableArray()
                };
                ko.applyBindings(self.vm_restfulList, $("#release_list")[0]);
                
                for(var i=0,len=self.releaseInfo.length; i<len; i++) {
                	var info = self.releaseInfo[i];
                	$.ajax({type: "GET", async: false, url: "release/" + info.textUrl, success: function(data){ info.textHtml = data; }});
    			}
                self.vm_restfulList.apis(self.releaseInfo);
            }
        });
        return app;
});