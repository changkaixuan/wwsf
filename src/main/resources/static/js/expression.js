define(['jquery'],function ($) {	
	var expressionModal = {};
	//控制选项卡显示
	var tabArr = [{id:'expressionModal_second',name:'秒'}
	             ,{id:'expressionModal_min',name:'分'}
	             ,{id:'expressionModal_hour',name:'时'}
	             ,{id:'expressionModal_day',name:'日'}
	             ,{id:'expressionModal_month',name:'月'}
	             ,{id:'expressionModal_week',name:'周'}
	             ,{id:'expressionModal_year',name:'年'}];
	//显示所有选项卡
	expressionModal.displayAllTabArr = ["expressionModal_second",
        					"expressionModal_min",
        					"expressionModal_hour",
        					"expressionModal_day",
        					"expressionModal_month",
        					"expressionModal_week",
        					"expressionModal_year"];
	//显示选项卡（日、周、月、年）
	expressionModal.displayDWMYTabArr = ["expressionModal_day",
							"expressionModal_month",
							"expressionModal_week",
							"expressionModal_year"];
	//显示选项卡（时、日、周、月、年）
	expressionModal.displayHDWMYTabArr = ["expressionModal_hour",
		                    "expressionModal_day",
							"expressionModal_month",
							"expressionModal_week",
							"expressionModal_year"];
	
	//时间选项卡Id
	var tabIdArr = ["expressionModal_second",
			        "expressionModal_min",
			        "expressionModal_hour",
			        "expressionModal_day",
			        "expressionModal_month",
			        "expressionModal_week",
			        "expressionModal_year"];
	//时间选项卡中指定表格的Id
	var appointTableIdArr = ["expressionModal_second_pointValue",
						     "expressionModal_min_pointValue",
						     "expressionModal_hour_pointValue",
						     "expressionModal_day_pointValue",
						     "expressionModal_month_pointValue",
						     "expressionModal_week_pointValue"];
	//指定表格中按钮最大值（例如：秒最大值为60,小时最大值24）
	var timeMaxArr = [60, 60, 24, 31, 12, 7];
	//初始化表达式模态框
	expressionModal.initExpression = function(inputId,inputValue,displayTabArr){
		//生成时间选项卡
		if(displayTabArr == null){
			displayTabArr = expressionModal.displayAllTabArr;
		}
		var tabClass = "nav-link mt-0 px-4";
		var activeTabClass = "nav-link active mt-0 px-4"; //默认第一个为活动的选项卡
		var firstTabFlag = true;
		$("#expressionModal_myTab").empty();
		for(var i=0;i<tabArr.length;i++){
			if($.inArray(tabArr[i].id, displayTabArr) != -1){
				var curTabClass = tabClass;
				if(firstTabFlag){
					$("#"+tabArr[i].id).attr("class","tab-pane active");
					curTabClass = activeTabClass;
					firstTabFlag = false;
				}else{
					$("#"+tabArr[i].id).attr("class","tab-pane fade");
				}
				$("#expressionModal_myTab").append("<li class='nav-item'><a href='#"+tabArr[i].id+"' data-toggle='tab' class='"+curTabClass+"' style='width:62px; text-align:center;'>"+tabArr[i].name+"</a></li>");
			}else{
				$("#"+tabArr[i].id).attr("class","tab-pane fade");
			}
		}
		//初始表达式模态框事件
		for(var i=0;i<tabIdArr.length;i++){
			if($.inArray(tabIdArr[i], displayTabArr) == -1){
				continue;
			}
			var inputValueIndex = displayTabArr.indexOf(tabIdArr[i]); //输入框值下标
			if(tabIdArr[i] == "expressionModal_year"){//时间[年]选项卡
				expressionModal.typeChange(tabIdArr[i], "", inputValueIndex); //时间[年]选项卡中操作类型、数字输入框加事件
			}else{
				expressionModal.typeChange(tabIdArr[i], appointTableIdArr[i], inputValueIndex); //时间[秒、分、时、日、月、周]选项卡中操作类型、数字输入框加事件
				expressionModal.initAppointBtnTableEvent(appointTableIdArr[i], inputValueIndex); //时间[秒、分、时、日、月、周]选项卡中指定值事件
			}
		}
		//确定按钮加事件
		var expressionModalSubmit = $("#expressionModal_submit");
		expressionModalSubmit.find("input[name=submit]").unbind('click').bind('click', function(event){
			var expression = expressionModalSubmit.find("input[name=expression]").val();
			var validateResult = validate.expression(expression,displayTabArr);
			if(validateResult != null){
				alert(validateResult);
				return;
			}
			$("#"+inputId).val(expression);
			$('#expressionModal').modal('hide');
		});
		//设置表达式模态框初始值
		if(null == inputValue || inputValue == ""){
			if(displayTabArr.length == 4){//日、周、月、年
				inputValue = "* * ? *";
			}else if(displayTabArr.length == 5){//时、日、周、月、年
				inputValue = "* * * ? *";
			}else{
				inputValue = "* * * * * ?";
			}
		}else{
			var validateResult = validate.expression(inputValue,displayTabArr);
			if(validateResult != null){
				alert(validateResult);
				if(displayTabArr.length == 4){//日、周、月、年
					inputValue = "* * ? *";
				}else if(displayTabArr.length == 5){//时、日、周、月、年
					inputValue = "* * * ? *";
				}else{
					inputValue = "* * * * * ?";
				}
			}
		}
		//设置秒、分、时、日、周、月和年选项卡值
		expressionModal.setExpressionRelaValue(inputValue.split(" "),displayTabArr);
		expressionModalSubmit.find("input[name=expression]").val(inputValue); //设置表达式输入框值
		//时间选项卡，默认选中秒
		//$("#expressionModal_myTab a:first").tab("show");
		//显示表达式模态框
		$('#expressionModal').modal('show');
	}
	
	expressionModal.setExpressionRelaValue = function(expressionArr,displayTabArr){
		//设置时间选项卡[年]中输入框最新值(设置年选项卡周期两个值的最新值)
		if(expressionArr.length == 6 || (expressionArr.length == 7 && expressionArr[6].indexOf("-") == -1)){
			var curYear = new Date().getFullYear();
			$("#"+tabIdArr[6]).find("input[name=periodStart]").attr("min",curYear);
			$("#"+tabIdArr[6]).find("input[name=periodEnd]").attr("min",curYear+1);
		}
		if(expressionArr.length == 3 || (expressionArr.length == 4 && expressionArr[3].indexOf("-") == -1)){
			var curYear = new Date().getFullYear();
			$("#"+tabIdArr[6]).find("input[name=periodStart]").attr("min",curYear);
			$("#"+tabIdArr[6]).find("input[name=periodEnd]").attr("min",curYear+1);
		}
		if(expressionArr.length == 4 || (expressionArr.length == 5 && expressionArr[4].indexOf("-") == -1)){
			var curYear = new Date().getFullYear();
			$("#"+tabIdArr[6]).find("input[name=periodStart]").attr("min",curYear);
			$("#"+tabIdArr[6]).find("input[name=periodEnd]").attr("min",curYear+1);
		}
		//设置时间选项卡[秒、分、时、日、周、月]中输入框默认值
		$.each($("#expressionModal").find("input[type=number]"),function(index,curNumber){
			curNumber.value = curNumber.min;
		});
		//根据表达式的值给时间选项卡赋值
		for(var i=0;i<tabIdArr.length;i++){
			if($.inArray(tabIdArr[i], displayTabArr) == -1){
				continue;
			}
			var inputValueIndex = displayTabArr.indexOf(tabIdArr[i]); //输入框值下标
			if(expressionArr[inputValueIndex] == undefined || expressionArr[inputValueIndex] == ""){//不存在年份
				$("#"+tabIdArr[i]).find("input:radio[value=notAppoint]").click();
			}else if(expressionArr[inputValueIndex] == "*"){ //每秒
				$("#"+tabIdArr[i]).find("input:radio[value=per]").click();
			}else if(expressionArr[inputValueIndex].indexOf("-") != -1){ //周期
				$("#"+tabIdArr[i]).find("input:radio[value=period]").click();
				$("#"+tabIdArr[i]).find("input[name=periodStart]").val(expressionArr[inputValueIndex].split("-")[0]);
				$("#"+tabIdArr[i]).find("input[name=periodEnd]").val(expressionArr[inputValueIndex].split("-")[1]);
			}else if(expressionArr[inputValueIndex].indexOf("/") != -1){ //循环
				$("#"+tabIdArr[i]).find("input:radio[value=loop]").click();
				$("#"+tabIdArr[i]).find("input[name=loopStart]").val(expressionArr[inputValueIndex].split("/")[0]);
				$("#"+tabIdArr[i]).find("input[name=loopEnd]").val(expressionArr[inputValueIndex].split("/")[1]);
			}else if(expressionArr[inputValueIndex] == "?" || expressionArr[inputValueIndex].indexOf(",") != -1 || validate.isNumber(expressionArr[inputValueIndex])){ //指定(?：未指定或指定所有; 指定一个：1个数字; 指定多个：逗号隔开)
				$("#"+tabIdArr[i]).find("input:radio[value=appoint]").click();
				$("#"+appointTableIdArr[i]).show(); //时间[秒、分、时、日、月、周]选项卡中指定值表格显示
				//选项卡中指定值表格按钮默认选中
				var timesArr = [];
				if(expressionArr[inputValueIndex] != "?"){
					timesArr = expressionArr[inputValueIndex].split(",");
				}
				if(timesArr.length > 0){
					$.each($("#"+appointTableIdArr[i]).find("button"),function(index,btn){//循环加点击事件
						if($.inArray(btn.value,timesArr) != -1){//选中
							$(btn).attr("class","btn btn-sm btn-primary");
						}
					});
				}
			}else if(expressionArr[inputValueIndex].indexOf("W") != -1){ //日/每月
				$("#"+tabIdArr[i]).find("input:radio[value=perMonth]").click();
				$("#"+tabIdArr[i]).find("input[name=latestWorkday]").val(expressionArr[inputValueIndex].replace("W",""));
			}else if(expressionArr[inputValueIndex] == "L"){ //日/本月最后一天
				$("#"+tabIdArr[i]).find("input:radio[value=curMonthLastDay]").click();
			}else if(expressionArr[inputValueIndex].indexOf("#") != -1){ //周/指定
				$("#"+tabIdArr[i]).find("input:radio[value=appointWeekAndWeek]").click();
				$("#"+tabIdArr[i]).find("input[name=appointWeek]").val(expressionArr[inputValueIndex].split("#")[0]);
				$("#"+tabIdArr[i]).find("input[name=appointWeekDay]").val(expressionArr[inputValueIndex].split("#")[1]);
			}else if(expressionArr[inputValueIndex].indexOf("L") != -1){ //周/本月最后一个星期
				$("#"+tabIdArr[i]).find("input:radio[value=curMonthLastOneWeek]").click();
				$("#"+tabIdArr[i]).find("input[name=curMonthLastOneWeek]").val(expressionArr[inputValueIndex].replace("L",""));
			}
		}
	}
	
	//初始化指定按钮事件
	expressionModal.initAppointBtnTableEvent = function(appointTableId,inputValueIndex){
		$("#"+appointTableId).hide(); //时间[秒、分、时、日、月、周]选项卡中指定值表格隐藏
		$.each($("#"+appointTableId).find("button"),function(index,but){//循环加点击事件
			$(but).attr("class","btn btn-sm btn-default");
        	$(but).unbind('click').bind('click', function(event){
        		//切换选中或未选中样式
        		var timeButClass = $(but).attr("class");
        		var regexp = RegExp("btn btn-sm btn-default");
        		if(regexp.test(timeButClass)){
        			$(but).attr("class","btn btn-sm btn-primary");
        		}else{
        			$(but).attr("class","btn btn-sm btn-default");
        		}
        		//修改表达式输入框的值
				var expressionModalSubmit = $("#expressionModal_submit");
				var expressionValue = expressionModalSubmit.find("input[name=expression]").val(); //表达式输入框值（表达式值）
				var expressionArr = expressionValue.split(" "); //拆分后的表达式值, 数组[秒、分、时、日、月、周]
				var timeIndex = appointTableIdArr.indexOf(appointTableId);  //数组[秒、分、时、日、月、周]索引号
				//取选中的指定值
        		var appointTimeArr = []; //指定秒数组 或 指定分数组 或 指定时数组 ...
				$.each($("#"+appointTableId).find("button[class='btn btn-sm btn-primary']"),function(inx,btn){
        			appointTimeArr.push($(btn).val());
		        });
				expressionArr[inputValueIndex] = appointTimeArr.length == 0 ? "?" : appointTimeArr.toString(); //设置选中的选项卡指定值
				if(timeMaxArr[timeIndex] == appointTimeArr.length) {
					expressionArr[inputValueIndex] = "*";
				}
				//重现组装表达式值
				var newExpressionValue = "";
				for(var i=0;i<expressionArr.length;i++){
					if(i == expressionArr.length - 1){
						newExpressionValue += expressionArr[i];
						continue;
					}
					newExpressionValue += expressionArr[i] + " ";
				}
				expressionModalSubmit.find("input[name=expression]").val(newExpressionValue); //给表达式输入框赋值
        	});
        });
	}
	
	expressionModal.typeChange = function(tabId, appointTableId, inputValueIndex){
		var appointTableIndex = appointTableIdArr.indexOf(appointTableId);
		var tab = $("#"+tabId);
		var appointTable = null;
		if(appointTableId != ""){
			appointTable = $("#"+appointTableId);
		}
		//绑定单选按钮点击事件
		tab.find("input:radio[name$=Type]").change(function(curRadio){
			var exp = "";
			if(curRadio.target.value == "appoint"){// 指定
				var selectArr = [];
				$.each(tab.find("button"),function(index,btn){
	        		var clazz = $(btn).attr("class");
	        		var regexp = RegExp("btn btn-sm btn-primary");
	        		if(regexp.test(clazz)){
        				selectArr.push($(btn).val());
	        		}
		        });
				exp = selectArr.length==0 ? "?" : selectArr.toString();
				// 全部选中时 置为 *
				if(timeMaxArr[appointTableIndex] == selectArr.length) {
					exp = "*";
				}
				appointTable.show();
			}else{
				if(curRadio.target.value == "per"){// 每秒/每分/每时/每日/每月/每周/每年
					exp = "*";
				}else if(curRadio.target.value == "period"){// 周期
					exp = tab.find("input[name=periodStart]").val() + "-" + tab.find("input[name=periodEnd]").val();
				}else if(curRadio.target.value == "loop"){// 循环
					exp = tab.find("input[name=loopStart]").val() + "/" + tab.find("input[name=loopEnd]").val();
				}else if(curRadio.target.value == "notAppoint") {// 不指定
					exp = "?";
				}else if(curRadio.target.value == "curMonthLastDay") {// 本月最后一天
					exp = "L";
				}else if(curRadio.target.value == "perMonth") {// 每月离 最近工作日
					exp = tab.find("input[name=latestWorkday]").val() + "W";
				}else if(curRadio.target.value == "appointWeekAndWeek") {// 指定第几周的星期几
					exp = tab.find("input[name=appointWeek]").val() + "#" + tab.find("input[name=appointWeekDay]").val();
				}else if(curRadio.target.value == "curMonthLastOneWeek") {// 本月最后一个星期几
					exp = tab.find("input[name=curMonthLastOneWeek]").val() + "L";
				}
				if(null != appointTable){
					appointTable.hide();
				}
			}
			var expArr = $("#expressionModal_submit").find("input[name=expression]").val().split(" ");
			if(tabId=="expressionModal_year") {// 年标签特别处理
				if(curRadio.target.value == "notAppoint") {
					expArr[inputValueIndex] = "";
				}else {
					expArr[inputValueIndex] = exp;
				}
			}else {
				expArr[inputValueIndex] = exp;
			}
			$("#expressionModal_submit").find("input[name=expression]").val(expArr.join(" ").trim());
		});
		//数字输入框加失去焦点事件
		tab.find("input[type=number]").blur(function(event) {
			expressionModal.numberChange(event, tab, inputValueIndex);
		});
	}
	
	expressionModal.numberChange = function(numberEvent, tab, inputValueIndex) {
		var numberExp = "";
		if(numberEvent.target.name=="periodStart" || numberEvent.target.name=="periodEnd") {// 周期
			numberExp = tab.find("input[name=periodStart]").val() + "-" + tab.find("input[name=periodEnd]").val();
			tab.find("input:radio[value=period]").click();
		}else if(numberEvent.target.name=="loopStart" || numberEvent.target.name=="loopEnd") {// 循环
			numberExp = tab.find("input[name=loopStart]").val() + "/" + tab.find("input[name=loopEnd]").val();
			tab.find("input:radio[value=loop]").click();
		}else if(numberEvent.target.name=="latestWorkday") {// 每月离某天最近工作日
			numberExp = tab.find("input[name=latestWorkday]").val() + "W";
			tab.find("input:radio[value=perMonth]").click();
		}else if(numberEvent.target.name=="appointWeek" || numberEvent.target.name=="appointWeekDay") {// 指定第几周的星期几
			numberExp = tab.find("input[name=appointWeek]").val() + "#" + tab.find("input[name=appointWeekDay]").val();
			tab.find("input:radio[value=appointWeekAndWeek]").click();
		}else if(numberEvent.target.name=="curMonthLastOneWeek") {// 本月最后一个星期几
			numberExp = tab.find("input[name=curMonthLastOneWeek]").val() + "L";
			tab.find("input:radio[value=curMonthLastOneWeek]").click();
		}
		var expArr = $("#expressionModal_submit").find("input[name=expression]").val().split(" ");
		if(tab[0].id == "expressionModal_year") {// 年标签
			if(expArr.length == 4){
				expArr[3] = numberExp;
			}else if(expArr.length == 5){
				expArr[4] = numberExp;
			}else{
				expArr[6] = numberExp;
			}
		}else {
			expArr[inputValueIndex] = numberExp;
		}
		$("#expressionModal_submit").find("input[name=expression]").val(expArr.join(" ").trim());
	}
	
	var validate = {
		isNumber : function(value){//大于等于0
			if(null != value && value != ""){
				if(value == 0){
					return true;
				}
				var exp = "^[0-9]+$"; 
		    	var regExp = new RegExp(exp);
		    	if(value.search(regExp) != -1){
					var first = value.substring(0,1);
					if(first < 1){
						return false;
					}
		    	    return true; 
		    	}
			}
	    	return false;
	    },
		startAndEnd : function(pValue,pSplit,sMinValue,sMaxValue,eMinValue,eMaxValue,isValSAE){//isValSAE 是否验证开始值<结束值
			if(pValue.split(pSplit).length != 2){
				return false;
			}
			var sValue = pValue.split(pSplit)[0];
			var eValue = pValue.split(pSplit)[1];
			if(!this.isNumber(sValue) || !this.isNumber(eValue)){//验证数字
				return false;
			}
			sValue = parseInt(sValue);
			eValue = parseInt(eValue);
			if(sValue >= sMinValue && sValue <= sMaxValue){//验证开始值
				if(eValue >= eMinValue && eValue <= eMaxValue){//验证结束值
					if(isValSAE){//需验证开始值<=结束值
						if(eValue > sValue){
							return true;
						}
					}else{
						return true;
					}
				}
			}
			return false;
		},
		appoint : function(timeArr,appointBtnNum){ //appointBtnNum:指定按钮数量
			var minValue,maxValue;
			if(appointBtnNum == 60 || appointBtnNum == 24){ //最小值 到 最大值 (例如 0 到 59、 0 到23)
				minValue = 0;
				maxValue = appointBtnNum - 1; 
			}else if(appointBtnNum == 31 || appointBtnNum == 7 || appointBtnNum == 12){ //最小值 到 最大值 (例如 1 到 31、 1 到 7、 1 到 12)
				minValue = 1;
				maxValue = appointBtnNum;
			}
			var tArr = [];
			for(var i=0;i<timeArr.length;i++){
				if(!this.isNumber(timeArr[i])){
					return false;
				}
				if(timeArr[i] < minValue || timeArr[i] > maxValue){//时间值在指定时间区间内
					return false;
				}
				if($.inArray(timeArr[i],tArr) != -1){//有重复值
					return false;
				}
				tArr.push(timeArr[i]);
			}
			return true;
		},
		isNumberAndWord : function(value,minValue,maxValue,word){
			var sValue = value.substring(0,value.length-1);
			var eValue = value.substring(value.length-1,value.length);
			if(!this.isNumber(sValue)){//验证数字
				return false;
			}
			if(sValue < minValue || sValue > maxValue || eValue != word){
				return false;
			}
			return true;
		},
		expression : function(expression,displayTabArr){
			var expressionArr = expression.split(" ");
			if(displayTabArr.length == 7){
				if(expressionArr.length !=6 && expressionArr.length != 7){
					return "表达式值的长度应该为6或7";
				}
			}
			if(displayTabArr.length == 4){
				if(expressionArr.length !=3 && expressionArr.length != 4){
					return "表达式值的长度应该为3或4";
				}
			}
			if(displayTabArr.length == 5){
				if(expressionArr.length !=4 && expressionArr.length != 5){
					return "表达式值的长度应该为4或5";
				}
			}
			var tabNameArr = ["秒","分","时","日","月","周","年"];
			for(var i=0;i<tabIdArr.length;i++){
				if($.inArray(tabIdArr[i], displayTabArr) == -1){
					continue;
				}
				var tab = $("#"+tabIdArr[i]);
				var inputValueIndex = displayTabArr.indexOf(tabIdArr[i]); //输入框值下标
				if(expressionArr[inputValueIndex] != undefined && expressionArr[inputValueIndex] != ""){
					if(expressionArr[inputValueIndex] == "*"){ //每秒
						var typeLength = $("#"+tabIdArr[i]).find("input:radio[value=per]").length;
						if(typeLength == 0){
							return "无法识别["+tabNameArr[i]+"]选项卡中值的类型";
						}
					}else if(expressionArr[inputValueIndex].indexOf("-") != -1){ //周期
						var typeLength = $("#"+tabIdArr[i]).find("input:radio[value=period]").length;
						if(typeLength == 0){
							return "无法识别["+tabNameArr[i]+"]选项卡中值的类型";
						}
						var sMinValue = tab.find("input[name=periodStart]").attr("min");
						var sMaxValue = tab.find("input[name=periodStart]").attr("max");
						var eMinValue = tab.find("input[name=periodEnd]").attr("min");
						var eMaxValue = tab.find("input[name=periodEnd]").attr("max");
						if(!this.startAndEnd(expressionArr[inputValueIndex],"-",sMinValue,sMaxValue,eMinValue,eMaxValue,true)){
							return "["+tabNameArr[i]+"]选项卡中周期值验证失败";
						}
					}else if(expressionArr[inputValueIndex].indexOf("/") != -1){ //循环
						var typeLength = $("#"+tabIdArr[i]).find("input:radio[value=loop]").length;
						if(typeLength == 0){
							return "无法识别["+tabNameArr[i]+"]选项卡中值的类型";
						}
						var sMinValue = tab.find("input[name=loopStart]").attr("min");
						var sMaxValue = tab.find("input[name=loopStart]").attr("max");
						var eMinValue = tab.find("input[name=loopEnd]").attr("min");
						var eMaxValue = tab.find("input[name=loopEnd]").attr("max");
						if(!this.startAndEnd(expressionArr[inputValueIndex],"/",sMinValue,sMaxValue,eMinValue,eMaxValue,false)){
							return "["+tabNameArr[i]+"]选项卡中循环值验证失败";
						}
					}else if(expressionArr[inputValueIndex] == "?" || expressionArr[inputValueIndex].indexOf(",") != -1 || this.isNumber(expressionArr[inputValueIndex])){ //指定(?：未指定或指定所有; 指定一个：1个数字; 指定多个：逗号隔开)
						var typeLength = $("#"+tabIdArr[i]).find("input:radio[value=appoint]").length;
						if(typeLength == 0){
							return "无法识别["+tabNameArr[i]+"]选项卡中值的类型";
						}
						if(expressionArr[inputValueIndex] != "?"){
							if(!this.appoint(expressionArr[inputValueIndex].split(","),timeMaxArr[i])){
								return "["+tabNameArr[i]+"]选项卡中指定值验证失败";
							}
						}
					}else if(expressionArr[inputValueIndex].indexOf("W") != -1){ //日/每月
						var typeLength = $("#"+tabIdArr[i]).find("input:radio[value=perMonth]").length;
						if(typeLength == 0){
							return "无法识别["+tabNameArr[i]+"]选项卡中值的类型";
						}
						var sMinValue = tab.find("input[name=latestWorkday]").attr("min");
						var sMaxValue = tab.find("input[name=latestWorkday]").attr("max");
						if(!this.isNumberAndWord(expressionArr[inputValueIndex],sMinValue,sMaxValue,"W")){
							return "["+tabNameArr[i]+"]选项卡中每月值验证失败";
						}
					}else if(expressionArr[inputValueIndex] == "L"){ //日/本月最后一天
						var typeLength = $("#"+tabIdArr[i]).find("input:radio[value=curMonthLastDay]").length;
						if(typeLength == 0){
							return "无法识别["+tabNameArr[i]+"]选项卡中值的类型";
						}
					}else if(expressionArr[inputValueIndex].indexOf("#") != -1){ //周/指定
						var typeLength = $("#"+tabIdArr[i]).find("input:radio[value=appointWeekAndWeek]").length;
						if(typeLength == 0){
							return "无法识别["+tabNameArr[i]+"]选项卡中值的类型";
						}
						var sMinValue = tab.find("input[name=appointWeek]").attr("min");
						var sMaxValue = tab.find("input[name=appointWeek]").attr("max");
						var eMinValue = tab.find("input[name=appointWeekDay]").attr("min");
						var eMaxValue = tab.find("input[name=appointWeekDay]").attr("max");
						if(!this.startAndEnd(expressionArr[inputValueIndex],"#",sMinValue,sMaxValue,eMinValue,eMaxValue,false)){
							return "["+tabNameArr[i]+"]选项卡中指定值验证失败";
						}
					}else if(expressionArr[inputValueIndex].indexOf("L") != -1){ //周/本月最后一个星期
						var typeLength = $("#"+tabIdArr[i]).find("input:radio[value=curMonthLastOneWeek]").length;
						if(typeLength == 0){
							return "无法识别["+tabNameArr[i]+"]选项卡中值的类型";
						}
						var sMinValue = tab.find("input[name=curMonthLastOneWeek]").attr("min");
						var sMaxValue = tab.find("input[name=curMonthLastOneWeek]").attr("max");
						if(!this.isNumberAndWord(expressionArr[inputValueIndex],sMinValue,sMaxValue,"L")){
							return "["+tabNameArr[i]+"]选项卡中本月最后一个星期值验证失败";
						}
					}else{
						return "无法识别["+tabNameArr[i]+"]选项卡中值的类型";
					}
				}else{
					$("#"+tabIdArr[i]).find("input:radio[value=notAppoint]").click();
				}
			}
			return null;
		}
	}
	
	return expressionModal;
});