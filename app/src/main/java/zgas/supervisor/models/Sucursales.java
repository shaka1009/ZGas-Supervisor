package zgas.supervisor.models;

public class Sucursales {
    public String getSucursal() {
        return sucursal;
    }

    public Sucursales setSucursal(String sucursal) {
        this.sucursal = sucursal;
        return this;
    }

    public String getDireccion() {
        return direccion;
    }

    public Sucursales setDireccion(String direccion) {
        this.direccion = direccion;
        return this;
    }

    public String getTelefono() {
        return telefono;
    }

    public Sucursales setTelefono(String telefono) {
        this.telefono = telefono;
        return this;
    }

    public String getHorario() {
        return horario;
    }

    public Sucursales setHorario(String horario) {
        this.horario = horario;
        return this;
    }

    public Sucursales(String sucursal, String direccion, String telefono, String horario) {
        this.sucursal = sucursal;
        this.direccion = direccion;
        this.telefono = telefono;
        this.horario = horario;
    }

    String sucursal;
    String direccion;
    String telefono;
    String horario;
}
