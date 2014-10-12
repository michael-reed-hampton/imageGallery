imageGallery
============

A web application for the display of images.  

What It Does
------------------------ 
Given a directory, this scans the directory for  pictures it understands, and indexes them using the EXIF data, and file names to allow you to search for images.

It uses the powerful and flexible lucene search syntax to allow users to perform advanced searches, or simplistic searchs.

It generates thumbnails to reduce bandwidth used, and make clients faster by using less memory.

Once indexed, it monitors the configured directory for changes to files of interest.

It builds multiple solr core indexes for each configured directory.



