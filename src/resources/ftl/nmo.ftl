<html>
<head>
<meta charset="utf-8">
<meta content="text/html; charset=UTF-8" http-equiv="content-type"/>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="cache-control" content="max-age=0" />
<meta http-equiv="cache-control" content="no-cache" />
<meta http-equiv="expires" content="0" />
<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
<meta http-equiv="pragma" content="no-cache" />
<title>NMO WEB UI :: ${system}</title>
<link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jasny-bootstrap/3.1.3/css/jasny-bootstrap.css" integrity="sha256-t0GDqKZnpVh1OVTSGzI7i52hYH9DN68z81MATIqktRo=" crossorigin="anonymous" />
<link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet" integrity="sha384-wvfXpqpZZVQGK6TAh5PVlGOfQNHSoD2xbE+QkPxCAFlNEevoEH3Sl0sibVcOQVnN" crossorigin="anonymous">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/ionicons/2.0.1/css/ionicons.min.css" integrity="sha256-3iu9jgsy9TpTwXKb7bNQzqWekRX7pPK+2OLj3R922fo=" crossorigin="anonymous" />
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.12.2/css/bootstrap-select.min.css" integrity="sha256-an4uqLnVJ2flr7w0U74xiF4PJjO2N5Df91R2CUmCLCA=" crossorigin="anonymous" />
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/ekko-lightbox/5.1.1/ekko-lightbox.min.css" integrity="sha256-8aNQFtmxcOMVoOhLD4mrHqaKC2Ui++LmlQsoKTqxwIE=" crossorigin="anonymous" />
<!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
<![endif]-->
<style>
form {
	margin-bottom: 4px;
}
.btn {
	padding: 2px !important;
}
.btn-purple { 
  color: #ffffff; 
  background-color: #764CAD; 
  border-color: #534E6B; 
} 
 
.btn-purple:hover, 
.btn-purple:focus, 
.btn-purple:active, 
.btn-purple.active, 
.open .dropdown-toggle.btn-purple { 
  color: #ffffff; 
  background-color: #49247A; 
  border-color: #534E6B; 
} 
 
.btn-purple:active, 
.btn-purple.active, 
.open .dropdown-toggle.btn-purple { 
  background-image: none; 
} 
 
.btn-purple.disabled, 
.btn-purple[disabled], 
fieldset[disabled] .btn-purple, 
.btn-purple.disabled:hover, 
.btn-purple[disabled]:hover, 
fieldset[disabled] .btn-purple:hover, 
.btn-purple.disabled:focus, 
.btn-purple[disabled]:focus, 
fieldset[disabled] .btn-purple:focus, 
.btn-purple.disabled:active, 
.btn-purple[disabled]:active, 
fieldset[disabled] .btn-purple:active, 
.btn-purple.disabled.active, 
.btn-purple[disabled].active, 
fieldset[disabled] .btn-purple.active { 
  background-color: #764CAD; 
  border-color: #534E6B; 
} 
 
.btn-purple .badge { 
  color: #764CAD; 
  background-color: #ffffff; 
}
</style>
</head>
<body>
<div class="container">
<h1>NoMoreOversleeps WEB UI <small>v${version}</small> <a href="https://github.com/PolyphasicDevTeam/NoMoreOversleeps"><button type="submit" class="btn-sm btn-primary" style="padding:2px; position:relative; top:-4px;">Download source</button></a></h1>
  <div style="clear:both; padding-top:4px;"></div>
  <h4>Status</h4>
  <table class="table table-striped">
    <tbody>
      <tr>
        <td><strong>Last refresh</strong></td>
        <td id="nmo_last_refresh">never</td>
      </tr>
      <tr>
        <td style="width:230px;"><strong>Current part of schedule</strong></td>
        <td id="nmo_schedule">?</td>
      </tr>
      <tr>
        <td><strong>Activity detection state</strong></td>
        <td id="nmo_pause_state">?</td>
      </tr>
      <tr>
        <td style="width:230px;"><strong>Last activity detected</strong></td>
        <td id="nmo_activity">?</td>
      </tr>
      <tr>
        <td style="width:230px;"><strong>Camera sockets open</strong></td>
        <td id="nmo_conn_count">?</td>
      </tr>
      <#if integration_noise>
      <tr>
        <td><strong>Annoying noise</strong></td>
        <td id="nmo_noise_state">?</td>
      </tr>
      </#if>
      <#if integration_philipsHue>
      <tr>
        <td><strong>Light</strong></td>
        <td id="nmo_light_state">?</td>
      </tr>
      </#if>
    </tbody>
  </table>
  <h4>Manual controls</h4>
  <div style="float:right;"><img id="webcamImage" height=320/></div>
  ${actionButtons}
  <div></div>
  <div style="clear:both;"></div>
  <h4>Log</h4>
  <iframe id="logframe" style="width:100%; height:390px" src="/log"></iframe>
</div>
<script src="https://code.jquery.com/jquery-2.1.4.min.js" integrity="sha384-R4/ztc4ZlRqWjqIuvf6RX5yb/v90qNGx6fS48N0tRxiGkqveZETq72KgDVJCp2TC" crossorigin="anonymous" type="text/javascript"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous" type="text/javascript"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jasny-bootstrap/3.1.3/js/jasny-bootstrap.js" integrity="sha256-NG7ORCRPjgqoYm8LQoOuJjXtFW2++/ElxKFup4JwXS8=" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/1000hz-bootstrap-validator/0.11.9/validator.min.js" integrity="sha256-dHf/YjH1A4tewEsKUSmNnV05DDbfGN3g7NMq86xgGh8=" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.12.2/js/bootstrap-select.min.js" integrity="sha256-JD3g+rB9BjW6/cGEuwCue1sGtitb2aQVNs/pl4114XQ=" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/ekko-lightbox/5.1.1/ekko-lightbox.min.js" integrity="sha256-1odJPEl+KoMUaA1T7QNMGSSU/r5LCKCRC6SL8P0r2gY=" crossorigin="anonymous"></script>
<script>
!function(a,b){"function"==typeof define&&define.amd?define([],b):"undefined"!=typeof module&&module.exports?module.exports=b():a.ReconnectingWebSocket=b()}(this,function(){function a(b,c,d){function l(a,b){var c=document.createEvent("CustomEvent");return c.initCustomEvent(a,!1,!1,b),c}var e={debug:!1,automaticOpen:!0,reconnectInterval:1e3,maxReconnectInterval:3e4,reconnectDecay:1.5,timeoutInterval:2e3};d||(d={});for(var f in e)this[f]="undefined"!=typeof d[f]?d[f]:e[f];this.url=b,this.reconnectAttempts=0,this.readyState=WebSocket.CONNECTING,this.protocol=null;var h,g=this,i=!1,j=!1,k=document.createElement("div");k.addEventListener("open",function(a){g.onopen(a)}),k.addEventListener("close",function(a){g.onclose(a)}),k.addEventListener("connecting",function(a){g.onconnecting(a)}),k.addEventListener("message",function(a){g.onmessage(a)}),k.addEventListener("error",function(a){g.onerror(a)}),this.addEventListener=k.addEventListener.bind(k),this.removeEventListener=k.removeEventListener.bind(k),this.dispatchEvent=k.dispatchEvent.bind(k),this.open=function(b){h=new WebSocket(g.url,c||[]),b||k.dispatchEvent(l("connecting")),(g.debug||a.debugAll)&&console.debug("ReconnectingWebSocket","attempt-connect",g.url);var d=h,e=setTimeout(function(){(g.debug||a.debugAll)&&console.debug("ReconnectingWebSocket","connection-timeout",g.url),j=!0,d.close(),j=!1},g.timeoutInterval);h.onopen=function(){clearTimeout(e),(g.debug||a.debugAll)&&console.debug("ReconnectingWebSocket","onopen",g.url),g.protocol=h.protocol,g.readyState=WebSocket.OPEN,g.reconnectAttempts=0;var d=l("open");d.isReconnect=b,b=!1,k.dispatchEvent(d)},h.onclose=function(c){if(clearTimeout(e),h=null,i)g.readyState=WebSocket.CLOSED,k.dispatchEvent(l("close"));else{g.readyState=WebSocket.CONNECTING;var d=l("connecting");d.code=c.code,d.reason=c.reason,d.wasClean=c.wasClean,k.dispatchEvent(d),b||j||((g.debug||a.debugAll)&&console.debug("ReconnectingWebSocket","onclose",g.url),k.dispatchEvent(l("close")));var e=g.reconnectInterval*Math.pow(g.reconnectDecay,g.reconnectAttempts);setTimeout(function(){g.reconnectAttempts++,g.open(!0)},e>g.maxReconnectInterval?g.maxReconnectInterval:e)}},h.onmessage=function(b){(g.debug||a.debugAll)&&console.debug("ReconnectingWebSocket","onmessage",g.url,b.data);var c=l("message");c.data=b.data,k.dispatchEvent(c)},h.onerror=function(b){(g.debug||a.debugAll)&&console.debug("ReconnectingWebSocket","onerror",g.url,b),k.dispatchEvent(l("error"))}},1==this.automaticOpen&&this.open(!1),this.send=function(b){if(h)return(g.debug||a.debugAll)&&console.debug("ReconnectingWebSocket","send",g.url,b),h.send(b);throw"INVALID_STATE_ERR : Pausing to reconnect websocket"},this.close=function(a,b){"undefined"==typeof a&&(a=1e3),i=!0,h&&h.close(a,b)},this.refresh=function(){h&&h.close()}}return a.prototype.onopen=function(){},a.prototype.onclose=function(){},a.prototype.onconnecting=function(){},a.prototype.onmessage=function(){},a.prototype.onerror=function(){},a.debugAll=!1,a.CONNECTING=WebSocket.CONNECTING,a.OPEN=WebSocket.OPEN,a.CLOSING=WebSocket.CLOSING,a.CLOSED=WebSocket.CLOSED,a});
</script>
<script>
    $(document).ready(function() {
        function request() {
            $.ajax({
                url: "/json",
                dataType: "text",
                cache: false,
                success: function(data) {
                    var json = $.parseJSON(data);
                    $('#nmo_last_refresh').html(json.update);
                    $('#nmo_activity').html(json.activity);
                    $('#nmo_pause_state').html(json.pause_state);
                    $('#nmo_conn_count').html(json.conn_count);
                    $('#nmo_schedule').html(json.schedule);
                    $('#nmo_noise_state').html(json.noise_state);
                    $('#nmo_light_state').html(json.light_state);
                    $('#logframe').attr('src',function(i,val){return val;});
                }
            });
        }
        request();
        setInterval(request, 1863);
    });
$(document).ready(function() {
	var ws = new ReconnectingWebSocket("ws://" + window.location.hostname + ":" + window.location.port + "/webcam");
	ws.onopen = function(e) {
		if (typeof console !== 'undefined') {
			console.info('WS open');
		}
	};
	ws.onmessage = function (e) {
		var data = JSON.parse(e.data), type = data.type, i = 0, $webcams = $('#webcams'), $img = null;
		if (typeof console !== 'undefined') {
			console.info('WS message', type);
		}
		$img = $('#webcamImage');
		$img.attr("src", "data:image/jpeg;base64," + data.image).addClass('shadow').trigger("change");
		setTimeout(function() {
			$img.removeClass('shadow').trigger("change");
		}, 1000);
	};
	ws.onclose = function() {
		if (typeof console !== 'undefined') {
			console.info('WS close');
		}
	};
	ws.onerror = function(err) {
		if (typeof console !== 'undefined') {
			console.info('WS error ' + err.data + ' ' + err.code + ' ' + err.message + ' ' + err + ' ' + err.detail + ' ' + err.originalEvent);
		}
	};
});
</script>
</body>
</html>
