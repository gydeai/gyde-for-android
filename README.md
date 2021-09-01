# Integrate Gyde SDK in your Android project
This article explains how to install and update the Gyde Android SDK in your project.

## Prerequisites:
- Android Studio
- Android version: 5.0(Lollipop) & later (API level/SDK version 21 & later)

## Integration:
#### Step 1: Add token to gradle.properties
Add the below token into to $HOME/.gradle/gradle.properties
```
authToken=jp_jmkqakru9rd4jo6s2l4v41lq7q
```

#### Step 2: Add Gyde Maven repo URL and credentials in your build
Add maven ``` { url 'https://jitpack.io' } ``` in Gradle root
```
repositories { 
   maven {

       url  'https://jitpack.io'
       credentials { username authToken }

   }
}
```

#### Step 3: Add Gyde Maven repo URL in your build
Add bintray link in the app gradle dependency.

To add Gyde dependency to your project, specify the following dependency configuration in the dependencies block of your build.gradle file.
```
implementation 'com.github.gydeai:gyde-for-android:$sdk-version'
```
where $sdk-version is the latest version of the SDK available which is **1.1.1**

[![Release](https://jitpack.io/v/gydeai/gyde-for-android.svg)](https://jitpack.io/#gydeai/gyde-for-android)

#### Step 4: Add GYDE APP ID in android manifest
Add Gyde App Id provided to you from Gyde.ai into your manifest file.
```
<application
  ...>
  <meta-data
       android:name="GYDE_APP_ID"
       android:value="YOUR_GYDE_APP_ID" />
</application>
```

#### Step 5: Create a layout to start Gyde walkthrough flow
Create a button or layout to start gyde flow. Navigate to GydeHomeScreen on layout click.

**Kotlin**
```
findViewById<Button>(R.id.btn_start).setOnClickListener { 
    startActivity(Intent(this@MainActivity, GydeHomeActivity::class.java))            
}
```

**Java**
```
Button gydeHelp = findViewById(R.id.btn_gyde_help);
        gydeHelp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GydeHomeActivity.class);
            startActivity(intent);
});
```
Replace your button id with your appropriate layout and add click listner for navigation.


### Other Features
#### 1. Show single tooltip on any event
You can show GydeTooltip if any event occured. It will show single Tooltip and on done button it will get dismissed.

**Kotlin**
```
GydeExternalMethods.showGydeTooltip(
            context = Context,         // Current activity context
            viewId = Int,              // View Id where you want to show tooltip
            title = String,            // Title for tooltip
            description = String,      // Description/Message for tooltip
            tooltipPosition = GydeTooltipPosition / null, // Tooltip position (Optional)
            buttonText = String / null  // Text for button. Default message is Done (Optional)
        )
```
**Java**
```
GydeExternalMethods.Companion.showGydeTooltip(
            context = Context,         // Current activity context
            viewId = Int,              // View Id where you want to show tooltip
            title = String,            // Title for tooltip
            description = String,      // Description/Message for tooltip
            tooltipPosition = GydeTooltipPosition / null, // Tooltip position (Optional)
            buttonText = String / null  // Text for button. Default message is Done (Optional)
        );
```
You can choose any GydeTooltipPosition from below. This will be the position of Gyde Tooltip. It's optional field. Default value is BOTTOM_CENTER<br>
GydeTooltipPosition.DRAW_BOTTOM_CENTER<br>
GydeTooltipPosition.DRAW_BOTTOM_LEFT<br>
GydeTooltipPosition.DRAW_BOTTOM_RIGHT<br>
GydeTooltipPosition.DRAW_TOP_LEFT<br>
GydeTooltipPosition.DRAW_TOP_CENTER<br>
GydeTooltipPosition.DRAW_TOP_RIGHT<br>

#### 2. Start walkthrough by walkthrough ID
You can start any walkthrough on specific event occured. For this you will need walkthrough ID.

**Kotlin**
```
GydeExternalMethods.startGydeWalkthrough(
            context = Context,      // current activity context
            walkthroughId = String  // walkthrough ID
        )
```

**Java**
```
GydeExternalMethods.Companion.startGydeWalkthrough(
            context = Context,      // current activity context
            walkthroughId = String  // walkthrough ID
        );
```

Note : Contact Gyde admin to get the walkthrough ID

#### 3. Add deeplinking into your project for Gyde library
##### Step I:
```
    <activity android:name=".YOUR_ACTIVITY_NAME">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:host="www.gyde.ai"
                    android:scheme="https" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:host="www.gyde.ai"
                    android:scheme="http" />
            </intent-filter>
        </activity>
```
Add deeplinking intent-filter into your manifest file inside activity tag where you wnat to capture the deeplining data.

##### Step II : Capture the data from deeplinking in activity
Capture the data inside your activity which you have already declared in manifest file.
**Kotlin**
```
private fun getDeepLinkingData() {
        try {
            val uri: Uri? = intent.data
            if (uri != null) {
                val parameters = uri.pathSegments
                val param = parameters[parameters.size - 1]
                startActivity(
                    Intent(this@MainActivity, GydeHomeActivity::class.java).putExtra(
                        "GYDE_DEEP_LINK_DATA", param
                    )
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
```
**Java**
```
   private void getDeepLinkingData() {
        try {
            Uri uri = getIntent().getData();
            if (uri != null) {
                List<String> parameters = uri.getPathSegments();
                String param = parameters.get(parameters.size() - 1);
                Intent intent = new Intent(MainActivity2.this, GydeHomeActivity.class);
                intent.putExtra("GYDE_DEEP_LINK_DATA", param);
                startActivity(intent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
```

Other Features
