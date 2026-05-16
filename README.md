# Literal Radio

The quiet companion.

A minimalist internet radio player for Android.

<p>
  <a href="https://github.com/256x/radio/releases/latest"><img src="https://img.shields.io/github/v/release/256x/radio?label=GitHub%20Release"></a>&nbsp;<img src="https://img.shields.io/badge/Android-9%2B-blue">&nbsp;<img src="https://img.shields.io/badge/license-MIT-lightgrey">
</p>

[User Guide](./docs/USER_GUIDE.md)

## Features

- Browse stations by Genre, Country, Language, or Name
- 22 curated genre presets
- Filter bar to narrow any list on the fly
- Mini player persistent at the bottom while browsing
- Full player screen with ICY track metadata, codec, bitrate, and tags
- Resumes last station on relaunch
- Color customization — background, text, accent
- No account. No ads. No tracking.

## Notes

**Country and language lists are cached.** Fetched once and reused for 24 hours. Tap **Clear list cache** in Settings to force a refresh.

**Some stations use HTTP.** Android 9+ blocks cleartext traffic by default; this app explicitly allows it to reach all available stations.

## Development

- Kotlin / Jetpack Compose / Media3 (ExoPlayer)
- Station data: [radio-browser.info](https://www.radio-browser.info)
- Target: Android 9.0+

This app was built with substantial assistance from [Claude](https://claude.ai) (Anthropic).

## License

MIT
