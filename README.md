# burp-dynamic-js
Plugin for Burp Suite Free wich detects reflected parameterss in responses generated server side.

## Introduction
Finding reflected parameters is alway useful for testing reflected XSS issues.
Find them while browsing with this plugin for burp free.

## Setup
Download the jar file located in the releases section and import it in the extender option of burp suite. For more information about how to use extender option see the [official documentation](https://portswigger.net/burp/documentation/desktop/tools/extender#loading-and-managing-extensions)

## Local compilation
If you want to compile the code yourself, you need to have [maven](https://maven.apache.org/) installed and run the following command in the base directory of the project:
```console
$ mvn clean install
```

## Contibuting
Feel free to create an issue or a pull request if you see any bugs.
