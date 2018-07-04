# UpdateEnable

This a simple project which can be used to update Software. 

LatestUpdate:

Android 8.0 and above applicable


working progress is as follows<br>
--(1)Check server for upate.json<br>
--(2)parse json according to json structure<br>
--(3)compare server version code and local version code<br>
--(4)download new apk file from server and auto-install<br>


**auto-update and mannual-update both utilized in the same function entrance

Server json configuration is as follows:

{<br>
	"update_ver_name": "2.0",<br>	
	"update_ver_code": 2,<br>
	"update_content": "修复多项BUG！",<br>
	"udpate_url": "http://192.168.15.36/Update/XXXXX.apk",<br>
	"ignore_able": true,<br>
	"md5":"1B2B717B7320690CF4E6F29F18B334CB"<br>
}

The main entrance is function updateStart();<br>


Usage<br>
   (1)for auto-update <br>
    call function updateStart() in main activity just as shown in MainActivity in this project.<br>
   (2)for mannual-update <br>
   call function updateStart() in callback function of designated components<br>
   
