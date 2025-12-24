# Debugging 403 Error - Step by Step

## Current Status
- ✅ Token exists and is being sent (174 characters)
- ✅ Test Auth endpoint works (GET /api/wallet/test-auth)
- ❌ Wallet Connect fails with 403 (POST /api/wallet/connect)
- ✅ MetaMask connection works

## What I've Added

### 1. **Backend Logging**
Added detailed System.out.println logs in:
- `JwtAuthenticationFilter` - Shows JWT processing steps
- `WalletController.connectWallet()` - Shows authentication details
- `WalletController.testAuth()` - Shows test auth details

### 2. **Frontend Logging**
Enhanced console logging to show:
- Token existence and length
- Auth test results
- Detailed error information

## Next Steps to Debug

### Step 1: Restart Backend
```bash
cd backend
mvn spring-boot:run
```

### Step 2: Try Connecting Wallet Again
1. Login to the app
2. Go to Dashboard
3. Click "Test Auth" button first
4. Check the alert - note what it shows
5. Then click "Connect MetaMask"
6. Check backend console output

### Step 3: Check Backend Console

You should see output like:
```
=== TEST AUTH REQUEST ===
Authentication object: EXISTS
Authentication authenticated: true
...
=== WALLET CONNECT REQUEST ===
Authentication object: EXISTS or NULL
...
```

### Step 4: Check What's Different

Compare the logs between:
- Test Auth (GET) - works
- Wallet Connect (POST) - fails

Look for differences in:
- Authentication object status
- Principal type
- JWT filter processing

## Expected Backend Logs

### For Test Auth (Working):
```
=== TEST AUTH REQUEST ===
Authentication object: EXISTS
Authentication authenticated: true
Authentication principal type: UserPrincipal
```

### For Wallet Connect (Failing):
Check what it shows - this will tell us the issue:
- If "Authentication object: NULL" → JWT filter not setting auth
- If "Principal type: ..." (not UserPrincipal) → Wrong principal type
- If "Authentication not authenticated" → Auth not set properly

## Common Issues Based on Logs

### Issue 1: Authentication is NULL in POST but EXISTS in GET
**Cause**: JWT filter might be clearing context between requests
**Fix**: Check if SecurityContext is being cleared

### Issue 2: Principal type is wrong
**Cause**: Authentication object has wrong principal
**Fix**: Check JWT filter is creating UserPrincipal correctly

### Issue 3: JWT validation fails
**Cause**: Token expired or invalid
**Fix**: Check JWT secret and token expiration

## Quick Test

After restarting backend, try this in browser console:
```javascript
// Test auth endpoint
fetch('http://localhost:8080/api/wallet/test-auth', {
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('token')
  }
})
.then(r => r.json())
.then(d => console.log('Test Auth:', d))
.catch(e => console.error('Test Auth Error:', e));

// Test connect endpoint
fetch('http://localhost:8080/api/wallet/connect', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('token'),
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ address: '0x1234567890123456789012345678901234567890' })
})
.then(r => {
  console.log('Connect Status:', r.status);
  return r.text();
})
.then(d => console.log('Connect Response:', d))
.catch(e => console.error('Connect Error:', e));
```

## Share Backend Logs

After trying to connect wallet, share the backend console output. It will show exactly where the authentication is failing.
