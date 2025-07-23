define([ 'underScore', 'text!application/doc_user_guide.html' ], function(_, template) {
	var app = function() {
		this.el = $(template);
	}
	_.extend(app.prototype, {
		load : function() {
		},
		render : function(container) {
			var self = this;
			var main_container = $(container);
			main_container.empty();
			var main_height = main_container.outerHeight();
			main_container.append(this.el);

			var catal = $('#fixed_toc');
			catal.css({"height" : main_height - 32});
			var fixedPos = catal.offset().top;
			$.event.add(window, 'scroll', function(){ self.fixedToc(fixedPos); });
			$.event.add(window, 'resize', function(){ self.fixedToc(fixedPos); });
		},
		fixedToc : function(fixedPos){
			var catal = $('#fixed_toc');
			var scrollTop = $(window).scrollTop();
			catal.css({
				"position": (scrollTop > (fixedPos - 80) ? 'fixed' : 'static'), 
				"top"     : (scrollTop > (fixedPos - 80) ? "80px" : ""), 
				"height"  : $(window).height() - 141,
				"width"   : catal.parent().width()
			});
		}
	});

	return app;
});