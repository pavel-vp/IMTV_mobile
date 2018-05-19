# IMTV_mobile
Andorid application for devices, which can be installed on vehicles and can show advertising clips.

This application is installing on a rooted OrangePi device and connecting with server through RestAPI to :
- get global setup data
- get playlist with clip's paths
- get video files by their paths
- calculate the vide file playing orderby the algorythm based on frequency in 1 hour
- plays the video file in VideoPlayer
- upload statistical data about which file have been played at the time
- download new verisonon the application (apk file) and try to install it on the device (as the device is rooted)

Also in this project there is another application called "imtv-guardian", that as "whatch dog", always running, 
and if the main application doesn't on the top - it relaunch th imtv-player application.
