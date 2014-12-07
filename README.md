Digital Rain
===============

A Swing project inspired by the popular graphical effect from the Matrix where characters fall from the top of the window downwards. In the Matrix movie series, the output of falling green code on computer terminals depicts actively of what's occurring in the computer simulation known as the Matrix.

The project builds an executable Jar file to run a program that contains the effect and various controls.

## Requirements

* Java 1.8

## Installation

`git clone --recursive https://github.com/JamesGames/digital-rain`

If you did not use the --recursive option, or if more git submodules are added later and you wish to pull those ones, then you can run following command afterwards:

`git submodule update --init --recursive`

However the --init option does not update submodules that were already initialized, so you also want to run the following command as well:

`git submodule update --recursive`


It's probably best to run both commands every time you pull the latest code.


## Building

To manually build the project, download Maven and change the current
working directory to the directory containing pom.xml and run the
command "mvn install".
The default location for the Maven local repository is

* "~/.m2" on Linux and OSX

or

* "Documents and Settings\{your-username}\.m2" on Windows

On a successful build you should find a directory named target within the directory you cloned the project to, and within there you should find the built executable .jar file inside.

## Credits

* James Murphy - JamesGames.Org(at)gmail.com
