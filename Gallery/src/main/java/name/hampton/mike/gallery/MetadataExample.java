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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.common.IImageMetadata.IImageMetadataItem;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegPhotoshopMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;

public class MetadataExample {
    //public static void metadataExample(final File file) throws ImageReadException,
    public static void metadataExample(final InputStream inputStream, PrintStream out) throws ImageReadException,
    	IOException {
    	metadataExample(inputStream, new PrintWriter(out));
    }

    public static void metadataExample(final InputStream inputStream, PrintWriter out) throws ImageReadException,
            IOException {
        // get all metadata stored in EXIF format (ie. from JPEG or TIFF).
        final IImageMetadata metadata = Imaging.getMetadata(inputStream, null);

        // out.println(metadata);

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
            out.println("    "
                    + "JpegPhotoshopMetadata: " + photoshopmetadata);
            BufferedImage thumbnail = jpegMetadata.getEXIFThumbnail();
            out.println("    "
                    + "EXIFThumbnail: " + thumbnail);
            

            // print out various interesting EXIF tags.
            printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_XRESOLUTION, out);
            printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME, out);
            printTagValue(jpegMetadata,
                    ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, out);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, out);
            printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO, out);
            printTagValue(jpegMetadata,
                    ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE, out);
            printTagValue(jpegMetadata,
                    ExifTagConstants.EXIF_TAG_APERTURE_VALUE, out);
            printTagValue(jpegMetadata,
                    ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE, out);
            printTagValue(jpegMetadata,
                    GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF, out);
            printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE, out);
            printTagValue(jpegMetadata,
                    GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF, out);
            printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE, out);

            out.println();

            // simple interface to GPS data
            final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
            if (null != exifMetadata) {
                final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                if (null != gpsInfo) {
                    final String gpsDescription = gpsInfo.toString();
                    final double longitude = gpsInfo.getLongitudeAsDegreesEast();
                    final double latitude = gpsInfo.getLatitudeAsDegreesNorth();

                    out.println("    " + "GPS Description: "
                            + gpsDescription);
                    out.println("    "
                            + "GPS Longitude (Degrees East): " + longitude);
                    out.println("    "
                            + "GPS Latitude (Degrees North): " + latitude);
                }
                List<TiffField> allFields = exifMetadata.getAllFields();
                if(null!=allFields){
                	for(TiffField field:allFields){
                		field.dump(out);
                		out.println();
                	}
                }
                List<? extends IImageMetadataItem> directories = exifMetadata.getDirectories();
                if(null!=directories){
                	for(IImageMetadataItem dir:directories){
                		out.println(dir);
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

                out.println("    " + "GPS Latitude: "
                        + gpsLatitudeDegrees.toDisplayString() + " degrees, "
                        + gpsLatitudeMinutes.toDisplayString() + " minutes, "
                        + gpsLatitudeSeconds.toDisplayString() + " seconds "
                        + gpsLatitudeRef);
                out.println("    " + "GPS Longitude: "
                        + gpsLongitudeDegrees.toDisplayString() + " degrees, "
                        + gpsLongitudeMinutes.toDisplayString() + " minutes, "
                        + gpsLongitudeSeconds.toDisplayString() + " seconds "
                        + gpsLongitudeRef);

            }

            out.println();

            final List<IImageMetadataItem> items = jpegMetadata.getItems();
            for (int i = 0; i < items.size(); i++) {
                final IImageMetadataItem item = items.get(i);
                out.println("    " + "item: " + item);
            }

            out.println();
        }
    }

    private static void printTagValue(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo, PrintWriter out) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            out.println(tagInfo.name + ": " + "Not Found.");
        } else {
            out.println(tagInfo.name + ": "
                    + field.getValueDescription());
        }
    }
}
