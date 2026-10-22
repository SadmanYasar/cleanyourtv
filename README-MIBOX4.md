# Xiaomi Mi Box 4 (oneday) - Clean Apple TV Setup Guide

This guide explains your new Apple TV-style home screen, the debloated packages, how to customize dock apps or wallpapers, how to restore apps, and how to safely close debugging.

---

## 1. Summary of Changes Made

- **Launcher Replaced**: Built and installed the clean Apple TV 4K-style launcher (`com.gathin.tvhome`), configured with a 4-slot dock row:
  - YouTube (`com.google.android.youtube.tv`)
  - Netflix (`com.netflix.ninja`)
  - BrowseHere (`com.tcl.browser`)
  - Google Play Games (`com.google.android.play.games`)
- **Stock Bloatware Disabled**: Stock Google TV launcher, PatchWall, Mi analytics, Alphonso telemetry, Xiaomi promo services, and unused streaming apps were safely disabled for user 0 (never uninstalled, completely reversible).
- **Services Kept**:
  - Google Voice Assistant & Remote Mic button
  - Chromecast Built-in
  - Android TV Remote phone control service
  - Google Play Store & Google Play Services
- **Private DNS Ad-Blocking**: Set to `dns.adguard-dns.com` (blocks tracker/ad domains system-wide).
- **Screensaver**: Switched to the modern clean Clock screensaver (`com.gathin.tvhome/.ClockDream`).
- **Free RAM**: Increased from ~770 MB to **~990 MB+**, providing a visibly snappier interface.

---

## 2. Re-enabling an App

If you ever want to re-enable an individual app (e.g. Disney+ Hotstar or Prime Video):

```bash
# With relay running (or directly on the TV IP):
TV_HOST=127.0.0.1 TV_PORT=15555 .venv/bin/python3 tools/tv.py "pm enable --user 0 <package-name>"
```

Example for Prime Video:
```bash
TV_HOST=127.0.0.1 TV_PORT=15555 .venv/bin/python3 tools/tv.py "pm enable --user 0 com.amazon.amazonvideo.livingroom"
```

To re-enable all debloated apps and revert back to stock Google TV launcher:
```bash
./restore-all.sh
```

---

## 3. Changing Dock Order, Columns, or Wallpaper

### A. Changing Dock Apps & Column Count
1. Open `launcher/app/src/main/res/values/config.xml`.
2. Locate `<string-array name="dock_apps">`:
   ```xml
   <string-array name="dock_apps">
       <item>com.google.android.youtube.tv</item>
       <item>com.netflix.ninja</item>
       <item>com.tcl.browser</item>
       <item>com.google.android.play.games</item>
   </string-array>
   ```
   Add, remove, or re-order packages.
3. If you want 5, 6, or 7 columns, change `<integer name="columns">4</integer>`.
4. Rebuild and reinstall with one command:
   ```bash
   ./rebuild-launcher.sh
   ```

### B. Changing Wallpaper / Themes
To apply an existing theme or custom wallpaper without rebuilding:
```bash
TV_HOST=127.0.0.1 TV_PORT=15555 ./tools/theme.sh themes/aurora
```
Or place your custom 1920x1080 JPEG/PNG wallpaper as `wallpaper.jpg` in a theme folder and push it.

---

## 4. Undoing Ad-Blocking Private DNS

If any streaming service complains about DNS resolution:
```bash
TV_HOST=127.0.0.1 TV_PORT=15555 .venv/bin/python3 tools/tv.py "settings put global private_dns_mode opportunistic"
```

---

## 5. Safely Closing USB Debugging & Securing the Device

Now that the setup is complete and verified persistent across reboots:

1. **On your TV**:
   - Go to **Settings → Device Preferences → Developer options**.
   - Turn **USB debugging** OFF.
   - Click **Revoke USB debugging authorizations**.
2. **Reboot your TV**:
   - Settings → About → Restart.
3. **USB Port Power Restored**:
   - On the Mi Box 4, turning USB debugging OFF switches the USB register back to host mode, restoring full power to any USB flash drives, hard drives, or USB accessories plugged into the box.
4. **Stop the Relay**:
   - In the Terminal window running `tools/relay.py`, press `Ctrl + C` to stop the relay.
5. **Verify ADB is Closed**:
   ```bash
   .venv/bin/python3 tools/adb-closed.py 192.168.0.79
   ```
   It should confirm that port 5555 connection is refused.
