define(['underScore', 'text!application/doc_product_manual.html'], function(_,template) {
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
			
			$("a[data-liftaction]", jcontainer).bind('click',function(){
				var floor = $(this).attr('data-liftaction');
				self.lift($(floor));
			});
		},
		lift : function(el){
			setTimeout(function(){
        		$('html,body').animate({scrollTop:el.offset().top - 70}, 800);
        	},300);
		}
	});

	return app;
});