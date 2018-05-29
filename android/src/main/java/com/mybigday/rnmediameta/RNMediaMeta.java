package com.mybigday.rnmediameta;

import android.content.Context;
import android.graphics.Bitmap;
import wseemann.media.FFmpegMediaMetadataRetriever;
import android.util.Base64;
import android.graphics.Matrix;

import com.facebook.react.bridge.Arguments;
// import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
// import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;

import java.io.ByteArrayOutputStream;
import java.io.File;


public class RNMediaMeta extends ReactContextBaseJavaModule {
  private Context context;

  public RNMediaMeta(ReactApplicationContext reactContext) {
    super(reactContext);

    this.context = (Context) reactContext;
  }

  @Override
  public String getName() {
    return "RNMediaMeta";
  }

  // Related to https://github.com/wseemann/FFmpegMediaMetadataRetriever/blob/master/gradle/fmmr-library/library/src/main/java/wseemann/media/FFmpegMediaMetadataRetriever.java#L632
  private final String[] metadatas = {
    "album",
    "album_artist",
    "performer",
    "track",
    "variant_bitrate",
    "artist",
    "composer",
    "title",
    "rotation"
  };

  private String convertToBase64(byte[] bytes) {
    return Base64.encodeToString(bytes, Base64.NO_WRAP);
  }

  private byte[] convertToBytes(Bitmap bmp) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bmp.compress(Bitmap.CompressFormat.PNG, 75, stream);
    return stream.toByteArray();
  }

  private void putString(WritableMap map, String key, String value) {
    if (value != null) map.putString(key, value);
  }

  private void getMetadata(String path, ReadableMap options, Promise promise) {
    File f = new File(path);
    if (!f.exists() || f.isDirectory()) {
      promise.reject("-15", "file not found");
      return;
    }

    // duration in seconds
    int durationDivisor = 1000;

    FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
    WritableMap result = Arguments.createMap();
    try {
      mmr.setDataSource(path);

      // check is audio
      String audioCodec = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_AUDIO_CODEC);

      if (audioCodec == null) {
        promise.resolve(result);
        mmr.release();
        return;
      }

      for (String meta: metadatas) {
        putString(result, meta, mmr.extractMetadata(meta));
      }
      result.putInt("duration",Integer.parseInt(mmr.extractMetadata("duration"))/durationDivisor);

      if (options.getBoolean("getThumb")) {
        // get thumb
        byte [] bytes = mmr.getEmbeddedPicture();
        if (bytes != null) {
          result.putString("thumb", convertToBase64(bytes));
        }
      }

      if (options.getBoolean("getChapters")) {        
        int chapterCount = Integer.parseInt(mmr.extractMetadata(
          FFmpegMediaMetadataRetriever.METADATA_CHAPTER_COUNT));
        if (chapterCount != 0){
          WritableArray chapters =  Arguments.createArray();          
          for(int i=0; i<chapterCount; i++)
          {
            WritableMap chapter = Arguments.createMap();
            int startTime = Integer.parseInt(mmr.extractMetadataFromChapter(
              FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_START_TIME,i))/durationDivisor;
            int endTime = Integer.parseInt(mmr.extractMetadataFromChapter(
              FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_END_TIME,i))/durationDivisor;
            chapter.putInt(FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_START_TIME,startTime);
            chapter.putInt(FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_END_TIME,endTime);
            chapter.putInt("duration",endTime-startTime);            
            putString(chapter, "title", mmr.extractMetadataFromChapter("title",i));            
            chapters.pushMap(chapter);
          }          
          result.putArray("chapters", chapters);
        }        
      }

    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      promise.resolve(result);
      mmr.release();
    }
  }

  @ReactMethod
  public void get(final String path, final ReadableMap options, final Promise promise) {
    getMetadata(path, options, promise);
  }
}
 