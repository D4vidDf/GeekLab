package com.daviddf.geeklab;

import android.graphics.Bitmap;

public class Experiments {
    String  Imagen,Titulo, Url;

    public Experiments(){}

    public Experiments(String imagen, String titulo, String url) {
        Imagen = imagen;
        Titulo = titulo;
        Url = url;
    }

    public String getImagen() {
        return Imagen;
    }

    public void setImagen(String imagen) {
        Imagen = imagen;
    }

    public String getTitulo() {
        return Titulo;
    }

    public void setTitulo(String titulo) {
        Titulo = titulo;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

}
