# WireGuard artifact availability

- date: 2026-11-04T09:14:18Z

## 1. Maven Central search API (group = com.wireguard.android)

```json
{"responseHeader":{"status":0,"QTime":1,"params":{"q":"g:\"com.wireguard.android\"","core":"","indent":"off","spellcheck":"true","fl":"id,g,a,latestVersion,p,ec,repositoryId,text,timestamp,versionCount","start":"","spellcheck.count":"5","sort":"score desc,timestamp desc,g asc,a asc","rows":"40","wt":"json","version":"2.2"}},"response":{"numFound":1,"start":0,"docs":[{"id":"com.wireguard.android:tunnel","g":"com.wireguard.android","a":"tunnel","latestVersion":"1.0.20230706","repositoryId":"central","p":"aar","timestamp":1688667191000,"versionCount":7,"text":["com.wireguard.android","tunnel","-sources.jar.sha256","-javadoc.jar",".aar.sha256",".aar.sha512","-javadoc.jar.sha512",".module.asc.sha256",".module.asc.sha512",".aar.asc.sha512","-javadoc.jar.sha256","-sources.jar",".pom.sha512",".module","-sources.jar.asc.sha512",".aar",".module.sha256",".aar.asc.sha256",".pom","-sources.jar.asc.sha256",".module.sha512","-javadoc.jar.asc.sha256",".pom.asc.sha256","-javadoc.jar.asc.sha512",".pom.asc.sha512","-sources.jar.sha512",".pom.sha256"],"ec":["-sources.jar.sha256","-javadoc.jar",".aar.sha256",".aar.sha512","-javadoc.jar.sha512",".module.asc.sha256",".module.asc.sha512",".aar.asc.sha512","-javadoc.jar.sha256","-sources.jar",".pom.sha512",".module","-sources.jar.asc.sha512",".aar",".module.sha256",".aar.asc.sha256",".pom","-sources.jar.asc.sha256",".module.sha512","-javadoc.jar.asc.sha256",".pom.asc.sha256","-javadoc.jar.asc.sha512",".pom.asc.sha512","-sources.jar.sha512",".pom.sha256"]}]},"spellcheck":{"suggestions":[]}}
```

## 2. Maven Central search API (name/artifact = tunnel, any group)

```json
{"responseHeader":{"status":0,"QTime":0,"params":{"q":"a:\"tunnel\"","core":"","indent":"off","spellcheck":"true","fl":"id,g,a,latestVersion,p,ec,repositoryId,text,timestamp,versionCount","start":"","spellcheck.count":"5","sort":"score desc,timestamp desc,g asc,a asc","rows":"20","wt":"json","version":"2.2"}},"response":{"numFound":3,"start":0,"docs":[{"id":"com.nb6868.onex:tunnel","g":"com.nb6868.onex","a":"tunnel","latestVersion":"4.5.38","repositoryId":"central","p":"jar","timestamp":1751252810000,"versionCount":283,"text":["com.nb6868.onex","tunnel","-sources.jar",".pom","-javadoc.jar",".jar"],"ec":["-sources.jar",".pom","-javadoc.jar",".jar"]},{"id":"com.wireguard.android:tunnel","g":"com.wireguard.android","a":"tunnel","latestVersion":"1.0.20230706","repositoryId":"central","p":"aar","timestamp":1688667191000,"versionCount":7,"text":["com.wireguard.android","tunnel","-sources.jar.sha256","-javadoc.jar",".aar.sha256",".aar.sha512","-javadoc.jar.sha512",".module.asc.sha256",".module.asc.sha512",".aar.asc.sha512","-javadoc.jar.sha256","-sources.jar",".pom.sha512",".module","-sources.jar.asc.sha512",".aar",".module.sha256",".aar.asc.sha256",".pom","-sources.jar.asc.sha256",".module.sha512","-javadoc.jar.asc.sha256",".pom.asc.sha256","-javadoc.jar.asc.sha512",".pom.asc.sha512","-sources.jar.sha512",".pom.sha256"],"ec":["-sources.jar.sha256","-javadoc.jar",".aar.sha256",".aar.sha512","-javadoc.jar.sha512",".module.asc.sha256",".module.asc.sha512",".aar.asc.sha512","-javadoc.jar.sha256","-sources.jar",".pom.sha512",".module","-sources.jar.asc.sha512",".aar",".module.sha256",".aar.asc.sha256",".pom","-sources.jar.asc.sha256",".module.sha512","-javadoc.jar.asc.sha256",".pom.asc.sha256","-javadoc.jar.asc.sha512",".pom.asc.sha512","-sources.jar.sha512",".pom.sha256"]},{"id":"org.webjars.npm:tunnel","g":"org.webjars.npm","a":"tunnel","latestVersion":"0.0.6","repositoryId":"central","p":"jar","timestamp":1574429727000,"versionCount":2,"text":["org.webjars.npm","tunnel","-javadoc.jar","-sources.jar",".jar",".pom"],"ec":["-javadoc.jar","-sources.jar",".jar",".pom"]}]},"spellcheck":{"suggestions":[]}}
```

## 3. Direct coordinate probes

| coordinate | HTTP |
| --- | --- |
| `com.wireguard.android:tunnel:1.0.20260315` | 404 |
| `com.wireguard.android:tunnel:1.0.20230706` | 200 |
| `com.wireguard.android:tunnel:1.0.20210424` | 404 |
| `com.wireguard.android:wireguard-android:1.0.20260315` | 404 |

## 4. Latest published version, if any

```
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>com.wireguard.android</groupId>
  <artifactId>tunnel</artifactId>
  <versioning>
    <latest>1.0.20260102</latest>
    <release>1.0.20260102</release>
    <versions>
      <version>1.0.20210211</version>
      <version>1.0.20210926</version>
      <version>1.0.20211029</version>
      <version>1.0.20230321</version>
      <version>1.0.20230405</version>
      <version>1.0.20230427</version>
      <version>1.0.20230706</version>
      <version>1.0.20250531</version>
      <version>1.0.20251231</version>
      <version>1.0.20260102</version>
    </versions>
    <lastUpdated>20260102024712</lastUpdated>
  </versioning>
</metadata>
```

## 5. Can Gradle resolve it in this exact project?

|    +--- com.wireguard.android:tunnel:1.0.20260102
\--- com.wireguard.android:tunnel:1.0.20260102 (*)
