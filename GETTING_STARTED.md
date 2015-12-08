# ImageJ FX - Getting Started

Before installing ImageJ FX, you need to install the lastest version of Oracle Java (8u45+).

Windows or MacOS users can download this version from the [Oracle website](https://www.java.com/en/download/). If you use Linux Ubuntu, please refer to the instruction below in order to install it.

## Installation

### Windows

Run the installer and follow the instructions. Don't install the software in "Program Files" or another place in the hard drive that would require administrator rights.


### Mac OS

Extract the downloaded file. Go to **System Preferences** > **Security and Privacy**, and in the **Allow apps downloaded from** section select the **Anywhere** option. Now you can install apps from any source. Drag and drop the ImageJ FX app into your application folder and launch it. If nothing happens after the ImageJ FX Updater downloads the necessary files, just close and restart the app.
### Linux

#### Ubuntu 14.04+

In order for ImageJ FX to work, you must install Oracle JDK 8.
For this, you can add the following repository. Open a terminal and run the following command:

~~~
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
~~~

Type your password and it should install the latest version of the OpenJDK. Once completed, I recommend you to unzip the whole folder " **ImageJ FX** " from the downloaded zip file. Then go inside the ImageJ FX folder and double click on **start-ijfx.jar** or **ImageJFX**.

