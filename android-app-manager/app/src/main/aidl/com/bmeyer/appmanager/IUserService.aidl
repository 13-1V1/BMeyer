// AIDL interface for the Shizuku user service. The service process is spawned
// by Shizuku with shell (ADB) or root privileges, letting it run `pm uninstall`
// silently — no per-app confirmation dialog.
package com.bmeyer.appmanager;

interface IUserService {
    // Shizuku's required destroy transaction id; called when the service unbinds.
    void destroy() = 16777114;

    // Uninstalls one package via `pm uninstall`. Returns "Success" or an error line.
    String uninstall(String packageName) = 1;
}
