define(['underScore', 'text!application/doc_character_terminal.html'], function(_,template) {
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
		}
	});

	return app;
});