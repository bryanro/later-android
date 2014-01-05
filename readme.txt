To build release:
- Set keystore values in signing.properties
- Set google analytics tracking id in src/main/res/values/analytics.xml
- Have git ignore changes to signing.properties and analytics.xml:
 - git update-index --assume-unchanged signing.properties
 - git update-index --assume-unchanged src/main/res/values/analytics.xml
- Run: gradle release