define([ 'underScore', 'text!application/doc_contact.html' ], function(_, template) {
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
			main_container.append(this.el);
		}
	});

	return app;
});