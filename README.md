CRX Maven Plugin
================

The [CRX Maven Plugin](http://crx-maven-plugin.btmatthews.com/) is a [Maven](http://maven.apache.org) plugin that
collects all the resources of a [Google Chrome Extensions](http://code.google.com/chrome/extensions/index.html) and
packages them into a signed [CRX](http://code.google.com/chrome/extensions/crx.html) archive.

There are two ways to use the **CRX Maven Plugin**:

* Use the **crx** packaging type when the primary artifact produced by the project is the CRX archive
* The [crx:crx](http://crx-maven-plugin.btmatthews.com/crx-mojo.html) goal when the CRX archive is not the primary
artifact produced by the build.

Signature
---------
The CRX archive must be signed using the [RSA algorithm](http://en.wikipedia.org/wiki/RSA_(algorithm)) with the
[SHA-1](http://en.wikipedia.org/wiki/SHA-1) hash function. The public/private key used to sign the CRX archive must
be supplied as a [.pem](http://en.wikipedia.org/wiki/X.509#Certificate_filename_extensions) file. And it is
recommended that the **.pem** file be secured with a password.

The location of the **.pem** file and password can be specified in the following ways:

### Globally properties in settings.xml (recommended)

Defining **crxPEMFile** and **crxPEMPassword** as global properties in the user’s
[settings.xml](http://maven.apache.org/settings.html) is recommended approach for specifying the location of the
**.pem** file and the password that was used to secure it.

```xml
<settings>
  <profiles>
    <profile>
      <id>crx</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <crxPEMFile>${user.home}/crx.pem</crxPEMFile>
        <crxPEMPassword>SparkleAndFade</crxPEMPassword>
      </properties>
    </profile>
  </profiles>
</settings>
```
Furthermore, it is possible to avoid storing the password in plain text in the **settings.xml** file. See the
[Password Encryption](http://maven.apache.org/guides/mini/guide-encryption.html) guide on the Maven site.

### Project properties pom.xml (not recommended)

It is possible to define **crxPEMFile** and **crxPEMPassword** as properties of the CRX Maven Plugin. But this
approach is not recommended because it means specifying the password in plain text in the
[pom.xml](http://maven.apache.org/pom.html) and hard-coding the dependency on the **.pem** file.

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>com.btmatthews.maven.plugins</groupId>
        <artifactId>crx-maven-plugin</artifactId>
        <version>1.0.0</version>
        <configuration>
          <crxPEMFile>${user.home}/crx.pem</crxPEMFile>
          <crxPEMPassword>SparkleAndFade</crxPEMPassword>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

### Command line properties

Another approach is to define the **crxPEMFile** and **crxPEMPassword** properties on the Maven command line. This
approach can be used to override properties defined in settings.xml.

```
$ mvn -DcrxPEMFile=<path> [-DcrxPEMPassword=<password>] <goals>

where:
<path> is the location of the .pem file, e.g. ~/crx.pem
<password> is the password used to secure the .pem file, e.g. SparkleAndFade
<goals> are the Maven goals being executed, e.g. install
```

Using the crx packaging type
----------------------------
The table below describes the life-cycle of the **CRX Maven Plugin** when using the **crx** packaging type:

<table>
<thead>
<tr><th>Phase</th><th>Actions</th></tr>
</thead>
<tbody>
<tr>
<td>package</td>
<td>The sources in **./src/main/chrome** are assembled in memory into a
[ZIP file format](http://en.wikipedia.org/wiki/Zip_(file_format)) that is then signed and output
in the [Google Chrome Extension](http://code.google.com/chrome/extensions/crx.html) format with the extension .crx.
This is the CRX archive.</td>
</tr>
<tr>
<td>install</td>
<td>The CRX archive file is deployed to the user’s local
[repository](http://maven.apache.org/guides/introduction/introduction-to-repositories.html).</td>
</tr>
<tr>
<td>deploy</td>
<td>The CRX archive file is deployed to the remote release or snapshot
[repository](http://maven.apache.org/guides/introduction/introduction-to-repositories.html).</td></tr>
</tbody>
</table>

The [Hello World](http://code.google.com/chrome/extensions/examples/tutorials/getstarted.zip) project described in
the Getting Started tutorial on the Google Chrome Extensions site is used as the basis for the following example.

The resources for the **Google Chrome Extension** project should be placed in the **src/main/chrome** sub-directory
as illustrated below:

```
HelloWorld
+- pom.xml
+- src
|  +- main
|     +- chrome
|        +- manifest.json
|        +- icon.png
|        +- popup.html
|        +- popup.js
+- target
   +- HelloWorld-1.0.0-SNAPSHOT.crx
```

The **pom.xml** should be as follows:

```xml
<project>
    <groupId>com.btmatthews.crx</groupId>
    <artifactId>HelloWorld</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>crx</packaging>
    <build>
        <plugins>
            <plugin>
                <groupId>com.btmatthews.maven.plugins</groupId>
                <artifactId>crx-maven-plugin</artifactId>
                <version>1.0.0</version>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>
</project>
```

The **&lt;extensions&gt;true&lt;/extensions&gt;** element is required in the **POM** or one of its ancestors in order
to activate the **crx** packaging type.

The following command line will produce the **HelloWord-1.0.0-SNAPSHOT.crx** artifact in the **target** sub-directory:

```
$ mvn -DcrxPEMFile=~/crx.pem -DcrxPEMPassword=SparkleAndFade package
```

### Using the crx:crx goal

As with the previous example the resources for the **Google Chrome Extension** project should be placed in the
**src/main/chrome** sub-directory as illustrated below:

```
HelloWorld
+- pom.xml
+- src
|  +- main
|     +- chrome
|     |  +- manifest.json
|     |  +- icon.png
|     |  +- popup.html
|     |  +- popup.js
|     +- java
|     |  +- ...
|     +- webapp
|        +- ...
+- target
   +- HelloWorld-1.0.0-SNAPSHOT.crx
   +- HelloWorld-1.0.0-SNAPSHOT.war
```

The **pom.xml** should be similar to the following:

```xml
<project>
    <groupId>com.btmatthews.crx</groupId>
    <artifactId>HelloWorld</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>crx</packaging>
    <build>
        <plugins>
            <plugin>
                <groupId>com.btmatthews.maven.plugins</groupId>
                <artifactId>crx-maven-plugin</artifactId>
                <version>1.0.0</version>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>
</project>
```

The following command line will produce the **HelloWord-1.0.0-SNAPSHOT.crx** artifact in the **target** sub-directory:

```
$ mvn -DcrxPEMFile=~/crx.pem -DcrxPEMPassword=SparkleAndFade crx:crx
```

Maven Central Coordinates
-------------------------
The **CRX Maven Plugin** has been published in [Maven Central](http://search.maven.org) at the following coordinates:

```xml
<plugin>
    <groupId>com.btmatthews.maven.plugins</groupId>
    <artifactId>crx-maven-plugin</artifactId>
    <version>1.1.1</version>
</plugin
```

License & Source Code
---------------------
The **CRX Maven Plugin** is made available under the [Apache License](http://www.apache.org/licenses/LICENSE-2.0.html)
and the source code is hosted on [GitHub](http://github.com) at https://github.com/bmatthews68/crx-maven-plugin.