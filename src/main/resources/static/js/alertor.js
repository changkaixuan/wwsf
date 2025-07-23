define(function () {
        return {
        	infoAlert : function(msg, size) {
    	    	var self = this;
    	    	bootbox.alert({ message:self.infoMessage(msg), closeButton: false, size:size });
    	    },
        	dangerAlert : function(msg, size) {
        		var self = this;
    	    	bootbox.alert({ message:self.dangerMessage(msg), closeButton: false, size:size });
    	    },
    	    successAlert : function(msg, size) {
    	    	var self = this;
    	    	bootbox.alert({ message:self.successMessage(msg), closeButton: false, size:size });
    	    },
    	    warningAlert : function(msg, size) {
    	    	var self = this;
    	    	bootbox.alert({ message:self.warningMessage(msg), closeButton: false, size:size });
    	    },
    	    alert : function(msg, size) {
    	    	bootbox.alert({ message:msg, closeButton: false, size:size });
    	    },
    	    infoMessage : function(msg){
    	    	return '<div class="alert alert-info mb-0 py-2"    >' + msg + '<div>';
    	    },
    	    warningMessage : function(msg){
    	    	return '<div class="alert alert-warning mb-0 py-2" >' + msg + '<div>';
    	    },
    	    dangerMessage : function(msg){
    	    	return '<div class="alert alert-danger mb-0 py-2"  >' + msg + '<div>';
    	    },
    	    successMessage : function(msg){
    	    	return '<div class="alert alert-success mb-0 py-2" >' + msg + '<div>';
    	    }
        };
});
