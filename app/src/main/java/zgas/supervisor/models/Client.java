package zgas.supervisor.models;

public class Client {

    public static boolean isIsLoad() {
        return isLoad;
    }

    private static boolean isLoad = false; //Llave de carga

    String id;
    String nombre;
    String apellido;
    String telefono;

    public Client() {
        nombre="";
        apellido="";
        telefono="";
    }


    public Client(String id, String nombre, String apellido, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
    }

    public static boolean isLoad() {
        return isLoad;
    }

    public static void setIsLoad(boolean isLoad) {
        Client.isLoad = isLoad;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }



}
