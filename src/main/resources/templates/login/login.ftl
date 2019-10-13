<html>
<#include "../common/header.ftl">

<body>
<div id="wrapper" class="toggled">

    <#--边栏sidebar-->
    <#include "../common/nav.ftl">
        <form role="form" method="get" action="/sell/seller/info/login">
            <div class="form-group">
                <label>openid</label>
                <input name="openid" type="text" class="form-control"/>
            </div>
            <button type="submit" class="btn btn-default">提交</button>
        </form>
</div>
</body>
</html>