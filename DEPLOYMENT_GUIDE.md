# To-Do App - Deployment Guide

This guide will help you prepare and deploy your To-Do Android application to the Google Play Store.

## Prerequisites

- Android Studio installed
- Java Development Kit (JDK) 8 or higher
- Google Play Developer account (for Play Store deployment)

## Step 1: Generate Release Keystore

Before building a release APK, you need to create a keystore for signing your app.

### Using Command Line:

```powershell
# Navigate to your project directory
cd "c:\Users\sachi\Downloads\AI Automation\Frontend Projects\To-Do Android"

# Generate keystore (replace YOUR_NAME with your name)
keytool -genkeypair -v -keystore todoapp-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias todoapp-key
```

**Important Information to Save:**
- **Keystore password**: Choose a strong password (you'll need this!)
- **Key password**: Can be the same as keystore password
- **Key alias**: `todoapp-key` (already set in the command)
- **Validity**: 10000 days (~27 years)

During key generation, you'll be asked for:
- Your name
- Organizational unit
- Organization name
- City/Locality
- State/Province
- Country code (2 letters)

**⚠️ CRITICAL: Backup your keystore file! If you lose it, you cannot update your app on Play Store.**

### Using Android Studio:

1. Go to **Build > Generate Signed Bundle / APK**
2. Select **Android App Bundle** or **APK**
3. Click **Create new...**
4. Fill in the details:
   - **Key store path**: Choose location (save as `todoapp-release-key.jks` in project root)
   - **Password**: Your keystore password
   - **Key alias**: `todoapp-key`
   - **Key password**: Your key password
   - **Validity**: 25 years or more
   - Fill in certificate details

## Step 2: Configure Keystore Properties

1. Open the `keystore.properties` file in your project root
2. Update the following values:

```properties
storeFile=todoapp-release-key.jks
storePassword=YOUR_ACTUAL_KEYSTORE_PASSWORD
keyAlias=todoapp-key
keyPassword=YOUR_ACTUAL_KEY_PASSWORD
```

**⚠️ Security Warning:** 
- Never commit `keystore.properties` to version control
- Never commit your `.jks` file to version control
- These files are already listed in `.gitignore`

## Step 3: Update Version Information (Optional)

Before each release, update version in `app/build.gradle`:

```gradle
defaultConfig {
    ...
    versionCode 2        // Increment for each release
    versionName "1.1"    // User-visible version
    ...
}
```

**Version Guidelines:**
- **versionCode**: Integer that must increase with each release (1, 2, 3, ...)
- **versionName**: String shown to users (1.0, 1.1, 2.0, etc.)

## Step 4: Build Release APK/AAB

### Option A: Using Command Line (Recommended)

#### Build Release APK:
```powershell
# Clean previous builds
./gradlew clean

# Build release APK
./gradlew assembleRelease

# Output location:
# app\build\outputs\apk\release\app-release.apk
```

#### Build Release AAB (App Bundle - Recommended for Play Store):
```powershell
# Clean previous builds
./gradlew clean

# Build release AAB
./gradlew bundleRelease

# Output location:
# app\build\outputs\bundle\release\app-release.aab
```

### Option B: Using Android Studio

1. Go to **Build > Generate Signed Bundle / APK**
2. Select **Android App Bundle** (recommended) or **APK**
3. Click **Next**
4. Choose your keystore file or use existing
5. Enter passwords
6. Select **release** build variant
7. Check both signature versions (V1 and V2)
8. Click **Finish**

## Step 5: Test Release Build

Before uploading to Play Store, test your release build:

### Install Release APK:
```powershell
adb install app\build\outputs\apk\release\app-release.apk
```

### Testing Checklist:
- ✅ App launches without crashes
- ✅ All features work correctly
- ✅ No debug logs appear (check Logcat)
- ✅ Google Sign-In works
- ✅ Notifications work
- ✅ Widget functions properly
- ✅ Database operations work
- ✅ App doesn't crash on different Android versions
- ✅ Test on multiple devices/screen sizes

## Step 6: Prepare Play Store Assets

Before uploading to Google Play Console, prepare:

### Required Assets:
1. **App Icon**: 512x512 PNG (already done in app)
2. **Feature Graphic**: 1024x500 PNG
3. **Screenshots**: At least 2 screenshots
   - Phone: 16:9 or 9:16 aspect ratio
   - Tablet: Different screenshots for 7" and 10" tablets (optional)
4. **Short Description**: Max 80 characters
5. **Full Description**: Max 4000 characters
6. **Privacy Policy URL**: If app collects user data

### Screenshot Tips:
- Take screenshots on device with `adb shell screencap`
- Or use Android Studio's Device File Explorer
- Showcase key features: Task list, Add task, Categories, Widget

## Step 7: Upload to Google Play Console

1. **Create App in Play Console:**
   - Go to [Google Play Console](https://play.google.com/console)
   - Click **Create App**
   - Fill in app details

2. **Set Up Store Listing:**
   - Upload screenshots
   - Add descriptions
   - Set app category: Productivity
   - Add content rating
   - Set up pricing (Free)

3. **Upload Release:**
   - Go to **Production > Create new release**
   - Upload your AAB file (`app-release.aab`)
   - Add release notes
   - Set rollout percentage (start with 20% for safety)
   - Review and roll out

4. **Complete App Content:**
   - Data safety form
   - Target audience and content
   - News apps declaration
   - COVID-19 contact tracing
   - Data deletion requirements
   - Privacy policy

5. **Submit for Review:**
   - Complete all required sections
   - Click **Send for review**
   - Review typically takes 1-7 days

## Current Build Configuration

Your app is configured with:

- **Package Name**: `com.todoapp`
- **Min SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Compile SDK**: 34
- **Version Code**: 1
- **Version Name**: 1.0

### Build Features:
- ✅ ProGuard/R8 code shrinking and obfuscation
- ✅ Resource shrinking enabled
- ✅ Optimized ProGuard rules
- ✅ Release signing configuration
- ✅ Debug symbols included

## Troubleshooting

### Common Issues:

**1. Build fails with signing error:**
- Check `keystore.properties` has correct paths and passwords
- Ensure keystore file exists in project root

**2. App crashes on startup:**
- Check Logcat for errors
- Verify ProGuard rules aren't removing required classes
- Test with ProGuard disabled first: set `minifyEnabled false`

**3. ProGuard removes necessary code:**
- Add `-keep` rules in `proguard-rules.pro`
- Check R8 output in `app/build/outputs/mapping/release/`

**4. Cannot sign in with Google:**
- Ensure SHA-1 certificate fingerprint is added to Firebase Console
- Get release SHA-1: `keytool -list -v -keystore todoapp-release-key.jks`
- Add to Firebase project settings

**5. Notifications don't work:**
- Check notification permissions are granted
- Test on Android 13+ devices

### Get SHA-1 Fingerprint:

For Firebase/Google Services configuration:

```powershell
# Get release SHA-1
keytool -list -v -keystore todoapp-release-key.jks -alias todoapp-key

# Get debug SHA-1 (for testing)
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

## Post-Release

After your app is live:

1. **Monitor Crashes**: Check Play Console > Quality > Crashes
2. **Review Feedback**: Respond to user reviews
3. **Track Metrics**: Monitor installs, uninstalls, ratings
4. **Plan Updates**: Regular updates keep users engaged

## Update Checklist for Future Releases

- [ ] Increment `versionCode` in `build.gradle`
- [ ] Update `versionName` if needed
- [ ] Write release notes
- [ ] Test thoroughly
- [ ] Build release AAB
- [ ] Upload to Play Console
- [ ] Create new release with release notes
- [ ] Roll out (start small, increase gradually)

## Security Best Practices

- ✅ Never commit keystore or passwords to version control
- ✅ Backup your keystore in multiple secure locations
- ✅ Use strong passwords for keystore
- ✅ Keep your signing key private
- ✅ Enable Google Play App Signing (recommended)
- ✅ Regular security updates for dependencies

## Useful Commands

```powershell
# Check app version
./gradlew tasks | Select-String -Pattern "version"

# List build variants
./gradlew tasks --all

# Run lint checks
./gradlew lint

# Run tests before release
./gradlew test

# Generate a mapping file for ProGuard
# Located at: app/build/outputs/mapping/release/mapping.txt
# Keep this file for each release to decode crash reports!
```

## Support

If you encounter issues:
1. Check build errors in Android Studio
2. Review Logcat for runtime errors
3. Check ProGuard mapping file for obfuscation issues
4. Test on multiple devices and Android versions

---

**Remember:** Always test your release build thoroughly before submitting to Play Store!

**Good luck with your deployment! 🚀**
