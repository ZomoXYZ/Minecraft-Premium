# Minecraft-Premium

A collection of useful commands and tools for Minecraft servers

## Install

[JDK](https://openjdk.java.net/install/) and [Maven](https://maven.apache.org/install.html) are required

[MCLang](https://github.com/ZomoXYZ/MCLang) and [MCCommands](https://github.com/ZomoXYZ/MCCommands) are required libraries

Running the `build.sh` file will both compile and install Minecraft-Premium into your local repository

`build.sh` will also automatically create a server in your `~/.papermc/` directory

`build.sh` usage:

**Note** this readme is incomplete

## Using in other plugins

In your `pom.xml`, add

```xml
<dependencies>
    <dependency>
        <groupId>dev.zomo</groupId>
        <artifactId>mcpremium</artifactId>
        <version>1.8.2</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**Note** this readme is incomplete