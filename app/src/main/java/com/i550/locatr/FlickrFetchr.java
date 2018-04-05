package com.i550.locatr;


import android.location.Location;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class FlickrFetchr {     //класс сетевых функций
    private static final String TAG = "FlickrFetchr";
    private static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final String API_KEY = "9a0554259914a86fb9e7eb014e4e5d52";
    private static final Uri    ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")  //строим запрос
                    .buildUpon()        //Uri.Builder
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")                     //json возвратить
                    .appendQueryParameter("nojsoncallback", "1")                //убирает из ответа имя и скобки
                    .appendQueryParameter("extras", "url_s")    // возвр URL для уменьшенной картинки
                    .build();

        //    .toString();

    public byte[] getUrlBytes(String urlSpec) throws IOException{   //получает дату по УРЛ и возвращает массив байтов
        URL url = new URL(urlSpec); //урл на основе полученной строки
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //открывает коннект, получаем объект коннекта
        try{    //подготавливаем запрос
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();   //прочитываем дату из потока
            if(connection.getResponseCode()!= HttpURLConnection.HTTP_OK){               //если не ок то
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);  //сообщение об ошибке
            }

            int bytesRead = 0;      //колво прочитанных байтов = 0;
            byte[] buffer = new byte[1024];
            while ( (bytesRead = in.read(buffer)) >0) {       //многократно считываем пока не кончатся данные (bytesRead = in.read(buffer) и
                out.write(buffer, 0, bytesRead);        //записываем в вых поток
            }
            out.close();
            return out.toByteArray();                           //возвращаем массив прочитанных байтов из ByteArrayOutputStream
        } finally {
            connection.disconnect();
        }
    }                   //    <uses-permission android:name="android.permission.INTERNET"/> в манифест (неопасное, без запроса)


    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));        //тупо приводит в стринг;
    }

    public List<GalleryItem> fetchRecentPhotos(){
        String url = buildUrl(FETCH_RECENT_METHOD,null);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query){
        String url = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(Location location) {
        String url = buildUrl(location);
        return downloadGalleryItems(url);
    }

    private List<GalleryItem> downloadGalleryItems(String url){
        List<GalleryItem> galleryItems = new ArrayList<>();

        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);   //json разбирается в объект с иерархией
            parseItems(galleryItems,jsonBody);      //запускаем разбор json-a;
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items ", ioe);
        } catch (JSONException je) {                            //ловим эксепшн разбора в json
            Log.e(TAG, "Failed to parse JSON ", je);
        }
        return galleryItems;
    }

    private String buildUrl(String method, String query){   //метод для построения URL запроса
        Uri.Builder uriBuilder = ENDPOINT.
                buildUpon().
                appendQueryParameter("method",method);
        if(method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }
        return uriBuilder.build().toString();
    }

    private String buildUrl(Location location){     //запрос поиска фото по местоположению
        return ENDPOINT.buildUpon()
                .appendQueryParameter("method", SEARCH_METHOD)
                .appendQueryParameter("lat","" + location.getLatitude())
                .appendQueryParameter("lon", "" + location.getLongitude())
                .build().toString();
    }


    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {
        //метод для извлечения инфы каждой фотографии, помещаем ее в Gallery item и кладем в список

        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");     //получаем объект "photos"
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");  //в нем берем array с фотками "photo"
        for (int i=0; i<photoJsonArray.length(); i++){                  //перебираем массив
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);   //получаем элемент массива
            GalleryItem item = new GalleryItem();               //создаем итем
            item.setmId(photoJsonObject.getString("id"));       //получаем id фотки и ложим в итем
            item.setmCaption(photoJsonObject.getString("title"));   //получаем title фотки и ложим в итем
            if(!photoJsonObject.has("url_s")){          //если нет "url_s"
                continue;
            }
            item.setmUrl(photoJsonObject.getString("url_s")); //получаем url_s фотки и ложим в итем
            item.setOwner(photoJsonObject.getString("owner"));  //получаем владельца из Jsonа и ложим в итем
            items.add(item);            //получившийся итем ложим в список
        }
    }
}
