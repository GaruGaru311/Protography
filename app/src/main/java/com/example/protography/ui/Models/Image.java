package com.example.protography.ui.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Image implements Parcelable {
    private String ImageTitle;
    private String ImageUrl;
    private String ImageDescription;
    private String ImageEquipment;
    private String ImageSettings;
    private String ImageTime;
    private String ImageTips;
    private String CoordsIndex;
    private double Latitude;
    private double Longitude;
    private String ImageCategory;
    private String ImageNameUser;
    private int SavesNumber;
    private String Uid;

    public Image(){ }

    public Image(String uid, String title, String url, String description, String settings, String time, String tips, String equipment, String coords, Double latitude, Double longitude, String category, String nameUser, int savesNumber){
        ImageTitle = title;
        ImageUrl = url;
        ImageDescription = description;
        ImageEquipment = equipment;
        ImageSettings = settings;
        ImageTime = time;
        ImageTips = tips;
        CoordsIndex = coords;
        Latitude = latitude;
        Longitude = longitude;
        ImageCategory = category;
        ImageNameUser = nameUser;
        SavesNumber = savesNumber;
        Uid = uid;
    }

    protected Image(Parcel in) {
        ImageTitle = in.readString();
        ImageUrl = in.readString();
        ImageDescription = in.readString();
        ImageEquipment = in.readString();
        ImageSettings = in.readString();
        ImageTime = in.readString();
        ImageTips = in.readString();
        CoordsIndex = in.readString();
        Latitude = in.readDouble();
        Longitude = in.readDouble();
        ImageCategory = in.readString();
        ImageNameUser = in.readString();
        SavesNumber = in.readInt();
        Uid = in.readString();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    public String getImageUid(){
        return Uid;
    }

    public void setImageUid(String uid){
        Uid = uid;
    }

    public String getImageTitle(){
        return ImageTitle;
    }

    public void setImageTitle(String title){
        ImageTitle = title;
    }

    public String getImageUrl(){
        return ImageUrl;
    }

    public void setImageUrl(String url){
        ImageUrl = url;
    }

    public String getImageDescription(){
        return ImageDescription;
    }

    public void setImageDescription(String description){
        ImageDescription = description;
    }

    public String getImageEquipment(){
        return ImageEquipment;
    }

    public void setImageEquipment(String equipment){
        ImageEquipment = equipment;
    }

    public String getImageTime(){return ImageTime;}

    public void setImageTime(String time){
        ImageTime = time;
    }

    public String getImageTips(){
        return ImageTips;
    }

    public void setImageTips(String tips){
        ImageTips = tips;
    }

    public String getImageSettings(){
        return ImageSettings;
    }

    public void setImageSettings(String settings){
        ImageSettings = settings;
    }

    public String getCoords(){
        return CoordsIndex;
    }

    public void setCoords(String coords){
        CoordsIndex = coords;
    }

    public Double getLatitude(){
        return Latitude;
    }

    public void setLatitude(Double latitude){
        Latitude = latitude;
    }

    public Double getLongitude(){
        return Longitude;
    }

    public void setLongitude(Double longitude){
        Longitude = longitude;
    }

    public String getImageCategory(){
        return ImageCategory;
    }

    public void setImageCategory(String category){
        ImageCategory = category;
    }

    public String getImageNameUser() { return ImageNameUser; }

    public void setImageNameUser(String nameUser) { ImageNameUser = nameUser; }

    public int getSavesNumber() { return SavesNumber; }

    public void setSavesNumber(int savesNumber) { SavesNumber = savesNumber; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ImageTitle);
        dest.writeString(ImageUrl);
        dest.writeString(ImageDescription);
        dest.writeString(ImageEquipment);
        dest.writeString(ImageSettings);
        dest.writeString(ImageTime);
        dest.writeString(ImageTips);
        dest.writeString(CoordsIndex);
        dest.writeDouble(Latitude);
        dest.writeDouble(Longitude);
        dest.writeString(ImageCategory);
        dest.writeString(ImageNameUser);
        dest.writeInt(SavesNumber);
    }
}