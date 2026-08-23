# Release Checklist

- [ ] Final audit passed
- [ ] No release blockers
- [x] Version verified from Gradle source: 1.0.14 / code 14
- [x] Application ID verified: `com.telegramdrive.uploader`
- [x] Manifest statically reviewed
- [x] Permissions reviewed
- [ ] TDLib verified in final release environment
- [x] R8/resource-shrinking configuration reviewed
- [ ] Release build verified
- [ ] AAB verified
- [ ] APK verified if required
- [ ] Signing verified
- [ ] Authentication smoke test
- [ ] Real upload smoke test
- [ ] Background test
- [ ] Notification test
- [x] Security static check
- [ ] CI final check for release-preparation commit
- [x] Git cleanliness reviewed before this documentation change
- [x] Documentation complete for current evidence

Release status: **NOT RELEASE READY**. Do not publish to Google Play, create a GitHub Release, or distribute APK/AAB artifacts until unchecked gates are completed.
