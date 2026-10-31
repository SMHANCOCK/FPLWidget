plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace = "com.stevemh.fitbitwidget"; compileSdk = 36
defaultConfig { applicationId = "com.stevemh.fitbitwidget"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0" } }