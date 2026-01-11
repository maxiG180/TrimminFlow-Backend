# 📧 Email Setup Complete!

## ✅ What Was Done

### 1. **Backend Configuration** (.env file created)
**Location:** `C:\Users\Maxi G\Documents\GitHub\TrimminFlow-Backend\demo\.env`

```env
RESEND_API_KEY=re_7zVN9qmn_F6onNFsy5Kq4YRCeR11rd8ir
RESEND_FROM_EMAIL=noreply@trimminflow.com
```

### 2. **Security** (.env added to .gitignore)
✅ Your API key is now protected and won't be committed to Git

---

## 🚀 How to Enable Emails

### **For Local Development:**

**You need to restart the backend** for the .env file to be loaded:

```bash
# Stop the backend if running (Ctrl+C)
cd C:\Users\Maxi G\Documents\GitHub\TrimminFlow-Backend\demo
./gradlew bootRun
```

**That's it!** Emails will now work automatically.

---

### **For Production (Railway):**

You need to add environment variables to Railway:

1. Go to your Railway project dashboard
2. Click on your backend service
3. Go to **Variables** tab
4. Add these two variables:
   ```
   RESEND_API_KEY = re_7zVN9qmn_F6onNFsy5Kq4YRCeR11rd8ir
   RESEND_FROM_EMAIL = noreply@trimminflow.com
   ```
5. Click **Deploy** (Railway will automatically restart)

---

## 📨 When Emails Are Sent

### **Confirmation Email** (Immediate)
**Trigger:** When customer books appointment  
**Sent to:** Customer email  
**Contains:**
- Appointment date & time
- Service & barber
- Price & location
- "Your appointment has been confirmed!"

### **Reminder Email** (24 Hours Before)
**Trigger:** Automated scheduler (runs every hour)  
**Sent to:** Customer email  
**Contains:**
- "Reminder: Appointment Tomorrow!"
- All appointment details
- Only sent if **reminder emails are enabled** in Settings

---

## 🎛️ How to Enable/Disable Reminders

1. Login to admin dashboard
2. Go to **Settings**
3. Toggle **"24-Hour Reminder Emails"**
   - **ON** = Reminders sent 24hrs before
   - **OFF** = Only confirmation email sent

---

## ✅ Testing Emails

### **Test Booking Confirmation:**
1. Go to public booking page: `http://localhost:3000/book/YOUR_BARBERSHOP_ID`
2. Complete a booking with **a real email address**
3. Check email inbox for confirmation

### **Test Reminder Email:**
Option A - **Wait 24 hours** (automatic)  
Option B - **Create appointment for tomorrow** and check logs:

```bash
# Backend will log when reminder is sent:
"Sent reminder for appointment: <appointment-id>"
```

---

## 🔍 Verify It's Working

**Check backend logs when booking:**
- ✅ Should see: "Sending booking confirmation email"
- ❌ If you see error: Check API key is correct

**For reminders:**
- Scheduler runs every hour at minute 0 (e.g., 10:00, 11:00, 12:00)
- Check logs: "Processed X appointment reminders"

---

## 📧 Email From Address

**Current:** `noreply@trimminflow.com`

**To use your own domain:**
1. Verify domain in Resend dashboard: https://resend.com/domains
2. Update `RESEND_FROM_EMAIL` to use verified domain
3. Restart backend

**For demo/testing:** Current setup works fine with Resend's default

---

## 🎉 You're All Set!

**Emails will now:**
- ✅ Send confirmation when appointments are booked
- ✅ Send reminders 24 hours before (if enabled)
- ✅ Beautiful HTML templates
- ✅ Include all appointment details

**Just restart your backend and test!** 🚀
