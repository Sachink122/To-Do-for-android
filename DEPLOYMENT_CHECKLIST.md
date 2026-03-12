# 🚀 Quick Deployment Checklist

Use this checklist to ensure your app is ready for deployment to Google Play Store.

## ✅ Pre-Deployment Setup (COMPLETED)

- [x] Release signing configuration added
- [x] Keystore file generated (`todoapp-release-key.jks`)
- [x] Keystore properties configured
- [x] ProGuard rules optimized
- [x] Build scripts created
- [x] .gitignore configured for security

## 📋 Before Building

- [ ] **Update Version** (in `app/build.gradle`)
  - [ ] Increment `versionCode` (e.g., 1 → 2)
  - [ ] Update `versionName` if needed (e.g., "1.0" → "1.1")

- [ ] **Update Firebase** (if using Google Sign-In)
  - [ ] Add SHA-1 fingerprint to Firebase Console
  - [ ] SHA-1: `9C:D2:85:97:B7:EB:33:49:55:F5:3A:EE:3B:B0:09:3C:1C:98:74:64`
  - [ ] Download updated `google-services.json`

- [ ] **Test Thoroughly**
  - [ ] All features work in debug build
  - [ ] No critical bugs
  - [ ] Test on multiple Android versions
  - [ ] Test on different screen sizes

## 🔨 Build Release

Choose ONE of these options:

### Option A: Build APK (for testing)
```powershell
./build-release.ps1
```
Output: `app\build\outputs\apk\release\app-release.apk`

### Option B: Build AAB (for Play Store - RECOMMENDED)
```powershell
./build-aab.ps1
```
Output: `app\build\outputs\bundle\release\app-release.aab`

### Option C: Manual Build
```powershell
# Clean project
./gradlew clean

# Build release APK
./gradlew assembleRelease

# OR build release AAB (recommended for Play Store)
./gradlew bundleRelease
```

## 🧪 Test Release Build

- [ ] **Install and test APK**
  ```powershell
  adb install app\build\outputs\apk\release\app-release.apk
  ```

- [ ] **Test checklist**
  - [ ] App launches without crashes
  - [ ] Google Sign-In works
  - [ ] Create/Edit/Delete tasks works
  - [ ] Categories work
  - [ ] Notifications work
  - [ ] Widget works
  - [ ] Data persists after app restart
  - [ ] No debug logs in Logcat

## 📦 Google Play Store Submission

### 1. Prepare Assets

- [ ] **App Icon**: 512x512 PNG (high-res)
- [ ] **Feature Graphic**: 1024x500 PNG
- [ ] **Screenshots**: Minimum 2, recommended 4-8
  - Phone screenshots (16:9 or 9:16)
  - Tablet screenshots (optional)
- [ ] **Short description**: Max 80 characters
- [ ] **Full description**: Max 4000 characters
- [ ] **Privacy Policy** (if collecting user data)

### 2. Google Play Console Setup

- [ ] Create app in Play Console
- [ ] Complete store listing
  - [ ] Upload app icon and graphics
  - [ ] Add screenshots
  - [ ] Write descriptions
  - [ ] Set category: **Productivity**
  - [ ] Set pricing: **Free**

### 3. Content Rating

- [ ] Complete content rating questionnaire
- [ ] Receive ESRB, PEGI ratings automatically

### 4. Data Safety

- [ ] Fill out Data Safety form
  - What data is collected
  - How data is used
  - Data sharing practices

### 5. Upload Release

- [ ] Go to Production → Create new release
- [ ] Upload AAB file
- [ ] Upload ProGuard mapping file
  - Location: `app\build\outputs\mapping\release\mapping.txt`
- [ ] Add release notes (what's new)
- [ ] Set rollout percentage (start with 20-50%)
- [ ] Review and roll out

### 6. Submit for Review

- [ ] Complete all required sections
- [ ] Submit for review
- [ ] Wait 1-7 days for review

## 🔒 Security Checklist

- [ ] **Backup keystore**
  - [ ] Copy `todoapp-release-key.jks` to safe location
  - [ ] Save keystore passwords in password manager
  - [ ] Store SHA-1 fingerprint in secure note
  
- [ ] **Verify .gitignore**
  - [ ] `todoapp-release-key.jks` NOT in Git
  - [ ] `keystore.properties` NOT in Git
  - [ ] No sensitive data committed

- [ ] **Save mapping file**
  - [ ] Keep `mapping.txt` for EVERY release
  - [ ] Needed to decode crash reports
  - [ ] Location: `app\build\outputs\mapping\release\`

## 📝 Post-Release

- [ ] Monitor crashes in Play Console
- [ ] Respond to user reviews
- [ ] Track metrics (installs, ratings, etc.)
- [ ] Plan next update

## 🆘 Troubleshooting

**Build fails with signing error?**
- Check `keystore.properties` has correct passwords
- Ensure `todoapp-release-key.jks` exists

**App crashes on launch?**
- Check Logcat for errors
- Test with `minifyEnabled false` first
- Review ProGuard rules

**Google Sign-In doesn't work?**
- Add release SHA-1 to Firebase Console
- Download updated `google-services.json`

**Can't find built APK/AAB?**
- Check `app\build\outputs\apk\release\`
- Check `app\build\outputs\bundle\release\`
- Delete `app\build` and rebuild

---

## 📚 Additional Resources

- **Full Guide**: See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
- **Keystore Info**: See [KEYSTORE_INFO.md](KEYSTORE_INFO.md)
- **Build Scripts**:
  - APK: `build-release.ps1`
  - AAB: `build-aab.ps1`

## 🎯 Quick Commands

```powershell
# Build release AAB (recommended)
./build-aab.ps1

# Build release APK
./build-release.ps1

# Install APK for testing
adb install app\build\outputs\apk\release\app-release.apk

# Check keystore info
keytool -list -v -keystore todoapp-release-key.jks -storepass TodoApp2024!

# View build variants
./gradlew tasks
```

---

**🎉 Your app is ready to deploy!**

**Default Passwords**: `TodoApp2024!` (Change these in `keystore.properties`)

**IMPORTANT**: Remember to add the SHA-1 fingerprint to Firebase Console for Google Sign-In to work in production!
