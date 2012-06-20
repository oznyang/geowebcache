<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
    <title>ArcGIS JavaScript API: ${serviceName}</title>
    <style type="text/css">
        body {
            padding: 0;
            background: #fff;
            margin: 0;
            font-family: verdana, arial, helvetica, sans-serif;
            min-width: 780px
        }

        html {
            padding: 0;
            background: #fff;
            margin: 0;
            font-family: verdana, arial, helvetica, sans-serif;
            min-width: 780px
        }

        code {
            font-size: 1.3em;
            font-family: serif
        }

        .restBody {
            font-size: 0.8em;
            margin-left: 22px;
        }

        #userTable {
            font-size: 0.80em;
        }

        #navTable {
            padding-bottom: 5px;
            margin: 0 0 3px;
            padding-top: 0;
            border-bottom: #000 1px solid;
            border-top: #000 1px solid;
            background-color: #e5eff7;
        }

        #loginTable {
            padding: 5px;
            margin: 10px 0 3px;
            border: #000 1px solid;
            background-color: #e5eff7;
        }

        #adminNavTable {
            padding-bottom: 5px;
            margin: 0 0 3px;
            padding-top: 0;
            border-bottom: #000 1px solid;
            border-top: #000 1px solid;
            background-color: #f7efe5;
        }

        #adminLoginTable {
            padding: 5px;
            margin: 10px 0 3px;
            border: #000 1px solid;
            background-color: #f7efe5;
        }

        #breadcrumbs {
            padding: 0px 0px 5px 11px;
            font-size: 0.8em;
            font-weight: bold;
            margin: 0 0 3px;
        }

        TD#help {
            padding: 3px 11px 5px 0;
            font-size: 0.70em;
            margin: 0 0 3px;
        }

        #titlecell {
            padding: 3px 0 5px 11px;
            font-size: 1.0em;
            font-weight: bold;
            margin: 0 0 3px;
        }

        td {
            padding: 3px 11px 5px 0;
            font-size: 0.90em;
            margin: 0 0 3px;
        }

        h2 {
            margin-left: 11px;
            font-weight: bold;
            font-size: 1.2em
        }

        h3 {
            font-weight: bold;
            font-size: 1.25em;
            margin-bottom: 0;
        }

        li {
            padding: 0 0 3px;
        }
    </style>
    <link href="http://serverapi.arcgisonline.com/jsapi/arcgis/1.2/js/dojo/dijit/themes/tundra/tundra.css" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="http://serverapi.arcgisonline.com/jsapi/arcgis?v=1.2"></script>
    <script type="text/javascript">
        dojo.require("esri.map");
        var map;
        dojo.addOnLoad(function(){
            dojo.style(dojo.byId("map"), { width:dojo.contentBox("map").w + "px", height:(esri.documentBox.h - dojo.contentBox("loginTable").h - 40) + "px" });
            map = new esri.Map("map");
            var layer = new esri.layers.${isTiled?string('ArcGISTiledMapServiceLayer','ArcGISDynamicMapServiceLayer')}("${ctx}/service/arcgisrest/${serviceName}/MapServer");
            map.addLayer(layer);
        });
    </script>
</head>

<body class="tundra">
<table id="loginTable" width="100%">
    <tr>
        <td id="breadcrumbs">ArcGIS JavaScript API: ${serviceName}</td>
        <td id="help" align="right">Built using the <a href="http://resources.esri.com/arcgisserver/apis/javascript/arcgis">ArcGIS JavaScript API</a></td>
    </tr>
</table>
<div id="map" style="margin-left:10px;margin-right:10px;width:97%;border:1px solid #000;"></div>
</body>
</html>