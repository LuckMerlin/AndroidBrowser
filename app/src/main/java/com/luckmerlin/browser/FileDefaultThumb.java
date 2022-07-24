package com.luckmerlin.browser;

public final class FileDefaultThumb {

    public static Integer thumb(String mime){
            Integer iconId = null;
            if (null == mime || mime.length() <= 0) {
                iconId = R.drawable.hidisk_icon_unknown;
            } else if (mime.endsWith("application/zip")) {
                iconId = R.drawable.hidisk_icon_zip;
            } else if (mime.endsWith("application/x-rar")) {
                iconId = R.drawable.hidisk_icon_rar;
            } else if (mime.endsWith("application/x-7z-compressed")) {
                iconId = R.drawable.hidisk_icon_7z;
            } else if (mime.endsWith("application/x-gzip") || mime.endsWith("application/x-bzip2")
                    || mime.endsWith("application/x-tar")) {
                iconId = R.drawable.hidisk_icon_compressed_files;
            } else if (mime.endsWith("application/vnd.ms-excel")) {
                iconId = R.drawable.hidisk_icon_xls;
            } else if (mime.endsWith("text/html")) {
                iconId = R.drawable.hidisk_icon_html;
            } else if (mime.endsWith("audio/amr")) {
                iconId = R.drawable.hidisk_icon_music_amr;
            } else if (mime.endsWith("application/xml")) {
                iconId = R.drawable.hidisk_icon_xml;
            } else if (mime.endsWith("audio/flac")) {
                iconId = R.drawable.hidisk_icon_music_flac;
            } else if (mime.endsWith("audio/mp4")) {
                iconId = R.drawable.hidisk_icon_music_m4a;
            } else if (mime.endsWith("application/vnd.ms-powerpoint")) {
                iconId = R.drawable.hidisk_icon_ppt;
            } else if (mime.endsWith("application/pdf")) {
                iconId = R.drawable.hidisk_icon_pdf;
            } else if (mime.endsWith("audio/x-ms-wma")) {
                iconId = R.drawable.hidisk_icon_music_wma;
            } else if (mime.endsWith("audio/x-wav")) {
                iconId = R.drawable.hidisk_icon_music_wav;
            } else if (mime.endsWith("audio/ape")) {
                iconId = R.drawable.hidisk_icon_music_ape;
            } else if (mime.endsWith("application/octet-stream")) {
                iconId = R.drawable.hidisk_icon_exe;
            } else if (mime.endsWith("/rar")) {
                iconId = R.drawable.hidisk_icon_rar;
            } else if (mime.endsWith("application/vnd.ms-word")) {
                iconId = R.drawable.hidisk_icon_doc;
            } else if (mime.endsWith("audio/mpeg")) {
                iconId = R.drawable.hidisk_icon_music_mp3;
            } else if (mime.endsWith("application/mshelp")) {
                iconId = R.drawable.hidisk_icon_chm;
            } else if (mime.startsWith("text/")) {
                iconId = R.drawable.hidisk_icon_text;
            } else if (mime.startsWith("audio/")) {
                iconId = R.drawable.hidisk_icon_music;
            } else if (mime.startsWith("video/")) {
                iconId = R.drawable.hidisk_icon_video;
            } else if (mime.equalsIgnoreCase(".log")) {
                iconId = R.drawable.hidisk_icon_log;
            }
            return iconId;
    }

}
