# Lottie Animation Integration Guide

## Overview

This guide explains how to integrate Lottie animations into the Telegram Drive Uploader app. The app uses a fallback system that provides built-in animated icons when Lottie `.json` assets are not available.

## Quick Start

### 1. Add Lottie Assets

Place your `.json` Lottie files in:
```
feature/src/main/res/raw/
```

Recommended files:
- `empty_queue.json` - Empty upload queue state
- `empty_upload.json` - Empty upload screen state  
- `no_connection.json` - Offline/no Telegram connection
- `upload_error.json` - Upload failure state
- `upload_success.json` - Successful upload completion
- `uploading.json` - Active upload in progress
- `preparing.json` - File preparation/compression state

### 2. Asset Requirements

| Property | Specification |
|----------|---------------|
| Format | Lottie `.json` (Bodymovin export) |
| Max file size | 200 KB (recommended) |
| Dimensions | 200×200 to 400×400 px |
| Frame rate | 30 or 60 fps |
| Duration | 1-3 seconds (looped) |
| Background | Transparent |
| Layers | Minimize for performance |

### 3. Using Animations in Code

```kotlin
// Empty state with animation
EmptyState(
    icon = Icons.Default.VideoLibrary,
    title = "No videos",
    supportingText = "Add videos to upload",
    animation = LottieAnimations.emptyUpload
)

// Error state with animated icon
ErrorState(
    message = "Upload failed",
    onRetryClick = { retry() },
    animation = LottieAnimations.uploadError
)

// Skeleton loading
SkeletonScreen(variant = SkeletonVariant.VideoItem)
```

### 4. Animation Types

| Type | Use Case | Default Speed |
|------|----------|---------------|
| `EmptyState` | Empty lists, no content | 1.0x |
| `ErrorState` | Failures, retries | 1.0x |
| `LoadingState` | Spinners, progress | 1.0x |
| `SuccessState` | Completion, confirmation | 1.0x |
| `NoConnection` | Offline states | 1.0x |

### 5. Custom Animation

```kotlin
// Custom Lottie asset
EmptyState(
    icon = Icons.Default.CloudQueue,
    title = "Custom State",
    supportingText = "Description",
    animation = LottieAnimation.LoadingState("my_custom_animation.json", speed = 1.5f)
)
```

### 6. Fallback System

When Lottie assets are missing, the app automatically uses built-in animated icons:
- **Loading**: Rotating hourglass with pulse scale
- **Error**: Pulsing error icon with color flash
- **Empty**: Gentle floating video library icon
- **Success**: Checkmark with scale bounce
- **No Connection**: Cloud with subtle rotation

The fallback respects the system "Reduce motion" accessibility setting.

### 7. Performance Tips

1. **Keep files small** - Under 200KB for smooth loading
2. **Use simple shapes** - Avoid complex gradients/effects
3. **Limit layers** - Max 10-15 layers per animation
3. **Test on low-end devices** - Verify 60fps on 5+ year old phones

### 8. Integration with Design System

Animations automatically adapt to:
- Light/dark theme colors
- Material You dynamic colors (Android 12+)
- Brand accent colors (secondary/tertiary roles)
- Reduced motion accessibility setting

### 9. Testing Checklist

- [ ] Animation plays correctly in light theme
- [ ] Animation plays correctly in dark theme  
- [ ] Respects "Reduce motion" setting
- [ ] No visual artifacts at 0.5x/2x animation speed
- [ ] File size under 200KB
- [ ] Transparent background renders correctly
- [ ] Loops seamlessly (no visible jump)

### 10. Resources

- [LottieFiles](https://lottiefiles.com/) - Free animations
- [Bodymovin](https://github.com/airbnb/lottie-web) - After Effects exporter
- [Material Motion](https://m3.material.io/foundations/motion/overview) - Guidelines

---

## Migration from Static Icons

If migrating from static icons:

1. Replace `Icon(...)` with `AnimatedEmptyStateIcon(animationType = ...)`
2. Remove manual `animateFloatAsState` rotation/scale logic
3. Add `animation` parameter to `EmptyState`/`ErrorState` calls
4. Test with `LottieAnimations` presets first

---

## Support

For questions or issues:
- Check existing `LottieAnimations` presets
- Verify `.json` validity at [LottieFiles Preview](https://lottiefiles.com/preview)
- Ensure `lottie-compose` dependency is in `feature/build.gradle.kts`