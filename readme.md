# Kvaesitso Plugin for OneDrive

This plugin allows you to search for files in your OneDrive account.

> [!IMPORTANT]  
> Plugins are a preview feature and are currently only available
> in [Kvaesitso Nightly](https://fdroid.mm20.de/app/de.mm20.launcher2.nightly)

## Building

To build this plugin, you need to setup a new project in
the [Microsoft Azure Portal](https://portal.azure.com/).

1. Go to the [Microsoft Azure Portal](https://portal.azure.com)
1. Create a new project.
    1. Search for App Registrations
    1. Add a new registration
        1. Supported account types: Accounts in any organizational directory and personal Microsoft
           accounts
        1. Leave "Redirect URI" empty
1. Add an authentication platform
    1. Go to Authentication
    1. Add a platform > Android
    1. Enter the debug package name (de.mm20.launcher2.plugin.onedrive) and the signature hash of
       your debug
       key
        1. You can use the following command to generate the signature hash:
           `keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore | openssl sha1 -binary | openssl base64`
    1. Click Configure > Done
    1. In the newly created Android section, click on Add URI
    1. Add package name (de.mm20.launcher2.plugin.onedrive) and signature hash of your release key
1. Download the client details
    1. In the debug client row, click on View
    1. Copy the JSON below MSAL Configuration
       to `ms-services/src/debug/res/raw/msal_auth_config.json` (you need to create this file
       first)
    1. Add `"account_mode": "SINGLE"` to the JSON (see the provided `msal_auth_config_example.json`)
1. Repeat the previous step for the release config
1. Add the required scopes
    1. Go to API permissions
    1. Add a permission
    1. Select Microsoft Graph > Delegated permissions
    1. Tick the following scopes:
        - Files.Read.All
        - User.Read
    1. Click Add permissions

## License

This plugin is licensed under the Apache License 2.0.

```
Copyright 2023 MM2-0 and the Kvaesitso contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```