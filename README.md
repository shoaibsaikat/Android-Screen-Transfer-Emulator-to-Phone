# ScreenTransferFromEmulatorToPhone
This is a test project. Here I've sent screenshot from andorid emulator to android phone using linux as a proxy.

1.	Connect android mobile and linux machine in the same network.
2.  Start emulator in linux machine.
3.  Before building android projet, go to MainActivity.java file. Change "myClient = new Client(this, "192.168.43.22", 7792);" line, by the ip address of your linux machine and the port you want to use. I am using 7792 port. Now build the andorid project.
4.	Build server.c with "gcc -pthread server.c -o server". This is our proxy server.
5.	run "server 7792" command to run the proxy server. Use your own selected port, if you want to use a different port.
6.	Start "EmuClient" android app in both android device and android emulator.
7.	Press connect in both device and emulator.
8.	Now press send in emulator and see the image on your android device.

Note: if you want to store the image in phones' local storage, uncomment "//createDirectoryAndSaveFile(BitmapFactory.decodeByteArray(image, 0, length), "echo.jpeg");" line, in MainActivity.java.
