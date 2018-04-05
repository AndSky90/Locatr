package com.i550.locatr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

public class LocatrFragment extends Fragment {
    private ImageView mImageView;
    private GoogleApiClient mClient;
    private static final String TAG = "LocatrFragment";
    private static final int REQUEST_LOC_PERMS = 0;
    private static final String[] LOC_PERMS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,};       //разрешения которые надо запрашивать
    //предоставляются на уровне групп

    private boolean hasLocationPermission(){        //проверка разрешений
        int result = ContextCompat.checkSelfPermission(getActivity(),LOC_PERMS[0]);     //проверяем разрешение у 1го элемента
        return result == PackageManager.PERMISSION_GRANTED;                             //если разрешено то группа разрешена
    }


    public static LocatrFragment newInstance(){
        return new LocatrFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);    // --есть меню
        mClient = new GoogleApiClient.Builder(getActivity()) //создаем клиента
                .addApi(LocationServices.API)               //с таким АПИ
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {     //инфо о состоянии подключения = колбэк
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getActivity().invalidateOptionsMenu();              //перерисовать при коннекте
                    }

                    @Override
                    public void onConnectionSuspended(int i) {}
                })
                .build();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_locatr,container,false);
        return v;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_locatr,menu);
        MenuItem searchItem = menu.findItem(R.id.action_locate);
        searchItem.setEnabled(mClient.isConnected());   //если клиент запущен - включаем кнопку поиска
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {   //запуск кнопки поиска
        switch (item.getItemId()) {
            case R.id.action_locate:
                if (hasLocationPermission()){
                findImage();
                } else {
                    requestPermissions(LOC_PERMS,REQUEST_LOC_PERMS);    //запрашиваем разрешение с параметрами (асинх запрос)
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOC_PERMS:
                if (hasLocationPermission()) findImage();
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();  //обновляем прорисовку кнопки и стартуем клиент
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();       //останавливаем клиент!
    }

    private void findImage(){       //запрос к FusedLocation
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);    //приоритет между точностью и зарядом аккума
        request.setNumUpdates(1);
        request.setInterval(0);
        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, new LocationListener() {     //вызывает секуритиЭксепшн, надо запросить разрешение
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "Got a fix: " + location);
                new SearchTask().execute(location); //ИЩЕМ
            }
        });
    }

    private class SearchTask extends AsyncTask<Location,Void,Void>{ //ИЩЕТ
        private GalleryItem mGalleryItem;
        private Bitmap mBitmap;

        @Override
        protected Void doInBackground(Location... params) {
            FlickrFetchr fetchr= new FlickrFetchr();
            List<GalleryItem> items = fetchr.searchPhotos(params[0]);
            if(items.size()==0) return null;
            mGalleryItem=items.get(0);
            try{
                byte[] bytes = fetchr.getUrlBytes(mGalleryItem.getmUrl());
                mBitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            } catch (IOException ioe) {
                Log.i(TAG, "Unable to download bitmap", ioe);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mImageView.setImageBitmap(mBitmap);
        }
    }
}
