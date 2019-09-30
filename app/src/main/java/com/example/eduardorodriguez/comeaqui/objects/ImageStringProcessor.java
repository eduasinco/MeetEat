package com.example.eduardorodriguez.comeaqui.objects;

class ImageStringProcessor {

    public static String server = "http://10.0.0.36:65100";

    static String processString(String imageString){

        if (imageString == null) {
            return "";
        }
        if (!imageString.contains("http")) {
            String s = server + imageString;
            return s;
        }
        return imageString;
    }
}
