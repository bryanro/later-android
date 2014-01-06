To build release:
- Make sure gradle version 1.8 is in your PATH
- Set keystore values in signing.properties
- Set google analytics tracking id in src/main/res/values/analytics.xml
- Have git ignore changes to signing.properties and analytics.xml:
 - git update-index --assume-unchanged signing.properties
 - git update-index --assume-unchanged Later/src/main/res/values/analytics.xml
- Run: gradle release