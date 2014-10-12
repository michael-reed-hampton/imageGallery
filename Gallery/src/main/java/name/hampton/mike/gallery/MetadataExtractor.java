package name.hampton.mike.gallery;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// package org.apache.commons.imaging.examples;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegPhotoshopMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataExtractor {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public Map<String, Object> extractImageMetaData(final InputStream inputStream, Map<String, Object> metadataH) throws ImageReadException, IOException {
        // get all metadata stored in EXIF format (ie. from JPEG or TIFF).
        final IImageMetadata metadata = Imaging.getMetadata(inputStream, null);

        if (metadata instanceof JpegImageMetadata) {
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

            // Jpeg EXIF metadata is stored in a TIFF-based directory structure
            // and is identified with TIFF tags.
            // Here we look for the "x resolution" tag, but
            // we could just as easily search for any other tag.
            //
            // see the TiffConstants file for a list of TIFF tags.

            // out.println("file: " + file.getPath());
            JpegPhotoshopMetadata photoshopmetadata = jpegMetadata.getPhotoshop();
            metadataH.put("photoshop", photoshopmetadata);
            BufferedImage thumbnail = jpegMetadata.getEXIFThumbnail();
            metadataH.put("hasEXIFThumbnail", null!=thumbnail);

            Map<String, Object> jpegMetadataH = new HashMap<String, Object>();
            metadataH.put("jpegMetadata", jpegMetadataH);

            // print out various interesting EXIF tags.
            recordTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_XRESOLUTION, jpegMetadataH);
            recordTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME, jpegMetadataH);
            recordTagValue(jpegMetadata,
                    ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, jpegMetadataH);
            recordTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, jpegMetadataH);
            recordTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO, jpegMetadataH);
            recordTagValue(jpegMetadata,
                    ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE, jpegMetadataH);
            recordTagValue(jpegMetadata,
                    ExifTagConstants.EXIF_TAG_APERTURE_VALUE, jpegMetadataH);
            recordTagValue(jpegMetadata,
                    ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE, jpegMetadataH);
            recordTagValue(jpegMetadata,
                    GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF, jpegMetadataH);
            recordTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE, jpegMetadataH);
            recordTagValue(jpegMetadata,
                    GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF, jpegMetadataH);
            recordTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE, jpegMetadataH);

            // simple interface to GPS data
            final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
            if (null != exifMetadata) {
                final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                if (null != gpsInfo) {
                    final String gpsDescription = gpsInfo.toString();
                    final double longitude = gpsInfo.getLongitudeAsDegreesEast();
                    final double latitude = gpsInfo.getLatitudeAsDegreesNorth();

                    jpegMetadataH.put("GPSDescription", gpsDescription);
                    jpegMetadataH.put("GPSLongitude",longitude);
                    jpegMetadataH.put("GPSLatitude", latitude);
                }
                List<TiffField> allFields = exifMetadata.getAllFields();
                List<Map<String, Object>> allFieldsL = new ArrayList<Map<String, Object>>(); 
                jpegMetadataH.put("allFields", allFieldsL);
                if(null!=allFields){
                	for(TiffField field : allFields){
                		Map<String, Object> fieldH = new HashMap<String, Object>(); 
                		allFieldsL.add(fieldH);
                		fieldH.put("fieldType", field.getFieldTypeName());
                		if(field.getFieldType().equals(FieldType.RATIONAL))
                		{
                			// These fields format oddly, must be strings
                			fieldH.put("value", field.getValue().toString());
                		}	
                		fieldH.put("descriptionWithoutValue", field.getDescriptionWithoutValue());
                		fieldH.put("directoryType", field.getDirectoryType());
                		fieldH.put("tagName", field.getTagName());
                		fieldH.put("valueDescription", field.getValueDescription());
                	}
                }
            }

            // more specific example of how to manually access GPS values
            final TiffField gpsLatitudeRefField = jpegMetadata
                    .findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
            final TiffField gpsLatitudeField = jpegMetadata
                    .findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE);
            final TiffField gpsLongitudeRefField = jpegMetadata
                    .findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
            final TiffField gpsLongitudeField = jpegMetadata
                    .findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
            if (gpsLatitudeRefField != null && gpsLatitudeField != null
                    && gpsLongitudeRefField != null
                    && gpsLongitudeField != null) {
                // all of these values are strings.
                final String gpsLatitudeRef = (String) gpsLatitudeRefField.getValue();
                final RationalNumber gpsLatitude[] = (RationalNumber[]) (gpsLatitudeField
                        .getValue());
                final String gpsLongitudeRef = (String) gpsLongitudeRefField
                        .getValue();
                final RationalNumber gpsLongitude[] = (RationalNumber[]) gpsLongitudeField
                        .getValue();

                final RationalNumber gpsLatitudeDegrees = gpsLatitude[0];
                final RationalNumber gpsLatitudeMinutes = gpsLatitude[1];
                final RationalNumber gpsLatitudeSeconds = gpsLatitude[2];

                final RationalNumber gpsLongitudeDegrees = gpsLongitude[0];
                final RationalNumber gpsLongitudeMinutes = gpsLongitude[1];
                final RationalNumber gpsLongitudeSeconds = gpsLongitude[2];

                // This will format the gps info like so:
                //
                // gpsLatitude: 8 degrees, 40 minutes, 42.2 seconds S
                // gpsLongitude: 115 degrees, 26 minutes, 21.8 seconds E

                Map<String, Object> gpsH = new HashMap<String, Object>();
                jpegMetadataH.put("gps", gpsH);
                Map<String, Object> gpsLatitudeH = new HashMap<String, Object>();
                Map<String, Object> gpsLongitudeH = new HashMap<String, Object>();
                gpsH.put("latitude", gpsLatitudeH);
                gpsH.put("longitude", gpsLongitudeH);

                gpsLatitudeH.put("degrees", gpsLatitudeDegrees.toDisplayString());
                gpsLatitudeH.put("minutes", gpsLatitudeMinutes.toDisplayString());
                gpsLatitudeH.put("seconds", gpsLatitudeSeconds.toDisplayString());
                gpsLatitudeH.put("display", gpsLatitudeRef);
                gpsLongitudeH.put("degrees", gpsLongitudeDegrees.toDisplayString());
                gpsLongitudeH.put("minutes", gpsLongitudeMinutes.toDisplayString());
                gpsLongitudeH.put("seconds", gpsLongitudeSeconds.toDisplayString());
                gpsLongitudeH.put("display", gpsLongitudeRef);

            }
        }
        return metadataH;
    }

    private static void recordTagValue(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo, Map<String, Object> out) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field != null) {
            out.put(tagInfo.name, field.getValueDescription());
        }
    }
    
}
