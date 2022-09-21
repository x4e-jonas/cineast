package org.vitrivr.cineast.core.iiif.imageapi.v2;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.EXTENSION_JPG;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.EXTENSION_PNG;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_BITONAL;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_COLOR;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_DEFAULT;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_GRAY;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_BASE_URI_REDIRECT;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_CORS;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_JSONLD_MEDIA_TYPE;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_REGION_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_REGION_BY_PX;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_ROTATION_BY_90s;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_BY_CONFINED_WH;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_BY_DISTORTED_WH;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_BY_H;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_BY_PCT;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_BY_W;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2.ProfileItem.SUPPORTS_SIZE_BY_WH;

/**
 * Class to check for IIIF Image API Compliance Level specific support for features, quality and image formats
 */
public class ImageApiCompliance_v2 {

  public static String LEVEL_0 = "http://iiif.io/api/image/2/level0.json";
  public static String LEVEL_1 = "http://iiif.io/api/image/2/level1.json";
  public static String LEVEL_2 = "http://iiif.io/api/image/2/level2.json";

  public static boolean isFeatureSupported(String feature, String level) {
    if (level == null || level.isEmpty()) {
      return false;
    }
    return switch (feature) {
      // Features supported by both level 1 and 2
      case SUPPORTS_REGION_BY_PX, SUPPORTS_SIZE_BY_W, SUPPORTS_SIZE_BY_H, SUPPORTS_SIZE_BY_PCT -> level.equals(LEVEL_1) || level.equals(LEVEL_2);
      // Features only supported by level 2
      case SUPPORTS_REGION_BY_PCT, SUPPORTS_SIZE_BY_CONFINED_WH, SUPPORTS_SIZE_BY_DISTORTED_WH, SUPPORTS_SIZE_BY_WH, SUPPORTS_ROTATION_BY_90s -> level.equals(LEVEL_2);
      default -> false;
    };
  }

  public static boolean isQualitySupported(String quality, String level) {
    // All levels support the default quality
    if (quality.equals(QUALITY_DEFAULT)) {
      return true;
    }
    if (level.equals(LEVEL_2)) {
      return quality.equals(QUALITY_COLOR) || quality.equals(QUALITY_GRAY) || quality.equals(QUALITY_BITONAL);
    }
    return false;
  }

  public static boolean isFormatSupported(String format, String level) {
    return switch (format) {
      case EXTENSION_JPG ->
        // All levels support the jpg format
          true;
      case EXTENSION_PNG -> level.equals(LEVEL_2);
      default -> false;
    };
  }

  public static boolean isHttpFeatureSupported(String httpFeature, String level) {
    if (httpFeature.equals(SUPPORTS_BASE_URI_REDIRECT) || httpFeature.equals(SUPPORTS_CORS) || httpFeature.equals(SUPPORTS_JSONLD_MEDIA_TYPE)) {
      return level.equals(LEVEL_1) || level.equals(LEVEL_2);
    }
    return false;
  }
}
