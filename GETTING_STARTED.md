# ImageJ FX Getting Started

Before installing ImageJ FX, you should have installed the lastest version of Oracle Java (8u45+).

To obtain this it, Windows or MacOS should please visit the [Oracle website](https://www.java.com/en/download/). If you use Linux Ubuntu, please refer to the instruction below in order to install it.

## Installation

### Windows

Run the installer and follow the instructions. Don't install the software in "Program File" or other space in the hard drive that would require Administrator rights.


### Mac OS

Extract the downloaded file. Go in **System Preferences** > **Security and Privacy**, click on the lock to unlock it. You can now allow apps coming from anyway. Drag and drop the ImageJ FX app in your application folder and launch it. If nothing happen after the Updater downloads the necessary files, just close it and restart the app.
### Linux

#### Ubuntu 14.04+

In order for ImageJ FX to work, you must install Oracle JDK 8.
For this, you can add the following repository and add . Open a terminal and run the following command :

~~~
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
~~~

Type your password and it should install the last version of the OpenJDK. Once done, I recommand you to install the whole folder " **ImageJ FX** " from the downloaded zip file. Go inside this folder, just double click on **start-ijfx.jar** or **ImageJFX**.

