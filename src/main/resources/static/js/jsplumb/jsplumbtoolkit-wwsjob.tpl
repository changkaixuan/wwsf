<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<html>
    <head>
        <script type="jtk" id="tmplTable">
            <div data-port-id="${column}" class="table node">
				<jtk-source port-id="${column}" filter=".edit-name,.fal,.fa-pencil,.table-column-delete, .table-column-delete-icon, span, .table-column-edit, .table-column-edit-icon" filter-exclude="true"/>
                <jtk-target port-id="${column}" />
                <div class="name float-right">
                    <div class="textbg">
                    	${name}
                    </div>
                    <div class="buttons">
                        <div class="edit-name" title="编辑任务项">
                            <i class="fal fa-pencil"/>
                        </div>
                        <div class="delete" title="删除任务项">
	                        <i class="fal fa-times"/>
	                    </div>
                    </div>
                </div>
				<ul class="table-columns clearfix">
                    <r-each in="columns">
                        <r-tmpl id="tmplColumn"/>
                    </r-each>
                </ul>
            </div>
        </script>
        
        <!-- table column template -->
        <script type="jtk" id="tmplColumn">
            <li class="table-column" style="padding:2px 6px;">
                <div><span>${id}</span></div>
            </li>
        </script>
        
        <!-- edit view query -->
        <script type="jtk" class="dlg" id="dlgViewQuery" title="Edit Query">
            <textarea class="txtViewQuery" jtk-focus jtk-att="query" jtk-commit="true"/>
        </script>
        
        <!-- edit name (table or view) -->
        <script type="jtk" class="dlg" id="dlgName" title="Enter Name">
            <input type="text" size="50" class="form-control-sm" jtk-focus jtk-att="name" jtk-commit="true"/>
        </script>
        
        <script type="jtk" class="dlg" id="dlgConfirm" title="信息提示窗口">
            ${msg}?
        </script>
        
        <script type="jtk" class="dlg" id="dlgMessage" title="信息提示窗口" cancel="false">
            ${msg}
        </script>
    
    </head>
    <body>
    </body>
</html>