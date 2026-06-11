# XEdgeCrop Helper

A lightweight, root-enabled companion application designed to restore the broken partial screenshot functionality in XEdgePro on modern Android builds (Android 14+).

## The Problem
On newer Android versions, Google's strict SELinux policies (`neverallow` rules) prevent background system processes like `SystemUI` from executing shell commands. When XEdgePro attempts to trigger a partial screenshot via root shell directly from its injected state, the kernel silently kills the execution, resulting in a dead trigger.

## The Solution
To bypass these deep-system constraints, this project adopts a decoupled, microservice-like architecture:
1. **The Smali Patch:** A custom patch redirects the XEdgePro partial screenshot trigger. Instead of executing a shell command, it utilizes a native Android `Intent` via the `SystemUI` context.
2. **The Helper App:** This standalone APK catches the intent in the unrestricted `untrusted_app` domain. It then safely requests Magisk/KernelSU root privileges to execute the `screencap` command, process the cropped bitmap, and save it directly to the gallery.

## Tested On
* Nothing Phone 3a
* Android 16

## Installation & Usage
1. Download and install the `app-release.apk` from the [Releases](../../releases) section.
2. Launch the application once (or trigger it via XEdgePro) to grant **Superuser** permissions.
3. Apply the Smali injection (`patch_5.md`) to your local XEdgePro module and recompile. (or use Patch 5 (or newer) from [XposedEdgeProPatched](https://github.com/trup40/XposedEdgeProPatchedforA16/releases))

## Third-Party Integration
Since the Helper is built as a standalone microservice, you can easily trigger the partial screenshot crop interface from any other application, automation tool (like Tasker/MacroDroid), or terminal.

### 1. Via Shell (Terminal / ADB / Root)
You can launch the crop interface using the standard Activity Manager (`am`) command. If you are calling this from a background service or a restricted environment, you may need to prefix it with `su -c`.

```bash
am start --user 0 -n com.trup40.xedgecrop/.MainActivity
```

### 2. Via Java / Kotlin (Native Intent)
If you are developing your own Android application or Xposed module, you can trigger the Helper using a standard explicit Intent:

```kotlin
val intent = Intent().apply {
    setClassName("com.trup40.xedgecrop", "com.trup40.xedgecrop.MainActivity")
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Required if calling from outside an Activity context
}
context.startActivity(intent)
```

### 3. Via Tasker / MacroDroid
To map this crop tool to a custom gesture or trigger using automation apps, configure a "Send Intent" action with the following parameters:
* **Action:** *Leave blank*
* **Package:** `com.trup40.xedgecrop`
* **Class:** `com.trup40.xedgecrop.MainActivity`
* **Target:** `Activity`

## Credits & Author
Developed by **trup40** (Eagle)