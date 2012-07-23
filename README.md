crx-maven-plugin
================

  The CRX Maven Plugin is responsible for collecting all the resources of a Google Chrome Extension and packaging
  them into a signed CRX archive.

```xml
<plugin>
    <groupId>com.btmatthews.maven.plugins</groupId>
    <artifactId>crx-maven-plugin</artifactId>
    <version>1.0.0</version>
    <extensions>true</extensions>
    <configuration>
        <pemFile>${user.home}/.ssh/crx.pem</pemFile>
        <pemPassword>${crxPassword}
    </configuration>
</plugin>
```