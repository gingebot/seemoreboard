# Releasing SeeMoreBoard

SeeMoreBoard releases are built and signed by GitHub Actions. The app
compiles against and targets Android API 36, as required by Google Play. Do not
commit the upload keystore, passwords, or any other signing material.

## Required GitHub environment

Create a GitHub environment named `release`. Store these repository or
environment secrets in it:

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded upload keystore |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Upload key alias |
| `ANDROID_KEY_PASSWORD` | Upload key password |

The release workflow is the only workflow that uses this environment.
The keystore is decoded into the runner's temporary directory and removed
after the job, including when the build fails.

## Versioning

Update both values in `/gradle.properties` before each release:

```properties
app.versionCode=2
app.versionName=1.0.1
```

`app.versionCode` must be a positive integer and must be greater than the
version code used by every previous Play release. `app.versionName` uses
semantic-version-style values such as `1.0.0`, `1.1.0`, and `1.1.1`.

## Creating a release

1. Update `app.versionCode` and `app.versionName`.
2. Run the normal checks locally:

   ```bash
   ./gradlew testDebugUnitTest lint assembleDebug --no-daemon
   ```

3. Commit and push the version change to `main`.
4. Create a tag whose name is exactly `v` followed by `app.versionName`:

   ```bash
   git tag v1.0.1
   git push origin v1.0.1
   ```

5. GitHub Actions validates the tag, runs tests and lint, decodes the
   upload keystore, and builds the signed AAB and APK.
6. The workflow creates a GitHub Release containing:

   - `SeeMoreBoard-<version>-release.aab` for Google Play
   - `SeeMoreBoard-<version>-release.apk` for direct testing
   - `SHA256SUMS` for artifact verification

The tag must point to the commit containing the matching version values.
If the workflow fails, fix the issue in a new commit and create a new
version/tag rather than reusing a published tag.

## Google Play

Upload the AAB to the Google Play Console. Google Play App Signing should
manage the app-signing key; the keystore stored in the GitHub environment
is the upload key. The application ID is:

```text
com.seemoreboard.keyboard
```

Never lose the upload keystore or its passwords. Losing them can prevent
future updates unless Google Play approves an upload-key reset.

## Local signing behavior

Local release signing is intentionally not configured through the
repository. `assembleRelease` and `bundleRelease` require these
environment variables and fail if they are absent:

```text
ANDROID_KEYSTORE_FILE
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

Normal debug builds and tests do not require signing secrets.