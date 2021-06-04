package zgas.supervisor.models;

import com.google.android.gms.maps.model.LatLng;

public class Driver {

    public Driver(String id, String nombre, String apellido, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    String unidad;

    public Driver(){unidad="";}

    String id;
    String nombre;
    String apellido;
    String telefono;

    public String getId() {
        return id;
    }

    public Driver setId(String id) {
        this.id = id;
        return this;
    }

    public String getNombre() {
        return nombre;
    }

    public Driver setNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public String getApellido() {
        return apellido;
    }

    public Driver setApellido(String apellido) {
        this.apellido = apellido;
        return this;
    }

    public String getTelefono() {
        return telefono;
    }

    public Driver setTelefono(String telefono) {
        this.telefono = telefono;
        return this;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public Driver setLatLng(LatLng latLng) {
        this.latLng = latLng;
        return this;
    }

    LatLng latLng;

}
