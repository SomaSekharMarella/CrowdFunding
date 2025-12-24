# Authentication Debug Guide - MetaMask Connection Issue

## Problem
Getting "Access denied" error when trying to connect MetaMask wallet.

## Debugging Steps

### 1. **Test Authentication First**

I've added a "Test Auth" button on the Dashboard. Use it to check if authentication is working:

1. Login to the application
2. Go to Dashboard
3. Click the **"Test Auth"** button (small button next to "Connect MetaMask")
4. Check the alert message - it will show:
   - If authentication is NULL
   - Authentication status
   - Principal type
   - User details

### 2. **Check Browser Console**

Open browser DevTools (F12) and check the Console tab:

- Look for "Request with token" messages
- Look for "403 Forbidden" error details
- Look for "401 Unauthorized" messages
- Check if token exists in localStorage

### 3. **Check Network Tab**

In DevTools Network tab:

1. Filter by "wallet"
2. Click on the `/api/wallet/connect` request
3. Check **Request Headers**:
   - Should have: `Authorization: Bearer <token>`
   - If missing, token is not being sent
4. Check **Response**:
   - Status code (403, 401, etc.)
   - Response body (error message)

### 4. **Verify Token in localStorage**

In browser console, run:
```javascript
localStorage.getItem('token')
```

Should return a JWT token string. If null or empty, you need to login again.

### 5. **Common Issues & Fixes**

#### Issue: Token exists but authentication fails
**Possible causes:**
- Token expired
- Token invalid/ corrupted
- JWT secret mismatch

**Fix:**
1. Logout
2. Clear localStorage: `localStorage.clear()`
3. Login again
4. Try connecting wallet

#### Issue: "Authentication is NULL"
**Possible causes:**
- JWT filter not setting authentication
- Token not being validated

**Fix:**
1. Check backend logs for JWT errors
2. Verify JWT secret in `application.yml`
3. Try logging out and in again

#### Issue: "Invalid authentication token - Principal type: ..."
**Possible causes:**
- Authentication object has wrong principal type
- JWT validation succeeded but wrong user details loaded

**Fix:**
1. Check backend logs
2. Verify user exists in database
3. Try creating a new account

## Backend Logs to Check

When you try to connect wallet, check backend console for:

1. **JWT Filter logs:**
   - "Authentication set for user: ..." (success)
   - "JWT token validation failed" (failure)
   - "Cannot set user authentication" (error)

2. **Wallet Controller logs:**
   - Authentication object status
   - Principal type
   - Error messages

## Quick Fixes to Try

### Fix 1: Clear and Re-login
```javascript
// In browser console
localStorage.clear()
// Then refresh and login again
```

### Fix 2: Check Token Format
```javascript
// In browser console
const token = localStorage.getItem('token');
console.log('Token:', token);
console.log('Token length:', token?.length);
console.log('Starts with eyJ:', token?.startsWith('eyJ')); // JWT tokens start with 'eyJ'
```

### Fix 3: Manual Token Test
```javascript
// In browser console - test if token works
fetch('http://localhost:8080/api/wallet/test-auth', {
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('token')
  }
})
.then(r => r.json())
.then(console.log)
.catch(console.error);
```

## Expected Behavior

### ✅ Working Authentication:
- "Test Auth" shows: `authenticated: true`, `principalType: UserPrincipal`
- Network request has `Authorization: Bearer <token>` header
- Backend logs show "Authentication set for user: <username>"
- Wallet connects successfully

### ❌ Broken Authentication:
- "Test Auth" shows: `authenticated: false` or `Authentication is NULL`
- Network request missing Authorization header
- Backend logs show JWT validation errors
- 403 or 401 errors

## Next Steps

1. **Restart Backend** (to get new logging):
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Use Test Auth Button** to diagnose

3. **Check Console Logs** for detailed errors

4. **Share the Results**:
   - What "Test Auth" shows
   - Console error messages
   - Network request details
   - Backend log messages

This will help identify the exact issue!
