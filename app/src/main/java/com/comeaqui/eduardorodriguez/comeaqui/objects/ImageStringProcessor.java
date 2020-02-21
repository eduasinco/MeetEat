package com.comeaqui.eduardorodriguez.comeaqui.objects;

class ImageStringProcessor {

    public static String server1 = "http://10.0.0.44:65100";
    public static String server2 = "http://13.52.249.41";

    static String processString(String imageString){

        if (imageString == null) {
            return "";
        }
        if (!imageString.contains("http")) {
            String s = server2 + imageString;
            return s;
        }
        return imageString;
    }
}
