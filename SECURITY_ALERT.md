# 🚨 SECURITY ALERT - API Key Exposed

## ⚠️ IMMEDIATE ACTION REQUIRED

GitHub detected the Resend API key in commit history.

**Exposed Key:** `re_7zVN9qmn_F6onNFsy5Kq4YRCeR11rd8ir`

---

## ✅ FIX NOW (3 Steps)

### **1. Revoke Old Key** 🔴
https://resend.com/api-keys
- Delete key: `re_7zVN9qmn_F6onNFsy5Kq4YRCeR11rd8ir`

### **2. Generate New Key** ✅
- Create new key in Resend dashboard
- Update `demo/.env` file with new key

### **3. Remove from Git** 🧹
```bash
cd C:\Users\Maxi G\Documents\GitHub\TrimminFlow-Backend
git filter-branch --force --index-filter "git rm --cached --ignore-unmatch EMAIL_SETUP_GUIDE.md" --prune-empty -- --all
git push origin --force --all
```

---

## 📋 Checklist
- [ ] Old key revoked
- [ ] New key generated  
- [ ] `.env` updated
- [ ] Git history cleaned
- [ ] Changes pushed
