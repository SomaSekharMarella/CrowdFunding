# MetaMask Wallet Connection - 403 Error Fix

## Issues Fixed

### 1. **CORS Preflight (OPTIONS) Requests**
- **Problem**: Browser sends OPTIONS request before POST, but Spring Security was blocking it
- **Fix**: 
  - Added OPTIONS method to permitted requests in SecurityConfig
  - Updated JwtAuthenticationFilter to skip OPTIONS requests
  - Enhanced CORS configuration with maxAge

### 2. **Authentication Error Handling**
- **Problem**: 403 errors weren't providing clear feedback
- **Fix**:
  - Added better error handling in WalletController
  - Added authentication null checks
  - Improved error messages

### 3. **Frontend Error Handling**
- **Problem**: Generic error messages didn't help debug
- **Fix**:
  - Added detailed error logging in API interceptor
  - Added authentication checks before wallet connection
  - Better user feedback with specific error messages

## Changes Made

### Backend Changes

1. **SecurityConfig.java**
   - Added `HttpMethod.OPTIONS` to permitted requests
   - Enhanced CORS configuration

2. **JwtAuthenticationFilter.java**
   - Skip JWT validation for OPTIONS requests

3. **WalletController.java**
   - Added authentication null checks
   - Better error handling and messages

### Frontend Changes

1. **api.js**
   - Added 403 error logging for debugging

2. **Dashboard.jsx**
   - Added authentication checks before connecting wallet
   - Better error messages for different scenarios
   - Token validation before API calls

## Testing Steps

1. **Restart Backend**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Restart Frontend**:
   ```bash
   cd frontend
   npm run dev
   ```

3. **Test Wallet Connection**:
   - Login to the application
   - Click "Connect MetaMask"
   - Approve MetaMask connection
   - Check browser console for any errors
   - Verify wallet address appears

## Common Issues & Solutions

### Issue: Still getting 403 error
**Solution**:
1. Check if you're logged in (check localStorage for 'token')
2. Check browser console for detailed error
3. Verify backend is running on port 8080
4. Check CORS headers in browser Network tab

### Issue: "Session expired" message
**Solution**:
1. Logout and login again
2. Check if JWT token is valid
3. Verify token expiration time in application.yml

### Issue: MetaMask not opening
**Solution**:
1. Ensure MetaMask extension is installed
2. Check if MetaMask is unlocked
3. Try refreshing the page

### Issue: CORS errors in console
**Solution**:
1. Verify backend CORS configuration allows `http://localhost:3000`
2. Check if backend is running
3. Clear browser cache and cookies

## Debug Checklist

- [ ] Backend is running on port 8080
- [ ] Frontend is running on port 3000
- [ ] User is logged in (check localStorage)
- [ ] JWT token exists and is valid
- [ ] MetaMask extension is installed and unlocked
- [ ] Network tab shows OPTIONS request succeeds (200 status)
- [ ] Network tab shows POST request with Authorization header
- [ ] Browser console shows no CORS errors

## Network Request Flow

1. **OPTIONS Request** (CORS Preflight)
   - Method: OPTIONS
   - Headers: Origin, Access-Control-Request-Method
   - Should return: 200 OK with CORS headers

2. **POST Request** (Actual API Call)
   - Method: POST
   - Headers: Authorization: Bearer <token>, Content-Type: application/json
   - Body: { "address": "0x..." }
   - Should return: 200 OK with success message

## Verification

After fixes, you should see:
- ✅ OPTIONS request returns 200
- ✅ POST request includes Authorization header
- ✅ POST request returns 200 with success message
- ✅ Wallet address saved and displayed in UI
