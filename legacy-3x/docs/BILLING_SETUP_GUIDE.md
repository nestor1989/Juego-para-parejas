# Google Play Billing Configuration Guide

## Problem
Your app shows: "This version of the application is not configured for billing through Google Play"

## Root Causes
1. **Certificate Fingerprint Not Registered**: Your signing certificate's SHA1 fingerprint isn't registered in Google Play Console
2. **Missing In-App Product Configuration**: SKUs (product IDs) like "premium_monthly" aren't set up in Google Play Console
3. **App Not Published**: The app must be deployed to at least internal testing track before billing works

## Solution Steps

### Step 1: Register Your Certificate in Google Play Console
Your certificate information:
- **Alias**: ideastudio
- **SHA1 Fingerprint**: 28:31:50:0E:19:DA:F1:89:53:91:CE:5F:9A:0D:07:21:27:12:21:5B
- **SHA256 Fingerprint**: A8:A9:47:53:15:84:59:F2:31:21:92:BC:FE:CD:1F:0F:E5:1E:7B:86:F3:51:84:7E:2D:BF:43:98:CB:5F:2E:49

**Actions in Google Play Console**:
1. Go to Google Play Console → Your App → Settings → App signing
2. Note the "App signing certificate" SHA1 and SHA256 fingerprints provided by Google Play
3. Go to Settings → API access
4. Create a Service Account and add your certificate fingerprints to the list of trusted certificates

### Step 2: Configure App Signing in build.gradle
Add signing configuration to your app's build.gradle file to ensure consistent signing across builds.

### Step 3: Create In-App Products in Google Play Console
1. Go to Google Play Console → Your App → Monetize → Products → Subscriptions
2. Create the following subscription products:
   - **Product ID**: `premium_monthly`
   - **Price**: Set your desired price
   - **Billing Period**: 1 Month
   
3. Create another subscription:
   - **Product ID**: `premium_yearly`
   - **Price**: Set your desired price
   - **Billing Period**: 1 Year

### Step 4: Deploy to Internal Testing Track
1. In Google Play Console, go to Release → Internal testing
2. Upload your APK/AAB signed with your keystore
3. Add test accounts that can make purchases
4. Test on those accounts

### Step 5: Verify Implementation
The app already has billing implementation with:
- ✅ Billing library (7.1.1)
- ✅ BILLING permission in manifest
- ✅ SubscriptionManager with proper product queries
- ✅ Backend verification support

## Testing Locally Before Releasing

To test billing without full Google Play setup, use test SKUs:
- `android.test.purchased` - Simulates successful purchase
- `android.test.canceled` - Simulates canceled purchase
- `android.test.refunded` - Simulates refunded purchase
- `android.test.item_unavailable` - Simulates unavailable item

The SubscriptionManager already has fallback logic to use these test SKUs if real ones fail.

## Troubleshooting

### Issue: "Billing unavailable" error code
**Solution**: Ensure the app is deployed to at least Internal testing track in Google Play Console

### Issue: "Item unavailable" when querying products
**Solution**: 
1. Verify SKU names match exactly in Google Play Console
2. Ensure subscription is "Active" (not in draft)
3. Wait 2-3 hours after creating SKU for cache propagation

### Issue: Purchase succeeds but "not acknowledged"
**Solution**: The SubscriptionManager automatically acknowledges purchases. Verify backend verification is working.

## Current App Setup

Your app has:
- Application ID: `com.idea3d.juegoparaparejas`
- Min SDK: 23
- Target SDK: 35
- Version: 2.1 (versionCode: 11)

These should all be configured in Google Play Console as well.

## Next Steps

1. ✅ Add signing configuration to build.gradle (done in companion files)
2. Open Google Play Console and complete Steps 1-3 above
3. Upload the app to internal testing track
4. Test with test accounts
5. Once verified, promote to production

