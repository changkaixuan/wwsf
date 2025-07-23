﻿define(['underScore', 'text!application/settings_layout_options.html'],
    function (_, template) {
        var app = function () {
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
            }
        });
        return app;
});