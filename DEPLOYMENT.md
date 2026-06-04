# Deployment Guide — Railway

Deploy the Aurelia Grand hotel reservation system to Railway in 3 services:
1. **MySQL Database**
2. **Spring Boot Backend**
3. **React Frontend**

---

## Prerequisites

- GitHub repository pushed (✅ already done)
- Railway account: https://railway.app (sign up with GitHub)
- Railway gives you **$5/month free credit** — enough for this app

---

## Step 1: Create Railway Project

1. Go to https://railway.app/new
2. Click **Deploy from GitHub repo**
3. Select **`Kavibarath/hotel-reservation-system`**
4. Railway will auto-detect the project

---

## Step 2: Add MySQL Database

1. In your Railway project, click **+ New**
2. Select **Database** → **Add MySQL**
3. Railway provisions a MySQL instance automatically
4. Click on the MySQL service → **Variables** tab
5. Copy these values (you'll need them):
   - `MYSQLHOST`
   - `MYSQLPORT`
   - `MYSQLDATABASE`
   - `MYSQLUSER`
   - `MYSQLPASSWORD`

---

## Step 3: Deploy Backend (Spring Boot)

1. Click on the **backend service** (auto-created from repo)
2. Go to **Settings** → set **Root Directory**: `/` (project root)
3. Go to **Variables** tab and add:

```
PORT=8080
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JWT_SECRET=your-super-secret-jwt-key-minimum-32-characters-required
JWT_EXPIRATION=3600000
CORS_ALLOWED_ORIGINS=https://YOUR-FRONTEND-DOMAIN.up.railway.app
```

4. Click **Settings** → **Networking** → **Generate Domain**
5. Copy the backend URL (e.g., `https://hotel-backend.up.railway.app`)

---

## Step 4: Deploy Frontend (React)

1. In Railway, click **+ New** → **GitHub Repo** → select same repo
2. Settings → **Root Directory**: `/frontend`
3. Go to **Variables** tab:

```
VITE_API_URL=https://YOUR-BACKEND-DOMAIN.up.railway.app
PORT=4173
```

4. Settings → **Networking** → **Generate Domain**
5. Copy the frontend URL (e.g., `https://hotel-frontend.up.railway.app`)

---

## Step 5: Update Backend CORS

1. Go back to **backend service** → **Variables**
2. Update `CORS_ALLOWED_ORIGINS` to your frontend URL:

```
CORS_ALLOWED_ORIGINS=https://hotel-frontend.up.railway.app
```

3. Backend will auto-redeploy

---

## Step 6: Initialize Database

The first time Spring Boot starts, JPA will create tables automatically (`ddl-auto=update`).

Default admin login (from DataSeeder):
- **Email**: `admin@aurelia.com`
- **Password**: `admin123`

---

## ✅ Verify Deployment

1. Open your frontend URL: `https://your-frontend.up.railway.app`
2. You should see the luxury hotel homepage
3. Try registering a new user
4. Browse rooms and make a booking
5. Login as admin to test admin dashboard

---

## 🔧 Troubleshooting

**Backend fails to start**
- Check logs: Railway → Backend service → **Deployments** → View Logs
- Ensure all environment variables are set
- Verify MySQL service is running

**CORS errors in browser**
- Ensure `CORS_ALLOWED_ORIGINS` matches exact frontend URL (no trailing slash)
- Redeploy backend after updating CORS

**Frontend shows blank page**
- Check that `VITE_API_URL` is set correctly
- Open browser console (F12) for errors
- Verify build succeeded in deployment logs

**Database connection failed**
- Verify all 5 MySQL variables are correctly referenced with `${{MySQL.VAR}}` syntax
- Check MySQL service is in same project

---

## 💰 Cost Estimate

| Service | Memory | Monthly Cost |
|---------|--------|--------------|
| Backend (512MB) | ~$2 |
| Frontend (256MB) | ~$1 |
| MySQL (512MB) | ~$2 |
| **Total** | **~$5/month** |

Railway's **$5 free credit** typically covers this fully for small projects.

---

## 🎯 Custom Domain (Optional)

1. Buy a domain (Namecheap, GoDaddy, etc.)
2. Railway service → **Settings** → **Domains** → **Custom Domain**
3. Add CNAME record at your DNS provider
4. Railway provides SSL certificate automatically

---

## 🔄 Continuous Deployment

Every `git push origin main` automatically redeploys both services on Railway. No manual steps required!

---

**Made by Kavibarath**
