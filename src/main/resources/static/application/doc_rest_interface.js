define(['underScore','ko','pubsub', 'text!application/doc_rest_interface.html'],
    function (_, ko, hub, template) {
        var app = function () {
            this.o_template = $(template);
            this.restful = null;
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
                    url: "application/doc_rest_interface.postman.json",//地址，就是json文件的请求路径
                    dataType: "json",//数据类型可以为 text xml json  script  jsonp
    　　　　　　　　　 		success: function(result){//返回的参数就是 action里面所有的有get和set方法的参数
    　　　　　　　　　 			self.restful = result.item;
                    }
                });
            	hub.subscribe("restful_list", function (msg, data) {
                    self.switchRestfulList(data);
                });
            },
            render: function (container) {
                var self = this;
                var o_container = $(container);
                o_container.empty();
                o_container.append(this.o_template);
                //KO绑定菜单
                var vm_restful = {
            		restfuls: ko.observableArray(),
                    switchRestful: function (selected) {
                    	hub.publish("restful_list", selected);
                    }
                };
                vm_restful.restfuls(self.restful);
                ko.applyBindings(vm_restful, $('#rest_menus').get(0));
                
                //KO绑定列表
                this.vm_restfulList = {
            		apis: ko.observableArray(),
            		module: ko.observable()
                };
                ko.applyBindings(this.vm_restfulList, $('#rest_list').get(0));
                
                $('#rest_menus').find('a:first').click();
            },
            switchRestfulList: function (data) {
            	this.vm_restfulList.module(data.name);
            	this.vm_restfulList.apis(data.item);
            }
        });
        return app;
});