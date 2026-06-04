# Deployment Guide — Free Tier (Render + Vercel + Aiven)

Deploy the Aurelia Grand hotel reservation system using free services:
1. **Aiven** — MySQL Database (1 month free trial)
2. **Render** — Spring Boot Backend (free tier, 750 hrs/month)
3. **Vercel** — React Frontend (free unlimited)

**Total cost**: $0 for the first month

---

## Step 1: Set Up MySQL on Aiven

1. Sign up at https://aiven.io (use GitHub login)
2. Click **Create Service** → **MySQL**
3. Select **Free Plan** (1 month free trial)
4. Choose cloud provider: **AWS** + region closest to you (e.g., `aws-us-east-1`)
5. Service name: `hotel-mysql`
6. Click **Create Service** (takes ~2 minutes)

### Get Connection Details

Once running:
1. Click on your service → **Overview** tab
2. Copy these values:
   - **Host**: `mysql-xxxxx.aivencloud.com`
   - **Port**: `12345` (something like this)
   - **User**: `avnadmin`
   - **Password**: (click eye icon to reveal)
   - **Database**: `defaultdb`

### Build Connection URL

```
jdbc:mysql://HOST:PORT/defaultdb?sslmode=require
```

Example:
```
jdbc:mysql://mysql-abc123.aivencloud.com:12345/defaultdb?sslmode=require
```

---

## Step 2: Deploy Backend on Render

1. Sign up at https://render.com (use GitHub)
2. Click **New +** → **Web Service**
3. Connect GitHub repository: **`Kavibarath/hotel-reservation-system`**
4. Configure:
   - **Name**: `hotel-reservation-backend`
   - **Region**: closest to you
   - **Branch**: `main`
   - **Runtime**: **Docker**
   - **Instance Type**: **Free**

5. Add Environment Variables (click **Advanced**):

```
PORT=8080
SPRING_DATASOURCE_URL=jdbc:mysql://mysql-xxxxx.aivencloud.com:12345/defaultdb?sslmode=require
SPRING_DATASOURCE_USERNAME=avnadmin
SPRING_DATASOURCE_PASSWORD=your-aiven-password-here
JWT_SECRET=my-super-secret-jwt-key-min-32-chars-aurelia-grand-2026
JWT_EXPIRATION=3600000
```

6. Click **Create Web Service**
7. Wait for build (~5-10 minutes for first deployment)
8. Once deployed, copy your backend URL: `https://hotel-reservation-backend.onrender.com`

> **Note**: Render free tier sleeps after 15 min idle. First request after sleep takes ~30 sec to wake up.

---

## Step 3: Deploy Frontend on Vercel

1. Sign up at https://vercel.com (use GitHub)
2. Click **Add New** → **Project**
3. Import repository: **`Kavibarath/hotel-reservation-system`**
4. Configure:
   - **Framework Preset**: **Vite**
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build` (auto-filled)
   - **Output Directory**: `dist` (auto-filled)

5. Add Environment Variable:

```
VITE_API_URL=https://hotel-reservation-backend.onrender.com
```

6. Click **Deploy**
7. Wait ~2 minutes
8. Your frontend is live at: `https://hotel-reservation-system.vercel.app`

---

## Step 4: Initialize Database

The first time backend starts, JPA creates tables automatically.

**Default Admin Login** (from DataSeeder):
- **Email**: `admin@aurelia.com`
- **Password**: `admin123`

---

## ✅ Test Your Deployment

1. Open frontend URL: `https://hotel-reservation-system.vercel.app`
2. Wait 30s for backend to wake up (first time only)
3. Register a new user account
4. Browse rooms, make a booking
5. Login as admin to test dashboard

---

## 🔧 Troubleshooting

**Backend won't start**
- Check Render logs → Look for database connection errors
- Verify all environment variables are set correctly
- Ensure Aiven database is running

**CORS errors**
- Backend's `SecurityConfig` already allows all origins via `setAllowedOriginPatterns(List.of("*"))`
- If still failing, check browser console for actual error

**Frontend shows blank page**
- Check Vercel deployment logs
- Verify `VITE_API_URL` is set correctly (no trailing slash)
- Open browser DevTools → Console for errors

**"Backend sleeping" delay**
- Render free tier sleeps after 15 min idle
- First request takes ~30 sec to wake up
- Solution: Upgrade to paid ($7/mo) OR use a cron service to ping every 14 min

**Database connection failed**
- Verify Aiven service is in **RUNNING** state
- Check the connection URL includes `?sslmode=require`
- Ensure password is correct (no extra spaces)

---

## 🆓 After Aiven Free Trial Expires

After 1 month, Aiven becomes paid. Switch to:

**Option A**: Convert to PostgreSQL + Neon (free forever)
- Free 0.5GB PostgreSQL database
- Requires changing JPA dialect in code

**Option B**: Use FreeSQLDatabase.com
- Free MySQL with 5MB limit
- Slower but free forever

**Option C**: Pay Aiven (~$25/month) for production use

---

## 🔄 Continuous Deployment

Every `git push origin main` automatically redeploys:
- ✅ **Render** rebuilds and redeploys backend
- ✅ **Vercel** rebuilds and redeploys frontend

No manual steps required!

---

## 🎯 Custom Domain (Optional)

### Vercel (Frontend)
1. Vercel dashboard → Project → **Settings** → **Domains**
2. Add your domain → follow DNS instructions
3. Free SSL automatic

### Render (Backend)
1. Render dashboard → Service → **Settings** → **Custom Domain**
2. Add subdomain (e.g., `api.yourdomain.com`)
3. Update DNS records as shown

---

## 📊 Free Tier Limits

| Service | Free Tier |
|---------|-----------|
| **Render Backend** | 750 hrs/month, 512MB RAM, sleeps after 15min |
| **Vercel Frontend** | Unlimited bandwidth, 100GB transfer |
| **Aiven MySQL** | 1 month free (then ~$25/mo) |

---

**Built by Kavibarath**

Live site: Your deployed URLs will appear here once ready!
