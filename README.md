# UpdateEnable

This a simple project which can be used to update Software.

LatestUpdate
Android 8.0 and above applicable


working progress is as follows
(1)Check server for upate.json
(2)parse json according to json structure
(3)compare server version code and local version code
(4)download new apk file from server and auto-install


**auto-update and mannual-update both utilized in the same function entrance

Server json configuration is as follows:

{
	"update_ver_name": "2.0",
	"update_ver_code": 2,
	"update_content": "修复多项BUG！",
	"udpate_url": "http://192.168.15.36/Update/XXXXX.apk",
	"ignore_able": true,
	"md5":"1B2B717B7320690CF4E6F29F18B334CB"
}

The main entrance is function updateStart();


Usage
   (1)for auto-update 
    call function updateStart() in main activity just as shown in MainActivity in this project.
   (2)for mannual-update 
   call function updateStart() in callback function of designated components
   
