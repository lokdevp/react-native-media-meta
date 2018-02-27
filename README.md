# React Native Media Meta 

> Get media file metadata in your React Native app

## Modifications from the orginal repo

> Changes are only for Android

  * Only audio metadata (video files ignored)
  * Duration in seconds 
  * Chapters metadata for audio 

## Installation

```bash
$ npm install git+https://github.com/lokdevp/react-native-media-meta.git
$ react-native link
```

## Usage

```js
import MediaMeta from 'react-native-media-meta';
const path = '<your file path here>';

MediaMeta.get(path)
  .then(metadata => console.log(metadata))
  .catch(err => console.error(err));
```

```js
// for metadata with chapters
const metadataWithChapters = await MediaMeta.get(path, {
          getChapters: true,
});
```

## API

#### `MediaMeta.get(path)` - Promise

Resolve: Object - included following keys (If it's found)
* `thumb` - Base64 image string (audio: get artwork if exist)
* `duration` 
* `width` - the thumb width
* `height` - the thumb height
* Others:

__*[Android]*__ We using [FFmpegMediaMetadataRetriever](https://github.com/wseemann/FFmpegMediaMetadataRetriever), see [RNMediaMeta.java#L36](android/src/main/java/com/mybigday/rn/RNMediaMeta.java#L36) for more information.  
__*[iOS]*__ We using [official AVMatadataItem](https://developer.apple.com/library/mac/documentation/AVFoundation/Reference/AVFoundationMetadataKeyReference/#//apple_ref/doc/constant_group/Common_Metadata_Keys), see [RNMediaMeta.m#L9](ios/RNMediaMeta/RNMediaMeta.m#L9) for more information.

## License

[MIT](LICENSE.md)
