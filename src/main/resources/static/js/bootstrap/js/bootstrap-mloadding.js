define(['jquery'],function ($) {
	
	/*var mLoadding = {
		counter : 0,
		showLoadding : function(){
			var self = this;
			//backdrop:'static' 使点击空白处遮罩层不会消失
	    	//keyboard:false 按Tab键遮罩层不会消失，默认值为true
			var loaddingModal = $("#myLoaddingModal");
			self.counter++;
	    	if(loaddingModal.data("bs.modal") != undefined && loaddingModal.data("bs.modal").isShown){
	    		return false;
	    	}
	    	loaddingModal.modal({backdrop:'static',keyboard:false});
	    	loaddingModal.modal("show");
	    	return true;
		},
		hideLoadding : function(){
			var self = this;
			self.counter --;
			if (self.counter == 0) {
				setTimeout(function(){
					$("#myLoaddingModal").modal("hide");
					$("#btn_loadding_modal_close").click();
		        },500);
			}
		}
	};
	return mLoadding;*/
	
	var mLoadding = {
		counter : 0,
		showLoadding : function(){
			var self = this;
			//backdrop:'static' 使点击空白处遮罩层不会消失
	    	//keyboard:false 按Tab键遮罩层不会消失，默认值为true
			self.counter++;
			if(self.counter != 1){
				return false;
			}
			var loaddingModal = $("#myLoaddingModal");
	    	loaddingModal.modal({backdrop:'static',keyboard:false});
	    	loaddingModal.modal("show");
	    	return true;
		},
		hideLoadding : function(){
			var self = this;
			setTimeout(function(){
				$("#myLoaddingModal").modal("hide");
				$("#btn_loadding_modal_close").click();
				self.counter = 0;
	        },300);
		}
	};
	return mLoadding;
	
});