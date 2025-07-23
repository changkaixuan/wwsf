define(['underScore', 'text!application/home.html'], function(_,template) {
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
			$('#btn_view_detail_rest_interface').bind('click', function(){$("a[data-module='doc_rest_interface']").click();});
			$('#btn_view_detail_release').bind('click', function(){$("a[data-module='doc_release']").click();});
			
			$('#btn_to_learnmore_about_wws').bind('click', function(){$("a[data-module='doc_product_manual']").click();});
			$('#btn_to_download_wws').bind('click', function(){$("a[data-module='doc_release']").click();});
			$('#btn_to_wws_docs').bind('click', function(){$("a[data-module='doc_release']").click();});
		}
	});

	return app;
});